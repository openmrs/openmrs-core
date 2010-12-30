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

import org.openmrs.api.db.DAOException;
import org.openmrs.hl7.HL7InArchive;
import org.openmrs.hl7.HL7InError;
import org.openmrs.hl7.HL7InQueue;
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
	
	/**
	 * Returns hl7s based on batch settings and filtered by a query
	 * @param <T>
	 * 
	 * @param clazz
	 * @param start
	 * @param length
	 * @param query
	 * @return list of hl7s
	 */
	@SuppressWarnings("rawtypes")
	public <T> List<T> getHL7Batch(Class clazz, int start, int length, Integer messageState, String query);
	
	/**
	 * Returns the amount of HL7 items in the database
	 * 
	 * @param clazz
	 * @param messageState
	 * @param query
	 * @return count of HL7 items
	 */
	@SuppressWarnings("rawtypes")
	public Integer countHL7s(Class clazz, Integer messageState, String query);
	
	/* HL7InArchive */

	/**
	 * @see org.openmrs.hl7.HL7Service#saveHL7InArchive(org.openmrs.hl7.HL7InArchive)
	 */
	public HL7InArchive saveHL7InArchive(HL7InArchive hl7InArchive) throws DAOException;
	
	/**
	 * @see org.openmrs.hl7.HL7Service#getHL7InArchive(Integer)
	 */
	public HL7InArchive getHL7InArchive(Integer hl7InArchiveId) throws DAOException;
	
	/**
	 * @see org.openmrs.hl7.HL7Service#getHL7InArchiveByUuid(String)
	 */
	public HL7InArchive getHL7InArchiveByUuid(String uuid) throws DAOException;
	
	/**
	 * @see org.openmrs.hl7.HL7Service#getHL7InArchiveByState(Integer state)
	 */
	public List<HL7InArchive> getHL7InArchiveByState(Integer state) throws DAOException;
	
	/**
	 * @see org.openmrs.hl7.HL7Service#getHL7InQueueByState(Integer stateId)
	 */
	public List<HL7InQueue> getHL7InQueueByState(Integer stateId) throws DAOException;
	
	/**
	 * @see org.openmrs.hl7.HL7Service#getAllHL7InArchives()
	 */
	public List<HL7InArchive> getAllHL7InArchives() throws DAOException;
	
	/**
	 * Returns hl7 in archives but with a limited resultset size to save memory
	 * 
	 * @param maxResults the maximum number of rows to be returned from the database
	 * @return list of hl7 archives
	 */
	public List<HL7InArchive> getAllHL7InArchives(Integer maxResults);
	
	/**
	 * @see org.openmrs.hl7.HL7Service#deleteHL7InArchive(org.openmrs.hl7.HL7InArchive)
	 */
	public void deleteHL7InArchive(HL7InArchive hl7InArchive) throws DAOException;
	
	/**
	 * provides a list of archives to be migrated
	 */
	public List<HL7InArchive> getHL7InArchivesToMigrate();
	
	/* HL7InError */

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
	
	// miscellaneous
	
	/**
	 * @see org.openmrs.hl7.HL7Service#garbageCollect()
	 */
	public void garbageCollect();
	
}
