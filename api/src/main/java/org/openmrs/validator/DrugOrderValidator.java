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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.CareSetting;
import org.openmrs.Concept;
import org.openmrs.DosingInstructions;
import org.openmrs.DrugOrder;
import org.openmrs.Duration;
import org.openmrs.Order;
import org.openmrs.annotation.Handler;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Validates the {@link DrugOrder} class.
 * 
 * @since 1.5
 */
@Handler(supports = { DrugOrder.class }, order = 50)
public class DrugOrderValidator extends OrderValidator implements Validator {
	
	/** Log for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * Determines if the command object being submitted is a valid type
	 * 
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public boolean supports(Class c) {
		return DrugOrder.class.isAssignableFrom(c);
	}
	
	/**
	 * Checks the form object for any inconsistencies/errors
	 * 
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 * @should fail validation if asNeeded is null
	 * @should fail validation if dosingType is null
	 * @should fail validation if drug concept is different from order concept
	 * @should fail validation if dose is null for SimpleDosingInstructions dosingType
	 * @should fail validation if doseUnits is null for SimpleDosingInstructions dosingType
	 * @should fail validation if route is null for SimpleDosingInstructions dosingType
	 * @should fail validation if frequency is null for SimpleDosingInstructions dosingType
	 * @should fail validation if dosingInstructions is null for FreeTextDosingInstructions
	 *         dosingType
	 * @should fail validation if numberOfRefills is null for outpatient careSetting
	 * @should fail validation if quantity is null for outpatient careSetting
	 * @should fail validation if doseUnits is null when dose is present
	 * @should fail validation if doseUnits is not a dose unit concept
	 * @should fail validation if quantityUnits is null when quantity is present
	 * @should fail validation if quantityUnits it not a quantity unit concept
	 * @should fail validation if durationUnits is null when duration is present
	 * @should fail validation if durationUnits is not a duration unit concept
	 * @should pass validation if all fields are correct
	 * @should not require all fields for a discontinuation order
	 * @should fail if route is not a valid concept
	 * @should fail if concept is null and drug is not specified
	 * @should fail if concept is null and cannot infer it from drug
	 * @should pass if concept is null and drug is set
	 * @should not validate a custom dosing type against any other dosing type validation
	 * @should apply validation for a custom dosing type
	 * @should pass validation if field lengths are correct
	 * @should fail validation if field lengths are not correct
	 */
	public void validate(Object obj, Errors errors) {
		super.validate(obj, errors);
		
		DrugOrder order = (DrugOrder) obj;
		if (order == null) {
			errors.reject("error.general");
		} else {
			// for the following elements Order.hbm.xml says: not-null="true"
			ValidationUtils.rejectIfEmpty(errors, "asNeeded", "error.null");
			if (order.getAction() != Order.Action.DISCONTINUE) {
				ValidationUtils.rejectIfEmpty(errors, "dosingType", "error.null");
			}
			if (order.getDrug() == null || order.getDrug().getConcept() == null) {
				ValidationUtils.rejectIfEmpty(errors, "concept", "error.null");
			}
			
			if (order.getConcept() != null && order.getDrug() != null && order.getDrug().getConcept() != null
			        && !order.getDrug().getConcept().equals(order.getConcept())) {
				errors.rejectValue("drug", "error.general");
				errors.rejectValue("concept", "error.concept");
			}
			if (order.getAction() != Order.Action.DISCONTINUE && order.getDosingType() != null) {
				DosingInstructions dosingInstructions = order.getDosingInstructionsInstance();
				dosingInstructions.validate(order, errors);
			}
			validateFieldsForOutpatientCareSettingType(order, errors);
			validatePairedFields(order, errors);
			validateUnitsAreAmongAllowedConcepts(errors, order);
			validateForRequireDrug(errors);
			ValidateUtil.validateFieldLengths(errors, obj.getClass(), "asNeededCondition", "brandName");
		}
	}
	
	private void validateForRequireDrug(Errors errors) {
		//Reject if global property is set to specify a formulation for drug order
		boolean requireDrug = Context.getAdministrationService().getGlobalPropertyValue(
		    OpenmrsConstants.GLOBAL_PROPERTY_DRUG_ORDER_REQUIRE_DRUG, false);
		if (requireDrug) {
			ValidationUtils.rejectIfEmpty(errors, "drug", "DrugOrder.error.drugIsRequired");
		}
	}
	
	private void validateFieldsForOutpatientCareSettingType(DrugOrder order, Errors errors) {
		if (order.getAction() != Order.Action.DISCONTINUE && order.getCareSetting() != null
		        && order.getCareSetting().getCareSettingType().equals(CareSetting.CareSettingType.OUTPATIENT)) {
			ValidationUtils.rejectIfEmpty(errors, "quantity", "DrugOrder.error.quantityIsNullForOutPatient");
			ValidationUtils.rejectIfEmpty(errors, "numRefills", "DrugOrder.error.numRefillsIsNullForOutPatient");
		}
	}
	
	private void validatePairedFields(DrugOrder order, Errors errors) {
		if (order.getDose() != null) {
			ValidationUtils.rejectIfEmpty(errors, "doseUnits", "DrugOrder.error.doseUnitsRequiredWithDose");
		}
		if (order.getQuantity() != null) {
			ValidationUtils.rejectIfEmpty(errors, "quantityUnits", "DrugOrder.error.quantityUnitsRequiredWithQuantity");
		}
		if (order.getDuration() != null) {
			ValidationUtils.rejectIfEmpty(errors, "durationUnits", "DrugOrder.error.durationUnitsRequiredWithDuration");
		}
	}
	
	private void validateUnitsAreAmongAllowedConcepts(Errors errors, DrugOrder order) {
		OrderService orderService = Context.getOrderService();
		if (order.getDoseUnits() != null) {
			List<Concept> drugDosingUnits = orderService.getDrugDosingUnits();
			if (!drugDosingUnits.contains(order.getDoseUnits())) {
				errors.rejectValue("doseUnits", "DrugOrder.error.notAmongAllowedConcepts");
			}
		}
		if (order.getQuantityUnits() != null) {
			List<Concept> drugDispensingUnits = orderService.getDrugDispensingUnits();
			if (!drugDispensingUnits.contains(order.getQuantityUnits())) {
				errors.rejectValue("quantityUnits", "DrugOrder.error.notAmongAllowedConcepts");
			}
		}
		if (order.getDurationUnits() != null) {
			List<Concept> drugDurationUnits = orderService.getDurationUnits();
			if (!drugDurationUnits.contains(order.getDurationUnits())) {
				errors.rejectValue("durationUnits", "DrugOrder.error.notAmongAllowedConcepts");
			}
			if (Duration.getCode(order.getDurationUnits()) == null) {
				errors.rejectValue("durationUnits", "DrugOrder.error.durationUnitsNotMappedToSnomedCtDurationCode");
			}
		}
		if (order.getRoute() != null) {
			List<Concept> routes = orderService.getDrugRoutes();
			if (!routes.contains(order.getRoute())) {
				errors.rejectValue("route", "DrugOrder.error.routeNotAmongAllowedConcepts");
			}
		}
	}
}
