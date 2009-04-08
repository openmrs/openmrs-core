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
package org.openmrs;

/**
 * OrderType
 * 
 * @see Order
 */
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
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof OrderType) {
			OrderType o = (OrderType) obj;
			if (o != null)
				return (getOrderTypeId().equals(o.getOrderTypeId()));
		}
		return false;
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		if (this.getOrderTypeId() == null)
			return super.hashCode();
		return this.getOrderTypeId().hashCode();
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
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	public Integer getId() {
		return getOrderTypeId();
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	public void setId(Integer id) {
		setOrderTypeId(id);
		
	}
	
}
