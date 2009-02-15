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

import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.db.EncounterDAO;
import org.openmrs.util.OpenmrsConstants;
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
	@Authorized( { OpenmrsConstants.PRIV_ADD_ENCOUNTERS, OpenmrsConstants.PRIV_EDIT_ENCOUNTERS })
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
	@Authorized( { OpenmrsConstants.PRIV_VIEW_ENCOUNTERS })
	public Encounter getEncounter(Integer encounterId) throws APIException;
	
	/**
	 * Get all encounters (not voided) for a patient.
	 * 
	 * @param patient
	 * @return List<Encounter> encounters (not voided) for a patient.
	 * @should not get voided encounters
	 * @should throw error when given null parameter
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_ENCOUNTERS })
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
	@Authorized( { OpenmrsConstants.PRIV_VIEW_ENCOUNTERS })
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
	@Authorized( { OpenmrsConstants.PRIV_VIEW_ENCOUNTERS })
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
	 * @param includeVoided true/false to include the voided encounters or not
	 * @return a list of encounters ordered by increasing encounterDatetime
	 * @should get encounters by location
	 * @should get encounters on or after date
	 * @should get encounters on or up to a date
	 * @should get encounters by form
	 * @should get encounters by type
	 * @should exclude voided encounters
	 * @should include voided encounters
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_ENCOUNTERS })
	public List<Encounter> getEncounters(Patient who, Location loc, Date fromDate, Date toDate,
	                                     Collection<Form> enteredViaForms, Collection<EncounterType> encounterTypes,
	                                     boolean includeVoided);
	
	/**
	 * Voiding a encounter essentially removes it from circulation
	 * 
	 * @param Encounter encounter
	 * @param String reason
	 * @should void encounter and set attributes
	 * @should cascade to obs
	 * @should cascade to orders
	 * @should throw error with null reason parameter
	 */
	@Authorized( { OpenmrsConstants.PRIV_EDIT_ENCOUNTERS })
	public Encounter voidEncounter(Encounter encounter, String reason);
	
	/**
	 * Unvoid encounter record
	 * 
	 * @param encounter encounter to be revived
	 * @should cascade unvoid to obs
	 * @should cascade unvoid to orders
	 * @should unvoid and unmark all attributes
	 */
	@Authorized( { OpenmrsConstants.PRIV_EDIT_ENCOUNTERS })
	public Encounter unvoidEncounter(Encounter encounter) throws APIException;
	
	/**
	 * Completely remove an encounter from database. For super users only. If dereferencing
	 * encounters, use <code>voidEncounter(org.openmrs.Encounter)</code>
	 * 
	 * @param encounter encounter object to be purged
	 * @should purgeEncounter
	 */
	@Authorized( { OpenmrsConstants.PRIV_PURGE_ENCOUNTERS })
	public void purgeEncounter(Encounter encounter) throws APIException;
	
	/**
	 * Completely remove an encounter from database. For super users only. If dereferencing
	 * encounters, use <code>voidEncounter(org.openmrs.Encounter)</code>
	 * 
	 * @param encounter encounter object to be purged
	 * @param cascade also purge observations
	 * @should cascade purge to obs and orders
	 */
	@Authorized( { OpenmrsConstants.PRIV_PURGE_ENCOUNTERS })
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
	@Authorized( { OpenmrsConstants.PRIV_MANAGE_ENCOUNTER_TYPES })
	public EncounterType saveEncounterType(EncounterType encounterType);
	
	/**
	 * Get encounterType by internal identifier
	 * 
	 * @param encounterType id
	 * @return encounterType with given internal identifier
	 * @throws APIException
	 * @should throw error if given null parameter
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_ENCOUNTER_TYPES })
	public EncounterType getEncounterType(Integer encounterTypeId) throws APIException;
	
	/**
	 * Get encounterType by exact name
	 * 
	 * @param encounterType string to match to an Encounter.name
	 * @return EncounterType that is not retired
	 * @throws APIException
	 * @should not get retired types
	 * @should return null if only retired type found
	 * @should not get by inexact name
	 * @should return null with null name parameter
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_ENCOUNTER_TYPES })
	public EncounterType getEncounterType(String name) throws APIException;
	
	/**
	 * Get all encounter types (not retired)
	 * 
	 * @return encounter types list
	 * @throws APIException
	 * @should not return retired types
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_ENCOUNTER_TYPES })
	public List<EncounterType> getAllEncounterTypes() throws APIException;
	
	/**
	 * Get all encounter types. If includeRetired is true, also get retired encounter types.
	 * 
	 * @param includeRetired
	 * @return encounter types list
	 * @throws APIException
	 * @should non return retired types
	 * @should include retired types with true includeRetired parameter
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_ENCOUNTER_TYPES })
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
	@Authorized( { OpenmrsConstants.PRIV_VIEW_ENCOUNTER_TYPES })
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
	@Authorized( { OpenmrsConstants.PRIV_MANAGE_ENCOUNTER_TYPES })
	public EncounterType retireEncounterType(EncounterType encounterType, String reason) throws APIException;
	
	/**
	 * Unretire an EncounterType. This brings back the given encounter type and says that it can be
	 * used again
	 * 
	 * @param encounterType the encounter type to unretire
	 * @throws APIException
	 * @should unretire type and unmark attributes
	 */
	@Authorized( { OpenmrsConstants.PRIV_MANAGE_ENCOUNTER_TYPES })
	public EncounterType unretireEncounterType(EncounterType encounterType) throws APIException;
	
	/**
	 * Completely remove an encounter type from database.
	 * 
	 * @param encounterType
	 * @throws APIException
	 * @should purge type
	 */
	@Authorized( { OpenmrsConstants.PRIV_PURGE_ENCOUNTER_TYPES })
	public void purgeEncounterType(EncounterType encounterType) throws APIException;
	
	/**
	 * Creates a new encounter
	 * 
	 * @param encounter to be created
	 * @throws APIException
	 * @deprecated replaced by {@link #saveEncounter(Encounter)}
	 */
	@Authorized( { OpenmrsConstants.PRIV_ADD_ENCOUNTERS })
	public void createEncounter(Encounter encounter) throws APIException;
	
	/**
	 * Save changes to encounter. Automatically applys encounter.patient to all
	 * encounter.obs.patient
	 * 
	 * @param encounter
	 * @throws APIException
	 * @deprecated replaced by {@link #saveEncounter(Encounter)}
	 */
	@Authorized( { OpenmrsConstants.PRIV_EDIT_ENCOUNTERS })
	public void updateEncounter(Encounter encounter) throws APIException;
	
	/**
	 * Delete encounter from database. For super users only. If dereferencing encounters, use
	 * <code>voidEncounter(org.openmrs.Encounter)</code>
	 * 
	 * @param encounter encounter object to be deleted
	 * @deprecated replaced by {@link #purgeEncounter(encounter)}
	 */
	@Authorized( { OpenmrsConstants.PRIV_DELETE_ENCOUNTERS })
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
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_ENCOUNTERS })
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
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_ENCOUNTERS })
	public List<Encounter> getEncountersByPatientIdentifier(String identifier, boolean includeVoided) throws APIException;
	
	/**
	 * Get all encounters (not voided) for a patient
	 * 
	 * @param who
	 * @return List<Encounter> encounters (not voided) for a patient
	 * @deprecated replaced by {@link #getEncountersByPatient(Patient)}
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_ENCOUNTERS })
	public List<Encounter> getEncounters(Patient who);
	
	/**
	 * Get all encounters (not voided) for a patient. To include voided encounters, use
	 * {@link #getEncounters(Patient, Location, Date, Date, Collection, Collection, boolean)}
	 * 
	 * @param who
	 * @param includeVoided No longer supported.
	 * @return
	 * @deprecated replaced by {@link #getEncountersByPatient(Patient)}
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_ENCOUNTERS })
	public List<Encounter> getEncounters(Patient who, boolean includeVoided);
	
	/**
	 * Get all encounters for a patient that took place at a specific location
	 * 
	 * @param who
	 * @param where
	 * @return
	 * @deprecated use
	 *             {@link #getEncounters(Patient, Location, Date, Date, Collection, Collection, boolean)}
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_ENCOUNTERS })
	public List<Encounter> getEncounters(Patient who, Location where);
	
	/**
	 * Get all encounters for a patient that took place between fromDate and toDate (both nullable
	 * and inclusive)
	 * 
	 * @param who
	 * @param fromDate
	 * @param toDate
	 * @return
	 * @deprecated use
	 *             {@link #getEncounters(Patient, Location, Date, Date, Collection, Collection, boolean)}
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_ENCOUNTERS })
	public List<Encounter> getEncounters(Patient who, Date fromDate, Date toDate);
	
	/**
	 * Get all encounters that took place between fromDate and toDate (both nullable and inclusive)
	 * 
	 * @param fromDate
	 * @param toDate
	 * @return
	 * @deprecated use
	 *             {@link #getEncounters(Patient, Location, Date, Date, Collection, Collection, boolean)}
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_ENCOUNTERS })
	public Collection<Encounter> getEncounters(Date fromDate, Date toDate);
	
	/**
	 * Get all encounters that took place between fromDate and toDate (both nullable and inclusive)
	 * at the given location
	 * 
	 * @param loc Location
	 * @param fromDate
	 * @param toDate
	 * @return
	 * @deprecated use
	 *             {@link #getEncounters(Patient, Location, Date, Date, Collection, Collection, boolean)}
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_ENCOUNTERS })
	public Collection<Encounter> getEncounters(Location loc, Date fromDate, Date toDate);
	
	/**
	 * Get all encounter types (not retired)
	 * 
	 * @return
	 * @throws APIException
	 * @deprecated replaced by {@link #getAllEncounterTypes()}
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_ENCOUNTER_TYPES })
	public List<EncounterType> getEncounterTypes() throws APIException;
	
	/**
	 * Get all locations
	 * 
	 * @return location list
	 * @throws APIException
	 * @deprecated replaced by {@link org.openmrs.api.LocationService#getAllLocations()}
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_LOCATIONS })
	public List<Location> getLocations() throws APIException;
	
	/**
	 * Get location by internal identifier
	 * 
	 * @param location id
	 * @return location with given internal identifier
	 * @throws APIException
	 * @deprecated replaced by {@link org.openmrs.api.LocationService#getLocation(Integer)}
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_LOCATIONS })
	public Location getLocation(Integer locationId) throws APIException;
	
	/**
	 * Get location by name
	 * 
	 * @param name location's name
	 * @return location with given name
	 * @throws APIException
	 * @deprecated replaced by {@link org.openmrs.api.LocationService#getLocation(String)}
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_LOCATIONS })
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
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_LOCATIONS })
	public List<Location> findLocations(String name) throws APIException;
	
}
