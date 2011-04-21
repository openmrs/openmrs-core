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

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.VisitType;
import org.openmrs.api.APIException;
import org.openmrs.api.VisitService;

/**
 * Database access functions for visits.
 * 
 * @since 1.9
 */
public interface VisitDAO {
	
	/**
	 * @see org.openmrs.api.VisitService#getAllVisitTypes()
	 */
	List<VisitType> getAllVisitTypes() throws APIException;
	
	/**
	 * @see org.openmrs.api.VisitService#getVisitType(java.lang.Integer)
	 */
	VisitType getVisitType(Integer visitTypeId);
	
	/**
	 * @see org.openmrs.api.VisitService#getVisitTypeByUuid(java.lang.String)
	 */
	VisitType getVisitTypeByUuid(String uuid);
	
	/**
	 * @see org.openmrs.api.VisitService#getVisitTypes(java.lang.String)
	 */
	List<VisitType> getVisitTypes(String fuzzySearchPhrase);
	
	/**
	 * @see org.openmrs.api.VisitService#saveVisitType(org.openmrs.VisitType)
	 */
	VisitType saveVisitType(VisitType visitType);
	
	/**
	 * @see org.openmrs.api.VisitService#purgeVisitType(org.openmrs.VisitType)
	 */
	void purgeVisitType(VisitType visitType);
	
	/**
	 * Gets all visits in the database if includeVoided is set to true otherwise returns only
	 * unvoided ones
	 * 
	 * @param includeVoided Specifies if voided visits should be returned or not
	 * @return a list of visits
	 * @throws DAOException
	 */
	public List<Visit> getVisits(boolean includeVoided) throws DAOException;
	
	/**
	 * @see VisitService#getVisit(Integer)
	 * @throws DAOException
	 */
	public Visit getVisit(Integer visitId) throws DAOException;
	
	/**
	 * @see VisitService#getVisitByUuid(String)
	 * @throws DAOException
	 */
	public Visit getVisitByUuid(String uuid) throws DAOException;
	
	/**
	 * @see VisitService#saveVisit(Visit)
	 * @throws DAOException
	 */
	public Visit saveVisit(Visit visit) throws DAOException;
	
	/**
	 * @see VisitService#purgeVisit(Visit)
	 * @throws DAOException
	 */
	public void purgeVisit(Visit visit) throws DAOException;
	
	/**
	 * Gets the visits matching the specified arguments
	 * 
	 * @param visitTypes a list of visit types to match against
	 * @param patients a list of patients to match against
	 * @param locations a list of locations to match against
	 * @param indications a list of indication concepts to match against
	 * @param minStartDatetime the minimum visit start date to match against
	 * @param maxStartDatetime the maximum visit start date to match against
	 * @param minEndDatetime the minimum visit end date to match against
	 * @param maxEndDatetime the maximum visit end date to match against
	 * @param includeInactive specifies if visits that are no longer active should be returned or
	 *            not, inactive visits are visits whose end date is not null.
	 * @param includeVoided specifies if voided visits should also be returned
	 * @return a list of visits
	 * @throws DAOException
	 * @should return all unvoided visits if includeInactive is set to true
	 * @should return only active visits if includeInactive is set to false
	 */
	public List<Visit> getVisits(Collection<VisitType> visitTypes, Collection<Patient> patients,
	        Collection<Location> locations, Collection<Concept> indications, Date minStartDatetime, Date maxStartDatetime,
	        Date minEndDatetime, Date maxEndDatetime, boolean includeInactive, boolean includeVoided) throws DAOException;
}
