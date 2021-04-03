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

import java.util.Date;

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
	
	/**
	 * Determines if the command object being submitted is a valid type
	 * 
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> c) {
		return Order.class.isAssignableFrom(c);
	}
	
	/**
	 * Checks the form object for any inconsistencies/errors
	 * 
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 * <strong>Should</strong> fail validation if order is null
	 * <strong>Should</strong> fail validation if order and encounter have different patients
	 * <strong>Should</strong> fail validation if voided is null
	 * <strong>Should</strong> fail validation if concept is null
	 * <strong>Should</strong> fail validation if patient is null
	 * <strong>Should</strong> fail validation if encounter is null
	 * <strong>Should</strong> fail validation if orderer is null
	 * <strong>Should</strong> fail validation if urgency is null
	 * <strong>Should</strong> fail validation if action is null
	 * <strong>Should</strong> fail validation if dateActivated after dateStopped
	 * <strong>Should</strong> fail validation if dateActivated after autoExpireDate
	 * <strong>Should</strong> fail validation if dateActivated is before encounter's encounterDatetime
	 * <strong>Should</strong> fail validation if scheduledDate is set and urgency is not set as ON_SCHEDULED_DATE
	 * <strong>Should</strong> fail validation if scheduledDate is null when urgency is ON_SCHEDULED_DATE
	 * <strong>Should</strong> fail validation if orderType.javaClass does not match order.class
	 * <strong>Should</strong> pass validation if the class of the order is a subclass of orderType.javaClass
	 * <strong>Should</strong> pass validation if all fields are correct
	 * <strong>Should</strong> not allow a future dateActivated
	 * <strong>Should</strong> pass validation if field lengths are correct
	 * <strong>Should</strong> fail validation if field lengths are not correct
	 */
	@Override
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
			ValidateUtil.validateFieldLengths(errors, obj.getClass(), "orderReasonNonCoded", "accessionNumber",
			    "commentToFulfiller", "voidReason");
			
			validateOrderGroupEncounter(order, errors);
			validateOrderGroupPatient(order, errors);
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
				errors.rejectValue("dateActivated", "Order.error.encounterDatetimeAfterDateActivated");
			}
		}
	}
	
	private void validateSamePatientInOrderAndEncounter(Order order, Errors errors) {
		if (order.getEncounter() != null && order.getPatient() != null
				&& !order.getEncounter().getPatient().equals(order.getPatient())) {
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
	
	private void validateOrderGroupEncounter(Order order, Errors errors) {
		if (order.getOrderGroup() != null && !(order.getEncounter().equals(order.getOrderGroup().getEncounter()))) {
			errors.rejectValue("encounter", "Order.error.orderEncounterAndOrderGroupEncounterMismatch");
		}
	}
	
	private void validateOrderGroupPatient(Order order, Errors errors) {
		if (order.getOrderGroup() != null && !(order.getPatient().equals(order.getOrderGroup().getPatient()))) {
			errors.rejectValue("patient", "Order.error.orderPatientAndOrderGroupPatientMismatch");
		}
	}
}
