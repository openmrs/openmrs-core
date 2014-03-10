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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.openmrs.api.ValidationException;
import org.openmrs.api.context.Context;
import org.springframework.util.Assert;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;

/**
 * This class should be used in the *Services to validate objects before saving them. <br/>
 * <br/>
 * The validators are added to this class in the spring applicationContext-service.xml file. <br/>
 * <br/>
 * Example usage:
 *
 * <pre>
 *  public Order saveOrder(order) {
 *  	ValidateUtil.validate(order);
 *  	dao.saveOrder(order);
 *  }
 * </pre>
 *
 * @since 1.5
 */
public class ValidateUtil {
	
	/**
	 * @deprecated in favor of using HandlerUtil to reflexively get validators
	 * @param newValidators the validators to set
	 */
	@Deprecated
	public void setValidators(List<Validator> newValidators) {
		
	}
	
	/**
	 * Test the given object against all validators that are registered as compatible with the
	 * object class
	 *
	 * @param obj the object to validate
	 * @throws ValidationException thrown if a binding exception occurs
	 * @should throw APIException if errors occur during validation
	 */
	public static void validate(Object obj) throws ValidationException {
		Errors errors = new BindException(obj, "");
		
		Context.getAdministrationService().validate(obj, errors);
		
		if (errors.hasErrors()) {
			Set<String> uniqueErrorMessages = new LinkedHashSet<String>();
			for (Object objerr : errors.getAllErrors()) {
				ObjectError error = (ObjectError) objerr;
				String message = Context.getMessageSourceService().getMessage(error.getCode());
				if (error instanceof FieldError) {
					message = ((FieldError) error).getField() + ": " + message;
				}
				uniqueErrorMessages.add(message);
			}
			
			String exceptionMessage = "'" + obj + "' failed to validate with reason: ";
			exceptionMessage += StringUtils.join(uniqueErrorMessages, ", ");
			throw new ValidationException(exceptionMessage, errors);
		}
	}
	
	/**
	 * Test the given object against all validators that are registered as compatible with the
	 * object class
	 *
	 * @param obj the object to validate
	 * @param errors the validation errors found
	 * @since 1.9
	 * @should populate errors if object invalid
	 */
	public static void validate(Object obj, Errors errors) {
		Context.getAdministrationService().validate(obj, errors);
	}
	
	/**
	 * Test the field lengths are valid
	 *
	 * @param errors
	 * @param aClass the class of the object being tested
	 * @param fields a var args that contains all of the fields from the model
	 * @should pass validation if regEx field length is not too long
	 * @should fail validation if regEx field length is too long
	 * @should fail validation if name field length is too long
	 */
	
	public static void validateFieldLengths(Errors errors, Class aClass, String... fields) {
		Assert.notNull(errors, "Errors object must not be null");
		for (String field : fields) {
			Object value = errors.getFieldValue(field);
			if (value == null || !(value instanceof String)) {
				return;
			}
			int length = Context.getAdministrationService().getMaximumPropertyLength(aClass, field);
			if (((String) value).length() > length) {
				errors.rejectValue(field, "error.exceededMaxLengthOfField", new Object[] { length }, null);
			}
		}
	}
}
