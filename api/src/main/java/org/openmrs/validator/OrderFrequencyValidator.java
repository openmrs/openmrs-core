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

import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.OrderFrequency;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Validates the {@link OrderFrequency} class.
 * 
 * @since 1.10
 */
@Handler(supports = { OrderFrequency.class })
public class OrderFrequencyValidator implements Validator {
	
	/**
	 * Determines if the command object being submitted is a valid type
	 * 
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> c) {
		return OrderFrequency.class.isAssignableFrom(c);
	}
	
	/**
	 * Checks the order frequency object for any inconsistencies/errors
	 * 
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 * @should fail if orderFrequency is null
	 * @should fail if concept is null
	 * @should fail if the concept is not of class frequency
	 * @should fail if concept is used by another frequency
	 * @should pass for a valid new order frequency
	 * @should pass for a valid existing order frequency
	 * @should be invoked when an order frequency is saved
	 * @should pass validation if field lengths are correct
	 * @should fail validation if field lengths are not correct
	 */
	@Override
	public void validate(Object obj, Errors errors) {
		OrderFrequency orderFrequency = (OrderFrequency) obj;
		if (orderFrequency == null) {
			errors.reject("error.general");
		} else {
			ValidationUtils.rejectIfEmpty(errors, "concept", "Concept.noConceptSelected");
			
			Concept concept = orderFrequency.getConcept();
			if (concept != null) {
				if (!ConceptClass.FREQUENCY_UUID.equals(concept.getConceptClass().getUuid())) {
					errors.rejectValue("concept", "OrderFrequency.concept.shouldBeClassFrequency");
				}
				
				OrderFrequency of = Context.getOrderService().getOrderFrequencyByConcept(concept);
				if (of != null && !of.equals(orderFrequency)) {
					errors.rejectValue("concept", "OrderFrequency.concept.shouldNotBeShared");
				}
			}
			ValidateUtil.validateFieldLengths(errors, obj.getClass(), "retireReason");
		}
	}
}
