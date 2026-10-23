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

import org.hibernate.Interceptor;
import org.hibernate.type.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ChainingInterceptorTest {

	private final Object entity = new Object();

	private ChainingInterceptor chain;

	private LegacyInterceptor legacy;

	private CurrentInterceptor current;

	@BeforeEach
	public void setUp() {
		legacy = new LegacyInterceptor();
		current = new CurrentInterceptor();
		chain = new ChainingInterceptor();
		chain.addInterceptor(legacy);
		chain.addInterceptor(current);
	}

	@Test
	public void onPersist_shouldCallEachInterceptorExactlyOnce() {
		chain.onPersist(entity, 1, new Object[0], new String[0], new Type[0]);

		assertEquals(1, legacy.saves);
		assertEquals(1, current.persists);
	}

	@Test
	public void onSave_shouldCallEachInterceptorExactlyOnce() {
		chain.onSave(entity, 1, new Object[0], new String[0], new Type[0]);

		assertEquals(1, legacy.saves);
		assertEquals(1, current.persists);
	}

	@Test
	public void onPersist_shouldReturnTrueIfAnyInterceptorModifiedTheState() {
		assertFalse(chain.onPersist(entity, 1, new Object[0], new String[0], new Type[0]));

		current.modify = true;

		assertTrue(chain.onPersist(entity, 1, new Object[0], new String[0], new Type[0]));
		assertEquals(2, legacy.saves);
	}

	@Test
	public void onRemove_shouldCallEachInterceptorExactlyOnce() {
		chain.onRemove(entity, 1, new Object[0], new String[0], new Type[0]);

		assertEquals(1, legacy.deletes);
		assertEquals(1, current.removes);
	}

	@Test
	public void onDelete_shouldCallEachInterceptorExactlyOnce() {
		chain.onDelete(entity, 1, new Object[0], new String[0], new Type[0]);

		assertEquals(1, legacy.deletes);
		assertEquals(1, current.removes);
	}

	@Test
	public void shouldForwardStatelessSessionAndMergeCallbacks() {
		chain.onInsert(entity, 1, new Object[0], new String[0], new Type[0]);
		chain.onUpdate(entity, 1, new Object[0], new String[0], new Type[0]);
		chain.onUpsert(entity, 1, new Object[0], new String[0], new Type[0]);
		chain.onDelete(entity, 1, new String[0], new Type[0]);
		chain.preMerge(entity, new Object[0], new String[0], new Type[0]);
		chain.postMerge(entity, entity, 1, new Object[0], new Object[0], new String[0], new Type[0]);

		assertEquals(6, current.otherCallbacks);
	}

	/**
	 * Implements only the deprecated callbacks, like interceptors written for Hibernate 5.
	 */
	private static class LegacyInterceptor implements Interceptor {

		int saves;

		int deletes;

		@Override
		public boolean onSave(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types) {
			saves++;
			return false;
		}

		@Override
		public void onDelete(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types) {
			deletes++;
		}
	}

	private static class CurrentInterceptor implements Interceptor {

		int persists;

		int removes;

		int otherCallbacks;

		boolean modify;

		@Override
		public boolean onPersist(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types) {
			persists++;
			return modify;
		}

		@Override
		public void onRemove(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types) {
			removes++;
		}

		@Override
		public void onInsert(Object entity, Object id, Object[] state, String[] propertyNames, Type[] propertyTypes) {
			otherCallbacks++;
		}

		@Override
		public void onUpdate(Object entity, Object id, Object[] state, String[] propertyNames, Type[] propertyTypes) {
			otherCallbacks++;
		}

		@Override
		public void onUpsert(Object entity, Object id, Object[] state, String[] propertyNames, Type[] propertyTypes) {
			otherCallbacks++;
		}

		@Override
		public void onDelete(Object entity, Object id, String[] propertyNames, Type[] propertyTypes) {
			otherCallbacks++;
		}

		@Override
		public void preMerge(Object entity, Object[] state, String[] propertyNames, Type[] propertyTypes) {
			otherCallbacks++;
		}

		@Override
		public void postMerge(Object source, Object target, Object id, Object[] targetState, Object[] originalState,
		        String[] propertyNames, Type[] propertyTypes) {
			otherCallbacks++;
		}
	}
}
