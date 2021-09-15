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
	 * <strong>Should</strong> save encounter with basic details
	 * <strong>Should</strong> update encounter successfully
	 * <strong>Should</strong> cascade save to contained obs
	 * <strong>Should</strong> cascade patient to orders in the encounter
	 * <strong>Should</strong> cascade save to contained obs when encounter already exists
	 * <strong>Should</strong> cascade encounter datetime to obs
	 * <strong>Should</strong> only cascade the obsdatetimes to obs with different initial obsdatetimes
	 * <strong>Should</strong> not overwrite creator if non null
	 * <strong>Should</strong> not overwrite dateCreated if non null
	 * <strong>Should</strong> not overwrite obs and orders creator or dateCreated
	 * <strong>Should</strong> not assign encounter to visit if no handler is registered
	 * <strong>Should</strong> not assign encounter to visit if the no assign handler is registered
	 * <strong>Should</strong> assign encounter to visit if the assign to existing handler is registered
	 * <strong>Should</strong> assign encounter to visit if the assign to existing or new handler is registered
	 * <strong>Should</strong> cascade save encounter providers
	 * <strong>Should</strong> cascade delete encounter providers
	 * <strong>Should</strong> cascade save encounter conditions
	 * <strong>Should</strong> void and create new obs when saving encounter
	 * <strong>Should</strong> fail if user is not supposed to edit encounters of type of given encounter
	 * <strong>Should</strong> cascade save encounter allergies
	 */
	@Authorized( { PrivilegeConstants.ADD_ENCOUNTERS, PrivilegeConstants.EDIT_ENCOUNTERS })
	public Encounter saveEncounter(Encounter encounter) throws APIException;
	
	/**
	 * Get encounter by internal identifier
	 * 
	 * @param encounterId encounter id
	 * @return encounter with given internal identifier
	 * @throws APIException
	 * <strong>Should</strong> throw error if given null parameter
	 * <strong>Should</strong> fail if user is not allowed to view encounter by given id
	 * <strong>Should</strong> return encounter if user is allowed to view it
	 */
	@Authorized( { PrivilegeConstants.GET_ENCOUNTERS })
	public Encounter getEncounter(Integer encounterId) throws APIException;
	
	/**
	 * Get Encounter by its UUID
	 * 
	 * @param uuid
	 * @return encounter or null
	 * <strong>Should</strong> find object given valid uuid
	 * <strong>Should</strong> return null if no object found with given uuid
	 */
	@Authorized( { PrivilegeConstants.GET_ENCOUNTERS })
	public Encounter getEncounterByUuid(String uuid) throws APIException;
	
	/**
	 * Get all encounters (not voided) for a patient, sorted by encounterDatetime ascending.
	 * 
	 * @param patient
	 * @return List&lt;Encounter&gt; encounters (not voided) for a patient.
	 * <strong>Should</strong> not get voided encounters
	 * <strong>Should</strong> throw error when given null parameter
	 */
	@Authorized( { PrivilegeConstants.GET_ENCOUNTERS })
	public List<Encounter> getEncountersByPatient(Patient patient);
	
	/**
	 * Get encounters for a patientId
	 * 
	 * @param patientId
	 * @return all encounters (not voided) for the given patient identifier
	 * @throws APIException
	 * <strong>Should</strong> not get voided encounters
	 * <strong>Should</strong> throw error if given a null parameter
	 */
	@Authorized( { PrivilegeConstants.GET_ENCOUNTERS })
	public List<Encounter> getEncountersByPatientId(Integer patientId) throws APIException;
	
	/**
	 * Get encounters (not voided) for a patient identifier
	 * 
	 * @param identifier
	 * @return all encounters (not retired) for the given patient identifier
	 * @throws APIException
	 * <strong>Should</strong> not get voided encounters
	 * <strong>Should</strong> throw error if given null parameter
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
	 * <strong>Should</strong> get encounters by location
	 * <strong>Should</strong> get encounters on or after date
	 * <strong>Should</strong> get encounters on or up to a date
	 * <strong>Should</strong> get encounters by form
	 * <strong>Should</strong> get encounters by type
	 * <strong>Should</strong> get encounters by provider
	 * <strong>Should</strong> get encounters by visit type
	 * <strong>Should</strong> get encounters by visit
	 * <strong>Should</strong> exclude voided encounters
	 * <strong>Should</strong> include voided encounters
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
	 * <strong>Should</strong> get encounters modified after specified date
	 */
	@Authorized( { PrivilegeConstants.GET_ENCOUNTERS })
	public List<Encounter> getEncounters(EncounterSearchCriteria encounterSearchCriteria);
	
	/**
	 * Voiding a encounter essentially removes it from circulation
	 * 
	 * @param encounter Encounter object to void
	 * @param reason String reason that it's being voided
	 * <strong>Should</strong> void encounter and set attributes
	 * <strong>Should</strong> cascade to obs
	 * <strong>Should</strong> cascade to orders
	 * <strong>Should</strong> throw error with null reason parameter
	 * <strong>Should</strong> not void providers
	 * <strong>Should</strong> fail if user is not supposed to edit encounters of type of given encounter
	 */
	@Authorized( { PrivilegeConstants.EDIT_ENCOUNTERS })
	public Encounter voidEncounter(Encounter encounter, String reason);
	
	/**
	 * Unvoid encounter record
	 * 
	 * @param encounter Encounter to be revived
	 * <strong>Should</strong> cascade unvoid to obs
	 * <strong>Should</strong> cascade unvoid to orders
	 * <strong>Should</strong> unvoid and unmark all attributes
	 * <strong>Should</strong> fail if user is not supposed to edit encounters of type of given encounter
	 */
	@Authorized( { PrivilegeConstants.EDIT_ENCOUNTERS })
	public Encounter unvoidEncounter(Encounter encounter) throws APIException;
	
	/**
	 * Completely remove an encounter from database. For super users only. If dereferencing
	 * encounters, use <code>voidEncounter(org.openmrs.Encounter)</code>
	 * 
	 * @param encounter encounter object to be purged
	 * <strong>Should</strong> purgeEncounter
	 * <strong>Should</strong> fail if user is not supposed to edit encounters of type of given encounter
	 */
	@Authorized( { PrivilegeConstants.PURGE_ENCOUNTERS })
	public void purgeEncounter(Encounter encounter) throws APIException;
	
	/**
	 * Completely remove an encounter from database. For super users only. If dereferencing
	 * encounters, use <code>voidEncounter(org.openmrs.Encounter)</code>
	 * 
	 * @param encounter encounter object to be purged
	 * @param cascade Purge any related observations as well?
	 * <strong>Should</strong> cascade purge to obs and orders
	 * <strong>Should</strong> fail if user is not supposed to edit encounters of type of given encounter
	 */
	@Authorized( { PrivilegeConstants.PURGE_ENCOUNTERS })
	public void purgeEncounter(Encounter encounter, boolean cascade) throws APIException;
	
	/**
	 * Save a new Encounter Type or update an existing Encounter Type.
	 * 
	 * @param encounterType
	 * <strong>Should</strong> save encounter type
	 * <strong>Should</strong> not overwrite creator
	 * <strong>Should</strong> not overwrite creator or date created
	 * <strong>Should</strong> not overwrite date created
	 * <strong>Should</strong> update an existing encounter type name
	 * <strong>Should</strong> throw error when trying to save encounter type when encounter types are locked
	 */
	@Authorized( { PrivilegeConstants.MANAGE_ENCOUNTER_TYPES })
	public EncounterType saveEncounterType(EncounterType encounterType) throws APIException;
	
	/**
	 * Get encounterType by internal identifier
	 * 
	 * @param encounterTypeId Integer
	 * @return encounterType with given internal identifier
	 * @throws APIException
	 * <strong>Should</strong> throw error if given null parameter
	 */
	@Authorized( { PrivilegeConstants.GET_ENCOUNTER_TYPES })
	public EncounterType getEncounterType(Integer encounterTypeId) throws APIException;
	
	/**
	 * Get EncounterType by its UUID
	 * 
	 * @param uuid
	 * @return encounter type or null
	 * <strong>Should</strong> find object given valid uuid
	 * <strong>Should</strong> return null if no object found with given uuid
	 */
	@Authorized( { PrivilegeConstants.GET_ENCOUNTER_TYPES })
	public EncounterType getEncounterTypeByUuid(String uuid) throws APIException;
	
	/**
	 * Get encounterType by exact name
	 * 
	 * @param name string to match to an Encounter.name
	 * @return EncounterType that is not retired
	 * @throws APIException
	 * <strong>Should</strong> not get retired types
	 * <strong>Should</strong> return null if only retired type found
	 * <strong>Should</strong> not get by inexact name
	 * <strong>Should</strong> return null with null name parameter
	 */
	@Authorized( { PrivilegeConstants.GET_ENCOUNTER_TYPES })
	public EncounterType getEncounterType(String name) throws APIException;
	
	/**
	 * Get all encounter types (including retired)
	 * 
	 * @return encounter types list
	 * @throws APIException
	 * <strong>Should</strong> not return retired types
	 */
	@Authorized( { PrivilegeConstants.GET_ENCOUNTER_TYPES })
	public List<EncounterType> getAllEncounterTypes() throws APIException;
	
	/**
	 * Get all encounter types. If includeRetired is true, also get retired encounter types.
	 * 
	 * @param includeRetired
	 * @return encounter types list
	 * @throws APIException
	 * <strong>Should</strong> not return retired types
	 * <strong>Should</strong> include retired types with true includeRetired parameter
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
	 * <strong>Should</strong> return types by partial name match
	 * <strong>Should</strong> return types by partial case insensitive match
	 * <strong>Should</strong> include retired types in the results
	 * <strong>Should</strong> not partial match name on internal substrings
	 * <strong>Should</strong> return types ordered on name and nonretired first
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
	 * <strong>Should</strong> retire type and set attributes
	 * <strong>Should</strong> throw error if given null reason parameter
	 * <strong>Should</strong> should throw error when trying to retire encounter type when encounter types are
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
	 * <strong>Should</strong> unretire type and unmark attributes
	 * <strong>Should</strong> should throw error when trying to unretire encounter type when encounter types are
	 *         locked
	 */
	@Authorized( { PrivilegeConstants.MANAGE_ENCOUNTER_TYPES })
	public EncounterType unretireEncounterType(EncounterType encounterType) throws APIException;
	
	/**
	 * Completely remove an encounter type from database.
	 * 
	 * @param encounterType
	 * @throws APIException
	 * <strong>Should</strong> purge type
	 * <strong>Should</strong> should throw error when trying to delete encounter type when encounter types are
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
	 * <strong>Should</strong> get all unvoided encounters for the given patient name
	 * <strong>Should</strong> get all unvoided encounters for the given patient identifier
	 * <strong>Should</strong> throw error if given null parameter
	 * <strong>Should</strong> include voided encounters in the returned list if includedVoided is true
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
	 * <strong>Should</strong> get all the unique encounters that match the specified parameter values
	 * <strong>Should</strong> not return voided encounters if includeVoided is set to true
	 * <strong>Should</strong> return empty list for empty query
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
	 * <strong>Should</strong> fetch encounters by patient id
	 * <strong>Should</strong> include voided encounters if includeVoided is set to true
	 * <strong>Should</strong> should match on provider identifier
	 * <strong>Should</strong> match on the provider name
	 * <strong>Should</strong> match on the location name
	 * <strong>Should</strong> match on the provider person name
	 * <strong>Should</strong> match on the encounter type name
	 * <strong>Should</strong> match on the form name
	 */
	@Authorized( { PrivilegeConstants.GET_ENCOUNTERS })
	public List<Encounter> getEncounters(String query, Integer patientId, Integer start, Integer length,
	        boolean includeVoided) throws APIException;
	
	/**
	 * Get all encounters for a cohort of patients
	 * 
	 * @param patients Cohort of patients to search
	 * @return Map of all encounters for specified patients.
	 * <strong>Should</strong> get all encounters for a cohort of patients
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
	 * <strong>Should</strong> get the correct count of unique encounters
	 */
	@Authorized( { PrivilegeConstants.GET_ENCOUNTERS })
	public Integer getCountOfEncounters(String query, boolean includeVoided);
	
	/**
	 * Gets all encounters grouped within a given visit.
	 * 
	 * @param visit the visit.
	 * @param includeVoided whether voided encounters should be returned
	 * @return list of encounters in the given visit.
	 * <strong>Should</strong> get active encounters by visit
	 * <strong>Should</strong> include voided encounters when includeVoided is true
	 * @since 1.9
	 */
	@Authorized( { PrivilegeConstants.GET_ENCOUNTERS })
	List<Encounter> getEncountersByVisit(Visit visit, boolean includeVoided);
	
	/**
	 * @return list of handlers for determining if an encounter should go into a visit. If none are
	 *         found, an empty list.
	 * @see EncounterVisitHandler
	 * @since 1.9
	 * <strong>Should</strong> return the no assignment handler
	 * <strong>Should</strong> return the existing visit only assignment handler
	 * <strong>Should</strong> return the existing or new visit assignment handler
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
	 * <strong>Should</strong> save encounter role with basic details
	 * <strong>Should</strong> update encounter role successfully
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
	 * <strong>Should</strong> purge Encounter Role
	 */
	@Authorized( { PrivilegeConstants.PURGE_ENCOUNTER_ROLES })
	public void purgeEncounterRole(EncounterRole encounterRole) throws APIException;
	
	/**
	 * Get all encounter roles based on includeRetired flag
	 * 
	 * @param includeRetired
	 * @return List of all encounter roles
	 * @since 1.9
	 * <strong>Should</strong> get all encounter roles based on include retired flag.
	 */
	@Authorized( { PrivilegeConstants.GET_ENCOUNTER_ROLES })
	public List<EncounterRole> getAllEncounterRoles(boolean includeRetired);
	
	/**
	 * Get EncounterRole by its UUID
	 * 
	 * @param uuid
	 * @return EncounterRole
	 * @since 1.9
	 * <strong>Should</strong> find encounter role based on uuid
	 */
	@Authorized( { PrivilegeConstants.GET_ENCOUNTER_ROLES })
	public EncounterRole getEncounterRoleByUuid(String uuid) throws APIException;
	
	/**
	 * Get EncounterRole by name
	 * 
	 * @param name
	 * @return EncounterRole object by name
	 * @since 1.10
	 * <strong>Should</strong> find an encounter role identified by its name
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
	 * <strong>Should</strong> retire type and set attributes
	 * <strong>Should</strong> throw error if given null reason parameter
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
	 * <strong>Should</strong> unretire type and unmark attributes
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
	 * <strong>Should</strong> return the unvoided encounters not assigned to any visit
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
	 * <strong>Should</strong> filter encounters if user is not allowed to see some encounters
	 * <strong>Should</strong> not filter all encounters when the encounter type's view privilege column is null
	 */
	@Authorized( { PrivilegeConstants.GET_ENCOUNTERS })
	public List<Encounter> filterEncountersByViewPermissions(List<Encounter> encounters, User user);
	
	/**
	 * Determines whether given user is granted to view all encounter types or not
	 * 
	 * @param subject the user whose permission to view all encounter types will be checked
	 * @return true if user has access to view all types of encounters
	 * <strong>Should</strong> return true if user is granted to view all encounters
	 * <strong>Should</strong> return true when the encounter type's view privilege column is null
	 */
	public boolean canViewAllEncounterTypes(User subject);
	
	/**
	 * Determines whether given user is granted to edit all encounter types or not
	 * 
	 * @param subject the user whose permission to edit all encounter types will be checked
	 * @return true if user has access to edit all types of encounters
	 * <strong>Should</strong> return true if user is granted to edit all encounters
	 * <strong>Should</strong> return true when the encounter type's edit privilege column is null
	 */
	public boolean canEditAllEncounterTypes(User subject);
	
	/**
	 * Checks if passed in user can edit given encounter. If user is not specified, then
	 * authenticated user will be taken by default
	 * 
	 * @param encounter the encounter instance to be checked
	 * @param subject the user, who requests edit access
	 * @return true if user has privilege denoted by <em>editPrivilege</em> given on encounter type
	 * <strong>Should</strong> return true if user can edit encounter
	 * <strong>Should</strong> return false if user can not edit encounter
	 * <strong>Should</strong> fail if encounter is null
	 */
	public boolean canEditEncounter(Encounter encounter, User subject);
	
	/**
	 * Checks if passed in user can view given encounter. If user is not specified, then
	 * authenticated user will be taken by default
	 * 
	 * @param encounter the encounter instance to be checked
	 * @param subject the user, who requests view access
	 * @return true if user has privilege denoted by <em>viewPrivilege</em> given on encounter type
	 * <strong>Should</strong> return true if user can view encounter
	 * <strong>Should</strong> return false if user can not view encounter
	 * <strong>Should</strong> fail if encounter is null
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
	 * <strong>Should</strong> find encounter roles based on their name
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
	 * <strong>Should</strong> transfer an encounter with observations but not orders to given patient
	 * <strong>Should</strong> void given encounter
	 * <strong>Should</strong> void given encounter visit if given encounter is the only encounter
	 */
	@Authorized( { PrivilegeConstants.EDIT_ENCOUNTERS })
	public Encounter transferEncounter(Encounter encounter, Patient patient);
}
