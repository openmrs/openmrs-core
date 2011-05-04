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
package org.openmrs.api;

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
import org.openmrs.annotation.Authorized;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.transaction.annotation.Transactional;

/**
 * This service contains methods relating to visits.
 * 
 * @since 1.9
 */
@Transactional
public interface VisitService extends OpenmrsService {
	
	/**
	 * Gets all visit types.
	 * 
	 * @return a list of visit type objects.
	 * @should get all visit types
	 */
	@Transactional(readOnly = true)
	@Authorized( { PrivilegeConstants.VIEW_VISIT_TYPES })
	List<VisitType> getAllVisitTypes();
	
	/**
	 * Gets a visit type by its visit type id.
	 * 
	 * @param visitTypeId the visit type id.
	 * @return the visit type object found with the given id, else null.
	 * @should get correct visit type
	 */
	@Transactional(readOnly = true)
	@Authorized( { PrivilegeConstants.VIEW_VISIT_TYPES })
	VisitType getVisitType(Integer visitTypeId);
	
	/**
	 * Gets a visit type by its UUID.
	 * 
	 * @param uuid the visit type UUID.
	 * @return the visit type object found with the given uuid, else null.
	 * @should get correct visit type
	 */
	@Transactional(readOnly = true)
	@Authorized( { PrivilegeConstants.VIEW_VISIT_TYPES })
	VisitType getVisitTypeByUuid(String uuid);
	
	/**
	 * Gets all visit types whose names are similar to or contain the given search phrase.
	 * 
	 * @param fuzzySearchPhrase the search phrase to use.
	 * @return a list of all visit types with names similar to or containing the given phrase
	 * @should get correct visit types
	 */
	@Transactional(readOnly = true)
	@Authorized( { PrivilegeConstants.VIEW_VISIT_TYPES })
	List<VisitType> getVisitTypes(String fuzzySearchPhrase);
	
	/**
	 * Creates or updates the given visit type in the database.
	 * 
	 * @param visitType the visit type to create or update.
	 * @return the created or updated visit type.
	 * @should save new visit type
	 * @should save edited visit type
	 * @should throw error when name is null
	 * @should throw error when name is empty string
	 */
	@Authorized( { PrivilegeConstants.MANAGE_VISIT_TYPES })
	VisitType saveVisitType(VisitType visitType) throws APIException;
	
	/**
	 * Retires a given visit type.
	 * 
	 * @param visitType the visit type to retire.
	 * @param reason the reason why the visit type is retired.
	 * @return the visit type that has been retired.
	 * @should retire given visit type
	 */
	@Authorized( { PrivilegeConstants.MANAGE_VISIT_TYPES })
	VisitType retireVisitType(VisitType visitType, String reason);
	
	/**
	 * Unretires a visit type.
	 * 
	 * @param visitType the visit type to unretire.
	 * @return the unretired visit type
	 * @should unretire given visit type
	 */
	@Authorized( { PrivilegeConstants.MANAGE_VISIT_TYPES })
	VisitType unretireVisitType(VisitType visitType);
	
	/**
	 * Completely removes a visit type from the database. This is not reversible.
	 * 
	 * @param visitType the visit type to delete from the database.
	 * @should delete given visit type
	 */
	@Authorized( { PrivilegeConstants.MANAGE_VISIT_TYPES })
	void purgeVisitType(VisitType visitType);
	
	/**
	 * Gets all unvoided visits in the database.
	 * 
	 * @return a list of visit objects.
	 * @throws APIException
	 * @should return all unvoided visits
	 */
	@Transactional(readOnly = true)
	@Authorized(PrivilegeConstants.VIEW_VISITS)
	public List<Visit> getAllVisits() throws APIException;
	
	/**
	 * Gets a visit by its visit id.
	 * 
	 * @param visitId the visit id.
	 * @return the visit object found with the given id, else null.
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized(PrivilegeConstants.VIEW_VISITS)
	public Visit getVisit(Integer visitId) throws APIException;
	
	/**
	 * Gets a visit by its UUID.
	 * 
	 * @param uuid the visit UUID.
	 * @return the visit object found with the given uuid, else null.
	 * @throws APIException
	 * @should return a visit matching the specified uuid
	 */
	@Transactional(readOnly = true)
	@Authorized(PrivilegeConstants.VIEW_VISITS)
	public Visit getVisitByUuid(String uuid) throws APIException;
	
	/**
	 * Creates or updates the given visit in the database.
	 * 
	 * @param visit the visit to create or update.
	 * @return the created or updated visit.
	 * @throws APIException
	 * @should add a new visit to the database
	 * @should update an existing visit in the database
	 * @should fail if validation errors are found
	 * @should pass if no validation errors are found
	 * @should be able to add an attribute to a visit
	 */
	@Authorized( { PrivilegeConstants.ADD_VISITS, PrivilegeConstants.EDIT_VISITS })
	public Visit saveVisit(Visit visit) throws APIException;
	
	/**
	 * Voids the given visit.
	 * 
	 * @param visit the visit to void.
	 * @param reason the reason why the visit is voided
	 * @return the visit that has been voided
	 * @throws APIException
	 * @should void the visit and set the voidReason
	 */
	@Authorized(PrivilegeConstants.DELETE_VISITS)
	public Visit voidVisit(Visit visit, String reason) throws APIException;
	
	/**
	 * Unvoids the given visit.
	 * 
	 * @param visit the visit to unvoid
	 * @return the unvoided visit
	 * @throws APIException
	 * @should unvoid the visit and unset all the void related fields
	 */
	@Authorized(PrivilegeConstants.DELETE_VISITS)
	public Visit unvoidVisit(Visit visit) throws APIException;
	
	/**
	 * Completely erases a visit from the database. This is not reversible.
	 * 
	 * @param visit the visit to delete from the database.
	 * @throws APIException
	 * @should erase the visit from the database
	 * @should fail if the visit has encounters associated to it
	 */
	@Authorized(PrivilegeConstants.PURGE_VISITS)
	public void purgeVisit(Visit visit) throws APIException;
	
	/**
	 * Gets the visits matching the specified arguments
	 * 
	 * @param visitTypes a list of visit types to match against
	 * @param locations a list of locations to match against
	 * @param indications a list of indication concepts to match against
	 * @param minStartDatetime the minimum visit start date to match against
	 * @param maxStartDatetime the maximum visit start date to match against
	 * @param minEndDatetime the minimum visit end date to match against
	 * @param maxEndDatetime the maximum visit end date to match against
	 * @param includeVoided specifies if voided visits should also be returned
	 * @return a list of visits
	 * @see #getActiveVisitsByPatient(Patient)
	 * @throws APIException
	 * @should get visits by visit type
	 * @should get visits by patients
	 * @should get visits by locations
	 * @should get visits by indications
	 * @should get visits started between the given start dates
	 * @should get visits ended between the given end dates
	 * @should return all visits if includeVoided is set to true
	 * @should get all visits with given attribute values
	 * @should not find any visits if none have given attribute values
	 */
	@Transactional(readOnly = true)
	@Authorized(PrivilegeConstants.VIEW_VISITS)
	public List<Visit> getVisits(Collection<VisitType> visitTypes, Collection<Patient> patients,
	        Collection<Location> locations, Collection<Concept> indications, Date minStartDatetime, Date maxStartDatetime,
	        Date minEndDatetime, Date maxEndDatetime, Map<VisitAttributeType, Object> attributeValues, boolean includeVoided)
	        throws APIException;
	
	/**
	 * Gets all unvoided visits for the specified patient
	 * 
	 * @param patient the patient whose visits to get
	 * @return a list of visits
	 * @throws APIException
	 * @should return all unvoided visits for the specified patient
	 */
	@Transactional(readOnly = true)
	@Authorized(PrivilegeConstants.VIEW_VISITS)
	public List<Visit> getVisitsByPatient(Patient patient) throws APIException;
	
	/**
	 * Gets all active unvoided visits for the specified patient i.e visits whose end date is null
	 * 
	 * @param patient the patient whose visits to get
	 * @return a list of visits
	 * @throws APIException
	 * @should return all unvoided active visits for the specified patient
	 */
	@Transactional(readOnly = true)
	@Authorized(PrivilegeConstants.VIEW_VISITS)
	public List<Visit> getActiveVisitsByPatient(Patient patient) throws APIException;
	
	/**
	 * @return all {@link VisitAttributeType}s
	 * @should return all visit attribute types including retired ones
	 */
	@Transactional(readOnly = true)
	@Authorized(PrivilegeConstants.VIEW_VISIT_ATTRIBUTE_TYPES)
	List<VisitAttributeType> getAllVisitAttributeTypes();
	
	/**
	 * @param id
	 * @return the {@link VisitAttributeType} with the given internal id
	 * @should return the visit attribute type with the given id
	 * @should return null if no visit attribute type exists with the given id
	 */
	@Transactional(readOnly = true)
	@Authorized(PrivilegeConstants.VIEW_VISIT_ATTRIBUTE_TYPES)
	VisitAttributeType getVisitAttributeType(Integer id);
	
	/**
	 * @param uuid
	 * @return the {@link VisitAttributeType} with the given uuid
	 * @should return the visit attribute type with the given uuid
	 * @should return null if no visit attribute type exists with the given uuid
	 */
	@Transactional(readOnly = true)
	@Authorized(PrivilegeConstants.VIEW_VISIT_ATTRIBUTE_TYPES)
	VisitAttributeType getVisitAttributeTypeByUuid(String uuid);
	
	/**
	 * Creates or updates the given visit attribute type in the database
	 * 
	 * @param visitAttributeType
	 * @return the visitAttribute created/saved
	 * @should create a new visit attribute type
	 * @should edit an existing visit attribute type
	 */
	@Authorized(PrivilegeConstants.MANAGE_VISIT_ATTRIBUTE_TYPES)
	VisitAttributeType saveVisitAttributeType(VisitAttributeType visitAttributeType);
	
	/**
	 * Retires the given visit attribute type in the database
	 * 
	 * @param visitAttributeType
	 * @return the visitAttribute retired
	 * @should retire a visit attribute type
	 */
	@Authorized(PrivilegeConstants.MANAGE_VISIT_ATTRIBUTE_TYPES)
	VisitAttributeType retireVisitAttributeType(VisitAttributeType visitAttributeType, String reason);
	
	/**
	 * Restores a visit attribute type that was previous retired in the database
	 * 
	 * @param visitAttributeType
	 * @return the visitAttribute unretired
	 * @should unretire a retired visit attribute type
	 */
	@Authorized(PrivilegeConstants.MANAGE_VISIT_ATTRIBUTE_TYPES)
	VisitAttributeType unretireVisitAttributeType(VisitAttributeType visitAttributeType);
	
	/**
	 * Completely removes a visit attribute type from the database
	 * 
	 * @param visitAttributeType
	 * @should completely remove a visit attribute type
	 */
	@Authorized(PrivilegeConstants.PURGE_VISIT_ATTRIBUTE_TYPES)
	void purgeVisitAttributeType(VisitAttributeType visitAttributeType);
	
	/**
	 * @param uuid
	 * @return the {@link VisitAttribute} with the given uuid
	 * @should get the visit attribute with the given uuid
	 * @should return null if no visit attribute has the given uuid
	 */
	@Transactional(readOnly = true)
	@Authorized(PrivilegeConstants.VIEW_VISITS)
	VisitAttribute getVisitAttributeByUuid(String uuid);
	
}
