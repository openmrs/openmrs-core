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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.reporting.AbstractReportObject;
import org.openmrs.reporting.ReportObject;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Validates attributes on the {@link ReportObject} object.
 * 
 * @since 1.5
 * @deprecated see reportingcompatibility module
 */
@Deprecated
public class ReportObjectValidator implements Validator {
	
	/** Log for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * Determines if the command object being submitted is a valid type
	 * 
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public boolean supports(Class c) {
		//String clsName = c.getName();
		// could also check if superclass is an AbstractReportObject
		return c.getSuperclass().equals(AbstractReportObject.class);
		//return Helper.isStringInArray(clsName, ReportObjectService.getAllReportObjectClasses());
	}
	
	/**
	 * Checks the form object for any inconsistencies/errors
	 * 
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 */
	public void validate(Object obj, Errors errors) {
		
		if (obj instanceof AbstractReportObject) {
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "error.name");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "type", "error.reportObject.type.required");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "subType", "error.reportObject.subType.required");
		} else {
			errors.rejectValue("report object", "error.general");
		}
	}
	
}
