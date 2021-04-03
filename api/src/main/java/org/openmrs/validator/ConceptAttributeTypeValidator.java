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

import org.openmrs.ConceptAttributeType;
import org.openmrs.annotation.Handler;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

/**
 * Validates attributes on the {@link ConceptAttributeType} object.
 *
 * @since 2.0
 */
@Handler(supports = {ConceptAttributeType.class}, order = 50)
public class ConceptAttributeTypeValidator extends BaseAttributeTypeValidator<ConceptAttributeType> {

    /**
     * Determines if the command object being submitted is a valid type
     *
     * @see org.springframework.validation.Validator#T)
     * <strong>Should</strong> pass validation if field lengths are correct
     * <strong>Should</strong> fail validation if field lengths are not correct
     */
    @Override
	public boolean supports(Class<?> c) {
        return ConceptAttributeType.class.isAssignableFrom(c);
    }

    @Override
	public void validate(Object obj, Errors errors) {
        super.validate(obj, errors);
        ConceptAttributeType conceptAttributeType = (ConceptAttributeType) obj;
        ConceptService conceptService = Context.getConceptService();
        if (conceptAttributeType.getName() != null && !conceptAttributeType.getName().isEmpty()) {
            ConceptAttributeType attributeType = conceptService.getConceptAttributeTypeByName(conceptAttributeType.getName());
            if (attributeType != null && !attributeType.getUuid().equals(conceptAttributeType.getUuid())) {
                errors.rejectValue("name", "ConceptAttributeType.error.nameAlreadyInUse");
            }
        } else {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "ConceptAttributeType.error.nameEmpty");
        }

        ValidateUtil.validateFieldLengths(errors, obj.getClass(), "name", "description", "datatypeClassname",
                "preferredHandlerClassname", "retireReason");
    }
}
