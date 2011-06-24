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

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This represents a group of orders. Generally, orders may be placed within groups to assist with
 * subsequent management or reporting of the orders (e.g., a drug regimen of three drugs may be
 * placed within an order group).
 * 
 * @since 1.9
 */
public class OrderGroup extends BaseOpenmrsData implements Serializable {
	
	public static final long serialVersionUID = 1L;
	
	private Integer orderGroupId;
	
	private OrderSet orderSet;
	
	private Patient patient;
	
	private Set<Order> members;
	
	public OrderGroup() {
	}
	
	/**
	 * Constructs order group with a given id, patient
	 * 
	 * @param orderGroupId the identifier of order group
	 * @param patient the target patient for this group
	 */
	public OrderGroup(Integer orderGroupId, Patient patient) {
		this();
		setOrderGroupId(orderGroupId);
		setPatient(patient);
	}
	
	/**
	 * @param orderGroupId the orderGroupId to set
	 */
	public void setOrderGroupId(Integer orderGroupId) {
		this.orderGroupId = orderGroupId;
	}
	
	/**
	 * @return the orderGroupId
	 */
	public Integer getOrderGroupId() {
		return orderGroupId;
	}
	
	/**
	 * @param orderSet the orderSet to set
	 */
	public void setOrderSet(OrderSet orderSet) {
		this.orderSet = orderSet;
	}
	
	/**
	 * @return the orderSet
	 */
	public OrderSet getOrderSet() {
		return orderSet;
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		return getOrderGroupId();
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		setOrderGroupId(id);
		
	}
	
	/**
	 * @param creator the creator to set
	 */
	public void setCreator(User creator) {
		this.creator = creator;
	}
	
	/**
	 * @return the creator
	 */
	public User getCreator() {
		return creator;
	}
	
	/**
	 * @param patient the patient to set
	 */
	public void setPatient(Patient patient) {
		this.patient = patient;
	}
	
	/**
	 * @return the patient
	 */
	public Patient getPatient() {
		return patient;
	}
	
	/**
	 * @param members the orders to set
	 */
	public void setMembers(Set<Order> members) {
		this.members = members;
	}
	
	/**
	 * @return the orders
	 */
	public Set<Order> getMembers() {
		return members;
	}
	
	/**
	 * Adds new order to group. If group doesn't exist it will be created
	 * 
	 * @param order the order to be added to group
	 */
	public void addOrder(Order order) {
		if (getMembers() == null)
			setMembers(new LinkedHashSet<Order>());
		getMembers().add(order);
	}
	
}
