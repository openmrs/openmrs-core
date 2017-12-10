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

import org.openmrs.ConceptNameTag;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Validates attributes on the {@link ConceptNameTag} object.
 *
 * @since 1.10
 */
@Handler(supports = { ConceptNameTag.class }, order = 50)
public class ConceptNameTagValidator implements Validator {

	/**
	 * Determines if the command object being submitted is a valid type
	 *
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> c) {
		return ConceptNameTag.class.isAssignableFrom(c);
	}
	
	/**
	 * Checks the form object for any inconsistencies/errors
	 *
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 * @should fail validation if conceptNameTag is null
	 * @should fail validation if tag is null or empty or whitespace
	 * @should pass validation if tag does not exist and is not null, empty or whitespace
	 * @should fail if the concept name tag is a duplicate
	 * @should pass validation if field lengths are correct
	 * @should fail validation if field lengths are not correct
	 */
	
	@Override
	public void validate(Object obj, Errors errors) {
		ConceptNameTag cnt = (ConceptNameTag) obj;
		if (cnt == null) {
			throw new IllegalArgumentException("The parameter obj should not be null");
		} else {
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "tag", "error.name");
			
			if (cnt.getTag() != null) {
				ConceptNameTag currentTag = Context.getConceptService().getConceptNameTagByName(cnt.getTag());
				if (currentTag != null && !OpenmrsUtil.nullSafeEqualsIgnoreCase(cnt.getUuid(), currentTag.getUuid())
				        && OpenmrsUtil.nullSafeEqualsIgnoreCase(currentTag.getTag(), cnt.getTag())) {
					errors.rejectValue("tag", "Concept.name.tag.duplicate");
				}
			}
			ValidateUtil.validateFieldLengths(errors, obj.getClass(), "tag", "voidReason");
		}
	}
}
