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


import static org.junit.Assert.assertEquals;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.db.DAOException;
import org.openmrs.test.BaseContextSensitiveTest;

public class HibernatePatientIdentifierMergeDAOTest extends BaseContextSensitiveTest {
	
	private HibernatePatientIdentifierMergeDAO hibernatePatientIdentifierMergeDao = new HibernatePatientIdentifierMergeDAO();
	private SessionFactory sessionFactory;
	
	@Before
	public void getMergePatientIdentifierTypeDAO() {
		sessionFactory = (SessionFactory) applicationContext.getBean("sessionFactory");
		hibernatePatientIdentifierMergeDao.setSessionFactory(sessionFactory);
	}
	
	@Test
	public void getPatientIdentifer_shouldMergeThePatientIdentiferTypes() {
		// Getting the PatientIdentifierType objects in the mock database
		// OpenMRS Identification Number id is 1
		// Old Identification Number id is 2
		PatientIdentifierType patientIdentifierType1 = (PatientIdentifierType) sessionFactory.getCurrentSession().get(PatientIdentifierType.class, 1);
		PatientIdentifierType patientIdentifierType2 = (PatientIdentifierType) sessionFactory.getCurrentSession().get(PatientIdentifierType.class, 2);
				
		// Get patients that are already in the database
		Patient patient1 = (Patient) sessionFactory.getCurrentSession().get(Patient.class, 2);
		Patient patient2 = (Patient) sessionFactory.getCurrentSession().get(Patient.class, 6);

		// Calling the merge function for merging duplicate patient identifier types
		hibernatePatientIdentifierMergeDao.mergePatientIdentifier(patientIdentifierType1, patientIdentifierType2);
		
		// Get the list of patient identifiers for all patients. They should all be 1 now. Was previously a mix of 1 and 2.
		String getAllPatientIdentifierUsed = "SELECT identifier_type FROM patient_identifier";
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(getAllPatientIdentifierUsed);
		for(int i = 0; i < query.list().size(); i++) {
			assertEquals(1, query.list().get(i));
		}
		
		sessionFactory.getCurrentSession().update(patient1);
		sessionFactory.getCurrentSession().update(patient2);
		Patient updatedPatient1 = (Patient) sessionFactory.getCurrentSession().get(Patient.class, 2);
		Patient updatedPatient2 = (Patient) sessionFactory.getCurrentSession().get(Patient.class, 6);

		// patient1 and patient2 should now have the same patient identifier type, which is "OpenMRS Identification Number id"
	    assertEquals(updatedPatient1.getPatientIdentifier().getIdentifierType(), updatedPatient2.getPatientIdentifier().getIdentifierType());
	}	
	
	@Test
	public void getPatientIdentifer_shouldGiveEverWhenThereAreDuplicateIdentifiers() {
		
		// Getting the PatientIdentifierType objects in the mock database
		PatientIdentifierType patientIdentifierType1 = (PatientIdentifierType) sessionFactory.getCurrentSession().get(PatientIdentifierType.class, 1);
		PatientIdentifierType patientIdentifierType2 = (PatientIdentifierType) sessionFactory.getCurrentSession().get(PatientIdentifierType.class, 2);
				
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
			sessionFactory.getCurrentSession().save(patient2);
			// Should give exception because you're not allowed to have duplicate identifiers
			} catch (ConstraintViolationException e){
			}

		try {
			// Calling the merge duplicate id function. Will give DAOException
			hibernatePatientIdentifierMergeDao.mergePatientIdentifier(patientIdentifierType1, patientIdentifierType2);
			}
			catch(DAOException e) {
			}
	}
}
