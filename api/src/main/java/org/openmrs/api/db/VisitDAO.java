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

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.VisitAttribute;
import org.openmrs.VisitAttributeType;
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
	 * @see org.openmrs.api.VisitService#getAllVisitTypes(boolean)
	 */
	public List<VisitType> getAllVisitTypes(boolean includeRetired) throws DAOException;
	
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
	public void deleteVisit(Visit visit) throws DAOException;
	
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
	 * @param includeInactive specifies if inactive visits should be returned or not
	 * @param includeVoided specifies if voided visits should also be returned
	 * @return a list of visits
	 * @throws DAOException
	 * <strong>Should</strong> return all unvoided visits if includeEnded is set to true
	 * <strong>Should</strong> return only active visits if includeEnded is set to false
	 */
	public List<Visit> getVisits(Collection<VisitType> visitTypes, Collection<Patient> patients,
	        Collection<Location> locations, Collection<Concept> indications, Date minStartDatetime, Date maxStartDatetime,
	        Date minEndDatetime, Date maxEndDatetime, Map<VisitAttributeType, String> serializedAttributeValues,
	        boolean includeInactive, boolean includeVoided) throws DAOException;
	
	/**
	 * @see VisitService#getAllVisitAttributeTypes()
	 */
	List<VisitAttributeType> getAllVisitAttributeTypes();
	
	/**
	 * @see VisitService#getVisitAttributeType(Integer)
	 */
	VisitAttributeType getVisitAttributeType(Integer id);
	
	/**
	 * @see VisitService#getVisitAttributeTypeByUuid(String)
	 */
	VisitAttributeType getVisitAttributeTypeByUuid(String uuid);
	
	/**
	 * @see VisitService#saveVisitAttributeType(VisitAttributeType)
	 */
	VisitAttributeType saveVisitAttributeType(VisitAttributeType visitAttributeType);
	
	/**
	 * Completely removes a visit attribute type from the database
	 * 
	 * @param visitAttributeType
	 */
	void deleteVisitAttributeType(VisitAttributeType visitAttributeType);
	
	/**
	 * @see VisitService#getVisitAttributeByUuid(String)
	 */
	VisitAttribute getVisitAttributeByUuid(String uuid);
	
	/**
	 * Gets the next active visit which matches the specified visit types
	 * 
	 * @param previousVisit the visit that precedes the one we are fetching
	 * @param visitTypes a collection of visit types to match against
	 * @param maximumStartDate the next visit should have been created before or at this date time
	 * @return a {@link Visit}
	 * <strong>Should</strong> return the next unvoided active visit matching the specified types and startDate
	 */
	public Visit getNextVisit(Visit previousVisit, Collection<VisitType> visitTypes, Date maximumStartDate);
	
}
