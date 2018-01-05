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

import static org.openmrs.Order.Action.DISCONTINUE;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.util.OpenmrsUtil;

/**
 * DrugOrder
 *
 * @version 1.0
 */
public class DrugOrder extends Order {

	public static final long serialVersionUID = 72232L;

	// Fields

	private Double dose;

	private Concept doseUnits;

	private OrderFrequency frequency;

	private Boolean asNeeded = false;

	private Double quantity;

	private Concept quantityUnits;

	private Drug drug;

	private String asNeededCondition;

	private Class<? extends DosingInstructions> dosingType = SimpleDosingInstructions.class;

	private Integer numRefills;

	private String dosingInstructions;

	private Integer duration;

	private Concept durationUnits;

	private Concept route;

	private String brandName;

	private Boolean dispenseAsWritten = Boolean.FALSE;

	private String drugNonCoded;

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
	@Override
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
		target.setDrugNonCoded(getDrugNonCoded());
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
	 * Returns true/false whether the drug is a "pro re nata" drug
	 *
	 * @return Boolean
	 * @since 1.10
	 */
	public Boolean getAsNeeded() {
		return asNeeded;
	}

	/**
	 * @param asNeeded the value to set
	 * @since 1.10
	 */
	public void setAsNeeded(Boolean asNeeded) {
		this.asNeeded = asNeeded;
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
	public Class<? extends DosingInstructions> getDosingType() {
		return dosingType;
	}

	/**
	 * Sets the dosingType
	 *
	 * @param dosingType the dosingType to set
	 * @since 1.10
	 */
	public void setDosingType(Class<? extends DosingInstructions> dosingType) {
		this.dosingType = dosingType;
	}

	/**
	 * Gets the dosingInstructions instance
	 *
	 * @since 1.10
	 */
	public DosingInstructions getDosingInstructionsInstance() {
		try {
			DosingInstructions instructions = getDosingType().newInstance();
			return instructions.getDosingInstructions(this);
		}
		catch (InstantiationException | IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
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
	public Integer getDuration() {
		return duration;
	}

	/**
	 * Sets the duration of a Drug Order
	 *
	 * @param duration to set
	 * @since 1.10
	 */
	public void setDuration(Integer duration) {
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
		newOrder.setAction(DISCONTINUE);
		newOrder.setPreviousOrder(this);
		newOrder.setPatient(getPatient());
		newOrder.setDrug(getDrug());
		newOrder.setOrderType(getOrderType());
		newOrder.setDrugNonCoded(getDrugNonCoded());
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
		target.setDrugNonCoded(getDrugNonCoded());

		return target;
	}

	/**
	 * Sets autoExpireDate based on duration.
	 *
	 * @should delegate calculation to dosingInstructions
	 * @should not calculate for discontinue action
	 * @should not calculate if autoExpireDate already set
	 */
	public void setAutoExpireDateBasedOnDuration() {
		if (DISCONTINUE != getAction() && getAutoExpireDate() == null) {
			setAutoExpireDate(getDosingInstructionsInstance().getAutoExpireDate(this));
		}
	}

	@Override
	public String toString() {
		String prefix = DISCONTINUE == getAction() ? "DC " : "";
		return prefix + "DrugOrder(" + getDose() + getDoseUnits() + " of "
		        + (isNonCodedDrug() ? getDrugNonCoded() : (getDrug() != null ? getDrug().getName() : "[no drug]")) + " from " + getDateActivated() + " to "
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

	/**
	 * Checks whether orderable of this drug order is same as other order
	 *
	 * @since 1.10
	 * @param otherOrder the other order to match on
	 * @return true if the drugs match
	 * @should return false if the other order is null
	 * @should return false if the other order is not a drug order
	 * @should return false if both drugs are null and the concepts are different
	 * @should return false if the concepts match and only this has a drug
	 * @should return false if the concepts match and only the other has a drug
	 * @should return false if the concepts match and drugs are different and not null
	 * @should return true if both drugs are null and the concepts match
	 * @should return true if the drugs match
	 */
	@Override
	public boolean hasSameOrderableAs(Order otherOrder) {
        if (!super.hasSameOrderableAs(otherOrder)) {
            return false;
        }
        if (!(otherOrder instanceof DrugOrder)) {
            return false;
        }
        DrugOrder otherDrugOrder = (DrugOrder) otherOrder;

        if (isNonCodedDrug() || otherDrugOrder.isNonCodedDrug()) {
            return OpenmrsUtil.nullSafeEqualsIgnoreCase(this.getDrugNonCoded(), otherDrugOrder.getDrugNonCoded());
        }
        return OpenmrsUtil.nullSafeEquals(this.getDrug(), otherDrugOrder.getDrug());
    }

	/**
	 * @since 1.12
	 * @return drugNonCoded
	 */
	public String getDrugNonCoded() {
		return drugNonCoded;
	}

	/**
	 * @since 1.12
	 * sets drugNonCoded
	 */
	public void setDrugNonCoded(String drugNonCoded) {
		this.drugNonCoded = StringUtils.isNotBlank(drugNonCoded) ? drugNonCoded.trim() : drugNonCoded;
	}

	/**
	 * @since 1.12
	 * return true if a drug is non coded
	 */
	public boolean isNonCodedDrug() {
		return StringUtils.isNotBlank(this.drugNonCoded);
	}
}
