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

import java.util.List;

import org.openmrs.Concept;
import org.openmrs.ServiceOrder;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validates the {@link org.openmrs.ServiceOrder} class.
 * 
 * @since 2.5.0
 */
@Handler(supports = { ServiceOrder.class }, order = 50)
@Component("serviceOrderValidator")
public class ServiceOrderValidator extends OrderValidator implements Validator {
	
	/**
	 * Determines if the object being submitted is a valid type
	 * 
	 * @see org.springframework.validation.Validator#supports(Class)
	 */
	@Override
	public boolean supports(Class<?> c) {
		return ServiceOrder.class.isAssignableFrom(c);
	}
	
	/**
	 * Checks the form object for any inconsistencies/errors
	 * 
	 * @see org.springframework.validation.Validator#validate(Object,
	 *      org.springframework.validation.Errors)
	 * <strong>Should</strong> fail validation if the specimen source is invalid
	 * <strong>Should</strong> pass validation if the specimen source is valid
	 */
	@Override
	public void validate(Object obj, Errors errors) {
		super.validate(obj, errors);
		ServiceOrder order = (ServiceOrder) obj;
		if (order == null) {
			errors.reject("error.general");
		} else {
			if (order.getSpecimenSource() != null) {
				List<Concept> specimenSources = Context.getOrderService().getTestSpecimenSources();
				if (!specimenSources.contains(order.getSpecimenSource())) {
					errors.rejectValue("specimenSource", "ServiceOrder.error.specimenSourceNotAmongAllowedConcepts");
				}
			}
		}
	}
}
