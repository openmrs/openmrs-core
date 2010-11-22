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

import org.openmrs.Cohort;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.db.EncounterDAO;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.transaction.annotation.Transactional;

/**
 * Services for Encounters and Encounter Types
 * 
 * @version 1.0
 */
@Transactional
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
	 * @should cascade creator and dateCreated to orders
	 */
	@Authorized({ PrivilegeConstants.ADD_ENCOUNTERS, PrivilegeConstants.EDIT_ENCOUNTERS })
	public Encounter saveEncounter(Encounter encounter) throws APIException;
	
	/**
	 * Get encounter by internal identifier
	 * 
	 * @param encounterId encounter id
	 * @return encounter with given internal identifier
	 * @throws APIException
	 * @should throw error if given null parameter
	 */
	@Transactional(readOnly = true)
	@Authorized({ PrivilegeConstants.VIEW_ENCOUNTERS })
	public Encounter getEncounter(Integer encounterId) throws APIException;
	
	/**
	 * Get Encounter by its UUID
	 * 
	 * @param uuid
	 * @return
	 * @should find object given valid uuid
	 * @should return null if no object found with given uuid
	 */
	@Transactional(readOnly = true)
	@Authorized({ PrivilegeConstants.VIEW_ENCOUNTERS })
	public Encounter getEncounterByUuid(String uuid) throws APIException;
	
	/**
	 * Get all encounters (not voided) for a patient, sorted by encounterDatetime ascending.
	 * 
	 * @param patient
	 * @return List<Encounter> encounters (not voided) for a patient.
	 * @should not get voided encounters
	 * @should throw error when given null parameter
	 */
	@Transactional(readOnly = true)
	@Authorized({ PrivilegeConstants.VIEW_ENCOUNTERS })
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
	@Transactional(readOnly = true)
	@Authorized({ PrivilegeConstants.VIEW_ENCOUNTERS })
	public List<Encounter> getEncountersByPatientId(Integer patientId) throws APIException;
	
	/**
	 * Get encounters (not voided) for a patient identifier
	 * 
	 * @param identifier
	 * @return all encounters (not retired) for the given patient identifer
	 * @throws APIException
	 * @should not get voided encounters
	 * @should throw error if given null parameter
	 */
	@Transactional(readOnly = true)
	@Authorized({ PrivilegeConstants.VIEW_ENCOUNTERS })
	public List<Encounter> getEncountersByPatientIdentifier(String identifier) throws APIException;
	
	/**
	 * @deprecated replaced by
	 *             {@link #getEncounters(Patient, Location, Date, Date, Collection, Collection, Collection, boolean)}
	 */
	@Deprecated
	@Transactional(readOnly = true)
	@Authorized({ PrivilegeConstants.VIEW_ENCOUNTERS })
	public List<Encounter> getEncounters(Patient who, Location loc, Date fromDate, Date toDate,
	                                     Collection<Form> enteredViaForms, Collection<EncounterType> encounterTypes,
	                                     boolean includeVoided);
	
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
	 * @param includeVoided true/false to include the voided encounters or not
	 * @return a list of encounters ordered by increasing encounterDatetime
	 * @since 1.5
	 * @should get encounters by location
	 * @should get encounters on or after date
	 * @should get encounters on or up to a date
	 * @should get encounters by form
	 * @should get encounters by type
	 * @should get encounters by provider
	 * @should exclude voided encounters
	 * @should include voided encounters
	 */
	@Transactional(readOnly = true)
	@Authorized({ PrivilegeConstants.VIEW_ENCOUNTERS })
	public List<Encounter> getEncounters(Patient who, Location loc, Date fromDate, Date toDate,
	                                     Collection<Form> enteredViaForms, Collection<EncounterType> encounterTypes,
	                                     Collection<User> providers, boolean includeVoided);
	
	/**
	 * Voiding a encounter essentially removes it from circulation
	 * 
	 * @param encounter Encounter object to void
	 * @param reason String reason that it's being voided
	 * @should void encounter and set attributes
	 * @should cascade to obs
	 * @should cascade to orders
	 * @should throw error with null reason parameter
	 */
	@Authorized({ PrivilegeConstants.EDIT_ENCOUNTERS })
	public Encounter voidEncounter(Encounter encounter, String reason);
	
	/**
	 * Unvoid encounter record
	 * 
	 * @param encounter Encounter to be revived
	 * @should cascade unvoid to obs
	 * @should cascade unvoid to orders
	 * @should unvoid and unmark all attributes
	 */
	@Authorized({ PrivilegeConstants.EDIT_ENCOUNTERS })
	public Encounter unvoidEncounter(Encounter encounter) throws APIException;
	
	/**
	 * Completely remove an encounter from database. For super users only. If dereferencing
	 * encounters, use <code>voidEncounter(org.openmrs.Encounter)</code>
	 * 
	 * @param encounter encounter object to be purged
	 * @should purgeEncounter
	 */
	@Authorized({ PrivilegeConstants.PURGE_ENCOUNTERS })
	public void purgeEncounter(Encounter encounter) throws APIException;
	
	/**
	 * Completely remove an encounter from database. For super users only. If dereferencing
	 * encounters, use <code>voidEncounter(org.openmrs.Encounter)</code>
	 * 
	 * @param encounter encounter object to be purged
	 * @param cascade Purge any related observations as well?
	 * @should cascade purge to obs and orders
	 */
	@Authorized({ PrivilegeConstants.PURGE_ENCOUNTERS })
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
	 */
	@Authorized({ PrivilegeConstants.MANAGE_ENCOUNTER_TYPES })
	public EncounterType saveEncounterType(EncounterType encounterType);
	
	/**
	 * Get encounterType by internal identifier
	 * 
	 * @param encounterTypeId Integer
	 * @return encounterType with given internal identifier
	 * @throws APIException
	 * @should throw error if given null parameter
	 */
	@Transactional(readOnly = true)
	@Authorized({ PrivilegeConstants.VIEW_ENCOUNTER_TYPES })
	public EncounterType getEncounterType(Integer encounterTypeId) throws APIException;
	
	/**
	 * Get EncounterType by its UUID
	 * 
	 * @param uuid
	 * @return
	 * @should find object given valid uuid
	 * @should return null if no object found with given uuid
	 */
	@Transactional(readOnly = true)
	@Authorized({ PrivilegeConstants.VIEW_ENCOUNTER_TYPES })
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
	@Transactional(readOnly = true)
	@Authorized({ PrivilegeConstants.VIEW_ENCOUNTER_TYPES })
	public EncounterType getEncounterType(String name) throws APIException;
	
	/**
	 * Get all encounter types (including retired)
	 * 
	 * @return encounter types list
	 * @throws APIException
	 * @should not return retired types
	 */
	@Transactional(readOnly = true)
	@Authorized({ PrivilegeConstants.VIEW_ENCOUNTER_TYPES })
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
	@Transactional(readOnly = true)
	@Authorized({ PrivilegeConstants.VIEW_ENCOUNTER_TYPES })
	public List<EncounterType> getAllEncounterTypes(boolean includeRetired) throws APIException;
	
	/**
	 * Find Encounter Types with name matching the beginning of the search string. Search strings
	 * are case insensitive so that "NaMe".equals("name") is true. Includes retired EncounterTypes.
	 * 
	 * @param name of the encounter type to find
	 * @return List<EncounterType> matching encounters
	 * @throws APIException
	 * @should return types by partial name match
	 * @should return types by partial case insensitive match
	 * @should include retired types in the results
	 * @should not partial match name on internal substrings
	 * @should return types ordered on name and nonretired first
	 */
	@Transactional(readOnly = true)
	@Authorized({ PrivilegeConstants.VIEW_ENCOUNTER_TYPES })
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
	 */
	@Authorized({ PrivilegeConstants.MANAGE_ENCOUNTER_TYPES })
	public EncounterType retireEncounterType(EncounterType encounterType, String reason) throws APIException;
	
	/**
	 * Unretire an EncounterType. This brings back the given encounter type and says that it can be
	 * used again
	 * 
	 * @param encounterType the encounter type to unretire
	 * @throws APIException
	 * @should unretire type and unmark attributes
	 */
	@Authorized({ PrivilegeConstants.MANAGE_ENCOUNTER_TYPES })
	public EncounterType unretireEncounterType(EncounterType encounterType) throws APIException;
	
	/**
	 * Completely remove an encounter type from database.
	 * 
	 * @param encounterType
	 * @throws APIException
	 * @should purge type
	 */
	@Authorized({ PrivilegeConstants.PURGE_ENCOUNTER_TYPES })
	public void purgeEncounterType(EncounterType encounterType) throws APIException;
	
	/**
	 * Creates a new encounter
	 * 
	 * @param encounter to be created
	 * @throws APIException
	 * @deprecated replaced by {@link #saveEncounter(Encounter)}
	 */
	@Deprecated
	@Authorized({ PrivilegeConstants.ADD_ENCOUNTERS })
	public void createEncounter(Encounter encounter) throws APIException;
	
	/**
	 * Save changes to encounter. Automatically applys encounter.patient to all
	 * encounter.obs.patient
	 * 
	 * @param encounter
	 * @throws APIException
	 * @deprecated replaced by {@link #saveEncounter(Encounter)}
	 */
	@Deprecated
	@Authorized({ PrivilegeConstants.EDIT_ENCOUNTERS })
	public void updateEncounter(Encounter encounter) throws APIException;
	
	/**
	 * Delete encounter from database. For super users only. If dereferencing encounters, use
	 * <code>voidEncounter(org.openmrs.Encounter)</code>
	 * 
	 * @param encounter encounter object to be deleted
	 * @deprecated replaced by {@link #purgeEncounter(Encounter)}
	 */
	@Deprecated
	@Authorized({ PrivilegeConstants.DELETE_ENCOUNTERS })
	public void deleteEncounter(Encounter encounter) throws APIException;
	
	/**
	 * Get encounters for a patientId (not voided). To include voided Encounters use
	 * {@link #getEncounters(Patient, Location, Date, Date, Collection, Collection, boolean)}
	 * 
	 * @param patientId
	 * @param includeVoided No longer supported
	 * @return all encounters for the given patient identifer
	 * @throws APIException
	 * @deprecated replaced by {@link #getEncountersByPatientId(Integer)}
	 */
	@Deprecated
	@Transactional(readOnly = true)
	@Authorized({ PrivilegeConstants.VIEW_ENCOUNTERS })
	public List<Encounter> getEncountersByPatientId(Integer patientId, boolean includeVoided) throws APIException;
	
	/**
	 * Get encounters (not voided) for a patient identifier. To include voided encounters use
	 * {@link #getEncounters(Patient, Location, Date, Date, Collection, Collection, boolean)}
	 * 
	 * @param identifier
	 * @param includeVoided No longer supported.
	 * @return all encounters for the given patient identifer
	 * @throws APIException
	 * @deprecated replaced by {@link #getEncountersByPatientIdentifier(String)}
	 */
	@Deprecated
	@Transactional(readOnly = true)
	@Authorized({ PrivilegeConstants.VIEW_ENCOUNTERS })
	public List<Encounter> getEncountersByPatientIdentifier(String identifier, boolean includeVoided) throws APIException;
	
	/**
	 * Get all encounters (not voided) for a patient
	 * 
	 * @param who
	 * @return List<Encounter> encounters (not voided) for a patient
	 * @deprecated replaced by {@link #getEncountersByPatient(Patient)}
	 */
	@Deprecated
	@Transactional(readOnly = true)
	@Authorized({ PrivilegeConstants.VIEW_ENCOUNTERS })
	public List<Encounter> getEncounters(Patient who);
	
	/**
	 * Get all encounters (not voided) for a patient. To include voided encounters, use
	 * {@link #getEncounters(Patient, Location, Date, Date, Collection, Collection, boolean)}
	 * 
	 * @param who
	 * @param includeVoided No longer supported.
	 * @return List<Encounter> object of non-voided Encounters
	 * @deprecated replaced by {@link #getEncountersByPatient(Patient)}
	 */
	@Deprecated
	@Transactional(readOnly = true)
	@Authorized({ PrivilegeConstants.VIEW_ENCOUNTERS })
	public List<Encounter> getEncounters(Patient who, boolean includeVoided);
	
	/**
	 * Get all encounters for a patient that took place at a specific location
	 * 
	 * @param who
	 * @param where
	 * @return List<Encounter> object of all encounters with this patient in specified location
	 * @deprecated use
	 *             {@link #getEncounters(Patient, Location, Date, Date, Collection, Collection, boolean)}
	 */
	@Deprecated
	@Transactional(readOnly = true)
	@Authorized({ PrivilegeConstants.VIEW_ENCOUNTERS })
	public List<Encounter> getEncounters(Patient who, Location where);
	
	/**
	 * Get all encounters for a patient that took place between fromDate and toDate (both nullable
	 * and inclusive)
	 * 
	 * @param who
	 * @param fromDate
	 * @param toDate
	 * @return List<Encounter> object of all encounters with this patient in specified date range
	 * @deprecated use
	 *             {@link #getEncounters(Patient, Location, Date, Date, Collection, Collection, boolean)}
	 */
	@Deprecated
	@Transactional(readOnly = true)
	@Authorized({ PrivilegeConstants.VIEW_ENCOUNTERS })
	public List<Encounter> getEncounters(Patient who, Date fromDate, Date toDate);
	
	/**
	 * Get all encounters that took place between fromDate and toDate (both nullable and inclusive)
	 * 
	 * @param fromDate
	 * @param toDate
	 * @return Encounters from specified date range
	 * @deprecated use
	 *             {@link #getEncounters(Patient, Location, Date, Date, Collection, Collection, boolean)}
	 */
	@Deprecated
	@Transactional(readOnly = true)
	@Authorized({ PrivilegeConstants.VIEW_ENCOUNTERS })
	public Collection<Encounter> getEncounters(Date fromDate, Date toDate);
	
	/**
	 * Get all encounters that took place between fromDate and toDate (both nullable and inclusive)
	 * at the given location
	 * 
	 * @param loc Location
	 * @param fromDate
	 * @param toDate
	 * @return Encounters from specified location and date range
	 * @deprecated use
	 *             {@link #getEncounters(Patient, Location, Date, Date, Collection, Collection, boolean)}
	 */
	@Deprecated
	@Transactional(readOnly = true)
	@Authorized({ PrivilegeConstants.VIEW_ENCOUNTERS })
	public Collection<Encounter> getEncounters(Location loc, Date fromDate, Date toDate);
	
	/**
	 * Get all encounter types (not retired)
	 * 
	 * @return A List<EncounterType> object of all non-retired EncounterTypes
	 * @throws APIException
	 * @deprecated replaced by {@link #getAllEncounterTypes()}
	 */
	@Deprecated
	@Transactional(readOnly = true)
	@Authorized({ PrivilegeConstants.VIEW_ENCOUNTER_TYPES })
	public List<EncounterType> getEncounterTypes() throws APIException;
	
	/**
	 * Get all locations
	 * 
	 * @return location list
	 * @throws APIException
	 * @deprecated replaced by {@link LocationService#getAllLocations()}
	 */
	@Deprecated
	@Transactional(readOnly = true)
	@Authorized({ PrivilegeConstants.VIEW_LOCATIONS })
	public List<Location> getLocations() throws APIException;
	
	/**
	 * Get location by internal identifier
	 * 
	 * @param locationId
	 * @return location with given internal identifier
	 * @throws APIException
	 * @deprecated replaced by {@link org.openmrs.api.LocationService#getLocation(Integer)}
	 */
	@Deprecated
	@Transactional(readOnly = true)
	@Authorized({ PrivilegeConstants.VIEW_LOCATIONS })
	public Location getLocation(Integer locationId) throws APIException;
	
	/**
	 * Get location by name
	 * 
	 * @param name location's name
	 * @return location with given name
	 * @throws APIException
	 * @deprecated replaced by {@link org.openmrs.api.LocationService#getLocation(String)}
	 */
	@Deprecated
	@Transactional(readOnly = true)
	@Authorized({ PrivilegeConstants.VIEW_LOCATIONS })
	public Location getLocationByName(String name) throws APIException;
	
	/**
	 * Search for locations by name. Matches returned match the given string at the beginning of the
	 * name
	 * 
	 * @param name location's name
	 * @return list of locations with similar name
	 * @throws APIException
	 * @deprecated replaced by {@link org.openmrs.api.LocationService#getLocations(String)}
	 */
	@Deprecated
	@Transactional(readOnly = true)
	@Authorized({ PrivilegeConstants.VIEW_LOCATIONS })
	public List<Location> findLocations(String name) throws APIException;
	
	/**
	 * Search for encounters by patient name or patient identifier.
	 * 
	 * @param query patient name or identifier
	 * @return list of encounters for the given patient
	 * @throws APIException
	 * @see {@link EncounterService}{@link #getEncountersByPatient(String, boolean)}
	 * @since 1.7
	 */
	@Authorized({ PrivilegeConstants.VIEW_ENCOUNTERS })
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
	@Authorized({ PrivilegeConstants.VIEW_ENCOUNTERS })
	public List<Encounter> getEncountersByPatient(String query, boolean includeVoided) throws APIException;
	
	/**
	 * Search for encounters by patient name or patient identifier and returns a specific number of
	 * them from the specified starting position.
	 * 
	 * @param query patient name or identifier
	 * @param start beginning index for the batch
	 * @param length number of encounters to return in the batch
	 * @param includeVoided Specifies whether voided encounters should be included
	 * @return list of encounters for the given patient based on batch settings
	 * @throws APIException
	 * @since 1.8
	 */
	@Authorized({ PrivilegeConstants.VIEW_ENCOUNTERS })
	public List<Encounter> getEncounters(String query, Integer start, Integer length, boolean includeVoided)
	                                                                                                        throws APIException;
	
	/**
	 * Get all encounters for a cohort of patients
	 * 
	 * @param patients Cohort of patients to search
	 * @return Map of all encounters for specified patients.
	 * @should get all encounters for a cohort of patients
	 * @since 1.8
	 */
	@Transactional(readOnly = true)
	public Map<Integer, List<Encounter>> getAllEncounters(Cohort patients);
	
	/**
	 * Return the number of encounters matching a patient name or patient identifier
	 * 
	 * @param query patient name or identifier
	 * @param includeVoided Specifies whether voided encounters should be included
	 * @return the number of encounters matching the given search phrase
	 * @since 1.8
	 */
	public Integer getCountOfEncounters(String query, boolean includeVoided);
	
}
