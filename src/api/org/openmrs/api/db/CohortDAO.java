/**
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
	
}
