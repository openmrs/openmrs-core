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

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.openmrs.api.db.DatatypeDAO;
import org.openmrs.api.db.ClobDatatypeStorage;

/**
 * Hibernate-specific Datatype-related functions. This class should not be used directly. All calls
 * should go through the {@link org.openmrs.api.DatatypeService} methods.
 * 
 * @see org.openmrs.api.db.DatatypeDAO
 * @see org.openmrs.api.DatatypeService
 */
public class HibernateDatatypeDAO implements DatatypeDAO {
	
	private SessionFactory sessionFactory;
	
	/**
	 * Set session factory
	 * 
	 * @param sessionFactory
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	/**
	 * get current Hibernate session
	 * 
	 * @return current Hibernate session
	 */
	private Session session() {
		return sessionFactory.getCurrentSession();
	}
	
	/**
	 * @see org.openmrs.api.db.DatatypeDAO#getClobDatatypeStorage(java.lang.Integer)
	 */
	@Override
	public ClobDatatypeStorage getClobDatatypeStorage(Integer id) {
		return (ClobDatatypeStorage) session().get(ClobDatatypeStorage.class, id);
	}
	
	/**
	 * @see org.openmrs.api.db.DatatypeDAO#getClobDatatypeStorageByUuid(java.lang.String)
	 */
	@Override
	public ClobDatatypeStorage getClobDatatypeStorageByUuid(String uuid) {
		return (ClobDatatypeStorage) session().createCriteria(ClobDatatypeStorage.class).add(Restrictions.eq("uuid", uuid))
		        .uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.DatatypeDAO#saveClobDatatypeStorage(org.openmrs.api.db.ClobDatatypeStorage)
	 */
	@Override
	public ClobDatatypeStorage saveClobDatatypeStorage(ClobDatatypeStorage storage) {
		session().saveOrUpdate(storage);
		return storage;
	}
	
	/**
	 * @see org.openmrs.api.db.DatatypeDAO#deleteClobDatatypeStorage(org.openmrs.api.db.ClobDatatypeStorage)
	 */
	@Override
	public void deleteClobDatatypeStorage(ClobDatatypeStorage storage) {
		session().delete(storage);
	}
	
}
