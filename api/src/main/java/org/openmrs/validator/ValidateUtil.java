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

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.OpenmrsObject;
import org.openmrs.api.ValidationException;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.hibernate.HibernateUtil;
import org.springframework.util.Assert;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

/**
 * This class should be used in the *Services to validate objects before saving them. <br>
 * <br>
 * The validators are added to this class in the spring applicationContext-service.xml file. <br>
 * <br>
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

	private ValidateUtil() {
	}

	/**
	 * This is set in {@link Context#checkCoreDataset()} class
	 */
	private static Boolean disableValidation = false;
	
	/**
	 * Test the given object against all validators that are registered as compatible with the
	 * object class
	 *
	 * @param obj the object to validate
	 * @throws ValidationException thrown if a binding exception occurs
	 * <strong>Should</strong> throw APIException if errors occur during validation
	 * <strong>Should</strong> return immediately if validation is disabled
	 */
	public static void validate(Object obj) throws ValidationException {
		if (disableValidation) {
			return;
		}

		obj = HibernateUtil.getRealObjectFromProxy(obj);
		
		Errors errors = new BindException(obj, "");
		
		Context.getAdministrationService().validate(obj, errors);
		
		if (errors.hasErrors()) {
			Set<String> uniqueErrorMessages = new LinkedHashSet<>();
			for (Object objerr : errors.getAllErrors()) {
				ObjectError error = (ObjectError) objerr;
				String message = Context.getMessageSourceService().getMessage(error.getCode(), error.getArguments(), Context.getLocale());
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
	 * <strong>Should</strong> populate errors if object invalid
	 * <strong>Should</strong> return immediately if validation is disabled and have no errors
	 */
	public static void validate(Object obj, Errors errors) {
		if (disableValidation) {
			return;
		}

		obj = HibernateUtil.getRealObjectFromProxy(obj);
		
		Context.getAdministrationService().validate(obj, errors);
	}
	
	/**
	 * Test the field lengths are valid
	 *
	 * @param errors
	 * @param aClass the class of the object being tested
	 * @param fields a var args that contains all of the fields from the model
	 * <strong>Should</strong> pass validation if regEx field length is not too long
	 * <strong>Should</strong> fail validation if regEx field length is too long
	 * <strong>Should</strong> fail validation if name field length is too long
	 * <strong>Should</strong> return immediately if validation is disabled and have no errors
	 */
	public static void validateFieldLengths(Errors errors, Class<?> aClass, String... fields) {
		if (disableValidation) {
			return;
		}

		Assert.notNull(errors, "Errors object must not be null");
		for (String field : fields) {
			Object value = errors.getFieldValue(field);
			if (value == null || !(value instanceof String)) {
				continue;
			}
			int length = Context.getAdministrationService().getMaximumPropertyLength((Class<? extends OpenmrsObject>) aClass, field);
			if (length == -1) {
				return;
			}
			if (((String) value).length() > length) {
				errors.rejectValue(field, "error.exceededMaxLengthOfField", new Object[] { length }, null);
			}
		}
	}

	public static Boolean getDisableValidation() {
		return disableValidation;
	}

	public static void setDisableValidation(Boolean disableValidation) {
		ValidateUtil.disableValidation = disableValidation;
	}

}
