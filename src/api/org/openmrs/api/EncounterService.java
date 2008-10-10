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
	 * Set the given <code>dao</code> on this encounter service. The dao will
	 * act as the conduit through with all encounter calls get to the database
	 * 
	 * @param dao
	 */
	public void setEncounterDAO(EncounterDAO dao);

	/**
	 * Saves a new encounter or updates an existing encounter. If an existing
	 * encounter, this method will automatically apply encounter.patient to all
	 * encounter.obs.patient
	 * 
	 * @param encounter to be saved
	 * @throws APIException
	 * 
	 * @should cascade patient to orders in the encounter
	 */
	@Authorized( { OpenmrsConstants.PRIV_ADD_ENCOUNTERS,
	        OpenmrsConstants.PRIV_EDIT_ENCOUNTERS })
	public Encounter saveEncounter(Encounter encounter) throws APIException;

	/**
	 * Get encounter by internal identifier
	 * 
	 * @param encounterId encounter id
	 * @return encounter with given internal identifier
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_ENCOUNTERS })
	public Encounter getEncounter(Integer encounterId) throws APIException;

	/**
	 * Get all encounters (not voided) for a patient.
	 * 
	 * @param patient
	 * @return List<Encounter> encounters (not voided) for a patient.
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_ENCOUNTERS })
	public List<Encounter> getEncountersByPatient(Patient patient);

	/**
	 * Get encounters for a patientId
	 * 
	 * @param patientId
	 * @return all encounters (not voided) for the given patient identifer
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_ENCOUNTERS })
	public List<Encounter> getEncountersByPatientId(Integer patientId)
	        throws APIException;

	/**
	 * Get encounters (not voided) for a patient identifier
	 * 
	 * @param identifier
	 * @return all encounters (not retired) for the given patient identifer
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_ENCOUNTERS })
	public List<Encounter> getEncountersByPatientIdentifier(String identifier)
	        throws APIException;

	/**
	 * Get all encounters that match a variety of (nullable) criteria
	 * 
	 * @param who
	 * @param loc
	 * @param fromDate
	 * @param toDate
	 * @param enteredViaForms
	 * @param encounterTypes
	 * @param includeVoided
	 * @return
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_ENCOUNTERS })
	public List<Encounter> getEncounters(Patient who, Location loc,
	        Date fromDate, Date toDate, Collection<Form> enteredViaForms,
	        Collection<EncounterType> encounterTypes, boolean includeVoided);

	/**
	 * Voiding a encounter essentially removes it from circulation
	 * 
	 * @param Encounter encounter
	 * @param String reason
	 */
	@Authorized( { OpenmrsConstants.PRIV_EDIT_ENCOUNTERS })
	public Encounter voidEncounter(Encounter encounter, String reason);

	/**
	 * Unvoid encounter record
	 * 
	 * @param encounter encounter to be revived
	 */
	@Authorized( { OpenmrsConstants.PRIV_EDIT_ENCOUNTERS })
	public Encounter unvoidEncounter(Encounter encounter) throws APIException;

	/**
	 * Completely remove an encounter from database.
	 * 
	 * For super users only. If dereferencing encounters, use
	 * <code>voidEncounter(org.openmrs.Encounter)</code>
	 * 
	 * @param encounter encounter object to be purged
	 */
	@Authorized( { OpenmrsConstants.PRIV_PURGE_ENCOUNTERS })
	public void purgeEncounter(Encounter encounter) throws APIException;

	/**
	 * Completely remove an encounter from database.
	 * 
	 * For super users only. If dereferencing encounters, use
	 * <code>voidEncounter(org.openmrs.Encounter)</code>
	 * 
	 * @param encounter encounter object to be purged
	 * @param cascade also purge observations
	 */
	@Authorized( { OpenmrsConstants.PRIV_PURGE_ENCOUNTERS })
	public void purgeEncounter(Encounter encounter, boolean cascade)
	        throws APIException;

	/**
	 * Save a new Encounter Type or update an existing Encounter Type.
	 * 
	 * @param encounterType
	 */
	@Authorized( { OpenmrsConstants.PRIV_MANAGE_ENCOUNTER_TYPES })
	public EncounterType saveEncounterType(EncounterType encounterType);

	/**
	 * Get encounterType by internal identifier
	 * 
	 * @param encounterType id
	 * @return encounterType with given internal identifier
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_ENCOUNTER_TYPES })
	public EncounterType getEncounterType(Integer encounterTypeId)
	        throws APIException;

	/**
	 * Get encounterType by exact name
	 * 
	 * @param encounterType string to match to an Encounter.name
	 * @return EncounterType that is not retired
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_ENCOUNTER_TYPES })
	public EncounterType getEncounterType(String name) throws APIException;

	/**
	 * Get all encounter types (not retired)
	 * 
	 * @return encounter types list
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_ENCOUNTER_TYPES })
	public List<EncounterType> getAllEncounterTypes() throws APIException;

	/**
	 * Get all encounter types. If includeRetired is true, also get retired
	 * encounter types.
	 * 
	 * @param includeRetired
	 * @return encounter types list
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_ENCOUNTER_TYPES })
	public List<EncounterType> getAllEncounterTypes(boolean includeRetired)
	        throws APIException;

	/**
	 * Find Encounter Types with name matching the beginning of the search
	 * string. Search strings are case insensitive so that "NaMe".equals("name")
	 * is true. Includes retired EncounterTypes.
	 * 
	 * @param name of the encounter type to find
	 * @return List<EncounterType> matching encounters
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_ENCOUNTER_TYPES })
	public List<EncounterType> findEncounterTypes(String name)
	        throws APIException;

	/**
	 * Retire an EncounterType.  This essentially marks the given
	 * encounter type as a non-current type that shouldn't be used 
	 * anymore.
	 * 
	 * @param encounterType the encounter type to retire
	 * @param reason required non-null purpose for retiring this encounter type
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_MANAGE_ENCOUNTER_TYPES })
	public EncounterType retireEncounterType(EncounterType encounterType, String reason)
	        throws APIException;

	/**
	 * Unretire an EncounterType.  This brings back the given encounter type
	 * and says that it can be used again
	 * 
	 * @param encounterType the encounter type to unretire
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_MANAGE_ENCOUNTER_TYPES })
	public EncounterType unretireEncounterType(EncounterType encounterType)
	        throws APIException;

	/**
	 * Completely remove an encounter type from database.
	 * 
	 * @param encounterType
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_PURGE_ENCOUNTER_TYPES })
	public void purgeEncounterType(EncounterType encounterType)
	        throws APIException;

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
	 * Delete encounter from database.
	 * 
	 * For super users only. If dereferencing encounters, use
	 * <code>voidEncounter(org.openmrs.Encounter)</code>
	 * 
	 * @param encounter encounter object to be deleted
	 * @deprecated replaced by {@link #purgeEncounter(encounter)}
	 */
	@Authorized( { OpenmrsConstants.PRIV_DELETE_ENCOUNTERS })
	public void deleteEncounter(Encounter encounter) throws APIException;

	/**
	 * Get encounters for a patientId (not voided). To include voided Encounters
	 * use
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
	public List<Encounter> getEncountersByPatientId(Integer patientId,
	        boolean includeVoided) throws APIException;

	/**
	 * Get encounters (not voided) for a patient identifier. To include voided
	 * encounters use
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
	public List<Encounter> getEncountersByPatientIdentifier(String identifier,
	        boolean includeVoided) throws APIException;

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
	 * Get all encounters (not voided) for a patient. To include voided
	 * encounters, use
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
	 * Get all encounters for a patient that took place between fromDate and
	 * toDate (both nullable and inclusive)
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
	 * Get all encounters that took place between fromDate and toDate (both
	 * nullable and inclusive)
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
	 * Get all encounters that took place between fromDate and toDate (both
	 * nullable and inclusive) at the given location
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
	public Collection<Encounter> getEncounters(Location loc, Date fromDate,
	        Date toDate);

	/**
	 * 
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
	 * @deprecated replaced by
	 *             {@link org.openmrs.api.LocationService#getAllLocations( )}
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
	 * @deprecated replaced by
	 *             {@link org.openmrs.api.LocationService#getLocation(Integer)}
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
	 * @deprecated replaced by
	 *             {@link org.openmrs.api.LocationService#getLocation(String)}
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_LOCATIONS })
	public Location getLocationByName(String name) throws APIException;

	/**
	 * Search for locations by name. Matches returned match the given string at
	 * the beginning of the name
	 * 
	 * @param name location's name
	 * @return list of locations with similar name
	 * @throws APIException
	 * @deprecated replaced by
	 *             {@link org.openmrs.api.LocationService#getLocations(String)}
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_LOCATIONS })
	public List<Location> findLocations(String name) throws APIException;

}
