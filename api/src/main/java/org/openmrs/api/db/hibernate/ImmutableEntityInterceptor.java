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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.openmrs.Retireable;
import org.openmrs.Voidable;
import org.openmrs.api.UnchangeableObjectException;
import org.openmrs.util.OpenmrsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
	
	private static final Logger log = LoggerFactory.getLogger(ImmutableEntityInterceptor.class);
	
	// This thread local enables storing additional mutable properties to allow for a given thread
	private final ThreadLocal<String[]> additionalMutableProperties = new ThreadLocal<>();
	
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
	 * <strong>Should</strong> fail if an entity has a changed property
	 * <strong>Should</strong> pass if an entity has changes for an allowed mutable property
	 * <strong>Should</strong> pass if the edited object is voided or retired and ignore is set to true
	 * <strong>Should</strong> fail if the edited object is voided or retired and ignore is set to false
	 */
	@Override
	public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState,
	                            String[] propertyNames, Type[] types) {
		
		if (getSupportedType().isAssignableFrom(entity.getClass())) {
			List<String> changedProperties = null;
			for (int i = 0; i < propertyNames.length; i++) {
				String property = propertyNames[i];
				if (isMutableProperty(property)) {
					continue;
				}
				
				boolean isVoidedOrRetired = false;
				if (Voidable.class.isAssignableFrom(entity.getClass())) {
					isVoidedOrRetired = ((Voidable) entity).getVoided();
				} else if (Retireable.class.isAssignableFrom(entity.getClass())) {
					isVoidedOrRetired = ((Retireable) entity).getRetired();
				}
				if (isVoidedOrRetired && ignoreVoidedOrRetiredObjects()) {
					continue;
				}
				
				Object previousValue = (previousState != null) ? previousState[i] : null;
				Object currentValue = (currentState != null) ? currentState[i] : null;
				if (!OpenmrsUtil.nullSafeEquals(currentValue, previousValue)) {
					if (changedProperties == null) {
						changedProperties = new ArrayList<>();
					}
					changedProperties.add(property);
				}
			}
			if (CollectionUtils.isNotEmpty(changedProperties)) {
				log.debug("The following fields cannot be changed for {} : {}", getSupportedType(), changedProperties);
				
				throw new UnchangeableObjectException("editing.fields.not.allowed", new Object[] { changedProperties,
				        getSupportedType().getSimpleName() });
			}
		}
		
		return false;
	}

	/**
	 * @return true if the given property is allowed to be mutable
	 */
	private boolean isMutableProperty(String property) {
		if (ArrayUtils.contains(getMutablePropertyNames(), property)) {
			return true;
		}
		String[] threadMutable = additionalMutableProperties.get();
		if (threadMutable != null && ArrayUtils.contains(threadMutable, property)) {
			return true;
		}
		return false;
	}

	/**
	 *  This allows code that is attempting to save immutable entities to bypass standard validation by allowing
	 *  additional properties to be considered mutable for the duration of the thread.
	 *  Note that the caller should ensure that {@link #removeMutablePropertiesForThread} is invoked when the operation is done
	 *  
	 * @param properties any additional properties that one wishes to make mutable for a given thread
	 */
	public void addMutablePropertiesForThread(String... properties) {
		additionalMutableProperties.set(properties);
	}

	/**
	 * If any additional properties were added for a given thread by invoking {@link #addMutablePropertiesForThread}, 
	 * this removes them.  NOTE, any usage of {@link #addMutablePropertiesForThread} should typically be followed by
	 * an invocation of this method in a finally block
	 */
	public void removeMutablePropertiesForThread() {
		additionalMutableProperties.remove();
	}
}
