/*
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
 * This is a type of order that adds tests specific attributes like: laterality,
 * clinical history, etc.
 *
 * @since 1.9.2, 1.10
 */
public class TestOrder extends Order {
	
	public enum Laterality {
		LEFT, RIGHT, BILATERAL
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
	
	protected Order copyHelper(TestOrder target) {
		super.copyHelper(target);
		this.specimenSource = getSpecimenSource();
		this.laterality = getLaterality();
		this.clinicalHistory = getClinicalHistory();
		this.frequency = getFrequency();
		this.numberOfRepeats = getNumberOfRepeats();
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
	 * Creates a TestOrder for revision from this order, sets the previousOrder, action field and other test order fields.
	 *
	 * @return the newly created order
	 * @since 1.10
	 * @should set all the relevant fields
	 */
	@Override
	public Order cloneForRevision() {
		TestOrder newOrder = new TestOrder();
		newOrder.setCareSetting(this.getCareSetting());
		newOrder.setConcept(this.getConcept());
		newOrder.setAction(Action.REVISE);
		newOrder.setPreviousOrder(this);
		newOrder.setPatient(this.getPatient());
		
		newOrder.setSpecimenSource(getSpecimenSource());
		newOrder.setLaterality(getLaterality());
		newOrder.setClinicalHistory(getClinicalHistory());
		newOrder.setFrequency(getFrequency());
		newOrder.setNumberOfRepeats(getNumberOfRepeats());
		return newOrder;
		
	}
}
