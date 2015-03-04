/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
			throw new APIException("DrugOrder.error.dosingTypeIsMismatched", new Object[] { this.getClass(),
			        order.getDosingType() });
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
