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

import org.openmrs.Cohort;
import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.Visit;
import org.openmrs.VisitType;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.db.EncounterDAO;
import org.openmrs.api.handler.EncounterVisitHandler;
import org.openmrs.parameter.EncounterSearchCriteria;
import org.openmrs.util.PrivilegeConstants;

/**
 * Services for Encounters and Encounter Types
 * 
 * @version 1.0
 */
public interface EncounterService extends OpenmrsService {
	
	/**
	 * Set the given <code>dao</code> on this encounter service. The dao will act as the conduit
	 * through with all encounter calls get to the database
	 * 
	 * @param dao
	 */
	public void setEncounterDAO(EncounterDAO dao);
	
	/**
	 * Saves a new encounter or updates an existing encounter. If an existing encounter, this method
	 * will automatically apply encounter.patient to all encounter.obs.patient
	 * 
	 * @param encounter to be saved
	 * @throws APIException
	 * @should save encounter with basic details
	 * @should update encounter successfully
	 * @should cascade save to contained obs
	 * @should cascade patient to orders in the encounter
	 * @should cascade save to contained obs when encounter already exists
	 * @should cascade encounter datetime to obs
	 * @should only cascade the obsdatetimes to obs with different initial obsdatetimes
	 * @should not overwrite creator if non null
	 * @should not overwrite dateCreated if non null
	 * @should not overwrite obs and orders creator or dateCreated
	 * @should not assign encounter to visit if no handler is registered
	 * @should not assign encounter to visit if the no assign handler is registered
	 * @should assign encounter to visit if the assign to existing handler is registered
	 * @should assign encounter to visit if the assign to existing or new handler is registered
	 * @should cascade save encounter providers
	 * @should cascade delete encounter providers
	 * @should void and create new obs when saving encounter
	 * @should fail if user is not supposed to edit encounters of type of given encounter
	 */
	@Authorized( { PrivilegeConstants.ADD_ENCOUNTERS, PrivilegeConstants.EDIT_ENCOUNTERS })
	public Encounter saveEncounter(Encounter encounter) throws APIException;
	
	/**
	 * Get encounter by internal identifier
	 * 
	 * @param encounterId encounter id
	 * @return encounter with given internal identifier
	 * @throws APIException
	 * @should throw error if given null parameter
	 * @should fail if user is not allowed to view encounter by given id
	 * @should return encounter if user is allowed to view it
	 */
	@Authorized( { PrivilegeConstants.GET_ENCOUNTERS })
	public Encounter getEncounter(Integer encounterId) throws APIException;
	
	/**
	 * Get Encounter by its UUID
	 * 
	 * @param uuid
	 * @return encounter or null
	 * @should find object given valid uuid
	 * @should return null if no object found with given uuid
	 */
	@Authorized( { PrivilegeConstants.GET_ENCOUNTERS })
	public Encounter getEncounterByUuid(String uuid) throws APIException;
	
	/**
	 * Get all encounters (not voided) for a patient, sorted by encounterDatetime ascending.
	 * 
	 * @param patient
	 * @return List&lt;Encounter&gt; encounters (not voided) for a patient.
	 * @should not get voided encounters
	 * @should throw error when given null parameter
	 */
	@Authorized( { PrivilegeConstants.GET_ENCOUNTERS })
	public List<Encounter> getEncountersByPatient(Patient patient);
	
	/**
	 * Get encounters for a patientId
	 * 
	 * @param patientId
	 * @return all encounters (not voided) for the given patient identifier
	 * @throws APIException
	 * @should not get voided encounters
	 * @should throw error if given a null parameter
	 */
	@Authorized( { PrivilegeConstants.GET_ENCOUNTERS })
	public List<Encounter> getEncountersByPatientId(Integer patientId) throws APIException;
	
	/**
	 * Get encounters (not voided) for a patient identifier
	 * 
	 * @param identifier
	 * @return all encounters (not retired) for the given patient identifier
	 * @throws APIException
	 * @should not get voided encounters
	 * @should throw error if given null parameter
	 */
	@Authorized( { PrivilegeConstants.GET_ENCOUNTERS })
	public List<Encounter> getEncountersByPatientIdentifier(String identifier) throws APIException;
		
	/**
	 * Get all encounters that match a variety of (nullable) criteria. Each extra value for a
	 * parameter that is provided acts as an "and" and will reduce the number of results returned
	 * 
	 * @param who the patient the encounter is for
	 * @param loc the location this encounter took place
	 * @param fromDate the minimum date (inclusive) this encounter took place
	 * @param toDate the maximum date (exclusive) this encounter took place
	 * @param enteredViaForms the form that entered this encounter must be in this list
	 * @param encounterTypes the type of encounter must be in this list
	 * @param providers the provider of this encounter must be in this list
	 * @param visitTypes the visit types of this encounter must be in this list
	 * @param visits the visits of this encounter must be in this list
	 * @param includeVoided true/false to include the voided encounters or not
	 * @return a list of encounters ordered by increasing encounterDatetime
	 * @since 1.9
	 * @should get encounters by location
	 * @should get encounters on or after date
	 * @should get encounters on or up to a date
	 * @should get encounters by form
	 * @should get encounters by type
	 * @should get encounters by provider
	 * @should get encounters by visit type
	 * @should get encounters by visit
	 * @should exclude voided encounters
	 * @should include voided encounters
	 * 
	 * @deprecated As of 2.0, replaced by {@link #getEncounters(EncounterSearchCriteria)}
	 */
	@Deprecated
	@Authorized( { PrivilegeConstants.GET_ENCOUNTERS })
	public List<Encounter> getEncounters(Patient who, Location loc, Date fromDate, Date toDate,
	        Collection<Form> enteredViaForms, Collection<EncounterType> encounterTypes, Collection<Provider> providers,
	        Collection<VisitType> visitTypes, Collection<Visit> visits, boolean includeVoided);
	
	/**
	 * Get all encounters that match a variety of (nullable) criteria contained in the parameter object.
	 * Each extra value for a parameter that is provided acts as an "and" and will reduce the number of results returned
	 *
	 * @param encounterSearchCriteria the object containing search parameters
	 * @return a list of encounters ordered by increasing encounterDatetime
	 * @since 1.12
	 * @should get encounters modified after specified date
	 */
	@Authorized( { PrivilegeConstants.GET_ENCOUNTERS })
	public List<Encounter> getEncounters(EncounterSearchCriteria encounterSearchCriteria);
	
	/**
	 * Voiding a encounter essentially removes it from circulation
	 * 
	 * @param encounter Encounter object to void
	 * @param reason String reason that it's being voided
	 * @should void encounter and set attributes
	 * @should cascade to obs
	 * @should cascade to orders
	 * @should throw error with null reason parameter
	 * @should not void providers
	 * @should fail if user is not supposed to edit encounters of type of given encounter
	 */
	@Authorized( { PrivilegeConstants.EDIT_ENCOUNTERS })
	public Encounter voidEncounter(Encounter encounter, String reason);
	
	/**
	 * Unvoid encounter record
	 * 
	 * @param encounter Encounter to be revived
	 * @should cascade unvoid to obs
	 * @should cascade unvoid to orders
	 * @should unvoid and unmark all attributes
	 * @should fail if user is not supposed to edit encounters of type of given encounter
	 */
	@Authorized( { PrivilegeConstants.EDIT_ENCOUNTERS })
	public Encounter unvoidEncounter(Encounter encounter) throws APIException;
	
	/**
	 * Completely remove an encounter from database. For super users only. If dereferencing
	 * encounters, use <code>voidEncounter(org.openmrs.Encounter)</code>
	 * 
	 * @param encounter encounter object to be purged
	 * @should purgeEncounter
	 * @should fail if user is not supposed to edit encounters of type of given encounter
	 */
	@Authorized( { PrivilegeConstants.PURGE_ENCOUNTERS })
	public void purgeEncounter(Encounter encounter) throws APIException;
	
	/**
	 * Completely remove an encounter from database. For super users only. If dereferencing
	 * encounters, use <code>voidEncounter(org.openmrs.Encounter)</code>
	 * 
	 * @param encounter encounter object to be purged
	 * @param cascade Purge any related observations as well?
	 * @should cascade purge to obs and orders
	 * @should fail if user is not supposed to edit encounters of type of given encounter
	 */
	@Authorized( { PrivilegeConstants.PURGE_ENCOUNTERS })
	public void purgeEncounter(Encounter encounter, boolean cascade) throws APIException;
	
	/**
	 * Save a new Encounter Type or update an existing Encounter Type.
	 * 
	 * @param encounterType
	 * @should save encounter type
	 * @should not overwrite creator
	 * @should not overwrite creator or date created
	 * @should not overwrite date created
	 * @should update an existing encounter type name
	 * @should throw error when trying to save encounter type when encounter types are locked
	 */
	@Authorized( { PrivilegeConstants.MANAGE_ENCOUNTER_TYPES })
	public EncounterType saveEncounterType(EncounterType encounterType) throws APIException;
	
	/**
	 * Get encounterType by internal identifier
	 * 
	 * @param encounterTypeId Integer
	 * @return encounterType with given internal identifier
	 * @throws APIException
	 * @should throw error if given null parameter
	 */
	@Authorized( { PrivilegeConstants.GET_ENCOUNTER_TYPES })
	public EncounterType getEncounterType(Integer encounterTypeId) throws APIException;
	
	/**
	 * Get EncounterType by its UUID
	 * 
	 * @param uuid
	 * @return encounter type or null
	 * @should find object given valid uuid
	 * @should return null if no object found with given uuid
	 */
	@Authorized( { PrivilegeConstants.GET_ENCOUNTER_TYPES })
	public EncounterType getEncounterTypeByUuid(String uuid) throws APIException;
	
	/**
	 * Get encounterType by exact name
	 * 
	 * @param name string to match to an Encounter.name
	 * @return EncounterType that is not retired
	 * @throws APIException
	 * @should not get retired types
	 * @should return null if only retired type found
	 * @should not get by inexact name
	 * @should return null with null name parameter
	 */
	@Authorized( { PrivilegeConstants.GET_ENCOUNTER_TYPES })
	public EncounterType getEncounterType(String name) throws APIException;
	
	/**
	 * Get all encounter types (including retired)
	 * 
	 * @return encounter types list
	 * @throws APIException
	 * @should not return retired types
	 */
	@Authorized( { PrivilegeConstants.GET_ENCOUNTER_TYPES })
	public List<EncounterType> getAllEncounterTypes() throws APIException;
	
	/**
	 * Get all encounter types. If includeRetired is true, also get retired encounter types.
	 * 
	 * @param includeRetired
	 * @return encounter types list
	 * @throws APIException
	 * @should not return retired types
	 * @should include retired types with true includeRetired parameter
	 */
	@Authorized( { PrivilegeConstants.GET_ENCOUNTER_TYPES })
	public List<EncounterType> getAllEncounterTypes(boolean includeRetired) throws APIException;
	
	/**
	 * Find Encounter Types with name matching the beginning of the search string. Search strings
	 * are case insensitive so that "NaMe".equals("name") is true. Includes retired EncounterTypes.
	 * 
	 * @param name of the encounter type to find
	 * @return List&lt;EncounterType&gt; matching encounters
	 * @throws APIException
	 * @should return types by partial name match
	 * @should return types by partial case insensitive match
	 * @should include retired types in the results
	 * @should not partial match name on internal substrings
	 * @should return types ordered on name and nonretired first
	 */
	@Authorized( { PrivilegeConstants.GET_ENCOUNTER_TYPES })
	public List<EncounterType> findEncounterTypes(String name) throws APIException;
	
	/**
	 * Retire an EncounterType. This essentially marks the given encounter type as a non-current
	 * type that shouldn't be used anymore.
	 * 
	 * @param encounterType the encounter type to retire
	 * @param reason required non-null purpose for retiring this encounter type
	 * @throws APIException
	 * @should retire type and set attributes
	 * @should throw error if given null reason parameter
	 * @should should throw error when trying to retire encounter type when encounter types are
	 *         locked
	 */
	@Authorized( { PrivilegeConstants.MANAGE_ENCOUNTER_TYPES })
	public EncounterType retireEncounterType(EncounterType encounterType, String reason) throws APIException;
	
	/**
	 * Unretire an EncounterType. This brings back the given encounter type and says that it can be
	 * used again
	 * 
	 * @param encounterType the encounter type to unretire
	 * @throws APIException
	 * @should unretire type and unmark attributes
	 * @should should throw error when trying to unretire encounter type when encounter types are
	 *         locked
	 */
	@Authorized( { PrivilegeConstants.MANAGE_ENCOUNTER_TYPES })
	public EncounterType unretireEncounterType(EncounterType encounterType) throws APIException;
	
	/**
	 * Completely remove an encounter type from database.
	 * 
	 * @param encounterType
	 * @throws APIException
	 * @should purge type
	 * @should should throw error when trying to delete encounter type when encounter types are
	 *         locked
	 */
	@Authorized( { PrivilegeConstants.PURGE_ENCOUNTER_TYPES })
	public void purgeEncounterType(EncounterType encounterType) throws APIException;
		
	/**
	 * Search for encounters by patient name or patient identifier.
	 * 
	 * @param query patient name or identifier
	 * @return list of encounters for the given patient
	 * @throws APIException
	 * @see EncounterService#getEncountersByPatient(String, boolean)
	 * @since 1.7
	 */
	@Authorized( { PrivilegeConstants.GET_ENCOUNTERS })
	public List<Encounter> getEncountersByPatient(String query) throws APIException;
	
	/**
	 * Search for encounters by patient name or patient identifier.
	 * 
	 * @param query patient name or identifier
	 * @param includeVoided Specifies whether voided encounters should be included
	 * @return list of encounters for the given patient
	 * @throws APIException
	 * @should get all unvoided encounters for the given patient name
	 * @should get all unvoided encounters for the given patient identifier
	 * @should throw error if given null parameter
	 * @should include voided encounters in the returned list if includedVoided is true
	 * @since 1.7
	 */
	@Authorized( { PrivilegeConstants.GET_ENCOUNTERS })
	public List<Encounter> getEncountersByPatient(String query, boolean includeVoided) throws APIException;
	
	/**
	 * Search for encounters by patient name or patient identifier and returns a specific number of
	 * them from the specified starting position. If start and length are not specified, then all
	 * matches are returned
	 * 
	 * @param query patient name or identifier
	 * @param start beginning index for the batch
	 * @param length number of encounters to return in the batch
	 * @param includeVoided Specifies whether voided encounters should be included
	 * @return list of encounters for the given patient based on batch settings
	 * @throws APIException
	 * @since 1.8
	 * @should get all the unique encounters that match the specified parameter values
	 * @should not return voided encounters if includeVoided is set to true
	 * @should return empty list for empty query
	 */
	@Authorized( { PrivilegeConstants.GET_ENCOUNTERS })
	public List<Encounter> getEncounters(String query, Integer start, Integer length, boolean includeVoided)
	        throws APIException;
	
	/**
	 * Searches for encounters by patient id, provider identifier, location, encounter type,
	 * provider, form or provider name. It returns a specific number of them from the specified
	 * starting position. If start and length are not specified, then all matches are returned
	 * 
	 * @param query provider identifier, location, encounter type, provider, form or provider name
	 * @param patientId the patient id
	 * @param start beginning index for the batch
	 * @param length number of encounters to return in the batch
	 * @param includeVoided Specifies whether voided encounters should be included
	 * @return list of encounters for the given patient based on batch settings
	 * @throws APIException
	 * @since 1.10
	 * @should fetch encounters by patient id
	 * @should include voided encounters if includeVoided is set to true
	 * @should should match on provider identifier
	 * @should match on the provider name
	 * @should match on the location name
	 * @should match on the provider person name
	 * @should match on the encounter type name
	 * @should match on the form name
	 */
	@Authorized( { PrivilegeConstants.GET_ENCOUNTERS })
	public List<Encounter> getEncounters(String query, Integer patientId, Integer start, Integer length,
	        boolean includeVoided) throws APIException;
	
	/**
	 * Get all encounters for a cohort of patients
	 * 
	 * @param patients Cohort of patients to search
	 * @return Map of all encounters for specified patients.
	 * @should get all encounters for a cohort of patients
	 * @since 1.8
	 */
	public Map<Integer, List<Encounter>> getAllEncounters(Cohort patients);
	
	/**
	 * Return the number of encounters matching a patient name or patient identifier
	 * 
	 * @param query patient name or identifier
	 * @param includeVoided Specifies whether voided encounters should be included
	 * @return the number of encounters matching the given search phrase
	 * @since 1.8
	 * @should get the correct count of unique encounters
	 */
	@Authorized( { PrivilegeConstants.GET_ENCOUNTERS })
	public Integer getCountOfEncounters(String query, boolean includeVoided);
	
	/**
	 * Gets all encounters grouped within a given visit.
	 * 
	 * @param visit the visit.
	 * @param includeVoided whether voided encounters should be returned
	 * @return list of encounters in the given visit.
	 * @should get active encounters by visit
	 * @should include voided encounters when includeVoided is true
	 * @since 1.9
	 */
	@Authorized( { PrivilegeConstants.GET_ENCOUNTERS })
	List<Encounter> getEncountersByVisit(Visit visit, boolean includeVoided);
	
	/**
	 * @return list of handlers for determining if an encounter should go into a visit. If none are
	 *         found, an empty list.
	 * @see EncounterVisitHandler
	 * @since 1.9
	 * @should return the no assignment handler
	 * @should return the existing visit only assignment handler
	 * @should return the existing or new visit assignment handler
	 */
	public List<EncounterVisitHandler> getEncounterVisitHandlers();
	
	/**
	 * Gets the active handler for assigning visits to encounters.
	 * 
	 * @see EncounterVisitHandler
	 * @since 1.9
	 * @return the active handler class.
	 * @throws APIException thrown if something goes wrong during the retrieval of the handler.
	 */
	public EncounterVisitHandler getActiveEncounterVisitHandler() throws APIException;
	
	/**
	 * Saves a new encounter role or updates an existing encounter role.
	 * 
	 * @param encounterRole to be saved
	 * @throws APIException
	 * @return EncounterRole
	 * @since 1.9
	 * @should save encounter role with basic details
	 * @should update encounter role successfully
	 */
	@Authorized( { PrivilegeConstants.MANAGE_ENCOUNTER_ROLES })
	public EncounterRole saveEncounterRole(EncounterRole encounterRole) throws APIException;
	
	/**
	 * Gets an encounter role when and internal encounter role id is provided.
	 * 
	 * @param encounterRoleId to be retrieved
	 * @throws APIException
	 * @return EncounterRole
	 * @since 1.9
	 */
	@Authorized( { PrivilegeConstants.GET_ENCOUNTER_ROLES })
	public EncounterRole getEncounterRole(Integer encounterRoleId) throws APIException;
	
	/**
	 * Completely remove an encounter role from database. For super users only. If dereferencing
	 * encounter roles, use
	 * <code>retireEncounterRole(org.openmrs.Encounter, java.lang.String)</code>
	 * 
	 * @param encounterRole encounter role object to be purged
	 * @since 1.9
	 * @should purge Encounter Role
	 */
	@Authorized( { PrivilegeConstants.PURGE_ENCOUNTER_ROLES })
	public void purgeEncounterRole(EncounterRole encounterRole) throws APIException;
	
	/**
	 * Get all encounter roles based on includeRetired flag
	 * 
	 * @param includeRetired
	 * @return List of all encounter roles
	 * @since 1.9
	 * @should get all encounter roles based on include retired flag.
	 */
	@Authorized( { PrivilegeConstants.GET_ENCOUNTER_ROLES })
	public List<EncounterRole> getAllEncounterRoles(boolean includeRetired);
	
	/**
	 * Get EncounterRole by its UUID
	 * 
	 * @param uuid
	 * @return EncounterRole
	 * @since 1.9
	 * @should find encounter role based on uuid
	 */
	@Authorized( { PrivilegeConstants.GET_ENCOUNTER_ROLES })
	public EncounterRole getEncounterRoleByUuid(String uuid) throws APIException;
	
	/**
	 * Get EncounterRole by name
	 * 
	 * @param name
	 * @return EncounterRole object by name
	 * @since 1.10
	 * @should find an encounter role identified by its name
	 */
	@Authorized( { PrivilegeConstants.GET_ENCOUNTER_ROLES })
	public EncounterRole getEncounterRoleByName(String name);
	
	/**
	 * Retire an EncounterRole. This essentially marks the given encounter role as a non-current
	 * type that shouldn't be used anymore.
	 * 
	 * @param encounterRole the encounter role to retire
	 * @param reason required non-null purpose for retiring this encounter role
	 * @throws APIException
	 * @since 1.9
	 * @should retire type and set attributes
	 * @should throw error if given null reason parameter
	 */
	@Authorized( { PrivilegeConstants.MANAGE_ENCOUNTER_ROLES })
	public EncounterRole retireEncounterRole(EncounterRole encounterRole, String reason) throws APIException;
	
	/**
	 * Unretire an EncounterRole. This brings back the given encounter role and says that it can be
	 * used again
	 * 
	 * @param encounterType the encounter role to unretire
	 * @throws APIException
	 * @since 1.9
	 * @should unretire type and unmark attributes
	 */
	@Authorized( { PrivilegeConstants.MANAGE_ENCOUNTER_ROLES })
	public EncounterRole unretireEncounterRole(EncounterRole encounterType) throws APIException;
	
	/**
	 * Gets the unvoided encounters for the specified patient that are not assigned to any visit.
	 * Note that this method will return a maximum of 100 encounters.
	 * 
	 * @param patient the patient to match against
	 * @return a list of {@link Encounter}s
	 * @throws APIException
	 * @should return the unvoided encounters not assigned to any visit
	 * @since 1.9
	 */
	@Authorized( { PrivilegeConstants.GET_ENCOUNTERS })
	public List<Encounter> getEncountersNotAssignedToAnyVisit(Patient patient) throws APIException;
	
	/**
	 * Gets encounters for the given patient. It populates results with empty encounters to include
	 * visits that have no assigned encounters.
	 * <p>
	 * The empty encounters have only visit set.
	 * 
	 * @param patient the patient to match
	 * @param includeVoided if voided encounters or visits should be included
	 * @param query filters results (defaults to return all results if <code>null</code>)
	 * @param start index to start with (defaults to 0 if <code>null</code>)
	 * @param length number of results to return (default to return all results if <code>null</code>)
	 * @return encounters and empty encounters with only visit set
	 * @throws APIException
	 * @since 1.9
	 */
	@Authorized( { PrivilegeConstants.GET_VISITS })
	public List<Encounter> getEncountersByVisitsAndPatient(Patient patient, boolean includeVoided, String query,
	        Integer start, Integer length) throws APIException;
	
	/**
	 * Returns result count for
	 * {@link #getEncountersByVisitsAndPatient(Patient, boolean, String, Integer, Integer)}.
	 * 
	 * @param patient
	 * @param includeVoided
	 * @param query
	 * @return number of results
	 * @throws APIException
	 * @since 1.9
	 */
	@Authorized( { PrivilegeConstants.GET_VISITS })
	public Integer getEncountersByVisitsAndPatientCount(Patient patient, boolean includeVoided, String query)
	        throws APIException;
	
	/**
	 * Filters out all encounters to which given user does not have access. If user is not specified
	 * then implementations should treat authenticated user from context as given user by default
	 * 
	 * @param encounters the list of encounters to be filtered
	 * @param user the user instance to filter "visible" encounters for
	 * @return list, that does not include encounters, which can not be shown to given user due to
	 *         permissions check
	 * @should filter encounters if user is not allowed to see some encounters
	 * @should not filter all encounters when the encounter type's view privilege column is null
	 */
	@Authorized( { PrivilegeConstants.GET_ENCOUNTERS })
	public List<Encounter> filterEncountersByViewPermissions(List<Encounter> encounters, User user);
	
	/**
	 * Determines whether given user is granted to view all encounter types or not
	 * 
	 * @param subject the user whose permission to view all encounter types will be checked
	 * @return true if user has access to view all types of encounters
	 * @should return true if user is granted to view all encounters
	 * @should return true when the encounter type's view privilege column is null
	 */
	public boolean canViewAllEncounterTypes(User subject);
	
	/**
	 * Determines whether given user is granted to edit all encounter types or not
	 * 
	 * @param subject the user whose permission to edit all encounter types will be checked
	 * @return true if user has access to edit all types of encounters
	 * @should return true if user is granted to edit all encounters
	 * @should return true when the encounter type's edit privilege column is null
	 */
	public boolean canEditAllEncounterTypes(User subject);
	
	/**
	 * Checks if passed in user can edit given encounter. If user is not specified, then
	 * authenticated user will be taken by default
	 * 
	 * @param encounter the encounter instance to be checked
	 * @param subject the user, who requests edit access
	 * @return true if user has privilege denoted by <em>editPrivilege</em> given on encounter type
	 * @should return true if user can edit encounter
	 * @should return false if user can not edit encounter
	 * @should fail if encounter is null
	 */
	public boolean canEditEncounter(Encounter encounter, User subject);
	
	/**
	 * Checks if passed in user can view given encounter. If user is not specified, then
	 * authenticated user will be taken by default
	 * 
	 * @param encounter the encounter instance to be checked
	 * @param subject the user, who requests view access
	 * @return true if user has privilege denoted by <em>viewPrivilege</em> given on encounter type
	 * @should return true if user can view encounter
	 * @should return false if user can not view encounter
	 * @should fail if encounter is null
	 */
	public boolean canViewEncounter(Encounter encounter, User subject);
	
	/**
	 * Check if the encounter types are locked, and if so, throw exception during manipulation of
	 * encounter type
	 * 
	 * @throws EncounterTypeLockedException
	 */
	public void checkIfEncounterTypesAreLocked() throws EncounterTypeLockedException;
	
	/**
	 * Get EncounterRoles by name
	 * 
	 * @param name
	 * @return List of EncounterRole objects
	 * @since 1.11
	 * @should find encounter roles based on their name
	 */
	
	@Authorized( { PrivilegeConstants.GET_ENCOUNTER_ROLES })
	public List<EncounterRole> getEncounterRolesByName(String name);
	
	/**
	 *Transfer encounter to another patient
	 *
	 * @param encounter
	 * @param patient
	 * @return transferred encounter
	 * @since 1.12
	 *
	 * @should transfer an encounter with observations but not orders to given patient
	 * @should void given encounter
	 * @should void given encounter visit if given encounter is the only encounter
	 */
	@Authorized( { PrivilegeConstants.EDIT_ENCOUNTERS })
	public Encounter transferEncounter(Encounter encounter, Patient patient);
}
