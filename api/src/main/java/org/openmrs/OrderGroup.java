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
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

/**
 * This represents a group of orders.
 * 
 * @since 1.9
 */
public class OrderGroup extends BaseOpenmrsObject implements Serializable {
	
	public static final long serialVersionUID = 1L;
	
	private Integer orderGroupId;
	
	private OrderSet orderSet;
	
	private Patient patient;
	
	private User creator;
	
	private Date dateCreated;
	
	private Boolean voided;
	
	private User voidedBy;
	
	private Date dateVoided;
	
	private String voidReason;
	
	private Set<Order> orders;
	
	public OrderGroup() {
		orders = new TreeSet<Order>();
	}
	
	/**
	 * Constructs order group with a given id, patient, creator and date of cretion
	 * 
	 * @param orderGroupId the identifier of order group
	 * @param patient the target patient for this group
	 * @param creator the user, who has created this group
	 * @param dateCreated the date of group creation
	 */
	public OrderGroup(Integer orderGroupId, Patient patient, User creator, Date dateCreated) {
		this();
		this.orderGroupId = orderGroupId;
		this.patient = patient;
		this.creator = creator;
		this.dateCreated = dateCreated;
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
	 * @param voided the voided to set
	 */
	public void setVoided(Boolean voided) {
		this.voided = voided;
	}
	
	/**
	 * @return the voided
	 */
	public Boolean getVoided() {
		return voided;
	}
	
	/**
	 * @param voidedBy the voidedBy to set
	 */
	public void setVoidedBy(User voidedBy) {
		this.voidedBy = voidedBy;
	}
	
	/**
	 * @return the voidedBy
	 */
	public User getVoidedBy() {
		return voidedBy;
	}
	
	/**
	 * @param dateVoided the dateVoided to set
	 */
	public void setDateVoided(Date dateVoided) {
		this.dateVoided = dateVoided;
	}
	
	/**
	 * @return the dateVoided
	 */
	public Date getDateVoided() {
		return dateVoided;
	}
	
	/**
	 * @param voidReason the voidReason to set
	 */
	public void setVoidReason(String voidReason) {
		this.voidReason = voidReason;
	}
	
	/**
	 * @return the voidReason
	 */
	public String getVoidReason() {
		return voidReason;
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
	 * @param dateCreated the dateCreated to set
	 */
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	
	/**
	 * @return the dateCreated
	 */
	public Date getDateCreated() {
		return dateCreated;
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
	 * @param orders the orders to set
	 */
	public void setOrders(Set<Order> orders) {
		this.orders = orders;
	}
	
	/**
	 * @return the orders
	 */
	public Set<Order> getOrders() {
		return orders;
	}
	
}
