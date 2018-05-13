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

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.openmrs.test.matchers.HasFieldErrors.hasFieldErrors;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.VisitAttribute;
import org.openmrs.api.APIException;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.util.GlobalPropertiesTestHelper;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

public class VisitValidatorTest extends BaseContextSensitiveTest {
	
	protected static final String DATA_XML = "org/openmrs/validator/include/VisitValidatorTest.xml";
	
	private GlobalPropertiesTestHelper globalPropertiesTestHelper;
	
	private VisitService visitService;
	
	private Calendar calendar;
	
	private static long DATE_TIME_2014_01_04_00_00_00_0 = 1388790000000L;
	
	private static long DATE_TIME_2014_02_05_00_00_00_0 = 1391554800000L;
	
	private static long DATE_TIME_2014_02_11_00_00_00_0 = 1392073200000L;
	
	@Before
	public void before() throws ParseException {
		executeDataSet(DATA_XML);
		visitService = Context.getVisitService();
		
		//The only reason for adding the four lines below is because without them,
		//some tests fail on my macbook.
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
		DATE_TIME_2014_01_04_00_00_00_0 = formatter.parse("2014/01/04").getTime();
		DATE_TIME_2014_02_05_00_00_00_0 = formatter.parse("2014/02/05").getTime();
		DATE_TIME_2014_02_11_00_00_00_0 = formatter.parse("2014/02/11").getTime();
		
		// Do not allow overlapping visits to test full validation of visit start and stop dates.
		//
		globalPropertiesTestHelper = new GlobalPropertiesTestHelper(Context.getAdministrationService());
		globalPropertiesTestHelper.setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_ALLOW_OVERLAPPING_VISITS, "false");
		
		calendar = Calendar.getInstance();
	}
	
	@After
	public void tearDown() {
		globalPropertiesTestHelper.setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_ALLOW_OVERLAPPING_VISITS, "true");
	}
	
	/**
	 * @see VisitValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldAcceptAVisitThatHasTheRightNumberOfAttributeOccurrences() {
		Visit visit = makeVisit();
		visit.addAttribute(makeAttribute("one"));
		visit.addAttribute(makeAttribute("two"));
		ValidateUtil.validate(visit);
	}
	
	/**
	 * @see VisitValidator#validate(Object,Errors)
	 */
	@Test(expected = APIException.class)
	public void validate_shouldRejectAVisitIfItHasFewerThanMinOccursOfAnAttribute() {
		Visit visit = makeVisit();
		visit.addAttribute(makeAttribute("one"));
		ValidateUtil.validate(visit);
	}
	
	/**
	 * @see VisitValidator#validate(Object,Errors)
	 */
	@Test(expected = APIException.class)
	public void validate_shouldRejectAVisitIfItHasMoreThanMaxOccursOfAnAttribute() {
		Visit visit = makeVisit();
		visit.addAttribute(makeAttribute("one"));
		visit.addAttribute(makeAttribute("two"));
		visit.addAttribute(makeAttribute("three"));
		visit.addAttribute(makeAttribute("four"));
		ValidateUtil.validate(visit);
	}
	
	private Visit makeVisit() {
		return makeVisit(2);
	}
	
	private Visit makeVisit(Integer patientId) {
		Visit visit = new Visit();
		visit.setPatient(Context.getPatientService().getPatient(patientId));
		visit.setStartDatetime(new Date());
		visit.setVisitType(visitService.getVisitType(1));
		return visit;
	}
	
	private VisitAttribute makeAttribute(Object typedValue) {
		VisitAttribute attr = new VisitAttribute();
		attr.setAttributeType(visitService.getVisitAttributeType(1));
		attr.setValue(typedValue);
		return attr;
	}
	
	/**
	 * @see VisitValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailIfPatientIsNotSet() {
		VisitService vs = Context.getVisitService();
		Visit visit = new Visit();
		visit.setVisitType(vs.getVisitType(1));
		visit.setStartDatetime(new Date());
		Errors errors = new BindException(visit, "visit");
		new VisitValidator().validate(visit, errors);
		assertTrue(errors.hasFieldErrors("patient"));
	}
	
	/**
	 * @see VisitValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailIfStartDatetimeIsNotSet() {
		VisitService vs = Context.getVisitService();
		Visit visit = new Visit();
		visit.setVisitType(vs.getVisitType(1));
		visit.setPatient(Context.getPatientService().getPatient(2));
		Errors errors = new BindException(visit, "visit");
		new VisitValidator().validate(visit, errors);
		assertTrue(errors.hasFieldErrors("startDatetime"));
	}
	
	/**
	 * @see VisitValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailIfVisitTypeIsNotSet() {
		Visit visit = new Visit();
		visit.setPatient(Context.getPatientService().getPatient(2));
		visit.setStartDatetime(new Date());
		Errors errors = new BindException(visit, "visit");
		new VisitValidator().validate(visit, errors);
		assertTrue(errors.hasFieldErrors("visitType"));
	}
	
	/**
	 * @see VisitValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailIfTheEndDatetimeIsBeforeTheStartDatetime() {
		Visit visit = new Visit();
		Calendar c = Calendar.getInstance();
		visit.setStartDatetime(c.getTime());
		c.set(2010, 3, 15);//set to an older date
		visit.setStopDatetime(c.getTime());
		Errors errors = new BindException(visit, "visit");
		new VisitValidator().validate(visit, errors);
		assertEquals(true, errors.hasFieldErrors("stopDatetime"));
	}
	
	/**
	 * @see VisitValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailIfTheStartDatetimeIsAfterAnyEncounter() {
		Visit visit = Context.getVisitService().getVisit(1);
		
		Encounter encounter = Context.getEncounterService().getEncounter(3);
		visit.setPatient(encounter.getPatient());
		encounter.setVisit(visit);
		encounter.setEncounterDatetime(visit.getStartDatetime());
		Context.getEncounterService().saveEncounter(encounter);
		
		//Set visit start date to after the encounter date.
		Date date = new Date(encounter.getEncounterDatetime().getTime() + 1);
		visit.setStartDatetime(date);
		
		Errors errors = new BindException(visit, "visit");
		new VisitValidator().validate(visit, errors);
		assertEquals(true, errors.hasFieldErrors("startDatetime"));
	}
	
	/**
	 * @see VisitValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailIfTheStopDatetimeIsBeforeAnyEncounter() {
		Visit visit = Context.getVisitService().getVisit(1);
		
		Encounter encounter = Context.getEncounterService().getEncounter(3);
		visit.setPatient(encounter.getPatient());
		encounter.setVisit(visit);
		encounter.setEncounterDatetime(visit.getStartDatetime());
		Context.getEncounterService().saveEncounter(encounter);
		
		//Set visit stop date to before the encounter date.
		Date date = new Date(encounter.getEncounterDatetime().getTime() - 1);
		visit.setStopDatetime(date);
		
		Errors errors = new BindException(visit, "visit");
		new VisitValidator().validate(visit, errors);
		assertEquals(true, errors.hasFieldErrors("stopDatetime"));
	}
	
	/**
	 * @see VisitValidator#validate(Object,Errors)
	 */
	@Test
	// This test will throw org.hibernate.PropertyValueException: not-null property references a null or transient value: org.openmrs.VisitAttribute.valueReference
	// This is a general problem, i.e. that validators on Customizable can't really be called unless you set Hibernate's flushMode to MANUAL.  
	// Once we figure it out, this test can be un-Ignored
	@Ignore
	public void validate_shouldFailIfAnAttributeIsBad() {
		Visit visit = visitService.getVisit(1);
		visit.addAttribute(makeAttribute(new Date()));
		visit.addAttribute(makeAttribute("not a date"));
		visit.getActiveAttributes();
		Errors errors = new BindException(visit, "visit");
		new VisitValidator().validate(visit, errors);
		assertEquals(true, errors.hasFieldErrors("attributes"));
	}
	
	/**
	 * @see VisitValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldRejectAVisitIfStartDateTimeIsEqualToStartDateTimeOfAnotherVisitOfTheSamePatient()
	        {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(DATE_TIME_2014_01_04_00_00_00_0);
		
		Visit visit = makeVisit(42);
		visit.setStartDatetime(calendar.getTime());
		
		Errors errors = new BindException(visit, "visit");
		new VisitValidator().validate(visit, errors);
		
		assertTrue(errors.hasFieldErrors("startDatetime"));
	}
	
	/**
	 * @see VisitValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldRejectAVisitIfStartDateTimeFallsIntoAnotherVisitOfTheSamePatient() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2014, Calendar.JANUARY, 6);
		
		Visit visit = makeVisit(42);
		visit.setStartDatetime(calendar.getTime());
		
		Errors errors = new BindException(visit, "visit");
		new VisitValidator().validate(visit, errors);
		
		assertTrue(errors.hasFieldErrors("startDatetime"));
	}
	
	/**
	 * @see VisitValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldRejectAVisitIfStopDateTimeFallsIntoAnotherVisitOfTheSamePatient() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2014, Calendar.JANUARY, 2);
		
		Visit visit = makeVisit(42);
		visit.setStartDatetime(calendar.getTime());
		
		calendar.set(2014, Calendar.JANUARY, 8);
		visit.setStopDatetime(calendar.getTime());
		
		Errors errors = new BindException(visit, "visit");
		new VisitValidator().validate(visit, errors);
		
		assertTrue(errors.hasFieldErrors("stopDatetime"));
	}
	
	/**
	 * @see VisitValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldRejectAVisitIfItContainsAnotherVisitOfTheSamePatient() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2014, Calendar.JANUARY, 2);
		
		Visit visit = makeVisit(42);
		visit.setStartDatetime(calendar.getTime());
		
		calendar.set(2014, Calendar.JANUARY, 12);
		visit.setStopDatetime(calendar.getTime());
		
		Errors errors = new BindException(visit, "visit");
		new VisitValidator().validate(visit, errors);
		
		assertTrue(errors.hasFieldErrors("stopDatetime"));
	}
	
	/**
	 * @see VisitValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldAcceptAVisitIfStartDateTimeIsEqualToStartDateTimeOfAnotherVoidedVisitOfTheSamePatient()
	        {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(DATE_TIME_2014_02_05_00_00_00_0);
		
		Visit visit = makeVisit(42);
		visit.setStartDatetime(calendar.getTime());
		
		assertTrue(patientHasVoidedVisit(visit.getPatient(), DATE_TIME_2014_02_05_00_00_00_0,
		    DATE_TIME_2014_02_11_00_00_00_0));
		
		Errors errors = new BindException(visit, "visit");
		new VisitValidator().validate(visit, errors);
		
		assertFalse(errors.hasFieldErrors("startDatetime"));
	}
	
	/**
	 * @see VisitValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldAcceptAVisitIfStartDateTimeFallsIntoAnotherVoidedVisitOfTheSamePatient() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2014, Calendar.FEBRUARY, 6);
		
		Visit visit = makeVisit(42);
		visit.setStartDatetime(calendar.getTime());
		
		assertTrue(patientHasVoidedVisit(visit.getPatient(), DATE_TIME_2014_02_05_00_00_00_0,
		    DATE_TIME_2014_02_11_00_00_00_0));
		
		Errors errors = new BindException(visit, "visit");
		new VisitValidator().validate(visit, errors);
		
		assertFalse(errors.hasFieldErrors("startDatetime"));
	}
	
	/**
	 * @see VisitValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldAcceptAVisitIfStopDateTimeFallsIntoAnotherVoidedVisitOfTheSamePatient() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2014, Calendar.FEBRUARY, 2);
		
		Visit visit = makeVisit(42);
		visit.setStartDatetime(calendar.getTime());
		
		calendar.set(2014, Calendar.FEBRUARY, 8);
		visit.setStopDatetime(calendar.getTime());
		
		assertTrue(patientHasVoidedVisit(visit.getPatient(), DATE_TIME_2014_02_05_00_00_00_0,
		    DATE_TIME_2014_02_11_00_00_00_0));
		
		Errors errors = new BindException(visit, "visit");
		new VisitValidator().validate(visit, errors);
		
		assertFalse(errors.hasFieldErrors("stopDatetime"));
	}
	
	/**
	 * @see VisitValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldAcceptAVisitIfItContainsAnotherVoidedVisitOfTheSamePatient() {
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(2014, Calendar.FEBRUARY, 2);
		
		Visit visit = makeVisit(42);
		visit.setStartDatetime(calendar.getTime());
		
		calendar.set(2014, Calendar.FEBRUARY, 12);
		visit.setStopDatetime(calendar.getTime());
		
		assertTrue(patientHasVoidedVisit(visit.getPatient(), DATE_TIME_2014_02_05_00_00_00_0,
		    DATE_TIME_2014_02_11_00_00_00_0));
		
		Errors errors = new BindException(visit, "visit");
		new VisitValidator().validate(visit, errors);
		
		assertFalse(errors.hasFieldErrors("stopDatetime"));
	}
	
	private boolean patientHasVoidedVisit(Patient patient, long startInMillis, long stopInMillis) {
		
		// To get voided visit from the past, both inactive AND voided visits are queried.
		//
		List<Visit> visitList = Context.getVisitService().getVisitsByPatient(patient, true, true);
		for (Visit visit : visitList) {
			if (visit.getStartDatetime() != null && visit.getStartDatetime().getTime() == startInMillis
			        && visit.getStopDatetime() != null && visit.getStopDatetime().getTime() == stopInMillis
			        && visit.getVoided()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @see VisitValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() {
		Visit visit = makeVisit(42);
		visit.setVoidReason("voidReason");
		
		Errors errors = new BindException(visit, "visit");
		new VisitValidator().validate(visit, errors);
		assertEquals(false, errors.hasFieldErrors("voidReason"));
	}
	
	/**
	 * @see VisitValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() {
		Visit visit = makeVisit(42);
		visit
		        .setVoidReason("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		
		Errors errors = new BindException(visit, "visit");
		new VisitValidator().validate(visit, errors);
		assertEquals(true, errors.hasFieldErrors("voidReason"));
	}
	
	@Test
	public void validate_shouldFailValidationIfVisitDateBeforeBirthDate() {
		Visit visit = new Visit();
		Patient patient = new Patient();
		calendar.set(1974, 4, 8);
		patient.setBirthdate(calendar.getTime());
		patient.setBirthdateEstimated(false);
		visit.setPatient(patient);
		calendar.set(1974, 4, 7);
		visit.setStartDatetime(calendar.getTime());
		Errors errors = new BindException(visit, "visit");
		
		new VisitValidator().validate(visit, errors);
		
		assertThat(errors, hasFieldErrors("startDatetime", "Visit.startDateCannotFallBeforeTheBirthDateOfTheSamePatient"));
	}
	
	@Test
	public void validate_shouldPassValidationIfVisitDateOnBirthDate() {
		Visit visit = new Visit();
		Patient patient = new Patient();
		calendar.set(1974, 4, 8);
		patient.setBirthdate(calendar.getTime());
		patient.setBirthdateEstimated(false);
		visit.setPatient(patient);
		calendar.set(1974, 4, 8);
		visit.setStartDatetime(calendar.getTime());
		Errors errors = new BindException(visit, "visit");
		
		new VisitValidator().validate(visit, errors);
		
		assertThat(errors, not(hasFieldErrors("startDatetime")));
	}
	
	@Test
	public void validate_shouldPassValidationIfVisitDateAfterBirthDate() {
		Visit visit = new Visit();
		Patient patient = new Patient();
		calendar.set(1974, 4, 8);
		patient.setBirthdate(calendar.getTime());
		patient.setBirthdateEstimated(false);
		visit.setPatient(patient);
		calendar.set(1974, 4, 9);
		visit.setStartDatetime(calendar.getTime());
		Errors errors = new BindException(visit, "visit");
		
		new VisitValidator().validate(visit, errors);
		
		assertThat(errors, not(hasFieldErrors("startDatetime")));
	}
	
	@Test
	public void validate_shouldPassValidationIfVisitDateOnEstimatedBirthDatesGracePeriod() {
		Visit visit = new Visit();
		Patient patient = new Patient();
		calendar.set(2000, 7, 25);
		patient.setBirthdate(calendar.getTime());
		patient.setBirthdateEstimated(true);
		calendar.set(2010, 7, 25);
		patient.setDeathDate(calendar.getTime());
		visit.setPatient(patient);
		calendar.set(1995, 7, 25);
		visit.setStartDatetime(calendar.getTime());
		Errors errors = new BindException(visit, "visit");
		
		new VisitValidator().validate(visit, errors);
		
		assertThat(errors, not(hasFieldErrors("startDatetime")));
	}
	
	@Test
	public void validate_shouldFailValidationIfVisitDateBeforeEstimatedBirthDatesGracePeriod() {
		Visit visit = new Visit();
		Patient patient = new Patient();
		calendar.set(2000, 7, 25);
		patient.setBirthdate(calendar.getTime());
		patient.setBirthdateEstimated(true);
		calendar.set(2010, 7, 25);
		patient.setDeathDate(calendar.getTime());
		visit.setPatient(patient);
		calendar.set(1995, 7, 24);
		visit.setStartDatetime(calendar.getTime());
		Errors errors = new BindException(visit, "visit");
		
		new VisitValidator().validate(visit, errors);
		
		assertThat(errors, hasFieldErrors("startDatetime", "Visit.startDateCannotFallBeforeTheBirthDateOfTheSamePatient"));
	}
	
	@Test
	public void validate_shouldPassValidationIfVisitDateOnEstimatedBirthDatesMinimumOneYearGracePeriod() {
		Visit visit = new Visit();
		Patient patient = new Patient();
		calendar.set(2000, 7, 25);
		patient.setBirthdate(calendar.getTime());
		patient.setBirthdateEstimated(true);
		calendar.set(2000, 8, 25);
		patient.setDeathDate(calendar.getTime());
		visit.setPatient(patient);
		calendar.set(1999, 7, 25);
		visit.setStartDatetime(calendar.getTime());
		Errors errors = new BindException(visit, "visit");
		
		new VisitValidator().validate(visit, errors);
		
		assertThat(errors, not(hasFieldErrors("startDatetime")));
	}
	
	@Test
	public void validate_shouldFailValidationIfVisitDateBeforeEstimatedBirthDatesMinimumOneYearGracePeriod() {
		Visit visit = new Visit();
		Patient patient = new Patient();
		calendar.set(2000, 7, 25);
		patient.setBirthdate(calendar.getTime());
		patient.setBirthdateEstimated(true);
		calendar.set(2000, 8, 25);
		patient.setDeathDate(calendar.getTime());
		visit.setPatient(patient);
		calendar.set(1999, 7, 24);
		visit.setStartDatetime(calendar.getTime());
		Errors errors = new BindException(visit, "visit");
		
		new VisitValidator().validate(visit, errors);
		
		assertThat(errors, hasFieldErrors("startDatetime", "Visit.startDateCannotFallBeforeTheBirthDateOfTheSamePatient"));
	}
}
