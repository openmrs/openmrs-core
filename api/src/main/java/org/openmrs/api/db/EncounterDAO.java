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
import java.util.Map;

import org.openmrs.Cohort;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.Visit;
import org.openmrs.VisitType;
import org.openmrs.api.EncounterService;

/**
 * Encounter-related database functions
 */
public interface EncounterDAO {
	
	/**
	 * Saves an encounter
	 * 
	 * @param encounter to be saved
	 * @throws DAOException
	 */
	public Encounter saveEncounter(Encounter encounter) throws DAOException;
	
	/**
	 * Purge an encounter from database.
	 * 
	 * @param encounter encounter object to be purged
	 */
	public void deleteEncounter(Encounter encounter) throws DAOException;
	
	/**
	 * Get encounter by internal identifier
	 * 
	 * @param encounterId encounter id
	 * @return encounter with given internal identifier
	 * @throws DAOException
	 */
	public Encounter getEncounter(Integer encounterId) throws DAOException;
	
	/**
	 * @param patientId
	 * @return all encounters for the given patient identifer
	 * @throws DAOException
	 */
	public List<Encounter> getEncountersByPatientId(Integer patientId) throws DAOException;
	
	/**
	 * @see org.openmrs.api.EncounterService#getEncounters(org.openmrs.Patient,
	 *      org.openmrs.Location, java.util.Date, java.util.Date, java.util.Collection,
	 *      java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection,
	 *      boolean)
	 */
	public List<Encounter> getEncounters(Patient patient, Location location, Date fromDate, Date toDate,
	        Collection<Form> enteredViaForms, Collection<EncounterType> encounterTypes, Collection<User> providers,
	        Collection<VisitType> visitTypes, Collection<Visit> visits, boolean includeVoided);
	
	/**
	 * Save an Encounter Type
	 * 
	 * @param encounterType
	 */
	public EncounterType saveEncounterType(EncounterType encounterType);
	
	/**
	 * Purge encounter type from database.
	 * 
	 * @param encounterType
	 * @throws DAOException
	 */
	public void deleteEncounterType(EncounterType encounterType) throws DAOException;
	
	/**
	 * Get encounterType by internal identifier
	 * 
	 * @param encounterTypeId Internal Integer identifier for an EncounterType
	 * @return EncounterType with given internal identifier
	 * @throws DAOException
	 */
	public EncounterType getEncounterType(Integer encounterTypeId) throws DAOException;
	
	/**
	 * Get encounterType by name
	 * 
	 * @param name String representation of an encounterType
	 * @return EncounterType
	 * @throws DAOException
	 */
	public EncounterType getEncounterType(String name) throws DAOException;
	
	/**
	 * Get all encounter types
	 * 
	 * @return encounter types list
	 * @throws DAOException
	 */
	public List<EncounterType> getAllEncounterTypes(Boolean includeVoided) throws DAOException;
	
	/**
	 * Find Encounter Types matching the given name. Search string is case insensitive, so that
	 * "NaMe".equals("name") is true.
	 * 
	 * @param name
	 * @return all EncounterTypes that match
	 * @throws DAOException
	 */
	public List<EncounterType> findEncounterTypes(String name) throws DAOException;
	
	/**
	 * Gets the value of encounterDatetime currently saved in the database for the given encounter,
	 * bypassing any caches. This is used prior to saving an encounter so that we can change the obs
	 * if need be
	 * 
	 * @param encounter the Encounter go the the encounterDatetime of
	 * @return the encounterDatetime currently in the database for this encounter
	 * @should get saved encounter datetime from database
	 */
	public Date getSavedEncounterDatetime(Encounter encounter);
	
	/**
	 * Find {@link Encounter} matching a uuid
	 * 
	 * @param uuid
	 * @return {@link Encounter}
	 */
	public Encounter getEncounterByUuid(String uuid);
	
	/**
	 * Find {@link EncounterType} matching a uuid
	 * 
	 * @param uuid
	 * @return {@link EncounterType}
	 */
	public EncounterType getEncounterTypeByUuid(String uuid);
	
	/**
	 * Get a list of {@link Encounter} by Patient name or identifier based on batch settings
	 * 
	 * @param query patient name or identifier
	 * @param start beginning index for the batch
	 * @param length number of encounters to return in the batch
	 * @param includeVoided Specifies whether voided encounters should be included
	 * @return list of {@link Encounter} based on batch settings
	 * @see EncounterService#getEncounters(String, Integer, Integer, boolean)
	 */
	List<Encounter> getEncounters(String query, Integer start, Integer length, boolean includeVoided);
	
	/**
	 * Gets the location of the encounter
	 * 
	 * @param encounter to be retrieved from the database
	 * @return {@link Location}
	 */
	public Location getSavedEncounterLocation(Encounter encounter);
	
	/**
	 * @see EncounterService#getAllEncounters(Cohort)
	 */
	public Map<Integer, List<Encounter>> getAllEncounters(Cohort patients);
	
	/**
	 * Return the number of encounters matching a patient name or patient identifier
	 * 
	 * @param query patient name or identifier
	 * @param includeVoided Specifies whether voided encounters should be included
	 * @return the number of encounters matching the given search phrase
	 * @see {@link EncounterService#getCountOfEncounters(String, boolean)}
	 */
	public Integer getCountOfEncounters(String query, boolean includeVoided);
	
	/**
	 * @see EncounterService#getEncountersByVisit(Visit)
	 */
	public List<Encounter> getEncountersByVisit(Visit visit);
}
