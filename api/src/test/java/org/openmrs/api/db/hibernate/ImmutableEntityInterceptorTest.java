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
import org.openmrs.api.UnchangeableObjectException;
import org.openmrs.api.context.Context;
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
	 * @see ImmutableEntityInterceptor#onFlushDirty(Object, java.io.Serializable, Object[],
	 *      Object[], String[], org.hibernate.type.Type[])
	 */
	@Test
	public void onFlushDirty_shouldFailIfAnEntityHasAChangedProperty() {
		String[] propertyNames = new String[] { SomeImmutableEntityInterceptor.IMMUTABLE_FIELD_NAME };
		String[] previousState = new String[] { "old" };
		String[] currentState = new String[] { "new" };
		ImmutableEntityInterceptor interceptor = new SomeImmutableEntityInterceptor();
		expectedException.expect(UnchangeableObjectException.class);
		expectedException.expectMessage(is(Context.getMessageSourceService().getMessage("editing.fields.not.allowed",
		    new Object[] { "[immutable]", Order.class.getSimpleName() }, null)));
		interceptor.onFlushDirty(new Order(), null, currentState, previousState, propertyNames, null);
	}
	
	/**
	 * @see ImmutableEntityInterceptor#onFlushDirty(Object, java.io.Serializable, Object[],
	 *      Object[], String[], org.hibernate.type.Type[])
	 */
	@Test
	public void onFlushDirty_shouldPassIfAnEntityHasChangesForAnAllowedMutableProperty() {
		String[] propertyNames = new String[] { SomeImmutableEntityInterceptor.MUTABLE_FIELD_NAME };
		String[] previousState = new String[] { "old" };
		String[] currentState = new String[] { "new" };
		new SomeImmutableEntityInterceptor().onFlushDirty(new Order(), null, currentState, previousState, propertyNames,
		    null);
	}
	
	/**
	 * @see ImmutableEntityInterceptor#onFlushDirty(Object, java.io.Serializable, Object[],
	 *      Object[], String[], org.hibernate.type.Type[])
	 */
	@Test
	public void onFlushDirty_shouldFailIfTheEditedObjectIsVoidedOrRetiredAndIgnoreIsSetToFalse() {
		String[] propertyNames = new String[] { SomeImmutableEntityInterceptor.IMMUTABLE_FIELD_NAME };
		String[] previousState = new String[] { "old" };
		String[] currentState = new String[] { "new" };
		ImmutableEntityInterceptor interceptor = new SomeImmutableEntityInterceptor();
		expectedException.expect(UnchangeableObjectException.class);
		expectedException.expectMessage(is(Context.getMessageSourceService().getMessage("editing.fields.not.allowed",
		    new Object[] { "[immutable]", Order.class.getSimpleName() }, null)));
		Order order = new Order();
		order.setVoided(true);
		interceptor.onFlushDirty(order, null, currentState, previousState, propertyNames, null);
	}
	
	/**
	 * @see ImmutableEntityInterceptor#onFlushDirty(Object, java.io.Serializable, Object[],
	 *      Object[], String[], org.hibernate.type.Type[])
	 */
	@Test
	public void onFlushDirty_shouldPassIfTheEditedObjectIsVoidedOrRetiredAndIgnoreIsSetToTrue() {
		String[] propertyNames = new String[] { SomeImmutableEntityInterceptor.IMMUTABLE_FIELD_NAME };
		String[] previousState = new String[] { "old" };
		String[] currentState = new String[] { "new" };
		ImmutableEntityInterceptor interceptor = new SomeImmutableEntityInterceptor(true);
		Order order = new Order();
		order.setVoided(true);
		interceptor.onFlushDirty(order, null, currentState, previousState, propertyNames, null);
	}
}
