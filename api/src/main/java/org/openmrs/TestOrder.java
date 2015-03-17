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
 * This is a type of order that adds tests specific attributes like: laterality, clinical history,
 * etc.
 * 
 * @since 1.9.2, 1.10
 */
public class TestOrder extends Order {
	
	public enum Laterality {
		LEFT,
		RIGHT,
		BILATERAL
	}
	
	public static final long serialVersionUID = 1L;
	
	private Concept specimenSource;
	
	private Laterality laterality;
	
	private String clinicalHistory;
	
	private OrderFrequency frequency;
	
	private Integer numberOfRepeats;
	
	/**
	 * Default Constructor
	 */
	public TestOrder() {
	}
	
	/**
	 * @see org.openmrs.Order#copy()
	 * @should copy all test order fields
	 */
	public TestOrder copy() {
		return copyHelper(new TestOrder());
	}
	
	protected TestOrder copyHelper(TestOrder target) {
		super.copyHelper(target);
		target.specimenSource = getSpecimenSource();
		target.laterality = getLaterality();
		target.clinicalHistory = getClinicalHistory();
		target.frequency = getFrequency();
		target.numberOfRepeats = getNumberOfRepeats();
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
	public void setLaterality(Laterality laterality) {
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
	public void setNumberOfRepeats(Integer numberOfRepeats) {
		this.numberOfRepeats = numberOfRepeats;
	}
	
	/**
	 * Creates a discontinuation order for this.
	 * 
	 * @see org.openmrs.Order#cloneForDiscontinuing()
	 * @return the newly created order
	 * @since 1.10
	 * @should set all the relevant fields
	 */
	@Override
	public TestOrder cloneForDiscontinuing() {
		TestOrder newOrder = new TestOrder();
		newOrder.setCareSetting(getCareSetting());
		newOrder.setConcept(getConcept());
		newOrder.setAction(Action.DISCONTINUE);
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
	 * @should set all the relevant fields
	 * @should set the relevant fields for a DC order
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
