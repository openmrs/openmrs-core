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
	
	// Fields
	
	private Double dose;
	
	private Double equivalentDailyDose;
	
	private String units;
	
	private String frequency;
	
	private Boolean prn = false;

	private Boolean structuredDosing = true;

	private Integer quantity;

	private Drug drug;
	
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
		target.equivalentDailyDose = getEquivalentDailyDose();
		target.units = getUnits();
		target.frequency = getFrequency();
		target.prn = getPrn();
		target.structuredDosing = getStructuredDosing();
		target.quantity = getQuantity();
		target.drug = getDrug();
		return target;
	}
	
	public boolean isDrugOrder() {
		return true;
	}
	
	// Property accessors
	
	/**
	 * Gets the units of this drug order
	 *
	 * @return units
	 */
	public String getUnits() {
		return this.units;
	}
	
	/**
	 * Sets the units of this drug order
	 *
	 * @param units
	 */
	public void setUnits(String units) {
		this.units = units;
	}
	
	/**
	 * Gets the frequency
	 *
	 * @return frequency
	 */
	public String getFrequency() {
		return this.frequency;
	}
	
	/**
	 * Sets the frequency
	 *
	 * @param frequency
	 */
	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}
	
	/**
	 * Returns true/false whether the drug is a "pro re nata" (as needed) drug
	 *
	 * @return Boolean
	 */
	public Boolean getPrn() {
		return this.prn;
	}
	
	/**
	 * Sets the prn
	 *
	 * @param prn
	 */
	public void setPrn(Boolean prn) {
		this.prn = prn;
	}
	
	/**
	 * Gets whether this drug is complex
	 * @deprecated use {@link #getStructuredDosing()}
	 * @return Boolean
	 */
	@Deprecated
	public Boolean getComplex() {
        return !getStructuredDosing();
	}

	/**
	 * Sets whether this drug is complex
	 * @deprecated use {@link #setStructuredDosing(Boolean}
	 * @param complex
	 */
	@Deprecated
	public void setComplex(Boolean complex) {
		setStructuredDosing(!complex);
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
	 * Gets the structuredDosing
	 * @since 1.10
	 * @return drug
	 */
	public Boolean getStructuredDosing() {
		return structuredDosing;
	}

	/**
	 * Sets the structuredDosing
	 *
	 * @param structuredDosing
	 * @since 1.10
	 */
	public void setStructuredDosing(Boolean structuredDosing) {
		this.structuredDosing = structuredDosing;
	}
	
	public Double getEquivalentDailyDose() {
		return equivalentDailyDose;
	}
	
	public void setEquivalentDailyDose(Double equivalentDailyDose) {
		this.equivalentDailyDose = equivalentDailyDose;
	}
	
	public void setDose(Double dose) {
		this.dose = dose;
	}
	
	public Double getDose() {
		return dose;
	}
	
	public String toString() {
		return "DrugOrder(" + getDose() + getUnits() + " of " + (getDrug() != null ? getDrug().getName() : "[no drug]")
		        + " from " + getStartDate() + " to " + (getDiscontinued() ? getDiscontinuedDate() : getAutoExpireDate())
		        + ")";
	}
	
}
