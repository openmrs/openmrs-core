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
 * DrugOrder
 * 
 * @version 1.0
 */
public class DrugOrder extends Order implements java.io.Serializable {
	
	public static final long serialVersionUID = 72232L;
	
	/**
	 * enum dosingType
	 * 
	 * @since 1.10
	 */
	public enum DosingType {
		SIMPLE, FREE_TEXT;
	}
	
	// Fields
	
	private Double dose;
	
	private Concept doseUnits;
	
	private OrderFrequency frequency;
	
	private Boolean asNeeded = false;
	
	private Double quantity;
	
	private Concept quantityUnits;
	
	private Drug drug;
	
	private String asNeededCondition;
	
	private DosingType dosingType = DosingType.SIMPLE;
	
	private Integer numRefills;
	
	private String administrationInstructions;
	
	private String dosingInstructions;
	
	private Double duration;
	
	private Concept durationUnits;
	
	private Concept route;
	
	// Constructors
	
	/** default constructor */
	public DrugOrder() {
	}
	
	/** constructor with id */
	public DrugOrder(Integer orderId) {
		this.setOrderId(orderId);
	}
	
	/**
	 * @see org.openmrs.Order#copy()
	 * @should copy all drug order fields
	 */
	public DrugOrder copy() {
		return copyHelper(new DrugOrder());
	}
	
	/**
	 * @see org.openmrs.Order#copyHelper(Order)
	 */
	protected DrugOrder copyHelper(DrugOrder target) {
		super.copyHelper(target);
		target.dose = getDose();
		target.doseUnits = getDoseUnits();
		target.frequency = getFrequency();
		target.asNeeded = getAsNeeded();
		target.asNeededCondition = getAsNeededCondition();
		target.quantity = getQuantity();
		target.quantityUnits = getQuantityUnits();
		target.drug = getDrug();
		target.dosingType = getDosingType();
		target.dosingInstructions = getDosingInstructions();
		target.duration = getDuration();
		target.durationUnits = getDurationUnits();
		target.setAdministrationInstructions(getAdministrationInstructions());
		target.setNumRefills(getNumRefills());
		target.route = getRoute();
		return target;
	}
	
	public boolean isDrugOrder() {
		return true;
	}
	
	// Property accessors
	
	/**
	 * Gets the doseUnits of this drug order
	 * 
	 * @return doseUnits
	 */
	public Concept getDoseUnits() {
		return this.doseUnits;
	}
	
	/**
	 * Sets the doseUnits of this drug order
	 * 
	 * @param doseUnits
	 */
	public void setDoseUnits(Concept doseUnits) {
		this.doseUnits = doseUnits;
	}
	
	/**
	 * Gets the frequency
	 * 
	 * @return frequency
	 * @since 1.10 (signature changed)
	 */
	public OrderFrequency getFrequency() {
		return this.frequency;
	}
	
	/**
	 * Sets the frequency
	 * 
	 * @param frequency
	 * @since 1.10 (signature changed)
	 */
	public void setFrequency(OrderFrequency frequency) {
		this.frequency = frequency;
	}
	
	/**
	 * @deprecated see {@link #getAsNeeded()}
	 * @return Boolean
	 */
	@Deprecated
	public Boolean getPrn() {
		return getAsNeeded();
	}
	
	/**
	 * @deprecated see {@link #setAsNeeded(Boolean)}
	 * @param prn
	 */
	@Deprecated
	public void setPrn(Boolean prn) {
		setAsNeeded(prn);
	}
	
	/**
	 * Returns true/false whether the drug is a "pro re nata" drug
	 * 
	 * @return Boolean
	 * @since 1.10
	 */
	public Boolean getAsNeeded() {
		return asNeeded;
	}
	
	/**
	 * @param asNeeded the value to set
	 * @since 1.10
	 */
	public void setAsNeeded(Boolean asNeeded) {
		this.asNeeded = asNeeded;
	}
	
	/**
	 * Gets whether this drug is complex
	 * 
	 * @return Boolean
	 * @deprecated use {@link #getDosingType()}
	 */
	@Deprecated
	public Boolean getComplex() {
		return this.dosingType != DosingType.SIMPLE;
	}
	
	/**
	 * Sets whether this drug is complex
	 * 
	 * @param complex
	 * @deprecated use {@link #setComplex(Boolean)}
	 */
	@Deprecated
	public void setComplex(Boolean complex) {
		if (complex) {
			setDosingType(DosingType.FREE_TEXT);
		} else {
			setDosingType(DosingType.SIMPLE);
		}
	}
	
	/**
	 * Gets the quantity
	 * 
	 * @return quantity
	 */
	public Double getQuantity() {
		return this.quantity;
	}
	
	/**
	 * Sets the quantity
	 * 
	 * @param quantity
	 */
	public void setQuantity(Double quantity) {
		this.quantity = quantity;
	}
	
	/**
	 * @since 1.10
	 * @return concept
	 */
	public Concept getQuantityUnits() {
		return quantityUnits;
	}
	
	/**
	 * @since 1.10
	 * @param quantityUnits
	 */
	public void setQuantityUnits(Concept quantityUnits) {
		this.quantityUnits = quantityUnits;
	}
	
	/**
	 * Gets the drug
	 * 
	 * @return drug
	 */
	public Drug getDrug() {
		return this.drug;
	}
	
	/**
	 * Sets the drug
	 * 
	 * @param drug
	 */
	public void setDrug(Drug drug) {
		this.drug = drug;
	}
	
	/**
	 * @return the asNeededCondition
	 * @since 1.10
	 */
	public String getAsNeededCondition() {
		return asNeededCondition;
	}
	
	/**
	 * @param asNeededCondition the asNeededCondition to set
	 * @since 1.10
	 */
	public void setAsNeededCondition(String asNeededCondition) {
		this.asNeededCondition = asNeededCondition;
	}
	
	/**
	 * Gets the route
	 * 
	 * @since 1.10
	 */
	public Concept getRoute() {
		return route;
	}
	
	/**
	 * Sets the route
	 * 
	 * @param route
	 * @since 1.10
	 */
	public void setRoute(Concept route) {
		this.route = route;
	}
	
	public void setDose(Double dose) {
		this.dose = dose;
	}
	
	public Double getDose() {
		return dose;
	}
	
	/**
	 * Gets the dosingType
	 * 
	 * @since 1.10
	 */
	public DosingType getDosingType() {
		return dosingType;
	}
	
	/**
	 * Sets the dosingType
	 * 
	 * @param dosingType the DosingType to set
	 * @since 1.10
	 */
	public void setDosingType(DosingType dosingType) {
		this.dosingType = dosingType;
	}
	
	/**
	 * Gets numRefills
	 * 
	 * @since 1.10
	 */
	public Integer getNumRefills() {
		return numRefills;
	}
	
	/**
	 * Sets numRefills
	 * 
	 * @param numRefills the numRefills to set
	 * @since 1.10
	 */
	public void setNumRefills(Integer numRefills) {
		this.numRefills = numRefills;
	}
	
	/**
	 * Gets the administrationInstructions
	 * 
	 * @since 1.10
	 */
	public String getAdministrationInstructions() {
		return administrationInstructions;
	}
	
	/**
	 * Sets the administrationInstructions
	 * 
	 * @param administrationInstructions to set
	 * @since 1.10
	 */
	public void setAdministrationInstructions(String administrationInstructions) {
		this.administrationInstructions = administrationInstructions;
	}
	
	/**
	 * Sets the dosingInstructions
	 * 
	 * @param dosingInstructions to set
	 * @since 1.10
	 */
	public void setDosingInstructions(String dosingInstructions) {
		this.dosingInstructions = dosingInstructions;
	}
	
	/**
	 * Gets the dosingInstructions
	 * 
	 * @since 1.10
	 */
	public String getDosingInstructions() {
		return this.dosingInstructions;
	}
	
	/**
	 * Gets the duration of a Drug Order
	 * 
	 * @since 1.10
	 */
	public Double getDuration() {
		return duration;
	}
	
	/**
	 * Sets the duration of a Drug Order
	 * 
	 * @param duration to set
	 * @since 1.10
	 */
	public void setDuration(Double duration) {
		this.duration = duration;
	}
	
	/**
	 * Gets durationUnits of a Drug Order
	 * 
	 * @since 1.10
	 */
	public Concept getDurationUnits() {
		return durationUnits;
	}
	
	/**
	 * Sets the durationUnits of a Drug Order
	 * 
	 * @param durationUnits
	 * @since 1.10
	 */
	public void setDurationUnits(Concept durationUnits) {
		this.durationUnits = durationUnits;
	}
	
	/**
	 * @see org.openmrs.Order#cloneForDiscontinuing()
	 * @should set all the relevant fields
	 * @since 1.10
	 */
	@Override
	public DrugOrder cloneForDiscontinuing() {
		DrugOrder newOrder = new DrugOrder();
		newOrder.setCareSetting(this.getCareSetting());
		newOrder.setConcept(this.getConcept());
		newOrder.setAction(Action.DISCONTINUE);
		newOrder.setPreviousOrder(this);
		newOrder.setPatient(this.getPatient());
		newOrder.setDrug(this.getDrug());
		newOrder.setOrderType(this.getOrderType());
		return newOrder;
	}
	
	/**
	 * Creates a DrugOrder for revision from this order, sets the previousOrder, action field and
	 * other drug order fields.
	 * 
	 * @return the newly created order
	 * @since 1.10
	 * @should set all the relevant fields
	 */
	@Override
	public DrugOrder cloneForRevision() {
		DrugOrder newOrder = new DrugOrder();
		newOrder.setCareSetting(this.getCareSetting());
		newOrder.setConcept(this.getConcept());
		newOrder.setAction(Action.REVISE);
		newOrder.setPreviousOrder(this);
		newOrder.setPatient(this.getPatient());
		newOrder.setOrderType(this.getOrderType());
		newOrder.setInstructions(this.getInstructions());
		newOrder.setUrgency(this.getUrgency());
		newOrder.setCommentToFulfiller(this.getCommentToFulfiller());
		newOrder.setAccessionNumber(this.getAccessionNumber());
		newOrder.setAutoExpireDate(this.getAutoExpireDate());
		newOrder.setOrderReason(this.getOrderReason());
		newOrder.setOrderReasonNonCoded(this.getOrderReasonNonCoded());
		newOrder.setScheduledDate(getScheduledDate());
		newOrder.setDose(this.getDose());
		newOrder.setDoseUnits(this.getDoseUnits());
		newOrder.setFrequency(this.getFrequency());
		newOrder.setAsNeeded(this.getAsNeeded());
		newOrder.setAsNeededCondition(this.getAsNeededCondition());
		newOrder.setQuantity(this.getQuantity());
		newOrder.setQuantityUnits(this.getQuantityUnits());
		newOrder.setDrug(this.getDrug());
		newOrder.setDosingType(this.getDosingType());
		newOrder.setDosingInstructions(this.getDosingInstructions());
		newOrder.setDuration(this.getDuration());
		newOrder.setDurationUnits(this.getDurationUnits());
		newOrder.setRoute(this.getRoute());
		newOrder.setAdministrationInstructions(this.getAdministrationInstructions());
		newOrder.setNumRefills(this.getNumRefills());
		return newOrder;
	}
	
	public String toString() {
		return "DrugOrder(" + getDose() + getDoseUnits() + " of " + (getDrug() != null ? getDrug().getName() : "[no drug]")
		        + " from " + getStartDate() + " to " + (isDiscontinuedRightNow() ? getDateStopped() : getAutoExpireDate())
		        + ")";
	}
	
}
