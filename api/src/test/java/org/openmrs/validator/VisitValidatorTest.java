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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
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
import org.openmrs.test.Verifies;
import org.openmrs.util.GlobalPropertiesTestHelper;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

public class VisitValidatorTest extends BaseContextSensitiveTest {
	
	protected static final String DATA_XML = "org/openmrs/validator/include/VisitValidatorTest.xml";
	
	private GlobalPropertiesTestHelper globalPropertiesTestHelper;
	
	private VisitService visitService;
	
	private static long DATE_TIME_2014_01_04_00_00_00_0 = 1388790000000L;
	
	private static long DATE_TIME_2014_02_05_00_00_00_0 = 1391554800000L;
	
	private static long DATE_TIME_2014_02_11_00_00_00_0 = 1392073200000L;
	
	@Before
	public void before() throws Exception {
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
	}
	
	@After
	public void tearDown() throws Exception {
		globalPropertiesTestHelper.setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_ALLOW_OVERLAPPING_VISITS, "true");
	}
	
	/**
	 * @see VisitValidator#validate(Object,Errors)
	 * @verifies accept a visit that has the right number of attribute occurrences
	 */
	@Test
	public void validate_shouldAcceptAVisitThatHasTheRightNumberOfAttributeOccurrences() throws Exception {
		Visit visit = makeVisit();
		visit.addAttribute(makeAttribute("one"));
		visit.addAttribute(makeAttribute("two"));
		ValidateUtil.validate(visit);
	}
	
	/**
	 * @see VisitValidator#validate(Object,Errors)
	 * @verifies reject a visit if it has fewer than min occurs of an attribute
	 */
	@Test(expected = APIException.class)
	public void validate_shouldRejectAVisitIfItHasFewerThanMinOccursOfAnAttribute() throws Exception {
		Visit visit = makeVisit();
		visit.addAttribute(makeAttribute("one"));
		ValidateUtil.validate(visit);
	}
	
	/**
	 * @see VisitValidator#validate(Object,Errors)
	 * @verifies reject a visit if it has more than max occurs of an attribute
	 */
	@Test(expected = APIException.class)
	public void validate_shouldRejectAVisitIfItHasMoreThanMaxOccursOfAnAttribute() throws Exception {
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
	 * @see {@link VisitValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if patient is not set", method = "validate(Object,Errors)")
	public void validate_shouldFailIfPatientIsNotSet() throws Exception {
		VisitService vs = Context.getVisitService();
		Visit visit = new Visit();
		visit.setVisitType(vs.getVisitType(1));
		visit.setStartDatetime(new Date());
		Errors errors = new BindException(visit, "visit");
		new VisitValidator().validate(visit, errors);
		assertTrue(errors.hasFieldErrors("patient"));
	}
	
	/**
	 * @see {@link VisitValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if startDatetime is not set", method = "validate(Object,Errors)")
	public void validate_shouldFailIfStartDatetimeIsNotSet() throws Exception {
		VisitService vs = Context.getVisitService();
		Visit visit = new Visit();
		visit.setVisitType(vs.getVisitType(1));
		visit.setPatient(Context.getPatientService().getPatient(2));
		Errors errors = new BindException(visit, "visit");
		new VisitValidator().validate(visit, errors);
		assertTrue(errors.hasFieldErrors("startDatetime"));
	}
	
	/**
	 * @see {@link VisitValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if visit type is not set", method = "validate(Object,Errors)")
	public void validate_shouldFailIfVisitTypeIsNotSet() throws Exception {
		Visit visit = new Visit();
		visit.setPatient(Context.getPatientService().getPatient(2));
		visit.setStartDatetime(new Date());
		Errors errors = new BindException(visit, "visit");
		new VisitValidator().validate(visit, errors);
		assertTrue(errors.hasFieldErrors("visitType"));
	}
	
	/**
	 * @see {@link VisitValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if the endDatetime is before the startDatetime", method = "validate(Object,Errors)")
	public void validate_shouldFailIfTheEndDatetimeIsBeforeTheStartDatetime() throws Exception {
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
	 * @see {@link VisitValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if the startDatetime is after any encounter", method = "validate(Object,Errors)")
	public void validate_shouldFailIfTheStartDatetimeIsAfterAnyEncounter() throws Exception {
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
	 * @see {@link VisitValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if the stopDatetime is before any encounter", method = "validate(Object,Errors)")
	public void validate_shouldFailIfTheStopDatetimeIsBeforeAnyEncounter() throws Exception {
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
	 * @verifies fail if an attribute is bad
	 */
	@Test
	// This test will throw org.hibernate.PropertyValueException: not-null property references a null or transient value: org.openmrs.VisitAttribute.valueReference
	// This is a general problem, i.e. that validators on Customizable can't really be called unless you set Hibernate's flushMode to MANUAL.  
	// Once we figure it out, this test can be un-Ignored
	@Ignore
	public void validate_shouldFailIfAnAttributeIsBad() throws Exception {
		Visit visit = visitService.getVisit(1);
		visit.addAttribute(makeAttribute(new Date()));
		visit.addAttribute(makeAttribute("not a date"));
		Collection<VisitAttribute> activeAttributes = visit.getActiveAttributes();
		Errors errors = new BindException(visit, "visit");
		new VisitValidator().validate(visit, errors);
		assertEquals(true, errors.hasFieldErrors("attributes"));
	}
	
	/**
	 * @verifies reject a visit if startDateTime is equal to startDateTime of another visit of the same patient
	 * @see VisitValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldRejectAVisitIfStartDateTimeIsEqualToStartDateTimeOfAnotherVisitOfTheSamePatient()
	        throws Exception {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(DATE_TIME_2014_01_04_00_00_00_0);
		
		Visit visit = makeVisit(42);
		visit.setStartDatetime(calendar.getTime());
		
		Errors errors = new BindException(visit, "visit");
		new VisitValidator().validate(visit, errors);
		
		assertTrue(errors.hasFieldErrors("startDatetime"));
	}
	
	/**
	 * @verifies reject a visit if startDateTime falls into another visit of the same patient
	 * @see VisitValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldRejectAVisitIfStartDateTimeFallsIntoAnotherVisitOfTheSamePatient() throws Exception {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2014, Calendar.JANUARY, 6);
		
		Visit visit = makeVisit(42);
		visit.setStartDatetime(calendar.getTime());
		
		Errors errors = new BindException(visit, "visit");
		new VisitValidator().validate(visit, errors);
		
		assertTrue(errors.hasFieldErrors("startDatetime"));
	}
	
	/**
	 * @verifies reject a visit if stopDateTime falls into another visit of the same patient
	 * @see VisitValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldRejectAVisitIfStopDateTimeFallsIntoAnotherVisitOfTheSamePatient() throws Exception {
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
	 * @verifies reject a visit if it contains another visit of the same patient
	 * @see VisitValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldRejectAVisitIfItContainsAnotherVisitOfTheSamePatient() throws Exception {
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
	 * @verifies accept a visit if startDateTime is equal to startDateTime of another voided visit of the same patient
	 * @see VisitValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldAcceptAVisitIfStartDateTimeIsEqualToStartDateTimeOfAnotherVoidedVisitOfTheSamePatient()
	        throws Exception {
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
	 * @verifies accept a visit if startDateTime falls into another voided visit of the same patient
	 * @see VisitValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldAcceptAVisitIfStartDateTimeFallsIntoAnotherVoidedVisitOfTheSamePatient() throws Exception {
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
	 * @verifies accept a visit if stopDateTime falls into another voided visit of the same patient
	 * @see VisitValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldAcceptAVisitIfStopDateTimeFallsIntoAnotherVoidedVisitOfTheSamePatient() throws Exception {
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
	 * @verifies accept a visit if it contains another voided visit of the same patient
	 * @see VisitValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldAcceptAVisitIfItContainsAnotherVoidedVisitOfTheSamePatient() throws Exception {
		
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
			        && visit.isVoided()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @see {@link VisitValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass validation if field lengths are correct", method = "validate(Object,Errors)")
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() throws Exception {
		Visit visit = makeVisit(42);
		visit.setVoidReason("voidReason");
		
		Errors errors = new BindException(visit, "visit");
		new VisitValidator().validate(visit, errors);
		assertEquals(false, errors.hasFieldErrors("voidReason"));
	}
	
	/**
	 * @see {@link VisitValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if field lengths are not correct", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() throws Exception {
		Visit visit = makeVisit(42);
		visit
		        .setVoidReason("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		
		Errors errors = new BindException(visit, "visit");
		new VisitValidator().validate(visit, errors);
		assertEquals(true, errors.hasFieldErrors("voidReason"));
	}
}
