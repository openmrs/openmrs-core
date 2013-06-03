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
import org.openmrs.Field;
import org.openmrs.annotation.Handler;
import org.openmrs.api.APIException;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Validator for {@link Field} class
 * 
 * @since 1.10
 */
@Handler(supports = { Field.class }, order = 50)
public class FieldValidator implements Validator {
	 
    public void supports(Object obj, Errors errors) {
        ConceptClass cc = (ConceptClass) obj;
    }
 
    public void validate(Object target, Errors errors) {
       ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "error.name");
 	ValidationUtils.rejectIfEmptyOrWhitespace(errors, "description", "error.description");

       if (!errors.hasFieldErrors("name"))
			{
				List<ConceptClass> ccs = Context.getConceptService().getAllConceptClasses();
				ValidateUtil.rejectIfDuplicateMetadataName(cc, ccs, errors, "name", "general.error.nameAlreadyInUse");
			}

    }
 }