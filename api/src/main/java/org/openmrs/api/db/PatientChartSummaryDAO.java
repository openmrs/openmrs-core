/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db;

import java.util.Date;
import java.util.List;

import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.Visit;

/**
 * Database access interface for patient chart summary operations. Provides optimized queries
 * to retrieve clinical data for chart summary generation.
 *
 * @since 3.0.0
 */
public interface PatientChartSummaryDAO {
	
	/**
	 * Returns all non-voided encounters for the given patient within the optional date range.
	 *
	 * @param patient the patient
	 * @param fromDate the start date (inclusive), or null for no lower bound
	 * @param toDate the end date (inclusive), or null for no upper bound
	 * @return the list of encounters
	 */
	List<Encounter> getEncounters(Patient patient, Date fromDate, Date toDate);
	
	/**
	 * Returns all non-voided observations for the given patient within the optional date range.
	 *
	 * @param patient the patient
	 * @param fromDate the start date (inclusive), or null for no lower bound
	 * @param toDate the end date (inclusive), or null for no upper bound
	 * @return the list of observations
	 */
	List<Obs> getObservations(Patient patient, Date fromDate, Date toDate);
	
	/**
	 * Returns all non-voided orders for the given patient within the optional date range.
	 *
	 * @param patient the patient
	 * @param fromDate the start date (inclusive), or null for no lower bound
	 * @param toDate the end date (inclusive), or null for no upper bound
	 * @return the list of orders
	 */
	List<Order> getOrders(Patient patient, Date fromDate, Date toDate);
	
	/**
	 * Returns all non-voided visits for the given patient within the optional date range.
	 *
	 * @param patient the patient
	 * @param fromDate the start date (inclusive), or null for no lower bound
	 * @param toDate the end date (inclusive), or null for no upper bound
	 * @return the list of visits
	 */
	List<Visit> getVisits(Patient patient, Date fromDate, Date toDate);
}
