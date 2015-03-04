/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.patient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.test.Verifies;
import org.openmrs.util.LocationUtility;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.web.WebConstants;
import org.openmrs.web.test.BaseWebContextSensitiveTest;
import org.openmrs.web.test.WebTestHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

/**
 * Consists of unit tests for the ShortPatientFormController
 * 
 * @see ShortPatientFormController
 */
public class ShortPatientFormControllerTest extends BaseWebContextSensitiveTest {
	
	@Autowired
	WebTestHelper webTestHelper;
	
	/**
	 * @see {@link ShortPatientFormController#saveShortPatient(WebRequest,ShortPatientModel,BindingResult,SessionStatus)}
	 */
	@Test
	@Verifies(value = "should pass if all the form data is valid", method = "saveShortPatient(WebRequest,ShortPatientModel,BindingResult,SessionStatus)")
	public void saveShortPatient_shouldPassIfAllTheFormDataIsValid() throws Exception {
		Patient p = Context.getPatientService().getPatient(2);
		ShortPatientModel patientModel = new ShortPatientModel(p);
		
		WebRequest mockWebRequest = new ServletWebRequest(new MockHttpServletRequest());
		BindException errors = new BindException(patientModel, "patientModel");
		mockWebRequest.setAttribute("personNameCache", BeanUtils.cloneBean(p.getPersonName()), WebRequest.SCOPE_SESSION);
		mockWebRequest.setAttribute("personAddressCache", p.getPersonAddress().clone(), WebRequest.SCOPE_SESSION);
		mockWebRequest.setAttribute("patientModel", patientModel, WebRequest.SCOPE_SESSION);
		
		ShortPatientFormController controller = (ShortPatientFormController) applicationContext
		        .getBean("shortPatientFormController");
		String redirectUrl = controller.saveShortPatient(mockWebRequest, (PersonName) mockWebRequest.getAttribute(
		    "personNameCache", WebRequest.SCOPE_SESSION), (PersonAddress) mockWebRequest.getAttribute("personAddressCache",
		    WebRequest.SCOPE_SESSION), null, (ShortPatientModel) mockWebRequest.getAttribute("patientModel",
		    WebRequest.SCOPE_SESSION), errors);
		
		Assert.assertTrue("Should pass with no validation errors", !errors.hasErrors());
		Assert.assertEquals("Patient.saved", mockWebRequest.getAttribute(WebConstants.OPENMRS_MSG_ATTR,
		    WebRequest.SCOPE_SESSION));
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
		patientModel.setPersonAddress(new PersonAddress());
		
		WebRequest mockWebRequest = new ServletWebRequest(new MockHttpServletRequest());
		BindException errors = new BindException(patientModel, "patientModel");
		
		ShortPatientFormController controller = (ShortPatientFormController) applicationContext
		        .getBean("shortPatientFormController");
		String redirectUrl = controller.saveShortPatient(mockWebRequest, new PersonName(), new PersonAddress(), null,
		    patientModel, errors);
		
		Assert.assertTrue("Should pass with no validation errors", !errors.hasErrors());
		Assert.assertNotNull(p.getId());
		Assert.assertNotNull(p.getPersonName());
		Assert.assertNotNull(p.getPersonName().getId());// the name was create
		// and added
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
		patientModel.setPersonAddress(new PersonAddress());
		
		WebRequest mockWebRequest = new ServletWebRequest(new MockHttpServletRequest());
		BindException errors = new BindException(patientModel, "patientModel");
		
		ShortPatientFormController controller = (ShortPatientFormController) applicationContext
		        .getBean("shortPatientFormController");
		String formUrl = controller.saveShortPatient(mockWebRequest, new PersonName(), new PersonAddress(), null,
		    patientModel, errors);
		
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
		
		// edit the name and submit
		patientModel.getPersonName().setGivenName("Changed");
		
		ShortPatientFormController controller = (ShortPatientFormController) applicationContext
		        .getBean("shortPatientFormController");
		String redirectUrl = controller.saveShortPatient(mockWebRequest, personNameCache, (PersonAddress) p
		        .getPersonAddress().clone(), null, patientModel, errors);
		
		Assert.assertEquals(nameCount + 1, p.getNames().size());
		Assert.assertTrue("The old name should be voided", oldPersonName.isVoided());
		Assert.assertNotNull("The void reason should be set", oldPersonName.getVoidReason());
		Assert.assertTrue("The old name should have remained un changed", oldGivenName.equalsIgnoreCase(oldPersonName
		        .getGivenName()));
		Assert.assertEquals("Changed", p.getGivenName());// the changes should
		// have taken effect
		Assert.assertTrue("Should pass with no validation errors", !errors.hasErrors());
		Assert.assertEquals("redirect:/patientDashboard.form?patientId=" + p.getPatientId(), redirectUrl);
	}
	
	/**
	 * @see {@link ShortPatientFormController#saveShortPatient(WebRequest,ShortPatientModel,BindingResult,SessionStatus)}
	 */
	@Test
	@Verifies(value = "should add a new name if the person had no names", method = "saveShortPatient(WebRequest,ShortPatientModel,BindingResult,SessionStatus)")
	public void saveShortPatient_shouldAddANewNameIfThePersonHadNoNames() throws Exception {
		Patient p = Context.getPatientService().getPatient(7);
		
		//Commenting this out because person validator requires each person
		//to have at least one non voided name.
		/*p.getPersonName().setVoided(true);
		Context.getPatientService().savePatient(p);
		Assert.assertNull(p.getPersonName());// make sure all names are voided*/

		// add a name that will used as a duplicate for testing purposes
		PersonName newName = new PersonName("new", null, "name");
		newName.setDateCreated(new Date());
		ShortPatientModel patientModel = new ShortPatientModel(p);
		patientModel.setPersonName(newName);
		
		BindException errors = new BindException(patientModel, "patientModel");
		ServletWebRequest mockWebRequest = new ServletWebRequest(new MockHttpServletRequest());
		
		ShortPatientFormController controller = (ShortPatientFormController) applicationContext
		        .getBean("shortPatientFormController");
		String redirectUrl = controller.saveShortPatient(mockWebRequest, new PersonName(), (PersonAddress) p
		        .getPersonAddress().clone(), null, patientModel, errors);
		
		Assert.assertTrue("Should pass with no validation errors", !errors.hasErrors());
		Assert.assertEquals("redirect:/patientDashboard.form?patientId=" + p.getPatientId(), redirectUrl);
		Assert.assertNotNull(newName.getId());// name should have been added to
		// DB
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
		
		// edit address1 value and submit
		patientModel.getPersonAddress().setAddress1("Kampala");
		
		ShortPatientFormController controller = (ShortPatientFormController) applicationContext
		        .getBean("shortPatientFormController");
		String redirectUrl = controller.saveShortPatient(mockWebRequest,
		    (PersonName) BeanUtils.cloneBean(p.getPersonName()), personAddressCache, null, patientModel, errors);
		Assert.assertEquals(addressCount + 1, p.getAddresses().size());
		Assert.assertTrue("The old address should be voided", oldPersonAddress.isVoided());
		Assert.assertNotNull("The void reason should be set", oldPersonAddress.getVoidReason());
		Assert.assertTrue("The old address should have remained the same", oldAddress1.equalsIgnoreCase(oldPersonAddress
		        .getAddress1()));
		Assert.assertEquals("Kampala", p.getPersonAddress().getAddress1());// the
		// changes
		// should
		// have
		// taken
		// effect
		Assert.assertEquals("redirect:/patientDashboard.form?patientId=" + p.getPatientId(), redirectUrl);
	}
	
	/**
	 * @see {@link ShortPatientFormController#saveShortPatient(WebRequest,PersonName,ShortPatientModel,Map,BindingResult, SessionStatus)}
	 */
	@Test
	@Verifies(value = "should add a new address if the person had none", method = "saveShortPatient(WebRequest,PersonName,ShortPatientModel,BindingResult,SessionStatus)")
	public void saveShortPatient_shouldAddANewAddressIfThePersonHadNone() throws Exception {
		Patient p = Context.getPatientService().getPatient(2);
		p.getPersonAddress().setVoided(true);
		Context.getPatientService().savePatient(p);
		Assert.assertNull(p.getPersonAddress());// make sure all addresses are
		// voided
		
		// add a name that will used as a duplicate for testing purposes
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
		    (PersonName) BeanUtils.cloneBean(p.getPersonName()), new PersonAddress(), null, patientModel, errors);
		
		Assert.assertTrue("Should pass with no validation errors", !errors.hasErrors());
		Assert.assertEquals("redirect:/patientDashboard.form?patientId=" + p.getPatientId(), redirectUrl);
		Assert.assertNotNull(newAddress.getId());// name should have been added
		// to DB
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
		// make sure all addresses are voided so that whatever is entered a new
		// address
		Assert.assertNull(p.getPersonAddress());
		
		// add the new address
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
		    (PersonName) BeanUtils.cloneBean(p.getPersonName()), new PersonAddress(), null, patientModel, errors);
		
		Assert.assertTrue("Should pass with no validation errors", !errors.hasErrors());
		Assert.assertEquals("redirect:/patientDashboard.form?patientId=" + p.getPatientId(), redirectUrl);
		// address should have been ignored
		Assert.assertNull(p.getPersonAddress());
	}
	
	/**
	 * @see {@link ShortPatientFormController#saveShortPatient(WebRequest,PersonName,PersonAddress,Map,ShortPatientModel, BindingResult)}
	 */
	@Test
	@Verifies(value = "should add a new person attribute with a non empty value", method = "saveShortPatient(WebRequest,PersonName,PersonAddress,ShortPatientModel,BindingResult)")
	public void saveShortPatient_shouldAddANewPersonAttributeWithANonEmptyValue() throws Exception {
		Patient p = Context.getPatientService().getPatient(2);
		int originalAttributeCount = p.getAttributes().size();
		ShortPatientModel patientModel = new ShortPatientModel(p);
		int attributeTypeId = 2;
		String birthPlace = "Kampala";
		PersonAttribute newPersonAttribute = new PersonAttribute(Context.getPersonService().getPersonAttributeType(
		    attributeTypeId), birthPlace);
		newPersonAttribute.setDateCreated(new Date());
		newPersonAttribute.setCreator(Context.getAuthenticatedUser());
		patientModel.getPersonAttributes().add(newPersonAttribute);
		
		WebRequest mockWebRequest = new ServletWebRequest(new MockHttpServletRequest());
		BindException errors = new BindException(patientModel, "patientModel");
		mockWebRequest.setAttribute("personNameCache", p.getPersonName(), WebRequest.SCOPE_SESSION);
		mockWebRequest.setAttribute("personAddressCache", p.getPersonAddress(), WebRequest.SCOPE_SESSION);
		mockWebRequest.setAttribute("patientModel", patientModel, WebRequest.SCOPE_SESSION);
		
		ShortPatientFormController controller = (ShortPatientFormController) applicationContext
		        .getBean("shortPatientFormController");
		String redirectUrl = controller.saveShortPatient(mockWebRequest, (PersonName) mockWebRequest.getAttribute(
		    "personNameCache", WebRequest.SCOPE_SESSION), (PersonAddress) mockWebRequest.getAttribute("personAddressCache",
		    WebRequest.SCOPE_SESSION), null, (ShortPatientModel) mockWebRequest.getAttribute("patientModel",
		    WebRequest.SCOPE_SESSION), errors);
		Assert.assertTrue("Should pass with no validation errors", !errors.hasErrors());
		
		Assert.assertEquals("Patient.saved", mockWebRequest.getAttribute(WebConstants.OPENMRS_MSG_ATTR,
		    WebRequest.SCOPE_SESSION));
		Assert.assertEquals("redirect:/patientDashboard.form?patientId=" + p.getPatientId(), redirectUrl);
		//The new person attribute should have been added and saved
		Assert.assertNotNull(p.getAttribute(2).getPersonAttributeId());
		Assert.assertEquals(birthPlace, p.getAttribute(2).getValue());
		Assert.assertEquals(originalAttributeCount + 1, p.getAttributes().size());
	}
	
	/**
	 * @see {@link ShortPatientFormController#saveShortPatient(WebRequest,PersonName,PersonAddress,Map,ShortPatientModel, BindingResult)}
	 */
	@Test
	@Verifies(value = "should not add a new person attribute with an empty value", method = "saveShortPatient(WebRequest,PersonName,PersonAddress,ShortPatientModel,BindingResult)")
	public void saveShortPatient_shouldNotAddANewPersonAttributeWithAnEmptyValue() throws Exception {
		Patient p = Context.getPatientService().getPatient(2);
		int originalAttributeCount = p.getAttributes().size();
		ShortPatientModel patientModel = new ShortPatientModel(p);
		//add a new person Attribute with no value
		patientModel.getPersonAttributes().add(
		    new PersonAttribute(Context.getPersonService().getPersonAttributeType(2), null));
		
		WebRequest mockWebRequest = new ServletWebRequest(new MockHttpServletRequest());
		BindException errors = new BindException(patientModel, "patientModel");
		mockWebRequest.setAttribute("personNameCache", p.getPersonName(), WebRequest.SCOPE_SESSION);
		mockWebRequest.setAttribute("personAddressCache", p.getPersonAddress(), WebRequest.SCOPE_SESSION);
		mockWebRequest.setAttribute("patientModel", patientModel, WebRequest.SCOPE_SESSION);
		
		ShortPatientFormController controller = (ShortPatientFormController) applicationContext
		        .getBean("shortPatientFormController");
		String redirectUrl = controller.saveShortPatient(mockWebRequest, (PersonName) mockWebRequest.getAttribute(
		    "personNameCache", WebRequest.SCOPE_SESSION), (PersonAddress) mockWebRequest.getAttribute("personAddressCache",
		    WebRequest.SCOPE_SESSION), null, (ShortPatientModel) mockWebRequest.getAttribute("patientModel",
		    WebRequest.SCOPE_SESSION), errors);
		
		Assert.assertTrue("Should pass with no validation errors", !errors.hasErrors());
		Assert.assertEquals("Patient.saved", mockWebRequest.getAttribute(WebConstants.OPENMRS_MSG_ATTR,
		    WebRequest.SCOPE_SESSION));
		Assert.assertEquals("redirect:/patientDashboard.form?patientId=" + p.getPatientId(), redirectUrl);
		//The new blank person attribute should have been ignored
		Assert.assertEquals(originalAttributeCount, p.getAttributes().size());
	}
	
	/**
	 * @see {@link ShortPatientFormController#saveShortPatient(WebRequest,PersonName,PersonAddress,Map,ShortPatientModel, BindingResult)}
	 */
	@Test
	@Verifies(value = "should void an existing person attribute with an empty value", method = "saveShortPatient(WebRequest,PersonName,PersonAddress,ShortPatientModel,BindingResult)")
	public void saveShortPatient_shouldVoidAnExistingPersonAttributeWithAnEmptyValue() throws Exception {
		int attributeTypeId = 8;
		PersonAttributeType pat = Context.getPersonService().getPersonAttributeType(attributeTypeId);
		//For this test to pass we need to have some viewable attributes to be displayed on the form for editing
		AdministrationService as = Context.getAdministrationService();
		GlobalProperty gp = as.getGlobalPropertyObject(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_VIEWING_ATTRIBUTES);
		if (gp == null)
			gp = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_VIEWING_ATTRIBUTES);
		gp.setPropertyValue(pat.getName());
		as.saveGlobalProperty(gp);
		
		Patient p = Context.getPatientService().getPatient(2);
		int originalActiveAttributeCount = p.getActiveAttributes().size();
		
		ShortPatientModel patientModel = new ShortPatientModel(p);
		PersonAttribute attributeToEdit = null;
		String oldValue = null;
		String newValue = "";
		//assuming we are in the webapp on the form, find the attribute with the matching 
		// attribute type and change its value to an empty string
		for (PersonAttribute at : patientModel.getPersonAttributes()) {
			if (at.getAttributeType().equals(pat)) {
				oldValue = at.getValue();
				at.setValue(newValue);
				attributeToEdit = at;
				break;
			}
		}
		//ensure we found and edited it
		Assert.assertNotNull(attributeToEdit);
		Assert.assertNotNull(oldValue);
		
		WebRequest mockWebRequest = new ServletWebRequest(new MockHttpServletRequest());
		BindException errors = new BindException(patientModel, "patientModel");
		mockWebRequest.setAttribute("personNameCache", p.getPersonName(), WebRequest.SCOPE_SESSION);
		mockWebRequest.setAttribute("personAddressCache", p.getPersonAddress(), WebRequest.SCOPE_SESSION);
		mockWebRequest.setAttribute("patientModel", patientModel, WebRequest.SCOPE_SESSION);
		
		ShortPatientFormController controller = (ShortPatientFormController) applicationContext
		        .getBean("shortPatientFormController");
		controller.saveShortPatient(mockWebRequest, (PersonName) mockWebRequest.getAttribute("personNameCache",
		    WebRequest.SCOPE_SESSION), (PersonAddress) mockWebRequest.getAttribute("personAddressCache",
		    WebRequest.SCOPE_SESSION), null, (ShortPatientModel) mockWebRequest.getAttribute("patientModel",
		    WebRequest.SCOPE_SESSION), errors);
		
		Assert.assertTrue("Should pass with no validation errors", !errors.hasErrors());
		Assert.assertEquals("Patient.saved", mockWebRequest.getAttribute(WebConstants.OPENMRS_MSG_ATTR,
		    WebRequest.SCOPE_SESSION));
		
		//the attribute should have been voided
		Assert.assertEquals(originalActiveAttributeCount - 1, p.getActiveAttributes().size());
	}
	
	/**
	 * @see {@link ShortPatientFormController#saveShortPatient(WebRequest,PersonName,PersonAddress,Map,ShortPatientModel, BindingResult)}
	 */
	@Test
	@Verifies(value = "should should replace an existing attribute with a new one when edited", method = "saveShortPatient(WebRequest,PersonName,PersonAddress,ShortPatientModel,BindingResult)")
	public void saveShortPatient_shouldShouldReplaceAnExistingAttributeWithANewOneWhenEdited() throws Exception {
		int attributeTypeId = 2;
		PersonAttributeType pat = Context.getPersonService().getPersonAttributeType(attributeTypeId);
		//For this test to pass we need to have some viewable attributes to be displayed on the form for editing
		AdministrationService as = Context.getAdministrationService();
		GlobalProperty gp = as.getGlobalPropertyObject(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_VIEWING_ATTRIBUTES);
		if (gp == null)
			gp = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_VIEWING_ATTRIBUTES);
		gp.setPropertyValue(pat.getName());
		as.saveGlobalProperty(gp);
		
		Patient p = Context.getPatientService().getPatient(2);
		int originalAttributeCount = p.getAttributes().size();
		
		ShortPatientModel patientModel = new ShortPatientModel(p);
		PersonAttribute attributeToEdit = null;
		String oldValue = null;
		String newValue = "New";
		//assuming we are in the webapp on the form, find the attribute with the matching 
		// attribute type and change its value
		for (PersonAttribute at : patientModel.getPersonAttributes()) {
			if (at.getAttributeType().equals(pat)) {
				oldValue = at.getValue();
				at.setValue(newValue);
				attributeToEdit = at;
				break;
			}
		}
		//ensure we found and edited it
		Assert.assertNotNull(attributeToEdit);
		Assert.assertNotNull(oldValue);
		
		WebRequest mockWebRequest = new ServletWebRequest(new MockHttpServletRequest());
		BindException errors = new BindException(patientModel, "patientModel");
		mockWebRequest.setAttribute("personNameCache", p.getPersonName(), WebRequest.SCOPE_SESSION);
		mockWebRequest.setAttribute("personAddressCache", p.getPersonAddress(), WebRequest.SCOPE_SESSION);
		mockWebRequest.setAttribute("patientModel", patientModel, WebRequest.SCOPE_SESSION);
		
		ShortPatientFormController controller = (ShortPatientFormController) applicationContext
		        .getBean("shortPatientFormController");
		controller.saveShortPatient(mockWebRequest, (PersonName) mockWebRequest.getAttribute("personNameCache",
		    WebRequest.SCOPE_SESSION), (PersonAddress) mockWebRequest.getAttribute("personAddressCache",
		    WebRequest.SCOPE_SESSION), null, (ShortPatientModel) mockWebRequest.getAttribute("patientModel",
		    WebRequest.SCOPE_SESSION), errors);
		
		Assert.assertTrue("Should pass with no validation errors", !errors.hasErrors());
		Assert.assertEquals("Patient.saved", mockWebRequest.getAttribute(WebConstants.OPENMRS_MSG_ATTR,
		    WebRequest.SCOPE_SESSION));
		
		//a new replacement attribute should have been created with the new value
		PersonAttribute newAttribute = p.getAttribute(attributeTypeId);
		Assert.assertEquals(originalAttributeCount + 1, p.getAttributes().size());
		Assert.assertEquals(newValue, newAttribute.getValue());
		
		PersonAttribute oldAttribute = null;
		//find the voided attribute
		for (PersonAttribute at : p.getAttributes()) {
			//skip past the new one since it will be having a matching attribute type
			//and find exactly the attribute with the expected void reason
			if (at.getAttributeType().equals(pat)
			        && OpenmrsUtil.nullSafeEquals("New value: " + newValue, at.getVoidReason())) {
				oldAttribute = at;
				break;
			}
		}
		
		//The old attribute should have been voided and maintained its old value
		Assert.assertNotNull(oldAttribute);
		Assert.assertEquals(oldValue, oldAttribute.getValue());
		Assert.assertTrue(oldAttribute.isVoided());
	}
	
	/**
	 * @see ShortPatientFormController#saveShortPatient(WebRequest,PersonName,PersonAddress,Map,ShortPatientModel,
	 *      BindingResult)
	 * @verifies not void address if it was not changed
	 */
	@Test
	public void saveShortPatient_shouldNotVoidAddressIfItWasNotChanged() throws Exception {
		Patient patient = Context.getPatientService().getPatient(2);
		PersonAddress personAddress = patient.getPersonAddress();
		
		MockHttpServletRequest request = webTestHelper.newPOST("/admin/patients/shortPatientForm.form");
		request.setParameter("patientId", "2");
		request.setParameter("personAddress.address1", personAddress.getAddress1());
		request.setParameter("personAddress.countyDistrict", "");
		
		webTestHelper.handle(request);
		
		patient = Context.getPatientService().getPatient(2);
		assertEquals(1, patient.getAddresses().size());
		assertFalse(patient.getPersonAddress().isVoided());
		assertEquals(personAddress, patient.getPersonAddress());
	}
	
	/**
	 * @see ShortPatientFormController#saveShortPatient(WebRequest,PersonName,PersonAddress,Map,ShortPatientModel,
	 *      BindingResult)
	 * @verifies void address if it was changed
	 */
	@Test
	public void saveShortPatient_shouldVoidAddressIfItWasChanged() throws Exception {
		Patient patient = Context.getPatientService().getPatient(2);
		PersonAddress personAddress = patient.getPersonAddress();
		
		MockHttpServletRequest request = webTestHelper.newPOST("/admin/patients/shortPatientForm.form");
		request.setParameter("patientId", "2");
		request.setParameter("personAddress.address1", "new");
		request.setParameter("personAddress.countyDistrict", "");
		
		webTestHelper.handle(request);
		
		patient = Context.getPatientService().getPatient(2);
		assertEquals(2, patient.getAddresses().size());
		for (PersonAddress address : patient.getAddresses()) {
			if (address.getAddress1().equals("new")) {
				assertFalse(address.isVoided());
			} else {
				assertTrue(address.isVoided());
			}
		}
	}
}
