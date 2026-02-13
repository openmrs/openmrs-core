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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.api.context.Context;
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
	 * @see PatientValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfAPreferredPatientIdentifierIsNotChosen() {
		Patient pa = Context.getPatientService().getPatient(2);
		assertNotNull(pa.getPatientIdentifier());
		//set all identifiers to be non-preferred
		for (PatientIdentifier id : pa.getIdentifiers())
			id.setPreferred(false);
		
		Errors errors = new BindException(pa, "patient");
		validator.validate(pa, errors);
		assertTrue(errors.hasErrors());
	}
	
	/**
	 * @see PatientValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfAPreferredPatientIdentifierIsNotChosenForVoidedPatients() {
		Patient pa = Context.getPatientService().getPatient(432);
		
		assertTrue(pa.getVoided());//sanity check
		assertNotNull(pa.getPatientIdentifier());
		for (PatientIdentifier id : pa.getIdentifiers())
			id.setPreferred(false);
		
		Errors errors = new BindException(pa, "patient");
		validator.validate(pa, errors);
		assertTrue(errors.hasErrors());
	}
	
	@Test
	public void validate_shouldNotFailWhenPatientHasOnlyOneIdentifierAndItsNotPreferred() {
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
		
		assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see org.openmrs.validator.PatientValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfGenderIsBlank() {
		Patient pa = new Patient(1);
		Errors errors = new BindException(pa, "patient");
		validator.validate(pa, errors);
		
		assertTrue(errors.hasFieldErrors("gender"));
	}
	
	/**
	 * @see PatientValidator#validate(Object,Errors)
	 */
	@Override
	@Test
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() {
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
		
		assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see PatientValidator#validate(Object,Errors)
	 */
	@Override
	@Test
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() {
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
		
		assertTrue(errors.hasFieldErrors("voidReason"));
	}

	/**
	* @see PatientValidator#validate(Object,Errors)
 	*/
	@Test
	public void validate_shouldFailIfRequiredIdentifierIsMissing() {
    	PatientIdentifierType patientIdentifierType = Context.getPatientService().getAllPatientIdentifierTypes(false).get(0);
    	patientIdentifierType.setRequired(true);
    	patientIdentifierType.setRetired(false);
    	Context.getPatientService().savePatientIdentifierType(patientIdentifierType);

    	Patient patient = new Patient();

    	PersonName pName = new PersonName();
   		pName.setGivenName("Tom");
    	pName.setMiddleName("E.");
    	pName.setFamilyName("Patient");
    	patient.addName(pName);
		patient.setGender("male");
		patient.setBirthdate(new Date());

    	PersonAddress pAddress = new PersonAddress();
    	pAddress.setAddress1("123 My street");
    	pAddress.setAddress2("Apt 402");
    	pAddress.setCityVillage("Anywhere city");
    	pAddress.setCountry("Some Country");
   	 	patient.addAddress(pAddress);

    	Errors errors = new BindException(patient, "patient");
    	validator.validate(patient, errors);

    	assertTrue(errors.hasFieldErrors("identifiers"));
		assertEquals(
   "Patient.missingRequiredIdentifier",
    		errors.getFieldError("identifiers").getCode()
		);
	}

	/**
	 * @see PatientValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassIfRequiredIdentifierIsPresent() {
    	PatientIdentifierType patientIdentifierType = Context.getPatientService().getAllPatientIdentifierTypes(false).get(0);
    	patientIdentifierType.setRequired(true);
    	patientIdentifierType.setRetired(false);
    	Context.getPatientService().savePatientIdentifierType(patientIdentifierType);

    	Patient patient = new Patient();

    	PersonName pName = new PersonName();
    	pName.setGivenName("Tom");
    	pName.setMiddleName("E.");
    	pName.setFamilyName("Patient");
    	patient.addName(pName);
		patient.setGender("male");
		patient.setBirthdate(new Date());

    	PersonAddress pAddress = new PersonAddress();
    	pAddress.setAddress1("123 My street");
    	pAddress.setAddress2("Apt 402");
    	pAddress.setCityVillage("Anywhere city");
    	pAddress.setCountry("Some Country");
    	patient.addAddress(pAddress);

    	PatientIdentifier patientIdentifier = new PatientIdentifier();
    	patientIdentifier.setLocation(new Location(1));
    	patientIdentifier.setIdentifier("012345678");
    	patientIdentifier.setDateCreated(new Date());
    	patientIdentifier.setIdentifierType(patientIdentifierType);
    	patientIdentifier.setPreferred(true);
    	patient.addIdentifier(patientIdentifier);

    	Errors errors = new BindException(patient, "patient");
    	validator.validate(patient, errors);

   		assertFalse(errors.hasErrors());
	}

	/**
 	* @see PatientValidator#validate(Object,Errors)
 	*/
	@Test
	public void validate_shouldIgnoreRetiredRequiredIdentifierTypes() {
		for (PatientIdentifierType pit : Context.getPatientService().getAllPatientIdentifierTypes(false)) {
        	pit.setRequired(false);
        	pit.setRetired(false);
        	Context.getPatientService().savePatientIdentifierType(pit);
    	}

    	PatientIdentifierType patientIdentifierType = Context.getPatientService().getAllPatientIdentifierTypes(false).get(0);
    	patientIdentifierType.setRequired(true);
    	patientIdentifierType.setRetired(true);
    	Context.getPatientService().savePatientIdentifierType(patientIdentifierType);

    	Patient patient = new Patient();

    	PersonName pName = new PersonName();
    	pName.setGivenName("Tom");
    	pName.setMiddleName("E.");
    	pName.setFamilyName("Patient");
    	patient.addName(pName);
		patient.setGender("male");
		patient.setBirthdate(new Date());

    	PersonAddress pAddress = new PersonAddress();
    	pAddress.setAddress1("123 My street");
   	 	pAddress.setAddress2("Apt 402");
    	pAddress.setCityVillage("Anywhere city");
    	pAddress.setCountry("Some Country");
    	patient.addAddress(pAddress);

		Errors errors = new BindException(patient, "patient");
    	validator.validate(patient, errors);

		assertFalse(errors.hasFieldErrors("identifiers"));
	}

	/**
 	* @see PatientValidator#validate(Object,Errors)
	*/
	@Test
	public void validate_shouldFailIfRequiredIdentifierIsVoided() {
    	PatientIdentifierType patientIdentifierType = Context.getPatientService().getAllPatientIdentifierTypes(false).get(0);
    	patientIdentifierType.setRequired(true);
    	patientIdentifierType.setRetired(false);
    	Context.getPatientService().savePatientIdentifierType(patientIdentifierType);

    	Patient patient = new Patient();

    	PersonName pName = new PersonName();
    	pName.setGivenName("Tom");
    	pName.setMiddleName("E.");
    	pName.setFamilyName("Patient");
    	patient.addName(pName);
		patient.setGender("male");
		patient.setBirthdate(new Date());

    	PersonAddress pAddress = new PersonAddress();
    	pAddress.setAddress1("123 My street");
    	pAddress.setAddress2("Apt 402");
    	pAddress.setCityVillage("Anywhere city");
    	pAddress.setCountry("Some Country");
    	patient.addAddress(pAddress);

    	PatientIdentifier patientIdentifier = new PatientIdentifier();
    	patientIdentifier.setLocation(new Location(1));
    	patientIdentifier.setIdentifier("012345678");
   		patientIdentifier.setDateCreated(new Date());
    	patientIdentifier.setIdentifierType(patientIdentifierType);
    	patientIdentifier.setVoided(true);
    	patient.addIdentifier(patientIdentifier);

    	Errors errors = new BindException(patient, "patient");
    	validator.validate(patient, errors);

    	assertTrue(errors.hasFieldErrors("identifiers"));
		assertEquals(
   "Patient.missingRequiredIdentifier",
    		errors.getFieldError("identifiers").getCode()
		);
	}

	/**
 	* @see PatientValidator#validate(Object,Errors)
 	*/
	@Test
	public void validate_shouldPassWithRequiredAndNonRequiredIdentifiers() {

    	List<PatientIdentifierType> types = Context.getPatientService().getAllPatientIdentifierTypes(false);

    	PatientIdentifierType requiredType = types.get(0);
    	requiredType.setRequired(true);
    	requiredType.setRetired(false);
    	Context.getPatientService().savePatientIdentifierType(requiredType);

    	PatientIdentifierType optionalType = types.get(1);
    	optionalType.setRequired(false);
    	optionalType.setRetired(false);
    	Context.getPatientService().savePatientIdentifierType(optionalType);

    	Patient patient = new Patient();
    	patient.addName(new PersonName("Tom", "", "Patient"));
   		patient.setGender("male");
    	patient.setBirthdate(new Date());

    	PatientIdentifier requiredIdentifier = new PatientIdentifier();
    	requiredIdentifier.setIdentifier("REQ123");
    	requiredIdentifier.setIdentifierType(requiredType);
    	requiredIdentifier.setLocation(new Location(1));
    	patient.addIdentifier(requiredIdentifier);

    	PatientIdentifier optionalIdentifier = new PatientIdentifier();
    	optionalIdentifier.setIdentifier("OPT456");
    	optionalIdentifier.setIdentifierType(optionalType);
    	optionalIdentifier.setLocation(new Location(1));
    	patient.addIdentifier(optionalIdentifier);

    	Errors errors = new BindException(patient, "patient");
    	validator.validate(patient, errors);

    	assertFalse(errors.hasFieldErrors("identifiers"));
	}

	/**
 	* @see PatientValidator#validate(Object,Errors)
	*/
	@Test
	public void validate_shouldPassIfVoidedRequiredIdentifierReplaced() {

    	PatientIdentifierType type = Context.getPatientService().getAllPatientIdentifierTypes(false).get(0);

    	type.setRequired(true);
    	type.setRetired(false);
    	Context.getPatientService().savePatientIdentifierType(type);

    	Patient patient = new Patient();
    	patient.addName(new PersonName("Tom", "", "Patient"));
    	patient.setGender("male");
    	patient.setBirthdate(new Date());

    	PatientIdentifier oldIdentifier = new PatientIdentifier();
    	oldIdentifier.setIdentifier("OLD123");
    	oldIdentifier.setIdentifierType(type);
    	oldIdentifier.setLocation(new Location(1));
    	oldIdentifier.setVoided(true);
    	patient.addIdentifier(oldIdentifier);

    	PatientIdentifier newIdentifier = new PatientIdentifier();
    	newIdentifier.setIdentifier("NEW456");
    	newIdentifier.setIdentifierType(type);
    	newIdentifier.setLocation(new Location(1));
    	patient.addIdentifier(newIdentifier);

   		Errors errors = new BindException(patient, "patient");
    	validator.validate(patient, errors);

    	assertFalse(errors.hasFieldErrors("identifiers"));
	}

	/**
 	* @see PatientValidator#validate(Object,Errors)
 	*/

	@Test
	public void validate_shouldNotFailForVoidedPatientWithoutRequiredIdentifier() {

    	PatientIdentifierType type = Context.getPatientService().getAllPatientIdentifierTypes(false).get(0);

    	type.setRequired(true);
    	type.setRetired(false);
    	Context.getPatientService().savePatientIdentifierType(type);

    	Patient patient = new Patient();
    	patient.setVoided(true);
    	patient.addName(new PersonName("Tom", "", "Patient"));
    	patient.setGender("male");
    	patient.setBirthdate(new Date());

    	Errors errors = new BindException(patient, "patient");
    	validator.validate(patient, errors);

    	assertFalse(errors.hasFieldErrors("identifiers"));
	}
}
