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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.ImplementationId;
import org.openmrs.annotation.Handler;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Validates attributes on the {@link ImplementationId} object.
 * 
 */
@Handler(supports = { ImplementationId.class }, order = 50)
public class ImplementationIdValidator implements Validator {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	@Override
	public boolean supports(Class<?> clazz) {
		return clazz.equals(ImplementationId.class);
	}
	
	/*
	 * @should should fail validation if implementation id is null
	 * @should should fail validation if description is null
	 * @should should fail validation if pass phrase is null
	 * @should should fail if given empty implementationId object
	 * @should should fail if given a pipe in the implementationId code
	 * 
	 */

	@Override
	public void validate(Object obj, Errors errors) throws APIException {
		ImplementationId implId = (ImplementationId) obj;
		char[] illegalChars = { '^', '|' };
		if (implId == null) {
			throw new APIException(Context.getMessageSourceService().getMessage("ImplementationId.null"));
		} else {
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "ImplementationId.name.empty");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "implementationId", "ImplementationId.implementationId.empty");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "passphrase", "ImplementationId.passphrase.empty");
			if (implId.getImplementationId() != null && StringUtils.containsAny(implId.getImplementationId(), illegalChars)) {
				errors.rejectValue("implementationId", "ImplementationId.implementationId.invalidCharacter");
			}
		}
	}
}
