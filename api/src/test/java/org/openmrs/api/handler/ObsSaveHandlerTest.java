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
import org.openmrs.Obs;
import org.openmrs.ObsReferenceRange;
import org.openmrs.Person;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ObsSaveHandlerTest extends BaseContextSensitiveTest {

	private final ObsSaveHandler obsSaveHandler = new ObsSaveHandler();

	@Test
	public void shouldSetObsReferenceRangeIfCriteriaMatches() {
		Person person = new Person(1);
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -6);
		person.setBirthdate(calendar.getTime());

		Obs obs = new Obs();
		obs.setPerson(person);
		obs.setConcept(Context.getConceptService().getConcept(4090));
		obs.setValueNumeric(88.0);
		obs.setObsDatetime(new Date());

		obsSaveHandler.handle(obs, null, null, null);

		assertNotNull(obs.getReferenceRange());
		assertEquals(140.0, obs.getReferenceRange().getHiAbsolute());
	}

	@Test
	public void shouldSetObsReferenceRangeValuesIfConceptReferenceRangeIsNullAndConceptNumericIsNotNull() {
		Person person = new Person(1);
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -600);
		person.setBirthdate(calendar.getTime());

		Obs obs = new Obs();
		obs.setPerson(person);
		obs.setConcept(Context.getConceptService().getConcept(4090));
		obs.setValueNumeric(88.0);
		obs.setObsDatetime(new Date());

		obsSaveHandler.handle(obs, null, null, null);

		assertNotNull(obs.getReferenceRange());
		assertEquals(145.0, obs.getReferenceRange().getHiAbsolute());
		assertEquals(70.0, obs.getReferenceRange().getLowAbsolute());
	}

	@Test
	public void shouldSetObsReferenceRangeValuesToNarrowestMatchingValues() {
		Person person = new Person(1);
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -6);
		person.setBirthdate(calendar.getTime());

		Obs obs = new Obs();
		obs.setPerson(person);
		obs.setConcept(Context.getConceptService().getConcept(4090));
		obs.setValueNumeric(88.0);
		obs.setObsDatetime(new Date());

		obsSaveHandler.handle(obs, null, null, null);

		assertNotNull(obs.getReferenceRange());
		assertEquals(140.0, obs.getReferenceRange().getHiAbsolute());
		assertEquals(130.0, obs.getReferenceRange().getHiCritical());
		assertEquals(118.0, obs.getReferenceRange().getHiNormal());
		assertEquals(80.0, obs.getReferenceRange().getLowNormal());
		assertEquals(75.0, obs.getReferenceRange().getLowCritical());
		assertEquals(70.0, obs.getReferenceRange().getLowAbsolute());
	}

	@Test
	public void shouldSetObsReferenceRangeValuesToConceptReferenceRangeValuesIfNoRuleBasedRangesArePresent() {
		Person person = new Person(1);
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -600);
		person.setBirthdate(calendar.getTime());

		Obs obs = new Obs();
		obs.setPerson(person);
		obs.setConcept(Context.getConceptService().getConcept(5497));
		obs.setValueNumeric(88.0);
		obs.setObsDatetime(new Date());

		obsSaveHandler.handle(obs, null, null, null);

		assertNotNull(obs.getReferenceRange());
		assertEquals(2500.0, obs.getReferenceRange().getHiAbsolute());
		assertEquals(1800.0, obs.getReferenceRange().getHiCritical());
		assertEquals(1497.0, obs.getReferenceRange().getHiNormal());
		assertEquals(445.0, obs.getReferenceRange().getLowNormal());
		assertEquals(99.0, obs.getReferenceRange().getLowCritical());
		assertEquals(0.0, obs.getReferenceRange().getLowAbsolute());
	}

	@Test
	public void shouldNotOverwriteCallerSuppliedReferenceRange() {
		Obs obs = getObs(60, 4090, 100.0);

		ObsReferenceRange referenceRange = new ObsReferenceRange();
		referenceRange.setHiAbsolute(200.0);
		referenceRange.setHiCritical(180.0);
		referenceRange.setHiNormal(150.0);
		referenceRange.setLowNormal(80.0);
		referenceRange.setLowCritical(60.0);
		referenceRange.setLowAbsolute(40.0);
		referenceRange.setObs(obs);

		obs.setReferenceRange(referenceRange);

		obsSaveHandler.handle(obs, null, null, null);

		assertEquals(200.0, obs.getReferenceRange().getHiAbsolute());
		assertEquals(180.0, obs.getReferenceRange().getHiCritical());
		assertEquals(150.0, obs.getReferenceRange().getHiNormal());
		assertEquals(80.0, obs.getReferenceRange().getLowNormal());
		assertEquals(60.0, obs.getReferenceRange().getLowCritical());
		assertEquals(40.0, obs.getReferenceRange().getLowAbsolute());
	}

	@Test
	public void shouldSetInterpretationToHighIfObsValueIsAboveHiNormalAndLessThanHighCritical() {
		Obs obs = createObsWithReferenceRange(60, 121.0, 4090, 80.0, 120.0, 75.0, 130.0, 70.0, 140.0);

		obsSaveHandler.handle(obs, null, null, null);

		assertNotNull(obs.getInterpretation());
		assertEquals(Obs.Interpretation.HIGH, obs.getInterpretation());
	}

	@Test
	public void shouldSetInterpretationToCriticallyHighIfObsValueIsAboveHighCritical() {
		Obs obs = createObsWithReferenceRange(60, 131.0, 4090, 80.0, 120.0, 75.0, 130.0, 70.0, 140.0);

		obsSaveHandler.handle(obs, null, null, null);

		assertEquals(Obs.Interpretation.CRITICALLY_HIGH, obs.getInterpretation());
	}

	@Test
	public void shouldSetInterpretationToCriticallyHighIfObsValueIsEqualToHighCritical() {
		Obs obs = createObsWithReferenceRange(60, 130.0, 4090, 80.0, 120.0, 75.0, 130.0, 70.0, 140.0);

		obsSaveHandler.handle(obs, null, null, null);

		assertEquals(Obs.Interpretation.CRITICALLY_HIGH, obs.getInterpretation());
	}

	@Test
	public void shouldSetInterpretationToCriticallyLowIfObsValueIsEqualToLowCritical() {
		Obs obs = createObsWithReferenceRange(60, 75.0, 4090, 80.0, 120.0, 75.0, 130.0, 70.0, 140.0);

		obsSaveHandler.handle(obs, null, null, null);

		assertEquals(Obs.Interpretation.CRITICALLY_LOW, obs.getInterpretation());
	}

	@Test
	public void shouldSetInterpretationToNormalIfObsValueIsWithinNormalRange() {
		Obs obs = createObsWithReferenceRange(60, 100.0, 4090, 80.0, 120.0, 75.0, 130.0, 70.0, 140.0);

		obsSaveHandler.handle(obs, null, null, null);

		assertEquals(Obs.Interpretation.NORMAL, obs.getInterpretation());
	}

	@Test
	public void shouldSetInterpretationToCriticallyLowIfObsValueIsBelowLowCritical() {
		Obs obs = createObsWithReferenceRange(60, 74.0, 4090, 80.0, 120.0, 75.0, 130.0, 70.0, 140.0);

		obsSaveHandler.handle(obs, null, null, null);

		assertEquals(Obs.Interpretation.CRITICALLY_LOW, obs.getInterpretation());
	}

	@Test
	public void shouldSetInterpretationToNormalIfObsValueIsAboveLowNormalAndHiNormalIsNull() {
		Obs obs = createObsWithReferenceRange(60, 95.0, 4090, 90.0, null, 0.0, 100.0, null, null);

		obsSaveHandler.handle(obs, null, null, null);

		assertEquals(Obs.Interpretation.NORMAL, obs.getInterpretation());
	}

	@Test
	public void shouldSetInterpretationToNormalIfObsValueIsBelowHiNormalAndLowNormalIsNull() {
		Obs obs = createObsWithReferenceRange(60, 100.0, 4090, null, 140.0, null, null, null, null);

		obsSaveHandler.handle(obs, null, null, null);

		assertEquals(Obs.Interpretation.NORMAL, obs.getInterpretation());
	}

	@Test
	public void shouldSetInterpretationToHighIfObsValueIsAboveHiNormalAndHiCriticalIsNull() {
		Obs obs = createObsWithReferenceRange(60, 150.0, 4090, null, 140.0, null, null, null, null);

		obsSaveHandler.handle(obs, null, null, null);

		assertEquals(Obs.Interpretation.HIGH, obs.getInterpretation());
	}

	@Test
	public void shouldSetInterpretationToLowIfObsValueIsBelowLowNormalAndLowCriticalIsNull() {
		Obs obs = createObsWithReferenceRange(60, 90.0, 4090, 100.0, 140.0, null, 180.0, 0.0, 100.0);

		obsSaveHandler.handle(obs, null, null, null);

		assertEquals(Obs.Interpretation.LOW, obs.getInterpretation());
	}

	private static Obs createObsWithReferenceRange(int numberOfYears, double value, int conceptId, Double lowNormal,
	        Double hiNormal, Double lowCritical, Double hiCritical, Double lowAbsolute, Double hiAbsolute) {
		Obs obs = getObs(numberOfYears, conceptId, value);

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
}
