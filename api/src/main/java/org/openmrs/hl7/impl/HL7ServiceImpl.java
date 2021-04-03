/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.hl7.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.PatientIdentifierException;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.hl7.HL7Constants;
import org.openmrs.hl7.HL7InArchive;
import org.openmrs.hl7.HL7InError;
import org.openmrs.hl7.HL7InQueue;
import org.openmrs.hl7.HL7QueueItem;
import org.openmrs.hl7.HL7Service;
import org.openmrs.hl7.HL7Source;
import org.openmrs.hl7.HL7Util;
import org.openmrs.hl7.Hl7InArchivesMigrateThread;
import org.openmrs.hl7.Hl7InArchivesMigrateThread.Status;
import org.openmrs.hl7.db.HL7DAO;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.util.PrivilegeConstants;
import org.openmrs.validator.PatientIdentifierValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.app.Application;
import ca.uhn.hl7v2.app.ApplicationException;
import ca.uhn.hl7v2.app.MessageTypeRouter;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v25.datatype.CX;
import ca.uhn.hl7v2.model.v25.datatype.ID;
import ca.uhn.hl7v2.model.v25.datatype.PL;
import ca.uhn.hl7v2.model.v25.datatype.TS;
import ca.uhn.hl7v2.model.v25.datatype.XCN;
import ca.uhn.hl7v2.model.v25.datatype.XPN;
import ca.uhn.hl7v2.model.v25.segment.NK1;
import ca.uhn.hl7v2.model.v25.segment.PID;
import ca.uhn.hl7v2.parser.EncodingNotSupportedException;
import ca.uhn.hl7v2.parser.GenericParser;

/**
 * OpenMRS HL7 API default methods This class shouldn't be instantiated by itself. Use the
 * {@link org.openmrs.api.context.Context}
 *
 * @see org.openmrs.hl7.HL7Service
 */
@Transactional
public class HL7ServiceImpl extends BaseOpenmrsService implements HL7Service {
	
	private static final Logger log = LoggerFactory.getLogger(HL7ServiceImpl.class);
	
	private static HL7ServiceImpl instance;
	
	protected HL7DAO dao;
	
	private GenericParser parser;
	
	private MessageTypeRouter router;
	
	/**
	 * Private constructor to only support on singleton instance.
	 *
	 * @see #getInstance()
	 */
	private HL7ServiceImpl() {
	}
	
	/**
	 * Singleton Factory method
	 *
	 * @return a singleton instance of this HL7ServiceImpl class
	 */
	public static HL7ServiceImpl getInstance() {
		if (instance == null) {
			instance = new HL7ServiceImpl();
		}
		return instance;
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#setHL7DAO(org.openmrs.hl7.db.HL7DAO)
	 */
	@Override
	public void setHL7DAO(HL7DAO dao) {
		this.dao = dao;
	}
	
	/**
	 * Used by spring to inject the parser
	 *
	 * @param parser the parser to use
	 */
	public void setParser(GenericParser parser) {
		this.parser = parser;
	}
	
	/**
	 * Used by spring to inject the router
	 *
	 * @param router the router to use
	 */
	public void setRouter(MessageTypeRouter router) {
		this.router = router;
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#saveHL7Source(org.openmrs.hl7.HL7Source)
	 */
	@Override
	public HL7Source saveHL7Source(HL7Source hl7Source) throws APIException {
		if (hl7Source.getCreator() == null) {
			hl7Source.setCreator(Context.getAuthenticatedUser());
		}
		if (hl7Source.getDateCreated() == null) {
			hl7Source.setDateCreated(new Date());
		}
		
		return dao.saveHL7Source(hl7Source);
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#purgeHL7Source(org.openmrs.hl7.HL7Source)
	 */
	@Override
	public void purgeHL7Source(HL7Source hl7Source) throws APIException {
		dao.deleteHL7Source(hl7Source);
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#retireHL7Source(org.openmrs.hl7.HL7Source)
	 */
	@Override
	public HL7Source retireHL7Source(HL7Source hl7Source) throws APIException {
		throw new APIException("general.not.yet.implemented", (Object[]) null);
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#getHL7Source(java.lang.Integer)
	 */
	@Override
	@Transactional(readOnly = true)
	public HL7Source getHL7Source(Integer hl7SourceId) {
		return dao.getHL7Source(hl7SourceId);
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#getAllHL7Sources()
	 */
	@Override
	@Transactional(readOnly = true)
	public List<HL7Source> getAllHL7Sources() throws APIException {
		return dao.getAllHL7Sources();
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#getHL7SourceByName(java.lang.String)
	 */
	@Override
	@Transactional(readOnly = true)
	public HL7Source getHL7SourceByName(String name) throws APIException {
		return dao.getHL7SourceByName(name);
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#getAllHL7InQueues()
	 */
	@Override
	@Transactional(readOnly = true)
	public List<HL7InQueue> getAllHL7InQueues() throws APIException {
		return dao.getAllHL7InQueues();
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#getHL7InQueueBatch(int, int, int, String)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<HL7InQueue> getHL7InQueueBatch(int start, int length, int messageState, String query) throws APIException {
		return dao.getHL7Batch(HL7InQueue.class, start, length, messageState, query);
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#getHL7InErrorBatch(int, int, java.lang.String)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<HL7InError> getHL7InErrorBatch(int start, int length, String query) throws APIException {
		return dao.getHL7Batch(HL7InError.class, start, length, null, query);
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#getHL7InArchiveBatch(int, int, int, String)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<HL7InArchive> getHL7InArchiveBatch(int start, int length, int messageState, String query)
	        throws APIException {
		return dao.getHL7Batch(HL7InArchive.class, start, length, messageState, query);
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#countHL7InQueue(int, java.lang.String)
	 */
	@Override
	@Transactional(readOnly = true)
	public Integer countHL7InQueue(int messageState, String query) throws APIException {
		return OpenmrsUtil.convertToInteger(dao.countHL7s(HL7InQueue.class, messageState, query));
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#countHL7InError(java.lang.String)
	 */
	@Override
	@Transactional(readOnly = true)
	public Integer countHL7InError(String query) throws APIException {
		return OpenmrsUtil.convertToInteger(dao.countHL7s(HL7InError.class, null, query));
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#countHL7InArchive(int, java.lang.String)
	 */
	@Override
	@Transactional(readOnly = true)
	public Integer countHL7InArchive(int messageState, String query) throws APIException {
		return OpenmrsUtil.convertToInteger(dao.countHL7s(HL7InArchive.class, messageState, query));
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#purgeHL7InQueue(org.openmrs.hl7.HL7InQueue)
	 */
	@Override
	public void purgeHL7InQueue(HL7InQueue hl7InQueue) {
		dao.deleteHL7InQueue(hl7InQueue);
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#saveHL7InQueue(org.openmrs.hl7.HL7InQueue)
	 */
	@Override
	public HL7InQueue saveHL7InQueue(HL7InQueue hl7InQueue) throws APIException {
		if (hl7InQueue.getDateCreated() == null) {
			hl7InQueue.setDateCreated(new Date());
		}
		
		if (hl7InQueue.getMessageState() == null) {
			hl7InQueue.setMessageState(HL7Constants.HL7_STATUS_PENDING);
		}
		
		return dao.saveHL7InQueue(hl7InQueue);
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#getHL7InQueue(java.lang.Integer)
	 */
	@Override
	@Transactional(readOnly = true)
	public HL7InQueue getHL7InQueue(Integer hl7InQueueId) {
		return dao.getHL7InQueue(hl7InQueueId);
	}
	
	@Override
	@Transactional(readOnly = true)
	public HL7InQueue getHL7InQueueByUuid(String uuid) throws APIException {
		return dao.getHL7InQueueByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#getNextHL7InQueue()
	 */
	@Override
	@Transactional(readOnly = true)
	public HL7InQueue getNextHL7InQueue() {
		return dao.getNextHL7InQueue();
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#getHL7InArchiveByState(java.lang.Integer)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<HL7InArchive> getHL7InArchiveByState(Integer state) throws APIException {
		return dao.getHL7InArchiveByState(state);
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#getHL7InQueueByState(java.lang.Integer)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<HL7InQueue> getHL7InQueueByState(Integer state) throws APIException {
		return dao.getHL7InQueueByState(state);
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#getAllHL7InArchives()
	 */
	@Override
	@Transactional(readOnly = true)
	public List<HL7InArchive> getAllHL7InArchives() throws APIException {
		return dao.getAllHL7InArchives();
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#purgeHL7InArchive(org.openmrs.hl7.HL7InArchive)
	 */
	@Override
	public void purgeHL7InArchive(HL7InArchive hl7InArchive) throws APIException {
		if (hl7InArchive != null) {
			dao.deleteHL7InArchive(hl7InArchive);
		}
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#saveHL7InArchive(org.openmrs.hl7.HL7InArchive)
	 */
	@Override
	public HL7InArchive saveHL7InArchive(HL7InArchive hl7InArchive) throws APIException {
		if (hl7InArchive.getDateCreated() == null) {
			hl7InArchive.setDateCreated(new Date());
		}
		return dao.saveHL7InArchive(hl7InArchive);
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#getHL7InArchive(java.lang.Integer)
	 */
	@Override
	@Transactional(readOnly = true)
	public HL7InArchive getHL7InArchive(Integer hl7InArchiveId) {
		return dao.getHL7InArchive(hl7InArchiveId);
	}
	
	/**
	 * get a list of archives to be migrated to the filesystem
	 */
	private List<HL7InArchive> getHL7InArchivesToMigrate() {
		return dao.getHL7InArchivesToMigrate();
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#getAllHL7InErrors()
	 */
	@Override
	@Transactional(readOnly = true)
	public List<HL7InError> getAllHL7InErrors() throws APIException {
		return dao.getAllHL7InErrors();
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#purgeHL7InError(org.openmrs.hl7.HL7InError)
	 */
	@Override
	public void purgeHL7InError(HL7InError hl7InError) throws APIException {
		dao.deleteHL7InError(hl7InError);
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#saveHL7InError(org.openmrs.hl7.HL7InError)
	 */
	@Override
	public HL7InError saveHL7InError(HL7InError hl7InError) throws APIException {
		if (hl7InError.getDateCreated() == null) {
			hl7InError.setDateCreated(new Date());
		}
		return dao.saveHL7InError(hl7InError);
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#getHL7InError(java.lang.Integer)
	 */
	@Override
	@Transactional(readOnly = true)
	public HL7InError getHL7InError(Integer hl7InErrorId) {
		return dao.getHL7InError(hl7InErrorId);
	}
	
	@Override
	@Transactional(readOnly = true)
	public HL7InError getHL7InErrorByUuid(String uuid) throws APIException {
		return dao.getHL7InErrorByUuid(uuid);
	}
	
	/** Gets the error message for failing to resolve a user with a certain id, family, and given name.
 	 * @param idNum id number
 	 * @param fName family name
 	 * @param gName given name
 	 * @return error string. User can not be resolveUserId
 	 */
 	private String getFindingUserErrorMessage(String idNum, String fName, String gName) {
	    return "Error resolving user with id '" + idNum + "' family name '" + fName
			      + "' and given name '" + gName + "'";
 	}
	
	/**
	 * @param xcn HL7 component of data type XCN (extended composite ID number and name for persons)
	 *            (see HL7 2.5 manual Ch.2A.86)
	 * @return Internal ID # of the specified user, or null if that user can't be found or is
	 *         ambiguous
	 */
	@Override
	@Transactional(readOnly = true)
	public Integer resolveUserId(XCN xcn) throws HL7Exception {
		String idNumber = xcn.getIDNumber().getValue();
		String familyName = xcn.getFamilyName().getSurname().getValue();
		String givenName = xcn.getGivenName().getValue();
		
		if (idNumber != null && idNumber.length() > 0) {
			try {
				Integer userId = Integer.valueOf(idNumber);
				User user = Context.getUserService().getUser(userId);
				return user.getUserId();
			}
			catch (Exception e) {
				log.error("Invalid user ID '" + idNumber + "'", e);
				return null;
			}
		} else {
			try {
				List<User> users = Context.getUserService().getUsersByName(givenName,familyName,true);
				if (users.size() == 1) {
					return users.get(0).getUserId();
				}
				else if (users.size() > 1) {
					//Return null if that user ambiguous
					log.error(getFindingUserErrorMessage(idNumber, familyName, givenName) + ": Found " + users.size() + " ambiguous users.");
					return null;
				}
				else {
					// legacy behavior is looking up by username
					StringBuilder username = new StringBuilder();
					if (familyName != null) {
						username.append(familyName);
					}
					if (givenName != null) {
						if (username.length() > 0) {
							username.append(" "); // separate names with a space
						}
						username.append(givenName);
					}
					User user = Context.getUserService().getUserByUsername(username.toString());
					
					if (user == null) {
						log.error(getFindingUserErrorMessage(idNumber, familyName, givenName) + ": User not found");
						return null;
					}
					return user.getUserId();
				}
			}
			catch (Exception e) {
				log.error(getFindingUserErrorMessage(idNumber, familyName, givenName), e);
				return null;
			}
		}
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#resolvePersonId(ca.uhn.hl7v2.model.v25.datatype.XCN)
	 */
	@Override
	@Transactional(readOnly = true)
	public Integer resolvePersonId(XCN xcn) throws HL7Exception {
		String idNumber = xcn.getIDNumber().getValue();
		String familyName = xcn.getFamilyName().getSurname().getValue();
		String givenName = xcn.getGivenName().getValue();
		
		if (idNumber != null && idNumber.length() > 0) {
			try {
				Person person = Context.getPersonService().getPerson(Integer.valueOf(idNumber));
				return person.getPersonId();
			}
			catch (Exception e) {
				log.error("Invalid person ID '" + idNumber + "'", e);
				return null;
			}
		} else {
			List<Person> persons = Context.getPersonService().getPeople(givenName + " " + familyName, null);
			if (persons.size() == 1) {
				return persons.get(0).getPersonId();
			} else if (persons.isEmpty()) {
				log.error("Couldn't find a person named " + givenName + " " + familyName);
				return null;
			} else {
				log.error("Found more than one person named " + givenName + " " + familyName);
				return null;
			}
		}
	}
	
	/**
	 * @param pl HL7 component of data type PL (person location) (see Ch 2.A.53)
	 * @return internal identifier of the specified location, or null if it is not found or
	 *         ambiguous
	 */
	@Override
	@Transactional(readOnly = true)
	public Integer resolveLocationId(PL pl) throws HL7Exception {
		// TODO: Get rid of hack that allows first component to be an integer
		// location.location_id
		String pointOfCare = pl.getPointOfCare().getValue();
		String facility = pl.getFacility().getUniversalID().getValue();
		// HACK: try to treat the first component (which should be "Point of
		// Care" as an internal openmrs location_id
		try {
			Integer locationId = Integer.valueOf(pointOfCare);
			Location l = Context.getLocationService().getLocation(locationId);
			if (l != null) {
				return l.getLocationId();
			}
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
	@Override
	@Transactional(readOnly = true)
	public Integer resolvePatientId(PID pid) throws HL7Exception {
		Person p = resolvePersonFromIdentifiers(pid.getPatientIdentifierList());
		if (p != null && p.getIsPatient()) {
			return p.getPersonId();
		}
		return null;
	}
	
	/**
	 * @param identifiers CX identifier list from an identifier (either PID or NK1)
	 * @return The internal id number of the Patient based on one of the given identifiers, or null
	 *         if the patient is not found
	 * @throws HL7Exception
	 */
	@Override
	@Transactional(readOnly = true)
	public Person resolvePersonFromIdentifiers(CX[] identifiers) throws HL7Exception {
		// TODO: Properly handle assigning authority. If specified it's
		// currently treated as PatientIdentifierType.name
		// TODO: Throw exceptions instead of returning null in some cases
		
		// give up if no identifiers exist
		if (identifiers.length < 1) {
			throw new HL7Exception("Missing patient identifier in PID segment");
		}
		
		// TODO other potential identifying characteristics in PID we could use
		// to identify the patient
		
		// Take the first uniquely matching identifier
		for (CX identifier : identifiers) {
			String hl7PersonId = identifier.getIDNumber().getValue();
			// TODO if 1st component is blank, check 2nd and 3rd of assigning
			// authority
			String assigningAuthority = identifier.getAssigningAuthority().getNamespaceID().getValue();
			
			if (StringUtils.isNotBlank(assigningAuthority)) {
				// Assigning authority defined
				try {
					PatientIdentifierType pit = Context.getPatientService().getPatientIdentifierTypeByName(
					    assigningAuthority);
					if (pit == null) {
						// there is no matching PatientIdentifierType
						if (assigningAuthority.equals(HL7Constants.HL7_AUTHORITY_UUID)) {
							// the identifier is a UUID
							Person p = Context.getPersonService().getPersonByUuid(hl7PersonId);
							if (p != null) {
								return p;
							}
							log.warn("Can't find person for UUID '" + hl7PersonId + "'");
							continue; // skip identifiers with unknown type
						} else if (assigningAuthority.equals(HL7Constants.HL7_AUTHORITY_LOCAL)) {
							// the ID is internal (local)
							String idType = identifier.getIdentifierTypeCode().getValue();
							try {
								if (idType.equals(HL7Constants.HL7_ID_PERSON)) {
									Integer pid = Integer.parseInt(hl7PersonId);
									// patient_id == person_id, so just look for
									// the person
									Person p = Context.getPersonService().getPerson(pid);
									if (p != null) {
										return p;
									}
								} else if (idType.equals(HL7Constants.HL7_ID_PATIENT)) {
									Integer pid = Integer.parseInt(hl7PersonId);
									// patient_id == person_id, so just look for
									// the person
									Patient p = Context.getPatientService().getPatient(pid);
									if (p != null) {
										return p;
									}
								}
							}
							catch (NumberFormatException e) {}
							log.warn("Can't find Local identifier of '" + hl7PersonId + "'");
							continue; // skip identifiers with unknown type
						}
						log.warn("Can't find PatientIdentifierType named '" + assigningAuthority + "'");
						continue; // skip identifiers with unknown type
					}
					List<PatientIdentifier> matchingIds = Context.getPatientService().getPatientIdentifiers(hl7PersonId,
					    Collections.singletonList(pit), null, null, null);
					if (matchingIds == null || matchingIds.isEmpty()) {
						// no matches
						log.warn("NO matches found for " + hl7PersonId);
					} else if (matchingIds.size() == 1) {
						// unique match -- we're done
						return matchingIds.get(0).getPatient();
					} else {
						// ambiguous identifier
						log.debug("Ambiguous identifier in PID. " + matchingIds.size() + " matches for identifier '"
						        + hl7PersonId + "' of type '" + pit + "'");
					}
				}
				catch (Exception e) {
					log.error("Error resolving patient identifier '" + hl7PersonId + "' for assigning authority '"
					        + assigningAuthority + "'", e);
				}
			} else {
				try {
					log.debug("CX contains ID '" + hl7PersonId
					        + "' without assigning authority -- assuming patient.patient_id");
					return Context.getPatientService().getPatient(Integer.parseInt(hl7PersonId));
				}
				catch (NumberFormatException e) {
					log.warn("Invalid patient ID '" + hl7PersonId + "'");
				}
			}
		}
		
		return null;
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#garbageCollect()
	 */
	@Override
	public void garbageCollect() {
		dao.garbageCollect();
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#processHL7InQueue(org.openmrs.hl7.HL7InQueue)
	 */
	@Override
	public HL7InQueue processHL7InQueue(HL7InQueue hl7InQueue) throws HL7Exception {
		
		if (hl7InQueue == null) {
			throw new HL7Exception("hl7InQueue argument cannot be null");
		}
		
		// mark this queue object as processing so that it isn't processed twice
		if (OpenmrsUtil.nullSafeEquals(HL7Constants.HL7_STATUS_PROCESSING, hl7InQueue.getMessageState())) {
			throw new HL7Exception("The hl7InQueue message with id: " + hl7InQueue.getHL7InQueueId()
			        + " is already processing. " + ",key=" + hl7InQueue.getHL7SourceKey() + ")");
		} else {
			hl7InQueue.setMessageState(HL7Constants.HL7_STATUS_PROCESSING);
		}
		
		log.debug("Processing HL7 inbound queue (id={},key={})", hl7InQueue.getHL7InQueueId(), hl7InQueue.getHL7SourceKey());
		
		// Parse the HL7 into an HL7Message or abort with failure
		String hl7Message = hl7InQueue.getHL7Data();
		try {
			// Parse the inbound HL7 message using the parser
			// NOT making a direct call here so that AOP can happen around this
			// method
			Message parsedMessage = Context.getHL7Service().parseHL7String(hl7Message);
			
			// Send the parsed message to our receiver routine for processing
			// into db
			// NOT making a direct call here so that AOP can happen around this
			// method
			Context.getHL7Service().processHL7Message(parsedMessage);
			
			// Move HL7 inbound queue entry into the archive before exiting
			log.debug("Archiving HL7 inbound queue entry");
			
			Context.getHL7Service().saveHL7InArchive(new HL7InArchive(hl7InQueue));
			
			log.debug("Removing HL7 message from inbound queue");
			Context.getHL7Service().purgeHL7InQueue(hl7InQueue);
		}
		catch (HL7Exception e) {
			boolean skipError = false;
			log.debug("Unable to process hl7inqueue: " + hl7InQueue.getHL7InQueueId(), e);
			log.debug("Hl7inqueue source: " + hl7InQueue.getHL7Source());
			log.debug("hl7_processor.ignore_missing_patient_non_local? "
			        + Context.getAdministrationService().getGlobalProperty(
			            OpenmrsConstants.GLOBAL_PROPERTY_IGNORE_MISSING_NONLOCAL_PATIENTS, "false"));
			if (e.getCause() != null
			        && "Could not resolve patient".equals(e.getCause().getMessage())
			        && !"local".equals(hl7InQueue.getHL7Source().getName())
			        && "true".equals(Context.getAdministrationService().getGlobalProperty(
			            OpenmrsConstants.GLOBAL_PROPERTY_IGNORE_MISSING_NONLOCAL_PATIENTS, "false"))) {
				skipError = true;
			}
			if (!skipError) {
				setFatalError(hl7InQueue, "Trouble parsing HL7 message (" + hl7InQueue.getHL7SourceKey() + ")", e);
			}
			
		}
		catch (Exception e) {
			setFatalError(hl7InQueue, "Exception while attempting to process HL7 In Queue (" + hl7InQueue.getHL7SourceKey()
			        + ")", e);
		}
		
		return hl7InQueue;
	}
	
	/**
	 * Convenience method to respond to fatal errors by moving the queue entry into an error bin
	 * prior to aborting
	 */
	private void setFatalError(HL7InQueue hl7InQueue, String error, Throwable cause) {
		HL7InError hl7InError = new HL7InError(hl7InQueue);
		hl7InError.setError(error);
		if (cause == null) {
			hl7InError.setErrorDetails("");
		} else {
			log.error("Fatal error", cause);
			hl7InError.setErrorDetails(ExceptionUtils.getStackTrace(cause));
		}
		Context.getHL7Service().saveHL7InError(hl7InError);
		Context.getHL7Service().purgeHL7InQueue(hl7InQueue);
		log.info(error, cause);
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#parseHL7String(String)
	 */
	@Override
	public Message parseHL7String(String hl7Message) throws HL7Exception {
		// Any pre-parsing for HL7 messages would go here
		// or a module can use AOP to pre-parse the message
		
		// First, try and parse the message
		Message message;
		try {
			message = parser.parse(hl7Message);
		}
		catch (EncodingNotSupportedException e) {
			throw new HL7Exception("HL7 encoding not supported", e);
		}
		catch (HL7Exception e) {
			throw new HL7Exception("Error parsing message", e);
		}
		
		return message;
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#getHL7InArchiveByUuid(java.lang.String)
	 */
	@Override
	@Transactional(readOnly = true)
	public HL7InArchive getHL7InArchiveByUuid(String uuid) throws APIException {
		if (Hl7InArchivesMigrateThread.isActive()) {
			throw new APIException("Hl7Service.cannot.fetch.archives", (Object[]) null);
		}
		return dao.getHL7InArchiveByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#processHL7Message(ca.uhn.hl7v2.model.Message)
	 */
	@Override
	public Message processHL7Message(Message message) throws HL7Exception {
		// Any post-parsing (pre-routing) processing would go here
		// or a module can use AOP to do the post-parsing
		
		Message response;
		try {
			if (!router.canProcess(message)) {
				throw new HL7Exception("No route for hl7 message: " + message.getName()
				        + ". Make sure you have a module installed that registers a hl7handler for this type");
			}
			response = router.processMessage(message);
		}
		catch (ApplicationException e) {
			throw new HL7Exception("Error while processing HL7 message: " + message.getName(), e);
		}
		
		return response;
	}
	
	/**
	 * Sets the given handlers as router applications that are available to HAPI when it is parsing
	 * an hl7 message.<br>
	 * This method is usually used by Spring and the handlers are set in the
	 * applicationContext-server.xml method.<br>
	 * The key in the map is a string like "ORU_R01" where the first part is the message type and
	 * the second is the trigger event.
	 *
	 * @param handlers a map from MessageName to Application object
	 */
	public void setHL7Handlers(Map<String, Application> handlers) {
		// loop over all the given handlers and add them to the router
		for (Map.Entry<String, Application> entry : handlers.entrySet()) {
			String messageName = entry.getKey();
			if (!messageName.contains("_")) {
				throw new APIException("Hl7Service.invalid.messageName", (Object[]) null);
			}
			
			String messageType = messageName.split("_")[0];
			String triggerEvent = messageName.split("_")[1];
			
			router.registerApplication(messageType, triggerEvent, entry.getValue());
		}
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#createPersonFromNK1(ca.uhn.hl7v2.model.v25.segment.NK1)
	 */
	@Override
	public Person createPersonFromNK1(NK1 nk1) throws HL7Exception {
		// NOTE: following block (with minor modifications) stolen from
		// ADTA28Handler
		// TODO: generalize this for use with both PID and NK1 segments
		
		Person person = new Person();
		
		// UUID
		CX[] identifiers = nk1.getNextOfKinAssociatedPartySIdentifiers();
		String uuid = getUuidFromIdentifiers(identifiers);
		if (Context.getPersonService().getPersonByUuid(uuid) != null) {
			throw new HL7Exception("Non-unique UUID '" + uuid + "' for new person");
		}
		person.setUuid(uuid);
		
		// Patient Identifiers
		List<PatientIdentifier> goodIdentifiers = new ArrayList<>();
		for (CX id : identifiers) {
			
			String assigningAuthority = id.getAssigningAuthority().getNamespaceID().getValue();
			String hl7PatientId = id.getIDNumber().getValue();
			
			log.debug("identifier has id=" + hl7PatientId + " assigningAuthority=" + assigningAuthority);
			
			if (assigningAuthority != null && assigningAuthority.length() > 0) {
				
				try {
					PatientIdentifierType pit = Context.getPatientService().getPatientIdentifierTypeByName(
					    assigningAuthority);
					if (pit == null) {
						if (!"UUID".equals(assigningAuthority)) {
							log.warn("Can't find PatientIdentifierType named '" + assigningAuthority + "'");
						}
						continue; // skip identifiers with unknown type
					}
					PatientIdentifier pi = new PatientIdentifier();
					pi.setIdentifierType(pit);
					pi.setIdentifier(hl7PatientId);
					
					// Get default location
					Location location = Context.getLocationService().getDefaultLocation();
					if (location == null) {
						throw new HL7Exception("Cannot find default location");
					}
					pi.setLocation(location);
					
					try {
						PatientIdentifierValidator.validateIdentifier(pi);
						goodIdentifiers.add(pi);
					}
					catch (PatientIdentifierException ex) {
						log.warn("Patient identifier in NK1 is invalid: " + pi, ex);
					}
					
				}
				catch (Exception e) {
					log.error("Uncaught error parsing/creating patient identifier '" + hl7PatientId
					        + "' for assigning authority '" + assigningAuthority + "'", e);
				}
			} else {
				log.debug("NK1 contains identifier with no assigning authority");
			}
		}
		if (!goodIdentifiers.isEmpty()) {
			//If we have one identifier, set it as the preferred to make the validator happy.
			if (goodIdentifiers.size() == 1) {
				goodIdentifiers.get(0).setPreferred(true);
			}
			
			// cast the person as a Patient and add identifiers
			person = new Patient(person);
			((Patient) person).addIdentifiers(goodIdentifiers);
		}
		
		// Person names
		for (XPN patientNameX : nk1.getNKName()) {
			PersonName name = new PersonName();
			name.setFamilyName(patientNameX.getFamilyName().getSurname().getValue());
			name.setGivenName(patientNameX.getGivenName().getValue());
			name.setMiddleName(patientNameX.getSecondAndFurtherGivenNamesOrInitialsThereof().getValue());
			person.addName(name);
		}
		
		// Gender (checks for null, but not for 'M' or 'F')
		String gender = nk1.getAdministrativeSex().getValue();
		if (gender == null) {
			throw new HL7Exception("Missing gender in an NK1 segment");
		}
		gender = gender.toUpperCase();
		if (!OpenmrsConstants.GENDERS.contains(gender)) {
			throw new HL7Exception("Unrecognized gender: " + gender);
		}
		person.setGender(gender);
		
		// Date of Birth
		TS dateOfBirth = nk1.getDateTimeOfBirth();
		if (dateOfBirth == null || dateOfBirth.getTime() == null || dateOfBirth.getTime().getValue() == null) {
			throw new HL7Exception("Missing birth date in an NK1 segment");
		}
		person.setBirthdate(HL7Util.parseHL7Timestamp(dateOfBirth.getTime().getValue()));
		
		// Estimated birthdate?
		ID precisionTemp = dateOfBirth.getDegreeOfPrecision();
		if (precisionTemp != null && precisionTemp.getValue() != null) {
			String precision = precisionTemp.getValue().toUpperCase();
			log.debug("The birthdate is estimated: " + precision);
			
			if ("Y".equals(precision) || "L".equals(precision)) {
				person.setBirthdateEstimated(true);
			}
		}
		
		// save the new person or patient
		if (person instanceof Patient) {
			Context.getPatientService().savePatient((Patient) person);
		} else {
			Context.getPersonService().savePerson(person);
		}
		
		return person;
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#getUuidFromIdentifiers(ca.uhn.hl7v2.model.v25.datatype.CX[])
	 */
	@Override
	public String getUuidFromIdentifiers(CX[] identifiers) throws HL7Exception {
		boolean found = false;
		String uuid = null;
		for (CX identifier : identifiers) {
			// check for UUID as the assigning authority
			if (OpenmrsUtil.nullSafeEquals(identifier.getAssigningAuthority().getNamespaceID().getValue(), "UUID")) {
				// check for duplicates
				if (found && !OpenmrsUtil.nullSafeEquals(identifier.getIDNumber().getValue(), uuid)) {
					throw new HL7Exception("multiple UUID values found");
				}
				uuid = identifier.getIDNumber().getValue();
				found = true;
			}
		}
		// returns null if not found
		return uuid;
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#loadHL7InArchiveData(List)
	 */
	@Override
	public void loadHL7InArchiveData(List<HL7InArchive> archives) throws APIException {
		for (HL7InArchive archive : archives) {
			loadHL7InArchiveData(archive);
		}
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#loadHL7InArchiveData(HL7InArchive)
	 */
	@Override
	public void loadHL7InArchiveData(HL7InArchive archive) throws APIException {
		// quit early if there is no archive to work with
		if (archive == null) {
			return;
		}
		
		// quit early if the message is not migrated or already loaded
		if (!OpenmrsUtil.nullSafeEquals(archive.getMessageState(), HL7Constants.HL7_STATUS_MIGRATED) || archive.isLoaded()) {
			return;
		}
		
		try {
			archive.setHL7Data(OpenmrsUtil.getFileAsString(new File(new URI(archive.getHL7Data()))));
			archive.setLoaded(true);
		}
		catch (URISyntaxException e) {
			throw new APIException("Hl7Service.malformed.archive.location", new Object[] { archive.getHL7Data() }, e);
		}
		catch (IOException e) {
			throw new APIException("Hl7Service.unable.convert.archive", new Object[] { archive.getHL7Data() }, e);
		}
	}
	
	/**
	 * @see org.openmrs.hl7.HL7Service#migrateHl7InArchivesToFileSystem(Map)
	 */
	@Override
	public void migrateHl7InArchivesToFileSystem(Map<String, Integer> progressStatusMap) throws APIException {
		int numberTransferred = 0;
		int numberOfFailedTransfers = 0;
		
		// HL7Constants.HL7_STATUS_ARCHIVED indicates the HL7 has been archived to the filesystem
		List<HL7InArchive> hl7InArchives = getHL7InArchivesToMigrate();
		
		// while we still we have any archives to be processed, process them
		while (Hl7InArchivesMigrateThread.isActive() && Hl7InArchivesMigrateThread.getTransferStatus() == Status.RUNNING
		        && hl7InArchives != null && !hl7InArchives.isEmpty()) {
			
			Iterator<HL7InArchive> iterator = hl7InArchives.iterator();
			
			while (Hl7InArchivesMigrateThread.isActive() && Hl7InArchivesMigrateThread.getTransferStatus() == Status.RUNNING
			        && iterator.hasNext()) {
				HL7InArchive archive = iterator.next();
				
				try {
					migrateHL7InArchive(archive);
					progressStatusMap.put(HL7Constants.NUMBER_TRANSFERRED_KEY, numberTransferred++);
				}
				catch (DAOException e) {
					progressStatusMap.put(HL7Constants.NUMBER_OF_FAILED_TRANSFERS_KEY, numberOfFailedTransfers++);
				}
			}
			
			// fetch more archives to be processed
			hl7InArchives = getHL7InArchivesToMigrate();
		}
		
		log.debug("Transfer of HL7 archives has completed or has been stopped");
	}
	
	/**
	 * moves data to the filesystem from an HL7InArchive
	 *
	 * @param archive
	 * @throws APIException
	 */
	private void migrateHL7InArchive(HL7InArchive archive) throws APIException {
		if (archive == null) {
			throw new APIException("Hl7Service.migrate.null.archive", (Object[]) null);
		}
		
		if (!OpenmrsUtil.nullSafeEquals(archive.getMessageState(), HL7Constants.HL7_STATUS_PROCESSED)) {
			throw new APIException("Hl7Service.migrate.archive.state", (Object[]) null);
		}
		
		try {
			URI uri = writeHL7InArchiveToFileSystem(archive);
			archive.setHL7Data(uri.toString());
			archive.setMessageState(HL7Constants.HL7_STATUS_MIGRATED);
			saveHL7InArchive(archive);
		}
		catch (APIException e) {
			throw new APIException("Hl7Service.migrate.archive", null, e);
		}
		
	}
	
	/**
	 * writes a given hl7 archive to the file system
	 *
	 * @param hl7InArchive the hl7 archive to write to the file system
	 */
	private URI writeHL7InArchiveToFileSystem(HL7InArchive hl7InArchive) throws APIException {
		
		PrintWriter writer = null;
		File destinationDir = HL7Util.getHl7ArchivesDirectory();
		try {
			// number formatter used to format month and day with zero padding
			DecimalFormat df = new DecimalFormat("00");
			
			//write the archive to a separate file while grouping them according to
			//the year, month and date of month when they were stored in the archives table
			Calendar calendar = Calendar.getInstance(Context.getLocale());
			calendar.setTime(hl7InArchive.getDateCreated());
			
			//resolve the year folder from the date of creation of the archive
			File yearDir = new File(destinationDir, Integer.toString(calendar.get(Calendar.YEAR)));
			if (!yearDir.isDirectory()) {
				yearDir.mkdirs();
			}
			
			//resolve the appropriate month folder
			File monthDir = new File(yearDir, df.format(calendar.get(Calendar.MONTH) + 1));
			if (!monthDir.isDirectory()) {
				monthDir.mkdirs();
			}
			
			//resolve the appropriate day of month folder
			File dayDir = new File(monthDir, df.format(calendar.get(Calendar.DAY_OF_MONTH)));
			if (!dayDir.isDirectory()) {
				dayDir.mkdirs();
			}
			
			//use the uuid, source id and source key(if present) to generate the file name
			File fileToWriteTo = new File(dayDir, hl7InArchive.getUuid()
			        + (StringUtils.isBlank(hl7InArchive.getHL7SourceKey()) ? "" : "_" + hl7InArchive.getHL7SourceKey())
			        + ".txt");
			
			//write the hl7 data to the file
			writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fileToWriteTo), StandardCharsets.UTF_8));
			writer.write(hl7InArchive.getHL7Data());
			
			//check if there was an error while writing to the current file
			if (writer.checkError()) {
				log.warn("An Error occured while writing hl7 archive with id '" + hl7InArchive.getHL7InArchiveId()
				        + "' to the file system");
				throw new APIException("Hl7Service.write.no.error", (Object[]) null);
			}
			
			// hand back the URI for the file
			return fileToWriteTo.toURI();
			
		}
		catch (FileNotFoundException e) {
			log
			        .warn("Failed to write hl7 archive with id '" + hl7InArchive.getHL7InArchiveId()
			                + "' to the file system ", e);
			throw new APIException("Hl7Service.write.error", null, e);
			
		}
		finally {
			if (writer != null) {
				writer.close();
			}
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public HL7QueueItem getHl7QueueItemByUuid(String uuid) throws APIException {
		HL7QueueItem result = getHL7InQueueByUuid(uuid);
		if (result != null) {
			Context.hasPrivilege(PrivilegeConstants.GET_HL7_IN_QUEUE);
			return result;
		}
		result = getHL7InErrorByUuid(uuid);
		if (result != null) {
			Context.hasPrivilege(PrivilegeConstants.GET_HL7_IN_EXCEPTION);
			return result;
		}
		result = getHL7InArchiveByUuid(uuid);
		if (result != null) {
			Context.hasPrivilege(PrivilegeConstants.GET_HL7_IN_ARCHIVE);
			return result;
		}
		return null;
	}
	
}
