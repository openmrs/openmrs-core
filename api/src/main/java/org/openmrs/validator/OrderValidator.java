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
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.annotation.Handler;
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
	 * @should fail validation if voided is null
	 * @should fail validation if concept is null
	 * @should fail validation if patient is null
	 * @should fail validation if encounter is null
	 * @should fail validation if orderer is null
	 * @should fail validation if urgency is null
	 * @should fail validation if action is null
	 * @should fail validation if dateActivated after dateStopped
	 * @should fail validation if dateActivated after autoExpireDate
	 * @should fail validation if dateActivated is before encounter's encounterDatetime
	 * @should fail validation if scheduledDate is set and urgency is not set as ON_SCHEDULED_DATE
	 * @should fail validation if scheduledDate is null when urgency is ON_SCHEDULED_DATE
	 * @should fail validation if orderType.javaClass does not match order.class
	 * @should pass validation if the class of the order is a subclass of orderType.javaClass
	 * @should pass validation if all fields are correct
	 * @should not allow a future dateActivated
	 */
	public void validate(Object obj, Errors errors) {
		Order order = (Order) obj;
		if (order == null) {
			errors.reject("error.general");
		} else {
			// for the following elements Order.hbm.xml says: not-null="true"
			ValidationUtils.rejectIfEmpty(errors, "voided", "error.null");
			//For DrugOrders, the api will set the concept to drug.concept
			if (!DrugOrder.class.isAssignableFrom(order.getClass())) {
				ValidationUtils.rejectIfEmpty(errors, "concept", "Concept.noConceptSelected");
			}
			ValidationUtils.rejectIfEmpty(errors, "patient", "error.null");
			ValidationUtils.rejectIfEmpty(errors, "encounter", "error.null");
			ValidationUtils.rejectIfEmpty(errors, "orderer", "error.null");
			ValidationUtils.rejectIfEmpty(errors, "urgency", "error.null");
			ValidationUtils.rejectIfEmpty(errors, "action", "error.null");
			
			validateSamePatientInOrderAndEncounter(order, errors);
			validateOrderTypeClass(order, errors);
			validateDateActivated(order, errors);
			validateScheduledDate(order, errors);
		}
	}
	
	private void validateOrderTypeClass(Order order, Errors errors) {
		OrderType orderType = order.getOrderType();
		if (orderType != null && !orderType.getJavaClass().isAssignableFrom(order.getClass())) {
			errors.rejectValue("orderType", "Order.error.orderTypeClassMismatchesOrderClass");
		}
	}
	
	private void validateDateActivated(Order order, Errors errors) {
		Date dateActivated = order.getDateActivated();
		if (dateActivated != null) {
			if (dateActivated.after(new Date())) {
				errors.rejectValue("dateActivated", "Order.error.dateActivatedInFuture");
				return;
			}
			Date dateStopped = order.getDateStopped();
			if (dateStopped != null && dateActivated.after(dateStopped)) {
				errors.rejectValue("dateActivated", "Order.error.dateActivatedAfterDiscontinuedDate");
				errors.rejectValue("dateStopped", "Order.error.dateActivatedAfterDiscontinuedDate");
			}
			Date autoExpireDate = order.getAutoExpireDate();
			if (autoExpireDate != null && dateActivated.after(autoExpireDate)) {
				errors.rejectValue("dateActivated", "Order.error.dateActivatedAfterAutoExpireDate");
				errors.rejectValue("autoExpireDate", "Order.error.dateActivatedAfterAutoExpireDate");
			}
			Encounter encounter = order.getEncounter();
			if (encounter != null && encounter.getEncounterDatetime() != null
			        && encounter.getEncounterDatetime().after(dateActivated)) {
				errors.rejectValue("dateActivated", "Order.error.dateActivatedAfterEncounterDatetime");
			}
		}
	}
	
	private void validateSamePatientInOrderAndEncounter(Order order, Errors errors) {
		if (order.getEncounter() != null && order.getPatient() != null) {
			if (!order.getEncounter().getPatient().equals(order.getPatient()))
				errors.rejectValue("encounter", "Order.error.encounterPatientMismatch");
		}
	}
	
	private void validateScheduledDate(Order order, Errors errors) {
		boolean isUrgencyOnScheduledDate = (order.getUrgency() != null && order.getUrgency().equals(
		    Order.Urgency.ON_SCHEDULED_DATE));
		if (order.getScheduledDate() != null && !isUrgencyOnScheduledDate) {
			errors.rejectValue("urgency", "Order.error.urgencyNotOnScheduledDate");
		}
		if (isUrgencyOnScheduledDate && order.getScheduledDate() == null) {
			errors.rejectValue("scheduledDate", "Order.error.scheduledDateNullForOnScheduledDateUrgency");
		}
	}
}
