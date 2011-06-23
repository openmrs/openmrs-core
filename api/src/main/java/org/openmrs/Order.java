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

/**
 * Dates should be interpreted as follows: If startDate is null then the order has been going on
 * "since the beginning of time" Otherwise the order starts on startDate If discontinued is non-null
 * and true, then the following fields should be ignored: autoExpireDate if discontinuedDate is null
 * then the order was discontinued "the instant after it began" otherwise it was given from its
 * starting date until discontinuedDate Otherwise (discontinued is null or false) if autoExpireDate
 * is null, the order is set to go forever otherwise the order goes until autoExpireDate the
 * following fields should be ignored: discontinuedBy discontinuedDate discontinuedReason It is an
 * error to have discontinued be true and have discontinuedDate be after autoExpireDate. However
 * this is not checked for in the database or the application.
 * 
 * @version 1.0
 */
public class Order extends BaseOpenmrsData implements java.io.Serializable {
	
	public static final long serialVersionUID = 4334343L;
	
	// Fields
	
	private Integer orderId;
	
	private Patient patient;
	
	/** This would be an enumeration of order types Ð e.g., DRUG, TEST, REFERRAL, DIET, etc. */
	private OrderType orderType;
	
	private Concept concept;
	
	/**
	 * Free text instructions for the order 
	 * (e.g., details about a referral, justification for a cardiac stress test, etc.)
	 */
	private String instructions;
	
	/** When the order should begin. */
	private Date startDate;
	
	/** When the order should be discontinued if it hasn't already. */
	private Date autoExpireDate;
	
	private Encounter encounter;
	
	private User orderer;
	
	private Boolean discontinued = false;
	
	private User discontinuedBy;
	
	/** When the order was discontinued. */
	private Date discontinuedDate;
	
	/** This is optional text that would go on the D/C order (this was a coded answer in previous versions of openmrs). */
	private Concept discontinuedReason;
	
	private String accessionNumber;
	
	private String discontinuedReasonNonCoded;
	
	/**
	 * This is an identifier generated for a given order and shared by all revisions (if any) of
	 * that order. The order number is passed to ancillary systems in order for results & events
	 * related to the order to be connected to the original order.
	 */
	private String orderNumber;
	
	/**
	 * Allows orders to be linked to a previous order - e.g., an order discontinue ampicillin linked
	 * to the original ampicillin order (the D/C gets its own order number)
	 */
	private String previousOrderNumber;
	
	/**
	 * Allows orders to be grouped. e.g., drug regimens. Orders may be placed within groups to
	 * assist with subsequent management or reporting of the orders (e.g., a drug regimen of three
	 * drugs may be placed within an order group).
	 */
	private Integer orderGroup;
	
	/**
	 * Represents the action being taken on an order.
	 * 
	 * @see OrderAction
	 */
	private OrderAction orderAction;
	
	/**
	 * Allows for orders to be created for items that are not yet in the dictionary. e.g., OTHER
	 * DRUG ORDER #1 with non-coded name "CANE".
	 */
	private String nonCodedName;
	
	/**
	 * Specifies when the order should first occur (e.g., stat/immediately, routine, on a specific
	 * date, etc.)
	 */
	private String urgency;
	
	/**
	 * For orders with a CONDITIONAL urgency, this property contains free text describing the 
	 * condition(s) under which the order should be performed, e.g., "when the patient returns from surgery"
	 */
	private String conditionality;
	
	/**
	 * Describes the frequency of repeats for an order 
	 * (note: eventually, we may want to draw these from a table of possible values)
	 */
	private String frequency;
	
	/** The reason for the order. */
	private Concept indication;
	
	/** Free text comments. */
	private String comment;
	
	/** User responsible for the order. */
	private User signedBy;
	
	/** When order was signed. */
	private Date dateSigned;
	
	/** 
	 * User who activates the order so that it could be carried out 
	 * (may be different from signing user in some cases). 
	 */
	private User activatedBy;
	
	/** When order was activated. */
	private Date dateActivated;
	
	/**
	 * This is an optional URI to a person or process that fulfilled the order - possibly even a
	 * pointer to the object/resource that represents the result.
	 * Unique reference to the party responsible for filling or carrying out the order, 
	 * e.g., the lab that reported the result or the pharmacy the filled the prescription 
	 * (note: we will need a convention for formatting this)
	 */
	private String filler;
	
	/** When order was filled. */
	private Date dateFilled;
	
	/**
	 * Represents an enumeration of the actions that can be taken on an order
	 * 
	 * @since 1.9
	 */
	public enum OrderAction {
		NEW, REVISE, DISCONTINUE, RENEW, CARRY_OVER
	}
	
	// Constructors
	
	/** default constructor */
	public Order() {
	}
	
	/** constructor with id */
	public Order(Integer orderId) {
		this.orderId = orderId;
	}
	
	public Order(Integer orderId, Patient patient, OrderType orderType, Concept concept) {
		this.setOrderId(orderId);
		this.setPatient(patient);
		this.setOrderType(orderType);
		this.setConcept(concept);
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
		target.setAccessionNumber(getAccessionNumber());
		target.setVoided(isVoided());
		target.setVoidedBy(getVoidedBy());
		target.setDateVoided(getDateVoided());
		target.setVoidReason(getVoidReason());
		target.setDiscontinuedReasonNonCoded(getDiscontinuedReasonNonCoded());
		target.setOrderNumber(getOrderNumber());
		target.setPreviousOrderNumber(getPreviousOrderNumber());
		target.setOrderGroup(getOrderGroup());
		target.setOrderAction(getOrderAction());
		target.setNonCodedName(getNonCodedName());
		target.setUrgency(getUrgency());
		target.setConditionality(getConditionality());
		target.setFrequency(getFrequency());
		target.setIndication(getIndication());
		target.setComment(getComment());
		target.setSignedBy(getSignedBy());
		target.setDateSigned(getDateSigned());
		target.setActivatedBy(getActivatedBy());
		target.setDateActivated(getDateActivated());
		target.setFiller(getFiller());
		target.setDateFilled(getDateFilled());
		
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
			Order o = (Order) obj;
			if (this.getOrderId() != null && o.getOrderId() != null)
				return (this.getOrderId().equals(o.getOrderId()));
			/*
			 * return (this.getOrderType().equals(o.getOrderType()) &&
			 * this.getConcept().equals(o.getConcept()) &&
			 * this.getEncounter().equals(o.getEncounter()) &&
			 * this.getInstructions().matches(o.getInstructions()));
			 */
		}
		return false;
	}
	
	public int hashCode() {
		if (this.getOrderId() == null)
			return super.hashCode();
		return this.getOrderId().hashCode();
	}
	
	/**
	 * true/false whether or not this is a drug order overridden in extending class drugOrders.
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
	 * @return Returns the discontinued status.
	 * @should get discontinued property
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
	 * @return the discontinuedReasonNonCoded
	 */
	public String getDiscontinuedReasonNonCoded() {
		return discontinuedReasonNonCoded;
	}
	
	/**
	 * @param discontinuedReasonNonCoded the discontinuedReasonNonCoded to set
	 */
	public void setDiscontinuedReasonNonCoded(String discontinuedReasonNonCoded) {
		this.discontinuedReasonNonCoded = discontinuedReasonNonCoded;
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
		 * if (discontinuedDate == null) { return false; }
		 */
		return true;
	}
	
	/**
	 * orderForm:jsp: <spring:bind path="order.discontinued" /> results in a call to
	 * isDiscontinued() which doesn't give access to the discontinued property so renamed it to
	 * isDiscontinuedRightNow which results in a call to getDiscontinued.
	 * @since 1.5
	 */
	public boolean isDiscontinuedRightNow() {
		return isDiscontinued(new Date());
	}
	
	/**
	 * Gets the patient.
	 * 
	 * @return the patient.
	 */
	public Patient getPatient() {
		return patient;
	}
	
	/**
	 * Sets the patient.
	 * 
	 * @param patient the patient to set.
	 */
	public void setPatient(Patient patient) {
		this.patient = patient;
	}
	
	/**
	 * Gets the order number.
	 * 
	 * @return the order number.
	 */
	public String getOrderNumber() {
		return orderNumber;
	}
	
	/**
	 * Sets the order number.
	 * 
	 * @param orderNumber the order number to set.
	 */
	public void setOrderNumber(String orderNumber) {
		this.orderNumber = orderNumber;
	}
	
	/**
	 * Gets the previous order number.
	 * 
	 * @return the previous order number.
	 */
	public String getPreviousOrderNumber() {
		return previousOrderNumber;
	}
	
	/**
	 * Sets the previous order number.
	 * 
	 * @param previousOrderNumber the previous order number to set.
	 */
	public void setPreviousOrderNumber(String previousOrderNumber) {
		this.previousOrderNumber = previousOrderNumber;
	}
	
	/**
	 * Gets the order group.
	 * 
	 * @return the order group.
	 */
	public Integer getOrderGroup() {
		return orderGroup;
	}
	
	/**
	 * Sets the order group.
	 * 
	 * @param orderGroup the order group to set.
	 */
	public void setOrderGroup(Integer orderGroup) {
		this.orderGroup = orderGroup;
	}
	
	/**
	 * @return the orderAction
	 * @since 1.9
	 */
	public OrderAction getOrderAction() {
		return orderAction;
	}
	
	/**
	 * @param orderAction the orderAction to set
	 * @since 1.9
	 */
	public void setOrderAction(OrderAction orderAction) {
		this.orderAction = orderAction;
	}
	
	/**
	 * Gets the non coded name.
	 * 
	 * @return the non coded name.
	 */
	public String getNonCodedName() {
		return nonCodedName;
	}
	
	/**
	 * Sets the non coded name.
	 * 
	 * @param nonCodedName the non coded name to set.
	 */
	public void setNonCodedName(String nonCodedName) {
		this.nonCodedName = nonCodedName;
	}
	
	/**
	 * Gets the urgency.
	 * 
	 * @return the urgency.
	 */
	public String getUrgency() {
		return urgency;
	}
	
	/**
	 * Sets the urgency.
	 * 
	 * @param urgency the urgency to set.
	 */
	public void setUrgency(String urgency) {
		this.urgency = urgency;
	}
	
	/**
	 * Gets the conditionality.
	 * 
	 * @return the conditionality.
	 */
	public String getConditionality() {
		return conditionality;
	}
	
	/**
	 * Sets the conditionality.
	 * 
	 * @param conditionality the conditionality to set.
	 */
	public void setConditionality(String conditionality) {
		this.conditionality = conditionality;
	}
	
	/**
	 * Gets the frequency.
	 * 
	 * @return the frequency.
	 */
	public String getFrequency() {
		return frequency;
	}
	
	/**
	 * Sets the frequency.
	 * 
	 * @param frequency the frequency to set.
	 */
	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}
	
	/**
	 * @return the indication
	 * @since 1.9
	 */
	public Concept getIndication() {
		return indication;
	}
	
	/**
	 * @param indication the indication to set
	 * @since 1.9
	 */
	public void setIndication(Concept indication) {
		this.indication = indication;
	}
	
	/**
	 * Gets the comment.
	 * 
	 * @return the comment.
	 */
	public String getComment() {
		return comment;
	}
	
	/**
	 * Sets the comment.
	 * 
	 * @param comment the comment.
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	/**
	 * Gets the user who signed the order.
	 * 
	 * @return the user who signed the order.
	 */
	public User getSignedBy() {
		return signedBy;
	}
	
	/**
	 * Sets the user who signed the order.
	 * 
	 * @param signedBy the user who signed the order.
	 */
	public void setSignedBy(User signedBy) {
		this.signedBy = signedBy;
	}
	
	/**
	 * Gets the date when the order was signed.
	 * 
	 * @return the date when the order was signed.
	 */
	public Date getDateSigned() {
		return dateSigned;
	}
	
	/**
	 * Sets the date when the order was signed.
	 * 
	 * @param dateSigned the date when the order was signed.
	 */
	public void setDateSigned(Date dateSigned) {
		this.dateSigned = dateSigned;
	}
	
	/**
	 * Gets the user who activated the order.
	 * 
	 * @return the user who activated the order.
	 */
	public User getActivatedBy() {
		return activatedBy;
	}
	
	/**
	 * Sets the user who activated the order.
	 * 
	 * @param activatedBy the user who activated the order.
	 */
	public void setActivatedBy(User activatedBy) {
		this.activatedBy = activatedBy;
	}
	
	/**
	 * Gets the date when order was activated.
	 * 
	 * @return the date activated.
	 */
	public Date getDateActivated() {
		return dateActivated;
	}
	
	/**
	 * Sets the date activated.
	 * 
	 * @param dateActivated the date activated to set.
	 */
	public void setDateActivated(Date dateActivated) {
		this.dateActivated = dateActivated;
	}
	
	/**
	 * Gets the filler.
	 * 
	 * @return the filler.
	 */
	public String getFiller() {
		return filler;
	}
	
	/**
	 * Sets the filler.
	 * 
	 * @param filler the filler to set.
	 */
	public void setFiller(String filler) {
		this.filler = filler;
	}
	
	/**
	 * Gets the date filled.
	 * 
	 * @return the date filled.
	 */
	public Date getDateFilled() {
		return dateFilled;
	}
	
	/**
	 * Sets the date filled.
	 * 
	 * @param dateFilled the date filled to set.
	 */
	public void setDateFilled(Date dateFilled) {
		this.dateFilled = dateFilled;
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	public Integer getId() {
		return getOrderId();
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "Order. orderId: " + orderId + " patient: " + patient + " orderType: " + orderType + " concept: " + concept;
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	public void setId(Integer id) {
		setOrderId(id);
	}
	
	/**
	 * Checks if an order is signed.
	 * 
	 * @return true if signed, else false.
	 */
	public boolean isSigned() {
		return getSignedBy() != null && getDateSigned() != null;
	}
	
	/**
	 * Checks if an order is activated.
	 * 
	 * @return true if activated, else false.
	 */
	public boolean isActivated() {
		return getActivatedBy() != null && getDateActivated() != null;
	}
}
