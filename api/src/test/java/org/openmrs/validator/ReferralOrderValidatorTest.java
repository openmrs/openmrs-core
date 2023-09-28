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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isIn;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import org.junit.jupiter.api.Test;
import org.openmrs.CareSetting;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.ReferralOrder;
import org.openmrs.api.ConceptService;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 *
 */
public class ReferralOrderValidatorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see ServiceOrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfTheSpecimenSourceIsInvalid() {
		ConceptService conceptService = Context.getConceptService();
		Concept specimenSource = conceptService.getConcept(3);
		OrderService orderService = Context.getOrderService();
		assertThat(specimenSource, not(isIn(orderService.getDrugRoutes())));
		ReferralOrder order = new ReferralOrder();
		Patient patient = new Patient(8);
		order.setPatient(patient);
		order.setOrderType(orderService.getOrderTypeByName("Referral order"));
		order.setConcept(conceptService.getConcept(5497));
		order.setOrderer(new Provider());
		order.setCareSetting(new CareSetting());
		Encounter encounter = new Encounter();
		encounter.setPatient(patient);
		order.setEncounter(encounter);
		order.setDateActivated(new Date());
		order.setSpecimenSource(specimenSource);
		
		Errors errors = new BindException(order, "order");
		new ServiceOrderValidator().validate(order, errors);
		assertTrue(errors.hasFieldErrors("specimenSource"));
		assertEquals("ServiceOrder.error.specimenSourceNotAmongAllowedConcepts", errors.getFieldError("specimenSource")
		        .getCode());
	}
	
	/**
	 * @see ServiceOrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfTheSpecimenSourceIsValid() {
		ConceptService conceptService = Context.getConceptService();
		Concept specimenSource = conceptService.getConcept(22);
		OrderService orderService = Context.getOrderService();
		assertThat(specimenSource, isIn(orderService.getDrugRoutes()));
		ReferralOrder order = new ReferralOrder();
		Patient patient = new Patient(8);
		order.setPatient(patient);
		order.setOrderType(orderService.getOrderTypeByName("Referral order"));
		order.setConcept(conceptService.getConcept(5497));
		order.setOrderer(Context.getProviderService().getProvider(1));
		order.setCareSetting(new CareSetting());
		Encounter encounter = new Encounter();
		encounter.setPatient(patient);
		order.setEncounter(encounter);
		order.setDateActivated(new Date());
		order.setSpecimenSource(specimenSource);
		
		Errors errors = new BindException(order, "order");
		new ServiceOrderValidator().validate(order, errors);
		assertFalse(errors.hasFieldErrors());
	}
}
