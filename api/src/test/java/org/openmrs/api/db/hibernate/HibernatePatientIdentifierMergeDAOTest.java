package org.openmrs.api.db.hibernate;


import static org.junit.Assert.assertEquals;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.test.BaseContextSensitiveTest;

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
