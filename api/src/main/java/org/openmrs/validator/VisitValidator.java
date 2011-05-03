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

import org.openmrs.Visit;
import org.openmrs.VisitAttribute;
import org.openmrs.VisitAttributeType;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Validator for the {@link Visit} class.
 * @since 1.9
 */
@Handler(supports = { Visit.class }, order = 50)
public class VisitValidator implements Validator {
	
	/**
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		return Visit.class.isAssignableFrom(clazz);
	}
	
	/**
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 * @should accept a visit that has the right number of attribute occurrences
	 * @should reject a visit if it has fewer than min occurs of an attribute
	 * @should reject a visit if it has more than max occurs of an attribute
	 * @should fail if patient is not set
	 * @should fail if visit type is not set
	 * @should fail if startDatetime is not set
	 * @should fail if the endDatetime is before the startDatetime
	 */
	@Override
	public void validate(Object target, Errors errors) {
		Visit visit = (Visit) target;
		ValidationUtils.rejectIfEmpty(errors, "patient", "Visit.error.patient.required");
		ValidationUtils.rejectIfEmpty(errors, "visitType", "Visit.error.visitType.required");
		ValidationUtils.rejectIfEmpty(errors, "startDatetime", "Visit.error.startDate.required");
		if (visit.getStartDatetime() != null
		        && OpenmrsUtil.compareWithNullAsLatest(visit.getStartDatetime(), visit.getStopDatetime()) > 0) {
			errors.rejectValue("stopDatetime", "Visit.error.endDateBeforeStartDate");
		}
		
		for (VisitAttributeType vat : Context.getVisitService().getAllVisitAttributeTypes()) {
			if (vat.getMinOccurs() > 0 || vat.getMaxOccurs() != null) {
				int numFound = 0;
				for (VisitAttribute attr : visit.getActiveAttributes()) {
					if (attr.getAttributeType().equals(vat))
						++numFound;
				}
				if (vat.getMinOccurs() > 0) {
					if (numFound < vat.getMinOccurs()) {
						// report an error
						if (vat.getMinOccurs() == 1)
							errors.rejectValue("activeAttributes", "error.required", new Object[] { vat.getName() }, null);
						else
							errors.rejectValue("activeAttributes", "attribute.error.minOccurs", new Object[] {
							        vat.getName(), vat.getMinOccurs() }, null);
					}
				}
				if (vat.getMaxOccurs() != null) {
					if (numFound > vat.getMaxOccurs()) {
						errors.rejectValue("activeAttributes", "attribute.error.maxOccurs", new Object[] { vat.getName(),
						        vat.getMaxOccurs() }, null);
					}
				}
			}
		}
	}
	
}
