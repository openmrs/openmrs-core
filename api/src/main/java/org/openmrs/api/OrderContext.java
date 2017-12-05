/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api;

import java.util.HashMap;
import java.util.Map;

import org.openmrs.CareSetting;
import org.openmrs.OrderType;

/**
 * Contains contextual information like the OrderType, CareSetting and any other custom attributes
 * that are passed to the service layer when placing a new Order E.g you could add a user defined
 * order number from a form that can be looked up from the context and returned by a custom
 * OrderNumberGenerator
 * 
 * @since 1.10
 */
public class OrderContext {
	
	private OrderType orderType;
	
	private CareSetting careSetting;
	
	private Map<String, Object> contextAttributes;
	
	/**
	 * @return the orderType
	 */
	public OrderType getOrderType() {
		return orderType;
	}
	
	/**
	 * @param orderType the OrderType to set
	 */
	public void setOrderType(OrderType orderType) {
		this.orderType = orderType;
	}
	
	/**
	 * @return the careSetting
	 */
	public CareSetting getCareSetting() {
		return careSetting;
	}
	
	/**
	 * @param careSetting the CareSetting to set
	 */
	public void setCareSetting(CareSetting careSetting) {
		this.careSetting = careSetting;
	}
	
	/**
	 * @return the contextAttributes
	 */
	public Map<String, Object> getContextAttributes() {
		if (contextAttributes == null) {
			return contextAttributes = new HashMap<>();
		}
		return contextAttributes;
	}
	
	/**
	 * @param contextAttributes the context attributes to set
	 */
	public void setContextAttributes(Map<String, Object> contextAttributes) {
		this.contextAttributes = contextAttributes;
	}
	
	/**
	 * Gets the value of for the specified attribute name
	 * 
	 * @param attributeName the attribute name
	 */
	public Object getAttribute(String attributeName) {
		return getContextAttributes().get(attributeName);
	}
	
	/**
	 * Adds the specified context attribute
	 * 
	 * @param attributeName the attribute name
	 * @param attributeValue the attribute value
	 */
	public void setAttribute(String attributeName, Object attributeValue) {
		getContextAttributes().put(attributeName, attributeValue);
	}
	
	/**
	 * Removes the attribute with the specified name
	 * 
	 * @param attributeName the attribute name
	 */
	public void removeAttribute(String attributeName) {
		getContextAttributes().remove(attributeName);
	}
	
	/**
	 * Clears all the context attributes
	 */
	public void clear() {
		getContextAttributes().clear();
	}
	
}
