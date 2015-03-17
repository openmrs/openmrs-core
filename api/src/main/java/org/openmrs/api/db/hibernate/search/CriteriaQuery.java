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
