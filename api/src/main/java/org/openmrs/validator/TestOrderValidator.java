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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.TestOrder;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validates the {@link org.openmrs.TestOrder} class.
 * 
 * @since 1.10
 */
@Handler(supports = { TestOrder.class }, order = 50)
@Component("testOrderValidator")
public class TestOrderValidator extends OrderValidator implements Validator {
	
	/** Log for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * Determines if the object being submitted is a valid type
	 * 
	 * @see org.springframework.validation.Validator#supports(Class)
	 */
	@SuppressWarnings("unchecked")
	public boolean supports(Class c) {
		return TestOrder.class.isAssignableFrom(c);
	}
	
	/**
	 * Checks the form object for any inconsistencies/errors
	 * 
	 * @see org.springframework.validation.Validator#validate(Object,
	 *      org.springframework.validation.Errors)
	 * @should fail validation if the specimen source is invalid
	 * @should pass validation if the specimen source is valid
	 */
	public void validate(Object obj, Errors errors) {
		super.validate(obj, errors);
		TestOrder order = (TestOrder) obj;
		if (order == null) {
			errors.reject("error.general");
		} else {
			if (order.getSpecimenSource() != null) {
				List<Concept> specimenSources = Context.getOrderService().getTestSpecimenSources();
				if (!specimenSources.contains(order.getSpecimenSource())) {
					errors.rejectValue("specimenSource", "TestOrder.error.specimenSourceNotAmongAllowedConcepts");
				}
			}
		}
	}
}
