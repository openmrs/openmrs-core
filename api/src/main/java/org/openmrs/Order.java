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

import org.openmrs.api.APIException;
import org.openmrs.api.db.hibernate.HibernateUtil;
import org.openmrs.order.OrderUtil;
import org.openmrs.util.OpenmrsUtil;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Encapsulates information about the clinical action of a provider requesting something for a
 * patient e.g requesting a test to be performed, prescribing a medication, requesting the patient
 * to enroll on a specific diet etc. There is the notion of effective dates, these are used to
 * determine the span of an order's schedule i.e its effective start and stop dates therefore dates
 * should be interpreted as follows: The effective start of the schedule is the scheduledDate if
 * urgency is set to ON_SCHEDULED_DATE otherwise it is the dateActivated; the effective end date is
 * dateStopped, if it is null then it is the autoExpireDate. For DrugOrders, if the autoExpireDate
 * is not specified then it will be calculated and set by the API based on the duration and
 * frequency, note that frequency is only used in case duration is specified as a recurring interval
 * e.g. 3 times.
 * 
 * @version 1.0
 */
public class Order extends BaseCustomizableData<OrderAttribute> implements FormRecordable {

	public static final long serialVersionUID = 4334343L;

	/**
	 * @since 1.9.2, 1.10
	 */
	public enum Urgency {
		ROUTINE,
		STAT,
		ON_SCHEDULED_DATE
	}
	
	/**
	 * @since 1.10
	 */
	public enum Action {
		NEW,
		REVISE,
		DISCONTINUE,
		RENEW
	}
	
	/**
	 * Valid values for the status of an order that is received from a filler
	 * @since 2.2.0  
	 * @since 2.6.1 added ON_HOLD & DECLINED
	 */
	public enum FulfillerStatus {
		RECEIVED, 
		IN_PROGRESS,
		EXCEPTION,
		ON_HOLD,
		DECLINED,
		COMPLETED
	}
	
	private Integer orderId;
	
	private Patient patient;
	
	private OrderType orderType;
	
	private Concept concept;
	
	private String instructions;
	
	private Date dateActivated;
	
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
	
	private Date scheduledDate;
	
	private String formNamespaceAndPath;
	
	/**
	 * Allows the orders if ordered as an orderGroup, to maintain a sequence of how members are
	 * added in the group ex - for two orders of isoniazid and ampicillin, the sequence of 1 and 2
	 * needed to be maintained
	 */
	private Double sortWeight;
	
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
	
	/**
	 * {@link org.openmrs.OrderGroup}
	 */
	private OrderGroup orderGroup;
	
	/**
	 * Represents the status of an order received from a fulfiller 
	 * @see FulfillerStatus
	 */
	private FulfillerStatus fulfillerStatus;
	
	/**
	 * Represents the comment that goes along with with fulfiller status
	 */	
	private String fulfillerComment;

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
	 * <strong>Should</strong> copy all fields
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
		target.setOrderType(getOrderType());
		target.setConcept(getConcept());
		target.setInstructions(getInstructions());
		target.setDateActivated(getDateActivated());
		target.setAutoExpireDate(getAutoExpireDate());
		target.setEncounter(getEncounter());
		target.setOrderer(getOrderer());
		target.setCreator(getCreator());
		target.setDateCreated(getDateCreated());
		target.dateStopped = getDateStopped();
		target.setOrderReason(getOrderReason());
		target.setOrderReasonNonCoded(getOrderReasonNonCoded());
		target.setAccessionNumber(getAccessionNumber());
		target.setVoided(getVoided());
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
		target.setOrderGroup(getOrderGroup());
		target.setSortWeight(getSortWeight());
		target.setFulfillerComment(getFulfillerComment());
		target.setFulfillerStatus(getFulfillerStatus());
		target.setFormNamespaceAndPath(getFormNamespaceAndPath());
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
	 * @return Returns the dateActivated.
	 */
	public Date getDateActivated() {
		return dateActivated;
	}
	
	/**
	 * @param dateActivated The dateActivated to set.
	 */
	public void setDateActivated(Date dateActivated) {
		this.dateActivated = dateActivated;
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
	 * Convenience method to determine if the order is activated as of the current date
	 * 
	 * @return boolean indicating whether the order was activated before or on the current date
	 * @since 2.0
	 * @see #isActivated(java.util.Date)
	 */
	public boolean isActivated() {
		return isActivated(new Date());
	}
	
	/**
	 * Convenience method to determine if the order is activated as of the specified date
	 * 
	 * @param checkDate - the date on which to check order. if null, will use current date
	 * @return boolean indicating whether the order was activated before or on the check date
	 * @since 2.0
	 * <strong>Should</strong> return true if an order was activated on the check date
	 * <strong>Should</strong> return true if an order was activated before the check date
	 * <strong>Should</strong> return false if dateActivated is null
	 * <strong>Should</strong> return false for an order activated after the check date
	 */
	public boolean isActivated(Date checkDate) {
		if (dateActivated == null) {
			return false;
		}
		if (checkDate == null) {
			checkDate = new Date();
		}
		return OpenmrsUtil.compare(dateActivated, checkDate) <= 0;
	}
	
	/**
	 * Convenience method to determine if the order was active as of the current date
	 * 
	 * @since 1.10.1
	 * @return boolean indicating whether the order was active on the check date
	 */
	public boolean isActive() {
		return isActive(new Date());
	}
	
	/**
	 * Convenience method to determine if the order is active as of the specified date
	 * 
	 * @param aCheckDate - the date on which to check order. if null, will use current date
	 * @return boolean indicating whether the order was active on the check date
	 * @since 1.10.1
	 * <strong>Should</strong> return true if an order expired on the check date
	 * <strong>Should</strong> return true if an order was discontinued on the check date
	 * <strong>Should</strong> return true if an order was activated on the check date
	 * <strong>Should</strong> return true if an order was activated on the check date but scheduled for the future
	 * <strong>Should</strong> return false for a voided order
	 * <strong>Should</strong> return false for a discontinued order
	 * <strong>Should</strong> return false for an expired order
	 * <strong>Should</strong> return false for an order activated after the check date
	 * <strong>Should</strong> return false for a discontinuation order
	 */
	public boolean isActive(Date aCheckDate) {
		if (getVoided() || action == Action.DISCONTINUE) {
			return false;
		}
		Date checkDate = aCheckDate == null ? new Date() : aCheckDate;
		return isActivated(checkDate) && !isDiscontinued(checkDate) && !isExpired(checkDate);
	}
	
	/**
	 * Convenience method to determine if order is started as of the current date
	 * 
	 * @return boolean indicating whether the order is started as of the current date
	 * @since 1.10.1
	 * @see #isStarted(java.util.Date)
	 */
	public boolean isStarted() {
		return isStarted(new Date());
	}
	
	/**
	 * Convenience method to determine if the order is started as of the specified date, returns
	 * true only if the order has been activated. In case of scheduled orders, the scheduledDate
	 * becomes the effective start date that gets used to determined if it is started.
	 * 
	 * @param aCheckDate - the date on which to check order. if null, will use current date
	 * @return boolean indicating whether the order is started as of the check date
	 * @since 1.10.1
	 * <strong>Should</strong> return false for a voided order
	 * <strong>Should</strong> return false if dateActivated is null
	 * <strong>Should</strong> return false if the order is not yet activated as of the check date
	 * <strong>Should</strong> return false if the order was scheduled to start after the check date
	 * <strong>Should</strong> return true if the order was scheduled to start on the check date
	 * <strong>Should</strong> return true if the order was scheduled to start before the check date
	 * <strong>Should</strong> return true if the order is started and not scheduled
	 */
	public boolean isStarted(Date aCheckDate) {
		if (getVoided()) {
			return false;
		}
		if (getEffectiveStartDate() == null) {
			return false;
		}
		Date checkDate = aCheckDate == null ? new Date() : aCheckDate;
		return !checkDate.before(getEffectiveStartDate());
	}
	
	/**
	 * Convenience method to determine if the order is discontinued as of the specified date
	 * 
	 * @param aCheckDate - the date on which to check order. if null, will use current date
	 * @return boolean indicating whether the order was discontinued on the input date
	 * <strong>Should</strong> return false for a voided order
	 * <strong>Should</strong> return false if date stopped and auto expire date are both null
	 * <strong>Should</strong> return false if auto expire date is null and date stopped is equal to check date
	 * <strong>Should</strong> return false if auto expire date is null and date stopped is after check date
	 * <strong>Should</strong> return false if dateActivated is after check date
	 * <strong>Should</strong> return true if auto expire date is null and date stopped is before check date
	 * <strong>Should</strong> fail if date stopped is after auto expire date
	 * <strong>Should</strong> return true if check date is after date stopped but before auto expire date
	 * <strong>Should</strong> return true if check date is after both date stopped auto expire date
	 * <strong>Should</strong> return true if the order is scheduled for the future and activated on check date but
	 *         the check date is after date stopped
	 */
	public boolean isDiscontinued(Date aCheckDate) {
		if (dateStopped != null && autoExpireDate != null && dateStopped.after(autoExpireDate)) {
			throw new APIException("Order.error.invalidDateStoppedAndAutoExpireDate", (Object[]) null);
		}
		if (getVoided()) {
			return false;
		}
		Date checkDate = aCheckDate == null ? new Date() : aCheckDate;
		if (!isActivated(checkDate) || dateStopped == null) {
			return false;
		}
		return checkDate.after(dateStopped);
	}
	
	/**
	 * Convenience method to determine if the order is expired as of the specified date
	 * 
	 * @return boolean indicating whether the order is expired at the current time
	 * @since 1.10.1
	 */
	public boolean isExpired() {
		return isExpired(new Date());
	}
	
	/**
	 * Convenience method to determine if order was expired at a given time
	 * 
	 * @param aCheckDate - the date on which to check order. if null, will use current date
	 * @return boolean indicating whether the order was expired on the input date
	 * <strong>Should</strong> return false for a voided order
	 * <strong>Should</strong> return false if date stopped and auto expire date are both null
	 * <strong>Should</strong> return false if date stopped is null and auto expire date is equal to check date
	 * <strong>Should</strong> return false if date stopped is null and auto expire date is after check date
	 * <strong>Should</strong> return false if check date is after both date stopped auto expire date
	 * <strong>Should</strong> return false if dateActivated is after check date
	 * <strong>Should</strong> return false if check date is after date stopped but before auto expire date
	 * <strong>Should</strong> fail if date stopped is after auto expire date
	 * <strong>Should</strong> return true if date stopped is null and auto expire date is before check date
	 * @since 1.10.1
	 */
	public boolean isExpired(Date aCheckDate) {
		if (dateStopped != null && autoExpireDate != null && dateStopped.after(autoExpireDate)) {
			throw new APIException("Order.error.invalidDateStoppedAndAutoExpireDate", (Object[]) null);
		}
		if (getVoided()) {
			return false;
		}
		Date checkDate = aCheckDate == null ? new Date() : aCheckDate;
		if (!isActivated(checkDate)) {
			return false;
		}
		if (isDiscontinued(checkDate) || autoExpireDate == null) {
			return false;
		}

		return checkDate.after(autoExpireDate);
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
	
	@Override
	public Integer getId() {
		return getOrderId();
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String prefix = Action.DISCONTINUE == getAction() ? "DC " : "";
		return prefix + "Order. orderId: " + orderId + " patient: " + patient + " concept: " + concept + " care setting: "
		        + careSetting;
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
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
		return HibernateUtil.getRealObjectFromProxy(previousOrder);
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
	 * <strong>Should</strong> set all the relevant fields
	 */
	public Order cloneForDiscontinuing() {
		Order newOrder = new Order();
		newOrder.setCareSetting(getCareSetting());
		newOrder.setConcept(getConcept());
		newOrder.setAction(Action.DISCONTINUE);
		newOrder.setPreviousOrder(this);
		newOrder.setPatient(getPatient());
		newOrder.setOrderType(getOrderType());
		
		return newOrder;
	}
	
	/**
	 * Creates an order for revision from this order, sets the previousOrder and action field.
	 * 
	 * @return the newly created order
	 * @since 1.10
	 * <strong>Should</strong> set all the relevant fields
	 * <strong>Should</strong> set the relevant fields for a DC order
	 */
	public Order cloneForRevision() {
		return cloneForRevisionHelper(new Order());
	}
	
	/**
	 * The purpose of this method is to allow subclasses of Order to delegate a portion of their
	 * cloneForRevision() method back to the superclass, in case the base class implementation
	 * changes.
	 * 
	 * @param target an Order that will have the state of <code>this</code> copied into it
	 * @return Returns the Order that was passed in, with state copied into it
	 */
	protected Order cloneForRevisionHelper(Order target) {
		if (getAction() == Action.DISCONTINUE) {
			target.setAction(Action.DISCONTINUE);
			target.setPreviousOrder(getPreviousOrder());
			target.setDateActivated(getDateActivated());
		} else {
			target.setAction(Action.REVISE);
			target.setPreviousOrder(this);
			target.setAutoExpireDate(getAutoExpireDate());
		}
		target.setCareSetting(getCareSetting());
		target.setConcept(getConcept());
		target.setPatient(getPatient());
		target.setOrderType(getOrderType());
		target.setScheduledDate(getScheduledDate());
		target.setInstructions(getInstructions());
		target.setUrgency(getUrgency());
		target.setCommentToFulfiller(getCommentToFulfiller());
		target.setOrderReason(getOrderReason());
		target.setOrderReasonNonCoded(getOrderReasonNonCoded());
		target.setOrderGroup(getOrderGroup());
		target.setSortWeight(getSortWeight());
		target.setFulfillerStatus(getFulfillerStatus());
		target.setFulfillerComment(getFulfillerComment());
		target.setFormNamespaceAndPath(getFormNamespaceAndPath());
		
		return target;
	}
	
	/**
	 * Checks whether this order's orderType matches or is a sub type of the specified one
	 * 
	 * @since 1.10
	 * @param orderType the orderType to match on
	 * @return true if the type of the order matches or is a sub type of the other order
	 * <strong>Should</strong> true if it is the same or is a subtype
	 * <strong>Should</strong> false if it neither the same nor a subtype
	 */
	public boolean isType(OrderType orderType) {
		return OrderUtil.isType(orderType, this.orderType);
	}
	
	/**
	 * Checks whether orderable of this order is same as other order
	 * 
	 * @see org.openmrs.DrugOrder for overridden behaviour
	 * @since 1.10
	 * @param otherOrder the other order to match on
	 * @return true if the concept of the orders match
	 * <strong>Should</strong> return false if the concept of the orders do not match
	 * <strong>Should</strong> return false if other order is null
	 * <strong>Should</strong> return true if the orders have the same concept
	 */
	public boolean hasSameOrderableAs(Order otherOrder) {
		if (otherOrder == null) {
			return false;
		}
		return OpenmrsUtil.nullSafeEquals(this.getConcept(), otherOrder.getConcept());
	}
	
	/**
	 * A convenience method to return start of the schedule for order.
	 * 
	 * @since 1.10
	 * <strong>Should</strong> return scheduledDate if Urgency is Scheduled
	 * <strong>Should</strong> return dateActivated if Urgency is not Scheduled
	 */
	public Date getEffectiveStartDate() {
		return this.urgency == Urgency.ON_SCHEDULED_DATE ? this.getScheduledDate() : this.getDateActivated();
	}
	
	/**
	 * A convenience method to return end of the schedule for order.
	 * 
	 * @since 1.10
	 * <strong>Should</strong> return dateStopped if dateStopped is not null
	 * <strong>Should</strong> return autoExpireDate if dateStopped is null
	 */
	public Date getEffectiveStopDate() {
		return this.getDateStopped() != null ? this.getDateStopped() : this.getAutoExpireDate();
	}
	
	/**
	 * @since 1.12 {@link org.openmrs.OrderGroup}
	 * @returns the OrderGroup
	 */
	public OrderGroup getOrderGroup() {
		return orderGroup;
	}
	
	/**
	 * Sets the OrderGroup for that order. If the order is ordered independently, it does not set an
	 * orderGroup for it. If the order is ordered as an orderGroup, then sets a link to the
	 * OrderGroup for that particular order.
	 * 
	 * @since 1.12
	 * @param orderGroup
	 */
	public void setOrderGroup(OrderGroup orderGroup) {
		this.orderGroup = orderGroup;
	}
	
	/**
	 * Gets the sortWeight for an order if it is ordered as an OrderGroup.
	 * 
	 * @since 1.12
	 * @return the sortWeight
	 */
	public Double getSortWeight() {
		return sortWeight;
	}
	
	/**
	 * Sets the sortWeight for an order if it is ordered as an OrderGroup. <tt>sortWeight</tt> is
	 * used internally by the API to manage the sequencing of orders when grouped. This value may be
	 * changed by the API as needed for that purpose. Instead of setting this internal value
	 * directly please use {@link OrderGroup#addOrder(Order, Integer)}.
	 * 
	 * @see OrderGroup#addOrder(Order, Integer)
	 * @since 1.12
	 * @param sortWeight
	 */
	public void setSortWeight(Double sortWeight) {
		this.sortWeight = sortWeight;
	}
	
	/**
	 * Returns the current status that was received from a fulfiller for this order. It can either be RECEIVED, IN_PROGRESS,
	 * EXCEPTION or COMPLETED.  
	 * 
	 * @since 2.2.0
	 * @return the status that was received from a fulfiller
	 */
	public FulfillerStatus getFulfillerStatus() {
		return fulfillerStatus;
	}

	/**
	 * Sets the status of this order according to the value that was received from a fulfiller. 
	 * 
	 * @param fulfillerStatus the status that was received from a fulfiller. 
	 * @since 2.2.0
	*/
	public void setFulfillerStatus(FulfillerStatus fulfillerStatus) {
		this.fulfillerStatus = fulfillerStatus;
	}
	
	/**
	 * Returns the comment received from the fulfiller regarding this order.
	 * 
	 * @since 2.2.0
	 * @return the comment of the fulfiller  
	 */
	public String getFulfillerComment() {
		return fulfillerComment;
	}
	
	/**
	 * Sets the comment received from the fulfiller for this order.
	 * 
	 * @param fulfillerComment the comment received from the fulfiller
	 * @since 2.2.0
	 */
	public void setFulfillerComment(String fulfillerComment) {
		this.fulfillerComment = fulfillerComment;		
	}
	
	/**
	 * @return Returns the formNamespaceAndPath.
	 * @since 2.5.0
	 */
	public String getFormNamespaceAndPath() {
		return formNamespaceAndPath;
	}

	/**
	 * Sets the form namespace and path
	 * 
	 * @param formNamespaceAndPath the form namespace and path to set
	 * @since 2.5.0
	 */
	public void setFormNamespaceAndPath(String formNamespaceAndPath) {
		this.formNamespaceAndPath = formNamespaceAndPath;
	}

	@Override
	public String getFormFieldNamespace() {
		return BaseFormRecordableOpenmrsData.getFormFieldNamespace(formNamespaceAndPath);
	}

	@Override
	public String getFormFieldPath() {
		return BaseFormRecordableOpenmrsData.getFormFieldPath(formNamespaceAndPath);
	}

	@Override
	public void setFormField(String namespace, String formFieldPath) {
		formNamespaceAndPath = BaseFormRecordableOpenmrsData.getFormNamespaceAndPath(namespace, formFieldPath);
	}
}
