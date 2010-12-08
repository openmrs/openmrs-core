package org.openmrs.web.controller.patient;

import java.util.Calendar;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonName;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.test.Verifies;
import org.openmrs.web.test.BaseWebContextSensitiveTest;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

public class ShortPatientFormValidatorTest extends BaseWebContextSensitiveTest {
	
	ShortPatientFormValidator validator = null;
	
	PatientService ps = null;
	
	/**
	 * Run this before each unit test in this class.
	 * 
	 * @throws Exception
	 */
	@Before
	public void runBeforeAllTests() throws Exception {
		validator = new ShortPatientFormValidator();
		ps = Context.getPatientService();
	}
	
	/**
	 * @see {@link ShortPatientFormValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if all identifiers have been voided", method = "validate(Object,Errors)")
	public void validate_shouldFailIfAllIdentifiersHaveBeenVoided() throws Exception {
		Patient p = ps.getPatient(2);
		for (PatientIdentifier pId : p.getActiveIdentifiers())
			pId.setVoided(true);
		ShortPatientModel model = new ShortPatientModel(p);
		Errors errors = new BindException(model, "patientModel");
		validator.validate(model, errors);
		Assert.assertEquals(true, errors.hasGlobalErrors());
	}
	
	/**
	 * @see {@link ShortPatientFormValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if all name fields are empty or white space characters", method = "validate(Object,Errors)")
	public void validate_shouldFailIfAllNameFieldsAreEmptyOrWhiteSpaceCharacters() throws Exception {
		Patient p = ps.getPatient(2);
		p.getPersonName().setGivenName(" ");
		p.getPersonName().setFamilyName("");
		ShortPatientModel model = new ShortPatientModel(p);
		Errors errors = new BindException(model, "patientModel");
		validator.validate(model, errors);
		Assert.assertEquals(true, errors.hasGlobalErrors());
	}
	
	/**
	 * @see {@link ShortPatientFormValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if any name has more than 50 characters", method = "validate(Object,Errors)")
	public void validate_shouldFailIfAnyNameHasMoreThan50Characters() throws Exception {
		Patient p = ps.getPatient(2);
		p.getPersonName().setGivenName("ooooooooooooooooooooooooooooooooooooooooooooooooooo");
		ShortPatientModel model = new ShortPatientModel(p);
		Errors errors = new BindException(model, "patientModel");
		validator.validate(model, errors);
		Assert.assertEquals(true, errors.hasErrors());
	}
	
	/**
	 * @see {@link ShortPatientFormValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if no identifiers are added", method = "validate(Object,Errors)")
	public void validate_shouldFailIfNoIdentifiersAreAdded() throws Exception {
		Patient p = ps.getPatient(2);
		List<PatientIdentifier> activeIdentifiers = p.getActiveIdentifiers();
		Set<PatientIdentifier> patientIdentifiers = p.getIdentifiers();
		//remove all the active identifiers
		for (PatientIdentifier activeIdentifier : activeIdentifiers)
			patientIdentifiers.remove(activeIdentifier);
		
		p.setIdentifiers(patientIdentifiers);
		ShortPatientModel model = new ShortPatientModel(p);
		Errors errors = new BindException(model, "patientModel");
		validator.validate(model, errors);
		Assert.assertEquals(true, errors.hasGlobalErrors());
	}
	
	/**
	 * @see {@link ShortPatientFormValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if the deathdate is before the birthdate incase the patient is dead", method = "validate(Object,Errors)")
	public void validate_shouldFailIfTheDeathdateIsBeforeTheBirthdateIncaseThePatientIsDead() throws Exception {
		Patient p = ps.getPatient(2);
		p.setDead(true);
		p.setCauseOfDeath(new Concept(88));
		Calendar c = Calendar.getInstance();
		c.set(1975, 3, 7);
		p.setDeathDate(c.getTime());
		ShortPatientModel model = new ShortPatientModel(p);
		Errors errors = new BindException(model, "patientModel");
		validator.validate(model, errors);
		Assert.assertEquals(true, errors.hasFieldErrors());
	}
	
	/**
	 * @see {@link ShortPatientFormValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if birthdate is a future date", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfBirthdateIsAFutureDate() throws Exception {
		Patient p = ps.getPatient(2);
		Calendar c = Calendar.getInstance();
		// put the time into the future by a minute
		c.add(Calendar.MINUTE, 1);
		p.setBirthdate(c.getTime());
		ShortPatientModel model = new ShortPatientModel(p);
		Errors errors = new BindException(model, "patientModel");
		validator.validate(model, errors);
		Assert.assertEquals(true, errors.hasFieldErrors());
	}
	
	/**
	 * @see {@link ShortPatientFormValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if birthdate is blank", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfBirthdateIsBlank() throws Exception {
		Patient p = ps.getPatient(2);
		p.setBirthdate(null);
		ShortPatientModel model = new ShortPatientModel(p);
		Errors errors = new BindException(model, "patientModel");
		validator.validate(model, errors);
		Assert.assertEquals(true, errors.hasFieldErrors());
	}
	
	/**
	 * @see {@link ShortPatientFormValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if causeOfDeath is blank when patient is dead", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfCauseOfDeathIsBlankWhenPatientIsDead() throws Exception {
		Patient p = ps.getPatient(2);
		p.setDead(true);
		p.setCauseOfDeath(null);
		p.setDeathDate(Calendar.getInstance().getTime());
		ShortPatientModel model = new ShortPatientModel(p);
		Errors errors = new BindException(model, "patientModel");
		validator.validate(model, errors);
		Assert.assertEquals(true, errors.hasFieldErrors());
	}
	
	/**
	 * @see {@link ShortPatientFormValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if deathdate is a future date", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfDeathdateIsAFutureDate() throws Exception {
		Patient p = ps.getPatient(2);
		p.setDead(true);
		p.setCauseOfDeath(new Concept(88));
		Calendar c = Calendar.getInstance();
		// put the time into the future by a minute
		c.add(Calendar.MINUTE, 1);
		p.setDeathDate(c.getTime());
		ShortPatientModel model = new ShortPatientModel(p);
		Errors errors = new BindException(model, "patientModel");
		validator.validate(model, errors);
		Assert.assertEquals(true, errors.hasFieldErrors());
	}
	
	/**
	 * @see {@link ShortPatientFormValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if gender is blank", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfGenderIsBlank() throws Exception {
		Patient p = ps.getPatient(2);
		p.setGender("");
		ShortPatientModel model = new ShortPatientModel(p);
		Errors errors = new BindException(model, "patientModel");
		validator.validate(model, errors);
		Assert.assertEquals(true, errors.hasFieldErrors());
	}
	
	/**
	 * @see {@link ShortPatientFormValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass if the minimum required fields are provided and are valid", method = "validate(Object,Errors)")
	public void validate_shouldPassIfTheMinimumRequiredFieldsAreProvidedAndAreValid() throws Exception {
		Patient p = new Patient();
		p.setGender("M");
		Calendar c = Calendar.getInstance();
		c.set(1950, 3, 3);
		p.setBirthdate(c.getTime());
		p.addName(new PersonName("hor", null, "ty"));
		p.addIdentifier(new PatientIdentifier("hiuh", new PatientIdentifierType(2), new Location(1)));
		ShortPatientModel model = new ShortPatientModel(p);
		Errors errors = new BindException(model, "patientModel");
		validator.validate(model, errors);
		Assert.assertEquals(false, errors.hasErrors());
		
	}
	
	/**
	 * @see {@link ShortPatientFormValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if birthdate makes patient 120 years old or older", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfBirthdateMakesPatient120YearsOldOrOlder() throws Exception {
		Patient p = ps.getPatient(2);
		Calendar c = Calendar.getInstance();
		c.roll(Calendar.YEAR, -120);
		p.setBirthdate(c.getTime());
		ShortPatientModel model = new ShortPatientModel(p);
		Errors errors = new BindException(model, "patientModel");
		validator.validate(model, errors);
		Assert.assertEquals(true, errors.hasFieldErrors());
	}
}
