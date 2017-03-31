/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import static junit.framework.TestCase.assertEquals;
import static org.openmrs.test.TestUtil.createDateTime;

import java.text.ParseException;
import java.util.Date;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

public class SimpleDosingInstructionsTest extends BaseContextSensitiveTest {
	
	@Test
	public void validate_shouldFailValidationIfAutoExpireDateIsNotSetAndDurationUnitsIsNotMappedToSNOMEDCTDuration()
	        {
		DrugOrder drugOrder = createValidDrugOrder();
		drugOrder.setDuration(30);
		Concept unMappedDurationUnits = new Concept();
		drugOrder.setDurationUnits(unMappedDurationUnits);
		drugOrder.setAutoExpireDate(null);
		Errors errors = new BindException(drugOrder, "drugOrder");
		
		new SimpleDosingInstructions().validate(drugOrder, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("durationUnits"));
		Assert.assertEquals("DrugOrder.error.durationUnitsNotMappedToSnomedCtDurationCode", errors.getFieldError(
		    "durationUnits").getCode());
	}
	
	@Test
	public void validate_shouldPassValidationIfAutoExpireDateIsSetAndDurationUnitsIsNotMappedToSNOMEDCTDuration()
	        throws ParseException
	        {
		DrugOrder drugOrder = createValidDrugOrder();
		drugOrder.setDuration(30);
		Concept unMappedDurationUnits = new Concept();
		drugOrder.setDurationUnits(unMappedDurationUnits);
		drugOrder.setAutoExpireDate(createDateTime("2014-07-01 10:00:00"));
		Errors errors = new BindException(drugOrder, "drugOrder");
		
		new SimpleDosingInstructions().validate(drugOrder, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	@Test
	public void validate_shouldPassValidationIfAutoExpireDateAndDurationUnitsAreNotSet() {
		DrugOrder drugOrder = createValidDrugOrder();
		drugOrder.setDurationUnits(null);
		drugOrder.setAutoExpireDate(null);
		Errors errors = new BindException(drugOrder, "drugOrder");
		
		new SimpleDosingInstructions().validate(drugOrder, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	@Test
	public void getAutoExpireDate_shouldInferAutoExpireDateForAKnownSNOMEDCTDurationUnit() throws ParseException {
		DrugOrder drugOrder = new DrugOrder();
		drugOrder.setDateActivated(createDateTime("2014-07-01 10:00:00"));
		drugOrder.setDuration(30);
		drugOrder.setDurationUnits(createUnits(Duration.SNOMED_CT_SECONDS_CODE));
		Date autoExpireDate = new SimpleDosingInstructions().getAutoExpireDate(drugOrder);
		assertEquals(createDateTime("2014-07-01 10:00:29"), autoExpireDate);
	}
	
	@Test
	public void getAutoExpireDate_shouldInferAutoExpireDateForScheduledDrugOrder() throws ParseException {
		DrugOrder drugOrder = new DrugOrder();
		drugOrder.setDateActivated(createDateTime("2014-07-01 00:00:00"));
		drugOrder.setScheduledDate(createDateTime("2014-07-05 00:00:00"));
		drugOrder.setUrgency(Order.Urgency.ON_SCHEDULED_DATE);
		drugOrder.setDuration(10);
		drugOrder.setDurationUnits(createUnits(Duration.SNOMED_CT_DAYS_CODE));
		Date autoExpireDate = new SimpleDosingInstructions().getAutoExpireDate(drugOrder);
		assertEquals(createDateTime("2014-07-14 23:59:59"), autoExpireDate);
	}
	
	@Test
	public void getAutoExpireDate_shouldNotInferAutoExpireDateWhenDrugOrderHasOneOrMoreRefill() throws ParseException {
		DrugOrder drugOrder = new DrugOrder();
		drugOrder.setDateActivated(createDateTime("2014-07-01 10:00:00"));
		drugOrder.setDuration(30);
		drugOrder.setDurationUnits(createUnits(Duration.SNOMED_CT_SECONDS_CODE));
		drugOrder.setNumRefills(1);
		
		Date autoExpireDate = new SimpleDosingInstructions().getAutoExpireDate(drugOrder);
		
		assertEquals(null, autoExpireDate);
	}
	
	@Test
	public void getAutoExpireDate_shouldNotInferAutoExpireDateWhenDurationDoesNotExist() throws ParseException {
		DrugOrder drugOrder = new DrugOrder();
		drugOrder.setDateActivated(createDateTime("2014-07-01 10:00:00"));
		drugOrder.setDurationUnits(createUnits(Duration.SNOMED_CT_SECONDS_CODE));
		drugOrder.setDuration(null);
		
		Date autoExpireDate = new SimpleDosingInstructions().getAutoExpireDate(drugOrder);
		
		assertEquals(null, autoExpireDate);
	}
	
	@Test
	public void getAutoExpireDate_shouldNotInferAutoExpireDateWhenDurationUnitsDoesNotExist() throws ParseException {
		DrugOrder drugOrder = new DrugOrder();
		drugOrder.setDateActivated(createDateTime("2014-07-01 10:00:00"));
		drugOrder.setDuration(1);
		drugOrder.setDurationUnits(null);
		
		Date autoExpireDate = new SimpleDosingInstructions().getAutoExpireDate(drugOrder);
		
		assertEquals(null, autoExpireDate);
	}
	
	@Test
	public void getAutoExpireDate_shouldNotInferAutoExpireDateWhenConceptMappingOfSourceSNOMEDCTDurationDoesNotExist()
	        throws ParseException {
		DrugOrder drugOrder = new DrugOrder();
		drugOrder.setDateActivated(createDateTime("2014-07-01 10:00:00"));
		drugOrder.setDuration(30);
		drugOrder.setDurationUnits(createUnits("Other.Source", Duration.SNOMED_CT_HOURS_CODE, null));
		
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
	
	public static Concept createUnits(String code) {
		return createUnits(Duration.SNOMED_CT_CONCEPT_SOURCE_HL7_CODE, code, null);
	}
	
	public static Concept createUnits(String source, String code, String mapTypeUuid) {
		Concept doseUnits = new Concept();
		doseUnits.addConceptMapping(getConceptMap(source, code, mapTypeUuid));
		return doseUnits;
	}
	
	private static ConceptMap getConceptMap(String sourceHl7Code, String code, String mapTypeUuid) {
		ConceptMap conceptMap = new ConceptMap();
		ConceptReferenceTerm conceptReferenceTerm = new ConceptReferenceTerm();
		ConceptSource conceptSource = new ConceptSource();
		conceptSource.setHl7Code(sourceHl7Code);
		conceptReferenceTerm.setConceptSource(conceptSource);
		conceptReferenceTerm.setCode(code);
		conceptMap.setConceptReferenceTerm(conceptReferenceTerm);
		ConceptMapType conceptMapType = new ConceptMapType();
		if (mapTypeUuid != null) {
			conceptMapType.setUuid(mapTypeUuid);
		} else {
			conceptMapType.setUuid(ConceptMapType.SAME_AS_MAP_TYPE_UUID);
		}
		conceptMap.setConceptMapType(conceptMapType);
		return conceptMap;
	}
	
	/**
	 * @see SimpleDosingInstructions#validate(DrugOrder, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldRejectADurationUnitWithAMappingOfAnInvalidType() {
		DrugOrder drugOrder = createValidDrugOrder();
		drugOrder.setDuration(30);
		Concept durationUnitWithInvalidMapType = createUnits("SCT", Duration.SNOMED_CT_DAYS_CODE, "Some-uuid");
		drugOrder.setDurationUnits(durationUnitWithInvalidMapType);
		Errors errors = new BindException(drugOrder, "drugOrder");
		
		new SimpleDosingInstructions().validate(drugOrder, errors);
		
		assertEquals(true, errors.hasErrors());
	}
}
