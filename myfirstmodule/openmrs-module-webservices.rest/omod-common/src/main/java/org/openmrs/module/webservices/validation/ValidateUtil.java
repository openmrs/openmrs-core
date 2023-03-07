/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.validation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

/**
 * This is just a copy of the 1.9.x core ValidateUtil method, *except* that if a validation failure
 * occurs, then it returns a ValidationException with the Spring Errors object included within in
 * (see RESTWS-422) This change will also been made to core (TRUNK-4296). Once REST-WS only supports
 * versions of core that have this change, this class (and ValidationException) can be removed, and
 * we will not have to explicitly call the "validate" within the DelegatingCrudResource, as we can
 * rely on the underlying API validation to do the right thing, and return a ValidationException
 * with Errors which we can trap in the BaseRestController
 */
public class ValidateUtil {
	
	/**
	 * Test the given object against all validators that are registered as compatible with the
	 * object class
	 * 
	 * @param obj the object to validate
	 * @throws ValidationException thrown if a binding exception occurs <strong>Should</strong>
	 *             throw ValidationException if errors occur during validation
	 */
	public static void validate(Object obj) throws ValidationException {
		
		Errors errors = new BindException(obj, "");
		
		AdministrationService administrationService = Context.getAdministrationService();
		
		try {
			// using refection here for compatibility with OpenMRS 1.8,x, which does not have this method
			Method validate = AdministrationService.class.getMethod("validate", Object.class, Errors.class);
			validate.invoke(administrationService, obj, errors);
		}
		catch (NoSuchMethodException ex) {
			// not running OpenMRS 1.9 or higher, so just skip this validation
			return;
		}
		catch (InvocationTargetException ex) {
			throw new APIException("Unable to invoke administrationService.validate(Object, Errors) via reflection", ex);
		}
		catch (IllegalAccessException ex) {
			throw new APIException("Unable to invoke administrationService.validate(Object, Errors) via reflection", ex);
		}
		
		if (errors.hasErrors()) {
			throw new ValidationException(generateExceptionMessage(obj, errors), errors);
		}
	}
	
	private static String generateExceptionMessage(Object obj, Errors errors) {
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
		return exceptionMessage;
	}
}
