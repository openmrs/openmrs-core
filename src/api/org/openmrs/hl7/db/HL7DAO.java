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
package org.openmrs.hl7.db;

import java.util.List;
import java.util.Map;

import org.openmrs.api.db.DAOException;
import org.openmrs.hl7.HL7InArchive;
import org.openmrs.hl7.HL7InError;
import org.openmrs.hl7.HL7InQueue;
import org.openmrs.hl7.HL7Service;
import org.openmrs.hl7.HL7Source;

/**
 * OpenMRS HL7 database related methods This class shouldn't be instantiated by itself. Use the
 * {@link org.openmrs.api.context.Context}
 * 
 * @see org.openmrs.hl7.HL7Service
 */
public interface HL7DAO {
	
	/* HL7Source */

	/**
	 * @see org.openmrs.hl7.HL7Service#saveHL7Source(org.openmrs.hl7.HL7Source)
	 */
	public HL7Source saveHL7Source(HL7Source hl7Source) throws DAOException;
	
	/**
	 * @see org.openmrs.hl7.HL7Service#getHL7Source(Integer)
	 */
	public HL7Source getHL7Source(Integer hl7SourceId) throws DAOException;
	
	/**
	 * @see org.openmrs.hl7.HL7Service#getHL7SourceByName(String)
	 */
	public HL7Source getHL7SourceByName(String name) throws DAOException;
	
	/**
	 * @see org.openmrs.hl7.HL7Service#getAllHL7Sources()
	 */
	public List<HL7Source> getAllHL7Sources() throws DAOException;
	
	/**
	 * @see org.openmrs.hl7.HL7Service#deleteHL7Source(org.openmrs.hl7.HL7Source)
	 */
	public void deleteHL7Source(HL7Source hl7Source) throws DAOException;
	
	/* HL7InQueue */

	/**
	 * @see org.openmrs.hl7.HL7Service#saveHL7InQueue(org.openmrs.hl7.HL7InQueue)
	 */
	public HL7InQueue saveHL7InQueue(HL7InQueue hl7InQueue) throws DAOException;
	
	/**
	 * @see org.openmrs.hl7.HL7Service#getHL7InQueue(Integer)
	 */
	public HL7InQueue getHL7InQueue(Integer hl7InQueueId) throws DAOException;
	
	/**
	 * @see org.openmrs.hl7.HL7Service#getAllHL7InQueues()
	 */
	public List<HL7InQueue> getAllHL7InQueues() throws DAOException;
	
	/**
	 * @see org.openmrs.hl7.HL7Service#getNextHL7InQueue()
	 */
	public HL7InQueue getNextHL7InQueue() throws DAOException;
	
	/**
	 * @see org.openmrs.hl7.HL7Service#deleteHL7InQueue(org.openmrs.hl7.HL7InQueue)
	 */
	public void deleteHL7InQueue(HL7InQueue hl7InQueue) throws DAOException;
	
	/* HL7InArchive */

	/**
	 * @see org.openmrs.hl7.HL7Service#saveHL7InArchive(org.openmrs.hl7.HL7InArchive)
	 */
	public HL7InArchive saveHL7InArchive(HL7InArchive hl7InArchive) throws DAOException;
	
	/**
	 * After archive migration has been done, this method is what gets called by the service layer
	 * to write the archive to the file system
	 */
	public HL7InArchive saveHL7InArchiveToFileSystem(HL7InArchive hl7InArchive) throws DAOException;
	
	/**
	 * @see org.openmrs.hl7.HL7Service#getHL7InArchive(Integer)
	 */
	public HL7InArchive getHL7InArchive(Integer hl7InArchiveId) throws DAOException;
	
	/**
	 * @see org.openmrs.hl7.HL7Service#getHL7InArchiveByUuid(String)
	 */
	public HL7InArchive getHL7InArchiveByUuid(String uuid) throws DAOException;
	
	/**
	 * @see org.openmrs.hl7.HL7Service#getHL7InArchiveByState(Integer stateId)
	 */
	public List<HL7InArchive> getHL7InArchiveByState(Integer stateId) throws DAOException;
	
	/**
	 * @see org.openmrs.hl7.HL7Service#getHL7InQueueByState(Integer stateId)
	 */
	public List<HL7InQueue> getHL7InQueueByState(Integer stateId) throws DAOException;
	
	/**
	 * @see org.openmrs.hl7.HL7Service#getAllHL7InArchives()
	 */
	public List<HL7InArchive> getAllHL7InArchives() throws DAOException;
	
	/**
	 * @see org.openmrs.hl7.HL7Service#deleteHL7InArchive(org.openmrs.hl7.HL7InArchive)
	 */
	public void deleteHL7InArchive(HL7InArchive hl7InArchive) throws DAOException;
	
	/* HL7InException */

	/**
	 * @see org.openmrs.hl7.HL7Service#saveHL7InError(org.openmrs.hl7.HL7InError)
	 */
	public HL7InError saveHL7InError(HL7InError hl7InError) throws DAOException;
	
	/**
	 * @see org.openmrs.hl7.HL7Service#getHL7InError(Integer)
	 */
	public HL7InError getHL7InError(Integer hl7InErrorId) throws DAOException;
	
	/**
	 * @see org.openmrs.hl7.HL7Service#getAllHL7InErrors()
	 */
	public List<HL7InError> getAllHL7InErrors() throws DAOException;
	
	/**
	 * @see org.openmrs.hl7.HL7Service#deleteHL7InError(org.openmrs.hl7.HL7InError)
	 */
	public void deleteHL7InError(HL7InError hl7InError) throws DAOException;
	
	/**
	 * @see org.openmrs.hl7.HL7Service#garbageCollect()
	 */
	public void garbageCollect();
	
	/**
	 * @see HL7Service#migrateHl7InArchivesToFileSystem(Map)
	 */
	public void migrateHl7InArchivesToFileSystem(Map<String, Integer> progressStatusMap) throws DAOException;
	
	/**
	 * Retrieves a single hl7 archive from the file system with the matching archive id
	 * 
	 * @param uuid uuid of the hl7 in archive to look up
	 * @return the hl7 archive with the matching id otherwise returns null if none found
	 */
	public HL7InArchive getHL7InArchiveInFileSystem(String uuid) throws DAOException;
	
	/**
	 * Deletes an HL7 archive from the file system
	 * 
	 * @param uuid uuid for the archive to delete
	 * @return true only if the file was successfully deleted from the file system
	 */
	public boolean deleteHL7InArchiveInFileSystem(String uuid) throws DAOException;
	
	/**
	 * Retrieves all hl7 in archives from the file system
	 * 
	 * @return a list of all hl7 in archives from the file system
	 */
	public List<HL7InArchive> getAllHL7InArchivesInFileSystem() throws DAOException;
	
	/**
	 * @see HL7Service#isArchiveMigrationRequired()
	 */
	public boolean isArchiveMigrationRequired() throws DAOException;
	
	/**
	 * Retrieves an hl7_in_archive with the matching the uuid
	 * 
	 * @return Hl7 in archive if it exists otherwise null
	 */
	public HL7InArchive getHL7InArchiveByUuidFromFileSystem(String uuid) throws DAOException;
	
	/**
	 * Returns hl7 in archives but with a limited resultset size to save memory
	 * 
	 * @param maxResultsSetSize the maximum number of rows to be returned from the database
	 * @return list of hl7 archives
	 */
	public List<HL7InArchive> getAllHL7InArchives(int maxResultsSetSize);
	
	/**
	 * Returns hl7s in queue based on batch settings and filtered by a query
	 * 
	 * @param start
	 * @param length
	 * @param query
	 * @return list of hl7s
	 */
	public List<HL7InQueue> getHL7InQueueBatch(int start, int length, int messageState, String query);
	
	/**
	 * Returns the amount of HL7InQueue items in the database
	 * 
	 * @return count of HL7InQueue items
	 */
	public Integer countHL7InQueue(Integer messageState, String query);
	
	/**
	 * Returns hl7s in error based on batch settings and filtered by a query
	 * 
	 * @param start
	 * @param length
	 * @param query
	 * @return
	 */
	public List<HL7InError> getHL7InErrorBatch(int start, int length, String query);
	
	/**
	 * Returns the amount of HL7InError items in the database
	 * 
	 * @return count of HL7InQueue items
	 */
	public Integer countHL7InError(String query);

}
