/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.hl7;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.openmrs.Encounter;
import org.openmrs.Person;
import org.openmrs.annotation.Authorized;
import org.openmrs.annotation.Logging;
import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;
import org.openmrs.hl7.db.HL7DAO;
import org.openmrs.util.PrivilegeConstants;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v25.datatype.CX;
import ca.uhn.hl7v2.model.v25.datatype.PL;
import ca.uhn.hl7v2.model.v25.datatype.XCN;
import ca.uhn.hl7v2.model.v25.segment.NK1;
import ca.uhn.hl7v2.model.v25.segment.PID;

/**
 * OpenMRS HL7 API
 */
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
	@Deprecated
	@Authorized(HL7Constants.PRIV_MANAGE_HL7_SOURCE)
	public void createHL7Source(HL7Source hl7Source) throws APIException;
	
	/**
	 * Auto generated method comment
	 * 
	 * @param hl7SourceId
	 * @return <code>HL7Source</code>object for given identifier
	 */
	@Authorized(HL7Constants.PRIV_VIEW_HL7_SOURCE)
	public HL7Source getHL7Source(Integer hl7SourceId) throws APIException;
	
	/**
	 * Get the hl7 source object from the database that has the given name
	 * 
	 * @param name string to 'search' on
	 * @return hl7 source object
	 */
	@Authorized(HL7Constants.PRIV_VIEW_HL7_SOURCE)
	public HL7Source getHL7SourceByName(String name) throws APIException;
	
	/**
	 * @deprecated use {@link #getHL7SourceByName(String)}
	 */
	@Deprecated
	@Authorized(HL7Constants.PRIV_VIEW_HL7_SOURCE)
	public HL7Source getHL7Source(String name);
	
	/**
	 * Get all of the hl7 source objects from the database. This includes retired ones
	 * 
	 * @return list of hl7 source objects
	 */
	@Authorized(HL7Constants.PRIV_VIEW_HL7_SOURCE)
	public List<HL7Source> getAllHL7Sources() throws APIException;
	
	/**
	 * @deprecated use {@link #getAllHL7Sources()}
	 */
	@Deprecated
	@Authorized(HL7Constants.PRIV_VIEW_HL7_SOURCE)
	public Collection<HL7Source> getHL7Sources();
	
	/**
	 * @deprecated use {@link #saveHL7Source(HL7Source)}
	 */
	@Deprecated
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
	@Deprecated
	@Authorized(HL7Constants.PRIV_MANAGE_HL7_SOURCE)
	public void deleteHL7Source(HL7Source hl7Source);
	
	/**
	 * Save the given <code>hl7InQueue</code> to the database
	 * 
	 * @param hl7InQueue the queue item to save
	 * @return the saved queue item
	 * @should add generated uuid if uuid is null
	 */
	@Authorized(value = { HL7Constants.PRIV_UPDATE_HL7_IN_QUEUE, HL7Constants.PRIV_ADD_HL7_IN_QUEUE }, requireAll = false)
	public HL7InQueue saveHL7InQueue(HL7InQueue hl7InQueue) throws APIException;
	
	/**
	 * @deprecated use {@link #saveHL7InQueue(HL7InQueue)}
	 */
	@Deprecated
	@Authorized(HL7Constants.PRIV_ADD_HL7_IN_QUEUE)
	public void createHL7InQueue(HL7InQueue hl7InQueue);
	
	/**
	 * Get the hl7 queue item with the given primary key id
	 * 
	 * @param hl7InQueueId the id to search on
	 * @return the desired hl7InQueue object or null if none found
	 * @throws APIException
	 */
	@Authorized(HL7Constants.PRIV_VIEW_HL7_IN_QUEUE)
	public HL7InQueue getHL7InQueue(Integer hl7InQueueId) throws APIException;
	
	/**
	 * Get the hl7 queue item with the given uuid
	 * 
	 * @param uuid
	 * @return the HL7InQueue or <code>null</code>
	 * @throws APIException
	 * @since 1.9
	 */
	@Authorized(PrivilegeConstants.PRIV_VIEW_HL7_IN_QUEUE)
	public HL7InQueue getHL7InQueueByUuid(String uuid) throws APIException;
	
	/**
	 * Return a list of all hl7 in queues in the database
	 * 
	 * @return all hl7 queue items
	 * @throws APIException
	 */
	@Authorized(HL7Constants.PRIV_VIEW_HL7_IN_QUEUE)
	public List<HL7InQueue> getAllHL7InQueues() throws APIException;
	
	/**
	 * Return a list of all hl7 in queues based on batch settings and a query string
	 * 
	 * @param start beginning index
	 * @param length size of the batch
	 * @param messageState status of the HL7InQueue message
	 * @param query search string
	 * @return all matching hl7 queue items within batch window
	 * @throws APIException
	 * @since 1.7
	 */
	@Authorized(HL7Constants.PRIV_VIEW_HL7_IN_QUEUE)
	public List<HL7InQueue> getHL7InQueueBatch(int start, int length, int messageState, String query) throws APIException;
	
	/**
	 * the total count of all HL7InQueue objects in the database
	 * 
	 * @param messageState HL7InQueue status
	 * @param query search string
	 * @return the count of matching HL7InQueue items
	 * @throws APIException
	 * @since 1.7
	 */
	@Authorized(HL7Constants.PRIV_VIEW_HL7_IN_QUEUE)
	public Integer countHL7InQueue(int messageState, String query) throws APIException;
	
	/**
	 * Return a list of all hl7 in errors based on batch settings and a query string
	 * 
	 * @param start beginning index
	 * @param length size of the batch
	 * @param query search string
	 * @return all matching hl7 queue items within batch window
	 * @throws APIException
	 * @since 1.7
	 */
	@Authorized(HL7Constants.PRIV_VIEW_HL7_IN_QUEUE)
	public List<HL7InError> getHL7InErrorBatch(int start, int length, String query) throws APIException;
	
	/**
	 * the total count of all HL7InError objects in the database
	 * 
	 * @param query search string
	 * @return the count of matching HL7InError items
	 * @throws APIException
	 * @since 1.7
	 */
	@Authorized(HL7Constants.PRIV_VIEW_HL7_IN_QUEUE)
	public Integer countHL7InError(String query) throws APIException;
	
	/**
	 * Return a list of all hl7 in archives based on batch settings and a query string
	 * 
	 * @param start beginning index
	 * @param length size of the batch
	 * @param messageState status of the HL7InArchive message
	 * @param query search string
	 * @return all matching hl7 archive items within batch window
	 * @throws APIException
	 * @since 1.7
	 */
	@Authorized(HL7Constants.PRIV_VIEW_HL7_IN_ARCHIVE)
	public List<HL7InArchive> getHL7InArchiveBatch(int start, int length, int messageState, String query)
	        throws APIException;
	
	/**
	 * the total count of all HL7InArchive objects in the database
	 * 
	 * @param messageState status of the HL7InArchive message
	 * @param query search string
	 * @return the count of matching HL7InArchive items
	 * @throws APIException
	 * @since 1.7
	 */
	@Authorized(HL7Constants.PRIV_VIEW_HL7_IN_ARCHIVE)
	public Integer countHL7InArchive(int messageState, String query) throws APIException;
	
	/**
	 * @deprecated use {@link #getAllHL7InQueues()}
	 */
	@Deprecated
	@Authorized(HL7Constants.PRIV_VIEW_HL7_IN_QUEUE)
	public Collection<HL7InQueue> getHL7InQueues();
	
	/**
	 * Get the first queue item in the database
	 * 
	 * @return the first queue item
	 */
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
	@Deprecated
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
	@Deprecated
	@Authorized(HL7Constants.PRIV_ADD_HL7_IN_ARCHIVE)
	public void createHL7InArchive(HL7InArchive hl7InArchive);
	
	/**
	 * Get the archive item with the given id, If hl7 archives were moved to the file system, you
	 * can't do a look up by hl7ArchiveId, instead call
	 * {@link HL7Service#getHL7InArchiveByUuid(String)}
	 * 
	 * @param hl7InArchiveId the id to search on
	 * @return the matching archive item
	 */
	@Authorized(HL7Constants.PRIV_VIEW_HL7_IN_ARCHIVE)
	public HL7InArchive getHL7InArchive(Integer hl7InArchiveId);
	
	/**
	 * Get the archive item with the given uuid
	 * 
	 * @param uuid to search on
	 * @return the archive with the matching uuid if any found
	 * @throws APIException
	 * @since Version 1.7
	 */
	@Authorized(PrivilegeConstants.PRIV_VIEW_HL7_IN_ARCHIVE)
	public HL7InArchive getHL7InArchiveByUuid(String uuid) throws APIException;
	
	/**
	 * If hl7 migration has been run and the state matches that of processed items, the method
	 * returns a list of all archives in the file system, for any other state , it returns an empty
	 * list, this is because all archives would have a status of 'processed' after migration and all
	 * deleted archives moved back into the queue with a status of 'deleted' otherwise it returns
	 * archives with a matching state if migration hasn't yet been run.
	 * 
	 * @return list of archive items that match the state
	 * @throws APIException
	 * @since 1.5
	 */
	@Authorized(HL7Constants.PRIV_VIEW_HL7_IN_ARCHIVE)
	public List<HL7InArchive> getHL7InArchiveByState(Integer state) throws APIException;
	
	/**
	 * Get the queue items given a state (deleted, error, pending, processing, processed).
	 * 
	 * @return list of hl7 queue items that match the given state
	 * @throws APIException
	 * @since 1.7
	 */
	@Authorized(HL7Constants.PRIV_VIEW_HL7_IN_QUEUE)
	public List<HL7InQueue> getHL7InQueueByState(Integer state) throws APIException;
	
	/**
	 * Get all archive hl7 queue items from the database
	 * 
	 * @return list of archive items
	 */
	@Authorized(HL7Constants.PRIV_VIEW_HL7_IN_ARCHIVE)
	public List<HL7InArchive> getAllHL7InArchives() throws APIException;
	
	/**
	 * @deprecated use {@link #getAllHL7InArchives()}
	 */
	@Deprecated
	@Authorized(HL7Constants.PRIV_VIEW_HL7_IN_ARCHIVE)
	public Collection<HL7InArchive> getHL7InArchives();
	
	/**
	 * @deprecated use {@link #saveHL7InArchive(HL7InArchive)}
	 */
	@Deprecated
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
	@Deprecated
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
	@Deprecated
	@Authorized(HL7Constants.PRIV_ADD_HL7_IN_EXCEPTION)
	public void createHL7InError(HL7InError hl7InError);
	
	/**
	 * Get the error item with the given id
	 * 
	 * @param hl7InErrorId the id to search on
	 * @return the matching error item
	 */
	@Authorized(HL7Constants.PRIV_VIEW_HL7_IN_EXCEPTION)
	public HL7InError getHL7InError(Integer hl7InErrorId) throws APIException;
	
	/**
	 * Get the error item with the given uuid
	 * 
	 * @param uuid
	 * @return the HL7InError or <code>null</code>
	 * @throws APIException
	 * @sine 1.9
	 */
	@Authorized(PrivilegeConstants.PRIV_VIEW_HL7_IN_EXCEPTION)
	public HL7InError getHL7InErrorByUuid(String uuid) throws APIException;
	
	/**
	 * Get all <code>HL7InError</code> items from the database
	 * 
	 * @return a List<HL7InError> object with all <code>HL7InError</code> items from the database
	 * @throws APIException
	 */
	@Authorized(HL7Constants.PRIV_VIEW_HL7_IN_EXCEPTION)
	public List<HL7InError> getAllHL7InErrors() throws APIException;
	
	/**
	 * @deprecated use {@link #getAllHL7InErrors()}
	 */
	@Deprecated
	@Authorized(HL7Constants.PRIV_VIEW_HL7_IN_EXCEPTION)
	public Collection<HL7InError> getHL7InErrors();
	
	/**
	 * @deprecated use {@link #saveHL7InError(HL7InError)}
	 */
	@Deprecated
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
	@Deprecated
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
	 * @param xcn HL7 component of data type XCN (extended composite ID number and name for persons)
	 *            (see HL7 2.5 manual Ch.2A.86)
	 * @return Internal ID # of the specified person, or null if that person can't be found or is
	 *         ambiguous
	 */
	public Integer resolvePersonId(XCN xcn) throws HL7Exception;
	
	/**
	 * Resolves location from person location object, and if location id is specified then returns
	 * correspond internal identifier of the specified location. If only location_name is specified,
	 * it tries to return location internal identifier by given name
	 * 
	 * @param pl HL7 component of data type PL (person location) (see Ch 2.A.53)
	 * @return internal identifier of the specified location, or null if it is not found or
	 *         ambiguous
	 * @should return internal identifier of location if only location name is specified
	 * @should return internal identifier of location if only location id is specified
	 * @should return null if location id and name are incorrect
	 */
	public Integer resolveLocationId(PL pl) throws HL7Exception;
	
	/**
	 * @param pid A PID segment of an hl7 message
	 * @return The internal id number of the Patient described by the PID segment, or null if the
	 *         patient is not found or if the PID segment is ambiguous
	 * @throws HL7Exception
	 */
	public Integer resolvePatientId(PID pid) throws HL7Exception;
	
	/**
	 * determines a person (or patient) based on identifiers from a CX array, as found in a PID or
	 * NK1 segment; the first resolving identifier in the list wins
	 * 
	 * @param identifiers CX identifier list from an identifier (either PID or NK1)
	 * @return The internal id number of a Person based on one of the given identifiers, or null if
	 *         the Person is not found
	 * @throws HL7Exception
	 * @should find a person based on a patient identifier
	 * @should find a person based on a UUID
	 * @should find a person based on the internal person ID
	 * @should return null if no person is found
	 */
	public Person resolvePersonFromIdentifiers(CX[] identifiers) throws HL7Exception;
	
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
	@Deprecated
	public void encounterCreated(Encounter encounter);
	
	/**
	 * Process the given {@link HL7InQueue} item. <br/>
	 * If an error occurs while processing, a new {@link HL7InError} is created and saved. <br/>
	 * If no error occurs, a new {@link HL7InArchive} is created and saved.<br/>
	 * The given {@link HL7InQueue} is removed from the hl7 in queue table regardless of success or
	 * failure of the processing.
	 * 
	 * @param inQueue the {@link HL7InQueue} to parse and save all encounters/obs to the db
	 * @return the processed {@link HL7InQueue}
	 * @should create HL7InArchive after successful parsing
	 * @should create HL7InError after failed parsing
	 * @should fail if given inQueue is already marked as processing
	 * @should parse oru r01 message using overridden parser provided by a module
	 */
	public HL7InQueue processHL7InQueue(HL7InQueue inQueue) throws HL7Exception;
	
	/**
	 * Parses the given string and returns the resulting {@link Message}
	 * 
	 * @param hl7String the hl7 string to parse and save
	 * @return the {@link Message} that the given hl7 string represents
	 * @throws HL7Exception
	 * @see #processHL7InQueue(HL7InQueue)
	 * @should parse the given string into Message
	 */
	@Logging(ignoreAllArgumentValues = true)
	public Message parseHL7String(String hl7String) throws HL7Exception;
	
	/**
	 * Parses the given {@link Message} and saves the resulting content to the database
	 * 
	 * @param hl7Message the {@link Message} to process and save to the db.
	 * @return the processed message
	 * @throws HL7Exception
	 * @see {@link #processHL7String(String)}
	 * @see #processHL7InQueue(HL7InQueue)
	 * @should save hl7Message to the database
	 * @should parse message type supplied by module
	 */
	public Message processHL7Message(Message hl7Message) throws HL7Exception;
	
	/**
	 * Method is called by the archives migration thread to transfer hl7 in archives from the
	 * hl7_in_archives database table to the file system
	 * 
	 * @param progressStatusMap the map holding the number of archives transferred and failed
	 *            transfers
	 * @throws APIException
	 */
	@Authorized(requireAll = true, value = { HL7Constants.PRIV_VIEW_HL7_IN_ARCHIVE, HL7Constants.PRIV_PURGE_HL7_IN_ARCHIVE,
	        HL7Constants.PRIV_ADD_HL7_IN_QUEUE })
	public void migrateHl7InArchivesToFileSystem(Map<String, Integer> progressStatusMap) throws APIException;
	
	/**
	 * finds a UUID from an array of identifiers
	 * 
	 * @param identifiers
	 * @return the UUID or null
	 * @throws HL7Exception
	 * @should return null if no UUID found
	 * @should find a UUID in any position of the array
	 * @should not fail if multiple similar UUIDs exist in identifiers
	 * @should not fail if no assigning authority is found
	 * @should fail if multiple different UUIDs exist in identifiers
	 */
	public String getUuidFromIdentifiers(CX[] identifiers) throws HL7Exception;
	
	/**
	 * creates a Person from information held in an NK1 segment; if valid PatientIdentifiers exist,
	 * a Patient will be created and returned
	 * 
	 * @param nk1 the NK1 segment with person information
	 * @return the newly formed (but not saved) person
	 * @throws HL7Exception
	 * @should return a saved new person
	 * @should return a Patient if valid patient identifiers exist
	 * @should fail if a person with the same UUID exists
	 * @should fail on an invalid gender
	 * @should fail if no gender specified
	 * @should fail if no birthdate specified
	 */
	public Person createPersonFromNK1(NK1 nk1) throws HL7Exception;
	
	/**
	 * Loads data for a list of HL7 archives from the filesystem
	 * 
	 * @since 1.7
	 * @throws APIException
	 * @param archives
	 */
	public void loadHL7InArchiveData(List<HL7InArchive> archives) throws APIException;
	
	/**
	 * Loads HL7 data from the filesystem for an archived HL7InArchive
	 * 
	 * @since 1.7
	 * @throws APIException
	 * @param archive
	 */
	public void loadHL7InArchiveData(HL7InArchive archive) throws APIException;
	
	/**
	 * Get {@link HL7QueueItem} with the given uuid.
	 * <p>
	 * It calls {@link #getHL7InQueueByUuid(String)}, {@link #getHL7InArchiveByUuid(String)} and
	 * {@link #getHL7InErrorByUuid(String)} consecutively and returns the first non-null result.
	 * 
	 * @param uuid
	 * @return the queue item or <code>null</code>
	 * @throws APIException
	 * @sine 1.9
	 */
	public HL7QueueItem getHl7QueueItemByUuid(String uuid) throws APIException;
	
}
