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
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.CallbackException;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.openmrs.Auditable;
import org.openmrs.User;
import org.openmrs.api.context.Context;

/**
 * This class looks for {@link Auditable} that are being inserted into the database. When found, if
 * the class is an update (instead of an insert) then the changedBy and dateChanged fields are set
 * to the current user and the current time. <br/>
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
	 * This class method is only called when flushing an updated dirty object, not inserting objects
	 * 
	 * @return true if the object got the changedBy and dateChanged fields set
	 * @should return false for non Auditable objects
	 * @should set the dateChanged field
	 * @should set the changedBy field
	 * @should be called when saving an Auditable
	 * 
	 * @see org.hibernate.EmptyInterceptor#onFlushDirty(java.lang.Object, java.io.Serializable, java.lang.Object[], java.lang.Object[], java.lang.String[], org.hibernate.type.Type[])
	 */
	@Override
	public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState,
	        String[] propertyNames, Type[] types) throws CallbackException {
		
		if (entity instanceof Auditable) {
			if (log.isDebugEnabled())
				log.debug("Setting changed by fields on " + entity);
			
			// the return value
			boolean objectWasChanged = false;
			
			// loop over the properties and only change the changedBy and dateChanged fields
			Date currentDate = new Date();
			
			User authenticatedUser = Context.getAuthenticatedUser();
			for (int x = 0; x < propertyNames.length; x++) {
				if (propertyNames[x].equals("changedBy") && previousState != null && previousState[x] != authenticatedUser) {
					currentState[x] = authenticatedUser;
					objectWasChanged = true;
				} else if (propertyNames[x].equals("dateChanged") && previousState != null
				        && previousState[x] != currentDate) {
					currentState[x] = currentDate;
					objectWasChanged = true;
				}
			}
			
			// tell hibernate that we've changed this object
			return objectWasChanged;
		}
		
		// if we get here it means we didn't change anything
		return false;
	}
	
}
