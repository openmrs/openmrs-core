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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.EncounterRole;
import org.openmrs.annotation.Handler;
import org.openmrs.api.APIException;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Validator for {@link org.openmrs.EncounterRole} class
 *
 * @since 1.9
 */
@Handler(supports = { EncounterRole.class }, order = 50)
public class EncounterRoleValidator implements Validator {
	
	private static final Log log = LogFactory.getLog(EncounterRoleValidator.class);
	
	/**
	 * Returns whether or not this validator supports validating a given class.
	 *
	 * @param c The class to check for support.
	 * @see org.springframework.validation.Validator#supports(Class)
	 */
	@SuppressWarnings("rawtypes")
	public boolean supports(Class c) {
		if (log.isDebugEnabled())
			log.debug(this.getClass().getName() + ".supports: " + c.getName());
		return EncounterRole.class.isAssignableFrom(c);
	}
	
	/**
	 * Validates the given EncounterRole. Currently checks if name
	 * of the encounter role is given or not.
	 * @param obj The encounter role to validate.
	 * @param errors Errors
	 * @see org.springframework.validation.Validator#validate(Object,
	 *      org.springframework.validation.Errors)
	 * @should fail if the name of the encounter role is not set
	 */
	public void validate(Object obj, Errors errors) throws APIException {
		if (log.isDebugEnabled())
			log.debug(this.getClass().getName() + ".validate...");
		
		if (obj == null || !(obj instanceof EncounterRole))
			throw new IllegalArgumentException("The parameter obj should not be null and must be of type "
			        + EncounterRole.class);
		
		EncounterRole encounterRole = (EncounterRole) obj;
		
		if (encounterRole != null) {
			ValidationUtils.rejectIfEmpty(errors, "name", "error.null");
		}
	}
}
