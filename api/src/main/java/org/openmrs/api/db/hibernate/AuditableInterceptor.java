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
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.CallbackException;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.openmrs.Auditable;
import org.openmrs.OpenmrsObject;
import org.openmrs.api.context.Context;

/**
 * This class looks for {@link OpenmrsObject} and {@link Auditable} that are being inserted into the
 * database. The creator and dateCreated fields are set when inserting or updating objects and the
 * fields are still null. If the class is an update (instead of an insert) then the changedBy and
 * dateChanged fields are set to the current user and the current time. <br/>
 * <br/>
 * This class replaces the logic that was in the AuditableSaveHandler. It is here so that the
 * cascading does NOT happen for dateChanged/changedBy to child OpenmrsObjects (because all handlers
 * recurse on lists of OpenmrsObjects.
 * 
 * @since 1.9
 */
public class AuditableInterceptor extends EmptyInterceptor {
	
	private static final Log log = LogFactory.getLog(AuditableInterceptor.class);
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * This method is only called when inserting new objects.
	 * @should return true if dateCreated was null
	 * @should return true if creator was null
	 * @should return false if dateCreated and creator was not null
	 * @should be called when saving OpenmrsObject
	 * @return true if the object got the dateCreated and creator fields set
	 * @see org.hibernate.EmptyInterceptor#onSave(java.lang.Object, java.io.Serializable,
	 *      java.lang.Object[], java.lang.String[], org.hibernate.type.Type[])
	 */
	@Override
	public boolean onSave(Object entity, Serializable id, Object[] currentState, String[] propertyNames, Type[] types) {
		return setCreatorAndDateCreatedIfNull(entity, currentState, propertyNames);
	}
	
	/**
	 * This class method is only called when flushing an updated dirty object, not inserting objects
	 * 
	 * @return true if the object got the changedBy and dateChanged fields set
	 * @should set the dateChanged field
	 * @should set the changedBy field
	 * @should be called when saving an Auditable
	 * @should not enter into recursion on entity
	 * @see org.hibernate.EmptyInterceptor#onFlushDirty(java.lang.Object, java.io.Serializable,
	 *      java.lang.Object[], java.lang.Object[], java.lang.String[], org.hibernate.type.Type[])
	 */
	@Override
	public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState,
	        String[] propertyNames, Type[] types) throws CallbackException {
		boolean objectWasChanged = false;
		
		objectWasChanged = setCreatorAndDateCreatedIfNull(entity, currentState, propertyNames);
		
		if (entity instanceof Auditable && propertyNames != null) {
			if (log.isDebugEnabled())
				log.debug("Setting changed by fields on " + entity.getClass());
			
			if (setValue(currentState, propertyNames, "changedBy", Context.getAuthenticatedUser(), false)) {
				objectWasChanged = true;
			}
			
			if (setValue(currentState, propertyNames, "dateChanged", new Date(), false)) {
				objectWasChanged = true;
			}
		}
		
		return objectWasChanged;
	}
	
	/**
	 * Sets the creator and dateCreated fields to the current user and the current time if they are
	 * null.
	 * 
	 * @param entity
	 * @param currentState
	 * @param propertyNames
	 * @return true if creator and dateCreated were changed
	 */
	private boolean setCreatorAndDateCreatedIfNull(Object entity, Object[] currentState, String[] propertyNames) {
		boolean objectWasChanged = false;
		
		if (entity instanceof OpenmrsObject) {
			if (log.isDebugEnabled())
				log.debug("Setting creator and dateCreated on " + entity);
			
			if (setValue(currentState, propertyNames, "creator", Context.getAuthenticatedUser(), true)) {
				objectWasChanged = true;
			}
			
			if (setValue(currentState, propertyNames, "dateCreated", new Date(), true)) {
				objectWasChanged = true;
			}
		}
		
		return objectWasChanged;
	}
	
	/**
	 * Sets the property to the given value.
	 * 
	 * @param currentState
	 * @param propertyNames
	 * @param propertyToSet
	 * @param value
	 * @param setNullOnly
	 * @return true if the property was changed
	 */
	private boolean setValue(Object[] currentState, String[] propertyNames, String propertyToSet, Object value,
	        boolean setNullOnly) {
		int index = Arrays.asList(propertyNames).indexOf(propertyToSet);
		
		// HACK! When I apply the patch for TRUNK-2588, and then I try to start OpenMRS for the first time during the init wizard
		// I get something like this:
		/*
		java.lang.NullPointerException
		at org.openmrs.api.db.hibernate.AuditableInterceptor.setValue(AuditableInterceptor.java:140)
		at org.openmrs.api.db.hibernate.AuditableInterceptor.onFlushDirty(AuditableInterceptor.java:83)
		at org.openmrs.api.db.hibernate.ChainingInterceptor.onFlushDirty(ChainingInterceptor.java:77)
		at org.hibernate.event.def.DefaultFlushEntityEventListener.invokeInterceptor(DefaultFlushEntityEventListener.java:331)
		at org.hibernate.event.def.DefaultFlushEntityEventListener.handleInterception(DefaultFlushEntityEventListener.java:308)
		at org.hibernate.event.def.DefaultFlushEntityEventListener.scheduleUpdate(DefaultFlushEntityEventListener.java:248)
		at org.hibernate.event.def.DefaultFlushEntityEventListener.onFlushEntity(DefaultFlushEntityEventListener.java:128)
		at org.hibernate.event.def.AbstractFlushingEventListener.flushEntities(AbstractFlushingEventListener.java:196)
		at org.hibernate.event.def.AbstractFlushingEventListener.flushEverythingToExecutions(AbstractFlushingEventListener.java:76)
		at org.hibernate.event.def.DefaultFlushEventListener.onFlush(DefaultFlushEventListener.java:26)
		at org.hibernate.impl.SessionImpl.flush(SessionImpl.java:1000)
		at org.hibernate.impl.SessionImpl.managedFlush(SessionImpl.java:338)
		at org.hibernate.transaction.JDBCTransaction.commit(JDBCTransaction.java:106)
		at org.springframework.orm.hibernate3.HibernateTransactionManager.doCommit(HibernateTransactionManager.java:656)
		at org.springframework.transaction.support.AbstractPlatformTransactionManager.processCommit(AbstractPlatformTransactionManager.java:754)
		at org.springframework.transaction.support.AbstractPlatformTransactionManager.commit(AbstractPlatformTransactionManager.java:723)
		at org.springframework.transaction.interceptor.TransactionAspectSupport.commitTransactionAfterReturning(TransactionAspectSupport.java:393)
		at org.springframework.transaction.interceptor.TransactionInterceptor.invoke(TransactionInterceptor.java:120)
		at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:172)
		at org.springframework.aop.framework.JdkDynamicAopProxy.invoke(JdkDynamicAopProxy.java:202)
		at $Proxy65.saveToMemento(Unknown Source)
		at org.openmrs.util.OpenmrsClassLoader.saveState(OpenmrsClassLoader.java:444)
		at org.openmrs.module.ModuleUtil.refreshApplicationContext(ModuleUtil.java:756)
		at org.openmrs.module.web.WebModuleUtil.refreshWAC(WebModuleUtil.java:825)
		at org.openmrs.web.Listener.performWebStartOfModules(Listener.java:565)
		at org.openmrs.web.filter.initialization.InitializationFilter$InitializationCompletion$1.run(InitializationFilter.java:1575)
		at java.lang.Thread.run(Thread.java:680)
		 */
		if (value == null)
			return false;
		// END HACK
		
		if (index >= 0) {
			if (currentState[index] == null || !setNullOnly) {
				if (!value.equals(currentState[index])) {
					currentState[index] = value;
					return true;
				}
			}
		}
		
		return false;
	}
	
}
