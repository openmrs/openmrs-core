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
 * and others depending on the openmrs implementation use case as need arises
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
	
	private Concept location;

	/**
	 * Default Constructor
	 */
	public ServiceOrder() {
	}

	protected ServiceOrder copyHelper(ServiceOrder target) {
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
	 * Gets frequency of a particular order
	 */
	public OrderFrequency getFrequency() {
		return frequency;
	}

	/**
	 * Sets frequency of a particular order
	 *
	 * @param frequency
	 */
	public void setFrequency(OrderFrequency frequency) {
		this.frequency = frequency;
	}

	/**
	 * Gets numberOfRepeats of a particular order
	 */
	public Integer getNumberOfRepeats() {
		return numberOfRepeats;
	}

	/**
	 * Sets numberOfRepeats of a particular order
	 *
	 * @param numberOfRepeats to set
	 */
	public void setNumberOfRepeats(Integer numberOfRepeats) {
		this.numberOfRepeats = numberOfRepeats;
	}

	/**
	 * Gets a location of the particular order
	 */
	public Concept getLocation() {
		return location;
	}

	/**
	 * Sets a location of the particular order
	 *
	 * @param location to set
	 */
	public void setLocation(Concept location) {
		this.location = location;
	}

	/**
	 * @see Order#cloneForRevisionHelper(Order)
	 */
	protected ServiceOrder cloneForRevisionHelper(ServiceOrder target) {
		super.cloneForRevisionHelper(target);
		target.setSpecimenSource(getSpecimenSource());
		target.setLaterality(getLaterality());
		target.setClinicalHistory(getClinicalHistory());
		target.setFrequency(getFrequency());
		target.setNumberOfRepeats(getNumberOfRepeats());
		target.setLocation(getLocation());
		return target;
	}

	/**
	 * The purpose of this method is to allow subclasses of a ServiceOrder to delegate a portion of their
	 * cloneForDiscontinuing() method back to the superclass, in case the base class implementation
	 * changes.
	 *
	 * @param target a particular order that will have the state of <code>this</code> copied into it
	 * @return Returns the Order that was passed in, with state copied into it
	 */
	protected ServiceOrder cloneForDiscontinuingHelper(ServiceOrder target) {
		target.setCareSetting(getCareSetting());
		target.setConcept(getConcept());
		target.setAction(Action.DISCONTINUE);
		target.setPreviousOrder(this);
		target.setPatient(getPatient());
		target.setOrderType(getOrderType());
		return target;
	}
}
