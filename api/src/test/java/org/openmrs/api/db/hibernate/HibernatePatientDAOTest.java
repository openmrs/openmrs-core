/**
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
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.db.DAOException;
import org.openmrs.test.BaseContextSensitiveTest;

public class HibernatePatientDAOTest extends BaseContextSensitiveTest {

	private HibernatePatientDAO hibernatePatientDao;
	private SessionFactory sessionFactory;

	@Before
	public void beforeEach() {
		sessionFactory = (SessionFactory) applicationContext.getBean("sessionFactory");
		updateSearchIndex();
		hibernatePatientDao = (HibernatePatientDAO) applicationContext.getBean("patientDAO");
	}

	@Test
	public void getPatientIdentifiers_shouldGetByIdentifierType() {
		List<PatientIdentifierType> identifierTypes = singletonList(new PatientIdentifierType(2));
		List<PatientIdentifier> identifiers = hibernatePatientDao
				.getPatientIdentifiers(null, identifierTypes, emptyList(), emptyList(), null);
		List<Integer> identifierIds = identifiers.stream().map(PatientIdentifier::getId)
				.collect(Collectors.toList());

		Assert.assertEquals(2, identifiers.size());
		Assert.assertThat(identifierIds, hasItems(1, 3));
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

		Assert.assertEquals(2, identifiers.size());
		Assert.assertThat(identifierIds, hasItems(3, 4));
	}
	
	@Test
	public void getPatientIdentifer_shouldMergeThePatientIdentiferTypes() {
		// Getting the PatientIdentifierType objects in the mock database
		// OpenMRS Identification Number id is 1
		// Old Identification Number id is 2
		PatientIdentifierType patientIdentifierType1 = (PatientIdentifierType) sessionFactory.getCurrentSession()
		        .get(PatientIdentifierType.class, 1);
		PatientIdentifierType patientIdentifierType2 = (PatientIdentifierType) sessionFactory.getCurrentSession()
		        .get(PatientIdentifierType.class, 2);
		
		// Get patients that are already in the database
		Patient patient1 = (Patient) sessionFactory.getCurrentSession().get(Patient.class, 2);
		Patient patient2 = (Patient) sessionFactory.getCurrentSession().get(Patient.class, 6);
		
		// Calling the merge function for merging duplicate patient identifier types
		hibernatePatientDao.mergePatientIdentifier(patientIdentifierType1, patientIdentifierType2);
		
		// Get the list of patient identifiers for all patients. They should all be 1 now. Was previously a mix of 1 and 2.
		String getAllPatientIdentifierUsed = "SELECT identifier_type FROM patient_identifier";
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(getAllPatientIdentifierUsed);
		for (int i = 0; i < query.list().size(); i++) {
			assertEquals(1, query.list().get(i));
		}
		
		sessionFactory.getCurrentSession().update(patient1);
		sessionFactory.getCurrentSession().update(patient2);
		Patient updatedPatient1 = (Patient) sessionFactory.getCurrentSession().get(Patient.class, 2);
		Patient updatedPatient2 = (Patient) sessionFactory.getCurrentSession().get(Patient.class, 6);
		
		// patient1 and patient2 should now have the same patient identifier type, which is "OpenMRS Identification Number id"
		assertEquals(updatedPatient1.getPatientIdentifier().getIdentifierType(),
		    updatedPatient2.getPatientIdentifier().getIdentifierType());
	}
	
	@Test
	public void getPatientIdentifer_shouldGiveEverWhenThereAreDuplicateIdentifiers() {
		
		// Getting the PatientIdentifierType objects in the mock database
		PatientIdentifierType patientIdentifierType1 = (PatientIdentifierType) sessionFactory.getCurrentSession()
		        .get(PatientIdentifierType.class, 1);
		PatientIdentifierType patientIdentifierType2 = (PatientIdentifierType) sessionFactory.getCurrentSession()
		        .get(PatientIdentifierType.class, 2);
		
		// Get patients that are already in the database and chnage their patient identifiers to be the same (should give error)
		Patient patient1 = (Patient) sessionFactory.getCurrentSession().get(Patient.class, 2);
		Patient patient2 = (Patient) sessionFactory.getCurrentSession().get(Patient.class, 6);
		// Delete patient then readd to database with the new attributes (duplicate identifiers)
		sessionFactory.getCurrentSession().delete(patient2);
		PatientIdentifier p2 = patient2.getPatientIdentifier();
		p2.setIdentifier(patient1.getPatientIdentifier().getIdentifier());
		patient2.removeIdentifier(patient2.getPatientIdentifier());
		patient2.addIdentifier(p2);
		
		try {
			// Should give exception because you're not allowed to have duplicate identifiers
			sessionFactory.getCurrentSession().save(patient2);
			// Calling the merge duplicate id function. Will give DAOException
			hibernatePatientDao.mergePatientIdentifier(patientIdentifierType1, patientIdentifierType2);
			Assert.fail("Either ConstraintViolationException or DAOException was expected!");
		}
		catch (ConstraintViolationException | DAOException e) {
			// should have an exception
		}
	}
}
