/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db.hibernate.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.hibernate.search.engine.search.predicate.SearchPredicate;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;
import org.hibernate.search.engine.search.query.SearchQuery;
import org.hibernate.search.mapper.orm.scope.SearchScope;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.api.PersonService;
import org.openmrs.api.db.hibernate.search.SearchQueryUnique.DeduplicationResult;
import org.openmrs.api.db.hibernate.search.session.SearchSessionFactory;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Verifies that {@link SearchQueryUnique} deduplicates correctly while bounding its work to the
 * requested page (list path) and to a single index scan per query (count path).
 */
public class SearchQueryUniqueTest extends BaseContextSensitiveTest {

	private static final String FAMILY_NAME = "zzzbound";

	private static final int PERSON_COUNT = 20;

	private static final int DUPLICATE_COUNT = 5;

	private static final int TOTAL_NAME_HITS = PERSON_COUNT + DUPLICATE_COUNT;

	// Fixture for the joined (multi-sub-query) tests: persons matching only the first sub-query, only
	// the second, and both (which must be deduplicated to one person across the join).
	private static final String FAMILY_A = "aaajoin";

	private static final String FAMILY_B = "bbbjoin";

	private static final int ONLY_A = 6;

	private static final int ONLY_B = 4;

	private static final int BOTH = 3;

	private static final int DISTINCT_JOINED = ONLY_A + ONLY_B + BOTH;

	private static final int RAW_JOINED = (ONLY_A + BOTH) + (ONLY_B + BOTH);

	@Autowired
	private SearchSessionFactory searchSessionFactory;

	@Autowired
	private PersonService personService;

	@BeforeEach
	public void createMatchingPersons() {
		// Every person shares the same family name so a single predicate matches them all. The first
		// DUPLICATE_COUNT persons get a second matching name, producing hits that share a person id and
		// therefore must be deduplicated.
		for (int i = 0; i < PERSON_COUNT; i++) {
			Person person = new Person();
			person.setGender("M");
			person.addName(new PersonName("Given" + i, null, FAMILY_NAME));
			if (i < DUPLICATE_COUNT) {
				person.addName(new PersonName("Alt" + i, null, FAMILY_NAME));
			}
			personService.savePerson(person);
		}

		updateSearchIndex();
	}

	private Function<SearchPredicateFactory, SearchPredicate> matchingPredicate() {
		return matchingPredicate(FAMILY_NAME);
	}

	private Function<SearchPredicateFactory, SearchPredicate> matchingPredicate(String familyName) {
		return f -> f.match().field("familyNameExact").matching(familyName).toPredicate();
	}

	/**
	 * The page mapper handed to {@link SearchQueryUnique}: it receives the whole page of hits at once
	 * so a real caller can batch-load, mirroring the DAOs. Here it simply resolves each name to its
	 * person.
	 */
	private static Function<List<PersonName>, List<Person>> personMapper() {
		return names -> names.stream().map(PersonName::getPerson).collect(Collectors.toList());
	}

	private SearchQueryUnique<PersonName, Person> personNameQuery() {
		return SearchQueryUnique.newQuery(PersonName.class, matchingPredicate(), "person.personId", personMapper());
	}

	/**
	 * Builds a two-sub-query join over PersonName: the first sub-query matches {@link #FAMILY_A}, the
	 * second {@link #FAMILY_B}. Persons matching both must be returned/counted once across the join.
	 */
	private SearchQueryUnique<?, Person> joinedQuery() {
		return SearchQueryUnique.newQuery(PersonName.class, matchingPredicate(FAMILY_A), "person.personId", personMapper())
		        .join(SearchQueryUnique.newQuery(PersonName.class, matchingPredicate(FAMILY_B), "person.personId",
		            personMapper()));
	}

	private void createJoinedFixture() {
		for (int i = 0; i < ONLY_A; i++) {
			savePersonWithFamilyNames(FAMILY_A);
		}
		for (int i = 0; i < ONLY_B; i++) {
			savePersonWithFamilyNames(FAMILY_B);
		}
		for (int i = 0; i < BOTH; i++) {
			savePersonWithFamilyNames(FAMILY_A, FAMILY_B);
		}
		updateSearchIndex();
	}

	private void savePersonWithFamilyNames(String... familyNames) {
		Person person = new Person();
		person.setGender("F");
		int i = 0;
		for (String familyName : familyNames) {
			person.addName(new PersonName("Join" + (i++), null, familyName));
		}
		personService.savePerson(person);
	}

	private static List<Integer> personIds(List<Person> persons) {
		return persons.stream().map(Person::getPersonId).collect(Collectors.toList());
	}

	@Test
	public void searchCount_shouldPerformOnlyOneQueryExecutionPerQuery() {
		// Regression test for the count path: when the result set is within the deduplication cap the
		// total hit count is computed from a single index-level scroll per query, so the follow-up
		// filtering query and the separate total-hit-count query that the previous implementation issued
		// must no longer be executed.
		SearchSession spySession = spy(searchSessionFactory.getSearchSession());
		SearchSessionFactory spyFactory = () -> spySession;

		Long count = SearchQueryUnique.searchCount(spyFactory, personNameQuery());

		assertEquals(Long.valueOf(PERSON_COUNT), count);
		verify(spySession, times(1)).search(any(SearchScope.class));
	}

	@Test
	public void searchCount_shouldReturnExactCountByDefault() {
		// The no-cap overload deduplicates without bound, so even the duplicate names collapse to the
		// exact distinct person count.
		Long count = SearchQueryUnique.searchCount(searchSessionFactory, personNameQuery());

		assertEquals(Long.valueOf(PERSON_COUNT), count);
	}

	@Test
	public void searchCount_shouldReturnRawUpperBoundWhenDistinctHitsExceedCap() {
		// With a cap below the number of distinct persons the exact deduplication is abandoned and the
		// raw hit count (which counts the duplicate names) is returned as an upper bound.
		Long count = SearchQueryUnique.searchCount(searchSessionFactory, personNameQuery(), DUPLICATE_COUNT);

		assertEquals(Long.valueOf(TOTAL_NAME_HITS), count);
	}

	@Test
	public void collectUniqueKeys_shouldStopScanningOnceCapExceeded() {
		int cap = DUPLICATE_COUNT;
		int chunkSize = 10;
		Set<Object> uniqueKeys = new LinkedHashSet<>();

		int scannedHitCount = SearchQueryUnique.collectUniqueKeys(uniqueKeyOnlyQuery(), uniqueKeys, cap, chunkSize);

		assertEquals(cap + 1, uniqueKeys.size(), "should collect one key past the cap and then stop");
		assertTrue(scannedHitCount <= chunkSize,
		    "expected the scroll to stop within the first chunk but scanned " + scannedHitCount);
		assertTrue(scannedHitCount < TOTAL_NAME_HITS, "expected fewer scanned hits than the full result set");
	}

	@Test
	public void collectUniqueKeys_shouldScanEverythingWhenBelowCap() {
		Set<Object> uniqueKeys = new LinkedHashSet<>();

		int scannedHitCount = SearchQueryUnique.collectUniqueKeys(uniqueKeyOnlyQuery(), uniqueKeys, 1000, 10);

		assertEquals(TOTAL_NAME_HITS, scannedHitCount);
		assertEquals(PERSON_COUNT, uniqueKeys.size());
	}

	@Test
	public void search_shouldReturnDeduplicatedResultsForFullResultSet() {
		List<Person> results = SearchQueryUnique.search(searchSessionFactory, personNameQuery(), 0, PERSON_COUNT);

		assertEquals(PERSON_COUNT, results.size());
		Set<Integer> personIds = results.stream().map(Person::getPersonId).collect(Collectors.toSet());
		assertEquals(PERSON_COUNT, personIds.size(), "results must not contain duplicate persons");
	}

	@Test
	public void search_shouldReturnAllDeduplicatedResultsWhenNoOffsetOrLimit() {
		// The no-offset/no-limit overload fetches the entire deduplicated result set (the "fetch
		// everything" path); the deduplication scroll is unbounded in this case.
		List<Person> results = SearchQueryUnique.search(searchSessionFactory, personNameQuery());

		assertEquals(PERSON_COUNT, results.size());
		Set<Integer> personIds = results.stream().map(Person::getPersonId).collect(Collectors.toSet());
		assertEquals(PERSON_COUNT, personIds.size(), "results must not contain duplicate persons");
	}

	@Test
	public void search_shouldReturnDeduplicatedPage() {
		int pageSize = 5;

		List<Person> results = SearchQueryUnique.search(searchSessionFactory, personNameQuery(), 0, pageSize);

		assertEquals(pageSize, results.size());
		Set<Integer> personIds = results.stream().map(Person::getPersonId).collect(Collectors.toSet());
		assertEquals(pageSize, personIds.size(), "results must not contain duplicate persons");
	}

	@Test
	public void search_shouldMapEachPageInASingleBatchedCall() {
		// The whole page must be handed to the mapper in a single call so a real caller can hydrate it in
		// one batched load; the previous implementation mapped hit-by-hit, issuing one load per hit.
		int pageSize = 5;
		List<Integer> mappedPageSizes = new ArrayList<>();
		Function<List<PersonName>, List<Person>> countingMapper = names -> {
			mappedPageSizes.add(names.size());
			return personMapper().apply(names);
		};
		SearchQueryUnique<PersonName, Person> query = SearchQueryUnique.newQuery(PersonName.class, matchingPredicate(),
		    "person.personId", countingMapper);

		List<Person> results = SearchQueryUnique.search(searchSessionFactory, query, 0, pageSize);

		assertEquals(pageSize, results.size());
		assertEquals(Arrays.asList(pageSize), mappedPageSizes, "the page must be mapped in a single batched call");
	}

	@Test
	public void search_shouldMapEachJoinedSubQueryInASingleBatchedCall() {
		// Across a join the mapping stays bounded by the number of sub-queries, not the number of hits:
		// each sub-query that contributes to the page maps its whole contribution in one call.
		createJoinedFixture();
		List<Integer> mappedPageSizes = new ArrayList<>();
		Function<List<PersonName>, List<Person>> countingMapper = names -> {
			mappedPageSizes.add(names.size());
			return personMapper().apply(names);
		};
		SearchQueryUnique<?, Person> query = SearchQueryUnique
		        .newQuery(PersonName.class, matchingPredicate(FAMILY_A), "person.personId", countingMapper)
		        .join(SearchQueryUnique.newQuery(PersonName.class, matchingPredicate(FAMILY_B), "person.personId",
		            countingMapper));

		List<Person> results = SearchQueryUnique.search(searchSessionFactory, query, 0, DISTINCT_JOINED);

		assertEquals(DISTINCT_JOINED, results.size());
		assertTrue(mappedPageSizes.size() <= 2,
		    "expected at most one mapper call per sub-query but got " + mappedPageSizes.size());
		assertEquals(DISTINCT_JOINED, mappedPageSizes.stream().mapToInt(Integer::intValue).sum(),
		    "every distinct result must be produced by the batched mapper calls");
	}

	@Test
	public void search_shouldTolerateMapperDroppingHits() {
		// A mapper may return fewer items than the page it was given: the DAO mappers drop hits whose
		// backing row has vanished from the database (a stale search-index entry). Such a dropped hit must
		// simply be absent from the results and must never surface as a null.
		List<Person> full = SearchQueryUnique.search(searchSessionFactory, personNameQuery(), 0, PERSON_COUNT);
		Integer droppedId = full.get(0).getPersonId();
		Function<List<PersonName>, List<Person>> droppingMapper = names -> personMapper().apply(names).stream()
		        .filter(person -> !person.getPersonId().equals(droppedId)).collect(Collectors.toList());
		SearchQueryUnique<PersonName, Person> query = SearchQueryUnique.newQuery(PersonName.class, matchingPredicate(),
		    "person.personId", droppingMapper);

		List<Person> results = SearchQueryUnique.search(searchSessionFactory, query, 0, 5);

		assertFalse(results.contains(null), "a dropped hit must not surface as a null result");
		assertFalse(results.stream().anyMatch(person -> person.getPersonId().equals(droppedId)),
		    "the dropped hit must be absent from the results");
	}

	@Test
	public void search_shouldReturnCorrectPageForNonZeroOffset() {
		// The second page must be the corresponding slice of the full ordered result, with no overlap or
		// gap relative to the first page.
		List<Integer> all = personIds(SearchQueryUnique.search(searchSessionFactory, personNameQuery(), 0, PERSON_COUNT));
		List<Integer> page = personIds(SearchQueryUnique.search(searchSessionFactory, personNameQuery(), 5, 5));

		assertEquals(all.subList(5, 10), page);
	}

	@Test
	public void searchCount_shouldDeduplicateAcrossJoinedQueries() {
		// A person matching both sub-queries must be counted once, so the count is the distinct person
		// count, not the summed raw hit count of the two sub-queries.
		createJoinedFixture();

		Long count = SearchQueryUnique.searchCount(searchSessionFactory, joinedQuery());

		assertEquals(Long.valueOf(DISTINCT_JOINED), count);
	}

	@Test
	public void search_shouldDeduplicatePersonsMatchedByMultipleJoinedQueries() {
		createJoinedFixture();

		List<Person> results = SearchQueryUnique.search(searchSessionFactory, joinedQuery(), 0, DISTINCT_JOINED);

		assertEquals(DISTINCT_JOINED, results.size());
		Set<Integer> distinct = results.stream().map(Person::getPersonId).collect(Collectors.toSet());
		assertEquals(DISTINCT_JOINED, distinct.size(), "a person matched by both sub-queries must appear once");
	}

	@Test
	public void search_shouldPageAcrossJoinedQueryBoundary() {
		// The first sub-query yields ONLY_A + BOTH distinct persons; a page whose offset lands near that
		// boundary must straddle both sub-queries correctly, matching the corresponding slice of the full
		// ordered result.
		createJoinedFixture();

		int offset = ONLY_A + BOTH - 2;
		int limit = 4;
		List<Integer> all = personIds(SearchQueryUnique.search(searchSessionFactory, joinedQuery(), 0, DISTINCT_JOINED));
		List<Integer> page = personIds(SearchQueryUnique.search(searchSessionFactory, joinedQuery(), offset, limit));

		assertEquals(all.subList(offset, offset + limit), page);
		assertEquals(limit, page.stream().distinct().count(), "page must not contain duplicate persons");
	}

	@Test
	public void searchCount_shouldReturnRawUpperBoundAcrossJoinedQueriesWhenCapExceeded() {
		createJoinedFixture();

		Long count = SearchQueryUnique.searchCount(searchSessionFactory, joinedQuery(), BOTH);

		assertEquals(Long.valueOf(RAW_JOINED), count);
	}

	@Test
	public void collectDuplicateIds_shouldStopScanningOncePageIsSatisfied() {
		SearchQuery<List<?>> uniqueKeyQuery = uniqueKeyQuery();

		int pageSize = DUPLICATE_COUNT; // fits within a single (small) scroll chunk
		int chunkSize = 10;
		DeduplicationResult bounded = SearchQueryUnique.collectDuplicateIds(uniqueKeyQuery, new LinkedHashSet<>(), 1000,
		    pageSize, chunkSize);

		assertEquals(pageSize, bounded.newUniqueKeyCount);
		assertTrue(bounded.scannedHitCount <= chunkSize,
		    "expected the scroll to stop within the first chunk but scanned " + bounded.scannedHitCount);
		assertTrue(bounded.scannedHitCount < TOTAL_NAME_HITS, "expected fewer scanned hits than the full result set");
	}

	@Test
	public void collectDuplicateIds_shouldScanEverythingWhenUnbounded() {
		SearchQuery<List<?>> uniqueKeyQuery = uniqueKeyQuery();

		DeduplicationResult unbounded = SearchQueryUnique.collectDuplicateIds(uniqueKeyQuery, new LinkedHashSet<>(), 1000,
		    null, 10);

		assertEquals(TOTAL_NAME_HITS, unbounded.scannedHitCount);
		assertEquals(PERSON_COUNT, unbounded.newUniqueKeyCount);
		assertEquals(DUPLICATE_COUNT, unbounded.duplicateIds.size());
	}

	@Test
	public void collectDuplicateIds_shouldStopScanningOnceMaxClauseCountReached() {
		// With more duplicates than maxClauseCount, the scroll must stop once the clause limit is
		// reached (it collects one duplicate id past the limit) rather than scanning the whole hit set.
		int maxClauseCount = 2;
		DeduplicationResult bounded = SearchQueryUnique.collectDuplicateIds(uniqueKeyQuery(), new LinkedHashSet<>(),
		    maxClauseCount, null, SearchQueryUnique.DEFAULT_SCROLL_CHUNK_SIZE);

		assertEquals(maxClauseCount + 1, bounded.duplicateIds.size());
		assertTrue(bounded.scannedHitCount < TOTAL_NAME_HITS, "expected fewer scanned hits than the full result set");
	}

	@Test
	public void search_shouldRejectJoinedSubQueryWithoutUniqueKey() {
		// A joined sub-query cannot participate in cross-sub-query dedup / offset carry without a unique
		// key, so this unsupported configuration must fail loudly rather than mis-paginate.
		SearchQueryUnique<?, Person> query = SearchQueryUnique
		        .newQuery(PersonName.class, matchingPredicate(FAMILY_A), null, personMapper()).join(SearchQueryUnique
		                .newQuery(PersonName.class, matchingPredicate(FAMILY_B), "person.personId", personMapper()));

		assertThrows(IllegalStateException.class, () -> SearchQueryUnique.search(searchSessionFactory, query, 0, 10));
	}

	private SearchQuery<List<?>> uniqueKeyQuery() {
		SearchSession searchSession = searchSessionFactory.getSearchSession();
		SearchScope<PersonName> scope = searchSession.scope(PersonName.class);
		SearchPredicate predicate = matchingPredicate().apply(scope.predicate());
		return searchSession.search(scope).select(f -> f.composite(f.field("person.personId"), f.id())).where(predicate)
		        .toQuery();
	}

	private SearchQuery<?> uniqueKeyOnlyQuery() {
		SearchSession searchSession = searchSessionFactory.getSearchSession();
		SearchScope<PersonName> scope = searchSession.scope(PersonName.class);
		SearchPredicate predicate = matchingPredicate().apply(scope.predicate());
		return searchSession.search(scope).select(f -> f.field("person.personId")).where(predicate).toQuery();
	}
}
