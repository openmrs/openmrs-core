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

import org.openmrs.api.APIException;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

import java.util.Date;
import java.util.Locale;

/**
 * @since 1.10
 */
public class FreeTextDosingInstructions implements DosingInstructions {
	
	private String instructions;
	
	/**
	 * @see DosingInstructions#getDosingInstructions(DrugOrder)
	 */
	@Override
	public String getDosingInstructionsAsString(Locale locale) {
		return this.instructions;
	}
	
	/**
	 * @see DosingInstructions#setDosingInstructions(DrugOrder)
	 */
	@Override
	public void setDosingInstructions(DrugOrder order) {
		order.setDosingType(this.getClass());
		order.setDosingInstructions(this.getInstructions());
	}
	
	/**
	 * @see DosingInstructions#getDosingInstructions(DrugOrder)
	 */
	@Override
	public DosingInstructions getDosingInstructions(DrugOrder order) throws APIException {
		if (!order.getDosingType().equals(this.getClass())) {
			throw new APIException("Dosing type of drug order is mismatched. Expected:" + this.getClass() + " but received:"
			        + order.getDosingType());
		}
		FreeTextDosingInstructions freeTextDosingInstructions = new FreeTextDosingInstructions();
		freeTextDosingInstructions.setInstructions(order.getDosingInstructions());
		return freeTextDosingInstructions;
	}
	
	@Override
	public void validate(DrugOrder order, Errors errors) {
		ValidationUtils.rejectIfEmpty(errors, "dosingInstructions",
		    "DrugOrder.error.dosingInstructionsIsNullForDosingTypeFreeText");
		
	}
	
	@Override
	public Date getAutoExpireDate(DrugOrder order) {
		return null;
	}
	
	public String getInstructions() {
		return instructions;
	}
	
	public void setInstructions(String instructions) {
		this.instructions = instructions;
	}
	
}
