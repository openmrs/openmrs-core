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
import java.util.Set;

import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.APIException;

/**
 * Encounter-related database functions
 * @version 1.0
 */
public interface EncounterDAO {

	/**
	 * Creates a new encounter
	 * 
	 * @param encounter to be created
	 * @throws DAOException
	 */
	public void createEncounter(Encounter encounter) throws DAOException;

	/**
	 * Get encounter by internal identifier
	 * 
	 * @param encounterId encounter id
	 * @return encounter with given internal identifier
	 * @throws DAOException
	 */
	public Encounter getEncounter(Integer encounterId) throws DAOException;
	
	/**
	 * 
	 * @param patientId
	 * @param includeVoided
	 * @return list of encounters for this patient
	 * @throws DAOException
	 */
	public List<Encounter> getEncountersByPatientId(Integer patientId, boolean includeVoided) throws DAOException;
	
	/**
	 * Get all encounter types
	 * 
	 * @return encounter types list
	 * @throws DAOException
	 */
	public List<EncounterType> getEncounterTypes() throws DAOException;

	/**
	 * Get encounterType by internal identifier
	 * 
	 * @param encounterType id
	 * @return encounterType with given internal identifier
	 * @throws DAOException
	 */
	public EncounterType getEncounterType(Integer encounterTypeId) throws DAOException;

	/**
	 * Get encounterType by name
	 * 
	 * @param encounterType string
	 * @return EncounterType
	 * @throws APIException
	 */
	public EncounterType getEncounterType(String name) throws DAOException;
	
	/**
	 * Get all locations
	 * 
	 * @return location list
	 * @throws DAOException
	 */
	public List<Location> getLocations() throws DAOException;

	/**
	 * Get location by internal identifier
	 * 
	 * @param location id
	 * @return location with given internal identifier
	 * @throws DAOException
	 */
	public Location getLocation(Integer locationId) throws DAOException;

	/**
	 * Get location by name
	 * 
	 * @param name location's name
	 * @return location with given name
	 * @throws DAOException
	 */
	public Location getLocationByName(String name) throws DAOException;
	
	/**
	 * Search for locations by name.  Matches returned match the given string at 
	 * the beginning of the name
	 * 
	 * @param name location's name
	 * @return list of locations with similar name
	 * @throws APIException
	 */
	public List<Location> findLocations(String name) throws DAOException;
	
	/**
	 * Save changes to encounter
	 * @param encounter
	 * @throws DAOException
	 */
	public void updateEncounter(Encounter encounter) throws DAOException;
	
	/**
	 * Delete encounter from database.
	 * 
	 * @param encounter encounter object to be deleted 
	 */
	public void deleteEncounter(Encounter encounter) throws DAOException;
	
	/**
	 * all encounters for a patient
	 * @param who
	 * @param includeVoided
	 * @return
	 */
	public Set<Encounter> getEncounters(Patient who, boolean includeVoided);

	/**
	 * Get all encounters for a patient that took place at a specific location
	 * @param who
	 * @param where
	 * @return
	 */
    public Set<Encounter> getEncounters(Patient who, Location where);

    /**
     * Get all encounters for a patient that took place between fromDate and toDate (both nullable and inclusive)
     * @param who
     * @param fromDate
     * @param toDate
     * @return
     */
    public Set<Encounter> getEncounters(Patient who, Date fromDate, Date toDate);
	
    /**
     * Get all encounters that took place between fromDate and toDate (both nullable and inclusive)
     * @param fromDate
     * @param toDate
     * @return
     */
    public Collection<Encounter> getEncounters(Date fromDate, Date toDate);
	
    /**
     * Get all encounters that took place between fromDate and toDate (both nullable and inclusive)
     * at the given location
     * @param loc Location
     * @param fromDate
     * @param toDate
     * @return
     */
    public Collection<Encounter> getEncounters(Location loc, Date fromDate, Date toDate);
    
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
    public Collection<Encounter> getEncounters(Patient who,
                                               Location loc,
                                               Date fromDate,
                                               Date toDate,
                                               Collection<Form> enteredViaForms,
                                               Collection<EncounterType> encounterTypes,
                                               boolean includeVoided);

	/**
     * Gets the value of encounterDatetime currently saved in the database
     * for the given encounter, bypassing any caches. 
     * 
     * @param encounter the Encounter go the the encounterDatetime of
     * @return the encounterDatetime currently in the database for this encounter
     */
    public Date getSavedEncounterDatetime(Encounter encounter);
	
}
