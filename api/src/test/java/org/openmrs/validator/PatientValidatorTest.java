/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.validator;

import java.util.Date;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.api.context.Context;
import org.openmrs.test.Verifies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Tests methods on the {@link PatientValidator} class.
 */
public class PatientValidatorTest extends PersonValidatorTest {
	
	@Autowired
	@Qualifier("patientValidator")
	@Override
	public void setValidator(Validator validator) {
		super.setValidator(validator);
	}
	
	/**
	 * @see {@link PatientValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if a preferred patient identifier is not chosen", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfAPreferredPatientIdentifierIsNotChosen() throws Exception {
		Patient pa = Context.getPatientService().getPatient(2);
		Assert.assertNotNull(pa.getPatientIdentifier());
		//set all identifiers to be non-preferred
		for (PatientIdentifier id : pa.getIdentifiers())
			id.setPreferred(false);
		
		Errors errors = new BindException(pa, "patient");
		validator.validate(pa, errors);
		Assert.assertTrue(errors.hasErrors());
	}
	
	/**
	 * @see {@link PatientValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if a preferred patient identifier is not chosen for voided patients", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfAPreferredPatientIdentifierIsNotChosenForVoidedPatients() throws Exception {
		Patient pa = Context.getPatientService().getPatient(432);
		
		Assert.assertTrue(pa.isVoided());//sanity check
		Assert.assertNotNull(pa.getPatientIdentifier());
		for (PatientIdentifier id : pa.getIdentifiers())
			id.setPreferred(false);
		
		Errors errors = new BindException(pa, "patient");
		validator.validate(pa, errors);
		Assert.assertTrue(errors.hasErrors());
	}
	
	@Test
	@Verifies(value = "should not fail validation if patient that is not preferred only has one identifier", method = "validate(Object,Errors)")
	public void validate_shouldNotFailWhenPatientHasOnlyOneIdentifierAndItsNotPreferred() throws Exception {
		PatientIdentifierType patientIdentifierType = Context.getPatientService().getAllPatientIdentifierTypes(false).get(0);
		Patient patient = new Patient();
		PersonName pName = new PersonName();
		pName.setGivenName("Tom");
		pName.setMiddleName("E.");
		pName.setFamilyName("Patient");
		patient.addName(pName);
		patient.setGender("male");
		PersonAddress pAddress = new PersonAddress();
		pAddress.setAddress1("123 My street");
		pAddress.setAddress2("Apt 402");
		pAddress.setCityVillage("Anywhere city");
		pAddress.setCountry("Some Country");
		Set<PersonAddress> pAddressList = patient.getAddresses();
		pAddressList.add(pAddress);
		patient.setAddresses(pAddressList);
		patient.addAddress(pAddress);
		PatientIdentifier patientIdentifier1 = new PatientIdentifier();
		patientIdentifier1.setLocation(new Location(1));
		patientIdentifier1.setIdentifier("012345678");
		patientIdentifier1.setDateCreated(new Date());
		patientIdentifier1.setIdentifierType(patientIdentifierType);
		patient.addIdentifier(patientIdentifier1);
		
		Errors errors = new BindException(patient, "patient");
		validator.validate(patient, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see {@link org.openmrs.validator.PatientValidator#validate(Object,Errors)}
	 * @verifies fail validation if gender is blank
	 */
	@Test
	@Verifies(value = "should fail validation if gender is blank", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfGenderIsBlank() throws Exception {
		Patient pa = new Patient(1);
		Errors errors = new BindException(pa, "patient");
		validator.validate(pa, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("gender"));
	}
	
	/**
	 * @see {@link PatientValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass validation if field lengths are correct", method = "validate(Object,Errors)")
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() throws Exception {
		PatientIdentifierType patientIdentifierType = Context.getPatientService().getAllPatientIdentifierTypes(false).get(0);
		Patient patient = new Patient();
		PersonName pName = new PersonName();
		pName.setGivenName("Tom");
		pName.setMiddleName("E.");
		pName.setFamilyName("Patient");
		patient.addName(pName);
		patient.setGender("male");
		PersonAddress pAddress = new PersonAddress();
		pAddress.setAddress1("123 My street");
		pAddress.setAddress2("Apt 402");
		pAddress.setCityVillage("Anywhere city");
		pAddress.setCountry("Some Country");
		Set<PersonAddress> pAddressList = patient.getAddresses();
		pAddressList.add(pAddress);
		patient.setAddresses(pAddressList);
		patient.addAddress(pAddress);
		PatientIdentifier patientIdentifier1 = new PatientIdentifier();
		patientIdentifier1.setLocation(new Location(1));
		patientIdentifier1.setIdentifier("012345678");
		patientIdentifier1.setDateCreated(new Date());
		patientIdentifier1.setIdentifierType(patientIdentifierType);
		patient.addIdentifier(patientIdentifier1);
		
		patient.setVoidReason("voidReason");
		
		Errors errors = new BindException(patient, "patient");
		validator.validate(patient, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see {@link PatientValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if field lengths are not correct", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() throws Exception {
		PatientIdentifierType patientIdentifierType = Context.getPatientService().getAllPatientIdentifierTypes(false).get(0);
		Patient patient = new Patient();
		PersonName pName = new PersonName();
		pName.setGivenName("Tom");
		pName.setMiddleName("E.");
		pName.setFamilyName("Patient");
		patient.addName(pName);
		patient.setGender("male");
		PersonAddress pAddress = new PersonAddress();
		pAddress.setAddress1("123 My street");
		pAddress.setAddress2("Apt 402");
		pAddress.setCityVillage("Anywhere city");
		pAddress.setCountry("Some Country");
		Set<PersonAddress> pAddressList = patient.getAddresses();
		pAddressList.add(pAddress);
		patient.setAddresses(pAddressList);
		patient.addAddress(pAddress);
		PatientIdentifier patientIdentifier1 = new PatientIdentifier();
		patientIdentifier1.setLocation(new Location(1));
		patientIdentifier1.setIdentifier("012345678");
		patientIdentifier1.setDateCreated(new Date());
		patientIdentifier1.setIdentifierType(patientIdentifierType);
		patient.addIdentifier(patientIdentifier1);
		
		patient
		        .setVoidReason("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		
		Errors errors = new BindException(patient, "patient");
		validator.validate(patient, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("voidReason"));
	}
}
