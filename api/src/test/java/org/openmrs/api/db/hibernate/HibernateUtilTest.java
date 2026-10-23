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

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.SessionFactory;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;
import org.hibernate.annotations.ParamDef;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptNumeric;
import org.openmrs.GlobalProperty;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.transaction.TestTransaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HibernateUtilTest extends BaseContextSensitiveTest {

	private static final String LOCATION_UUID = "8d6c993e-c2cc-11de-8d13-0010c6dffd0f";

	private static final String OTHER_LOCATION_UUID = "9356400c-a5a2-4532-8f2b-2361b3446eb8";

	private static final String HIDE_LOCATION_FILTER = "hideLocation";

	private static final String CONCEPT_UUID = "0cbe2ed3-cd5f-4f46-9459-26127c9265ab";

	private static final String NON_PATIENT_PERSON_UUID = "df8ae447-6745-45be-b859-403241d9913c";

	private static final String ASSIGNED_UUID = "3f1c9a52-8d7e-5b4a-9f60-2c1e7d8b4a93";

	@Autowired
	private SessionFactory sessionFactory;

	@AfterEach
	void disableHideLocationFilter() {
		sessionFactory.getCurrentSession().disableFilter(HIDE_LOCATION_FILTER);
	}

	@Test
	void getUniqueEntityByUUID_shouldReturnACachedEntityWithoutQueryingTheDatabase() {
		startNewTransaction();
		assertNotNull(getByUuid(Location.class, LOCATION_UUID));
		Context.clearSession();
		Statistics statistics = sessionFactory.getStatistics();
		statistics.clear();

		Location location = getByUuid(Location.class, LOCATION_UUID);

		assertEquals(LOCATION_UUID, location.getUuid());
		assertEquals(0, statistics.getPrepareStatementCount());
	}

	@Test
	void getUniqueEntityByUUID_shouldReturnACachedHbmMappedEntityWithoutQueryingTheDatabase() {
		startNewTransaction();
		assertNotNull(getByUuid(Concept.class, CONCEPT_UUID));
		Context.clearSession();
		Statistics statistics = sessionFactory.getStatistics();
		statistics.clear();

		Concept concept = getByUuid(Concept.class, CONCEPT_UUID);

		assertEquals(CONCEPT_UUID, concept.getUuid());
		assertEquals(0, statistics.getPrepareStatementCount());
	}

	@Test
	void getUniqueEntityByUUID_shouldReturnTheUpdatedEntityAfterACachedEntityIsChanged() {
		startNewTransaction();
		Location location = getByUuid(Location.class, LOCATION_UUID);
		assertTrue(sessionFactory.getCache().containsEntity(Location.class, location.getLocationId()));

		location.setName("Renamed location");
		Context.flushSession();
		Context.clearSession();

		assertEquals("Renamed location", getByUuid(Location.class, LOCATION_UUID).getName());
	}

	@Test
	void getUniqueEntityByUUID_shouldReturnNullAfterTheEntityIsDeleted() {
		Location location = new Location();
		location.setName("Location to delete");
		Context.getLocationService().saveLocation(location);
		Context.flushSession();
		Context.clearSession();
		String uuid = location.getUuid();

		Context.getLocationService().purgeLocation(getByUuid(Location.class, uuid));
		Context.flushSession();
		Context.clearSession();

		assertNull(getByUuid(Location.class, uuid));
	}

	@Test
	void getUniqueEntityByUUID_shouldFindAnEntityWhoseUuidWasAssignedBeforeItWasSaved() {
		Location location = new Location();
		location.setName("Location with an assigned uuid");
		location.setUuid(ASSIGNED_UUID);
		Context.getLocationService().saveLocation(location);
		Context.flushSession();
		Context.clearSession();

		assertEquals(location.getId(), getByUuid(Location.class, ASSIGNED_UUID).getId());
	}

	@Test
	void getUniqueEntityByUUID_shouldFindAnHbmMappedEntityWhoseUuidWasAssignedBeforeItWasSaved() {
		GlobalProperty globalProperty = new GlobalProperty("test.assignedUuid", "value");
		globalProperty.setUuid(ASSIGNED_UUID);
		Context.getAdministrationService().saveGlobalProperty(globalProperty);
		Context.flushSession();
		Context.clearSession();

		assertEquals("test.assignedUuid", getByUuid(GlobalProperty.class, ASSIGNED_UUID).getProperty());
	}

	@Test
	void getUniqueEntityByUUID_shouldReturnNullForAnUnknownUuid() {
		assertNull(getByUuid(Location.class, "unknown-uuid"));
	}

	@Test
	void getUniqueEntityByUUID_shouldReturnNullForANullUuid() {
		assertNull(getByUuid(Location.class, null));
	}

	@Test
	void getUniqueEntityByUUID_shouldReturnNullWhenTheUuidBelongsToAnotherSubclass() {
		assertNotNull(getByUuid(Person.class, NON_PATIENT_PERSON_UUID));
		assertNotNull(getByUuid(Concept.class, CONCEPT_UUID));

		assertNull(getByUuid(Patient.class, NON_PATIENT_PERSON_UUID));
		assertNull(getByUuid(ConceptNumeric.class, CONCEPT_UUID));
	}

	@Test
	void getUniqueEntityByUUID_shouldApplyAFilterEnabledOnTheSession() {
		enableHideLocationFilter();

		assertNull(getByUuid(FilteredLocation.class, LOCATION_UUID));
		assertNotNull(getByUuid(FilteredLocation.class, OTHER_LOCATION_UUID));
	}

	@Test
	void getUniqueEntityByUUID_shouldApplyAFilterEnabledOnTheSessionToACachedEntity() {
		startNewTransaction();
		FilteredLocation location = getByUuid(FilteredLocation.class, LOCATION_UUID);
		assertTrue(sessionFactory.getCache().containsEntity(FilteredLocation.class, location.getLocationId()));
		Context.clearSession();

		enableHideLocationFilter();

		assertNull(getByUuid(FilteredLocation.class, LOCATION_UUID));
	}

	private void enableHideLocationFilter() {
		sessionFactory.getCurrentSession().enableFilter(HIDE_LOCATION_FILTER).setParameter("uuid", LOCATION_UUID);
	}

	/**
	 * The second-level cache is cleared after every test, and it only stores what a transaction loads
	 * if that transaction started after the last clearing, counted in whole milliseconds. This starts a
	 * new transaction in a later millisecond than the clearing, so the loads of the test are cached.
	 */
	private void startNewTransaction() {
		TestTransaction.end();
		long clearedBy = System.currentTimeMillis();
		while (System.currentTimeMillis() <= clearedBy) {
			Thread.onSpinWait();
		}
		TestTransaction.start();
	}

	private <T> T getByUuid(Class<T> entityClass, String uuid) {
		return HibernateUtil.getUniqueEntityByUUID(sessionFactory, entityClass, uuid);
	}

	/**
	 * A read-only, cached view of the location table with a filter that hides the location with a given
	 * uuid. It exists only in tests, because no entity in core declares a filter.
	 */
	@Entity
	@Table(name = "location")
	@Immutable
	@Cacheable
	@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
	@NaturalIdCache
	@FilterDef(name = HIDE_LOCATION_FILTER, defaultCondition = "uuid <> :uuid", parameters = @ParamDef(name = "uuid", type = String.class))
	@Filter(name = HIDE_LOCATION_FILTER)
	static class FilteredLocation {

		@Id
		@GeneratedValue(strategy = GenerationType.IDENTITY)
		@Column(name = "location_id")
		private Integer locationId;

		@NaturalId
		@Column(name = "uuid", unique = true, nullable = false, length = 38)
		private String uuid;

		Integer getLocationId() {
			return locationId;
		}
	}
}
