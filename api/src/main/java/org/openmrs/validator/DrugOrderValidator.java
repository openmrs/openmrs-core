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
import org.openmrs.DrugOrder;
import org.openmrs.annotation.Handler;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

/**
 * Validates the {@link DrugOrder} class.
 *
 * @since 1.5
 */
@Handler(supports = { DrugOrder.class }, order = 50)
public class DrugOrderValidator extends OrderValidator {
	
	/** Log for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * Determines if the command object being submitted is a valid type
	 *
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	public boolean supports(Class<?> c) {
		return DrugOrder.class.isAssignableFrom(c);
	}
	
	/**
	 * Checks the form object for any inconsistencies/errors
	 *
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 * @should fail validation if order concept is null
	 * @should fail validation if drug concept is different from order concept
	 * @should fail validation if drug concept is not set
	 * @should fail validation if drug dose is set and units is not set
	 * @should fail if quantity set and units not set
	 * @should pass validation if all fields are correct
	 */
	public void validate(Object obj, Errors errors) {
		super.validate(obj, errors);
		
		DrugOrder order = (DrugOrder) obj;
		if (order == null) {
			errors.rejectValue("order", "error.general");
		} else {
			ValidationUtils.rejectIfEmpty(errors, "concept", "error.null");
			if (order.getDuration() != null) {
				ValidationUtils.rejectIfEmpty(errors, "durationUnits", "DrugOrder.add.error.missingDurationUnits");
			}
			
			if (order.getQuantity() != null) {
				ValidationUtils.rejectIfEmpty(errors, "quantityUnits", "DrugOrder.add.error.missingQuantityUnits");
			}
			
			if (order.getStrength() != null) {
				ValidationUtils.rejectIfEmpty(errors, "strengthUnits", "DrugOrder.add.error.missingStrengthUnits");
			}
			
			if (order.getDose() != null) {
				ValidationUtils.rejectIfEmpty(errors, "doseUnits", "DrugOrder.add.error.missingDoseUnits");
			}
			
			// for the following elements Order.hbm.xml says: not-null="true"
			ValidationUtils.rejectIfEmpty(errors, "drug", "error.null");
			if (order.getDrug() != null) {
				ValidationUtils.rejectIfEmpty(errors, "drug.concept", "error.null");
			}
			
			if (!(order.getConcept() == null)) {
				if (!(order.getDrug() == null) && !(order.getDrug().getConcept().equals(order.getConcept()))) {
					errors.rejectValue("drug", "error.general");
					errors.rejectValue("concept", "error.concept");
					
				}
			}
		}
	}
}
