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
import java.util.Iterator;

import org.hibernate.CallbackException;
import org.hibernate.Interceptor;
import org.hibernate.metamodel.spi.EntityRepresentationStrategy;
import org.hibernate.type.Type;

/**
 * Base implementation of Hibernate Interceptor with empty default methods.
 * This replaces the deprecated org.hibernate.EmptyInterceptor in Hibernate 7.x.
 * Subclasses can override only the methods they need.
 * 
 * @since 2.7
 */
public abstract class EmptyInterceptor implements Interceptor {
	
	@Override
	public boolean onLoad(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types)
	        throws CallbackException {
		return false;
	}
	
	@Override
	public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState,
	        String[] propertyNames, Type[] types) throws CallbackException {
		return false;
	}
	
	@Override
	public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types)
	        throws CallbackException {
		return false;
	}
	
	@Override
	public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types)
	        throws CallbackException {
	}
	
	@Override
	public void onCollectionRecreate(Object collection, Serializable key) throws CallbackException {
	}
	
	@Override
	public void onCollectionRemove(Object collection, Serializable key) throws CallbackException {
	}
	
	@Override
	public void onCollectionUpdate(Object collection, Serializable key) throws CallbackException {
	}
	
	@Override
	public void preFlush(Iterator<?> entities) throws CallbackException {
	}
	
	@Override
	public void postFlush(Iterator<?> entities) throws CallbackException {
	}
	
	@Override
	public Boolean isTransient(Object entity) {
		return null;
	}
	
	@Override
	public int[] findDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState,
	        String[] propertyNames, Type[] types) {
		return null;
	}
	
	@Override
	public Object instantiate(String entityName, EntityRepresentationStrategy representationStrategy, Object id) throws CallbackException {
		return null;
	}
	
	@Override
	public String getEntityName(Object object) throws CallbackException {
		return null;
	}
	
	@Override
	public Object getEntity(String entityName, Serializable id) throws CallbackException {
		return null;
	}
	
	@Override
	public void afterTransactionBegin(org.hibernate.Transaction tx) {
	}
	
	@Override
	public void beforeTransactionCompletion(org.hibernate.Transaction tx) {
	}
	
	@Override
	public void afterTransactionCompletion(org.hibernate.Transaction tx) {
	}
	
	@Override
	public String onPrepareStatement(String sql) {
		return sql;
	}
}
