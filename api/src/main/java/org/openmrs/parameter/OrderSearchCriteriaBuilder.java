/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.parameter;

import org.openmrs.CareSetting;
import org.openmrs.Concept;
import org.openmrs.OrderType;
import org.openmrs.Patient;

import java.util.Collection;
import java.util.Date;

/**
 * A convenience builder for {@link OrderSearchCriteria}. Create a builder, set
 * its properties to desired values and finally call {@link #build()}
 * to create the actual search criteria instance.
 * @see OrderSearchCriteria
 */
public class OrderSearchCriteriaBuilder {
	private Patient patient;

	private CareSetting careSetting;

	private Collection<Concept> concepts;

	private Collection<OrderType> orderTypes;

	private Date activatedOnOrBeforeDate;

	private Date activatedOnOrAfterDate;

	private boolean includeVoided;

	/**
	 * @param patient the patient the order is for
	 * @return this builder instance
	 */
	public OrderSearchCriteriaBuilder setPatient(Patient patient) {
		this.patient = patient;
		return (this);
	}

	/**
	 * @param careSetting the care setting to match on
	 * @return this builder instance
	 */
	public OrderSearchCriteriaBuilder setCareSetting(CareSetting careSetting) {
		this.careSetting = careSetting;
		return (this);
	}

	/**
	 * @param concepts the concepts defining the order must be in this collection
	 * @return this builder instance
	 */
	public OrderSearchCriteriaBuilder setConcepts(Collection<Concept> concepts) {
		this.concepts = concepts;
		return (this);
	}

	/**
	 * @param orderTypes the order types to match on must be in this collection
	 * @return this builder instance
	 */
	public OrderSearchCriteriaBuilder setOrderTypes(Collection<OrderType> orderTypes) {
		this.orderTypes = orderTypes;
		return (this);
	}

	/**
	 * @param activatedOnOrBeforeDate orders must have dateActivated on or before this date
	 * @return this builder instance
	 */
	public OrderSearchCriteriaBuilder setActivatedOnOrBeforeDate(Date activatedOnOrBeforeDate) {
		this.activatedOnOrBeforeDate = activatedOnOrBeforeDate;
		return (this);
	}

	/**
	 * @param activatedOnOrAfterDate orders must have dateActivated on or after this date
	 * @return this builder instance
	 */
	public OrderSearchCriteriaBuilder setActivatedOnOrAfterDate(Date activatedOnOrAfterDate) {
		this.activatedOnOrAfterDate = activatedOnOrAfterDate;
		return (this);
	}

	/**
	 * @param includeVoided whether to include the voided orders or not
	 * @return this builder instance
	 */
	public OrderSearchCriteriaBuilder setIncludeVoided(boolean includeVoided) {
		this.includeVoided = includeVoided;
		return (this);
	}

	/**
	 * Create an {@link OrderSearchCriteria} with the properties of this builder instance.
	 * @return a new search criteria instance
	 */
	public OrderSearchCriteria build() {
		return new OrderSearchCriteria(patient, careSetting, concepts, orderTypes, activatedOnOrBeforeDate,  
			activatedOnOrAfterDate, includeVoided);
	}
}

