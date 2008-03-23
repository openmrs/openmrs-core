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

import java.util.Collection;

import org.openmrs.api.db.DAOException;
import org.openmrs.hl7.HL7InArchive;
import org.openmrs.hl7.HL7InError;
import org.openmrs.hl7.HL7InQueue;
import org.openmrs.hl7.HL7Source;

/**
 * Database API for HL7-related tables
 * 
 * @version 1.0
 */
public interface HL7DAO {
	
	/* HL7Source */
	
	public void createHL7Source(HL7Source hl7Source) throws DAOException;
	
	public HL7Source getHL7Source(Integer hl7SourceId) throws DAOException;
	
	public HL7Source getHL7Source(String name) throws DAOException;

	public Collection<HL7Source> getHL7Sources() throws DAOException;

	public void updateHL7Source(HL7Source hl7Source) throws DAOException;
	
	public void deleteHL7Source(HL7Source hl7Source) throws DAOException;

	/* HL7InQueue */
	
	public void createHL7InQueue(HL7InQueue hl7InQueue) throws DAOException;
	
	public HL7InQueue getHL7InQueue(Integer hl7InQueueId) throws DAOException;
	
	public Collection<HL7InQueue> getHL7InQueues() throws DAOException;
	
	public HL7InQueue getNextHL7InQueue() throws DAOException;
	
	public void deleteHL7InQueue(HL7InQueue hl7InQueue) throws DAOException;
	
	/* HL7InArchive */
	
	public void createHL7InArchive(HL7InArchive hl7InArchive) throws DAOException;
	
	public HL7InArchive getHL7InArchive(Integer hl7InArchiveId) throws DAOException;
	
	public Collection<HL7InArchive> getHL7InArchives() throws DAOException;
	
	public void updateHL7InArchive(HL7InArchive hl7InArchive) throws DAOException;
	
	public void deleteHL7InArchive(HL7InArchive hl7InArchive) throws DAOException;

	/* HL7InException */

	public void createHL7InError(HL7InError hl7InError) throws DAOException;
	
	public HL7InError getHL7InError(Integer hl7InErrorId) throws DAOException;
	
	public Collection<HL7InError> getHL7InErrors() throws DAOException;
	
	public void updateHL7InError(HL7InError hl7InError) throws DAOException;
	
	public void deleteHL7InError(HL7InError hl7InError) throws DAOException;

	public void garbageCollect();
}
