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
package org.openmrs.hl7.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.hl7.HL7InArchive;
import org.openmrs.hl7.HL7InError;
import org.openmrs.hl7.HL7InQueue;
import org.openmrs.hl7.HL7Service;
import org.openmrs.hl7.HL7Source;
import org.openmrs.hl7.db.HL7DAO;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.v25.datatype.CX;
import ca.uhn.hl7v2.model.v25.datatype.PL;
import ca.uhn.hl7v2.model.v25.datatype.XCN;
import ca.uhn.hl7v2.model.v25.segment.PID;

/**
 * OpenMRS HL7 API default methods This class shouldn't be instantiated by itself. Use the
 * {@link org.openmrs.api.context.Context}
 * 
 * @see org.openmrs.hl7.HL7Service
 */
public class HL7ServiceImpl extends BaseOpenmrsService implements HL7Service {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	protected HL7DAO dao;
	
	/**
	 * Default constructor
	 */
	public HL7ServiceImpl() {
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#setHL7DAO(org.openmrs.hl7.db.HL7DAO)
	 */
	public void setHL7DAO(HL7DAO dao) {
		this.dao = dao;
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#saveHL7Source(org.openmrs.hl7.HL7Source)
	 */
	public HL7Source saveHL7Source(HL7Source hl7Source) throws APIException {
		if (hl7Source.getCreator() == null)
			hl7Source.setCreator(Context.getAuthenticatedUser());
		if (hl7Source.getDateCreated() == null)
			hl7Source.setDateCreated(new Date());
		
		return dao.saveHL7Source(hl7Source);
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#purgeHL7Source(org.openmrs.hl7.HL7Source)
	 */
	public void purgeHL7Source(HL7Source hl7Source) throws APIException {
		dao.deleteHL7Source(hl7Source);
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#retireHL7Source(org.openmrs.hl7.HL7Source)
	 */
	public HL7Source retireHL7Source(HL7Source hl7Source) throws APIException {
		throw new APIException("Not implemented yet");
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#createHL7Source(org.openmrs.hl7.HL7Source)
	 * @deprecated
	 */
	public void createHL7Source(HL7Source hl7Source) {
		saveHL7Source(hl7Source);
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#getHL7Source(java.lang.Integer)
	 */
	public HL7Source getHL7Source(Integer hl7SourceId) {
		return dao.getHL7Source(hl7SourceId);
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#getAllHL7Sources()
	 */
	public List<HL7Source> getAllHL7Sources() throws APIException {
		return dao.getAllHL7Sources();
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#getHL7SourceByName(java.lang.String)
	 */
	public HL7Source getHL7SourceByName(String name) throws APIException {
		return dao.getHL7SourceByName(name);
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#getHL7Source(java.lang.String)
	 * @deprecated
	 */
	public HL7Source getHL7Source(String name) {
		return getHL7SourceByName(name);
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#getHL7Sources()
	 * @deprecated
	 */
	public Collection<HL7Source> getHL7Sources() {
		return getAllHL7Sources();
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#updateHL7Source(org.openmrs.hl7.HL7Source)
	 * @deprecated
	 */
	public void updateHL7Source(HL7Source hl7Source) {
		saveHL7Source(hl7Source);
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#deleteHL7Source(org.openmrs.hl7.HL7Source)
	 * @deprecated
	 */
	public void deleteHL7Source(HL7Source hl7Source) {
		purgeHL7Source(hl7Source);
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#getAllHL7InQueues()
	 */
	public List<HL7InQueue> getAllHL7InQueues() throws APIException {
		return dao.getAllHL7InQueues();
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#purgeHL7InQueue(org.openmrs.hl7.HL7InQueue)
	 */
	public void purgeHL7InQueue(HL7InQueue hl7InQueue) {
		dao.deleteHL7InQueue(hl7InQueue);
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#saveHL7InQueue(org.openmrs.hl7.HL7InQueue)
	 */
	public HL7InQueue saveHL7InQueue(HL7InQueue hl7InQueue) throws APIException {
		if (hl7InQueue.getDateCreated() == null)
			hl7InQueue.setDateCreated(new Date());
		
		return dao.saveHL7InQueue(hl7InQueue);
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#createHL7InQueue(org.openmrs.hl7.HL7InQueue)
	 * @deprecated
	 */
	public void createHL7InQueue(HL7InQueue hl7InQueue) {
		saveHL7InQueue(hl7InQueue);
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#getHL7InQueue(java.lang.Integer)
	 */
	public HL7InQueue getHL7InQueue(Integer hl7InQueueId) {
		return dao.getHL7InQueue(hl7InQueueId);
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#getHL7InQueues()
	 * @deprecated
	 */
	public Collection<HL7InQueue> getHL7InQueues() {
		return getAllHL7InQueues();
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#getNextHL7InQueue()
	 */
	public HL7InQueue getNextHL7InQueue() {
		return dao.getNextHL7InQueue();
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#deleteHL7InQueue(org.openmrs.hl7.HL7InQueue)
	 * @deprecated
	 */
	public void deleteHL7InQueue(HL7InQueue hl7InQueue) {
		purgeHL7InQueue(hl7InQueue);
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#getHL7InArchiveByState(java.lang.Integer)
	 */
	public List<HL7InArchive> getHL7InArchiveByState(Integer state) throws APIException {
		return dao.getHL7InArchiveByState(state);
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#getAllHL7InArchives()
	 */
	public List<HL7InArchive> getAllHL7InArchives() throws APIException {
		return dao.getAllHL7InArchives();
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#purgeHL7InArchive(org.openmrs.hl7.HL7InArchive)
	 */
	public void purgeHL7InArchive(HL7InArchive hl7InArchive) throws APIException {
		dao.deleteHL7InArchive(hl7InArchive);
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#saveHL7InArchive(org.openmrs.hl7.HL7InArchive)
	 */
	public HL7InArchive saveHL7InArchive(HL7InArchive hl7InArchive) throws APIException {
		
		hl7InArchive.setDateCreated(new Date());
		
		return dao.saveHL7InArchive(hl7InArchive);
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#createHL7InArchive(org.openmrs.hl7.HL7InArchive)
	 * @deprecated
	 */
	public void createHL7InArchive(HL7InArchive hl7InArchive) {
		saveHL7InArchive(hl7InArchive);
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#getHL7InArchive(java.lang.Integer)
	 */
	public HL7InArchive getHL7InArchive(Integer hl7InArchiveId) {
		return dao.getHL7InArchive(hl7InArchiveId);
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#getHL7InArchives()
	 * @deprecated
	 */
	public Collection<HL7InArchive> getHL7InArchives() {
		return getAllHL7InArchives();
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#updateHL7InArchive(org.openmrs.hl7.HL7InArchive)
	 * @deprecated
	 */
	public void updateHL7InArchive(HL7InArchive hl7InArchive) {
		saveHL7InArchive(hl7InArchive);
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#deleteHL7InArchive(org.openmrs.hl7.HL7InArchive)
	 * @deprecated
	 */
	public void deleteHL7InArchive(HL7InArchive hl7InArchive) {
		purgeHL7InArchive(hl7InArchive);
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#getAllHL7InErrors()
	 */
	public List<HL7InError> getAllHL7InErrors() throws APIException {
		return dao.getAllHL7InErrors();
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#purgeHL7InError(org.openmrs.hl7.HL7InError)
	 */
	public void purgeHL7InError(HL7InError hl7InError) throws APIException {
		dao.deleteHL7InError(hl7InError);
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#saveHL7InError(org.openmrs.hl7.HL7InError)
	 */
	public HL7InError saveHL7InError(HL7InError hl7InError) throws APIException {
		hl7InError.setDateCreated(new Date());
		
		return dao.saveHL7InError(hl7InError);
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#createHL7InError(org.openmrs.hl7.HL7InError)
	 * @deprecated
	 */
	public void createHL7InError(HL7InError hl7InError) {
		saveHL7InError(hl7InError);
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#getHL7InError(java.lang.Integer)
	 */
	public HL7InError getHL7InError(Integer hl7InErrorId) {
		return dao.getHL7InError(hl7InErrorId);
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#getHL7InErrors()
	 * @deprecated
	 */
	public Collection<HL7InError> getHL7InErrors() {
		return dao.getAllHL7InErrors();
	}
	
	/**
	 * @deprecated
	 * @see org.openmrs.hl7.HL7Service#updateHL7InError(org.openmrs.hl7.HL7InError)
	 */
	public void updateHL7InError(HL7InError hl7InError) {
		saveHL7InError(hl7InError);
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#deleteHL7InError(org.openmrs.hl7.HL7InError)
	 * @deprecated
	 */
	public void deleteHL7InError(HL7InError hl7InError) {
		purgeHL7InError(hl7InError);
	}
	
	/**
	 * @param xcn HL7 component of data type XCN (extended composite ID number and name for persons)
	 *            (see HL7 2.5 manual Ch.2A.86)
	 * @return Internal ID # of the specified user, or null if that user can't be found or is
	 *         ambiguous
	 */
	public Integer resolveUserId(XCN xcn) throws HL7Exception {
		// TODO: properly handle family and given names. For now I'm treating
		// givenName+familyName as a username.
		String idNumber = xcn.getIDNumber().getValue();
		String familyName = xcn.getFamilyName().getSurname().getValue();
		String givenName = xcn.getGivenName().getValue();
		
		// unused
		// String assigningAuthority = xcn.getAssigningAuthority()
		// .getUniversalID().getValue();
		
		/*
		 * if ("null".equals(familyName)) familyName = null; if
		 * ("null".equals(givenName)) givenName = null; if
		 * ("null".equals(assigningAuthority)) assigningAuthority = null;
		 */
		if (idNumber != null && idNumber.length() > 0) {
			// log.debug("searching for user by id " + idNumber);
			try {
				Integer userId = new Integer(idNumber);
				User user = Context.getUserService().getUser(userId);
				return user.getUserId();
			}
			catch (Exception e) {
				log.error("Invalid user ID '" + idNumber + "'", e);
				return null;
			}
		} else {
			// log.debug("searching for user by name");
			try {
				StringBuilder username = new StringBuilder();
				if (familyName != null) {
					username.append(familyName);
				}
				if (givenName != null) {
					if (username.length() > 0)
						username.append(" "); // separate names with a space
					username.append(givenName);
				}
				// log.debug("looking for username '" + username + "'");
				User user = Context.getUserService().getUserByUsername(username.toString());
				return user.getUserId();
			}
			catch (Exception e) {
				log.error("Error resolving user with id '" + idNumber + "' family name '" + familyName
				        + "' and given name '" + givenName + "'", e);
				return null;
			}
		}
	}
	
	/**
	 * @param pl HL7 component of data type PL (person location) (see Ch 2.A.53)
	 * @return internal identifier of the specified location, or null if it is not found or
	 *         ambiguous
	 */
	public Integer resolveLocationId(PL pl) throws HL7Exception {
		// TODO: Get rid of hack that allows first component to be an integer
		// location.location_id
		String pointOfCare = pl.getPointOfCare().getValue();
		String facility = pl.getFacility().getUniversalID().getValue();
		
		// HACK: try to treat the first component (which should be "Point of
		// Care" as an internal openmrs location_id
		try {
			Integer locationId = new Integer(pointOfCare);
			Location l = Context.getLocationService().getLocation(locationId);
			return l == null ? null : l.getLocationId();
		}
		catch (Exception ex) {
			if (facility == null) { // we have no tricks left up our sleeve, so
				// throw an exception
				throw new HL7Exception("Error trying to treat PL.pointOfCare '" + pointOfCare
				        + "' as a location.location_id", ex);
			}
		}
		
		// Treat the 4th component "Facility" as location.name
		try {
			Location l = Context.getLocationService().getLocation(facility);
			if (l == null) {
				log.debug("Couldn't find a location named '" + facility + "'");
			}
			return l == null ? null : l.getLocationId();
		}
		catch (Exception ex) {
			log.error("Error trying to treat PL.facility '" + facility + "' as a location.name", ex);
			return null;
		}
	}
	
	/**
	 * @param pid A PID segment of an hl7 message
	 * @return The internal id number of the Patient described by the PID segment, or null of the
	 *         patient is not found, or if the PID segment is ambiguous
	 * @throws HL7Exception
	 */
	public Integer resolvePatientId(PID pid) throws HL7Exception {
		// TODO: Properly handle assigning authority. If specified it's
		// currently treated as PatientIdentifierType.name
		// TODO: Throw exceptions instead of returning null in some cases
		// TODO: Don't hydrate Patient objects unnecessarily
		// TODO: Determine how to handle assigning authority and openmrs
		// patient_id numbers
		
		Integer patientId = null;
		
		CX[] patientIdentifierList = pid.getPatientIdentifierList();
		if (patientIdentifierList.length < 1)
			throw new HL7Exception("Missing patient identifier in PID segment");
		
		// TODO other potential identifying characteristics in PID we could use
		// to identify the patient
		// XPN[] patientName = pid.getPersonName();
		// String gender = pid.getAdministrativeSex().getValue();
		// TS dateOfBirth = pid.getDateTimeOfBirth();
		
		// Take the first uniquely matching identifier
		for (CX identifier : patientIdentifierList) {
			String hl7PatientId = identifier.getIDNumber().getValue();
			// TODO if 1st component is blank, check 2nd and 3rd of assigning
			// authority
			String assigningAuthority = identifier.getAssigningAuthority().getNamespaceID().getValue();
			
			if (assigningAuthority != null && assigningAuthority.length() > 0) {
				// Assigning authority defined
				try {
					PatientIdentifierType pit = Context.getPatientService().getPatientIdentifierTypeByName(
					    assigningAuthority);
					if (pit == null) {
						log.warn("Can't find PatientIdentifierType named '" + assigningAuthority + "'");
						continue; // skip identifiers with unknown type
					}
					List<PatientIdentifier> matchingIds = Context.getPatientService().getPatientIdentifiers(hl7PatientId,
					    Collections.singletonList(pit), null, null, null);
					if (matchingIds == null || matchingIds.size() < 1) {
						// no matches
						log.warn("NO matches found for " + hl7PatientId);
						continue; // try next identifier
					} else if (matchingIds.size() == 1) {
						// unique match -- we're done
						return matchingIds.get(0).getPatient().getPatientId();
					} else {
						// ambiguous identifier
						log.debug("Ambiguous identifier in PID. " + matchingIds.size() + " matches for identifier '"
						        + hl7PatientId + "' of type '" + pit + "'");
						continue; // try next identifier
					}
				}
				catch (Exception e) {
					log.error("Error resolving patient identifier '" + hl7PatientId + "' for assigning authority '"
					        + assigningAuthority + "'", e);
					continue;
				}
			} else {
				try {
					log.debug("PID contains patient ID '" + hl7PatientId
					        + "' without assigning authority -- assuming patient.patient_id");
					patientId = Integer.parseInt(hl7PatientId);
					return patientId;
				}
				catch (NumberFormatException e) {
					// throw new HL7Exception("Invalid patient ID '" +
					// hl7PatientId + "'");
					log.warn("Invalid patient ID '" + hl7PatientId + "'");
				}
			}
		}
		
		return null;
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#garbageCollect()
	 */
	public void garbageCollect() {
		dao.garbageCollect();
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#encounterCreated(org.openmrs.Encounter)
	 * @deprecated This method is no longer needed. When an encounter is created in the ROUR01
	 *             handler, it is created with all obs. Any AOP hooking should be done on the
	 *             EncounterService.createEncounter(Encounter) method
	 */
	public void encounterCreated(Encounter encounter) {
		// nothing is done here in core. Modules override/hook on this method
	}
	
}
