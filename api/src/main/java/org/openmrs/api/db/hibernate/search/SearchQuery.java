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

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.openmrs.collection.ListPart;

/**
 * Wraps around a query to provide common result methods.
 * 
 * @since 1.11
 */
public abstract class SearchQuery<T> {
	
	private final Class<T> type;
	
	private final Session session;
	
	public SearchQuery(Session session, Class<T> type) {
		if (session == null) {
			throw new IllegalArgumentException("session must not be null");
		}
		if (type == null) {
			throw new IllegalArgumentException("type must not be null");
		}
		
		this.session = session;
		this.type = type;
	}
	
	/**
	 * @return the type
	 */
	protected Class<T> getType() {
		return type;
	}
	
	protected Session getSession() {
		return session;
	}
	
	/**
	 * Runs the query returning a unique result.
	 * 
	 * @return the unique result or null
	 * @throws HibernateException if more than one result
	 */
	public abstract T uniqueResult() throws HibernateException;
	
	/**
	 * Runs the query returning a list with all results.
	 * 
	 * @return the list
	 */
	public abstract List<T> list();
	
	/**
	 * Runs the query returning a partial results list.
	 * 
	 * @param firstResult position of the first result to return, optional
	 * @param maxResults maximum number of results, optional
	 * @return the partial list
	 */
	public abstract ListPart<T> listPart(Long firstResult, Long maxResults);
	
	public abstract long resultSize();
	
	public ListPart<T> listPart(Integer firstResult, Integer maxResults) {
		Long first = (firstResult != null) ? Long.valueOf(firstResult) : null;
		Long max = (maxResults != null) ? Long.valueOf(maxResults) : null;
		return listPart(first, max);
	}
}
