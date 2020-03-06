package org.openmrs.api.db.hibernate;

import static java.util.Collections.emptyList;
import java.util.SortedSet;  
import java.util.TreeSet;



import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.HashSet;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.mail.Address;

import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.util.GlobalPropertiesTestHelper;

public class HibernatePatientIdentifierMergeDAOTest extends BaseContextSensitiveTest {
	
	private HibernatePatientIdentifierMergeDAO hibernatePatientIdentifierMergeDao = new HibernatePatientIdentifierMergeDAO();
	private SessionFactory sessionFactory;
	
	@Before
	public void getPersonDAO() {
		sessionFactory = (SessionFactory) applicationContext.getBean("sessionFactory");
		hibernatePatientIdentifierMergeDao.setSessionFactory(sessionFactory);
	}
	
	

	@Test
	public void mergePatientIdentifierTypes() {
		
		// Getting the PatientIdentifierType objects in the mock database
		// OpenMRS Identification Number id is 1
		// Old Identification Number id is 2
		PatientIdentifierType patientIdentifierType1 = (PatientIdentifierType) sessionFactory.getCurrentSession().get(PatientIdentifierType.class, 1);
		PatientIdentifierType patientIdentifierType2 = (PatientIdentifierType) sessionFactory.getCurrentSession().get(PatientIdentifierType.class, 2);
				
		// Get patients that are already in the database
		Patient patient1 = (Patient) sessionFactory.getCurrentSession().get(Patient.class, 2);
		Patient patient2 = (Patient) sessionFactory.getCurrentSession().get(Patient.class, 6);
		
		// What the patient identifier types were before merging
		String beforeMerge = "SELECT identifier_type FROM patient_identifier";
		SQLQuery queryBefore = sessionFactory.getCurrentSession().createSQLQuery(beforeMerge);
		System.out.println(queryBefore.list());

		// Calling the merge function for merging duplicate patient identifier types
		hibernatePatientIdentifierMergeDao.mergePatientIdentifier(patientIdentifierType1, patientIdentifierType2);
		
		// Get the list of patient identifiers for all patients. They should all be 1 now. Was previously a mix of 1 and 2.
		String getAllPatientIdentifierUsed = "SELECT identifier_type FROM patient_identifier";
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(getAllPatientIdentifierUsed);
		System.out.println(query.list());
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
	
	
}
