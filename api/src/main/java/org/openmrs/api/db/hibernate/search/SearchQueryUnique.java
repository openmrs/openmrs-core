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
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.lucene.search.BooleanQuery;
import org.hibernate.search.engine.search.predicate.SearchPredicate;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;
import org.hibernate.search.engine.search.query.SearchQuery;
import org.hibernate.search.engine.search.query.SearchScroll;
import org.hibernate.search.engine.search.query.SearchScrollResult;
import org.hibernate.search.mapper.orm.scope.SearchScope;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.openmrs.api.db.hibernate.search.session.SearchSessionFactory;

/**
 * Provides methods for removing duplicate search results based on the given uniqueKey and combining
 * search results from multiple queries.
 * <p>
 * When fetching results, deduplication is bounded to the requested page: the follow-up filtering
 * query removes at most <code>max = {@link BooleanQuery#getMaxClauseCount()} / 2.5</code> duplicate
 * keys, and up to <code>max</code> unique keys from previous queries are removed from results in
 * the following queries.
 * <p>
 * When calculating a total hit count, deduplication is exact by default but can be bounded by the
 * caller via {@link #searchCount(SearchSessionFactory, SearchQueryUnique, int)}: the count is exact
 * while the number of distinct hits stays within the given cap, and degrades to a raw upper bound
 * (which counts duplicates) above it. This lets an expensive search (e.g. patient search over a
 * huge hit set) bound the count cost while other callers keep exact counts.
 *
 * @param <T> query scope
 * @param <R> query return type
 * @since 2.8.0
 */
public class SearchQueryUnique<T, R> {

	/**
	 * Cap value for {@link #searchCount(SearchSessionFactory, SearchQueryUnique, int)} that keeps the
	 * total hit count exact by never falling back to the raw upper bound.
	 */
	public static final int UNBOUNDED_DEDUPLICATION = Integer.MAX_VALUE;

	/**
	 * The number of hits to pull from the index per scroll iteration when deduplicating.
	 */
	static final int DEFAULT_SCROLL_CHUNK_SIZE = 500;

	Class<? extends T> scope;

	Function<SearchPredicateFactory, SearchPredicate> search;

	Function<T, R> mapper;

	String uniqueKey;

	SearchQueryUnique<?, R> joinedQuery;

	public SearchQueryUnique(Class<? extends T> scope, Function<SearchPredicateFactory, SearchPredicate> search,
	    String uniqueKey, Function<T, R> mapper, SearchQueryUnique<?, R> joinedQuery) {
		this.scope = scope;
		this.search = search;
		this.mapper = mapper;
		this.uniqueKey = uniqueKey;
		this.joinedQuery = joinedQuery;
	}

	public Class<? extends T> getScope() {
		return scope;
	}

	public Function<SearchPredicateFactory, SearchPredicate> getSearch() {
		return search;
	}

	public Function<T, R> getMapper() {
		return mapper;
	}

	public String getUniqueKey() {
		return uniqueKey;
	}

	public SearchQueryUnique<?, R> getJoinedQuery() {
		return joinedQuery;
	}

	/**
	 * When joining a query, the algorithm will use the unique key values from the previous query (if
	 * any) to filter out items using its unique key.
	 *
	 * @param query the query to join
	 * @return joined queries
	 */
	public SearchQueryUnique<?, R> join(SearchQueryUnique<?, R> query) {
		joinedQuery = query;
		return this;
	}

	/**
	 * Creates a new query for {@link #search(SearchSessionFactory, SearchQueryUnique)}
	 *
	 * @param scope the index type to be searched
	 * @param search the search predicate
	 * @param uniqueKey the field to use as unique key, or <code>null</code> if none
	 * @param mapper the mapper from index type to result type, or <code>null</code> if none
	 * @return the search
	 * @param <T> the index type
	 * @param <R> the result type
	 */
	public static <T, R> SearchQueryUnique<T, R> newQuery(Class<? extends T> scope,
	        Function<SearchPredicateFactory, SearchPredicate> search, String uniqueKey, Function<T, R> mapper) {
		return new SearchQueryUnique<>(scope, search, uniqueKey, mapper, null);
	}

	/**
	 * See {@link #newQuery(Class, Function, String, Function)}.
	 *
	 * @param scope thh index type to be searched
	 * @param search the search predicate
	 * @param uniqueKey the field to use as unique key, or <code>null</code> if none
	 * @return the search
	 * @param <T> the index type
	 * @param <R> the result type
	 */
	public static <T, R> SearchQueryUnique<T, R> newQuery(Class<? extends T> scope,
	        Function<SearchPredicateFactory, SearchPredicate> search, String uniqueKey) {
		return new SearchQueryUnique<>(scope, search, uniqueKey, null, null);
	}

	public static class SearchUniqueResults<T> {

		List<T> results;

		Integer offset;

		Integer limit;

		Long totalHitCount;

		public SearchUniqueResults(List<T> results, Integer offset, Integer limit, Long totalHitCount) {
			this.results = results;
			this.offset = offset;
			this.limit = limit;
			this.totalHitCount = totalHitCount;
		}

		public List<T> getResults() {
			return results;
		}

		public Integer getOffset() {
			return offset;
		}

		public Integer getLimit() {
			return limit;
		}

		public Long getTotalHitCount() {
			return totalHitCount;
		}
	}

	/**
	 * Runs the search calculating the exact total hit count only.
	 *
	 * @param searchSessionFactory SearchSessionFactory
	 * @param uniqueQuery unique query {@link #newQuery(Class, Function, String, Function)}
	 * @return the total hit count
	 */
	public static Long searchCount(SearchSessionFactory searchSessionFactory, SearchQueryUnique<?, ?> uniqueQuery) {
		return searchCount(searchSessionFactory, uniqueQuery, UNBOUNDED_DEDUPLICATION);
	}

	/**
	 * Runs the search calculating the total hit count only, bounding the exact deduplication to
	 * <code>deduplicationCap</code> distinct hits. The count is exact while the number of distinct hits
	 * stays within the cap; above it the raw hit count (which counts duplicates, so an upper bound) is
	 * returned instead of scanning the whole hit set. Pass {@link #UNBOUNDED_DEDUPLICATION} for an
	 * always exact count.
	 *
	 * @param searchSessionFactory SearchSessionFactory
	 * @param uniqueQuery unique query {@link #newQuery(Class, Function, String, Function)}
	 * @param deduplicationCap the number of distinct hits to deduplicate exactly before falling back to
	 *            the raw upper bound
	 * @return the total hit count
	 */
	public static Long searchCount(SearchSessionFactory searchSessionFactory, SearchQueryUnique<?, ?> uniqueQuery,
	        int deduplicationCap) {
		return searchTotalHitCount(searchSessionFactory.getSearchSession(), uniqueQuery, deduplicationCap);
	}

	/**
	 * Runs the search without calculating the total hit count. See
	 * {@link #search(SearchSessionFactory, SearchQueryUnique, Integer, Integer, Boolean)}.
	 *
	 * @param searchSessionFactory SearchSessionFactory
	 * @param uniqueQuery unique query {@link #newQuery(Class, Function, String, Function)}
	 * @return the results
	 * @param <T> the type of results
	 */
	public static <T> List<T> search(SearchSessionFactory searchSessionFactory, SearchQueryUnique<?, T> uniqueQuery) {
		return search(searchSessionFactory, uniqueQuery, null, null, false).getResults();
	}

	/**
	 * Runs the search without calculating the total hit count. See
	 * {@link #search(SearchSessionFactory, SearchQueryUnique, Integer, Integer, Boolean)}.
	 *
	 * @param searchSessionFactory SearchSessionFactory
	 * @param uniqueQuery unique query {@link #newQuery(Class, Function, String, Function)}
	 * @param offset offset of results
	 * @param limit limit of results
	 * @return the results
	 * @param <T> the type of results
	 */
	public static <T> List<T> search(SearchSessionFactory searchSessionFactory, SearchQueryUnique<?, T> uniqueQuery,
	        Integer offset, Integer limit) {
		return search(searchSessionFactory, uniqueQuery, offset, limit, false).getResults();
	}

	/**
	 * Executes unique queries applying joins and mapping to the result type.
	 * <p>
	 * When <code>includeTotalHitCount</code> is <code>true</code> only the exact total hit count is
	 * calculated and the returned results are empty; use
	 * {@link #searchCount(SearchSessionFactory, SearchQueryUnique, int)} to bound the count cost.
	 * Otherwise the requested page of results is returned and the total hit count is <code>null</code>.
	 *
	 * @param searchSessionFactory search session factory
	 * @param uniqueQuery unique query {@link #newQuery(Class, Function, String, Function)}
	 * @param offset offset of results
	 * @param limit limit of results
	 * @param includeTotalHitCount calculate the total hit count instead of fetching results
	 * @return the results
	 * @param <T> the type of results
	 */
	public static <T> SearchUniqueResults<T> search(SearchSessionFactory searchSessionFactory,
	        SearchQueryUnique<?, T> uniqueQuery, final Integer offset, final Integer limit, Boolean includeTotalHitCount) {
		SearchSession searchSession = searchSessionFactory.getSearchSession();

		if (Boolean.TRUE.equals(includeTotalHitCount)) {
			// The count path only needs the total number of distinct hits, so it skips fetching and
			// hydrating a page of results entirely.
			long totalHitCount = searchTotalHitCount(searchSession, uniqueQuery, UNBOUNDED_DEDUPLICATION);
			return new SearchUniqueResults<>(new ArrayList<>(), offset, limit, totalHitCount);
		}

		return searchWithResults(searchSession, uniqueQuery, offset, limit);
	}

	/**
	 * Fetches the requested page of deduplicated results, walking the joined queries in order.
	 * <p>
	 * The deduplication scroll for each query is bounded to the requested page: it stops once
	 * <code>offset + limit</code> unique keys have been found, because any hit beyond that point sorts
	 * after the page and therefore cannot appear in it. This keeps the work proportional to the page
	 * size rather than to the full hit set.
	 * <p>
	 * This bound relies on the deduplication scroll and the page fetch producing hits in the same
	 * order. Both queries are left unsorted so they default to descending score, and the fetch's filter
	 * clauses do not affect scoring; do not add an explicit sort to one without the other, or the bound
	 * no longer holds.
	 */
	private static <T> SearchUniqueResults<T> searchWithResults(SearchSession searchSession,
	        SearchQueryUnique<?, T> uniqueQuery, final Integer offset, final Integer limit) {
		List<T> results = new ArrayList<>();
		Set<Object> uniqueKeys = new LinkedHashSet<>(); // Preserve the order
		final int maxClauseCount = Math.round(BooleanQuery.getMaxClauseCount() / 2.5f);
		SearchQueryUnique<?, T> nextQuery = uniqueQuery;
		Integer currentOffset = offset;
		Integer currentLimit = limit;
		while (nextQuery != null) {
			// A sub-query that is followed by another must define a unique key: the join deduplicates
			// across sub-queries by unique key, and the offset carried to the next sub-query is derived
			// from the number of unique keys this one contributed. A null key would leave both unsupported,
			// so fail loudly rather than silently mis-paginate. All current callers satisfy this.
			if (nextQuery.getJoinedQuery() != null && nextQuery.getUniqueKey() == null) {
				throw new IllegalStateException("A joined SearchQueryUnique sub-query must define a unique key");
			}

			SearchScope<?> scope = searchSession.scope(nextQuery.getScope());
			SearchPredicateFactory predicateFactory = scope.predicate();
			SearchPredicate searchPredicate = nextQuery.getSearch().apply(predicateFactory);
			SearchQuery<?> query;

			final Collection<Object> previousQueryUniqueKeys = new ArrayList<>(uniqueKeys);
			DeduplicationResult dedup = null;

			if (nextQuery.getUniqueKey() != null) {
				final String uniqueKey = nextQuery.getUniqueKey();
				// Find unique keys and duplicate ids
				SearchQuery<List<?>> uniqueKeyQuery = searchSession.search(scope)
				        .select(f -> f.composite(f.field(uniqueKey), f.id())).where(searchPredicate).toQuery();

				// A null limit means "fetch everything", so scan the whole hit set; otherwise only scan
				// far enough to fill the requested page.
				final Integer maxNewUniqueKeys;
				if (currentLimit == null) {
					maxNewUniqueKeys = null;
				} else {
					maxNewUniqueKeys = (currentOffset == null ? 0 : currentOffset) + currentLimit;
				}
				dedup = collectDuplicateIds(uniqueKeyQuery, uniqueKeys, maxClauseCount, maxNewUniqueKeys,
				    DEFAULT_SCROLL_CHUNK_SIZE);
				final List<Object> duplicateIds = dedup.duplicateIds;

				if (uniqueKeys.size() > maxClauseCount) {
					// Keep it a Set so later queries can still detect duplicates; a plain List here would
					// make add() always return true and defeat deduplication for subsequent joined queries.
					uniqueKeys = new LinkedHashSet<>(new ArrayList<>(uniqueKeys).subList(0, maxClauseCount));
				}

				query = searchSession.search(scope).where(f -> f.bool().with(b -> {
					b.must(searchPredicate);
					if (!duplicateIds.isEmpty()) {
						b.filter(f.not(f.id().matchingAny(duplicateIds)));
					}
					// Get rid of unique keys that were added to results in a previous query
					if (!previousQueryUniqueKeys.isEmpty()) {
						b.filter(f.not(f.terms().field(uniqueKey).matchingAny(previousQueryUniqueKeys)));
					}
				})).toQuery();
			} else {
				query = searchSession.search(scope).where(searchPredicate).toQuery();
			}

			List<?> partialResults;
			if (currentOffset != null) {
				partialResults = query.fetchHits(currentOffset, currentLimit);
			} else if (currentLimit != null) {
				partialResults = query.fetchHits(currentLimit);
			} else {
				partialResults = query.fetchAllHits();
			}

			if (!partialResults.isEmpty()) {
				if (nextQuery.getMapper() != null) {
					//noinspection unchecked
					results.addAll(partialResults.stream().map((Function<Object, T>) nextQuery.getMapper())
					        .collect(Collectors.toList()));
				} else {
					//noinspection unchecked
					results.addAll((Collection<? extends T>) partialResults);
				}
			}

			if (limit != null && results.size() == limit) {
				// The requested page is full, so there is no need to visit the remaining queries.
				return new SearchUniqueResults<>(results, offset, limit, null);
			}

			// The page was not filled by this query, so carry the remaining offset/limit to the next
			// query. Because the page is not full, this query's whole deduplicated hit set was consumed,
			// which is exactly the number of new unique keys the scroll found (barring the pathological
			// maxClauseCount duplicate-id cap, which can stop the scroll early and understate the count for
			// queries with more than maxClauseCount duplicates). The guard above guarantees dedup != null
			// whenever there is a joined query to carry to.
			if (nextQuery.getJoinedQuery() != null) {
				int consumed = dedup.newUniqueKeyCount;
				if (currentLimit != null) {
					currentLimit = limit - results.size();
				}
				if (currentOffset != null) {
					currentOffset = Math.max(0, currentOffset - consumed);
				}
			}

			nextQuery = nextQuery.getJoinedQuery();
		}

		return new SearchUniqueResults<>(results, offset, limit, null);
	}

	/**
	 * Calculates the total number of distinct hits across the joined queries.
	 * <p>
	 * Deduplication is done by collecting distinct unique-key values from an index-level projection,
	 * which avoids hydrating hits and avoids the follow-up filtering query used on the results path.
	 * <p>
	 * The exact deduplication is bounded by <code>cap</code>: once more than <code>cap</code> distinct
	 * hits have been seen, the scroll stops and the raw hit count (which counts duplicates and so is an
	 * upper bound on the distinct count) is returned instead of scanning the whole hit set. This lets a
	 * caller keep the count exact for the result sizes users actually paginate through while bounding
	 * the cost for very large hit sets; {@link #UNBOUNDED_DEDUPLICATION} keeps it always exact.
	 */
	private static long searchTotalHitCount(SearchSession searchSession, SearchQueryUnique<?, ?> uniqueQuery, int cap) {
		final Set<Object> uniqueKeys = new HashSet<>();
		long nonUniqueHitCount = 0;
		boolean exceededCap = false;
		SearchQueryUnique<?, ?> nextQuery = uniqueQuery;
		while (nextQuery != null && !exceededCap) {
			if (nextQuery.getUniqueKey() != null) {
				SearchScope<?> scope = searchSession.scope(nextQuery.getScope());
				SearchPredicate searchPredicate = nextQuery.getSearch().apply(scope.predicate());
				final String uniqueKey = nextQuery.getUniqueKey();
				SearchQuery<?> keyQuery = searchSession.search(scope).select(f -> f.field(uniqueKey)).where(searchPredicate)
				        .toQuery();
				collectUniqueKeys(keyQuery, uniqueKeys, cap, DEFAULT_SCROLL_CHUNK_SIZE);
				exceededCap = uniqueKeys.size() > cap;
			} else {
				// Without a unique key we cannot deduplicate, so fall back to the raw hit count.
				nonUniqueHitCount += fetchTotalHitCount(searchSession, nextQuery);
			}
			nextQuery = nextQuery.getJoinedQuery();
		}

		if (!exceededCap) {
			return uniqueKeys.size() + nonUniqueHitCount;
		}

		// There are more than `cap` distinct hits, so return the raw hit count as an upper bound rather
		// than scanning the entire hit set to determine the exact distinct count.
		return rawHitCount(searchSession, uniqueQuery);
	}

	/**
	 * Returns the raw (non-deduplicated) total hit count across the joined queries. This counts
	 * documents that share a unique key more than once, so it is an upper bound on the distinct count.
	 */
	private static long rawHitCount(SearchSession searchSession, SearchQueryUnique<?, ?> uniqueQuery) {
		long totalHitCount = 0;
		SearchQueryUnique<?, ?> nextQuery = uniqueQuery;
		while (nextQuery != null) {
			totalHitCount += fetchTotalHitCount(searchSession, nextQuery);
			nextQuery = nextQuery.getJoinedQuery();
		}
		return totalHitCount;
	}

	/**
	 * Returns the raw (non-deduplicated) hit count for a single query.
	 */
	private static long fetchTotalHitCount(SearchSession searchSession, SearchQueryUnique<?, ?> query) {
		SearchScope<?> scope = searchSession.scope(query.getScope());
		SearchPredicate searchPredicate = query.getSearch().apply(scope.predicate());
		return searchSession.search(scope).where(searchPredicate).fetchTotalHitCount();
	}

	/**
	 * Scrolls the unique-key projection of the given query, adding each key to <code>uniqueKeys</code>
	 * until more than <code>cap</code> distinct keys have been collected.
	 *
	 * @param keyQuery the unique-key projection query to scroll
	 * @param uniqueKeys the running set of unique keys, mutated in place
	 * @param cap the number of distinct keys to collect before stopping
	 * @param chunkSize the scroll chunk size
	 * @return the number of hits scanned before stopping
	 */
	static int collectUniqueKeys(SearchQuery<?> keyQuery, Set<Object> uniqueKeys, int cap, int chunkSize) {
		int scannedHitCount = 0;
		try (SearchScroll<?> scroll = keyQuery.scroll(chunkSize)) {
			SearchScrollResult<?> chunk = scroll.next();
			scan: while (chunk.hasHits()) {
				for (Object key : chunk.hits()) {
					scannedHitCount++;
					uniqueKeys.add(key);
					if (uniqueKeys.size() > cap) {
						break scan;
					}
				}
				chunk = scroll.next();
			}
		}
		return scannedHitCount;
	}

	/**
	 * Scrolls the (uniqueKey, id) projection of the given query, adding newly seen unique keys to
	 * <code>uniqueKeys</code> and collecting the ids of documents whose unique key was already seen.
	 * <p>
	 * The scroll stops early once <code>maxNewUniqueKeys</code> new unique keys have been collected
	 * (when non-null), bounding the work to the requested page. It also stops once more than
	 * <code>maxClauseCount</code> duplicate ids have been collected, since those ids become clauses of
	 * the follow-up filtering query and are subject to the Lucene clause limit.
	 *
	 * @param uniqueKeyQuery the (uniqueKey, id) projection query to scroll
	 * @param uniqueKeys the running set of unique keys, mutated in place
	 * @param maxClauseCount the maximum number of duplicate ids to collect
	 * @param maxNewUniqueKeys the number of new unique keys to collect before stopping, or
	 *            <code>null</code> to scan the whole hit set
	 * @param chunkSize the scroll chunk size
	 * @return the collected duplicate ids together with counts describing the scan
	 */
	static DeduplicationResult collectDuplicateIds(SearchQuery<List<?>> uniqueKeyQuery, Set<Object> uniqueKeys,
	        int maxClauseCount, Integer maxNewUniqueKeys, int chunkSize) {
		final List<Object> duplicateIds = new ArrayList<>();
		int newUniqueKeyCount = 0;
		int scannedHitCount = 0;
		try (SearchScroll<List<?>> scroll = uniqueKeyQuery.scroll(chunkSize)) {
			SearchScrollResult<List<?>> chunk = scroll.next();
			scan: while (chunk.hasHits()) {
				for (List<?> match : chunk.hits()) {
					scannedHitCount++;
					if (uniqueKeys.add(match.get(0))) {
						newUniqueKeyCount++;
						if (maxNewUniqueKeys != null && newUniqueKeyCount >= maxNewUniqueKeys) {
							// We have enough unique keys to satisfy the requested page.
							break scan;
						}
					} else {
						duplicateIds.add(match.get(1));
						if (duplicateIds.size() > maxClauseCount) {
							// stop at max clause count
							break scan;
						}
					}
				}
				chunk = scroll.next();
			}
		}
		return new DeduplicationResult(duplicateIds, newUniqueKeyCount, scannedHitCount);
	}

	/**
	 * Holds the outcome of {@link #collectDuplicateIds}: the duplicate ids to filter out, the number of
	 * new unique keys found, and the number of hits actually scanned (which is bounded when the page
	 * limit is reached).
	 */
	static final class DeduplicationResult {

		final List<Object> duplicateIds;

		final int newUniqueKeyCount;

		/**
		 * Total hits scanned before the scroll stopped; exposed only for test assertions on boundedness.
		 */
		final int scannedHitCount;

		DeduplicationResult(List<Object> duplicateIds, int newUniqueKeyCount, int scannedHitCount) {
			this.duplicateIds = duplicateIds;
			this.newUniqueKeyCount = newUniqueKeyCount;
			this.scannedHitCount = scannedHitCount;
		}
	}

	/**
	 * Finds unique keys for the specified search.
	 * <p>
	 * It returns up to <code>max = {@link BooleanQuery#getMaxClauseCount()} / 2</code> unique keys.
	 *
	 * @param searchSession searchSession
	 * @param scope scope
	 * @param searchPredicate searchPredicate
	 * @param uniqueKey uniqueKey
	 * @return unique keys
	 */
	public static List<Object> findUniqueKeys(SearchSession searchSession, SearchScope<?> scope,
	        SearchPredicate searchPredicate, String uniqueKey) {
		// The default Lucene clauses limit is 1024. We arbitrarily choose to use half here as it does
		// not make sense to return more hits by concept name anyway.
		int maxClauseCount = BooleanQuery.getMaxClauseCount() / 2;
		LinkedHashSet<Object> uniqueKeys = new LinkedHashSet<>(); // Preserve the order

		try (SearchScroll<?> scroll = searchSession.search(scope).select(f -> f.field(uniqueKey)).where(searchPredicate)
		        .scroll(500)) {

			SearchScrollResult<?> chunk = scroll.next();
			while (chunk.hasHits()) {
				uniqueKeys.addAll(chunk.hits());
				if (uniqueKeys.size() > maxClauseCount) {
					break;
				}
				chunk = scroll.next();
			}
		}

		if (uniqueKeys.size() > maxClauseCount) {
			return new ArrayList<>(uniqueKeys).subList(0, maxClauseCount);
		} else {
			return new ArrayList<>(uniqueKeys);
		}
	}
}
