package org.openmrs.validator;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.CareSetting;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.TestOrder;
import org.openmrs.api.ConceptService;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 *
 */
public class TestOrderValidatorTest extends BaseContextSensitiveTest {
	
	/**
	 * @verifies fail validation if the specimen source is invalid
	 * @see TestOrderValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfTheSpecimenSourceIsInvalid() throws Exception {
		TestOrder order = new TestOrder();
		Patient patient = new Patient(8);
		order.setPatient(patient);
		ConceptService conceptService = Context.getConceptService();
		OrderService orderService = Context.getOrderService();
		order.setOrderType(orderService.getOrderTypeByName("Test order"));
		order.setConcept(conceptService.getConcept(5497));
		order.setOrderer(new Provider());
		order.setCareSetting(new CareSetting());
		Encounter encounter = new Encounter();
		encounter.setPatient(patient);
		order.setEncounter(encounter);
		order.setStartDate(new Date());
		order.setSpecimenSource(conceptService.getConcept(3));
		
		Errors errors = new BindException(order, "order");
		new TestOrderValidator().validate(order, errors);
		Assert.assertTrue(errors.hasFieldErrors("specimenSource"));
		
	}
}
