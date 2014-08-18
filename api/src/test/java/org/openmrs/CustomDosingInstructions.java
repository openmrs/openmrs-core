package org.openmrs;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

import java.util.Date;
import java.util.Locale;

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
