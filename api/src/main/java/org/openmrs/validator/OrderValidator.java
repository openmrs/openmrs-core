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

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Order;
import org.openmrs.annotation.Handler;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Validates the {@link Order} class.
 *
 * @since 1.5
 */
@Handler(supports = { Order.class })
public class OrderValidator implements Validator {
	
	/** Log for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * Determines if the command object being submitted is a valid type
	 *
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	public boolean supports(Class<?> c) {
		return Order.class.isAssignableFrom(c);
	}
	
	/**
	 * Checks the form object for any inconsistencies/errors
	 *
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 * @should fail validation if order is null
	 * @should fail validation if order and encounter have different patients
	 * @should fail validation if discontinued is null
	 * @should fail validation if voided is null
	 * @should fail validation if concept is null
	 * @should fail validation if patient is null
	 * @should fail validation if startDate after discontinuedDate
	 * @should fail validation if startDate after autoExpireDate
	 * @should fail validation if discontinued but date is null
	 * @should fail validation if discontinued but by is null
	 * @should fail validation if discontinued but reason is null
	 * @should fail validation if not discontinued but date is not null
	 * @should fail validation if not discontinued but by is not null
	 * @should fail validation if not discontinued but reason is not null
	 * @should fail validation if discontinuedDate in future
	 * @should fail validation if discontinuedDate after autoExpireDate
	 * @should pass validation if all fields are correct
	 */
	public void validate(Object obj, Errors errors) {
		Order order = (Order) obj;
		if (order == null) {
			errors.reject("error.general");
		} else {
			if (order.getEncounter() != null && order.getPatient() != null) {
				if (!order.getEncounter().getPatient().equals(order.getPatient())) {
					errors.rejectValue("encounter", "Order.error.encounterPatientMismatch");
				}
			}
			
			// for the following elements Order.hbm.xml says: not-null="true"
			ValidationUtils.rejectIfEmpty(errors, "discontinued", "error.null");
			ValidationUtils.rejectIfEmpty(errors, "voided", "error.null");
			ValidationUtils.rejectIfEmpty(errors, "concept", "Concept.noConceptSelected");
			ValidationUtils.rejectIfEmpty(errors, "patient", "error.null");
			
			Date startDate = order.getStartDate();
			if (startDate != null) {
				Date discontinuedDate = order.getDiscontinuedDate();
				if (discontinuedDate != null && startDate.after(discontinuedDate)) {
					errors.rejectValue("startDate", "Order.error.startDateAfterDiscontinuedDate");
					errors.rejectValue("discontinuedDate", "Order.error.startDateAfterDiscontinuedDate");
				}
				
				Date autoExpireDate = order.getAutoExpireDate();
				if (autoExpireDate != null && startDate.after(autoExpireDate)) {
					errors.rejectValue("startDate", "Order.error.startDateAfterAutoExpireDate");
					errors.rejectValue("autoExpireDate", "Order.error.startDateAfterAutoExpireDate");
				}
			}
			
			if (order.getDiscontinued() == null) {
				errors.rejectValue("discontinued", "error.null");
			} else if (order.getDiscontinued()) {
				ValidationUtils.rejectIfEmpty(errors, "discontinuedDate", "Order.error.discontinueNeedsDateAndPerson");
				ValidationUtils.rejectIfEmpty(errors, "discontinuedBy", "Order.error.discontinueNeedsDateAndPerson");
				ValidationUtils
				        .rejectIfEmptyOrWhitespace(errors, "discontinuedReason", "Order.error.discontinueNeedsReason");
				if (order.getDiscontinuedDate() != null) {
					// must be <= now(), and <= autoExpireDate
					if (OpenmrsUtil.compare(order.getDiscontinuedDate(), new Date()) > 0) {
						errors.rejectValue("discontinuedDate", "Order.error.discontinuedDateInFuture");
					}
					if (order.getAutoExpireDate() != null
					        && OpenmrsUtil.compare(order.getDiscontinuedDate(), order.getAutoExpireDate()) > 0) {
						errors.rejectValue("autoExpireDate", "Order.error.discontinuedAfterAutoExpireDate");
						errors.rejectValue("discontinuedDate", "Order.error.discontinuedAfterAutoExpireDate");
					}
				}
			} else if (!order.getDiscontinued()) {
				if (order.getDiscontinuedDate() != null) {
					errors.rejectValue("discontinuedDate", "Order.error.notDiscontinuedShouldHaveNoDate");
				}
				
				if (order.getDiscontinuedBy() != null) {
					errors.rejectValue("discontinuedBy", "Order.error.notDiscontinuedShouldHaveNoBy");
				}
				
				if (order.getDiscontinuedReason() != null) {
					errors.rejectValue("discontinuedReason", "Order.error.notDiscontinuedShouldHaveNoReason");
				}
			}
		}
	}
}
