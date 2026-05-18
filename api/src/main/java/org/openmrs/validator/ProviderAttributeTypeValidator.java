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

import org.openmrs.ProviderAttributeType;
import org.openmrs.annotation.Handler;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

/**
 * Validates attributes on the {@link ProviderAttributeType} object.
 *
 * @since 1.9
 */
@Handler(supports = { ProviderAttributeType.class }, order = 50)
public class ProviderAttributeTypeValidator extends BaseAttributeTypeValidator<ProviderAttributeType> {

	/**
	 * Determines if the command object being submitted is a valid type
	 * <p>
	 * <strong>Should</strong> pass validation if field lengths are correct<br/>
	 * <strong>Should</strong> fail validation if field lengths are not correct
	 *
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> c) {
		return ProviderAttributeType.class.isAssignableFrom(c);
	}

	/**
	 * <p>
	 * <strong>Should</strong> fail validation if name is null<br/>
	 * <strong>Should</strong> fail validation if datatypeClassname is empty<br/>
	 * <strong>Should</strong> fail validation if name already in use<br/>
	 * <strong>Should</strong> pass validation if description is null or empty or whitespace<br/>
	 * <strong>Should</strong> pass validation if all fields are correct<br/>
	 * <strong>Should</strong> pass validation if field lengths are correct
	 *
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors) <strong>NOTE</strong>: the current behaviour of the
	 *      name is that;- when you create an attribute with a name "test", you cannot create another
	 *      one with the same name not until you retire the first one. When you retire "test", you
	 *      create a new one with the name "test" since the existing one has been retired.
	 */
	@Override
	public void validate(Object obj, Errors errors) {
		if (obj != null) {
			super.validate(obj, errors);
			ValidateUtil.validateFieldLengths(errors, obj.getClass(), "name", "description", "datatypeClassname",
			    "preferredHandlerClassname", "retireReason");
			ProviderAttributeType type = (ProviderAttributeType) obj;
			ValidationUtils.rejectIfEmpty(errors, "name", "ProviderAttributeType.error.nameEmpty");
			ValidationUtils.rejectIfEmpty(errors, "datatypeClassname", "ProviderAttributeType.error.datatypeEmpty");
			ProviderService service = Context.getProviderService();
			ProviderAttributeType attributeType = service.getProviderAttributeTypeByName(type.getName());
			if (attributeType != null) {
				if (!attributeType.getUuid().equals(type.getUuid()) && !attributeType.getRetired()) {
					errors.rejectValue("name", "ProviderAttributeType.error.nameAlreadyInUse");
				}
			}
		}
	}
}
