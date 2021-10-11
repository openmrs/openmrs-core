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
 * @since 1.9.2, 1.10, 2.5.0
 */
public class TestOrder extends ServiceOrder {
	
	/**
	 * Default Constructor
	 */
	public TestOrder() {
	}

	/**
	 * @see ServiceOrder#copy()
	 */
	@Override
	public Order copy() {
		return copyHelper(new TestOrder());
	}
	
	/**
	 * @return the specimenSource
	 */
	@Override
	public Concept getSpecimenSource() {
		return specimenSource;
	}

	/**
	 * @param specimenSource the specimenSource to set
	 */
	@Override
	public void setSpecimenSource(Concept specimenSource) {
		specimenSource = specimenSource;
	}
	
	/**
	 * @see ServiceOrder#getLaterality() 
	 */
	@Override
	public Laterality getLaterality() {
		return super.getLaterality();
	}
	
	/**
	 * @see org.openmrs.ServiceOrder#setLaterality(Laterality)
	 */
	@Override
	public void setLaterality(Laterality laterality) {
		super.setLaterality(laterality);
	}

	/**
	 * @see ServiceOrder#getClinicalHistory() 
	 */
	@Override
	public String getClinicalHistory() {
		return super.getClinicalHistory();
	}

	/**
	 * @see org.openmrs.ServiceOrder#setClinicalHistory(String) 
	 */
	@Override
	public void setClinicalHistory(String clinicalHistory) {
		super.setClinicalHistory(clinicalHistory);
	}
	
	/**
	 * Gets frequency of test order
	 */
	@Override
	public OrderFrequency getFrequency() {
		return super.getFrequency();
	}
	
	/**
	 * Sets frequency of test order
	 * 
	 * @param frequency
	 */
	@Override
	public void setFrequency(OrderFrequency frequency) {
		super.setFrequency(frequency);
	}
	
	/**
	 * Gets numberOfRepeats of test order
	 */
	@Override
	public Integer getNumberOfRepeats() {
		return super.getNumberOfRepeats();
	}
	
	/**
	 * Sets numberOfRepeats of test order
	 * 
	 * @param numberOfRepeats to set
	 */
	@Override
	public void setNumberOfRepeats(Integer numberOfRepeats) {
		super.setNumberOfRepeats(numberOfRepeats);
	}
	
	/**
	 * @see org.openmrs.ServiceOrder#cloneForDiscontinuing
	 */
	@Override
	public Order cloneForDiscontinuing() {
		return super.cloneForDiscontinuing();
	}

	/**
	 * @see ServiceOrder#cloneForRevision() 
	 */
	@Override
	public Order cloneForRevision() {
		return super.cloneForRevision();
	}

	/**
	 * @see org.openmrs.ServiceOrder#cloneForRevisionHelper(Order) 
	 */
	@Override
	protected Order cloneForRevisionHelper(Order target) {
		return super.cloneForRevisionHelper(target);
	}
	
	/**
	 * @see ServiceOrder#cloneForDiscontinuingHelper(ServiceOrder)  
	 */
	@Override
	protected ServiceOrder cloneForDiscontinuingHelper(ServiceOrder target) {
		return super.cloneForDiscontinuingHelper(target);
	}
}
