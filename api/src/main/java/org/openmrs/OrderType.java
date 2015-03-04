/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

/**
 * OrderType
 * 
 * @deprecated Will be removed in version 1.10
 * @see Order
 */
@Deprecated
public class OrderType extends BaseOpenmrsMetadata implements java.io.Serializable {
	
	public static final long serialVersionUID = 23232L;
	
	// Fields
	
	private Integer orderTypeId;
	
	// Constructors
	
	/** default constructor */
	public OrderType() {
	}
	
	/** constructor with id */
	public OrderType(Integer orderTypeId) {
		this.orderTypeId = orderTypeId;
	}
	
	/**
	 * Convenience constructor that takes in the elements required to save this OrderType to the
	 * database
	 * 
	 * @param name The name of this order Type
	 * @param description A short description about this order type
	 */
	public OrderType(String name, String description) {
		setName(name);
		setDescription(description);
	}
	
	// Property accessors
	
	/**
	 * @return Returns the orderTypeId.
	 */
	public Integer getOrderTypeId() {
		return orderTypeId;
	}
	
	/**
	 * @param orderTypeId The orderTypeId to set.
	 */
	public void setOrderTypeId(Integer orderTypeId) {
		this.orderTypeId = orderTypeId;
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	public Integer getId() {
		return getOrderTypeId();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	public void setId(Integer id) {
		setOrderTypeId(id);
		
	}
	
}
