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

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Dates should be interpreted as follows:
 *    If startDate is null then the order has been going on "since the beginning of time"
 *    Otherwise the order starts on startDate
 *    
 *    If discontinued is non-null and true, then
 *       the following fields should be ignored:
 *          autoExpireDate
 *       if discontinuedDate is null then the order was discontinued "the instant after it began"
 *          otherwise it was given from its starting date until discontinuedDate
 *    
 *    Otherwise (discontinued is null or false)
 *       if autoExpireDate is null, the order is set to go forever
 *          otherwise the order goes until autoExpireDate
 *       the following fields should be ignored:
 *          discontinuedBy
 *          discontinuedDate
 *          discontinuedReason
 * 
 * It is an error to have discontinued be true and have discontinuedDate be after autoExpireDate.
 *    However this is not checked for in the database or the application.
 * @version 1.0
 */
public class Order implements java.io.Serializable {

	protected final Log log = LogFactory.getLog(getClass());
	public static final long serialVersionUID = 4334343L;

	// Fields

	private Integer orderId;
	private Patient patient;
	private OrderType orderType;
	private Concept concept;
	private String instructions;
	private Date startDate;
	private Date autoExpireDate;
	private Encounter encounter;
	private User orderer;
	private User creator;
	private Date dateCreated;
	private Boolean discontinued = false;
	private User discontinuedBy;
	private Date discontinuedDate;
	private Concept discontinuedReason;
	private Boolean voided = false;
	private User voidedBy;
	private Date dateVoided;
	private String voidReason;

	
	// Constructors

	/** default constructor */
	public Order() {
	}

	/** constructor with id */
	public Order(Integer orderId) {
		this.orderId = orderId;
	}
	
	/**
	 * Performs a shallow copy of this Order. Does NOT copy orderId.
	 * 
	 * @return a shallow copy of this Order
	 */
	public Order copy() {
		return copyHelper(new Order());
	}
	
	/**
	 * The purpose of this method is to allow subclasses of Order to delegate a portion of
	 * their copy() method back to the superclass, in case the base class implementation changes. 
	 * 
	 * @param ret an Order that will have the state of <code>this</code> copied into it
	 * @return the Order that was passed in, with state copied into it
	 */
	protected Order copyHelper(Order target) {
		target.setPatient(getPatient());
		target.setOrderType(getOrderType());
		target.setConcept(getConcept());
		target.setInstructions(getInstructions());
		target.setStartDate(getStartDate());
		target.setAutoExpireDate(getAutoExpireDate());
		target.setEncounter(getEncounter());
		target.setOrderer(getOrderer());
		target.setCreator(getCreator());
		target.setDateCreated(getDateCreated());
		target.setDiscontinued(getDiscontinued());
		target.setDiscontinuedDate(getDiscontinuedDate());
		target.setDiscontinuedReason(getDiscontinuedReason());
		target.setDiscontinuedBy(getDiscontinuedBy());
		target.setVoided(getVoided());
		target.setVoidedBy(getVoidedBy());
		target.setDateVoided(getDateVoided());
		target.setVoidReason(getVoidReason());
		return target;
	}

	/** 
	 * Compares two objects for similarity
	 * 
	 * @param obj
	 * @return boolean true/false whether or not they are the same objects
	 */
	public boolean equals(Object obj) {
		if (obj instanceof Order) {
			Order o = (Order)obj;
			if (this.getOrderId() != null && o.getOrderId() != null)
				return (this.getOrderId().equals(o.getOrderId()));
			/*return (this.getOrderType().equals(o.getOrderType()) &&
					this.getConcept().equals(o.getConcept()) &&
					this.getEncounter().equals(o.getEncounter()) &&
					this.getInstructions().matches(o.getInstructions())); */
		}
		return false;
	}
	
	public int hashCode() {
		if (this.getOrderId() == null) return super.hashCode();
		return this.getOrderId().hashCode();
	}

	/**
	 * true/false whether or not this is a drug order
	 * overridden in extending class drugOrders. 
	 */
	public boolean isDrugOrder() {
		return false;
	}
	
	// Property accessors

	/**
	 * @return Returns the autoExpireDate.
	 */
	public Date getAutoExpireDate() {
		return autoExpireDate;
	}

	/**
	 * @param autoExpireDate The autoExpireDate to set.
	 */
	public void setAutoExpireDate(Date autoExpireDate) {
		this.autoExpireDate = autoExpireDate;
	}

	/**
	 * @return Returns the concept.
	 */
	public Concept getConcept() {
		return concept;
	}

	/**
	 * @param concept The concept to set.
	 */
	public void setConcept(Concept concept) {
		this.concept = concept;
	}

	/**
	 * @return Returns the creator.
	 */
	public User getCreator() {
		return creator;
	}

	/**
	 * @param creator The creator to set.
	 */
	public void setCreator(User creator) {
		this.creator = creator;
	}

	/**
	 * @return Returns the dateCreated.
	 */
	public Date getDateCreated() {
		return dateCreated;
	}

	/**
	 * @param dateCreated The dateCreated to set.
	 */
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	/**
	 * @return Returns the discontinued status.
	 */
	public Boolean getDiscontinued() {
		return discontinued;
	}

	/**
	 * @param discontinued The discontinued status to set.
	 */
	public void setDiscontinued(Boolean discontinued) {
		this.discontinued = discontinued;
	}

	/**
	 * @return Returns the discontinuedBy.
	 */
	public User getDiscontinuedBy() {
		return discontinuedBy;
	}

	/**
	 * @param discontinuedBy The discontinuedBy to set.
	 */
	public void setDiscontinuedBy(User discontinuedBy) {
		this.discontinuedBy = discontinuedBy;
	}

	/**
	 * @return Returns the discontinuedDate.
	 */
	public Date getDiscontinuedDate() {
		return discontinuedDate;
	}

	/**
	 * @param discontinuedDate The discontinuedDate to set.
	 */
	public void setDiscontinuedDate(Date discontinuedDate) {
		this.discontinuedDate = discontinuedDate;
	}

	/**
	 * @return Returns the discontinuedReason.
	 */
	public Concept getDiscontinuedReason() {
		return discontinuedReason;
	}

	/**
	 * @param discontinuedReason The discontinuedReason to set.
	 */
	public void setDiscontinuedReason(Concept discontinuedReason) {
		this.discontinuedReason = discontinuedReason;
	}

	/**
	 * @return Returns the dateVoided.
	 */
	public Date getDateVoided() {
		return dateVoided;
	}

	/**
	 * @param dateVoided The dateVoided to set.
	 */
	public void setDateVoided(Date dateVoided) {
		this.dateVoided = dateVoided;
	}

	/**
	 * @return Returns the voided.
	 */
	public Boolean getVoided() {
		return voided;
	}

	/**
	 * @param voided The voided to set.
	 */
	public void setVoided(Boolean voided) {
		this.voided = voided;
	}

	/**
	 * @return Returns the voidedBy.
	 */
	public User getVoidedBy() {
		return voidedBy;
	}

	/**
	 * @param voidedBy The voidedBy to set.
	 */
	public void setVoidedBy(User voidedBy) {
		this.voidedBy = voidedBy;
	}

	/**
	 * @return Returns the voidReason.
	 */
	public String getVoidReason() {
		return voidReason;
	}

	/**
	 * @param voidReason The voidReason to set.
	 */
	public void setVoidReason(String voidReason) {
		this.voidReason = voidReason;
	}

	/**
	 * @return Returns the encounter.
	 */
	public Encounter getEncounter() {
		return encounter;
	}

	/**
	 * @param encounter The encounter to set.
	 */
	public void setEncounter(Encounter encounter) {
		this.encounter = encounter;
	}

	/**
	 * @return Returns the instructions.
	 */
	public String getInstructions() {
		return instructions;
	}

	/**
	 * @param instructions The instructions to set.
	 */
	public void setInstructions(String instructions) {
		this.instructions = instructions;
	}

	/**
	 * @return Returns the orderer.
	 */
	public User getOrderer() {
		return orderer;
	}

	/**
	 * @param orderer The orderer to set.
	 */
	public void setOrderer(User orderer) {
		this.orderer = orderer;
	}

	/**
	 * @return Returns the orderId.
	 */
	public Integer getOrderId() {
		return orderId;
	}

	/**
	 * @param orderId The orderId to set.
	 */
	public void setOrderId(Integer orderId) {
		this.orderId = orderId;
	}

	/**
	 * @return Returns the orderType.
	 */
	public OrderType getOrderType() {
		return orderType;
	}

	/**
	 * @param orderType The orderType to set.
	 */
	public void setOrderType(OrderType orderType) {
		this.orderType = orderType;
	}

	/**
	 * @return Returns the startDate.
	 */
	public Date getStartDate() {
		return startDate;
	}

	/**
	 * @param startDate The startDate to set.
	 */
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	
	/**
	 * Convenience method to determine if order is current
	 * @param checkDate - the date on which to check order. if null, will use current date
	 * @return boolean indicating whether the order was current on the input date
	 */
	public boolean isCurrent(Date checkDate) {
		if (voided)
			return false;

		if (checkDate == null) {
			checkDate = new Date();
		}

		if (startDate != null && checkDate.before(startDate)) {
			return false;
		}

		if (discontinued != null && discontinued) {
			if (discontinuedDate == null)
				return checkDate.equals(startDate);
			else
				return checkDate.before(discontinuedDate);
				
		} else {
			if (autoExpireDate == null)
				return true;
			else
				return checkDate.before(autoExpireDate);
		}
	}
	
	public boolean isCurrent() {
		return isCurrent(new Date());
	}

	public boolean isFuture(Date checkDate) {
		if (voided)
			return false;
		if (checkDate == null)
			checkDate = new Date();

		return startDate != null && checkDate.before(startDate);
	}

	public boolean isFuture() {
		return isFuture(new Date());
	}

	
	/**
	 * Convenience method to determine if order is discontinued at a given time
	 * @param checkDate - the date on which to check order. if null, will use current date
	 * @return boolean indicating whether the order was discontinued on the input date
	 */
	public boolean isDiscontinued(Date checkDate) {
		if (voided)
			return false;
		if (checkDate == null)
			checkDate = new Date();

		if (discontinued == null || !discontinued)
			return false;
		
		if (startDate == null || checkDate.before(startDate)) { 
			return false; 
		} 
		if (discontinuedDate != null && discontinuedDate.after(checkDate)) { 
			return false; 
		}
		
		// guess we can't assume this has been filled correctly?
		/*
		if (discontinuedDate == null) { 
			return false; 
		} 
		*/
		return true;
	}
	
	public boolean isDiscontinued() {
		return isDiscontinued(new Date());
	}

	public Patient getPatient() {
		return patient;
	}

	public void setPatient(Patient patient) {
		this.patient = patient;
	}
	
	public Integer getId() {
		return this.orderId;
	}

}