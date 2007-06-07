package org.openmrs.web.controller.report;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.reporting.AbstractReportObject;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class ReportObjectValidator implements Validator {

	/** Log for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());

	/**
	 * 
	 * Determines if the command object being submitted is a valid type
	 * 
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	public boolean supports(Class c) {
		//String clsName = c.getName();
		// could also check if superclass is an AbstractReportObject
		return c.getSuperclass().equals(AbstractReportObject.class); 
		//return Helper.isStringInArray(clsName, ReportService.getAllReportObjectClasses());
	}

	
	/**
	 * 
	 * Checks the form object for any inconsistencies/errors
	 * 
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	public void validate(Object obj, Errors errors) {
		
		if ( obj instanceof AbstractReportObject ) {
			AbstractReportObject reportObject = (AbstractReportObject)obj;
			if (reportObject == null) {
				errors.rejectValue("report object", "error.general");
			} else {
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "error.name");
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "type", "error.reportObject.type.required");
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "subType", "error.reportObject.subType.required");
			}
		} else {
			errors.rejectValue("report object", "error.general");
		}
	}

}
