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
	
	private String dosingInstructions;
	
	private Double duration;
	
	private Concept durationUnits;
	
	private Concept route;
	
	private String brandName;
	
	private Boolean dispenseAsWritten = Boolean.FALSE;
	
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
		target.setDose(getDose());
		target.setDoseUnits(getDoseUnits());
		target.setFrequency(getFrequency());
		target.setAsNeeded(getAsNeeded());
		target.setAsNeededCondition(getAsNeededCondition());
		target.setQuantity(getQuantity());
		target.setQuantityUnits(getQuantityUnits());
		target.setDrug(getDrug());
		target.setDosingType(getDosingType());
		target.setDosingInstructions(getDosingInstructions());
		target.setDuration(getDuration());
		target.setDurationUnits(getDurationUnits());
		target.setNumRefills(getNumRefills());
		target.setRoute(getRoute());
		target.setBrandName(getBrandName());
		target.setDispenseAsWritten(getDispenseAsWritten());
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
		if (drug != null && getConcept() == null) {
			setConcept(drug.getConcept());
		}
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
	 * Gets the brandName
	 * 
	 * @return brandName
	 * @since 1.10
	 */
	public String getBrandName() {
		return brandName;
	}
	
	/**
	 * Sets the brandName
	 * 
	 * @since 1.10
	 * @param brandName the brandName to set to
	 */
	public void setBrandName(String brandName) {
		this.brandName = brandName;
	}
	
	/**
	 * @return true or false
	 * @since 1.10
	 */
	public Boolean getDispenseAsWritten() {
		return dispenseAsWritten;
	}
	
	/**
	 * @param dispenseAsWritten
	 * @since 1.10
	 */
	public void setDispenseAsWritten(Boolean dispenseAsWritten) {
		this.dispenseAsWritten = dispenseAsWritten;
	}
	
	/**
	 * @see org.openmrs.Order#cloneForDiscontinuing()
	 * @should set all the relevant fields
	 * @since 1.10
	 */
	@Override
	public DrugOrder cloneForDiscontinuing() {
		DrugOrder newOrder = new DrugOrder();
		newOrder.setCareSetting(getCareSetting());
		newOrder.setConcept(getConcept());
		newOrder.setAction(Action.DISCONTINUE);
		newOrder.setPreviousOrder(this);
		newOrder.setPatient(getPatient());
		newOrder.setDrug(getDrug());
		newOrder.setOrderType(getOrderType());
		return newOrder;
	}
	
	/**
	 * Creates a DrugOrder for revision from this order, sets the previousOrder, action field and
	 * other drug order fields.
	 * 
	 * @return the newly created order
	 * @since 1.10
	 * @should set all the relevant fields
	 * @should set the relevant fields for a DC order
	 */
	@Override
	public DrugOrder cloneForRevision() {
		return cloneForRevisionHelper(new DrugOrder());
	}
	
	/**
	 * @see Order#cloneForRevisionHelper(Order)
	 */
	protected DrugOrder cloneForRevisionHelper(DrugOrder target) {
		super.cloneForRevisionHelper(target);
		target.setDose(getDose());
		target.setDoseUnits(getDoseUnits());
		target.setFrequency(getFrequency());
		target.setAsNeeded(getAsNeeded());
		target.setAsNeededCondition(getAsNeededCondition());
		target.setQuantity(getQuantity());
		target.setQuantityUnits(getQuantityUnits());
		target.setDrug(getDrug());
		target.setDosingType(getDosingType());
		target.setDosingInstructions(getDosingInstructions());
		target.setDuration(getDuration());
		target.setDurationUnits(getDurationUnits());
		target.setRoute(getRoute());
		target.setNumRefills(getNumRefills());
		target.setBrandName(getBrandName());
		target.setDispenseAsWritten(getDispenseAsWritten());
		
		return target;
	}
	
	public String toString() {
		String prefix = Action.DISCONTINUE == getAction() ? "DC " : "";
		return prefix + "DrugOrder(" + getDose() + getDoseUnits() + " of "
		        + (getDrug() != null ? getDrug().getName() : "[no drug]") + " from " + getStartDate() + " to "
		        + (isDiscontinuedRightNow() ? getDateStopped() : getAutoExpireDate()) + ")";
	}
	
	/**
	 * Set dosing instructions to drug order
	 * 
	 * @param di dosing instruction object to fetch data
	 * @since 1.10
	 */
	public void setDosing(DosingInstructions di) {
		di.setDosingInstructions(this);
	}
}
