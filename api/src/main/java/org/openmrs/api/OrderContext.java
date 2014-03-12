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
			return contextAttributes = new HashMap<String, Object>();
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
