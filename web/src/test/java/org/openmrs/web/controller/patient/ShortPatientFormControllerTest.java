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
package org.openmrs.web.controller.patient;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.MapUtils;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.api.context.Context;
import org.openmrs.test.Verifies;
import org.openmrs.util.LocationUtility;
import org.openmrs.web.WebConstants;
import org.openmrs.web.test.BaseWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.bind.support.SimpleSessionStatus;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

/**
 * Consists of unit tests for the ShortPatientFormController
 * 
 * @see ShortPatientFormController
 */
public class ShortPatientFormControllerTest extends BaseWebContextSensitiveTest {
	
	/**
	 * @see {@link ShortPatientFormController#saveShortPatient(WebRequest,ShortPatientModel,BindingResult,SessionStatus)}
	 */
	@Test
	@Verifies(value = "should pass if all the form data is valid", method = "saveShortPatient(WebRequest,ShortPatientModel,BindingResult,SessionStatus)")
	public void saveShortPatient_shouldPassIfAllTheFormDataIsValid() throws Exception {
		Patient p = Context.getPatientService().getPatient(2);
		ShortPatientModel patientModel = new ShortPatientModel(p);
		
		WebRequest mockWebRequest = new ServletWebRequest(new MockHttpServletRequest());
		SimpleSessionStatus status = new SimpleSessionStatus();
		BindException errors = new BindException(patientModel, "patientModel");
		mockWebRequest.setAttribute("personNameCache", BeanUtils.cloneBean(p.getPersonName()), WebRequest.SCOPE_SESSION);
		mockWebRequest.setAttribute("personAddressCache", p.getPersonAddress().clone(), WebRequest.SCOPE_SESSION);
		mockWebRequest.setAttribute("patientModel", patientModel, WebRequest.SCOPE_SESSION);
		
		ShortPatientFormController controller = (ShortPatientFormController) applicationContext
		        .getBean("shortPatientFormController");
		String redirectUrl = controller.saveShortPatient(mockWebRequest, (PersonName) mockWebRequest.getAttribute(
		    "personNameCache", WebRequest.SCOPE_SESSION), (PersonAddress) mockWebRequest.getAttribute("personAddressCache",
		    WebRequest.SCOPE_SESSION), (ShortPatientModel) mockWebRequest.getAttribute("patientModel",
		    WebRequest.SCOPE_SESSION), errors, status);
		
		Assert.assertTrue("Should pass with no validation errors", !errors.hasErrors());
		Assert.assertEquals("Patient.saved", mockWebRequest.getAttribute(WebConstants.OPENMRS_MSG_ATTR,
		    WebRequest.SCOPE_SESSION));
		Assert.assertTrue("Should set the status to complete", status.isComplete());
		Assert.assertEquals("redirect:/patientDashboard.form?patientId=" + p.getPatientId(), redirectUrl);
	}
	
	/**
	 * @see {@link ShortPatientFormController#saveShortPatient(WebRequest,ShortPatientModel,BindingResult,SessionStatus)}
	 */
	@Test
	@Verifies(value = "should create a new patient", method = "saveShortPatient(WebRequest,ShortPatientModel,BindingResult,SessionStatus)")
	public void saveShortPatient_shouldCreateANewPatient() throws Exception {
		int patientCount = Context.getPatientService().getAllPatients().size();
		Patient p = new Patient();
		ShortPatientModel patientModel = new ShortPatientModel(p);
		patientModel.setPersonName(new PersonName("new", "", "patient"));
		List<PatientIdentifier> identifiers = new ArrayList<PatientIdentifier>();
		PatientIdentifier id = new PatientIdentifier("myID", Context.getPatientService().getPatientIdentifierType(2),
		        LocationUtility.getDefaultLocation());
		id.setPreferred(true);
		identifiers.add(id);
		patientModel.setIdentifiers(identifiers);
		patientModel.getPatient().setBirthdate(new Date());
		patientModel.getPatient().setGender("M");
		
		WebRequest mockWebRequest = new ServletWebRequest(new MockHttpServletRequest());
		BindException errors = new BindException(patientModel, "patientModel");
		
		ShortPatientFormController controller = (ShortPatientFormController) applicationContext
		        .getBean("shortPatientFormController");
		String redirectUrl = controller.saveShortPatient(mockWebRequest, new PersonName(), new PersonAddress(),
		    patientModel, errors, new SimpleSessionStatus());
		
		Assert.assertTrue("Should pass with no validation errors", !errors.hasErrors());
		Assert.assertNotNull(p.getId());
		Assert.assertNotNull(p.getPersonName());
		Assert.assertNotNull(p.getPersonName().getId());//the name was create and added
		Assert.assertEquals(patientCount + 1, Context.getPatientService().getAllPatients().size());
		Assert.assertEquals("Patient.saved", mockWebRequest.getAttribute(WebConstants.OPENMRS_MSG_ATTR,
		    WebRequest.SCOPE_SESSION));
		Assert.assertEquals("redirect:/patientDashboard.form?patientId=" + p.getPatientId(), redirectUrl);
	}
	
	/**
	 * @see {@link ShortPatientFormController#saveShortPatient(WebRequest,ShortPatientModel,BindingResult,SessionStatus)}
	 */
	@Test
	@Verifies(value = "should send the user back to the form in case of validation errors", method = "saveShortPatient(WebRequest,ShortPatientModel,BindingResult,SessionStatus)")
	public void saveShortPatient_shouldSendTheUserBackToTheFormInCaseOfValidationErrors() throws Exception {
		Patient p = new Patient();
		ShortPatientModel patientModel = new ShortPatientModel(p);
		patientModel.setPersonName(new PersonName("new", "", "patient"));
		List<PatientIdentifier> identifiers = new ArrayList<PatientIdentifier>();
		patientModel.setIdentifiers(identifiers);
		
		WebRequest mockWebRequest = new ServletWebRequest(new MockHttpServletRequest());
		BindException errors = new BindException(patientModel, "patientModel");
		
		ShortPatientFormController controller = (ShortPatientFormController) applicationContext
		        .getBean("shortPatientFormController");
		String formUrl = controller.saveShortPatient(mockWebRequest, new PersonName(), new PersonAddress(), patientModel,
		    errors, new SimpleSessionStatus());
		
		Assert.assertTrue("Should report validation errors", errors.hasErrors());
		Assert.assertEquals("/admin/patients/shortPatientForm", formUrl);
	}
	
	/**
	 * @see {@link ShortPatientFormController#saveShortPatient(WebRequest,ShortPatientModel,BindingResult,SessionStatus)}
	 */
	@Test
	@Verifies(value = "should void a name and replace it with a new one if it is changed to a unique value", method = "saveShortPatient(WebRequest,ShortPatientModel,BindingResult,SessionStatus)")
	public void saveShortPatient_shouldVoidANameAndReplaceItWithANewOneIfItIsChangedToAUniqueValue() throws Exception {
		Patient p = Context.getPatientService().getPatient(2);
		ShortPatientModel patientModel = new ShortPatientModel(p);
		PersonName oldPersonName = p.getPersonName();
		String oldGivenName = oldPersonName.getGivenName();
		int nameCount = p.getNames().size();
		
		BindException errors = new BindException(patientModel, "patientModel");
		ServletWebRequest mockWebRequest = new ServletWebRequest(new MockHttpServletRequest());
		PersonName personNameCache = (PersonName) BeanUtils.cloneBean(p.getPersonName());
		
		//edit the name and submit
		patientModel.getPersonName().setGivenName("Changed");
		
		ShortPatientFormController controller = (ShortPatientFormController) applicationContext
		        .getBean("shortPatientFormController");
		String redirectUrl = controller.saveShortPatient(mockWebRequest, personNameCache, (PersonAddress) p
		        .getPersonAddress().clone(), patientModel, errors, new SimpleSessionStatus());
		
		Assert.assertEquals(nameCount + 1, p.getNames().size());
		Assert.assertTrue("The old name should be voided", oldPersonName.isVoided());
		Assert.assertNotNull("The void reason should be set", oldPersonName.getVoidReason());
		Assert.assertTrue("The old name should have remained un changed", oldGivenName.equalsIgnoreCase(oldPersonName
		        .getGivenName()));
		Assert.assertEquals("Changed", p.getGivenName());//the changes should have taken effect
		Assert.assertTrue("Should pass with no validation errors", !errors.hasErrors());
		Assert.assertEquals("redirect:/patientDashboard.form?patientId=" + p.getPatientId(), redirectUrl);
	}
	
	/**
	 * @see {@link ShortPatientFormController#showForm(Integer,ModelMap,WebRequest)}
	 */
	@Test
	@Verifies(value = "should redirect to the shortPatientForm", method = "showForm(Integer,ModelMap,WebRequest)")
	public void showForm_shouldRedirectToTheShortPatientForm() throws Exception {
		ServletWebRequest mockWebRequest = new ServletWebRequest(new MockHttpServletRequest());
		
		ShortPatientFormController controller = (ShortPatientFormController) applicationContext
		        .getBean("shortPatientFormController");
		ModelMap model = new ModelMap();
		String redirectUrl = controller.showForm(2, model, mockWebRequest);
		Assert.assertTrue(MapUtils.isNotEmpty(model));
		Assert.assertEquals("/admin/patients/shortPatientForm", redirectUrl);
	}
	
	/**
	 * @see {@link ShortPatientFormController#saveShortPatient(WebRequest,ShortPatientModel,BindingResult,SessionStatus)}
	 */
	@Test
	@Verifies(value = "should add a new name if the person had no names", method = "saveShortPatient(WebRequest,ShortPatientModel,BindingResult,SessionStatus)")
	public void saveShortPatient_shouldAddANewNameIfThePersonHadNoNames() throws Exception {
		Patient p = Context.getPatientService().getPatient(2);
		p.getPersonName().setVoided(true);
		Context.getPatientService().savePatient(p);
		Assert.assertNull(p.getPersonName());//make sure all names are voided
		
		//add a name that will used as a duplicate for testing purposes			
		PersonName newName = new PersonName("new", null, "name");
		newName.setDateCreated(new Date());
		ShortPatientModel patientModel = new ShortPatientModel(p);
		patientModel.setPersonName(newName);
		
		BindException errors = new BindException(patientModel, "patientModel");
		ServletWebRequest mockWebRequest = new ServletWebRequest(new MockHttpServletRequest());
		
		ShortPatientFormController controller = (ShortPatientFormController) applicationContext
		        .getBean("shortPatientFormController");
		String redirectUrl = controller.saveShortPatient(mockWebRequest, new PersonName(), (PersonAddress) p
		        .getPersonAddress().clone(), patientModel, errors, new SimpleSessionStatus());
		
		Assert.assertTrue("Should pass with no validation errors", !errors.hasErrors());
		Assert.assertEquals("redirect:/patientDashboard.form?patientId=" + p.getPatientId(), redirectUrl);
		Assert.assertNotNull(newName.getId());//name should have been added to DB
	}
	
	/**
	 * @see {@link ShortPatientFormController#saveShortPatient(WebRequest,ShortPatientModel,BindingResult,SessionStatus)}
	 */
	@Test
	@Verifies(value = "should void an address and replace it with a new one if it is changed to a unique value", method = "saveShortPatient(WebRequest,ShortPatientModel,BindingResult,SessionStatus)")
	public void saveShortPatient_shouldVoidAnAddressAndReplaceItWithANewOneIfItIsChangedToAUniqueValue() throws Exception {
		Patient p = Context.getPatientService().getPatient(2);
		ShortPatientModel patientModel = new ShortPatientModel(p);
		PersonAddress oldPersonAddress = patientModel.getPersonAddress();
		String oldAddress1 = oldPersonAddress.getAddress1();
		int addressCount = p.getAddresses().size();
		
		BindException errors = new BindException(patientModel, "patientModel");
		ServletWebRequest mockWebRequest = new ServletWebRequest(new MockHttpServletRequest());
		PersonAddress personAddressCache = (PersonAddress) p.getPersonAddress().clone();
		
		//edit address1	value and submit
		patientModel.getPersonAddress().setAddress1("Kampala");
		
		ShortPatientFormController controller = (ShortPatientFormController) applicationContext
		        .getBean("shortPatientFormController");
		String redirectUrl = controller.saveShortPatient(mockWebRequest,
		    (PersonName) BeanUtils.cloneBean(p.getPersonName()), personAddressCache, patientModel, errors,
		    new SimpleSessionStatus());
		Assert.assertEquals(addressCount + 1, p.getAddresses().size());
		Assert.assertTrue("The old address should be voided", oldPersonAddress.isVoided());
		Assert.assertNotNull("The void reason should be set", oldPersonAddress.getVoidReason());
		Assert.assertTrue("The old address should have remained the same", oldAddress1.equalsIgnoreCase(oldPersonAddress
		        .getAddress1()));
		Assert.assertEquals("Kampala", p.getPersonAddress().getAddress1());//the changes should have taken effect
		Assert.assertEquals("redirect:/patientDashboard.form?patientId=" + p.getPatientId(), redirectUrl);
	}
	
	/**
	 * @see {@link ShortPatientFormController#saveShortPatient(WebRequest,PersonName,ShortPatientModel,BindingResult,SessionStatus)}
	 */
	@Test
	@Verifies(value = "should add a new address if the person had none", method = "saveShortPatient(WebRequest,PersonName,ShortPatientModel,BindingResult,SessionStatus)")
	public void saveShortPatient_shouldAddANewAddressIfThePersonHadNone() throws Exception {
		Patient p = Context.getPatientService().getPatient(2);
		p.getPersonAddress().setVoided(true);
		Context.getPatientService().savePatient(p);
		Assert.assertNull(p.getPersonAddress());//make sure all addresses are voided
		
		//add a name that will used as a duplicate for testing purposes			
		PersonAddress newAddress = new PersonAddress();
		newAddress.setAddress1("Kampala");
		newAddress.setDateCreated(new Date());
		ShortPatientModel patientModel = new ShortPatientModel(p);
		patientModel.setPersonAddress(newAddress);
		
		BindException errors = new BindException(patientModel, "patientModel");
		ServletWebRequest mockWebRequest = new ServletWebRequest(new MockHttpServletRequest());
		
		ShortPatientFormController controller = (ShortPatientFormController) applicationContext
		        .getBean("shortPatientFormController");
		String redirectUrl = controller.saveShortPatient(mockWebRequest,
		    (PersonName) BeanUtils.cloneBean(p.getPersonName()), new PersonAddress(), patientModel, errors,
		    new SimpleSessionStatus());
		
		Assert.assertTrue("Should pass with no validation errors", !errors.hasErrors());
		Assert.assertEquals("redirect:/patientDashboard.form?patientId=" + p.getPatientId(), redirectUrl);
		Assert.assertNotNull(newAddress.getId());//name should have been added to DB
	}
	
	/**
	 * @see {@link ShortPatientFormController#saveShortPatient(WebRequest,PersonName,PersonAddress,ShortPatientModel,BindingResult,SessionStatus)}
	 */
	@Test
	@Verifies(value = "should ignore a new address that was added and voided at same time", method = "saveShortPatient(WebRequest,PersonName,PersonAddress,ShortPatientModel,BindingResult,SessionStatus)")
	public void saveShortPatient_shouldIgnoreANewAddressThatWasAddedAndVoidedAtSameTime() throws Exception {
		Patient p = Context.getPatientService().getPatient(2);
		p.getPersonAddress().setVoided(true);
		Context.getPatientService().savePatient(p);
		//make sure all addresses are voided so that whatever is entered a new address
		Assert.assertNull(p.getPersonAddress());
		
		//add the new address	
		PersonAddress newAddress = new PersonAddress();
		newAddress.setAddress1("Kampala");
		newAddress.setDateCreated(new Date());
		newAddress.setVoided(true);
		ShortPatientModel patientModel = new ShortPatientModel(p);
		patientModel.setPersonAddress(newAddress);
		
		BindException errors = new BindException(patientModel, "patientModel");
		ServletWebRequest mockWebRequest = new ServletWebRequest(new MockHttpServletRequest());
		
		ShortPatientFormController controller = (ShortPatientFormController) applicationContext
		        .getBean("shortPatientFormController");
		String redirectUrl = controller.saveShortPatient(mockWebRequest,
		    (PersonName) BeanUtils.cloneBean(p.getPersonName()), new PersonAddress(), patientModel, errors,
		    new SimpleSessionStatus());
		
		Assert.assertTrue("Should pass with no validation errors", !errors.hasErrors());
		Assert.assertEquals("redirect:/patientDashboard.form?patientId=" + p.getPatientId(), redirectUrl);
		Assert.assertNull(p.getPersonAddress());//address should have been ignored
	}
}
