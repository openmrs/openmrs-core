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
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.Patient;

import java.util.Collection;
import java.util.Date;

/**
 * The search parameter object for orders. A convenience interface for building
 * instances is provided by {@link OrderSearchCriteriaBuilder}.
 *
 * @since 2.2
 * @see OrderSearchCriteriaBuilder
 */
public class OrderSearchCriteria {

	private final Patient patient;

	private final CareSetting careSetting;

	private final Collection<Concept> concepts;

	private final Collection<OrderType> orderTypes;

	/**
	 * Accession Number to match on; performs an exact match, case-insensitive
	 */
	private String accessionNumber;

	/**
	 * Accession Number to match on; performs an exact match, case-insensitive
	 */
	private String orderNumber;

	/**
	 * Matches on dateActivated that is any time on this date or less
	 */
	private final Date activatedOnOrBeforeDate;

	/**
	 * Matches on dateActivated that is any time on this date or more
	 */
	private final Date activatedOnOrAfterDate;

	/**
	 * Matches on autoExpireDate that is any time on this date or less
	 */
	private final Date autoExpireOnOrBeforeDate;

	/**
	 * Matches on dateStopped that is any time on this date or less
	 */
	private final boolean isStopped;

	/**
	 * All canceled or auto expired orders before date
	 */
	private final Date canceledOrExpiredOnOrBeforeDate;

	/**
	 * Matches on fulfillerStatus
	 */
	private final Order.FulfillerStatus fulfillerStatus;

	/**
	 * Matches on orders with fulfiller_status = null
	 * This parameter could work in conjunction with fulfillerStatus.
	 *  If fulfillerStatus is specified then includeNullFulfillerStatus=true would include
	 *  all orders where fulfillerStatus=null OR fulfillerStatus = specified value
	 */
	private final Boolean includeNullFulfillerStatus;

    /**
     * Matches on action
     */
	private final Order.Action action;

	private final boolean includeVoided;

	private final boolean excludeCanceledAndExpired;

	private final boolean excludeDiscontinueOrders;

	/**
	 * Instead of calling this constructor directly, it is recommended to use {@link OrderSearchCriteriaBuilder}.
	 * @param patient the patient the order is for
	 * @param careSetting the care setting to match on
	 * @param concepts the concepts to match on; if not specified, matches on all concepts
	 * @param orderTypes the order types to match on; if not specified, matches all order types
	 * @param accessionNumber to match on; performs exact match if specified
	 * @param orderNumber to match on; performs exact match if specifed
	 * @param activatedOnOrBeforeDate orders must have dateActivated on or before this date
	 * @param activatedOnOrAfterDate orders must have dateActivated on or after this date
	 * @param includeVoided whether to include the voided orders or not
	 */
	public OrderSearchCriteria(Patient patient, CareSetting careSetting, Collection<Concept> concepts,
							   Collection<OrderType> orderTypes, String accessionNumber, String orderNumber,
							   Date activatedOnOrBeforeDate, Date activatedOnOrAfterDate, boolean isStopped,
							   Date autoExpireOnOrBeforeDate,
							   Date canceledOrExpiredOnOrBeforeDate,
							   Order.Action action,
							   Order.FulfillerStatus fulfillerStatus,
							   Boolean includeNullFulfillerStatus,
							   boolean excludeCanceledAndExpired,
							   boolean excludeDiscontinueOrders,
							   boolean includeVoided) {
		this.patient = patient;
		this.careSetting = careSetting;
		this.concepts = concepts;
		this.orderTypes = orderTypes;
		this.accessionNumber = accessionNumber;
		this.orderNumber = orderNumber;
		this.activatedOnOrBeforeDate = activatedOnOrBeforeDate;
		this.activatedOnOrAfterDate = activatedOnOrAfterDate;
		this.isStopped = isStopped;
		this.autoExpireOnOrBeforeDate = autoExpireOnOrBeforeDate;
		this.canceledOrExpiredOnOrBeforeDate = canceledOrExpiredOnOrBeforeDate;
		this.action = action;
		this.fulfillerStatus = fulfillerStatus;
		this.includeNullFulfillerStatus = includeNullFulfillerStatus;
		this.excludeCanceledAndExpired = excludeCanceledAndExpired;
		this.excludeDiscontinueOrders = excludeDiscontinueOrders;
		this.includeVoided = includeVoided;
	}

	/**
	 * (Legacy constructor, before addition of Order Number and Accession Number fields)
	 * Instead of calling this constructor directly, it is recommended to use {@link OrderSearchCriteriaBuilder}.
	 * @param patient the patient the order is for
	 * @param careSetting the care setting to match on
	 * @param concepts the concepts to match on; if not specified, matches on all concepts
	 * @param orderTypes the order types to match on; if not specified, matches all order types
	 * @param activatedOnOrBeforeDate orders must have dateActivated on or before this date
	 * @param activatedOnOrAfterDate orders must have dateActivated on or after this date
	 * @param includeVoided whether to include the voided orders or not
	 */
	@Deprecated
	public OrderSearchCriteria(Patient patient, CareSetting careSetting, Collection<Concept> concepts,
							   Collection<OrderType> orderTypes, Date activatedOnOrBeforeDate,
							   Date activatedOnOrAfterDate, boolean isStopped, Date autoExpireOnOrBeforeDate,
							   Date canceledOrExpiredOnOrBeforeDate,
							   Order.Action action,
							   Order.FulfillerStatus fulfillerStatus,
							   Boolean includeNullFulfillerStatus,
							   boolean excludeCanceledAndExpired,
							   boolean excludeDiscontinueOrders,
							   boolean includeVoided) {

		this(patient, careSetting, concepts, orderTypes, null, null,
			activatedOnOrBeforeDate, activatedOnOrAfterDate, isStopped, autoExpireOnOrBeforeDate, canceledOrExpiredOnOrBeforeDate,
			action, fulfillerStatus, includeNullFulfillerStatus, excludeCanceledAndExpired, excludeDiscontinueOrders, includeVoided);

	}

	/**
	 * @return the patient the order is for
	 */
	public Patient getPatient() { return patient; }

	/**
	 * @return the care setting to match on
	 */
	public CareSetting getCareSetting() { return careSetting; }

	/**
	 * @return the concepts defining the order must be in this collection
	 */
	public Collection<Concept> getConcepts() { return concepts; }

	/**
	 * @return the order types to match on must be in this collection
	 */
	public Collection<OrderType> getOrderTypes() { return orderTypes; }

	/**
	 * @return the accession number to match on; must be case-insensitive exact-match
	 * @since 2.3.1
	 */
	public String getAccessionNumber() {
		return accessionNumber;
	}

	/**
	 * @return the order number to match on; must be case-insensitive exact-match
	 * @since 2.3.1
	 */
	public String getOrderNumber() {
		return orderNumber;
	}

	/**
	 * @return orders must have dateActivated on or before this date
	 */
	public Date getActivatedOnOrBeforeDate() { return activatedOnOrBeforeDate; }

	/**
	 * @return orders must have dateActivated on or after this date
	 */
	public Date getActivatedOnOrAfterDate() { return activatedOnOrAfterDate; }

	/**
	 *
	 * @return orders must have dateStopped on or before this date
	 */
	public boolean isStopped() {
		return isStopped;
	}

	/**
	 *
	 * @return orders must have autoExpireDate on or before this date
	 */
	public Date getAutoExpireOnOrBeforeDate() {
		return autoExpireOnOrBeforeDate;
	}

	/**
	 *
	 * @return orders that are canceled or have autoExpireDate on or before this date
	 */
	public Date getCanceledOrExpiredOnOrBeforeDate() {
		return canceledOrExpiredOnOrBeforeDate;
	}

	/**
     *
     * @return orders must match the action
     */
	public Order.Action getAction() {
        return action;
    }

    /**
	 *
	 * @return orders must match the fulfillerstatus
	 */
	public Order.FulfillerStatus getFulfillerStatus() {
		return fulfillerStatus;
	}

	/**
	 *
	 * @return include(OR) orders with fulfiller_status = null
	 */
	public Boolean getIncludeNullFulfillerStatus() {
		return includeNullFulfillerStatus;
	}

	public boolean getExcludeCanceledAndExpired() {
		return excludeCanceledAndExpired;
	}


	public boolean getExcludeDiscontinueOrders() {
		return excludeDiscontinueOrders;
	}

	/**
	 * @return whether to include the voided orders or not
	 */
	public boolean getIncludeVoided() { return includeVoided; }

}
