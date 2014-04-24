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

import static org.hamcrest.core.Is.is;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.Order;
import org.openmrs.api.APIException;

/**
 * Contains tests for ImmutableEntityInterceptor
 */
public class ImmutableEntityInterceptorTest {
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	private static class SomeImmutableEntityInterceptor extends ImmutableEntityInterceptor {
		
		boolean ignoreVoidedOrRetiredObjects = false;
		
		SomeImmutableEntityInterceptor() {
		}
		
		SomeImmutableEntityInterceptor(boolean ignoreVoidedOrRetiredObjects) {
			this.ignoreVoidedOrRetiredObjects = ignoreVoidedOrRetiredObjects;
		}
		
		static final String MUTABLE_FIELD_NAME = "mutable";
		
		static final String IMMUTABLE_FIELD_NAME = "immutable";
		
		@Override
		protected Class<?> getSupportedType() {
			return Order.class;
		}
		
		@Override
		protected String[] getMutablePropertyNames() {
			return new String[] { MUTABLE_FIELD_NAME };
		}
		
		@Override
		protected boolean ignoreVoidedOrRetiredObjects() {
			return ignoreVoidedOrRetiredObjects;
		}
	}
	
	/**
	 * @verifies fail if an entity has a changed property
	 * @see ImmutableEntityInterceptor#onFlushDirty(Object, java.io.Serializable, Object[],
	 *      Object[], String[], org.hibernate.type.Type[])
	 */
	@Test
	public void onFlushDirty_shouldFailIfAnEntityHasAChangedProperty() throws Exception {
		String[] propertyNames = new String[] { SomeImmutableEntityInterceptor.IMMUTABLE_FIELD_NAME };
		String[] previousState = new String[] { "old" };
		String[] currentState = new String[] { "new" };
		ImmutableEntityInterceptor interceptor = new SomeImmutableEntityInterceptor();
		expectedException.expect(APIException.class);
		expectedException.expectMessage(is("Editing some fields on " + interceptor.getSupportedType().getSimpleName()
		        + " is not allowed"));
		interceptor.onFlushDirty(new Order(), null, currentState, previousState, propertyNames, null);
	}
	
	/**
	 * @verifies pass if an entity has changes for an allowed mutable property
	 * @see ImmutableEntityInterceptor#onFlushDirty(Object, java.io.Serializable, Object[],
	 *      Object[], String[], org.hibernate.type.Type[])
	 */
	@Test
	public void onFlushDirty_shouldPassIfAnEntityHasChangesForAnAllowedMutableProperty() throws Exception {
		String[] propertyNames = new String[] { SomeImmutableEntityInterceptor.MUTABLE_FIELD_NAME };
		String[] previousState = new String[] { "old" };
		String[] currentState = new String[] { "new" };
		new SomeImmutableEntityInterceptor().onFlushDirty(new Order(), null, currentState, previousState, propertyNames,
		    null);
	}
	
	/**
	 * @verifies fail if the edited object is voided or retired and ignore is set to false
	 * @see ImmutableEntityInterceptor#onFlushDirty(Object, java.io.Serializable, Object[],
	 *      Object[], String[], org.hibernate.type.Type[])
	 */
	@Test
	public void onFlushDirty_shouldFailIfTheEditedObjectIsVoidedOrRetiredAndIgnoreIsSetToFalse() throws Exception {
		String[] propertyNames = new String[] { SomeImmutableEntityInterceptor.IMMUTABLE_FIELD_NAME };
		String[] previousState = new String[] { "old" };
		String[] currentState = new String[] { "new" };
		ImmutableEntityInterceptor interceptor = new SomeImmutableEntityInterceptor();
		expectedException.expect(APIException.class);
		expectedException.expectMessage(is("Editing some fields on " + interceptor.getSupportedType().getSimpleName()
		        + " is not allowed"));
		Order order = new Order();
		order.setVoided(true);
		interceptor.onFlushDirty(order, null, currentState, previousState, propertyNames, null);
	}
	
	/**
	 * @verifies pass if the edited object is voided or retired and ignore is set to true
	 * @see ImmutableEntityInterceptor#onFlushDirty(Object, java.io.Serializable, Object[],
	 *      Object[], String[], org.hibernate.type.Type[])
	 */
	@Test
	public void onFlushDirty_shouldPassIfTheEditedObjectIsVoidedOrRetiredAndIgnoreIsSetToTrue() throws Exception {
		String[] propertyNames = new String[] { SomeImmutableEntityInterceptor.IMMUTABLE_FIELD_NAME };
		String[] previousState = new String[] { "old" };
		String[] currentState = new String[] { "new" };
		ImmutableEntityInterceptor interceptor = new SomeImmutableEntityInterceptor(true);
		Order order = new Order();
		order.setVoided(true);
		interceptor.onFlushDirty(order, null, currentState, previousState, propertyNames, null);
	}
}
