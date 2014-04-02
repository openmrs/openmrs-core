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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.order.OrderUtil;

import java.util.Date;

/**
 * Dates should be interpreted as follows: If startDate is null then the order has been going on
 * "since the beginning of time" Otherwise the order starts on startDate If discontinued is non-null
 * and true, then the following fields should be ignored: autoExpireDate if dateStopped is null then
 * the order was discontinued "the instant after it began" otherwise it was given from its starting
 * date until dateStopped Otherwise (discontinued is null or false) if autoExpireDate is null, the
 * order is set to go forever otherwise the order goes until autoExpireDate the following fields
 * should be ignored: discontinuedBy dateStopped discontinuedReason It is an error to have
 * discontinued be true and have dateStopped be after autoExpireDate. However this is not checked
 * for in the database or the application.
 * 
 * @version 1.0
 */
public class Order extends BaseOpenmrsData implements java.io.Serializable {
	
	public static final long serialVersionUID = 4334343L;
	
	/**
	 * @since 1.9.2, 1.10
	 */
	public enum Urgency {
		ROUTINE, STAT, ON_SCHEDULED_DATE
	}
	
	/**
	 * @since 1.10
	 */
	public enum Action {
		NEW, REVISE, RENEW, DISCONTINUE
	}
	
	private static final Log log = LogFactory.getLog(Order.class);
	
	// Fields
	
	private Integer orderId;
	
	private Patient patient;
	
	private Concept concept;
	
	private String instructions;
	
	private Date startDate;
	
	private Date autoExpireDate;
	
	private Encounter encounter;
	
	private Provider orderer;
	
	private Date dateStopped;
	
	private Concept orderReason;
	
	private String accessionNumber;
	
	private String orderReasonNonCoded;
	
	private Urgency urgency = Urgency.ROUTINE;
	
	private String orderNumber;
	
	private String commentToFulfiller;
	
	private CareSetting careSetting;
	
	private OrderType orderType;
	
	private Date scheduledDate;
	
	/**
	 * Allows orders to be linked to a previous order - e.g., an order discontinue ampicillin linked
	 * to the original ampicillin order (the D/C gets its own order number)
	 */
	private Order previousOrder;
	
	/**
	 * Represents the action being taken on an order.
	 * 
	 * @see org.openmrs.Order.Action
	 */
	private Action action = Action.NEW;
	
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
	 * @should copy all fields
	 */
	public Order copy() {
		return copyHelper(new Order());
	}
	
	/**
	 * The purpose of this method is to allow subclasses of Order to delegate a portion of their
	 * copy() method back to the superclass, in case the base class implementation changes.
	 * 
	 * @param target an Order that will have the state of <code>this</code> copied into it
	 * @return Returns the Order that was passed in, with state copied into it
	 */
	protected Order copyHelper(Order target) {
		target.setPatient(getPatient());
		target.setConcept(getConcept());
		target.setOrderType(getOrderType());
		target.setInstructions(getInstructions());
		target.setStartDate(getStartDate());
		target.setAutoExpireDate(getAutoExpireDate());
		target.setEncounter(getEncounter());
		target.setOrderer(getOrderer());
		target.setCreator(getCreator());
		target.setDateCreated(getDateCreated());
		target.setDateStopped(getDateStopped());
		target.setOrderReason(getOrderReason());
		target.setOrderReasonNonCoded(getOrderReasonNonCoded());
		target.setAccessionNumber(getAccessionNumber());
		target.setVoided(isVoided());
		target.setVoidedBy(getVoidedBy());
		target.setDateVoided(getDateVoided());
		target.setVoidReason(getVoidReason());
		target.setUrgency(getUrgency());
		target.setCommentToFulfiller(getCommentToFulfiller());
		target.previousOrder = getPreviousOrder();
		target.action = getAction();
		target.orderNumber = getOrderNumber();
		target.setCareSetting(getCareSetting());
		target.setChangedBy(getChangedBy());
		target.setDateChanged(getDateChanged());
		target.setScheduledDate(getScheduledDate());
		return target;
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
	 * @return the scheduledDate
	 * @since 1.10
	 */
	public Date getScheduledDate() {
		return scheduledDate;
	}
	
	/**
	 * @param scheduledDate the date to set
	 * @since 1.10
	 */
	public void setScheduledDate(Date scheduledDate) {
		this.scheduledDate = scheduledDate;
	}
	
	/**
	 * @return Returns the dateStopped.
	 * @since 1.10
	 */
	public Date getDateStopped() {
		return dateStopped;
	}
	
	/**
	 * @param dateStopped The dateStopped to set.
	 * @since 1.10
	 */
	public void setDateStopped(Date dateStopped) {
		this.dateStopped = dateStopped;
	}
	
	/**
	 * @return Returns the orderReason.
	 */
	public Concept getOrderReason() {
		return orderReason;
	}
	
	/**
	 * @param orderReason The orderReason to set.
	 */
	public void setOrderReason(Concept orderReason) {
		this.orderReason = orderReason;
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
	 * @return Returns the accessionNumber.
	 */
	public String getAccessionNumber() {
		return accessionNumber;
	}
	
	/**
	 * @param accessionNumber The accessionNumber to set.
	 */
	public void setAccessionNumber(String accessionNumber) {
		this.accessionNumber = accessionNumber;
	}
	
	/**
	 * @return Returns the orderer.
	 */
	public Provider getOrderer() {
		return orderer;
	}
	
	/**
	 * @param orderer The orderer to set.
	 */
	public void setOrderer(Provider orderer) {
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
	 * @return Returns the orderReasonNonCoded.
	 */
	public String getOrderReasonNonCoded() {
		return orderReasonNonCoded;
	}
	
	/**
	 * @param orderReasonNonCoded The orderReasonNonCoded to set.
	 */
	public void setOrderReasonNonCoded(String orderReasonNonCoded) {
		this.orderReasonNonCoded = orderReasonNonCoded;
	}
	
	/**
	 * @return the commentToFulfiller
	 * @since 1.10
	 */
	public String getCommentToFulfiller() {
		return commentToFulfiller;
	}
	
	/**
	 * @param commentToFulfiller The commentToFulfiller to set
	 * @since 1.10
	 */
	public void setCommentToFulfiller(String commentToFulfiller) {
		this.commentToFulfiller = commentToFulfiller;
	}
	
	/**
	 * Convenience method to determine if order is current
	 * 
	 * @param checkDate - the date on which to check order. if null, will use current date
	 * @return boolean indicating whether the order was current on the input date
	 */
	public boolean isCurrent(Date checkDate) {
		if (isVoided())
			return false;
		
		if (checkDate == null) {
			checkDate = new Date();
		}
		
		if (startDate != null && checkDate.before(startDate)) {
			return false;
		}
		
		if (isDiscontinuedRightNow()) {
			if (dateStopped == null)
				return checkDate.equals(startDate);
			else
				return checkDate.before(dateStopped);
			
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
		if (isVoided())
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
	 * 
	 * @param checkDate - the date on which to check order. if null, will use current date
	 * @return boolean indicating whether the order was discontinued on the input date
	 */
	public boolean isDiscontinued(Date checkDate) {
		if (isVoided())
			return false;
		if (checkDate == null)
			checkDate = new Date();
		
		if (startDate == null || checkDate.before(startDate)) {
			return false;
		}
		if (dateStopped != null && dateStopped.after(checkDate)) {
			return false;
		}
		if (dateStopped == null) {
			return false;
		}
		
		// guess we can't assume this has been filled correctly?
		/*
		 * if (dateStopped == null) { return false; }
		 */
		return true;
	}
	
	/*
	 * orderForm:jsp: <spring:bind path="order.discontinued" /> results in a call to
	 * isDiscontinued() which doesn't give access to the discontinued property so renamed it to
	 * isDiscontinuedRightNow which results in a call to getDiscontinued.
	 * @since 1.5
	 */
	public boolean isDiscontinuedRightNow() {
		return isDiscontinued(new Date());
	}
	
	public Patient getPatient() {
		return patient;
	}
	
	public void setPatient(Patient patient) {
		this.patient = patient;
	}
	
	public Integer getId() {
		return getOrderId();
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "Order. orderId: " + orderId + " patient: " + patient + " concept: " + concept + " care setting: "
		        + careSetting;
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	public void setId(Integer id) {
		setOrderId(id);
		
	}
	
	/**
	 * @return the urgency
	 * @since 1.9.2
	 */
	public Urgency getUrgency() {
		return urgency;
	}
	
	/**
	 * @param urgency the urgency to set
	 * @since 1.9.2
	 */
	public void setUrgency(Urgency urgency) {
		this.urgency = urgency;
	}
	
	/**
	 * @return the orderNumber
	 * @since 1.10
	 */
	public String getOrderNumber() {
		return orderNumber;
	}
	
	/**
	 * Gets the previous related order.
	 * 
	 * @since 1.10
	 * @return the previous order.
	 */
	public Order getPreviousOrder() {
		return previousOrder;
	}
	
	/**
	 * Sets the previous order.
	 * 
	 * @since 1.10
	 * @param previousOrder the previous order to set.
	 */
	public void setPreviousOrder(Order previousOrder) {
		this.previousOrder = previousOrder;
	}
	
	/**
	 * Gets the action
	 * 
	 * @return the action
	 * @since 1.10
	 */
	public Action getAction() {
		return action;
	}
	
	/**
	 * Sets the ation
	 * 
	 * @param action the action to set
	 * @since 1.10
	 */
	public void setAction(Action action) {
		this.action = action;
	}
	
	/**
	 * Gets the careSetting
	 * 
	 * @return the action
	 * @since 1.10
	 */
	public CareSetting getCareSetting() {
		return careSetting;
	}
	
	/**
	 * Sets the careSetting
	 * 
	 * @param careSetting the action to set
	 * @since 1.10
	 */
	public void setCareSetting(CareSetting careSetting) {
		this.careSetting = careSetting;
	}
	
	/**
	 * Get the {@link org.openmrs.OrderType}
	 * 
	 * @return the {@link org.openmrs.OrderType}
	 */
	public OrderType getOrderType() {
		return orderType;
	}
	
	/**
	 * Set the {@link org.openmrs.OrderType}
	 * 
	 * @param orderType the {@link org.openmrs.OrderType}
	 */
	public void setOrderType(OrderType orderType) {
		this.orderType = orderType;
	}
	
	/**
	 * Creates a discontinuation order for this order, sets the previousOrder and action fields,
	 * note that the discontinuation order needs to be saved for the discontinuation to take effect
	 * 
	 * @return the newly created order
	 * @since 1.10
	 * @should set all the relevant fields
	 */
	public Order cloneForDiscontinuing() {
		Order newOrder = new Order();
		newOrder.setCareSetting(this.getCareSetting());
		newOrder.setConcept(this.getConcept());
		newOrder.setAction(Action.DISCONTINUE);
		newOrder.setPreviousOrder(this);
		newOrder.setPatient(this.getPatient());
		newOrder.setOrderType(getOrderType());
		
		return newOrder;
	}
	
	/**
	 * Creates an order for revision from this order, sets the previousOrder and action field.
	 * 
	 * @return the newly created order
	 * @since 1.10
	 * @should set all the relevant fields
	 */
	public Order cloneForRevision() {
		Order newOrder = new Order();
		newOrder.setCareSetting(this.getCareSetting());
		newOrder.setConcept(this.getConcept());
		newOrder.setAction(Action.REVISE);
		newOrder.setPreviousOrder(this);
		newOrder.setPatient(this.getPatient());
		newOrder.setOrderType(this.getOrderType());
		newOrder.setScheduledDate(getScheduledDate());
		newOrder.setInstructions(this.getInstructions());
		newOrder.setUrgency(this.getUrgency());
		newOrder.setCommentToFulfiller(this.getCommentToFulfiller());
		newOrder.setAccessionNumber(this.getAccessionNumber());
		newOrder.setAutoExpireDate(this.getAutoExpireDate());
		newOrder.setOrderReason(this.getOrderReason());
		newOrder.setOrderReasonNonCoded(this.getOrderReasonNonCoded());
		return newOrder;
	}
	
	/**
	 * Checks whether this order's orderType matches or is a sub type of the specified one
	 * 
	 * @since 1.10
	 * @param orderType the orderType to match on
	 * @return true if the type of the order matches or is a sub type of the other order
	 * @should true if it is the same or is a subtype
	 * @should false if it neither the same nor a subtype
	 */
	public boolean isType(OrderType orderType) {
		return OrderUtil.isType(orderType, this.orderType);
	}
}
