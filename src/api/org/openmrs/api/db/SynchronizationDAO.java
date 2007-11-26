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

import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import org.openmrs.api.APIException;
import org.openmrs.synchronization.SyncRecordState;
import org.openmrs.synchronization.engine.SyncRecord;
import org.openmrs.synchronization.filter.SyncClass;
import org.openmrs.synchronization.ingest.SyncImportRecord;
import org.openmrs.synchronization.server.RemoteServer;

/**
 * Synchronization related database functions 
 */
public interface SynchronizationDAO {

    /**
     * Create a new SyncRecord
     * @param SyncRecord The SyncRecord to create
     * @throws DAOException
     */
    public void createSyncRecord(SyncRecord record) throws DAOException;
    
    /**
     * Update a SyncRecord
     * @param SyncRecord The SyncRecord to update
     * @throws DAOException
     */
    public void updateSyncRecord(SyncRecord record) throws DAOException;
    
    /**
     * Delete a SyncRecord
     * @param SyncRecord The SyncRecord to delete
     * @throws DAOException
     */
    public void deleteSyncRecord(SyncRecord record) throws DAOException;
    
    /**
     * 
     * @param guid of the SyncRecord to retrieve
     * @return SyncRecord The SyncRecord or null if not found
     * @throws DAOException
     */
    public SyncRecord getSyncRecord(String guid) throws DAOException;

    public SyncRecord getSyncRecordByOriginalGuid(String originalGuid) throws DAOException;

    /**
     * 
     * @return SyncRecord The latest SyncRecord or null if not found
     * @throws DAOException
     */
    public SyncRecord getLatestRecord() throws DAOException;

    /**
     * Create a new SyncImportRecord
     * @param SyncImportRecord The SyncImportRecord to create
     * @throws DAOException
     */
    public void createSyncImportRecord(SyncImportRecord record) throws DAOException;
    
    /**
     * Update a SyncImportRecord
     * @param SyncImportRecord The SyncImportRecord to update
     * @throws DAOException
     */
    public void updateSyncImportRecord(SyncImportRecord record) throws DAOException;
    
    /**
     * Delete a SyncImportRecord
     * @param SyncImportRecord The SyncImportRecord to delete
     * @throws DAOException
     */
    public void deleteSyncImportRecord(SyncImportRecord record) throws DAOException;
    
    /**
     * 
     * @param guid of the SyncImportRecord to retrieve
     * @return SyncImportRecord The SyncImportRecord or null if not found
     * @throws DAOException
     */
    public SyncImportRecord getSyncImportRecord(String guid) throws DAOException;

    /**
     * Returns the first SyncRecord in either the PENDING SEND or the NEW state
     * @return SyncRecord The first SyncRecord matching the criteria, or null if none matches
     * @throws DAOException
     */
    public SyncRecord getFirstSyncRecordInQueue() throws DAOException;
    
    /**
     * Get all SyncRecords
     * @return SyncRecord A list containing all SyncRecords
     * @throws DAOException
     */
    public List<SyncRecord> getSyncRecords() throws DAOException;
    
    /**
     * Get all SyncRecords in a specific SyncRecordState
     * @param state SyncRecordState for the SyncRecords to be returned
     * @return SyncRecord A list containing all SyncRecords with the given state
     * @throws DAOException
     */
    public List<SyncRecord> getSyncRecords(SyncRecordState state) throws DAOException;

    /**
     * Get all SyncRecords in specific SyncRecordStates
     * @param state SyncRecordStates for the SyncRecords to be returned
     * @return SyncRecord A list containing all SyncRecords with the given states
     * @throws DAOException
     */
    public List<SyncRecord> getSyncRecords(SyncRecordState[] states, boolean inverse) throws DAOException;

    public List<SyncRecord> getSyncRecords(SyncRecordState[] states, boolean inverse, RemoteServer server) throws DAOException;

    
    /**
     * Get all SyncRecords since a timestamp
     * @param from Timestamp specifying lower bound, not included.
     * @return SyncRecord A list containing all SyncRecords with a timestamp after the given timestamp
     * @throws DAOException
     */
    public List<SyncRecord> getSyncRecordsSince(Date from) throws DAOException;
    
    /**
     * Get all SyncRecords between two timestamps, including the to-timestamp.
     * @param from Timestamp specifying lower bound, not included.
     * @param to Timestamp specifying upper bound, included.
     * @return SyncRecord A list containing all SyncRecords with a timestamp between the from timestamp and up to and including the to timestamp
     * @throws DAOException
     */
    public List<SyncRecord> getSyncRecordsBetween(Date from, Date to) throws DAOException;

    /**
     * 
     * Retrieve value of given global property using synchronization data access meachnisms.
     * 
     * @param propertyName
     * @return
     */
    public String getGlobalProperty(String propertyName);
    
    /**
     * Set global property related to synchronization; notably bypasses any changeset recording mechanisms.
     * @param propertyName String specifying property name which value is to be set.
     * @param propertyValue String specifying property value to be set.
     * @throws APIException
     */
    public void setGlobalProperty(String propertyName, String propertyValue) throws APIException;    

    /**
     * Create a new RemoteServer
     * @param RemoteServer The RemoteServer to create
     * @throws DAOException
     */
    public void createRemoteServer(RemoteServer server) throws DAOException;
    
    /**
     * Update a RemoteServer
     * @param RemoteServer The RemoteServer to update
     * @throws DAOException
     */
    public void updateRemoteServer(RemoteServer server) throws DAOException;
    
    /**
     * Delete a RemoteServer
     * @param RemoteServer The RemoteServer to delete
     * @throws DAOException
     */
    public void deleteRemoteServer(RemoteServer server) throws DAOException;
    
    /**
     * 
     * @param guid of the RemoteServer to retrieve
     * @return RemoteServer The RemoteServer or null if not found
     * @throws DAOException
     */
    public RemoteServer getRemoteServer(Integer serverId) throws DAOException;

    /**
     * 
     * @param guid of the RemoteServer to retrieve
     * @return RemoteServer The RemoteServer or null if not found
     * @throws DAOException
     */
    public RemoteServer getRemoteServer(String guid) throws DAOException;

    /**
     * 
     * @param childUsername of the RemoteServer to retrieve
     * @return RemoteServer The RemoteServer or null if not found
     * @throws DAOException
     */
    public RemoteServer getRemoteServerByUsername(String username) throws DAOException;

    /**
     * 
     * @param guid of the RemoteServer to retrieve
     * @return RemoteServer The RemoteServer or null if not found
     * @throws DAOException
     */
    public List<RemoteServer> getRemoteServers() throws DAOException;

    /**
     * 
     * @param guid of the RemoteServer to retrieve
     * @return RemoteServer The RemoteServer or null if not found
     * @throws DAOException
     */
    public RemoteServer getParentServer() throws DAOException;

    /**
     * Create a new SyncClass
     * @param SyncClass The SyncClass to create
     * @throws DAOException
     */
    public void createSyncClass(SyncClass record) throws DAOException;
    
    /**
     * Update a SyncClass
     * @param SyncClass The SyncClass to update
     * @throws DAOException
     */
    public void updateSyncClass(SyncClass record) throws DAOException;
    
    /**
     * Delete a SyncClass
     * @param SyncClass The SyncClass to delete
     * @throws DAOException
     */
    public void deleteSyncClass(SyncClass record) throws DAOException;
    
    /**
     * 
     * @param guid of the SyncClass to retrieve
     * @return SyncClass The SyncClass or null if not found
     * @throws DAOException
     */
    public SyncClass getSyncClass(Integer syncClassId) throws DAOException;

    /**
     * 
     * @return SyncClass The latest SyncClass or null if not found
     * @throws DAOException
     */
    public List<SyncClass> getSyncClasses() throws DAOException;

    /**
     * Dumps the entire database, much like what you'd get from the mysqldump command, and
     * adds a few lines to set the child's GUID, and delete sync history 
     * 
     * @param guidForChild if not null, use this as the guid for the child server, otherwise autogenerate one 
     * @param out write the sql here
     * @throws DAOException
     */
    public void createDatabaseForChild(String guidForChild, OutputStream out) throws DAOException;
    
}
