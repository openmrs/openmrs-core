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

import org.openmrs.ConceptClass;
import org.openmrs.OrderType;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.order.OrderUtil;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validates the {@link OrderType} class.
 * 
 * @since 1.10
 */
@Handler(supports = { OrderType.class })
public class OrderTypeValidator implements Validator {
	
	/**
	 * Determines if the command object being submitted is a valid type
	 * 
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> c) {
		return OrderType.class.isAssignableFrom(c);
	}
	
	/**
	 * Validates an Order object
	 * 
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 * <strong>Should</strong> fail if the orderType object is null
	 * <strong>Should</strong> fail if name is null
	 * <strong>Should</strong> fail if name is empty
	 * <strong>Should</strong> fail if name is whitespace
	 * <strong>Should</strong> fail if name is a duplicate
	 * <strong>Should</strong> fail if conceptClass is a duplicate
	 * <strong>Should</strong> fail if parent is among its descendants
	 * <strong>Should</strong> fail if parent is also a direct child
	 * <strong>Should</strong> pass if all fields are correct for a new order type
	 * <strong>Should</strong> pass if all fields are correct for an existing order type
	 * <strong>Should</strong> be invoked when an order type is saved
	 * <strong>Should</strong> pass validation if field lengths are correct
	 * <strong>Should</strong> fail validation if field lengths are not correct
	 */
	@Override
	public void validate(Object obj, Errors errors) {
		if (obj == null || !(obj instanceof OrderType)) {
			throw new IllegalArgumentException("The parameter obj should not be null and must be of type" + OrderType.class);
		} else {
			OrderType orderType = (OrderType) obj;
			String name = orderType.getName();
			if (!StringUtils.hasText(name)) {
				errors.rejectValue("name", "error.name");
				return;
			}
			
			if (orderType.getParent() != null && OrderUtil.isType(orderType, orderType.getParent())) {
				errors.rejectValue("parent", "OrderType.parent.amongDescendants", new Object[] { orderType.getName() },
				    "Parent of " + orderType.getName() + " is among its descendants");
			}
			
			OrderType duplicate = Context.getOrderService().getOrderTypeByName(name);
			if (duplicate != null && !orderType.equals(duplicate)) {
				errors.rejectValue("name", "OrderType.duplicate.name", "Duplicate order type name: " + name);
			}
			
			for (OrderType ot : Context.getOrderService().getOrderTypes(true)) {
				if (ot != null) {
					//If this was an edit, skip past the order we are actually validating 
					if (orderType.equals(ot)) {
						continue;
					}
					int index = 0;
					for (ConceptClass cc : ot.getConceptClasses()) {
						if (cc != null && orderType.getConceptClasses().contains(cc)) {
							errors.rejectValue("conceptClasses[" + index + "]", "OrderType.duplicate", new Object[] {
							        cc.getName(), orderType.getName() }, cc.getName()
							        + " is already associated to another order type:" + orderType.getName());
						}
						index++;
					}
				}
			}
			ValidateUtil
			        .validateFieldLengths(errors, obj.getClass(), "name", "description", "retireReason", "javaClassName");
		}
	}
}
