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
package org.openmrs.hl7;

import java.util.Collection;
import java.util.List;

import org.openmrs.Encounter;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;
import org.openmrs.hl7.db.HL7DAO;
import org.springframework.transaction.annotation.Transactional;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.v25.datatype.PL;
import ca.uhn.hl7v2.model.v25.datatype.XCN;
import ca.uhn.hl7v2.model.v25.segment.PID;

/**
 * OpenMRS HL7 API
 */
@Transactional
public interface HL7Service extends OpenmrsService {
	
	/**
	 * Auto generated method comment
	 * 
	 * @param dao
	 */
	public void setHL7DAO(HL7DAO dao);
	
	/**
	 * Save the given <code>hl7Source</code> to the database
	 * 
	 * @param hl7Source the source to save
	 * @return the saved source
	 */
	@Authorized(HL7Constants.PRIV_MANAGE_HL7_SOURCE)
	public HL7Source saveHL7Source(HL7Source hl7Source) throws APIException;
	
	/**
	 * @deprecated use {@link #saveHL7Source(HL7Source)}
	 */
	@Authorized(HL7Constants.PRIV_MANAGE_HL7_SOURCE)
	public void createHL7Source(HL7Source hl7Source) throws APIException;
	
	/**
	 * Auto generated method comment
	 * 
	 * @param hl7SourceId
	 * @return
	 */
	@Transactional(readOnly = true)
	@Authorized(HL7Constants.PRIV_VIEW_HL7_SOURCE)
	public HL7Source getHL7Source(Integer hl7SourceId) throws APIException;
	
	/**
	 * Get the hl7 source object from the database that has the given name
	 * 
	 * @param name string to 'search' on
	 * @return hl7 source object
	 */
	@Transactional(readOnly = true)
	@Authorized(HL7Constants.PRIV_VIEW_HL7_SOURCE)
	public HL7Source getHL7SourceByName(String name) throws APIException;
	
	/**
	 * @deprecated use {@link #getHL7SourceByName(String)}
	 */
	@Transactional(readOnly = true)
	@Authorized(HL7Constants.PRIV_VIEW_HL7_SOURCE)
	public HL7Source getHL7Source(String name);
	
	/**
	 * Get all of the hl7 source objects from the database. This includes retired ones
	 * 
	 * @return list of hl7 source objects
	 */
	@Transactional(readOnly = true)
	@Authorized(HL7Constants.PRIV_VIEW_HL7_SOURCE)
	public List<HL7Source> getAllHL7Sources() throws APIException;
	
	/**
	 * @deprecated use {@link #getAllHL7Sources()}
	 */
	@Transactional(readOnly = true)
	@Authorized(HL7Constants.PRIV_VIEW_HL7_SOURCE)
	public Collection<HL7Source> getHL7Sources();
	
	/**
	 * @deprecated use {@link #saveHL7Source(HL7Source)}
	 */
	@Authorized(HL7Constants.PRIV_MANAGE_HL7_SOURCE)
	public void updateHL7Source(HL7Source hl7Source);
	
	/**
	 * Mark the given <code>hl7Source</code> as no longer active
	 * 
	 * @param hl7Source the source to retire
	 * @return the retired source
	 */
	@Authorized(HL7Constants.PRIV_MANAGE_HL7_SOURCE)
	public HL7Source retireHL7Source(HL7Source hl7Source) throws APIException;
	
	/**
	 * Completely remove the source from the database. This should only be used in rare cases. See
	 * {@link #retireHL7Source(HL7Source)}
	 * 
	 * @param hl7Source
	 */
	@Authorized(HL7Constants.PRIV_PURGE_HL7_SOURCE)
	public void purgeHL7Source(HL7Source hl7Source) throws APIException;
	
	/**
	 * @see #retireHL7Source(HL7Source)
	 * @deprecated use {@link #purgeHL7Source(HL7Source)}
	 */
	@Authorized(HL7Constants.PRIV_MANAGE_HL7_SOURCE)
	public void deleteHL7Source(HL7Source hl7Source);
	
	/**
	 * Save the given <code>hl7InQueue</code> to the database
	 * 
	 * @param hl7InQueue the queue item to save
	 * @return the saved queue item
	 */
	@Authorized(value = { HL7Constants.PRIV_UPDATE_HL7_IN_QUEUE, HL7Constants.PRIV_ADD_HL7_IN_QUEUE }, requireAll = false)
	public HL7InQueue saveHL7InQueue(HL7InQueue hl7InQueue) throws APIException;
	
	/**
	 * @deprecated use {@link #saveHL7InQueue(HL7InQueue)}
	 */
	@Authorized(HL7Constants.PRIV_ADD_HL7_IN_QUEUE)
	public void createHL7InQueue(HL7InQueue hl7InQueue);
	
	/**
	 * Get the hl7 queue item with the given primary key id
	 * 
	 * @param hl7InQueueId the id to search on
	 * @return the desired hl7InQueue object or null if none found
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized(HL7Constants.PRIV_VIEW_HL7_IN_QUEUE)
	public HL7InQueue getHL7InQueue(Integer hl7InQueueId) throws APIException;
	
	/**
	 * Return a list of all hl7 in queues in the database
	 * 
	 * @return all hl7 queue items
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized(HL7Constants.PRIV_VIEW_HL7_IN_QUEUE)
	public List<HL7InQueue> getAllHL7InQueues() throws APIException;
	
	/**
	 * @deprecated use {@link #getAllHL7InQueues()}
	 */
	@Transactional(readOnly = true)
	@Authorized(HL7Constants.PRIV_VIEW_HL7_IN_QUEUE)
	public Collection<HL7InQueue> getHL7InQueues();
	
	/**
	 * Get the first queue item in the database
	 * 
	 * @return the first queue item
	 */
	@Transactional(readOnly = true)
	@Authorized(HL7Constants.PRIV_VIEW_HL7_IN_QUEUE)
	public HL7InQueue getNextHL7InQueue() throws APIException;
	
	/**
	 * Completely delete the hl7 in queue item from the database.
	 * 
	 * @param hl7InQueue
	 */
	@Authorized(HL7Constants.PRIV_PURGE_HL7_IN_QUEUE)
	public void purgeHL7InQueue(HL7InQueue hl7InQueue);
	
	/**
	 * @deprecated use {@link #purgeHL7InQueue(HL7InQueue)}
	 */
	@Authorized(HL7Constants.PRIV_DELETE_HL7_IN_QUEUE)
	public void deleteHL7InQueue(HL7InQueue hl7InQueue);
	
	/**
	 * Save the given hl7 in archive to the database
	 * 
	 * @param hl7InArchive the archive to save
	 * @return the saved archive item
	 * @throws APIException
	 */
	@Authorized(value = { HL7Constants.PRIV_UPDATE_HL7_IN_ARCHIVE, HL7Constants.PRIV_ADD_HL7_IN_ARCHIVE }, requireAll = false)
	public HL7InArchive saveHL7InArchive(HL7InArchive hl7InArchive) throws APIException;
	
	/**
	 * @deprecated use {@link #saveHL7InArchive(HL7InArchive)}
	 */
	@Authorized(HL7Constants.PRIV_ADD_HL7_IN_ARCHIVE)
	public void createHL7InArchive(HL7InArchive hl7InArchive);
	
	/**
	 * Get the archive item with the given id
	 * 
	 * @param hl7InArchiveId the id to search on
	 * @return the matching archive item
	 */
	@Transactional(readOnly = true)
	@Authorized(HL7Constants.PRIV_VIEW_HL7_IN_ARCHIVE)
	public HL7InArchive getHL7InArchive(Integer hl7InArchiveId);
	
	/**
	 * Get the archive items given a state (deleted, error, pending, processing, processed).
	 * 
	 * @return list of archive item that actually were deleted
	 */
	@Transactional(readOnly = true)
	@Authorized(HL7Constants.PRIV_VIEW_HL7_IN_ARCHIVE)
	public List<HL7InArchive> getHL7InArchiveByState(Integer state) throws APIException;;
	
	/**
	 * Get all archive hl7 queue items from the database
	 * 
	 * @return list of archive items
	 */
	@Transactional(readOnly = true)
	@Authorized(HL7Constants.PRIV_VIEW_HL7_IN_ARCHIVE)
	public List<HL7InArchive> getAllHL7InArchives() throws APIException;
	
	/**
	 * @deprecated use {@link #getAllHL7InArchives()}
	 */
	@Transactional(readOnly = true)
	@Authorized(HL7Constants.PRIV_VIEW_HL7_IN_ARCHIVE)
	public Collection<HL7InArchive> getHL7InArchives();
	
	/**
	 * @deprecated use {@link #saveHL7InArchive(HL7InArchive)}
	 */
	@Authorized(HL7Constants.PRIV_UPDATE_HL7_IN_ARCHIVE)
	public void updateHL7InArchive(HL7InArchive hl7InArchive);
	
	/**
	 * Completely delete the hl7 in archive item from the database
	 * 
	 * @param hl7InArchive the archived item to delete
	 * @throws APIException
	 */
	@Authorized(HL7Constants.PRIV_PURGE_HL7_IN_ARCHIVE)
	public void purgeHL7InArchive(HL7InArchive hl7InArchive) throws APIException;
	
	/**
	 * @deprecated use {@link #purgeHL7InArchive(HL7InArchive)}
	 */
	@Authorized(HL7Constants.PRIV_DELETE_HL7_IN_ARCHIVE)
	public void deleteHL7InArchive(HL7InArchive hl7InArchive);
	
	/**
	 * Save the given error item to the database
	 * 
	 * @param hl7InError the item to save
	 * @return the saved item
	 * @throws APIException
	 */
	@Authorized(value = { HL7Constants.PRIV_UPDATE_HL7_IN_EXCEPTION, HL7Constants.PRIV_ADD_HL7_IN_EXCEPTION }, requireAll = false)
	public HL7InError saveHL7InError(HL7InError hl7InError) throws APIException;
	
	/**
	 * @deprecated use {@link #saveHL7InError(HL7InError)}
	 */
	@Authorized(HL7Constants.PRIV_ADD_HL7_IN_EXCEPTION)
	public void createHL7InError(HL7InError hl7InError);
	
	/**
	 * Get the error item with the given id
	 * 
	 * @param hl7InErrorId the id to search on
	 * @return the matching error item
	 */
	@Transactional(readOnly = true)
	@Authorized(HL7Constants.PRIV_VIEW_HL7_IN_EXCEPTION)
	public HL7InError getHL7InError(Integer hl7InErrorId) throws APIException;
	
	/**
	 * Get all hl7 in error items from the database
	 * 
	 * @return
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized(HL7Constants.PRIV_VIEW_HL7_IN_EXCEPTION)
	public List<HL7InError> getAllHL7InErrors() throws APIException;
	
	/**
	 * @deprecated use {@link #getAllHL7InErrors()}
	 */
	@Transactional(readOnly = true)
	@Authorized(HL7Constants.PRIV_VIEW_HL7_IN_EXCEPTION)
	public Collection<HL7InError> getHL7InErrors();
	
	/**
	 * @deprecated use {@link #saveHL7InError(HL7InError)}
	 */
	@Authorized(HL7Constants.PRIV_UPDATE_HL7_IN_EXCEPTION)
	public void updateHL7InError(HL7InError hl7InError);
	
	/**
	 * Completely remove this error item from the database
	 * 
	 * @param hl7InError the item to remove
	 * @throws APIException
	 */
	@Authorized(HL7Constants.PRIV_PURGE_HL7_IN_EXCEPTION)
	public void purgeHL7InError(HL7InError hl7InError) throws APIException;
	
	/**
	 * @deprecated use {@link #purgeHL7InError(HL7InError)}
	 */
	@Authorized(HL7Constants.PRIV_DELETE_HL7_IN_EXCEPTION)
	public void deleteHL7InError(HL7InError hl7InError);
	
	/**
	 * @param xcn HL7 component of data type XCN (extended composite ID number and name for persons)
	 *            (see HL7 2.5 manual Ch.2A.86)
	 * @return Internal ID # of the specified user, or null if that user can't be found or is
	 *         ambiguous
	 */
	public Integer resolveUserId(XCN xcn) throws HL7Exception;
	
	/**
	 * @param pl HL7 component of data type PL (person location) (see Ch 2.A.53)
	 * @return internal identifier of the specified location, or null if it is not found or
	 *         ambiguous
	 */
	public Integer resolveLocationId(PL pl) throws HL7Exception;
	
	/**
	 * @param pid A PID segment of an hl7 message
	 * @return The internal id number of the Patient described by the PID segment, or null of the
	 *         patient is not found, or if the PID segment is ambiguous
	 * @throws HL7Exception
	 */
	public Integer resolvePatientId(PID pid) throws HL7Exception;
	
	/**
	 * Clean up the current memory consumption
	 */
	public void garbageCollect();
	
	/**
	 * This method is called after an encounter and its obs are created. This method can be removed
	 * once we have obs groups being created correctly
	 * 
	 * @param encounter
	 * @deprecated This method is no longer needed. When an encounter is created in the ROUR01
	 *             handler, it is created with all obs. Any AOP hooking should be done on the
	 *             EncounterService.createEncounter(Encounter) method
	 */
	public void encounterCreated(Encounter encounter);
	
}
