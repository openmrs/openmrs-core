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
import org.openmrs.attribute.AttributeType;
import org.openmrs.customdatatype.CustomDatatype;
import org.openmrs.customdatatype.CustomDatatypeHandler;
import org.openmrs.customdatatype.CustomDatatypeUtil;
import org.openmrs.customdatatype.datatype.RegexValidatedTextDatatype;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Abstract class which handles basic validation common to all attribute types
 *
 * @since 1.9
 */
public abstract class BaseAttributeTypeValidator<T extends AttributeType<?>> implements Validator {
	
	/** Log for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 * @should require name
	 * @should require minOccurs
	 * @should not allow maxOccurs less than 1
	 * @should not allow maxOccurs less than minOccurs
	 * @should require datatypeClassname
	 * @should require DatatypeConfiguration if Datatype equals Regex-Validated Text
	 * @should pass validation if all required values are set
	 * @should pass validation if field lengths are correct
	 * @should fail validation if field lengths are not correct
	 */
	@Override
	public void validate(Object target, Errors errors) {
		@SuppressWarnings("unchecked")
		T attributeType = (T) target;
		
		if (attributeType == null) {
			errors.reject("error.general");
		} else {
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "error.name");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "minOccurs", "error.null");
			
			Integer minOccurs = attributeType.getMinOccurs();
			Integer maxOccurs = attributeType.getMaxOccurs();
			
			if (minOccurs != null && minOccurs < 0) {
				errors.rejectValue("minOccurs", "AttributeType.minOccursShouldNotBeLessThanZero");
			}
			
			if (maxOccurs != null) {
				if (maxOccurs < 1) {
					errors.rejectValue("maxOccurs", "AttributeType.maxOccursShouldNotBeLessThanOne");
				} else if (maxOccurs < minOccurs) {
					errors.rejectValue("maxOccurs", "AttributeType.maxOccursShouldNotBeLessThanMinOccurs");
				}
			}
			
			if (StringUtils.isBlank(attributeType.getDatatypeClassname())) {
				errors.rejectValue("datatypeClassname", "error.null");
			} else {
				try {
					CustomDatatype<?> datatype = CustomDatatypeUtil.getDatatype(attributeType);
					if (datatype instanceof RegexValidatedTextDatatype
					        && StringUtils.isBlank(attributeType.getDatatypeConfig())) {
						errors.rejectValue("datatypeConfig", "error.null");
					}
				}
				catch (Exception ex) {
					errors.rejectValue("datatypeConfig", "AttributeType.datatypeConfig.invalid", new Object[] { ex
					        .getMessage() }, "Invalid");
				}
			}
			
			// ensure that handler is suitable for datatype
			if (StringUtils.isNotEmpty(attributeType.getPreferredHandlerClassname())) {
				try {
					CustomDatatype<?> datatype = CustomDatatypeUtil.getDatatype(attributeType);
					CustomDatatypeHandler<?, ?> handler = CustomDatatypeUtil.getHandler(attributeType);
					if (!CustomDatatypeUtil.isCompatibleHandler(handler, datatype)) {
						errors.rejectValue("preferredHandlerClassname",
						    "AttributeType.preferredHandlerClassname.wrongDatatype");
					}
				}
				catch (Exception ex) {
					errors.rejectValue("handlerConfig", "AttributeType.handlerConfig.invalid", new Object[] { ex
					        .getMessage() }, "Invalid");
				}
			}
			ValidateUtil.validateFieldLengths(errors, target.getClass(), "datatypeConfig", "handlerConfig");
		}
	}
	
}
