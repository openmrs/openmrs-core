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
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.util.HandlerUtil;
import org.springframework.validation.BindException;
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
	 * Fetches all validators that are registered
	 * 
	 * @param obj the object that will be validated
	 * @return list of compatibile validators
	 */
	protected static List<Validator> getValidators(Object obj) {
		List<Validator> matchingValidators = new Vector<Validator>();
		
		List<Validator> validators = HandlerUtil.getHandlersForType(Validator.class, obj.getClass());
		
		for (Validator validator : validators) {
			if (validator.supports(obj.getClass())) {
				matchingValidators.add(validator);
			}
		}
		
		return matchingValidators;
	}
	
	/**
	 * Test the given object against all validators that are registered as compatible with the
	 * object class
	 * 
	 * @param obj the object to validate
	 * @throws APIException thrown if a binding exception occurs
	 * @should throw APIException if errors occur during validation
	 */
	public static void validate(Object obj) throws APIException {
		BindException errors = new BindException(obj, "");
		
		for (Validator validator : getValidators(obj)) {
			validator.validate(obj, errors);
		}
		
		if (errors.hasErrors()) {
			Set<String> uniqueErrorMessages = new LinkedHashSet<String>();
			for (Object objerr : errors.getAllErrors()) {
				ObjectError error = (ObjectError) objerr;
				String message = Context.getMessageSourceService().getMessage(error.getCode());
				uniqueErrorMessages.add(message);
			}
			
			String exceptionMessage = "'" + obj + "' failed to validate with reason: ";
			exceptionMessage += StringUtils.join(uniqueErrorMessages, ", ");
			throw new APIException(exceptionMessage, errors.getCause());
		}
	}
}
