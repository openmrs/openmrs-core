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
 * This is a type of order that adds drug specific attributes.
 * 
 * @version 1.0
 */
public class DrugOrder extends Order implements java.io.Serializable {
	
	public static final long serialVersionUID = 1L;
	
	// Fields
	
	private Drug drug;
	
	private Double dose;
	
	private String doseUnits;
	
	Boolean isStructuredDosing = false;
	
	Double strength;
	
	String strengthUnits;
	
	Integer quantity;
	
	String quantityUnits;
	
	String frequency;
	
	String brandName;
	
	String dosageForm;
	
	String route;
	
	Boolean asNeeded = false; // - Indicates PRN
	
	String asNeededCondition;
	
	String additionalInstructions;
	
	Integer duration;
	
	String durationUnits;
	
	Integer numRefills;
	
	// Constructors
	
	/** default constructor */
	public DrugOrder() {
	}
	
	/** constructor with id */
	public DrugOrder(Integer orderId) {
		this.setOrderId(orderId);
	}
	
	public DrugOrder(Integer orderId, String brandName) {
		this.setOrderId(orderId);
		this.setBrandName(brandName);
	}
	
	/**
	 * @see org.openmrs.Order#copy()
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
		target.asNeeded = getAsNeeded();
		target.quantity = getQuantity();
		target.quantityUnits = getQuantityUnits();
		target.drug = getDrug();
		
		target.brandName = getBrandName();
		target.isStructuredDosing = getIsStructuredDosing();
		target.strength = getStrength();
		target.strengthUnits = getStrengthUnits();
		target.dosageForm = getDosageForm();
		target.route = getRoute();
		target.asNeededCondition = getAsNeededCondition();
		target.additionalInstructions = getAdditionalInstructions();
		target.numRefills = getNumRefills();
		target.duration = getDuration();
		target.durationUnits = getDurationUnits();
		
		return target;
	}
	
	public boolean isDrugOrder() {
		return true;
	}
	
	// Property accessors
	
	/**
	 * Gets the dose units of this drug order
	 * 
	 * @return doseUnits
	 */
	public String getDoseUnits() {
		return this.doseUnits;
	}
	
	/**
	 * Sets the dose units of this drug order
	 * 
	 * @param units
	 */
	public void setDoseUnits(String doseUnits) {
		this.doseUnits = doseUnits;
	}
	
	/**
	 * @return the asNeeded
	 */
	public Boolean getAsNeeded() {
		return asNeeded;
	}
	
	/**
	 * @param asNeeded the asNeeded to set
	 */
	public void setAsNeeded(Boolean asNeeded) {
		this.asNeeded = asNeeded;
	}
	
	/**
	 * Gets the quantity
	 * 
	 * @return quantity
	 */
	public Integer getQuantity() {
		return this.quantity;
	}
	
	/**
	 * Sets the quantity
	 * 
	 * @param quantity
	 */
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
	
	/**
	 * @return the quantityUnits
	 */
	public String getQuantityUnits() {
		return quantityUnits;
	}
	
	/**
	 * @param quantityUnits the quantityUnits to set
	 */
	public void setQuantityUnits(String quantityUnits) {
		this.quantityUnits = quantityUnits;
	}
	
	/**
	 * @return the frequency
	 */
	public String getFrequency() {
		return frequency;
	}
	
	/**
	 * @param frequency the frequency to set
	 */
	public void setFrequency(String frequency) {
		this.frequency = frequency;
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
	 * Sets the dose.
	 * 
	 * @param dose the dose to set.
	 */
	public void setDose(Double dose) {
		this.dose = dose;
	}
	
	/**
	 * Gets the dose.
	 * 
	 * @return the dose.
	 */
	public Double getDose() {
		return dose;
	}
	
	public String toString() {
		return "DrugOrder(" + getDose() + getDoseUnits() + " of " + (getDrug() != null ? getDrug().getName() : "[no drug]")
		        + " from " + getStartDate() + " to " + (getDiscontinued() ? getDiscontinuedDate() : getAutoExpireDate())
		        + " orderNumber " + getOrderNumber() + " brandName " + getBrandName() + ")";
	}
	
	/**
	 * Gets the brand name.
	 * 
	 * @return the brand name.
	 */
	public String getBrandName() {
		return brandName;
	}
	
	/**
	 * Sets the brand name.
	 * 
	 * @param brandName the brand name to set.
	 */
	public void setBrandName(String brandName) {
		this.brandName = brandName;
	}
	
	/**
	 * Gets the strength.
	 * 
	 * @return the strength.
	 */
	public Double getStrength() {
		return strength;
	}
	
	/**
	 * Sets the strength.
	 * 
	 * @param strength the strength to set.
	 */
	public void setStrength(Double strength) {
		this.strength = strength;
	}
	
	/**
	 * Gets the strength units.
	 * 
	 * @return the strength units.
	 */
	public String getStrengthUnits() {
		return strengthUnits;
	}
	
	/**
	 * Sets the strength units.
	 * 
	 * @param strengthUnits the strength units to set.
	 */
	public void setStrengthUnits(String strengthUnits) {
		this.strengthUnits = strengthUnits;
	}
	
	/**
	 * Gets the dosage form.
	 * 
	 * @return the dosage form.
	 */
	public String getDosageForm() {
		return dosageForm;
	}
	
	/**
	 * Sets the dosage form.
	 * 
	 * @param dosageForm the dosage form to set.
	 */
	public void setDosageForm(String dosageForm) {
		this.dosageForm = dosageForm;
	}
	
	/**
	 * Gets the route.
	 * 
	 * @return the route.
	 */
	public String getRoute() {
		return route;
	}
	
	/**
	 * Sets the route.
	 * 
	 * @param route the route to set.
	 */
	public void setRoute(String route) {
		this.route = route;
	}
	
	/**
	 * Gets the as needed condition.
	 * 
	 * @return the as needed condition.
	 */
	public String getAsNeededCondition() {
		return asNeededCondition;
	}
	
	/**
	 * Sets the as needed condition.
	 * 
	 * @param asNeededCondition the as needed condition to set.
	 */
	public void setAsNeededCondition(String asNeededCondition) {
		this.asNeededCondition = asNeededCondition;
	}
	
	/**
	 * Gets the additional instructions.
	 * 
	 * @return the additional instructions.
	 */
	public String getAdditionalInstructions() {
		return additionalInstructions;
	}
	
	/**
	 * Sets the additional instructions.
	 * 
	 * @param additionalInstructions the additional instructions to set.
	 */
	public void setAdditionalInstructions(String additionalInstructions) {
		this.additionalInstructions = additionalInstructions;
	}
	
	/**
	 * Gets the number of refills.
	 * 
	 * @return the number of refills.
	 */
	public Integer getNumRefills() {
		return numRefills;
	}
	
	/**
	 * Sets the number of refills.
	 * 
	 * @param numRefills the number of refills to set.
	 */
	public void setNumRefills(Integer numRefills) {
		this.numRefills = numRefills;
	}
	
	/**
	 * @see org.openmrs.Order#getId()
	 */
	public Integer getId() {
		return getOrderId();
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	public void setId(Integer id) {
		setOrderId(id);
	}
	
	/**
	 * Sets isStructuredDosing dosing of drug order
	 * 
	 * @param isStructuredDosing the value to set
	 */
	public void setIsStructuredDosing(Boolean isStructuredDosing) {
		this.isStructuredDosing = isStructuredDosing;
	}
	
	/**
	 * @return unstructured dosing
	 */
	public Boolean getIsStructuredDosing() {
		return isStructuredDosing;
	}
	
	/**
	 * @return the duration
	 */
	public Integer getDuration() {
		return duration;
	}
	
	/**
	 * @param duration the duration to set
	 */
	public void setDuration(Integer duration) {
		this.duration = duration;
	}
	
	/**
	 * @return the durationUnits
	 */
	public String getDurationUnits() {
		return durationUnits;
	}
	
	/**
	 * @param durationUnits the durationUnits to set
	 */
	public void setDurationUnits(String durationUnits) {
		this.durationUnits = durationUnits;
	}
}
