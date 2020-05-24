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

import org.openmrs.*;

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
	
	private String accessionNumber;
	
	private String orderNumber;

	private Date activatedOnOrBeforeDate;

	private Date activatedOnOrAfterDate;

	private boolean isStopped;

	private Date autoExpireOnOrBeforeDate;

	private Date canceledOrExpiredOnOrBeforeDate;

	private Order.Action action;

	private Order.FulfillerStatus fulfillerStatus;

	private Boolean includeNullFulfillerStatus;

	private boolean excludeCanceledAndExpired;

	private boolean excludeDiscontinueOrders;

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
	 * @param accessionNumber the accessionNumber to match on (exact match, case-insensitive)
	 * @return this builder instance
	 */
	public OrderSearchCriteriaBuilder setAccessionNumber(String accessionNumber) {
		this.accessionNumber = accessionNumber;
		return (this);
	}

	/**
	 * @param orderNumber the orderNumber to match on (exact match, case-insensitive)
	 * @return this builder instance
	 */
	public OrderSearchCriteriaBuilder setOrderNumber(String orderNumber) {
		this.orderNumber = orderNumber;
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
	 *
	 * @param isStopped
	 * @return this builde instance
	 */
	public OrderSearchCriteriaBuilder setIsStopped(boolean isStopped) {
		this.isStopped = isStopped;
		return (this);
	}

	/**
	 * 
	 * @param autoExpireOnOrBeforeDate
	 * @return this builder instance
	 */
	public OrderSearchCriteriaBuilder setAutoExpireOnOrBeforeDate(Date autoExpireOnOrBeforeDate) {
		this.autoExpireOnOrBeforeDate = autoExpireOnOrBeforeDate;
		return (this);
	}

	/**
	 *
	 * @param canceledOrExpiredOnOrBeforeDate
	 * @return this builder instance
	 */
	public OrderSearchCriteriaBuilder setCanceledOrExpiredOnOrBeforeDate(Date canceledOrExpiredOnOrBeforeDate) {
		this.canceledOrExpiredOnOrBeforeDate = canceledOrExpiredOnOrBeforeDate;
		return (this);
	}

	/**
     *
     * @param action
     * @return this builder instance
     */
    public OrderSearchCriteriaBuilder setAction(Order.Action action) {
        this.action = action;
        return (this);
    }

    /**
	 *
	 * @param fulfillerStatus
	 * @return this builder instance
	 */
	public OrderSearchCriteriaBuilder setFulfillerStatus(Order.FulfillerStatus fulfillerStatus) {
		this.fulfillerStatus = fulfillerStatus;
		return (this);
	}

	/**
	 *
	 * @param includeNullFulfillerStatus
	 * @return
	 */
	public OrderSearchCriteriaBuilder setIncludeNullFulfillerStatus(Boolean includeNullFulfillerStatus) {
		this.includeNullFulfillerStatus = includeNullFulfillerStatus;
		return (this);
	}

	public OrderSearchCriteriaBuilder setExcludeCanceledAndExpired(boolean excludeCanceledAndExpired) {
		this.excludeCanceledAndExpired = excludeCanceledAndExpired;
		return (this);
	}

	public OrderSearchCriteriaBuilder setExcludeDiscontinueOrders(boolean excludeDiscontinueOrders) {
		this.excludeDiscontinueOrders = excludeDiscontinueOrders;
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
		return new OrderSearchCriteria(patient, careSetting, concepts, orderTypes, accessionNumber, orderNumber, activatedOnOrBeforeDate,  
			activatedOnOrAfterDate, isStopped, autoExpireOnOrBeforeDate, canceledOrExpiredOnOrBeforeDate,
				action, fulfillerStatus, includeNullFulfillerStatus, excludeCanceledAndExpired, excludeDiscontinueOrders, includeVoided);
	}
}

