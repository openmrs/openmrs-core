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
				log.debug("Setting changed by fields on " + entity);
			
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
