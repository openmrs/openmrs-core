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

import java.util.List;

import org.openmrs.Cohort;

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
	public Cohort getCohort(Integer id) throws DAOException;
	
	/**
	 * Finds a cohort by name
	 * 
	 * @param name
	 * @return The Cohort with the given name, or null if none exists
	 */
	public Cohort getCohort(String name);
	
	/**
	 * Gets all cohorts in the database
	 * 
	 * @param includeVoided whether to include voided cohorts
	 * @return All cohorts in the database, possibly including voided ones
	 * @throws DAOException
	 */
	public List<Cohort> getAllCohorts(boolean includeVoided) throws DAOException;
	
	/**
	 * Finds all cohorts that contain the given patientId
	 * 
	 * @param patientId
	 * @return List<Cohort> object of matching Cohorts
	 * @throws DAOException
	 */
	public List<Cohort> getCohortsContainingPatientId(Integer patientId) throws DAOException;
	
	/**
	 * Saves a Cohort to the database
	 * 
	 * @param cohort Cohort to save
	 * @return the saved Cohort
	 */
	public Cohort saveCohort(Cohort cohort) throws DAOException;
	
	/**
	 * Finds all Cohorts with matching names
	 * 
	 * @param nameFragment
	 * @return List<Cohort> object of matching Cohorts
	 */
	public List<Cohort> getCohorts(String nameFragment) throws DAOException;
	
	/**
	 * Removes a cohort from the database
	 * 
	 * @param cohort
	 * @return the deleted Cohort
	 */
	public Cohort deleteCohort(Cohort cohort) throws DAOException;
	
	/**
	 * Auto generated method comment
	 * 
	 * @param uuid
	 * @return
	 */
	public Cohort getCohortByUuid(String uuid);
	
}
