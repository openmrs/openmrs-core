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

import java.sql.Timestamp;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.api.UnchangeableObjectException;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.DateUtil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Contains tests for ImmutableEntityInterceptor
 */
public class ImmutableEntityInterceptorTest extends BaseContextSensitiveTest {

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
			return new String[] { MUTABLE_FIELD_NAME, "voided" };
		}

		@Override
		protected boolean ignoreVoidedOrRetiredObjects() {
			return ignoreVoidedOrRetiredObjects;
		}
	}

	private static class SomeImmutableMetadataInterceptor extends ImmutableEntityInterceptor {

		@Override
		protected Class<?> getSupportedType() {
			return Location.class;
		}

		@Override
		protected boolean ignoreVoidedOrRetiredObjects() {
			return true;
		}
	}

	/**
	 * @see ImmutableEntityInterceptor#onFlushDirty(Object, java.io.Serializable, Object[], Object[],
	 *      String[], org.hibernate.type.Type[])
	 */
	@Test
	public void onFlushDirty_shouldFailIfAnEntityHasAChangedProperty() {
		String[] propertyNames = new String[] { SomeImmutableEntityInterceptor.IMMUTABLE_FIELD_NAME };
		String[] previousState = new String[] { "old" };
		String[] currentState = new String[] { "new" };
		ImmutableEntityInterceptor interceptor = new SomeImmutableEntityInterceptor();
		UnchangeableObjectException exception = assertThrows(UnchangeableObjectException.class,
		    () -> interceptor.onFlushDirty(new Order(), null, currentState, previousState, propertyNames, null));
		assertThat(exception.getMessage(), is(Context.getMessageSourceService().getMessage("editing.fields.not.allowed",
		    new Object[] { "[immutable]", Order.class.getSimpleName() }, null)));
	}

	/**
	 * @see ImmutableEntityInterceptor#onFlushDirty(Object, java.io.Serializable, Object[], Object[],
	 *      String[], org.hibernate.type.Type[])
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
	 * @see ImmutableEntityInterceptor#onFlushDirty(Object, java.io.Serializable, Object[], Object[],
	 *      String[], org.hibernate.type.Type[])
	 */
	@Test
	public void onFlushDirty_shouldFailIfTheEditedObjectIsVoidedInTheDatabaseAndIgnoreIsSetToFalse() {
		String[] propertyNames = new String[] { SomeImmutableEntityInterceptor.IMMUTABLE_FIELD_NAME, "voided" };
		Object[] previousState = new Object[] { "old", true };
		Object[] currentState = new Object[] { "new", true };
		ImmutableEntityInterceptor interceptor = new SomeImmutableEntityInterceptor();
		UnchangeableObjectException exception = assertThrows(UnchangeableObjectException.class,
		    () -> interceptor.onFlushDirty(new Order(), null, currentState, previousState, propertyNames, null));
		assertThat(exception.getMessage(), is(Context.getMessageSourceService().getMessage("editing.fields.not.allowed",
		    new Object[] { "[immutable]", Order.class.getSimpleName() }, null)));
	}

	/**
	 * @see ImmutableEntityInterceptor#onFlushDirty(Object, java.io.Serializable, Object[], Object[],
	 *      String[], org.hibernate.type.Type[])
	 */
	@Test
	public void onFlushDirty_shouldPassIfTheEditedObjectIsVoidedInTheDatabaseAndIgnoreIsSetToTrue() {
		String[] propertyNames = new String[] { SomeImmutableEntityInterceptor.IMMUTABLE_FIELD_NAME, "voided" };
		Object[] previousState = new Object[] { "old", true };
		Object[] currentState = new Object[] { "new", true };
		ImmutableEntityInterceptor interceptor = new SomeImmutableEntityInterceptor(true);
		interceptor.onFlushDirty(new Order(), null, currentState, previousState, propertyNames, null);
	}

	/**
	 * @see ImmutableEntityInterceptor#onFlushDirty(Object, Object, Object[], Object[], String[],
	 *      org.hibernate.type.Type[])
	 */
	@Test
	public void onFlushDirty_shouldPassIfTheEditedObjectIsRetiredInTheDatabaseAndIgnoreIsSetToTrue() {
		String[] propertyNames = new String[] { "name", "retired" };
		Object[] previousState = new Object[] { "old", true };
		Object[] currentState = new Object[] { "new", true };
		new SomeImmutableMetadataInterceptor().onFlushDirty(new Location(), null, currentState, previousState, propertyNames,
		    null);
	}

	/**
	 * @see ImmutableObsInterceptor#isAllowedChange(Object, String, Object, Object)
	 */
	@Test
	public void isAllowedChange_shouldFailIfAnObsLosesItsPerson() {
		Encounter encounter = new Encounter();
		encounter.setPatient(new Patient(7));
		Obs obs = new Obs();
		obs.setVoided(true);
		obs.setEncounter(encounter);
		String[] propertyNames = new String[] { "person" };
		Object[] previousState = new Object[] { new Person(7) };
		Object[] currentState = new Object[] { null };
		ImmutableEntityInterceptor interceptor = new ImmutableObsInterceptor();
		assertThrows(UnchangeableObjectException.class,
		    () -> interceptor.onFlushDirty(obs, null, currentState, previousState, propertyNames, null));
	}

	/**
	 * A live obs moves to another person as a new version, so even a move to its encounter's patient is
	 * rejected in place.
	 *
	 * @see ImmutableObsInterceptor#isAllowedChange(Object, String, Object, Object)
	 */
	@Test
	public void isAllowedChange_shouldRejectAPersonChangeOnALiveObs() {
		Encounter encounter = new Encounter();
		encounter.setPatient(new Patient(7));
		Obs obs = new Obs();
		obs.setEncounter(encounter);
		String[] propertyNames = new String[] { "person" };
		ImmutableEntityInterceptor interceptor = new ImmutableObsInterceptor();
		assertThrows(UnchangeableObjectException.class, () -> interceptor.onFlushDirty(obs, null,
		    new Object[] { new Person(7) }, new Object[] { new Person(3) }, propertyNames, null));
	}

	/**
	 * @see ImmutableEntityInterceptor#setPatientMergeForThread(Patient, Patient)
	 */
	@Test
	public void setPatientMergeForThread_shouldLetAVoidedObsMoveInAPatientMergeOnlyFromTheNonPreferredToThePreferredPatient() {
		Obs obs = new Obs();
		obs.setVoided(true);
		String[] propertyNames = new String[] { "person" };
		ImmutableEntityInterceptor interceptor = new ImmutableObsInterceptor();
		ImmutableEntityInterceptor.setPatientMergeForThread(new Patient(999), new Patient(7));
		try {
			interceptor.onFlushDirty(obs, null, new Object[] { new Person(999) }, new Object[] { new Person(7) },
			    propertyNames, null);
			assertThrows(UnchangeableObjectException.class, () -> interceptor.onFlushDirty(obs, null,
			    new Object[] { new Person(999) }, new Object[] { new Person(2) }, propertyNames, null));
			assertThrows(UnchangeableObjectException.class, () -> interceptor.onFlushDirty(obs, null,
			    new Object[] { new Person(2) }, new Object[] { new Person(7) }, propertyNames, null));
		} finally {
			ImmutableEntityInterceptor.removePatientMergeForThread();
		}
	}

	/**
	 * @see ImmutableObsInterceptor#isAllowedChange(Object, String, Object, Object)
	 */
	@Test
	public void isAllowedChange_shouldRejectAPersonChangeOnAGroupMemberWithoutAnEncounterOutsideAPatientMerge() {
		Encounter groupEncounter = new Encounter();
		groupEncounter.setPatient(new Patient(2));
		Obs group = new Obs();
		group.setEncounter(groupEncounter);
		Obs member = new Obs();
		member.setVoided(true);
		member.setObsGroup(group);
		String[] propertyNames = new String[] { "person" };
		ImmutableEntityInterceptor interceptor = new ImmutableObsInterceptor();
		assertThrows(UnchangeableObjectException.class, () -> interceptor.onFlushDirty(member, null,
		    new Object[] { new Person(2) }, new Object[] { new Person(7) }, propertyNames, null));
	}

	/**
	 * @see ImmutableObsInterceptor#isAllowedChange(Object, String, Object, Object)
	 */
	@Test
	public void isAllowedChange_shouldHoldAGroupMemberWithAnEncounterToThePatientOfThatEncounter() {
		Encounter ownEncounter = new Encounter();
		ownEncounter.setPatient(new Patient(7));
		Encounter groupEncounter = new Encounter();
		groupEncounter.setPatient(new Patient(2));
		Obs group = new Obs();
		group.setEncounter(groupEncounter);
		Obs member = new Obs();
		member.setVoided(true);
		member.setEncounter(ownEncounter);
		member.setObsGroup(group);
		String[] propertyNames = new String[] { "person" };
		ImmutableEntityInterceptor interceptor = new ImmutableObsInterceptor();
		interceptor.onFlushDirty(member, null, new Object[] { new Person(7) }, new Object[] { new Person(3) }, propertyNames,
		    null);
		assertThrows(UnchangeableObjectException.class, () -> interceptor.onFlushDirty(member, null,
		    new Object[] { new Person(2) }, new Object[] { new Person(3) }, propertyNames, null));
	}

	/**
	 * Whether an object is voided is read from its stored state, so voiding it in the same flush does
	 * not let its other fields change.
	 *
	 * @see ImmutableEntityInterceptor#onFlushDirty(Object, Object, Object[], Object[], String[],
	 *      org.hibernate.type.Type[])
	 */
	@Test
	public void onFlushDirty_shouldFailIfTheEditedObjectIsVoidedInTheSameFlushAndIgnoreIsSetToTrue() {
		String[] propertyNames = new String[] { SomeImmutableEntityInterceptor.IMMUTABLE_FIELD_NAME, "voided" };
		Object[] previousState = new Object[] { "old", false };
		Object[] currentState = new Object[] { "new", true };
		ImmutableEntityInterceptor interceptor = new SomeImmutableEntityInterceptor(true);
		Order order = new Order();
		order.setVoided(true);
		UnchangeableObjectException exception = assertThrows(UnchangeableObjectException.class,
		    () -> interceptor.onFlushDirty(order, null, currentState, previousState, propertyNames, null));
		assertThat(exception.getMessage(), is(Context.getMessageSourceService().getMessage("editing.fields.not.allowed",
		    new Object[] { "[immutable]", Order.class.getSimpleName() }, null)));
	}

	/**
	 * {@link DropMillisecondsHibernateInterceptor} truncates dates before this check runs, so a stored
	 * date with a sub-second part is not a change.
	 *
	 * @see ImmutableEntityInterceptor#onFlushDirty(Object, Object, Object[], Object[], String[],
	 *      org.hibernate.type.Type[])
	 */
	@Test
	public void onFlushDirty_shouldPassIfADateDiffersOnlyBelowASecond() {
		Timestamp stored = new Timestamp(1700000000123L);
		String[] propertyNames = new String[] { SomeImmutableEntityInterceptor.IMMUTABLE_FIELD_NAME };
		Object[] previousState = new Object[] { stored };
		Object[] currentState = new Object[] { DateUtil.truncateToSeconds(stored) };
		new SomeImmutableEntityInterceptor().onFlushDirty(new Order(), null, currentState, previousState, propertyNames,
		    null);
	}

	/**
	 * @see ImmutableEntityInterceptor#onFlushDirty(Object, Object, Object[], Object[], String[],
	 *      org.hibernate.type.Type[])
	 */
	@Test
	public void onFlushDirty_shouldPassIfADateBefore1970DiffersOnlyBelowASecond() {
		Timestamp stored = new Timestamp(-1500L);
		String[] propertyNames = new String[] { SomeImmutableEntityInterceptor.IMMUTABLE_FIELD_NAME };
		Object[] previousState = new Object[] { stored };
		Object[] currentState = new Object[] { DateUtil.truncateToSeconds(stored) };
		new SomeImmutableEntityInterceptor().onFlushDirty(new Order(), null, currentState, previousState, propertyNames,
		    null);
	}

	/**
	 * @see ImmutableEntityInterceptor#onFlushDirty(Object, Object, Object[], Object[], String[],
	 *      org.hibernate.type.Type[])
	 */
	@Test
	public void onFlushDirty_shouldFailIfADateFallsInAnotherSecond() {
		Timestamp stored = new Timestamp(1700000000123L);
		String[] propertyNames = new String[] { SomeImmutableEntityInterceptor.IMMUTABLE_FIELD_NAME };
		Object[] previousState = new Object[] { stored };
		Object[] currentState = new Object[] { new Date(1700000001000L) };
		ImmutableEntityInterceptor interceptor = new SomeImmutableEntityInterceptor();
		assertThrows(UnchangeableObjectException.class,
		    () -> interceptor.onFlushDirty(new Order(), null, currentState, previousState, propertyNames, null));
	}
}
