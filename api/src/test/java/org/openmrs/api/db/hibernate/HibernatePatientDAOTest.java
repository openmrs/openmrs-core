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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonName;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HibernatePatientDAOTest extends BaseContextSensitiveTest {

	private HibernatePatientDAO hibernatePatientDao;

	private HibernatePersonDAO hibernatePersonDAO;

	/**
	 * Restricts search-index rebuilds to the entity types patient searches actually query, instead of
	 * rebuilding every indexed type on each test.
	 */
	@Override
	public Class<?>[] getIndexedTypes() {
		return new Class<?>[] { PersonName.class, PersonAttribute.class, PatientIdentifier.class };
	}

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
	 * @see HibernatePatientDAO#getPatients(String, List, boolean, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldFindPatientsByIdentifierTypeName() {
		// Search using only the patient identifier type name.
		String identifierTypeName = "Old Identification Number";
		PatientIdentifierType type = Context.getPatientService().getPatientIdentifierTypeByName(identifierTypeName);
		List<Patient> patients = hibernatePatientDao.getPatients(identifierTypeName, singletonList(type), false, null, null);

		// Patients with identifiers of the requested type should be returned.
		assertEquals(3, patients.size());

		List<Integer> ids = patients.stream().map(Patient::getPatientId).collect(Collectors.toList());

		assertThat(ids, hasItems(2, 6, 432));
		// Patients with a different identifier type should not be returned.
		assertThat(ids, not(hasItem(7)));
	}

	/**
	 * @see HibernatePatientDAO#getPatients(String, List, boolean, Integer, Integer)
	 */
	@Test
	public void getPatients_shouldStillFindPatientsByIdentifierValue() {
		String identifierTypeName = "Old Identification Number";
		PatientIdentifierType type = Context.getPatientService().getPatientIdentifierTypeByName(identifierTypeName);

		// Ensure searching by identifier value continues to work after adding identifier type name to the search predicate.
		List<Patient> patients = hibernatePatientDao.getPatients("12345K", singletonList(type), false, null, null);

		assertThat(patients.stream().map(Patient::getPatientId).collect(Collectors.toList()), hasItem(6));
	}

	/**
	 * @see HibernatePatientDAO#findPatients(String, boolean, Integer, Integer)
	 */
	@Test
	public void findPatients_shouldFindPatientsByIdentifierTypeName() {
		String identifierTypeName = "Old Identification Number";
		List<Patient> patients = hibernatePatientDao.findPatients(identifierTypeName, false, null, null);

		List<Integer> ids = patients.stream().map(Patient::getPatientId).collect(Collectors.toList());

		assertThat(ids, hasItems(2, 6));
		assertThat(ids, not(hasItem(432))); // voided patient excluded
	}
}
