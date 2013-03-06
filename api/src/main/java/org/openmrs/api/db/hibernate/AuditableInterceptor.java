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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.CallbackException;
import org.hibernate.EmptyInterceptor;
import org.hibernate.EntityMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.collection.PersistentCollection;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.CollectionType;
import org.hibernate.type.Type;
import org.openmrs.Auditable;
import org.openmrs.OpenmrsObject;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

/**
 * This class looks for {@link OpenmrsObject} and {@link Auditable} that are being inserted into the
 * database. The creator and dateCreated fields are set when inserting or updating objects and the
 * fields are still null. If the class is an update (instead of an insert) then the changedBy and
 * dateChanged fields are set to the current user and the current time. <br/>
 * <br/>
 * This class replaces the logic that was in the AuditableSaveHandler. It is here so that the
 * cascading does NOT happen for dateChanged/changedBy to child OpenmrsObjects (because all handlers
 * recurse on lists of OpenmrsObjects. However, cascading does happen for dateChanged/changedBy from
 * items in child collections to their parent objects.
 * 
 * @since 1.9
 */
public class AuditableInterceptor extends EmptyInterceptor {
	
	private static final Log log = LogFactory.getLog(AuditableInterceptor.class);
	
	private static final long serialVersionUID = 1L;
	
	private ThreadLocal<Date> date = new ThreadLocal<Date>();
	
	/**
	 * @see org.hibernate.EmptyInterceptor#afterTransactionBegin(org.hibernate.Transaction)
	 */
	@Override
	public void afterTransactionBegin(Transaction tx) {
		//Ensures all Auditables in a transaction have uniform date created/changed
		//NOTE: we still need to check if it is not null where before we use it
		//Because if this intercetptor is being invoked again due to another commit in this
		//transaction by some other interceptor in the chain, date could be 
		//null if afterTransactionCompletion which clears them was already called
		date.set(new Date());
	}
	
	/**
	 * @see org.hibernate.EmptyInterceptor#findDirty(java.lang.Object, java.io.Serializable,
	 *      java.lang.Object[], java.lang.Object[], java.lang.String[], org.hibernate.type.Type[])
	 */
	@Override
	public int[] findDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState,
	        String[] propertyNames, Type[] types) {
		
		if (entity instanceof Auditable && propertyNames != null) {
			SessionFactory sf = Context.getRegisteredComponents(SessionFactory.class).get(0);
			Session currentSession = sf.getCurrentSession();
			//we will load the previous states of the collection items in a new session
			Session tempSession = null;
			try {
				propertiesLoop: for (int i = 0; i < propertyNames.length; i++) {
					if (types[i].isCollectionType()) {
						if (tempSession == null)
							tempSession = SessionFactoryUtils.getNewSession(sf);
						
						Object coll = currentState[i];
						
						//TODO handle maps too
						if (coll != null && Collection.class.isAssignableFrom(coll.getClass())) {
							Collection<?> collection = (Collection<?>) coll;
							if (!collection.isEmpty()) {
								Class<?> elementClass = ((CollectionType) types[i]).getElementType(
								    (SessionFactoryImplementor) sf).getReturnedClass();
								ClassMetadata classMetadata = sf.getClassMetadata(elementClass);
								//skip simple single value types e.g primitives, enums etc that have no classmetadata
								//An example is Cohort.memberIds where the items are of a simple single value type
								if (classMetadata != null) {
									String[] properties = classMetadata.getPropertyNames();
									for (Object item : collection) {
										Object[] itemCurrentState = classMetadata.getPropertyValues(item, EntityMode.POJO);
										//load the previous state from the DB and compare fields for changes
										Serializable primaryKey = classMetadata.getIdentifier(item,
										    (SessionImplementor) currentSession);
										if (primaryKey != null) {//why is it null?
											Object originalObject = tempSession.get(elementClass, primaryKey);
											if (originalObject != null) {
												Object[] itemPreviousState = classMetadata.getPropertyValues(originalObject,
												    EntityMode.POJO);
												
												if (isDirty(itemCurrentState, itemPreviousState, properties)) {
													try {
														((Auditable) entity).setChangedBy(Context.getAuthenticatedUser());
														((Auditable) entity).setDateChanged(new Date());
													}
													catch (Exception e) {
														if (!(e instanceof UnsupportedOperationException))
															log.warn("Error while setting audit info:", e);
													}
													break propertiesLoop;
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
			finally {
				if (tempSession != null)
					tempSession.close();
			}
		}
		
		return super.findDirty(entity, id, currentState, previousState, propertyNames, types);
	}
	
	/**
	 * This method is only called when inserting new objects.
	 * 
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
	 * @should set the audit info on a parent when an element in a child collection is updated
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
			
			if (setValue(currentState, propertyNames, "dateChanged", (date.get() != null) ? date.get() : new Date(), false)) {
				objectWasChanged = true;
			}
		}
		
		return objectWasChanged;
	}
	
	/**
	 * @see org.hibernate.EmptyInterceptor#onCollectionUpdate(java.lang.Object,
	 *      java.io.Serializable)
	 * @should set the audit info on a parent when an element is added to a child collection
	 * @should set the audit info on a parent when an element is removed from a child collection
	 */
	@Override
	public void onCollectionUpdate(Object collection, Serializable key) throws CallbackException {
		if (collection != null) {
			Object owningObject = ((PersistentCollection) collection).getOwner();
			if (owningObject instanceof Auditable) {
				Auditable auditable = (Auditable) owningObject;
				auditable.setDateChanged(new Date());
				auditable.setChangedBy(Context.getAuthenticatedUser());
			}
		}
	}
	
	/**
	 * @see org.hibernate.EmptyInterceptor#afterTransactionCompletion(org.hibernate.Transaction)
	 */
	@Override
	public void afterTransactionCompletion(Transaction tx) {
		date.remove();
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
			
			if (setValue(currentState, propertyNames, "dateCreated", (date.get() != null) ? date.get() : new Date(), true)) {
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
	
	/**
	 * Contains custom logic for checking for a dirty object
	 * 
	 * @param currentState
	 * @param previousState
	 * @param propertyNames
	 * @return true if the object associated to the states is dirty
	 */
	private boolean isDirty(Object[] currentState, Object[] previousState, String[] propertyNames) {
		for (int i = 0; i < propertyNames.length; i++) {
			Object previousValue = (previousState != null) ? previousState[i] : null;
			Object currentValue = (currentState != null) ? currentState[i] : null;
			if (!OpenmrsUtil.nullSafeEquals(previousValue, currentValue)) {
				return true;
			}
		}
		return false;
	}
}
