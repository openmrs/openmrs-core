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

import org.openmrs.Cohort;
import org.openmrs.CohortMembership;

/**
 * Database methods for cohort objects.
 * 
 * @see org.openmrs.Cohort
 * @see org.openmrs.api.CohortService
 * @see org.openmrs.api.context.Context
 */
public interface CohortDAO {
	
	/**
	 * Finds the cohort with the given primary key
	 * 
	 * @param id
	 * @return The cohort with the given cohortId, or null if none exists
	 * @throws DAOException
	 */
	Cohort getCohort(Integer id) throws DAOException;
	
	/**
	 * Finds a cohort by name
	 * 
	 * @param name
	 * @return The Cohort with the given name, or null if none exists
	 */
	Cohort getCohort(String name);
	
	/**
	 * Gets all cohorts in the database
	 * 
	 * @param includeVoided whether to include voided cohorts
	 * @return All cohorts in the database, possibly including voided ones
	 * @throws DAOException
	 */
	List<Cohort> getAllCohorts(boolean includeVoided) throws DAOException;

	/**
	 * Finds all cohorts that contain the given patientId
	 *
	 * @param patientId patient id to get the cohorts containing the patient
	 * @param includeVoided voided true/false whether or not to include voided cohorts
	 * @param asOfDate
	 * @return matching cohorts
	 * @throws DAOException
	 */
	List<Cohort> getCohortsContainingPatientId(Integer patientId, boolean includeVoided, Date asOfDate);

	/**
	 * Saves a Cohort to the database
	 * 
	 * @param cohort Cohort to save
	 * @return the saved Cohort
	 */
	Cohort saveCohort(Cohort cohort) throws DAOException;
	
	/**
	 * Finds all Cohorts with matching names
	 * 
	 * @param nameFragment
	 * @return List&lt;Cohort&gt; object of matching Cohorts
	 */
	List<Cohort> getCohorts(String nameFragment) throws DAOException;
	
	/**
	 * Removes a cohort from the database
	 * 
	 * @param cohort
	 * @return the deleted Cohort
	 */
	Cohort deleteCohort(Cohort cohort) throws DAOException;
	
	/**
	 * @param uuid
	 * @return cohort or null
	 */
	Cohort getCohortByUuid(String uuid);
	
	/**
	 * @param uuid
	 * @return cohort membership or null
	 * @since 2.1.0
	 */
	CohortMembership getCohortMembershipByUuid(String uuid);
	
	/**
	 * @param patientId
	 * @param activeOnDate optional
	 * @param includeVoided
	 * @return cohort memberships for the given patient (optionally active on a given date)
	 * @since 2.1.0
	 */
	List<CohortMembership> getCohortMemberships(Integer patientId, Date activeOnDate, boolean includeVoided);
	
	/**
	 * @param cohortMembership
	 * @return the cohortMembership (now persisted or updated)
	 * @since 2.1.0
	 */
	CohortMembership saveCohortMembership(CohortMembership cohortMembership);
}
