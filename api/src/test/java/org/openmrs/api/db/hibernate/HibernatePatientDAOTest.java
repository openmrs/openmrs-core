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

import java.sql.Date;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.Hibernate;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HibernatePatientDAOTest extends BaseContextSensitiveTest {

	private static final String SEARCH_FIXTURE_TOKEN = "BOUNDED";

	private static final int SEARCH_FIXTURE_SIZE = 50;

	private static final int SMALL_PAGE = 5;

	private static final int LARGE_PAGE = 40;

	/** Headroom for effects that scale with the page without being per-hit loading. */
	private static final int PAGE_GROWTH_ALLOWANCE = 8;

	private HibernatePatientDAO hibernatePatientDao;

	private HibernatePersonDAO hibernatePersonDAO;

	@BeforeEach
	public void beforeEach() {
		updateSearchIndex();
		hibernatePatientDao = (HibernatePatientDAO) applicationContext.getBean("patientDAO");
		hibernatePersonDAO = (HibernatePersonDAO) applicationContext.getBean("personDAO");
	}

	@Test
	public void getPatientIdentifiers_shouldGetByIdentifierType() {
		List<PatientIdentifierType> identifierTypes = singletonList(new PatientIdentifierType(2));
		List<PatientIdentifier> identifiers = hibernatePatientDao.getPatientIdentifiers(null, identifierTypes, emptyList(),
		    emptyList(), null);
		List<Integer> identifierIds = identifiers.stream().map(PatientIdentifier::getId).collect(Collectors.toList());

		assertEquals(2, identifiers.size());
		assertThat(identifierIds, hasItems(1, 3));
	}

	@Test
	public void getPatientIdentifiers_shouldGetByPatients() {
		List<Patient> patients = Arrays.asList(hibernatePatientDao.getPatient(6), hibernatePatientDao.getPatient(7));
		List<PatientIdentifier> identifiers = hibernatePatientDao.getPatientIdentifiers(null, emptyList(), emptyList(),
		    patients, null);
		List<Integer> identifierIds = identifiers.stream().map(PatientIdentifier::getId).collect(Collectors.toList());

		assertEquals(2, identifiers.size());
		assertThat(identifierIds, hasItems(3, 4));
	}

	@Test
	public void getDuplicatePatientsByAttributes_shouldNotReturnPatientsWithUniqueNames() {
		// given
		Person person1 = new Person();
		person1.addName(new PersonName("Ioan", "Theo", "Fletcher"));
		person1 = hibernatePersonDAO.savePerson(person1);
		hibernatePatientDao.savePatient(new Patient(person1));

		Person person2 = new Person();
		person2.addName(new PersonName("Ioan1", "Theo1", "Fletcher1"));
		person2 = hibernatePersonDAO.savePerson(person2);
		hibernatePatientDao.savePatient(new Patient(person2));

		// when
		List<String> attributes = Arrays.asList("givenName", "middleName", "familyName");
		Collections.shuffle(attributes); // random order of attributes to make sure this test isn't flaky
		List<Patient> duplicatePatients = hibernatePatientDao.getDuplicatePatientsByAttributes(attributes);

		// then
		assertThat(duplicatePatients.size(), equalTo(0));
	}

	@Test
	public void getDuplicatePatientsByAttributes_shouldReturnPatientsWithDuplicatedAllNames() {
		// given
		Person person1 = new Person();
		person1.addName(new PersonName("Ioan", "Theo", "Fletcher"));
		person1 = hibernatePersonDAO.savePerson(person1);
		hibernatePatientDao.savePatient(new Patient(person1));

		Person person2 = new Person();
		person2.addName(new PersonName("Ioan", "Theo", "Fletcher"));
		person2 = hibernatePersonDAO.savePerson(person2);
		hibernatePatientDao.savePatient(new Patient(person2));
		Context.flushSession(); //needed by postgres

		// when
		List<String> attributes = Arrays.asList("givenName", "middleName", "familyName");
		Collections.shuffle(attributes); // random order of attributes to make sure this test isn't flaky
		List<Patient> duplicatePatients = hibernatePatientDao.getDuplicatePatientsByAttributes(attributes);

		// then
		assertThat(duplicatePatients.size(), equalTo(2));
	}

	@Test
	public void getDuplicatePatientsByAttributes_shouldReturnPatientsWithDuplicatedFamilyNames() {
		// given
		Person person1 = new Person();
		person1.addName(new PersonName("Ioan", "Theo", "Fletcher"));
		person1 = hibernatePersonDAO.savePerson(person1);
		hibernatePatientDao.savePatient(new Patient(person1));

		Person person2 = new Person();
		person2.addName(new PersonName("Ioan1", "Theo1", "Fletcher"));
		person2 = hibernatePersonDAO.savePerson(person2);
		hibernatePatientDao.savePatient(new Patient(person2));
		Context.flushSession(); //needed by postgres

		// when
		List<Patient> duplicatePatients = hibernatePatientDao.getDuplicatePatientsByAttributes(singletonList("familyName"));

		// then
		assertThat(duplicatePatients.size(), equalTo(2));
	}

	@Test
	public void getDuplicatePatientsByAttributes_shouldReturnPatientsWithDuplicatedAllFields() {
		// given
		Person person1 = new Person();
		person1.addName(new PersonName("Ioan", "Theo", "Fletcher"));
		person1.setGender("M");
		person1.setBirthdate(Date.valueOf("2021-06-26"));
		person1 = hibernatePersonDAO.savePerson(person1);
		hibernatePatientDao.savePatient(new Patient(person1));

		Person person2 = new Person();
		person2.addName(new PersonName("Ioan", "Theo", "Fletcher"));
		person2.setGender("M");
		person2.setBirthdate(Date.valueOf("2021-06-26"));
		person2 = hibernatePersonDAO.savePerson(person2);
		hibernatePatientDao.savePatient(new Patient(person2));
		Context.flushSession(); //needed by postgres

		// when
		List<String> attributes = Arrays.asList("gender", "birthdate", "givenName", "middleName", "familyName");
		Collections.shuffle(attributes); // random order of attributes to make sure this test isn't flaky
		List<Patient> duplicatePatients = hibernatePatientDao.getDuplicatePatientsByAttributes(attributes);

		// then
		assertThat(duplicatePatients.size(), equalTo(2));
	}

	@Test
	public void getDuplicatePatientsByAttributes_shouldNotReturnPatientsWithDuplicatedAllFieldsBesidesGender() {
		// given
		Person person1 = new Person();
		person1.addName(new PersonName("Ioan", "Theo", "Fletcher"));
		person1.setGender("M");
		person1.setBirthdate(Date.valueOf("2021-06-26"));
		person1 = hibernatePersonDAO.savePerson(person1);
		hibernatePatientDao.savePatient(new Patient(person1));

		Person person2 = new Person();
		person2.addName(new PersonName("Ioan", "Theo", "Fletcher"));
		person2.setGender("F");
		person2.setBirthdate(Date.valueOf("2021-06-26"));
		person2 = hibernatePersonDAO.savePerson(person2);
		hibernatePatientDao.savePatient(new Patient(person2));

		// when
		List<String> attributes = Arrays.asList("gender", "birthdate", "givenName", "middleName", "familyName");
		Collections.shuffle(attributes); // random order of attributes to make sure this test isn't flaky
		List<Patient> duplicatePatients = hibernatePatientDao.getDuplicatePatientsByAttributes(attributes);

		// then
		assertThat(duplicatePatients.size(), equalTo(0));
	}

	@Test
	public void getDuplicatePatientsByAttributes_shouldReturnPatientsWithDuplicatedAllFieldsIncludingIdentifier() {
		// given
		Person person1 = new Person();
		person1.addName(new PersonName("Ioan", "Theo", "Fletcher"));
		person1.setGender("M");
		person1.setBirthdate(Date.valueOf("2021-06-26"));
		person1 = hibernatePersonDAO.savePerson(person1);
		Patient patient1 = new Patient(person1);
		patient1.addIdentifier(new PatientIdentifier("101X", new PatientIdentifierType(1), null));
		hibernatePatientDao.savePatient(patient1);

		Person person2 = new Person();
		person2.addName(new PersonName("Ioan", "Theo", "Fletcher"));
		person2.setGender("M");
		person2.setBirthdate(Date.valueOf("2021-06-26"));
		person2 = hibernatePersonDAO.savePerson(person2);
		Patient patient2 = new Patient(person2);
		patient2.addIdentifier(new PatientIdentifier("101X", new PatientIdentifierType(1), null));
		hibernatePatientDao.savePatient(patient2);
		Context.flushSession(); //needed by postgres

		// when
		List<String> attributes = Arrays.asList("gender", "identifier", "birthdate", "givenName", "middleName",
		    "familyName");
		Collections.shuffle(attributes); // random order of attributes to make sure this test isn't flaky
		List<Patient> duplicatePatients = hibernatePatientDao.getDuplicatePatientsByAttributes(attributes);

		// then
		assertThat(duplicatePatients.size(), equalTo(2));
	}

	@Test
	public void getDuplicatePatientsByAttributes_shouldNotReturnVoidedPatientsWithDuplicatedAllFields() {
		// given
		Person person1 = new Person();
		person1.addName(new PersonName("Ioan", "Theo", "Fletcher"));
		person1.setGender("M");
		person1.setBirthdate(Date.valueOf("2021-06-26"));
		person1 = hibernatePersonDAO.savePerson(person1);
		Patient patient1 = new Patient(person1);
		patient1.addIdentifier(new PatientIdentifier("101X", new PatientIdentifierType(1), null));
		patient1.setVoided(true);
		hibernatePatientDao.savePatient(patient1);

		Person person2 = new Person();
		person2.addName(new PersonName("Ioan", "Theo", "Fletcher"));
		person2.setGender("M");
		person2.setBirthdate(Date.valueOf("2021-06-26"));
		person2 = hibernatePersonDAO.savePerson(person2);
		Patient patient2 = new Patient(person2);
		patient2.addIdentifier(new PatientIdentifier("101X", new PatientIdentifierType(1), null));
		patient2.setVoided(true);
		hibernatePatientDao.savePatient(patient2);

		// flush DB session to persist voiding patients
		Context.flushSession();

		// when
		List<String> attributes = Arrays.asList("gender", "identifier", "birthdate", "givenName", "middleName",
		    "familyName");
		Collections.shuffle(attributes); // random order of attributes to make sure this test isn't flaky
		List<Patient> duplicatePatients = hibernatePatientDao.getDuplicatePatientsByAttributes(attributes);

		// then
		assertThat(duplicatePatients.size(), equalTo(0));
	}

	@Test
	public void getDuplicatePatientsByAttributes_shouldReturnVoidedPatientsWithDuplicatedAllFieldsWithIncludeVoided() {
		// given
		Person person1 = new Person();
		person1.addName(new PersonName("Ioan", "Theo", "Fletcher"));
		person1.setGender("M");
		person1.setBirthdate(Date.valueOf("2021-06-26"));
		person1 = hibernatePersonDAO.savePerson(person1);
		Patient patient1 = new Patient(person1);
		patient1.addIdentifier(new PatientIdentifier("101X", new PatientIdentifierType(1), null));
		patient1.setVoided(true);
		hibernatePatientDao.savePatient(patient1);

		Person person2 = new Person();
		person2.addName(new PersonName("Ioan", "Theo", "Fletcher"));
		person2.setGender("M");
		person2.setBirthdate(Date.valueOf("2021-06-26"));
		person2 = hibernatePersonDAO.savePerson(person2);
		Patient patient2 = new Patient(person2);
		patient2.addIdentifier(new PatientIdentifier("101X", new PatientIdentifierType(1), null));
		patient2.setVoided(true);
		hibernatePatientDao.savePatient(patient2);

		// flush DB session to persist voiding patients
		Context.flushSession();

		// when
		List<String> attributes = Arrays.asList("gender", "identifier", "birthdate", "givenName", "middleName", "familyName",
		    "includeVoided");
		Collections.shuffle(attributes); // random order of attributes to make sure this test isn't flaky
		List<Patient> duplicatePatients = hibernatePatientDao.getDuplicatePatientsByAttributes(attributes);

		// then
		assertThat(duplicatePatients.size(), equalTo(2));
	}

	/**
	 * Asserts the shape of the cost rather than an absolute number, which would break on any change
	 * shifting the fixed overhead. Before {@link PatientIdentifier#getPatient()} was left unresolved
	 * while loading hits, the difference here was roughly one round trip per extra result.
	 */
	@Test
	public void getPatients_shouldNotIssueMoreDatabaseTripsAsTheIdentifierSearchPageGrows() {
		List<PatientIdentifierType> identifierTypes = singletonList(Context.getPatientService().getPatientIdentifierType(2));
		createIdentifierSearchFixture();

		// The first search of the process carries one-off warm-up, so prime it before measuring.
		hibernatePatientDao.getPatients(SEARCH_FIXTURE_TOKEN, identifierTypes, false, 0, SMALL_PAGE);

		int smallPageTrips = countDatabaseTrips(identifierTypes, SMALL_PAGE);
		int largePageTrips = countDatabaseTrips(identifierTypes, LARGE_PAGE);

		assertTrue(largePageTrips <= smallPageTrips + PAGE_GROWTH_ALLOWANCE,
		    String.format(
		        "identifier search should cost a bounded number of database trips regardless of page size, "
		                + "but a page of %d cost %d trips against %d for a page of %d",
		        LARGE_PAGE, largePageTrips, smallPageTrips, SMALL_PAGE));
	}

	/**
	 * The search's loading options are scoped to that search, so any other path must still eagerly
	 * populate the patient - otherwise callers reading it after the session closes would start failing.
	 */
	@Test
	public void getPatientIdentifiers_shouldStillEagerlyFetchThePatientOutsideSearch() {
		Context.flushSession();
		Context.clearSession();

		List<PatientIdentifier> identifiers = hibernatePatientDao.getPatientIdentifiers(null,
		    singletonList(new PatientIdentifierType(2)), emptyList(), emptyList(), null);

		assertThat(identifiers.isEmpty(), equalTo(false));
		for (PatientIdentifier identifier : identifiers) {
			assertTrue(Hibernate.isInitialized(identifier.getPatient()),
			    "the mapped eager fetch of PatientIdentifier.patient must be unchanged outside search");
		}
	}

	private int countDatabaseTrips(List<PatientIdentifierType> identifierTypes, int pageSize) {
		Context.flushSession();
		Context.clearSession();

		SessionFactory sessionFactory = (SessionFactory) applicationContext.getBean("sessionFactory");
		sessionFactory.getStatistics().clear();

		List<Patient> results = hibernatePatientDao.getPatients(SEARCH_FIXTURE_TOKEN, identifierTypes, false, 0, pageSize);

		assertEquals(pageSize, results.size(), "fixture should be large enough to fill the requested page");

		return (int) sessionFactory.getStatistics().getPrepareStatementCount();
	}

	private void createIdentifierSearchFixture() {
		PatientIdentifierType identifierType = Context.getPatientService().getPatientIdentifierType(2);

		for (int i = 0; i < SEARCH_FIXTURE_SIZE; i++) {
			Patient patient = new Patient();
			patient.addName(new PersonName("Bounded", null, "Patient" + i));
			patient.setGender("F");

			PatientIdentifier identifier = new PatientIdentifier(SEARCH_FIXTURE_TOKEN + String.format("%04d", i),
			        identifierType, null);
			identifier.setPreferred(true);
			patient.addIdentifier(identifier);

			hibernatePatientDao.savePatient(patient);
		}

		Context.flushSession();
		Context.clearSession();
		updateSearchIndex();
	}
}
