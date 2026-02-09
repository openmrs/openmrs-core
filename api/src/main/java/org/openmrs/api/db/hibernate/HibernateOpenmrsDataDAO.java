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

import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.api.db.OpenmrsDataDAO;

/**
 * Abstract class implementing basic data access methods for BaseOpenmrsData persistents
 *
 * @since 1.10
 *
 * @param <T>
 */
public class HibernateOpenmrsDataDAO<T extends BaseOpenmrsData> extends HibernateOpenmrsObjectDAO<T> implements OpenmrsDataDAO<T> {
	
	public HibernateOpenmrsDataDAO(Class<T> mappedClass) {
		super();
		this.mappedClass = mappedClass;
	}
	
	/**
	 * @see org.openmrs.api.db.OpenmrsDataDAO#getAll(boolean)
	 */
	@Override
	public List<T> getAll(boolean includeVoided) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(mappedClass);
		Root<T> root = cq.from(mappedClass);

		if (!includeVoided) {
			cq.where(cb.isFalse(root.get("voided")));
		}

		return session.createQuery(cq).getResultList();
	}
	
	/**
	 * @see org.openmrs.api.db.OpenmrsDataDAO#getAll(boolean, java.lang.Integer, java.lang.Integer)
	 */
	@Override
	public List<T> getAll(boolean includeVoided, Integer firstResult, Integer maxResults) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(mappedClass);
		Root<T> root = cq.from(mappedClass);

		if (!includeVoided) {
			cq.where(cb.isFalse(root.get("voided")));
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
	 * @see org.openmrs.api.db.OpenmrsDataDAO#getAllCount(boolean)
	 */
	@Override
	public int getAllCount(boolean includeVoided) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<T> root = cq.from(mappedClass);

		cq.select(cb.count(root));

		if (!includeVoided) {
			cq.where(cb.isFalse(root.get("voided")));
		}

		Long count = session.createQuery(cq).getSingleResult();

		return count == null ? 0 : count.intValue();
	}
}
