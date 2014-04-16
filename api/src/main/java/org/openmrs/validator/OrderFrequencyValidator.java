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
package org.openmrs.validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
	
	/** Log for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * Determines if the command object being submitted is a valid type
	 * 
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean supports(Class c) {
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
		}
	}
}
