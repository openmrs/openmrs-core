/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db.hibernate;

import java.util.List;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.openmrs.BaseOpenmrsMetadata;
import org.openmrs.api.db.OpenmrsMetadataDAO;

/**
 * Abstract class implementing basic data access methods for BaseOpenmrsMetadata persistents
 *
 * @since 1.10
 *
 * @param <T>
 */
public class HibernateOpenmrsMetadataDAO<T extends BaseOpenmrsMetadata> extends HibernateOpenmrsObjectDAO<T> implements OpenmrsMetadataDAO<T> {
	
	public HibernateOpenmrsMetadataDAO(Class<T> mappedClass) {
		super();
		this.mappedClass = mappedClass;
	}
	
	/**
	 * @see org.openmrs.api.db.OpenmrsMetadataDAO#getAll(boolean)
	 */
	@Override
	public List<T> getAll(boolean includeRetired) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(mappedClass);
		Root<T> root = cq.from(mappedClass);

		if (!includeRetired) {
			cq.where(cb.isFalse(root.get("retired")));
		}

		return session.createQuery(cq).getResultList();
	}

	/**
	 * @see org.openmrs.api.db.OpenmrsMetadataDAO#getAll(boolean, java.lang.Integer, java.lang.Integer)
	 */
	@Override
	public List<T> getAll(boolean includeRetired, Integer firstResult, Integer maxResults) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(mappedClass);
		Root<T> root = cq.from(mappedClass);

		if (!includeRetired) {
			cq.where(cb.isFalse(root.get("retired")));
		}

		TypedQuery<T> query = session.createQuery(cq);
		if (firstResult != null) {
			query.setFirstResult(firstResult);
		}
		if (maxResults != null) {
			query.setMaxResults(maxResults);
		}
		
		return query.getResultList();
	}


	/**
	 * @see org.openmrs.api.db.OpenmrsMetadataDAO#getAllCount(boolean)
	 */
	@Override
	public int getAllCount(boolean includeRetired) {
		
		String hql = "select count(*)" + " from " + mappedClass;
		
		if (!includeRetired) {
			hql += " where retired = false";
		}
		Query query = sessionFactory.getCurrentSession().createQuery(hql);
		
		Number count = JpaUtils.getSingleResultOrNull(query);
		
		return count == null ? 0 : count.intValue();
	}
}
