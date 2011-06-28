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

import org.openmrs.Order;
import org.openmrs.OrderGroup;
import org.openmrs.annotation.Handler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Validates the {@link OrderGroup} class.
 * 
 * @since 1.9
 */
@Handler(supports = { OrderGroup.class })
public class OrderGroupValidator implements Validator {
	
	@Autowired
	OrderValidator orderValidator;
	
	/**
	 * Determines if the command object being submitted is a valid type
	 * 
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> c) {
		return OrderGroup.class.isAssignableFrom(c);
	}
	
	/**
	 * Checks the orders group object for any inconsistencies/errors
	 * 
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 * @should fail if order group is null
	 * @should fail if any required field is null
	 * @should fail if group doesn't have any members
	 * @should fail if any order has different from group patient
	 * @should fail if any member is invalid
	 */
	@Override
	public void validate(Object target, Errors errors) {
		OrderGroup group = (OrderGroup) target;
		if (group == null) {
			errors.reject("error.general");
		} else {
			
			// for the following elements OrderGroup.hbm.xml says: not-null="true"
			ValidationUtils.rejectIfEmpty(errors, "creator", "error.null");
			ValidationUtils.rejectIfEmpty(errors, "patient", "error.null");
			ValidationUtils.rejectIfEmpty(errors, "dateCreated", "error.null");
			
			if (group.getMembers() == null || group.getMembers().isEmpty())
				errors.rejectValue("members", "OrderGroup.noMembersPresent");
			else {
				int index = 0;
				for (Order order : group.getMembers()) {
					try {
						errors.pushNestedPath("members[" + index + "]");
						ValidationUtils.invokeValidator(orderValidator, order, errors);
						
						if (order.getPatient() != null && !order.getPatient().equals(group.getPatient()))
							errors.rejectValue("patient", "OrderGroup.orderPatientMatching");
					}
					finally {
						errors.popNestedPath();
						index++;
					}
					
				}
			}
			
		}
		
	}
	
}
