package org.openmrs.api.db;

import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonName;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;

public class PatientDAOTest extends BaseContextSensitiveTest {
	
	private PatientDAO dao = null;
	
	PatientService pService = null;
	
	/**
	 * Run this before each unit test in this class. The "@Before" method in
	 * {@link BaseContextSensitiveTest} is run right before this method.
	 * 
	 * @throws Exception
	 */
	@Before
	public void runBeforeEachTest() throws Exception {
		
		if (dao == null)
			// fetch the dao from the spring application context 
			// this bean name matches the name in /metadata/spring/applicationContext-service.xml 
			dao = (PatientDAO) applicationContext.getBean("patientDAO");
		if (pService == null)
			pService = Context.getPatientService();
	}
	
	/**
	 * @see {@link PatientDAO#getPatients(String,String,List<QPatientIdentifierType;>,null)}
	 */
	@Test
	@Verifies(value = "should escape an asterix character in identifier phrase", method = "getPatients(String,String,List<QPatientIdentifierType;>,null)")
	public void getPatients_shouldEscapeAnAsterixCharacterInIdentifierPhrase() throws Exception {
		//Note that all tests for wildcard should be pass in 2s due to the behaviour of wildcards,
		//that is we test for the size and actual patient object returned
		Patient patient2 = pService.getPatient(2);
		PatientIdentifier patientIdentifier = new PatientIdentifier("\\*567", pService.getPatientIdentifierType(2), Context
		        .getLocationService().getLocation(1));
		patient2.addIdentifier(patientIdentifier);
		pService.savePatient(patient2);
		
		//add closely matching identifier to a different patient
		Patient patient6 = pService.getPatient(6);
		PatientIdentifier patientIdentifier6 = new PatientIdentifier("4567", pService.getPatientIdentifierType(2), Context
		        .getLocationService().getLocation(1));
		patient6.addIdentifier(patientIdentifier6);
		pService.savePatient(patient6);
		
		List<PatientIdentifierType> identifierTypes = Collections.emptyList();
		//we expect only one matching patient       
		int actualSize = dao.getPatients(null, "*567", identifierTypes, false).size();
		Assert.assertEquals(1, actualSize);
		
		//if actually the search returned the matching patient       
		Patient actualPatient = dao.getPatients(null, "*567", identifierTypes, false).get(0);
		
		Assert.assertEquals(patient2, actualPatient);
	}
	
	/**
	 * @see {@link PatientDAO#getPatients(String,String,List<QPatientIdentifierType;>,null)}
	 */
	@Test
	@Verifies(value = "should escape percentage character in identifier phrase", method = "getPatients(String,String,List<QPatientIdentifierType;>,null)")
	public void getPatients_shouldEscapePercentageCharacterInIdentifierPhrase() throws Exception {
		
		Patient patient2 = pService.getPatient(2);
		PatientIdentifier patientIdentifier = new PatientIdentifier("\\%567", pService.getPatientIdentifierType(2), Context
		        .getLocationService().getLocation(1));
		patient2.addIdentifier(patientIdentifier);
		pService.savePatient(patient2);
		
		//add closely matching identifier to a different patient
		Patient patient6 = pService.getPatient(6);
		PatientIdentifier patientIdentifier6 = new PatientIdentifier("4567", pService.getPatientIdentifierType(2), Context
		        .getLocationService().getLocation(1));
		patient6.addIdentifier(patientIdentifier6);
		pService.savePatient(patient6);
		
		List<PatientIdentifierType> identifierTypes = Collections.emptyList();
		//we expect only one matching patient       
		int actualSize = dao.getPatients(null, "%567", identifierTypes, false).size();
		Assert.assertEquals(1, actualSize);
		
		//if actually the search returned the matching patient       
		Patient actualPatient = dao.getPatients(null, "%567", identifierTypes, false).get(0);
		
		Assert.assertEquals(patient2, actualPatient);
	}
	
	/**
	 * @see {@link PatientDAO#getPatients(String,String,List<QPatientIdentifierType;>,null)}
	 */
	@Test
	@Verifies(value = "should escape underscore character in identifier phrase", method = "getPatients(String,String,List<QPatientIdentifierType;>,null)")
	public void getPatients_shouldEscapeUnderscoreCharacterInIdentifierPhrase() throws Exception {
		
		Patient patient2 = pService.getPatient(2);
		PatientIdentifier patientIdentifier = new PatientIdentifier("\\_567", pService.getPatientIdentifierType(2), Context
		        .getLocationService().getLocation(1));
		patient2.addIdentifier(patientIdentifier);
		pService.savePatient(patient2);
		
		//add closely matching identifier to a different patient
		Patient patient6 = pService.getPatient(6);
		PatientIdentifier patientIdentifier6 = new PatientIdentifier("4567", pService.getPatientIdentifierType(2), Context
		        .getLocationService().getLocation(1));
		patient6.addIdentifier(patientIdentifier6);
		pService.savePatient(patient6);
		
		List<PatientIdentifierType> identifierTypes = Collections.emptyList();
		//we expect only one matching patient       
		int actualSize = dao.getPatients(null, "_567", identifierTypes, false).size();
		Assert.assertEquals(1, actualSize);
		
		//if actually the search returned the matching patient       
		Patient actualPatient = dao.getPatients(null, "_567", identifierTypes, false).get(0);
		
		Assert.assertEquals(patient2, actualPatient);
	}
	
	/**
	 * @see {@link PatientDAO#getPatients(String,String,List<QPatientIdentifierType;>,null)}
	 */
	@Test
	@Verifies(value = "should escape percentage character in name phrase", method = "getPatients(String,String,List<QPatientIdentifierType;>,null)")
	public void getPatients_shouldEscapePercentageCharacterInNamePhrase() throws Exception {
		
		Patient patient2 = pService.getPatient(2);
		PersonName name = new PersonName("\\%cats", "and", "dogs");
		patient2.addName(name);
		pService.savePatient(patient2);
		
		//add a new closely matching identifier to another patient
		Patient patient6 = pService.getPatient(6);
		PersonName name6 = new PersonName("acats", "and", "dogs");
		patient6.addName(name6);
		pService.savePatient(patient6);
		
		List<PatientIdentifierType> identifierTypes = Collections.emptyList();
		//we expect only one matching patient       
		int actualSize = dao.getPatients("%ca", null, identifierTypes, false).size();
		Assert.assertEquals(1, actualSize);
		
		Patient actualPatient = dao.getPatients("%ca", null, identifierTypes, false).get(0);
		//if actually the search returned the matching patient  
		Assert.assertEquals(patient2, actualPatient);
	}
	
	/**
	 * @see {@link PatientDAO#getPatients(String,String,List<QPatientIdentifierType;>,null)}
	 */
	@Test
	@Verifies(value = "should escape underscore character in name phrase", method = "getPatients(String,String,List<QPatientIdentifierType;>,null)")
	public void getPatients_shouldEscapeUnderscoreCharacterInNamePhrase() throws Exception {
		
		Patient patient2 = pService.getPatient(2);
		PersonName name = new PersonName("\\_cats", "and", "dogs");
		patient2.addName(name);
		pService.savePatient(patient2);
		
		//add a new closely matching name to another patient
		Patient patient6 = pService.getPatient(6);
		PersonName name6 = new PersonName("acats", "and", "dogs");
		patient6.addName(name6);
		pService.savePatient(patient6);
		
		List<PatientIdentifierType> identifierTypes = Collections.emptyList();
		//we expect only one matching patient       
		int actualSize = dao.getPatients("_ca", null, identifierTypes, false).size();
		Assert.assertEquals(1, actualSize);
		
		//if actually the search returned the matching patient       
		Patient actualPatient = dao.getPatients("_ca", null, identifierTypes, false).get(0);
		Assert.assertEquals(patient2, actualPatient);
		
	}
	
	/**
	 * @see {@link PatientDAO#getPatients(String,String,List<QPatientIdentifierType;>,null)}
	 */
	@Test
	@Verifies(value = "should escape an asterix character in name phrase", method = "getPatients(String,String,List<QPatientIdentifierType;>,null)")
	public void getPatients_shouldEscapeAnAsterixCharacterInNamePhrase() throws Exception {
		
		Patient patient2 = pService.getPatient(2);
		PersonName name = new PersonName("\\*cats", "and", "dogs");
		patient2.addName(name);
		pService.savePatient(patient2);
		
		//add a new closely matching name to another patient
		Patient patient6 = pService.getPatient(6);
		PersonName name6 = new PersonName("acats", "and", "dogs");
		patient6.addName(name6);
		pService.savePatient(patient6);
		
		List<PatientIdentifierType> identifierTypes = Collections.emptyList();
		//we expect only one matching patient       
		int actualSize = dao.getPatients("*ca", null, identifierTypes, false).size();
		Assert.assertEquals(1, actualSize);
		
		//if actually the search returned the matching patient       
		Patient actualPatient = dao.getPatients("*ca", null, identifierTypes, false).get(0);
		Assert.assertEquals(patient2, actualPatient);
	}
	
}
