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
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.TermsFilter;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.hibernate.Session;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.openmrs.collection.ListPart;

/**
 * Performs Lucene queries.
 * 
 * @since 1.11
 */
public abstract class LuceneQuery<T> extends SearchQuery<T> {
	
	private FullTextQuery fullTextQuery;
	
	private Set<Set<Term>> includeTerms = new HashSet<Set<Term>>();
	
	private Set<Term> excludeTerms = new HashSet<Term>();
	
	/**
	 * The preferred way to create a Lucene query using the query parser.
	 * @param type filters on type
	 * @param session
	 * @param query
	 * 
	 * @return the Lucene query
	 */
	public static <T> LuceneQuery<T> newQuery(final Class<T> type, final Session session, final String query) {
		return new LuceneQuery<T>(
		                          type, session) {
			
			@Override
			protected Query prepareQuery() throws ParseException {
				if (query.isEmpty()) {
					return new MatchAllDocsQuery();
				}
				return newQueryParser().parse(query);
			}
			
		};
	}
	
	/**
	 * Escape any characters that can be interpreted by the query parser.
	 * 
	 * @param query
	 * @return the escaped query
	 */
	public static String escapeQuery(final String query) {
		return QueryParser.escape(query);
	}
	
	public LuceneQuery(Class<T> type, Session session) {
		super(session, type);
		
		buildQuery();
	}
	
	/**
	 * Include items with the given value in the specified field.
	 * <p>
	 * It is a filter applied before the query.
	 * 
	 * @param field
	 * @param value
	 * @return the query
	 */
	public LuceneQuery<T> include(String field, Object value) {
		if (value != null) {
			include(field, new Object[] { value });
		}
		
		return this;
	}
	
	public LuceneQuery<T> include(String field, Collection<?> values) {
		if (values != null) {
			include(field, values.toArray());
		}
		
		return this;
	}
	
	/**
	 * Include items with any of the given values in the specified field.
	 * <p>
	 * It is a filter applied before the query.
	 * 
	 * @param field
	 * @param values
	 * @return the query
	 */
	public LuceneQuery<T> include(String field, Object[] values) {
		if (values != null && values.length != 0) {
			Set<Term> terms = new HashSet<Term>();
			for (Object value : values) {
				terms.add(new Term(field, value.toString()));
			}
			includeTerms.add(terms);
			
			fullTextQuery.enableFullTextFilter("termsFilterFactory").setParameter("includeTerms", includeTerms)
			        .setParameter("excludeTerms", excludeTerms);
		}
		
		return this;
	}
	
	/**
	 * Exclude any items with the given value in the specified field.
	 * <p>
	 * It is a filter applied before the query.
	 * 
	 * @param field
	 * @param value
	 * @return the query
	 */
	public LuceneQuery<T> exclude(String field, Object value) {
		if (value != null) {
			exclude(field, new Object[] { value });
		}
		
		return this;
	}
	
	/**
	 * Exclude any items with the given values in the specified field.
	 * <p>
	 * It is a filter applied before the query.
	 * 
	 * @param field
	 * @param values
	 * @return the query
	 */
	public LuceneQuery<T> exclude(String field, Object[] values) {
		if (values != null && values.length != 0) {
			for (Object value : values) {
				excludeTerms.add(new Term(field, value.toString()));
			}
			
			fullTextQuery.enableFullTextFilter("termsFilterFactory").setParameter("includeTerms", includeTerms)
			        .setParameter("excludeTerms", excludeTerms);
		}
		
		return this;
	}
	
	/**
	 * It is called by the constructor to get an instance of a query.
	 * <p>
	 * To construct the query you can use {@link #newQueryBuilder()} or {@link #newQueryParser()},
	 * which are created for the proper type.
	 * 
	 * @return the query
	 * @throws ParseException
	 */
	protected abstract Query prepareQuery() throws ParseException;
	
	/**
	 * It is called by the constructor after creating {@link FullTextQuery}.
	 * <p>
	 * You can override it to adjust the full text query, e.g. add a filter.
	 * 
	 * @param fullTextQuery
	 */
	protected void adjustFullTextQuery(FullTextQuery fullTextQuery) {
	}
	
	/**
	 * You can use it in {@link #prepareQuery()}.
	 * 
	 * @return the query builder
	 */
	protected QueryBuilder newQueryBuilder() {
		return getFullTextSession().getSearchFactory().buildQueryBuilder().forEntity(getType()).get();
	}
	
	/**
	 * You can use it in {@link #prepareQuery()}.
	 * 
	 * @return the query parser
	 */
	protected QueryParser newQueryParser() {
		Analyzer analyzer = getFullTextSession().getSearchFactory().getAnalyzer(getType());
		QueryParser queryParser = new QueryParser(null, analyzer);
		queryParser.setDefaultOperator(Operator.AND);
		return queryParser;
	}
	
	/**
	 * Gives you access to the full text session.
	 * 
	 * @return the full text session
	 */
	protected FullTextSession getFullTextSession() {
		return Search.getFullTextSession(getSession());
	}
	
	/**
	 * Skip elements, values of which repeat in the given field.
	 * <p>
	 * Only the first element will be included in the results.
	 * <p>
	 * <b>Note:</b> For performance reasons you should call this method as last when constructing a
	 * query. When called it will project the query and create a filter to eliminate duplicates.
	 * 
	 * @param field
	 * @return
	 */
	public LuceneQuery<T> skipSame(String field) {
		String idPropertyName = getSession().getSessionFactory().getClassMetadata(getType()).getIdentifierPropertyName();
		
		List<Object> documents = listProjection(idPropertyName, field);
		
		TermsFilter termsFilter = null;
		if (!documents.isEmpty()) {
			Set<Object> uniqueFieldValues = new HashSet<Object>();
			List<Term> terms = new ArrayList<Term>();
			for (Object document : documents) {
				Object[] row = (Object[]) document;
				if (uniqueFieldValues.add(row[1])) {
					terms.add(new Term(idPropertyName, row[0].toString()));
				}
			}
			termsFilter = new TermsFilter(terms);
		}
		
		buildQuery();
		
		if (termsFilter != null) {
			fullTextQuery.setFilter(termsFilter);
		}
		
		return this;
	}
	
	@Override
	public T uniqueResult() {
		@SuppressWarnings("unchecked")
		T result = (T) fullTextQuery.uniqueResult();
		
		return result;
	}
	
	@Override
	public List<T> list() {
		@SuppressWarnings("unchecked")
		List<T> list = fullTextQuery.list();
		
		return list;
	}
	
	@Override
	public ListPart<T> listPart(Long firstResult, Long maxResults) {
		applyPartialResults(fullTextQuery, firstResult, maxResults);
		
		@SuppressWarnings("unchecked")
		List<T> list = fullTextQuery.list();
		
		return ListPart.newListPart(list, firstResult, maxResults, Long.valueOf(fullTextQuery.getResultSize()),
		    !fullTextQuery.hasPartialResults());
	}
	
	/**
	 * @see org.openmrs.api.db.hibernate.search.SearchQuery#resultSize()
	 */
	@Override
	public long resultSize() {
		return fullTextQuery.getResultSize();
	}
	
	public List<Object> listProjection(String... fields) {
		fullTextQuery.setProjection(fields);
		
		@SuppressWarnings("unchecked")
		List<Object> list = fullTextQuery.list();
		
		return list;
	}
	
	public ListPart<Object> listPartProjection(Long firstResult, Long maxResults, String... fields) {
		applyPartialResults(fullTextQuery, firstResult, maxResults);
		
		fullTextQuery.setProjection(fields);
		
		@SuppressWarnings("unchecked")
		List<Object> list = fullTextQuery.list();
		
		return ListPart.newListPart(list, firstResult, maxResults, Long.valueOf(fullTextQuery.getResultSize()),
		    !fullTextQuery.hasPartialResults());
		
	}
	
	public ListPart<Object> listPartProjection(Integer firstResult, Integer maxResults, String... fields) {
		Long first = (firstResult != null) ? Long.valueOf(firstResult) : null;
		Long max = (maxResults != null) ? Long.valueOf(maxResults) : null;
		return listPartProjection(first, max, fields);
	}
	
	private void buildQuery() {
		Query query;
		try {
			query = prepareQuery();
		}
		catch (ParseException e) {
			throw new IllegalStateException("Invalid query", e);
		}
		
		fullTextQuery = getFullTextSession().createFullTextQuery(query, getType());
		adjustFullTextQuery(fullTextQuery);
	}
	
	private void applyPartialResults(FullTextQuery fullTextQuery, Long firstResult, Long maxResults) {
		if (firstResult != null) {
			fullTextQuery.setFirstResult(firstResult.intValue());
		}
		
		if (maxResults != null) {
			fullTextQuery.setMaxResults(maxResults.intValue());
		}
	}
}
