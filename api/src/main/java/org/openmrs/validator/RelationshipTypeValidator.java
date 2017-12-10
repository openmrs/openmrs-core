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

import org.openmrs.RelationshipType;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Validates a {@link RelationshipType} object.
 * 
 * @since 1.10
 */
@Handler(supports = { RelationshipType.class }, order = 50)
public class RelationshipTypeValidator implements Validator {
	
	/**
	 * Determines if the command object being submitted is a valid type
	 * 
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> c) {
		return RelationshipType.class.isAssignableFrom(c);
	}
	
	/**
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 * @should fail validation if aIsToB(or A is To B) is null or empty or whitespace
	 * @should fail validation if bIsToA(or B is To A) is null or empty or whitespace
	 * @should fail validation if description is null or empty or whitespace
	 * @should pass validation if all required fields are set
	 * @should fail validation if relationshipTypeName already exist
	 * @should pass validation if field lengths are correct
	 * @should fail validation if field lengths are not correct
	 */
	@Override
	public void validate(Object obj, Errors errors) {
		RelationshipType relationshipType = (RelationshipType) obj;
		if (relationshipType == null) {
			errors.rejectValue("relationshipType", "error.general");
		} else {
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "aIsToB", "RelationshipType.aIsToB.required");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "bIsToA", "RelationshipType.bIsToA.required");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "description", "RelationshipType.description.required");
			RelationshipType exist = Context.getPersonService().getRelationshipTypeByName(
			    relationshipType.getaIsToB() + "/" + relationshipType.getbIsToA());
			if (exist != null && !exist.getRetired()
			        && !OpenmrsUtil.nullSafeEquals(relationshipType.getUuid(), exist.getUuid())) {
				errors.reject("duplicate.relationshipType");
			}
			ValidateUtil.validateFieldLengths(errors, obj.getClass(), "aIsToB", "bIsToA", "description", "retireReason");
		}
	}
}
