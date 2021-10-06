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

/**
 * Provides properties for several order types like TestOrder , ReferralOrder
 * and others to accommodate referrals depending on the openmrs implementation
 * use case
 * 
 * @since 2.5.0
 */
public abstract class ServiceOrder extends Order {

	public enum Laterality {
		LEFT,
		RIGHT,
		BILATERAL
	}

	public static final long serialVersionUID = 1L;

	public Concept specimenSource;

	private Laterality laterality;

	private String clinicalHistory;

	private OrderFrequency frequency;

	private Integer numberOfRepeats;
	
	private Location location;

	/**
	 * Default Constructor
	 */
	public ServiceOrder() {}

	/**
	 * @see org.openmrs.Order#copy()
	 * <strong>Should</strong> copy all sub-class order fields
	 */
	@Override
	public Order copy() {
		return copyHelper(new Order());
	}

	protected Order copyHelper(ServiceOrder target) {
		super.copyHelper(target);
		target.specimenSource = getSpecimenSource();
		target.laterality = getLaterality();
		target.clinicalHistory = getClinicalHistory();
		target.frequency = getFrequency();
		target.numberOfRepeats = getNumberOfRepeats();
		target.location = getLocation();
		return target;
	}

	/**
	 * @return the specimenSource
	 */
	public Concept getSpecimenSource() {
		return specimenSource;
	}

	/**
	 * @param specimenSource the specimenSource to set
	 */
	public void setSpecimenSource(Concept specimenSource) {
		this.specimenSource = specimenSource;
	}

	/**
	 * Gets the laterality.
	 *
	 * @return the laterality.
	 */
	public Laterality getLaterality() {
		return laterality;
	}

	/**
	 * Sets the laterality.
	 *
	 * @param laterality the laterality to set.
	 */
	public void setLaterality(ServiceOrder.Laterality laterality) {
		this.laterality = laterality;
	}

	/**
	 * Gets the clinical history.
	 *
	 * @return the clinical history.
	 */
	public String getClinicalHistory() {
		return clinicalHistory;
	}

	/**
	 * Sets the clinical history.
	 *
	 * @param clinicalHistory the clinical history to set.
	 */
	public void setClinicalHistory(String clinicalHistory) {
		this.clinicalHistory = clinicalHistory;
	}

	/**
	 * Gets frequency of test order
	 *
	 * @since 1.10
	 */
	public OrderFrequency getFrequency() {
		return frequency;
	}

	/**
	 * Sets frequency of test order
	 *
	 * @param frequency
	 * @since 1.10
	 */
	public void setFrequency(OrderFrequency frequency) {
		this.frequency = frequency;
	}

	/**
	 * Gets numberOfRepeats of test order
	 *
	 * @since 1.10
	 */
	public Integer getNumberOfRepeats() {
		return numberOfRepeats;
	}

	/**
	 * Sets numberOfRepeats of test order
	 *
	 * @param numberOfRepeats to set
	 * @since 1.10
	 */
	public void setNumberOfRepeats(Integer numberOfRepeats) {this.numberOfRepeats = numberOfRepeats;}

	/**
	 * Gets a location of the particular order
	 *
	 * @since 2.5.0
	 */
	public Location getLocation() {return location;}

	/**
	 * Sets a location of the particular order
	 *
	 * @param location to set
	 * @since 2.5.0
	 */
	public void setLocation(Location location) {this.location = location;}

	/**
	 * Creates a discontinuation order for this.
	 *
	 * @see org.openmrs.Order#cloneForDiscontinuing()
	 * @return the newly created order
	 * @since 1.10
	 * <strong>Should</strong> set all the relevant fields
	 */
	@Override
	public TestOrder cloneForDiscontinuing() {
		TestOrder newOrder = new TestOrder();
		newOrder.setCareSetting(getCareSetting());
		newOrder.setConcept(getConcept());
		newOrder.setAction(Order.Action.DISCONTINUE);
		newOrder.setPreviousOrder(this);
		newOrder.setPatient(getPatient());
		newOrder.setOrderType(getOrderType());
		return newOrder;
	}

	/**
	 * Creates a TestOrder for revision from this order, sets the previousOrder, action field and
	 * other test order fields.
	 *
	 * @return the newly created order
	 * @since 1.10
	 * <strong>Should</strong> set all the relevant fields
	 * <strong>Should</strong> set the relevant fields for a DC order
	 */
	@Override
	public TestOrder cloneForRevision() {
		return cloneForRevisionHelper(new TestOrder());
	}

	/**
	 * @see Order#cloneForRevisionHelper(Order)
	 */
	protected TestOrder cloneForRevisionHelper(TestOrder target) {
		super.cloneForRevisionHelper(target);
		target.setSpecimenSource(getSpecimenSource());
		target.setLaterality(getLaterality());
		target.setClinicalHistory(getClinicalHistory());
		target.setFrequency(getFrequency());
		target.setNumberOfRepeats(getNumberOfRepeats());
		return target;
	}
}
