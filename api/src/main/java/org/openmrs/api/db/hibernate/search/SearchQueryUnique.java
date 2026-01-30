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
import java.util.LinkedHashSet;
import java.util.List;
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
 * Due to max clause count limit dictated by performance reasons only first <code>max = {@link BooleanQuery#getMaxClauseCount()} / 2.5</code>
 * duplicate keys are removed from results. 
 * <p>
 * The same <code>max</code> limit applies when combining results of multiple queries. 
 * Up to <code>max</code> unique keys from the previous queries are removed from results in the following queries.
 * 
 * @param <T> query scope
 * @param <R> query return type
 * @since 2.8.0
 */
public class SearchQueryUnique<T, R> {
	Class<? extends T> scope;
	Function<SearchPredicateFactory, SearchPredicate> search;
	Function<T, R> mapper;
	String uniqueKey;
	SearchQueryUnique<?, R> joinedQuery;

	public SearchQueryUnique(Class<? extends T> scope, Function<SearchPredicateFactory, SearchPredicate> search,
							 String uniqueKey, Function<T, R> mapper,
							 SearchQueryUnique<?, R> joinedQuery) {
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
	 * When joining a query, the algorithm will use the unique key values from the previous query (if any) to
	 * filter out items using its unique key.
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
	 * @param scope the index type to be searched
	 * @param search the search predicate
	 * @param uniqueKey the field to use as unique key, or <code>null</code> if none
	 * @param mapper the mapper from index type to result type, or <code>null</code> if none
	 * @return the search
	 * @param <T> the index type
	 * @param <R> the result type
	 */
	public static <T,R> SearchQueryUnique<T,R> newQuery(Class<? extends T> scope, 
														Function<SearchPredicateFactory, SearchPredicate> search, 
														String uniqueKey, Function<T, R> mapper) {
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
	public static <T,R> SearchQueryUnique<T,R> newQuery(Class<? extends T> scope,
														Function<SearchPredicateFactory, SearchPredicate> search,
														String uniqueKey) {
		return new SearchQueryUnique<>(scope, search, uniqueKey, null,null);
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
	 * Runs the search calculating the total hit count only.
	 * See {@link #search(SearchSessionFactory, SearchQueryUnique, Integer, Integer, Boolean)}.
	 *
	 * @param searchSessionFactory SearchSessionFactory
	 * @param uniqueQuery  unique query {@link #newQuery(Class, Function, String, Function)}
	 * @return the total hit count
	 */
	public static Long searchCount(SearchSessionFactory searchSessionFactory,
								   SearchQueryUnique<?, ?> uniqueQuery) {
		return search(searchSessionFactory, uniqueQuery,0, 0, true).getTotalHitCount();
	}

	/**
	 * Runs the search without calculating the total hit count. 
	 * See {@link #search(SearchSessionFactory, SearchQueryUnique, Integer, Integer, Boolean)}.
	 *
	 * @param searchSessionFactory SearchSessionFactory
	 * @param uniqueQuery  unique query {@link #newQuery(Class, Function, String, Function)}
	 * @return the results
	 * @param <T> the type of results
	 */
	public static <T> List<T> search(SearchSessionFactory searchSessionFactory,
									 SearchQueryUnique<?, T> uniqueQuery) {
		return search(searchSessionFactory, uniqueQuery, null, null, false).getResults();
	}

	/**
	 * Runs the search without calculating the total hit count. 
	 * See {@link #search(SearchSessionFactory, SearchQueryUnique, Integer, Integer, Boolean)}.
	 * 
	 * @param searchSessionFactory SearchSessionFactory
	 * @param uniqueQuery  unique query {@link #newQuery(Class, Function, String, Function)}
	 * @param offset offset of results
	 * @param limit limit of results
	 * @return the results
	 * @param <T> the type of results
	 */
	public static <T> List<T> search(SearchSessionFactory searchSessionFactory,
									 SearchQueryUnique<?, T> uniqueQuery, Integer offset, Integer limit) {
		return search(searchSessionFactory, uniqueQuery, offset, limit, false).getResults();
	}

	/**
	 * Executes unique queries applying joins and mapping to the result type.
	 * 
	 * @param searchSessionFactory search session factory
	 * @param uniqueQuery unique query {@link #newQuery(Class, Function, String, Function)}
	 * @param offset offset of results
	 * @param limit limit of results
	 * @param includeTotalHitCount calculate total hit count (it is more expensive query)
	 * @return the results
	 * @param <T> the type of results
	 */
	public static <T> SearchUniqueResults<T> search(SearchSessionFactory searchSessionFactory,
													SearchQueryUnique<?, T> uniqueQuery,
													final Integer offset, final Integer limit, Boolean includeTotalHitCount) {
		if (includeTotalHitCount == null) {
			includeTotalHitCount = false;
		}

		SearchSession searchSession = searchSessionFactory.getSearchSession();
		List<T> results = new ArrayList<>();
		Collection<Object> uniqueKeys = new LinkedHashSet<>(); // Preserve the order
		final int maxClauseCount = Math.round(BooleanQuery.getMaxClauseCount() / 2.5f);
		SearchQueryUnique<?, T> nextQuery = uniqueQuery;
		long totalHitCount = 0;
		Integer currentOffset = offset;
		Integer currentLimit = limit;
		while (nextQuery != null) {
			SearchScope<?> scope = searchSession.scope(nextQuery.getScope());
			SearchPredicateFactory predicateFactory = scope.predicate();
			SearchPredicate searchPredicate = nextQuery.getSearch().apply(predicateFactory);
			SearchQuery<?> query;

			final Collection<Object> previousQueryUniqueKeys = new ArrayList<>(uniqueKeys);
			
			if (nextQuery.getUniqueKey() != null) {
				final String uniqueKey = nextQuery.getUniqueKey();
				// Find unique keys and duplicate ids
				SearchQuery<List<?>> uniqueKeyQuery = searchSession.search(scope).select(f ->
					f.composite(
						f.field(uniqueKey),
						f.id()
					)).where(searchPredicate).toQuery();
				
				final List<Object> duplicateIds = new ArrayList<>();
				try (SearchScroll<List<?>> scroll = uniqueKeyQuery.scroll(500)){
					SearchScrollResult<List<?>> chunk = scroll.next();
					while (chunk.hasHits()) {
						boolean limitReached = false;
						for (List<?> match : chunk.hits()) {
							if (!uniqueKeys.add(match.get(0))) {
								duplicateIds.add(match.get(1));
								if (duplicateIds.size() > maxClauseCount) {
									// stop at max clause count
									limitReached = true;
									break;
								}
							}
						}
						if (limitReached) {
							break;
						}
						chunk = scroll.next();
					}
				}

				if (uniqueKeys.size() > maxClauseCount) {
					uniqueKeys = new ArrayList<>(uniqueKeys).subList(0, maxClauseCount);
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
					results.addAll(partialResults.stream().map((Function<Object, T>) nextQuery.getMapper()).collect(Collectors.toList()));
				} else {
					//noinspection unchecked
					results.addAll((Collection<? extends T>) partialResults);
				}
			}

			if (limit != null && results.size() == limit && !includeTotalHitCount) {
				// End early as we don't need to calculate total hit count
				return new SearchUniqueResults<>(results, offset, limit, null);
			}

			if (includeTotalHitCount || (nextQuery.getJoinedQuery() != null && partialResults.isEmpty() && 
				currentOffset != null && currentOffset != 0)) {
				// Fetch total hit count only if explicitly requested or if offset caused the query to return 0 results,
				// and we will be trying to fetch more results in the next query
				totalHitCount += query.fetchTotalHitCount();
			} else {
				totalHitCount += partialResults.size();
			}

			if (currentLimit != null) {
				currentLimit = limit - results.size();
			}
			if (currentOffset != null) {
				currentOffset = currentOffset - (int) totalHitCount;
				currentOffset = currentOffset < 0 ? 0 : currentOffset;
			}
			
			nextQuery = nextQuery.getJoinedQuery();
		}
		
		return new SearchUniqueResults<>(results, offset, limit, includeTotalHitCount ? totalHitCount : null);
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
	 * 
	 * @return unique keys
	 */
	public static List<Object> findUniqueKeys(SearchSession searchSession, SearchScope<?> scope,
											   SearchPredicate searchPredicate, String uniqueKey) {
		// The default Lucene clauses limit is 1024. We arbitrarily choose to use half here as it does 
		// not make sense to return more hits by concept name anyway.
		int maxClauseCount = BooleanQuery.getMaxClauseCount() / 2;
		LinkedHashSet<Object> uniqueKeys = new LinkedHashSet<>(); // Preserve the order

		try (SearchScroll<?> scroll = searchSession.search(scope)
			.select(f -> f.field(uniqueKey))
			.where(searchPredicate).scroll(500)) {

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
