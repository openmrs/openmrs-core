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
	@SuppressWarnings("unchecked")
	public boolean supports(Class c) {
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
	 * @should pass validation if all fields are correct
	 */
	public void validate(Object obj, Errors errors) {
		Order order = (Order) obj;
		if (order == null) {
			errors.rejectValue("order", "error.general");
		} else {
			if (order.getEncounter() != null && order.getPatient() != null) {
				if (!order.getEncounter().getPatient().equals(order.getPatient()))
					errors.rejectValue("encounter", "Order.error.encounterPatientMismatch");
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
			
			// As a temporary restriction, until we properly implement Draft orders, all persisted orders must be both signed and activated
			ValidationUtils.rejectIfEmpty(errors, "signedBy", "Order.error.mustBeSignedAndActivated");
			ValidationUtils.rejectIfEmpty(errors, "dateSigned", "Order.error.mustBeSignedAndActivated");
			ValidationUtils.rejectIfEmpty(errors, "activatedBy", "Order.error.mustBeSignedAndActivated");
			ValidationUtils.rejectIfEmpty(errors, "dateActivated", "Order.error.mustBeSignedAndActivated");
			
			if (order.getDiscontinued()) {
				ValidationUtils.rejectIfEmpty(errors, "discontinuedDate", "Order.error.discontinueNeedsDateAndPerson");
				ValidationUtils.rejectIfEmpty(errors, "discontinuedBy", "Order.error.discontinueNeedsDateAndPerson");
				if (order.getDiscontinuedDate() != null) {
					// must be >= activatedDate, <= now(), and <= autoExpireDate
					if (!order.isActivated())
						errors.rejectValue("discontinuedDate", "Order.error.discontinuedDateButNotActivated");
					else if (OpenmrsUtil.compare(order.getDiscontinuedDate(), order.getDateActivated()) < 0)
						errors.rejectValue("discontinuedDate", "Order.error.discontinuedDateBeforeActivated");
					if (OpenmrsUtil.compare(order.getDiscontinuedDate(), new Date()) > 0)
						errors.rejectValue("discontinuedDate", "Order.error.discontinuedDateInFuture");
					if (order.getAutoExpireDate() != null && OpenmrsUtil.compare(order.getDiscontinuedDate(), order.getAutoExpireDate()) > 0)
						errors.rejectValue("discontinuedDate", "Order.error.discontinuedAfterAutoExpireDate");
				}
			}
		}
	}
}
