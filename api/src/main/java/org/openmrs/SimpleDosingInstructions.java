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
package org.openmrs;

import java.util.Date;
import java.util.Locale;

import org.openmrs.api.APIException;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

/**
 * @since 1.10
 */
public class SimpleDosingInstructions implements DosingInstructions {
	
	private Double dose;
	
	private Concept doseUnits;
	
	private Concept route;
	
	private OrderFrequency frequency;
	
	private Integer duration;
	
	private Concept durationUnits;
	
	private Boolean asNeeded;
	
	private String asNeededCondition;
	
	private String administrationInstructions;
	
	/**
	 * @see DosingInstructions#getDosingInstructionsAsString(java.util.Locale)
	 */
	@Override
	public String getDosingInstructionsAsString(Locale locale) {
		StringBuilder dosingInstructions = new StringBuilder();
		dosingInstructions.append(this.dose);
		dosingInstructions.append(" ");
		dosingInstructions.append(this.doseUnits.getName(locale).getName());
		dosingInstructions.append(" ");
		dosingInstructions.append(this.route.getName(locale).getName());
		dosingInstructions.append(" ");
		dosingInstructions.append(this.frequency);
		if (duration != null) {
			dosingInstructions.append(" ");
			dosingInstructions.append(this.duration);
			dosingInstructions.append(" ");
			dosingInstructions.append(this.durationUnits.getName(locale).getName());
		}
		if (this.asNeeded) {
			dosingInstructions.append(" ");
			dosingInstructions.append("PRN");
			if (this.asNeededCondition != null) {
				dosingInstructions.append(" ");
				dosingInstructions.append(this.asNeededCondition);
			}
		}
		if (this.administrationInstructions != null) {
			dosingInstructions.append(" ");
			dosingInstructions.append(this.administrationInstructions);
		}
		return dosingInstructions.toString();
	}
	
	/**
	 * @see DosingInstructions#setDosingInstructions(DrugOrder)
	 */
	@Override
	public void setDosingInstructions(DrugOrder order) {
		order.setDosingType(this.getClass());
		order.setDose(this.dose);
		order.setDoseUnits(this.doseUnits);
		order.setRoute(this.route);
		order.setFrequency(this.frequency);
		order.setDuration(this.duration);
		order.setDurationUnits(this.durationUnits);
		order.setAsNeeded(this.asNeeded);
		order.setAsNeededCondition(this.asNeededCondition);
		order.setDosingInstructions(this.administrationInstructions);
	}
	
	/**
	 * @see DosingInstructions#getDosingInstructions(DrugOrder)
	 */
	@Override
	public DosingInstructions getDosingInstructions(DrugOrder order) {
		if (!order.getDosingType().equals(this.getClass())) {
			throw new APIException("Dosing type of drug order is mismatched. Expected:" + this.getClass().getName()
			        + " but received:" + order.getDosingType());
		}
		SimpleDosingInstructions simpleDosingInstructions = new SimpleDosingInstructions();
		simpleDosingInstructions.setDose(order.getDose());
		simpleDosingInstructions.setDoseUnits(order.getDoseUnits());
		simpleDosingInstructions.setRoute(order.getRoute());
		simpleDosingInstructions.setFrequency(order.getFrequency());
		simpleDosingInstructions.setDuration(order.getDuration());
		simpleDosingInstructions.setDurationUnits(order.getDurationUnits());
		simpleDosingInstructions.setAsNeeded(order.getAsNeeded());
		simpleDosingInstructions.setAsNeededCondition(order.getAsNeededCondition());
		simpleDosingInstructions.setAdministrationInstructions(order.getDosingInstructions());
		return simpleDosingInstructions;
	}
	
	@Override
	public void validate(DrugOrder order, Errors errors) {
		ValidationUtils.rejectIfEmpty(errors, "dose", "DrugOrder.error.doseIsNullForDosingTypeSimple");
		ValidationUtils.rejectIfEmpty(errors, "doseUnits", "DrugOrder.error.doseUnitsIsNullForDosingTypeSimple");
		ValidationUtils.rejectIfEmpty(errors, "route", "DrugOrder.error.routeIsNullForDosingTypeSimple");
		ValidationUtils.rejectIfEmpty(errors, "frequency", "DrugOrder.error.frequencyIsNullForDosingTypeSimple");
		if (order.getAutoExpireDate() == null && order.getDurationUnits() != null) {
			if (ISO8601Duration.getCode(order.getDurationUnits()) == null) {
				errors.rejectValue("durationUnits", "DrugOrder.error.durationUnitsNotMappedToISO8601DurationCode");
			}
		}
	}
	
	/**
	 * @see DosingInstructions#getAutoExpireDate(DrugOrder)
	 */
	@Override
	public Date getAutoExpireDate(DrugOrder drugOrder) {
		if (drugOrder.getDuration() == null || drugOrder.getDurationUnits() == null) {
			return null;
		}
		if (drugOrder.getNumRefills() != null && drugOrder.getNumRefills() > 0) {
			return null;
		}
		String durationCode = ISO8601Duration.getCode(drugOrder.getDurationUnits());
		if (durationCode == null) {
			return null;
		}
		ISO8601Duration iso8601Duration = new ISO8601Duration(drugOrder.getDuration(), durationCode);
		return iso8601Duration.addToDate(drugOrder.getEffectiveStartDate(), drugOrder.getFrequency());
	}
	
	public Double getDose() {
		return dose;
	}
	
	public void setDose(Double dose) {
		this.dose = dose;
	}
	
	public Concept getDoseUnits() {
		return doseUnits;
	}
	
	public void setDoseUnits(Concept doseUnits) {
		this.doseUnits = doseUnits;
	}
	
	public Concept getRoute() {
		return route;
	}
	
	public void setRoute(Concept route) {
		this.route = route;
	}
	
	public OrderFrequency getFrequency() {
		return frequency;
	}
	
	public void setFrequency(OrderFrequency frequency) {
		this.frequency = frequency;
	}
	
	public Integer getDuration() {
		return duration;
	}
	
	public void setDuration(Integer duration) {
		this.duration = duration;
	}
	
	public Concept getDurationUnits() {
		return durationUnits;
	}
	
	public void setDurationUnits(Concept durationUnits) {
		this.durationUnits = durationUnits;
	}
	
	public Boolean getAsNeeded() {
		return asNeeded;
	}
	
	public void setAsNeeded(Boolean asNeeded) {
		this.asNeeded = asNeeded;
	}
	
	public String getAsNeededCondition() {
		return asNeededCondition;
	}
	
	public void setAsNeededCondition(String asNeededCondition) {
		this.asNeededCondition = asNeededCondition;
	}
	
	public String getAdministrationInstructions() {
		return administrationInstructions;
	}
	
	public void setAdministrationInstructions(String administrationInstructions) {
		this.administrationInstructions = administrationInstructions;
	}
}
