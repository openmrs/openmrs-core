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

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.openmrs.collection.ListPart;

/**
 * Performs criteria queries.
 * 
 * @since 1.11
 */
public abstract class CriteriaQuery<T> extends SearchQuery<T> {
	
	private final Criteria criteria;
	
	/**
	 * @param session
	 */
	public CriteriaQuery(Session session, Class<T> type) {
		super(session, type);
		criteria = getSession().createCriteria(getType());
		prepareCriteria(criteria);
	}
	
	public abstract void prepareCriteria(Criteria criteria);
	
	@Override
	public List<T> list() {
		criteria.setProjection(null);
		criteria.setResultTransformer(Criteria.ROOT_ENTITY);
		
		@SuppressWarnings("unchecked")
		List<T> list = criteria.list();
		
		return list;
	}
	
	@Override
	public ListPart<T> listPart(Long firstResult, Long maxResults) {
		criteria.setProjection(null);
		criteria.setResultTransformer(Criteria.ROOT_ENTITY);
		
		if (firstResult != null) {
			criteria.setFirstResult(firstResult.intValue());
		}
		
		if (maxResults != null) {
			criteria.setMaxResults(maxResults.intValue());
		}
		
		@SuppressWarnings("unchecked")
		List<T> list = criteria.list();
		
		return ListPart.newListPart(list, firstResult, maxResults, null, null);
	}
	
	@Override
	public T uniqueResult() throws HibernateException {
		@SuppressWarnings("unchecked")
		T result = (T) criteria.uniqueResult();
		
		return result;
	}
	
	/**
	 * @see org.openmrs.api.db.hibernate.search.SearchQuery#resultSize()
	 */
	@Override
	public long resultSize() {
		criteria.setProjection(Projections.rowCount());
		
		return ((Number) criteria.uniqueResult()).longValue();
	}
	
}
