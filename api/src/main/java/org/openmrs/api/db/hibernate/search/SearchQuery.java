/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.api.db.hibernate.search;

import java.util.List;

import org.apache.commons.lang.Validate;
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
		Validate.notNull(session);
		Validate.notNull(type);
		
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
