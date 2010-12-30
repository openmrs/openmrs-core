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

import org.openmrs.PersonAttributeType;
import org.openmrs.annotation.Handler;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Validates the {@link PersonAttributeType} class.
 * 
 * @since 1.5
 */
@Handler(supports = { PersonAttributeType.class }, order = 50)
public class PersonAttributeTypeValidator implements Validator {
	
	/**
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public boolean supports(Class c) {
		return PersonAttributeType.class.isAssignableFrom(c);
	}
	
	/**
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 * @should fail validation if name is null
	 * @should fail validation if name already in use
	 * @should pass validation if all fields are correct
	 */
	public void validate(Object obj, Errors errors) {
		PersonAttributeType patObj = (PersonAttributeType) obj;
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "PersonAttributeType.error.nameEmpty");
		
		PersonService ps = Context.getPersonService();
		PersonAttributeType pat = ps.getPersonAttributeTypeByName(patObj.getName());
		if (pat != null && !pat.getPersonAttributeTypeId().equals(patObj.getPersonAttributeTypeId())) {
			errors.rejectValue("name", "PersonAttributeType.error.nameAlreadyInUse");
		}
	}
}
