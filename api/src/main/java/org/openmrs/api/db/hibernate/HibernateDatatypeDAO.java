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
