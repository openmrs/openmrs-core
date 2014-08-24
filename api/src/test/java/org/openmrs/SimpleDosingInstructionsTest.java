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
package org.openmrs;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

import java.util.Date;
import java.util.Random;
import java.util.UUID;

import static junit.framework.TestCase.assertEquals;
import static org.openmrs.test.TestUtil.createDateTime;

public class SimpleDosingInstructionsTest extends BaseContextSensitiveTest {
	
	@Test
	public void validate_shouldFailValidationIfAutoExpireDateIsNotSetAndDurationUnitsIsNotMappedToISO8601Duration()
	        throws Exception {
		DrugOrder drugOrder = createValidDrugOrder();
		drugOrder.setDuration(30);
		Concept unMappedDurationUnits = new Concept();
		drugOrder.setDurationUnits(unMappedDurationUnits);
		drugOrder.setAutoExpireDate(null);
		Errors errors = new BindException(drugOrder, "drugOrder");
		
		new SimpleDosingInstructions().validate(drugOrder, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("durationUnits"));
		Assert.assertEquals("DrugOrder.error.durationUnitsNotMappedToISO8601DurationCode", errors.getFieldError(
		    "durationUnits").getCode());
	}
	
	@Test
	public void validate_shouldPassValidationIfAutoExpireDateIsSetAndDurationUnitsIsNotMappedToISO8601Duration()
	        throws Exception {
		DrugOrder drugOrder = createValidDrugOrder();
		drugOrder.setDuration(30);
		Concept unMappedDurationUnits = new Concept();
		drugOrder.setDurationUnits(unMappedDurationUnits);
		drugOrder.setAutoExpireDate(createDateTime("2014-07-01 10-00-00"));
		Errors errors = new BindException(drugOrder, "drugOrder");
		
		new SimpleDosingInstructions().validate(drugOrder, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	@Test
	public void validate_shouldPassValidationIfAutoExpireDateAndDurationUnitsAreNotSet() throws Exception {
		DrugOrder drugOrder = createValidDrugOrder();
		drugOrder.setDurationUnits(null);
		drugOrder.setAutoExpireDate(null);
		Errors errors = new BindException(drugOrder, "drugOrder");
		
		new SimpleDosingInstructions().validate(drugOrder, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	@Test
	public void getAutoExpireDate_shouldInferAutoExpireDateForAKnownISO8601DurationUnit() throws Exception {
		DrugOrder drugOrder = new DrugOrder();
		drugOrder.setDateActivated(createDateTime("2014-07-01 10-00-00"));
		drugOrder.setDuration(30);
		drugOrder.setDurationUnits(createUnitsSameAs(ISO8601Duration.SECONDS_CODE));
		
		Date autoExpireDate = new SimpleDosingInstructions().getAutoExpireDate(drugOrder);
		
		assertEquals(createDateTime("2014-07-01 10-00-30"), autoExpireDate);
	}
	
	@Test
	public void getAutoExpireDate_shouldInferAutoExpireDateForScheduledDrugOrder() throws Exception {
		DrugOrder drugOrder = new DrugOrder();
		drugOrder.setDateActivated(createDateTime("2014-07-01 00-00-00"));
		drugOrder.setScheduledDate(createDateTime("2014-07-05 00-00-00"));
		drugOrder.setUrgency(Order.Urgency.ON_SCHEDULED_DATE);
		drugOrder.setDuration(10);
		drugOrder.setDurationUnits(createUnitsSameAs(ISO8601Duration.DAYS_CODE));
		
		Date autoExpireDate = new SimpleDosingInstructions().getAutoExpireDate(drugOrder);
		
		assertEquals(createDateTime("2014-07-15 00-00-00"), autoExpireDate);
	}
	
	@Test
	public void getAutoExpireDate_shouldNotInferAutoExpireDateWhenDrugOrderHasOneOrMoreRefill() throws Exception {
		DrugOrder drugOrder = new DrugOrder();
		drugOrder.setDateActivated(createDateTime("2014-07-01 10-00-00"));
		drugOrder.setDuration(30);
		drugOrder.setDurationUnits(createUnitsSameAs(ISO8601Duration.SECONDS_CODE));
		drugOrder.setNumRefills(1);
		
		Date autoExpireDate = new SimpleDosingInstructions().getAutoExpireDate(drugOrder);
		
		assertEquals(null, autoExpireDate);
	}
	
	@Test
	public void getAutoExpireDate_shouldNotInferAutoExpireDateWhenDurationDoesNotExist() throws Exception {
		DrugOrder drugOrder = new DrugOrder();
		drugOrder.setDateActivated(createDateTime("2014-07-01 10-00-00"));
		drugOrder.setDurationUnits(createUnitsSameAs(ISO8601Duration.SECONDS_CODE));
		drugOrder.setDuration(null);
		
		Date autoExpireDate = new SimpleDosingInstructions().getAutoExpireDate(drugOrder);
		
		assertEquals(null, autoExpireDate);
	}
	
	@Test
	public void getAutoExpireDate_shouldNotInferAutoExpireDateWhenDurationUnitsDoesNotExist() throws Exception {
		DrugOrder drugOrder = new DrugOrder();
		drugOrder.setDateActivated(createDateTime("2014-07-01 10-00-00"));
		drugOrder.setDuration(1);
		drugOrder.setDurationUnits(null);
		
		Date autoExpireDate = new SimpleDosingInstructions().getAutoExpireDate(drugOrder);
		
		assertEquals(null, autoExpireDate);
	}
	
	@Test
	public void getAutoExpireDate_shouldNotInferAutoExpireDateWhenConceptMappingOfTypeSameAsDoesNotExist() throws Exception {
		DrugOrder drugOrder = new DrugOrder();
		drugOrder.setDateActivated(createDateTime("2014-07-01 10-00-00"));
		drugOrder.setDuration(30);
		String mapTypeUuid = UUID.randomUUID().toString();
		Concept units = createUnits(mapTypeUuid, ISO8601Duration.CONCEPT_SOURCE_UUID, ISO8601Duration.HOURS_CODE);
		drugOrder.setDurationUnits(units);
		
		Date autoExpireDate = new SimpleDosingInstructions().getAutoExpireDate(drugOrder);
		
		assertEquals(null, autoExpireDate);
	}
	
	@Test
	public void getAutoExpireDate_shouldNotInferAutoExpireDateWhenConceptMappingOfSourceISO8601DurationDoesNotExist()
	        throws Exception {
		DrugOrder drugOrder = new DrugOrder();
		drugOrder.setDateActivated(createDateTime("2014-07-01 10-00-00"));
		drugOrder.setDuration(30);
		drugOrder.setDurationUnits(createUnits(ConceptMapType.SAME_AS_UUID, "Other.Source", ISO8601Duration.HOURS_CODE));
		
		Date autoExpireDate = new SimpleDosingInstructions().getAutoExpireDate(drugOrder);
		
		assertEquals(null, autoExpireDate);
	}
	
	private DrugOrder createValidDrugOrder() {
		DrugOrder drugOrder = new DrugOrder();
		drugOrder.setDose(10.0);
		drugOrder.setDoseUnits(createConceptWithName("ml"));
		drugOrder.setRoute(createConceptWithName("IV"));
		OrderFrequency frequency = new OrderFrequency();
		frequency.setConcept(createConceptWithName("Twice a day"));
		drugOrder.setFrequency(frequency);
		return drugOrder;
	}
	
	private Concept createConceptWithName(String name) {
		Concept concept = new Concept(new Random().nextInt());
		ConceptName conceptName = new ConceptName();
		conceptName.setName(name);
		conceptName.setLocale(Context.getLocale());
		conceptName.setLocalePreferred(true);
		concept.addName(conceptName);
		return concept;
	}
	
	private Concept createUnitsSameAs(String code) {
		return createUnits(ConceptMapType.SAME_AS_UUID, ISO8601Duration.CONCEPT_SOURCE_UUID, code);
	}
	
	private Concept createUnits(String mapTypeUuid, String source, String code) {
		Concept doseUnits = new Concept();
		doseUnits.addConceptMapping(getConceptMap(mapTypeUuid, source, code));
		return doseUnits;
	}
	
	private ConceptMap getConceptMap(String mapTypeUuid, String sourceUuuid, String code) {
		ConceptMap conceptMap = new ConceptMap();
		ConceptReferenceTerm conceptReferenceTerm = new ConceptReferenceTerm();
		ConceptSource conceptSource = new ConceptSource();
		conceptSource.setUuid(sourceUuuid);
		conceptReferenceTerm.setConceptSource(conceptSource);
		conceptReferenceTerm.setCode(code);
		conceptMap.setConceptReferenceTerm(conceptReferenceTerm);
		ConceptMapType conceptMapType = new ConceptMapType();
		conceptMapType.setUuid(mapTypeUuid);
		conceptMap.setConceptMapType(conceptMapType);
		return conceptMap;
	}
	
}
