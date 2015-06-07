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

import java.io.Serializable;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.openmrs.BaseOpenmrsObject;
import org.openmrs.api.db.OpenmrsObjectDAO;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Generic base class for all OpenMrs DAOs
 * @since 1.10
 *
 */
public class HibernateOpenmrsObjectDAO<T extends BaseOpenmrsObject> implements OpenmrsObjectDAO<T> {
	
	@Autowired
	protected SessionFactory sessionFactory;
	
	protected Class<T> mappedClass;
	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	@Override
	public T getById(Serializable id) {
		return (T) sessionFactory.getCurrentSession().get(mappedClass, id);
	}
	
	/**
	 * @see org.openmrs.api.db.OpenmrsObjectDAO#getByUuid(java.lang.String)
	 */
	public T getByUuid(String uuid) {
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(mappedClass);
		return (T) crit.add(Restrictions.eq("uuid", uuid)).uniqueResult();
	}
	
	/** 
	 * @see org.openmrs.api.db.OpenmrsObjectDAO#delete(org.openmrs.BaseOpenmrsObject)
	 */
	public void delete(T persistent) {
		sessionFactory.getCurrentSession().delete(persistent);
	}
	
	/**
	 * @see org.openmrs.api.db.OpenmrsObjectDAO#saveOrUpdate(org.openmrs.BaseOpenmrsObject)
	 */
	public T saveOrUpdate(T persistent) {
		sessionFactory.getCurrentSession().saveOrUpdate(persistent);
		return persistent;
	}
	
}
