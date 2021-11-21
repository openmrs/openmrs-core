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

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
import org.openmrs.PersonName;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

public class HibernatePatientDAOTest extends BaseContextSensitiveTest {

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
		List<PatientIdentifier> identifiers = hibernatePatientDao
				.getPatientIdentifiers(null, identifierTypes, emptyList(), emptyList(), null);
		List<Integer> identifierIds = identifiers.stream().map(PatientIdentifier::getId)
				.collect(Collectors.toList());

		assertEquals(2, identifiers.size());
		assertThat(identifierIds, hasItems(1, 3));
	}

	@Test
	public void getPatientIdentifiers_shouldGetByPatients() {
		List<Patient> patients = Arrays.asList(
				hibernatePatientDao.getPatient(6),
				hibernatePatientDao.getPatient(7)
		);
		List<PatientIdentifier> identifiers = hibernatePatientDao
				.getPatientIdentifiers(null, emptyList(), emptyList(), patients, null);
		List<Integer> identifierIds = identifiers.stream().map(PatientIdentifier::getId)
				.collect(Collectors.toList());

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
		patient1.addIdentifier(new PatientIdentifier("101X", null, null));
		hibernatePatientDao.savePatient(patient1);

		Person person2 = new Person();
		person2.addName(new PersonName("Ioan", "Theo", "Fletcher"));
		person2.setGender("M");
		person2.setBirthdate(Date.valueOf("2021-06-26"));
		person2 = hibernatePersonDAO.savePerson(person2);
		Patient patient2 = new Patient(person2);
		patient2.addIdentifier(new PatientIdentifier("101X", null, null));
		hibernatePatientDao.savePatient(patient2);

		// when
		List<String> attributes = Arrays.asList("gender", "identifier", "birthdate", "givenName", "middleName", "familyName");
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
		patient1.addIdentifier(new PatientIdentifier("101X", null, null));
		patient1.setVoided(true);
		hibernatePatientDao.savePatient(patient1);

		Person person2 = new Person();
		person2.addName(new PersonName("Ioan", "Theo", "Fletcher"));
		person2.setGender("M");
		person2.setBirthdate(Date.valueOf("2021-06-26"));
		person2 = hibernatePersonDAO.savePerson(person2);
		Patient patient2 = new Patient(person2);
		patient2.addIdentifier(new PatientIdentifier("101X", null, null));
		patient2.setVoided(true);
		hibernatePatientDao.savePatient(patient2);

		// flush DB session to persist voiding patients
		Context.flushSession();

		// when
		List<String> attributes = Arrays.asList("gender", "identifier", "birthdate", "givenName", "middleName", "familyName");
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
		patient1.addIdentifier(new PatientIdentifier("101X", null, null));
		patient1.setVoided(true);
		hibernatePatientDao.savePatient(patient1);

		Person person2 = new Person();
		person2.addName(new PersonName("Ioan", "Theo", "Fletcher"));
		person2.setGender("M");
		person2.setBirthdate(Date.valueOf("2021-06-26"));
		person2 = hibernatePersonDAO.savePerson(person2);
		Patient patient2 = new Patient(person2);
		patient2.addIdentifier(new PatientIdentifier("101X", null, null));
		patient2.setVoided(true);
		hibernatePatientDao.savePatient(patient2);

		// flush DB session to persist voiding patients
		Context.flushSession();

		// when
		List<String> attributes = Arrays.asList("gender", "identifier", "birthdate", "givenName", "middleName", "familyName", "includeVoided");
		Collections.shuffle(attributes); // random order of attributes to make sure this test isn't flaky
		List<Patient> duplicatePatients = hibernatePatientDao.getDuplicatePatientsByAttributes(attributes);

		// then
		assertThat(duplicatePatients.size(), equalTo(2));
	}
}
