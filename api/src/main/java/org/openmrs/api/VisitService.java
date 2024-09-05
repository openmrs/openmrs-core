/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.PrivilegeConstants;

/**
 * This service contains methods relating to visits.
 * 
 * @since 1.9
 */
public interface VisitService extends OpenmrsService {
	
	/**
	 * Gets all visit types.
	 * 
	 * @return a list of visit type objects.
	 * <strong>Should</strong> get all visit types
	 */
	@Authorized( { PrivilegeConstants.GET_VISIT_TYPES })
	List<VisitType> getAllVisitTypes();
	
	/**
	 * Get all visit types based on includeRetired flag
	 * 
	 * @param includeRetired
	 * @return List of all visit types
	 * @since 1.9
	 * <strong>Should</strong> get all visit types based on include retired flag.
	 */
	@Authorized( { PrivilegeConstants.MANAGE_VISIT_TYPES })
	public List<VisitType> getAllVisitTypes(boolean includeRetired);
	
	/**
	 * Gets a visit type by its visit type id.
	 * 
	 * @param visitTypeId the visit type id.
	 * @return the visit type object found with the given id, else null.
	 * <strong>Should</strong> get correct visit type
	 */
	@Authorized( { PrivilegeConstants.GET_VISIT_TYPES })
	VisitType getVisitType(Integer visitTypeId);
	
	/**
	 * Gets a visit type by its UUID.
	 * 
	 * @param uuid the visit type UUID.
	 * @return the visit type object found with the given uuid, else null.
	 * <strong>Should</strong> get correct visit type
	 */
	@Authorized( { PrivilegeConstants.GET_VISIT_TYPES })
	VisitType getVisitTypeByUuid(String uuid);
	
	/**
	 * Gets all visit types whose names are similar to or contain the given search phrase.
	 * 
	 * @param fuzzySearchPhrase the search phrase to use.
	 * @return a list of all visit types with names similar to or containing the given phrase
	 * <strong>Should</strong> get correct visit types
	 */
	@Authorized( { PrivilegeConstants.GET_VISIT_TYPES })
	List<VisitType> getVisitTypes(String fuzzySearchPhrase);
	
	/**
	 * Creates or updates the given visit type in the database.
	 * 
	 * @param visitType the visit type to create or update.
	 * @return the created or updated visit type.
	 * <strong>Should</strong> save new visit type
	 * <strong>Should</strong> save edited visit type
	 * <strong>Should</strong> throw error when name is null
	 * <strong>Should</strong> throw error when name is empty string
	 */
	@Authorized( { PrivilegeConstants.MANAGE_VISIT_TYPES })
	VisitType saveVisitType(VisitType visitType) throws APIException;
	
	/**
	 * Retires a given visit type.
	 * 
	 * @param visitType the visit type to retire.
	 * @param reason the reason why the visit type is retired.
	 * @return the visit type that has been retired.
	 * <strong>Should</strong> retire given visit type
	 */
	@Authorized( { PrivilegeConstants.MANAGE_VISIT_TYPES })
	VisitType retireVisitType(VisitType visitType, String reason);
	
	/**
	 * Unretires a visit type.
	 * 
	 * @param visitType the visit type to unretire.
	 * @return the unretired visit type
	 * <strong>Should</strong> unretire given visit type
	 */
	@Authorized( { PrivilegeConstants.MANAGE_VISIT_TYPES })
	VisitType unretireVisitType(VisitType visitType);
	
	/**
	 * Completely removes a visit type from the database. This is not reversible.
	 * 
	 * @param visitType the visit type to delete from the database.
	 * <strong>Should</strong> delete given visit type
	 */
	@Authorized( { PrivilegeConstants.MANAGE_VISIT_TYPES })
	void purgeVisitType(VisitType visitType);
	
	/**
	 * Gets all unvoided visits in the database.
	 * 
	 * @return a list of visit objects.
	 * @throws APIException
	 * <strong>Should</strong> return all unvoided visits
	 */
	@Authorized(PrivilegeConstants.GET_VISITS)
	public List<Visit> getAllVisits() throws APIException;
	
	/**
	 * Gets a visit by its visit id.
	 * 
	 * @param visitId the visit id.
	 * @return the visit object found with the given id, else null.
	 * @throws APIException
	 */
	@Authorized(PrivilegeConstants.GET_VISITS)
	public Visit getVisit(Integer visitId) throws APIException;
	
	/**
	 * Gets a visit by its UUID.
	 * 
	 * @param uuid the visit UUID.
	 * @return the visit object found with the given uuid, else null.
	 * @throws APIException
	 * <strong>Should</strong> return a visit matching the specified uuid
	 */
	@Authorized(PrivilegeConstants.GET_VISITS)
	public Visit getVisitByUuid(String uuid) throws APIException;
	
	/**
	 * Creates or updates the given visit in the database.
	 * 
	 * @param visit the visit to create or update.
	 * @return the created or updated visit.
	 * @throws APIException
	 * <strong>Should</strong> add a new visit to the database
	 * <strong>Should</strong> update an existing visit in the database
	 * <strong>Should</strong> fail if validation errors are found
	 * <strong>Should</strong> pass if no validation errors are found
	 * <strong>Should</strong> be able to add an attribute to a visit
	 * <strong>Should</strong> void an attribute if max occurs is 1 and same attribute type already exists
	 * <strong>Should</strong> save a visit though changedBy and dateCreated are not set for VisitAttribute
	 *         explicitly
	 * <strong>Should</strong> should save new visit with encounters successfully
	 */
	@Authorized( { PrivilegeConstants.ADD_VISITS, PrivilegeConstants.EDIT_VISITS })
	public Visit saveVisit(Visit visit) throws APIException;
	
	/**
	 * Sets the stopDate of a given visit.
	 * 
	 * @param visit the visit whose stopDate is to be set
	 * @param stopDate the date and time the visit is ending. if null, current date is used
	 * @return the visit that was ended
	 * <strong>Should</strong> set the stopDateTime of visit
	 * <strong>Should</strong> set stopdatetime as current date if stopdate is null
	 * <strong>Should</strong> not fail if no validation errors are found
	 * <strong>Should</strong> fail if validation errors are found
	 */
	@Authorized( { PrivilegeConstants.EDIT_VISITS })
	public Visit endVisit(Visit visit, Date stopDate) throws APIException;
	
	/**
	 * Voids the given visit.
	 * 
	 * @param visit the visit to void.
	 * @param reason the reason why the visit is voided
	 * @return the visit that has been voided
	 * @throws APIException
	 * <strong>Should</strong> void the visit and set the voidReason
	 * <strong>Should</strong> void encounters with visit
	 */
	@Authorized(PrivilegeConstants.DELETE_VISITS)
	public Visit voidVisit(Visit visit, String reason) throws APIException;
	
	/**
	 * Unvoids the given visit.
	 * 
	 * @param visit the visit to unvoid
	 * @return the unvoided visit
	 * @throws APIException
	 * <strong>Should</strong> unvoid the visit and unset all the void related fields
	 * <strong>Should</strong> unvoid encounters voided with visit
	 */
	@Authorized(PrivilegeConstants.DELETE_VISITS)
	public Visit unvoidVisit(Visit visit) throws APIException;
	
	/**
	 * Completely erases a visit from the database. This is not reversible.
	 * 
	 * @param visit the visit to delete from the database.
	 * @throws APIException
	 * <strong>Should</strong> erase the visit from the database
	 * <strong>Should</strong> fail if the visit has encounters associated to it
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
	 * @param includeInactive if false, the min/maxEndDatetime parameters are ignored and only open
	 *            visits are returned
	 * @param includeVoided specifies if voided visits should also be returned
	 * @return a list of visits
	 * @see #getActiveVisitsByPatient(Patient)
	 * @throws APIException
	 * <strong>Should</strong> get visits by visit type
	 * <strong>Should</strong> get visits by patients
	 * <strong>Should</strong> get visits by locations
	 * <strong>Should</strong> get visits by indications
	 * <strong>Should</strong> get visits started between the given start dates
	 * <strong>Should</strong> get visits ended between the given end dates
	 * <strong>Should</strong> get visits that are still open even if minStartDatetime is specified
	 * <strong>Should</strong> return all visits if includeVoided is set to true
	 * <strong>Should</strong> get all visits with given attribute values
	 * <strong>Should</strong> not find any visits if none have given attribute values
	 */
	@Authorized(PrivilegeConstants.GET_VISITS)
	public List<Visit> getVisits(Collection<VisitType> visitTypes, Collection<Patient> patients,
	        Collection<Location> locations, Collection<Concept> indications, Date minStartDatetime, Date maxStartDatetime,
	        Date minEndDatetime, Date maxEndDatetime, Map<VisitAttributeType, Object> attributeValues,
	        boolean includeInactive, boolean includeVoided) throws APIException;
	
	/**
	 * Gets all unvoided visits for the specified patient
	 * 
	 * @param patient the patient whose visits to get
	 * @return a list of visits
	 * @throws APIException
	 * <strong>Should</strong> return all unvoided visits for the specified patient
	 */
	@Authorized(PrivilegeConstants.GET_VISITS)
	public List<Visit> getVisitsByPatient(Patient patient) throws APIException;
	
	/**
	 * Convenience method that delegates to getVisitsByPatient(patient, false, false)
	 * 
	 * @param patient the patient whose visits to get
	 * @return a list of visits
	 * @throws APIException
	 */
	@Authorized(PrivilegeConstants.GET_VISITS)
	public List<Visit> getActiveVisitsByPatient(Patient patient) throws APIException;
	
	/**
	 * Gets all visits for the specified patient
	 * 
	 * @param patient the patient whose visits to get
	 * @param includeInactive
	 * @param includeVoided
	 * @return a list of visits
	 * @throws APIException
	 * <strong>Should</strong> return all active unvoided visits for the specified patient
	 * <strong>Should</strong> return all unvoided visits for the specified patient
	 * <strong>Should</strong> return all active visits for the specified patient
	 */
	@Authorized(PrivilegeConstants.GET_VISITS)
	public List<Visit> getVisitsByPatient(Patient patient, boolean includeInactive, boolean includeVoided)
	        throws APIException;
	
	/**
	 * @return all {@link VisitAttributeType}s
	 * <strong>Should</strong> return all visit attribute types including retired ones
	 */
	@Authorized(PrivilegeConstants.GET_VISIT_ATTRIBUTE_TYPES)
	List<VisitAttributeType> getAllVisitAttributeTypes();
	
	/**
	 * @param id
	 * @return the {@link VisitAttributeType} with the given internal id
	 * <strong>Should</strong> return the visit attribute type with the given id
	 * <strong>Should</strong> return null if no visit attribute type exists with the given id
	 */
	@Authorized(PrivilegeConstants.GET_VISIT_ATTRIBUTE_TYPES)
	VisitAttributeType getVisitAttributeType(Integer id);
	
	/**
	 * @param uuid
	 * @return the {@link VisitAttributeType} with the given uuid
	 * <strong>Should</strong> return the visit attribute type with the given uuid
	 * <strong>Should</strong> return null if no visit attribute type exists with the given uuid
	 */
	@Authorized(PrivilegeConstants.GET_VISIT_ATTRIBUTE_TYPES)
	VisitAttributeType getVisitAttributeTypeByUuid(String uuid);
	
	/**
	 * Creates or updates the given visit attribute type in the database
	 * 
	 * @param visitAttributeType
	 * @return the VisitAttributeType created/saved
	 * <strong>Should</strong> create a new visit attribute type
	 * <strong>Should</strong> edit an existing visit attribute type
	 */
	@Authorized(PrivilegeConstants.MANAGE_VISIT_ATTRIBUTE_TYPES)
	VisitAttributeType saveVisitAttributeType(VisitAttributeType visitAttributeType);
	
	/**
	 * Retires the given visit attribute type in the database
	 * 
	 * @param visitAttributeType
	 * @return the visitAttribute retired
	 * <strong>Should</strong> retire a visit attribute type
	 */
	@Authorized(PrivilegeConstants.MANAGE_VISIT_ATTRIBUTE_TYPES)
	VisitAttributeType retireVisitAttributeType(VisitAttributeType visitAttributeType, String reason);
	
	/**
	 * Restores a visit attribute type that was previous retired in the database
	 * 
	 * @param visitAttributeType
	 * @return the VisitAttributeType unretired
	 * <strong>Should</strong> unretire a retired visit attribute type
	 */
	@Authorized(PrivilegeConstants.MANAGE_VISIT_ATTRIBUTE_TYPES)
	VisitAttributeType unretireVisitAttributeType(VisitAttributeType visitAttributeType);
	
	/**
	 * Completely removes a visit attribute type from the database
	 * 
	 * @param visitAttributeType
	 * <strong>Should</strong> completely remove a visit attribute type
	 */
	@Authorized(PrivilegeConstants.PURGE_VISIT_ATTRIBUTE_TYPES)
	void purgeVisitAttributeType(VisitAttributeType visitAttributeType);
	
	/**
	 * @param uuid
	 * @return the {@link VisitAttribute} with the given uuid
	 * <strong>Should</strong> get the visit attribute with the given uuid
	 * <strong>Should</strong> return null if no visit attribute has the given uuid
	 */
	@Authorized(PrivilegeConstants.GET_VISITS)
	VisitAttribute getVisitAttributeByUuid(String uuid);
	
	/**
	 * Stops all active visits started before or on the specified date which match any of the visit
	 * types specified by the {@link OpenmrsConstants#GP_VISIT_TYPES_TO_AUTO_CLOSE} global property.
	 * If startDatetime is null, the default will be end of the current day.
	 * 
	 * @param maximumStartDate Visits started on or before this date time value will get stopped
	 * <strong>Should</strong> close all unvoided active visit matching the specified visit types
	 */
	@Authorized(PrivilegeConstants.EDIT_VISITS)
	public void stopVisits(Date maximumStartDate);
}
