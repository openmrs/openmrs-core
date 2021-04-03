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

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

public class CustomDosingInstructions implements DosingInstructions {
	
	@Override
	public String getDosingInstructionsAsString(Locale locale) {
		return null;
	}
	
	@Override
	public void setDosingInstructions(DrugOrder order) {
		
	}
	
	@Override
	public DosingInstructions getDosingInstructions(DrugOrder order) {
		return this;
	}
	
	@Override
	public void validate(DrugOrder order, Errors errors) {
		ValidationUtils.rejectIfEmpty(errors, "brandName", "DrugOrder.error.brandNameIsNull");
	}
	
	@Override
	public Date getAutoExpireDate(DrugOrder order) {
		return null;
	}
}
