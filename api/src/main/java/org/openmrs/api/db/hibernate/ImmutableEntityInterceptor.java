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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.openmrs.Retireable;
import org.openmrs.Voidable;
import org.openmrs.api.APIException;
import org.openmrs.util.OpenmrsUtil;

/**
 * Superclass for all Interceptors that would like to ensure that changes to immutable entities of
 * specific types don't get persisted to the database, more granularity of the immutable properties
 * is also supported so as to allow editing some properties while not for others
 * 
 * <pre>
 * <b>NOTE:</b> Subclasses MUST not make any changes to the persistent object because they get 
 * called last, if they make any changes other interceptors would never know about them.
 * </pre>
 * 
 * @since 1.10
 */
public abstract class ImmutableEntityInterceptor extends EmptyInterceptor {
	
	private static final Log log = LogFactory.getLog(ImmutableEntityInterceptor.class);
	
	/**
	 * Returns the class handled by the interceptor
	 */
	protected abstract Class<?> getSupportedType();
	
	/**
	 * Subclasses can override this to return fields that are allowed to be edited, returning null
	 * or an empty array implies the entity is immutable
	 * 
	 * @return an array of properties
	 */
	protected String[] getMutablePropertyNames() {
		return null;
	}
	
	/**
	 * Subclasses can override this to specify whether voided or retired items are mutable
	 * 
	 * @return true if voided or retired objects are mutable otherwise false means they are
	 *         immutable
	 */
	protected boolean ignoreVoidedOrRetiredObjects() {
		return false;
	}
	
	/**
	 * @see org.hibernate.EmptyInterceptor#onFlushDirty(Object, java.io.Serializable, Object[],
	 *      Object[], String[], org.hibernate.type.Type[])
	 * @should fail if an entity has a changed property
	 * @should pass if an entity has changes for an allowed mutable property
	 * @should pass if the edited object is voided or retired and ignore is set to true
	 * @should fail if the edited object is voided or retired and ignore is set to false
	 */
	@Override
	public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState,
	        String[] propertyNames, Type[] types) {
		
		if (getSupportedType().isAssignableFrom(entity.getClass())) {
			List<String> changedProperties = null;
			for (int i = 0; i < propertyNames.length; i++) {
				String property = propertyNames[i];
				if (ArrayUtils.contains(getMutablePropertyNames(), property)) {
					continue;
				}
				
				boolean isVoidedOrRetired = false;
				if (Voidable.class.isAssignableFrom(entity.getClass())) {
					isVoidedOrRetired = ((Voidable) entity).isVoided();
				} else if (Retireable.class.isAssignableFrom(entity.getClass())) {
					isVoidedOrRetired = ((Retireable) entity).isRetired();
				}
				if (isVoidedOrRetired && ignoreVoidedOrRetiredObjects()) {
					continue;
				}
				
				Object previousValue = (previousState != null) ? previousState[i] : null;
				Object currentValue = (currentState != null) ? currentState[i] : null;
				if (!OpenmrsUtil.nullSafeEquals(currentValue, previousValue)) {
					if (changedProperties == null) {
						changedProperties = new ArrayList<String>();
					}
					changedProperties.add(property);
				}
			}
			if (CollectionUtils.isNotEmpty(changedProperties)) {
				if (log.isDebugEnabled()) {
					log.debug("The following fields cannot be changed for " + getSupportedType() + ":" + changedProperties);
				}
				throw new APIException("editing.fields.not.allowed", new Object[] { getSupportedType().getSimpleName() });
			}
		}
		
		return super.onFlushDirty(entity, id, currentState, previousState, propertyNames, types);
	}
}
