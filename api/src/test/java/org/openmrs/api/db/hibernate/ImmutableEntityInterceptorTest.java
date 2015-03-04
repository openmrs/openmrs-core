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

import static org.hamcrest.core.Is.is;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.Order;
import org.openmrs.api.APIException;
import org.openmrs.test.BaseContextSensitiveTest;

/**
 * Contains tests for ImmutableEntityInterceptor
 */
public class ImmutableEntityInterceptorTest extends BaseContextSensitiveTest {
	
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
		expectedException.expectMessage(is("editing.fields.not.allowed"));
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
		expectedException.expectMessage(is("editing.fields.not.allowed"));
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
