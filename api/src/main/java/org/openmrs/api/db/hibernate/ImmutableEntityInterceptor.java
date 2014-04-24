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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.openmrs.api.APIException;
import org.openmrs.util.OpenmrsUtil;

/**
 * Superclass for all Interceptors that wish to ensure that changes to immutable entities of
 * specific type don't get persisted to the database, more granularity of the immutable properties
 * is also supported so as to allow editing some properties while not for others
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
	 * @see org.hibernate.EmptyInterceptor#onFlushDirty(Object, java.io.Serializable, Object[],
	 *      Object[], String[], org.hibernate.type.Type[])
	 * @should fail if an entity has a changed property
	 * @should pass if an entity has changes for an allowed mutable property
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
				throw new APIException("Editing some fields on " + getSupportedType().getSimpleName() + " is not allowed");
			}
		}
		
		return super.onFlushDirty(entity, id, currentState, previousState, propertyNames, types);
	}
}
