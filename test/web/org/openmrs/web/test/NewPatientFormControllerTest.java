/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.web.test;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService.ATTR_VIEW_TYPE;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.util.OpenmrsConstants.PERSON_TYPE;
import org.openmrs.web.controller.patient.NewPatientFormController;
import org.openmrs.web.controller.patient.ShortPatientModel;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Test the methods on the org.openmrs.web.controller.encounter.EncounterDisplayController
 */
public class NewPatientFormControllerTest extends BaseContextSensitiveTest {

	protected static final String CONTROLLER_DATA = "org/openmrs/web/test/include/NewPatientFormControllerTest.xml";
	protected static final String CONTROLLER_PATIENTS_DATA = "org/openmrs/web/test/include/NewPatientFormControllerTest-patients.xml";
	
	@Override
	protected void onSetUpInTransaction() throws Exception {
		initializeInMemoryDatabase();
		executeDataSet(CONTROLLER_DATA);
		authenticate();
	}
	
	/**
	 * This test just loads the page without giving it any parameters.
	 * This should load the page just fine without errors (and the user
	 * would see empty input boxes all around).
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked" })
    public void testPageLoadWithEmptyParameters() throws Exception {
		NewPatientFormController controller = new NewPatientFormController();
		MockHttpServletRequest request = new MockHttpServletRequest();
		HttpServletResponse response = new MockHttpServletResponse();
		
		request.setMethod("GET");
		ModelAndView modelAndView = controller.handleRequest(request, response);
		
		Map<String, Object> model = (Map<String, Object>)modelAndView.getModel().get("model");
		
	}
	
	/**
	 * This test will fill in all the required fields on the 
	 * form to test a successful submission
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked" })
    public void testSubmitWithAllFieldsEnteredCorrectly() throws Exception {
		
		PatientService ps = Context.getPatientService();
		
		// make sure we don't have any John's to begin with
		List<Patient> patients = ps.getPatients("John");
		assertTrue(patients.isEmpty());
		
		NewPatientFormController controller = new NewPatientFormController();
		controller.setApplicationContext(getApplicationContext());
		controller.setSuccessView("patientDashboard.form");
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		HttpServletResponse response = new MockHttpServletResponse();
		
		// set this to be a page submission
		request.setMethod("POST");
		
		// add all of the parameters that are expected
		request.addParameter("name.givenName", "John");
		request.addParameter("name.familyName", "Doe");
		request.addParameter("identifier", "123");
		request.addParameter("identifierType", "1");
		request.addParameter("location", "1");
		request.addParameter("preferred", "1");
		request.addParameter("gender", "M");
		request.addParameter("birthdate", "05/05/1959");
		request.addParameter("birthdateEstimated", "0");
		request.addParameter("tribe", "");
		request.addParameter("address.address1", "1234 Somewhere Street");
		
		// all person attribute types in the viewing list are required
		request.addParameter("1", "");
		
		// send the parameters to the controller
		ModelAndView modelAndView = controller.handleRequest(request, response);
		
		// make sure it is redirecting to the right place after a successful submit
		assertEquals(RedirectView.class, modelAndView.getView().getClass());
		RedirectView redirectView = (RedirectView)modelAndView.getView();
		assertTrue(redirectView.getUrl().startsWith("patientDashboard.form"));
		
		// make sure a John was created
		patients = ps.getPatients("John");
		assertFalse(patients.isEmpty());
	}
	
	/**
	 * This test submits a page without an identifier.  It should not bomb out
	 * and it should display properly again
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked" })
    public void testSubmitWithNoIdentifierFilledIn() throws Exception {
		
		PatientService ps = Context.getPatientService();
		
		// make sure we don't have any John's to begin with
		List<Patient> patients = ps.getPatients("Jane");
		assertTrue(patients.isEmpty());
		
		NewPatientFormController controller = new NewPatientFormController();
		controller.setApplicationContext(getApplicationContext());
		controller.setSuccessView("patientDashboard.form");
		controller.setFormView("newPatient.form");
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		HttpServletResponse response = new MockHttpServletResponse();
		
		// set this to be a page submission
		request.setMethod("POST");
		
		// add all of the parameters that are expected
		request.addParameter("name.givenName", "Jane");
		request.addParameter("name.familyName", "Doe");
		request.addParameter("identifier", "");
		request.addParameter("identifierType", "");
		request.addParameter("location", "");
		request.addParameter("preferred", "1");
		request.addParameter("gender", "F");
		request.addParameter("birthdate", "05/05/1959");
		request.addParameter("birthdateEstimated", "0");
		request.addParameter("tribe", "");
		request.addParameter("address.address1", "1234 Somewhere Street");
		
		// the phone number attribute
		request.addParameter("1", "1234");
		
		// send the parameters to the controller
		ModelAndView modelAndView = controller.handleRequest(request, response);
		
		// make sure it is redirecting to the right place after an unsuccessful submit
		assertEquals("newPatient.form", modelAndView.getViewName());
		
		// make sure a John was not created
		patients = ps.getPatients("Jane");
		assertTrue(patients.isEmpty());
		
		Map<String, Object> model = modelAndView.getModel();
		
		// get the formbacking object
		ShortPatientModel shortPatient = (ShortPatientModel)model.get(controller.getCommandName());
		assertNotNull(shortPatient);
		
		// make sure the formbackingobject for the redirected form has all the data
		// that was input earlier up above
		assertNotNull(shortPatient.getName());
		assertEquals("Jane", shortPatient.getName().getGivenName());
		assertEquals("Doe", shortPatient.getName().getFamilyName());
		assertEquals("F", shortPatient.getGender());
		assertEquals("1234 Somewhere Street", shortPatient.getAddress().getAddress1());
		
		assertEquals("1234", shortPatient.getAttributeMap().get("Phone Number").getValue());
	}
	
	/**
	 * This test changes just the location of an identifier.
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked" })
    public void testSubmitChangedIdentifierLocation() throws Exception {
		executeDataSet(CONTROLLER_PATIENTS_DATA);
		
		PatientService ps = Context.getPatientService();
		
		// make sure we don't have any John's to begin with
		List<Patient> patients = ps.getPatients("John");
		assertFalse(patients.isEmpty());
		
		
		NewPatientFormController controller = new NewPatientFormController();
		controller.setApplicationContext(getApplicationContext());
		controller.setSuccessView("patientDashboard.form");
		controller.setFormView("newPatient.form");
		controller.setSessionForm(true);
		
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin/patients/newPatient.form?patientId=2");
		request.setSession(new MockHttpSession(null));
		HttpServletResponse response = new MockHttpServletResponse();
		ModelAndView initialPageLoad = controller.handleRequest(request, response);
		
		// set this to be a page submission
		request.setMethod("POST");
		
		// add all of the parameters that are expected
		// all but the location should match the patient info in the xml file
		request.addParameter("patientId", "2");
		request.addParameter("name.givenName", "John");
		request.addParameter("name.familyName", "Doe");
		request.addParameter("identifier", "1234");
		request.addParameter("identifierType", "1");
		request.addParameter("location", "2");
		request.addParameter("preferred", "1");
		request.addParameter("gender", "F");
		request.addParameter("birthdate", "05/05/1959");
		request.addParameter("birthdateEstimated", "0");
		request.addParameter("tribe", "");
		request.addParameter("address.address1", "1234 Somewhere Street");
		
		// the phone number attribute
		request.addParameter("1", "1234");
		Context.getPersonService().getPersonAttributeTypes(PERSON_TYPE.PATIENT, ATTR_VIEW_TYPE.VIEWING);
		
		// send the parameters to the controller
		ModelAndView modelAndView = controller.handleRequest(request, response);
		
		// make sure it is redirecting to the right place after a successful submit
		assertEquals(RedirectView.class, modelAndView.getView().getClass());
		RedirectView redirectView = (RedirectView)modelAndView.getView();
		assertTrue(redirectView.getUrl().startsWith("patientDashboard.form"));
		
		// make sure John's identifier location was modified
		Patient patient = ps.getPatient(2);
		assertNotNull(patient);
		assertNotNull(patient.getIdentifiers());
		assertFalse(patient.getIdentifiers().isEmpty());
		PatientIdentifier identifier = (PatientIdentifier)patient.getIdentifiers().toArray()[0]; 
		assertEquals(2, identifier.getLocation().getLocationId().intValue());
		
	}
}
