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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.ImplementationId;
import org.openmrs.annotation.Handler;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Validates attributes on the {@link ImplementationId} object.
 *
 * @should should fail validation if name is null
 * @should should fail validation if implementation id is null
 * @should should fail validation if description is null
 * @should should fail validation if pass phrase is null
 * @should should fail if given empty implementationId object
 * @should should fail if given a pipe in the implementationId code
 * 
 */
@Handler(supports = { ImplementationId.class }, order = 50)
public class ImplementationIdValidator implements Validator {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	@Override
	public boolean supports(Class<?> clazz) {
		return clazz.equals(ImplementationId.class);
	}
	
	@Override
	public void validate(Object obj, Errors errors) {
		ImplementationId implId = (ImplementationId) obj;
		char[] illegalChars = { '^', '|' };
		if (implId == null) {
			errors.rejectValue("implementationId", "error.general");
		} else {
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "ImplementationId.name.empty");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "implementationId", "ImplementationId.implementationId.empty");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "passphrase", "ImplementationId.passphrase.empty");
			if (implId.getImplementationId() != null && StringUtils.containsAny(implId.getImplementationId(), illegalChars)) {
				errors.rejectValue("implementationId", "ImplementationId.implementationId.invalidcharacter");
			}
		}
	}
}
