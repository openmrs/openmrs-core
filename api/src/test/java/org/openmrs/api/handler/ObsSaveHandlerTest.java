/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.handler;

import java.util.Calendar;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.ObsReferenceRange;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.validator.ValidateUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the {@link ObsSaveHandler} class.
 */
public class ObsSaveHandlerTest extends BaseContextSensitiveTest {

	private final ObsSaveHandler obsSaveHandler = new ObsSaveHandler();

	Calendar calendar = Calendar.getInstance();

	/**
	 * @see ObsSaveHandler#handle(Obs,User,Date,String)
	 */
	@Test
	public void handle_shouldSetObsReferenceRangeIfCriteriaMatches() {
		Person person = new Person(1);
		calendar.add(Calendar.YEAR, -6);
		person.setBirthdate(calendar.getTime());

		Obs obs = new Obs();
		obs.setPerson(person);
		obs.setConcept(Context.getConceptService().getConcept(4090));
		obs.setValueNumeric(88.0);
		obs.setObsDatetime(new Date());

		obsSaveHandler.handle(obs, null, new Date(), null);

		assertNotNull(obs.getReferenceRange());
		assertEquals(140.0, obs.getReferenceRange().getHiAbsolute());
	}

	/**
	 * @see ObsSaveHandler#handle(Obs,User,Date,String)
	 */
	@Test
	public void handle_shouldSetObsReferenceRangeValuesIfConceptReferenceRangeIsNullAndConceptNumericIsNotNull() {
		Person person = new Person(1);
		calendar.add(Calendar.YEAR, -600);
		person.setBirthdate(calendar.getTime());

		Obs obs = new Obs();
		obs.setPerson(person);
		obs.setConcept(Context.getConceptService().getConcept(4090));
		obs.setValueNumeric(88.0);
		obs.setObsDatetime(new Date());

		obsSaveHandler.handle(obs, null, new Date(), null);

		assertNotNull(obs.getReferenceRange());
		assertEquals(145.0, obs.getReferenceRange().getHiAbsolute());
		assertEquals(70.0, obs.getReferenceRange().getLowAbsolute());
	}

	/**
	 * @see ObsSaveHandler#handle(Obs,User,Date,String)
	 */
	@Test
	public void handle_shouldSetObsReferenceRangeValuesToNarrowestMatchingValues() {
		// we assume there are two rules that will match a person of this age
		Person person = new Person(1);
		calendar.add(Calendar.YEAR, -6);
		person.setBirthdate(calendar.getTime());

		Obs obs = new Obs();
		obs.setPerson(person);
		obs.setConcept(Context.getConceptService().getConcept(4090));
		obs.setValueNumeric(88.0);
		obs.setObsDatetime(new Date());

		obsSaveHandler.handle(obs, null, new Date(), null);

		assertNotNull(obs.getReferenceRange());
		assertEquals(140.0, obs.getReferenceRange().getHiAbsolute());
		assertEquals(130.0, obs.getReferenceRange().getHiCritical());
		assertEquals(118.0, obs.getReferenceRange().getHiNormal());
		assertEquals(80.0, obs.getReferenceRange().getLowNormal());
		assertEquals(75.0, obs.getReferenceRange().getLowCritical());
		assertEquals(70.0, obs.getReferenceRange().getLowAbsolute());
	}

	/**
	 * @see ObsSaveHandler#handle(Obs,User,Date,String)
	 */
	@Test
	public void handle_shouldSetObsReferenceRangeValuesToConceptReferenceRangeValuesIfNoRuleBasedRangesArePresent() {
		Person person = new Person(1);
		calendar.add(Calendar.YEAR, -600);
		person.setBirthdate(calendar.getTime());

		Obs obs = new Obs();
		obs.setPerson(person);
		obs.setConcept(Context.getConceptService().getConcept(5497));
		obs.setValueNumeric(88.0);
		obs.setObsDatetime(new Date());

		obsSaveHandler.handle(obs, null, new Date(), null);

		assertNotNull(obs.getReferenceRange());
		assertEquals(2500.0, obs.getReferenceRange().getHiAbsolute());
		assertEquals(1800.0, obs.getReferenceRange().getHiCritical());
		assertEquals(1497.0, obs.getReferenceRange().getHiNormal());
		assertEquals(445.0, obs.getReferenceRange().getLowNormal());
		assertEquals(99.0, obs.getReferenceRange().getLowCritical());
		assertEquals(0.0, obs.getReferenceRange().getLowAbsolute());
	}

	/**
	 * @see ObsSaveHandler#handle(Obs,User,Date,String)
	 */
	@Test
	public void handle_shouldNotSetObsReferenceRangeValueIfConceptIsNotFound() {
		Person person = new Person(1);
		calendar.add(Calendar.YEAR, -6);
		person.setBirthdate(calendar.getTime());

		Obs obs = new Obs();
		obs.setPerson(person);
		obs.setConcept(Context.getConceptService().getConcept(409000));
		obs.setValueNumeric(8.0);
		obs.setObsDatetime(new Date());

		obsSaveHandler.handle(obs, null, new Date(), null);

		assertNull(obs.getReferenceRange());
	}

	/**
	 * @see ObsSaveHandler#handle(Obs,User,Date,String)
	 */
	@Test
	public void handle_shouldNotSetObsReferenceRangeForAVoidedObs() {
		Obs obs = getObs(60, 4090, 100.0);
		obs.setVoided(true);

		obsSaveHandler.handle(obs, null, new Date(), null);

		assertNull(obs.getReferenceRange());
		assertNull(obs.getInterpretation());
	}

	/**
	 * @see ObsSaveHandler#handle(Obs,User,Date,String)
	 */
	@Test
	public void handle_shouldSetInterpretationToHighIfObsValueIsAboveHiNormalAndLessThanHighCritical() {
		Obs obs = getObs(60, 4090, 121.0);

		obsSaveHandler.handle(obs, null, new Date(), null);

		assertEquals(Obs.Interpretation.HIGH, obs.getInterpretation());
	}

	/**
	 * @see ObsSaveHandler#handle(Obs,User,Date,String)
	 */
	@Test
	public void handle_shouldSetInterpretationToCriticallyHighIfObsValueIsAboveHighCritical() {
		Obs obs = getObs(60, 4090, 131.0);

		obsSaveHandler.handle(obs, null, new Date(), null);

		assertEquals(Obs.Interpretation.CRITICALLY_HIGH, obs.getInterpretation());
	}

	/**
	 * @see ObsSaveHandler#handle(Obs,User,Date,String)
	 */
	@Test
	public void handle_shouldSetInterpretationToCriticallyHighIfObsValueIsEqualToHighCritical() {
		Obs obs = getObs(60, 4090, 130.0);

		obsSaveHandler.handle(obs, null, new Date(), null);

		assertEquals(Obs.Interpretation.CRITICALLY_HIGH, obs.getInterpretation());
	}

	/**
	 * @see ObsSaveHandler#handle(Obs,User,Date,String)
	 */
	@Test
	public void handle_shouldSetInterpretationToCriticallyLowIfObsValueIsEqualToLowCritical() {
		Obs obs = getObs(60, 4090, 75.0);

		obsSaveHandler.handle(obs, null, new Date(), null);

		assertEquals(Obs.Interpretation.CRITICALLY_LOW, obs.getInterpretation());
	}

	/**
	 * @see ObsSaveHandler#handle(Obs,User,Date,String)
	 */
	@Test
	public void handle_shouldSetInterpretationToNormalIfObsValueIsWithinNormalRange() {
		Obs obs = getObs(60, 4090, 100.0);

		obsSaveHandler.handle(obs, null, new Date(), null);

		assertEquals(Obs.Interpretation.NORMAL, obs.getInterpretation());
	}

	/**
	 * @see ObsSaveHandler#handle(Obs,User,Date,String)
	 */
	@Test
	public void handle_shouldSetInterpretationToCriticalLowIfObsValueIsBelowLowCritical() {
		Obs obs = getObs(60, 4090, 74.0);

		obsSaveHandler.handle(obs, null, new Date(), null);

		assertEquals(Obs.Interpretation.CRITICALLY_LOW, obs.getInterpretation());
	}

	/**
	 * @see ObsSaveHandler#handle(Obs,User,Date,String)
	 */
	@Test
	public void handle_shouldSetInterpretationToNormalIfObsValueIsAboveLowNormalAndHiNormalIsNull() {
		Obs obs = createObsWithReferenceRange(60, 97, 4090, 95.0, null, 90.0, null, 0.0, 100.0);

		obsSaveHandler.handle(obs, null, new Date(), null);

		assertEquals(Obs.Interpretation.NORMAL, obs.getInterpretation());
	}

	/**
	 * @see ObsSaveHandler#handle(Obs,User,Date,String)
	 */
	@Test
	public void handle_shouldSetInterpretationToNormalIfObsValueIsBelowHiNormalAndLowNormalIsNull() {
		Obs obs = createObsWithReferenceRange(60, 100, 4090, null, 140.0, null, null, null, null);

		obsSaveHandler.handle(obs, null, new Date(), null);

		assertEquals(Obs.Interpretation.NORMAL, obs.getInterpretation());
	}

	/**
	 * @see ObsSaveHandler#handle(Obs,User,Date,String)
	 */
	@Test
	public void handle_shouldSetInterpretationToHighIfObsValueIsAboveHiNormalAndHiCriticalIsNull() {
		Obs obs = createObsWithReferenceRange(60, 150, 4090, null, 140.0, null, null, null, null);

		obsSaveHandler.handle(obs, null, new Date(), null);

		assertEquals(Obs.Interpretation.HIGH, obs.getInterpretation());
	}

	/**
	 * @see ObsSaveHandler#handle(Obs,User,Date,String)
	 */
	@Test
	public void handle_shouldSetInterpretationToLowIfObsValueIsBelowLowNormalAndLowCriticalIsNull() {
		Obs obs = createObsWithReferenceRange(60, 90, 4090, 100.0, 140.0, null, 180.0, 0.0, 100.0);

		obsSaveHandler.handle(obs, null, new Date(), null);

		assertEquals(Obs.Interpretation.LOW, obs.getInterpretation());
	}

	/**
	 * The reference range used to be derived by the Obs validator, which meant that implementations
	 * running with the <code>validation.disable</code> global property saved observations with no
	 * reference range at all. It is derived by a save handler now, so it survives validation being
	 * switched off.
	 *
	 * @see ObsSaveHandler#handle(Obs,User,Date,String)
	 */
	@Test
	public void handle_shouldSetObsReferenceRangeEvenWhenValidationIsDisabled() {
		Obs obs = buildObservation();

		Obs savedObs;
		ValidateUtil.disableValidationForThread();
		try {
			assertTrue(ValidateUtil.isValidationDisabledForThread());
			savedObs = Context.getObsService().saveObs(obs, null);
		} finally {
			ValidateUtil.resumeValidationForThread();
		}

		assertNotNull(savedObs.getReferenceRange());
		assertNotNull(savedObs.getInterpretation());
	}

	/**
	 * @see ObsSaveHandler#handle(Obs,User,Date,String)
	 */
	@Test
	public void handle_shouldNotOverwriteAReferenceRangeSuppliedByTheCaller() {
		Obs obs = buildObservation();

		ObsReferenceRange callerSuppliedRange = new ObsReferenceRange();
		callerSuppliedRange.setHiAbsolute(1000.0);
		callerSuppliedRange.setHiCritical(900.0);
		callerSuppliedRange.setHiNormal(800.0);
		callerSuppliedRange.setLowNormal(200.0);
		callerSuppliedRange.setLowCritical(100.0);
		callerSuppliedRange.setLowAbsolute(0.0);
		callerSuppliedRange.setObs(obs);
		obs.setReferenceRange(callerSuppliedRange);

		Obs savedObs = Context.getObsService().saveObs(obs, null);

		ObsReferenceRange referenceRange = savedObs.getReferenceRange();
		assertNotNull(referenceRange);
		assertEquals(1000.0, referenceRange.getHiAbsolute());
		assertEquals(900.0, referenceRange.getHiCritical());
		assertEquals(800.0, referenceRange.getHiNormal());
		assertEquals(200.0, referenceRange.getLowNormal());
		assertEquals(100.0, referenceRange.getLowCritical());
		assertEquals(0.0, referenceRange.getLowAbsolute());
	}

	/**
	 * Helper method to create an Obs with specific reference range values
	 *
	 * @param value The numeric value for the observation
	 * @param conceptId The concept id for the observation
	 * @param lowNormal Low normal value (can be null)
	 * @param hiNormal High normal value (can be null)
	 * @param lowCritical Low critical value (can be null)
	 * @param hiCritical High critical value (can be null)
	 * @param lowAbsolute Low absolute value (can be null)
	 * @param hiAbsolute High absolute value (can be null)
	 */
	private static Obs createObsWithReferenceRange(int numberOfYears, double value, int conceptId, Double lowNormal,
	        Double hiNormal, Double lowCritical, Double hiCritical, Double lowAbsolute, Double hiAbsolute) {
		Obs obs = getObs(numberOfYears, conceptId, value);

		// Set up the reference range manually with the provided values
		ObsReferenceRange obsRefRange = new ObsReferenceRange();
		obsRefRange.setHiAbsolute(hiAbsolute);
		obsRefRange.setHiCritical(hiCritical);
		obsRefRange.setHiNormal(hiNormal);
		obsRefRange.setLowAbsolute(lowAbsolute);
		obsRefRange.setLowCritical(lowCritical);
		obsRefRange.setLowNormal(lowNormal);
		obsRefRange.setObs(obs);
		obs.setReferenceRange(obsRefRange);

		return obs;
	}

	private static Obs getObs(int numberOfYears, int conceptId, double valueNumeric) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -numberOfYears);

		Person person = new Person(10);
		person.setBirthdate(calendar.getTime());

		Obs obs = new Obs();
		obs.setPerson(person);
		obs.setConcept(Context.getConceptService().getConcept(conceptId));
		obs.setValueNumeric(valueNumeric);
		obs.setObsDatetime(new Date());
		return obs;
	}

	/**
	 * Builds an observation that can be saved through the ObsService, i.e. one whose person is a
	 * persisted patient.
	 */
	private static Obs buildObservation() {
		Concept concept = Context.getConceptService().getConcept(4089);
		Patient patient = Context.getPatientService().getPatient(2);
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -5);
		patient.setBirthdate(calendar.getTime());

		Obs obs = new Obs();
		obs.setConcept(concept);
		obs.setPerson(patient);
		obs.setEncounter(Context.getEncounterService().getEncounter(3));
		obs.setObsDatetime(new Date());
		obs.setLocation(new Location(1));
		obs.setValueNumeric(90.0);

		return obs;
	}
}
