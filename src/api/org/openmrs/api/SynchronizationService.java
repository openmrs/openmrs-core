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
package org.openmrs.api;

import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import org.openmrs.annotation.Authorized;
import org.openmrs.synchronization.SyncRecordState;
import org.openmrs.synchronization.engine.SyncRecord;
import org.openmrs.synchronization.filter.SyncClass;
import org.openmrs.synchronization.ingest.SyncImportRecord;
import org.openmrs.synchronization.server.RemoteServer;
import org.springframework.transaction.annotation.Transactional;
import org.openmrs.synchronization.Synchronizable;

@Transactional
public interface SynchronizationService {

    /**
     * Create a new SyncRecord
     * @param SyncRecord The SyncRecord to create
     * @throws APIException
     */
    //@Authorized({"Manage Synchronization Records"})
    public void createSyncRecord(SyncRecord record) throws APIException;

    /**
     * Auto generated method comment
     * 
     * @param record
     * @param originalGuid
     */
    public void createSyncRecord(SyncRecord record, String originalGuid);

    /**
     * Update a SyncRecord
     * @param SyncRecord The SyncRecord to update
     * @throws APIException
     */
    //@Authorized({"Manage Synchronization Records"})
    public void updateSyncRecord(SyncRecord record) throws APIException;
    
    /**
     * Delete a SyncRecord
     * @param SyncRecord The SyncRecord to delete
     * @throws APIException
     */
    //@Authorized({"Manage Synchronization Records"})
    public void deleteSyncRecord(SyncRecord record) throws APIException;

    /**
     * 
     * @param guid of the SyncRecord to retrieve
     * @return SyncRecord The SyncRecord or null if not found
     * @throws APIException
     */
    //@Authorized({"View Synchronization Records"})
    @Transactional(readOnly=true)
    public SyncRecord getSyncRecord(String guid) throws APIException;

    @Transactional(readOnly=true)
    public SyncRecord getSyncRecordByOriginalGuid(String originalGuid) throws APIException;

    /**
     * 
     * @return SyncRecord The latest SyncRecord or null if not found
     * @throws APIException
     */
    @Authorized({"View Synchronization Records"})
    @Transactional(readOnly=true)
    public SyncRecord getLatestRecord() throws APIException;

    /**
     * Create a new SyncImportRecord
     * @param SyncImportRecord The SyncImportRecord to create
     * @throws APIException
     */
    @Authorized({"Manage Synchronization Records"})
    public void createSyncImportRecord(SyncImportRecord record) throws APIException;
    
    /**
     * Update a SyncImportRecord
     * @param SyncImportRecord The SyncImportRecord to update
     * @throws APIException
     */
    @Authorized({"Manage Synchronization Records"})
    public void updateSyncImportRecord(SyncImportRecord record) throws APIException;
    
    /**
     * Delete a SyncImportRecord
     * @param SyncImportRecord The SyncImportRecord to delete
     * @throws APIException
     */
    @Authorized({"Manage Synchronization Records"})
    public void deleteSyncImportRecord(SyncImportRecord record) throws APIException;

    /**
     * 
     * @param guid of the SyncImportRecord to retrieve
     * @return SyncRecord The SyncImportRecord or null if not found
     * @throws APIException
     */
    @Authorized({"View Synchronization Records"})
    @Transactional(readOnly=true)
    public SyncImportRecord getSyncImportRecord(String guid) throws APIException;
    
    /**
     * Returns the first SyncRecord in either the PENDING SEND or the NEW state
     * @return SyncRecord The first SyncRecord matching the criteria, or null if none matches
     * @throws APIException
     */
    //@Authorized({"View Synchronization Records"})
    @Transactional(readOnly=true)
    public SyncRecord getFirstSyncRecordInQueue() throws APIException;
    
    /**
     * Get all SyncRecords
     * @return SyncRecord A list containing all SyncRecords
     * @throws APIException
     */
    //@Authorized({"View Synchronization Records"})
    @Transactional(readOnly=true)
    public List<SyncRecord> getSyncRecords() throws APIException;
    
    /**
     * Get all SyncRecords in a specific SyncRecordState
     * @param state SyncRecordState for the SyncRecords to be returned
     * @return SyncRecord A list containing all SyncRecords with the given state
     * @throws APIException
     */
    //@Authorized({"View Synchronization Records"})
    @Transactional(readOnly=true)
    public List<SyncRecord> getSyncRecords(SyncRecordState state) throws APIException;

    /**
     * Get all SyncRecords in a specific SyncRecordStates
     * @param states SyncRecordStates for the SyncRecords to be returned
     * @return SyncRecord A list containing all SyncRecords with the given states
     * @throws APIException
     */
    @Authorized({"View Synchronization Records"})
    @Transactional(readOnly=true)
    public List<SyncRecord> getSyncRecords(SyncRecordState[] states) throws APIException;

    /**
     * Get all SyncRecords in a specific SyncRecordStates, that the server allows sending for (per-server basis)
     * @param states SyncRecordStates for the SyncRecords to be returned
     * @param server Server these records will be sent to, so we can filter on Class
     * @return SyncRecord A list containing all SyncRecords with the given states
     * @throws APIException
     */
    @Authorized({"View Synchronization Records"})
    @Transactional(readOnly=true)
    public List<SyncRecord> getSyncRecords(SyncRecordState[] states, RemoteServer server) throws APIException;

    
    /**
     * Get all SyncRecords in a specific SyncRecordStates
     * @param states SyncRecordStates for the SyncRecords to be returned
     * @return SyncRecord A list containing all SyncRecords with the given states
     * @throws APIException
     */
    @Authorized({"View Synchronization Records"})
    @Transactional(readOnly=true)
    public List<SyncRecord> getSyncRecords(SyncRecordState[] states, boolean inverse) throws APIException;

    /**
     * Get all SyncRecords after a given timestamp
     * @param from Timestamp specifying lower bound, not included.
     * @return SyncRecord A list containing all SyncRecords with a timestamp after the given timestamp
     * @throws APIException
     */
    //Authorized({"View Synchronization Records"})
    @Transactional(readOnly=true)
    public List<SyncRecord> getSyncRecordsSince(Date from) throws APIException;
    
    /**
     * Get all SyncRecords between two timestamps, including the to-timestamp.
     * @param from Timestamp specifying lower bound, not included.
     * @param to Timestamp specifying upper bound, included.
     * @return SyncRecord A list containing all SyncRecords with a timestamp between the from timestamp and up to and including the to timestamp
     * @throws APIException
     */
    //@Authorized({"View Synchronization Records"})
    @Transactional(readOnly=true)
    public List<SyncRecord> getSyncRecordsBetween(Date from, Date to) throws APIException;

    
    /**
     * 
     * Retrieve value of given global property using synchronization data access mechanisms.
     * 
     * @param propertyName
     * @return
     */
    //@Authorized({"View Synchronization Records"})
    @Transactional(readOnly=true)    
    public String getGlobalProperty(String propertyName) throws APIException;
    
    /**
     * Set global property related to synchronization; notably bypasses any changeset recording mechanisms.
     * @param propertyName String specifying property name which value is to be set.
     * @param propertyValue String specifying property value to be set.
     * @throws APIException
     */
    //@Authorized({"Manage Synchronization Records"})
    public void setGlobalProperty(String propertyName, String propertyValue) throws APIException;


    /**
     * Create a new SyncRecord
     * @param SyncRecord The SyncRecord to create
     * @throws APIException
     */
    @Authorized({"Manage Synchronization Servers"})
    public void createRemoteServer(RemoteServer server) throws APIException;
    
    /**
     * Update a SyncRecord
     * @param SyncRecord The SyncRecord to update
     * @throws APIException
     */
    @Authorized({"Manage Synchronization Servers"})
    public void updateRemoteServer(RemoteServer server) throws APIException;
    
    /**
     * Delete a SyncRecord
     * @param SyncRecord The SyncRecord to delete
     * @throws APIException
     */
    @Authorized({"Manage Synchronization Servers"})
    public void deleteRemoteServer(RemoteServer server) throws APIException;

    /**
     * 
     * @param guid of the SyncRecord to retrieve
     * @return SyncRecord The SyncRecord or null if not found
     * @throws APIException
     */
    @Authorized({"View Synchronization Servers"})
    @Transactional(readOnly=true)
    public RemoteServer getRemoteServer(Integer serverId) throws APIException;

    /**
     * 
     * @param guid of the SyncRecord to retrieve
     * @return SyncRecord The SyncRecord or null if not found
     * @throws APIException
     */
    @Authorized({"View Synchronization Servers"})
    @Transactional(readOnly=true)
    public RemoteServer getRemoteServer(String guid) throws APIException;

    /**
     * 
     * @param username child_username of the RemoteServer to retrieve
     * @return SyncRecord The SyncRecord or null if not found
     * @throws APIException
     */
    @Authorized({"View Synchronization Servers"})
    @Transactional(readOnly=true)
    public RemoteServer getRemoteServerByUsername(String username) throws APIException;

    /**
     * 
     * @param guid of the SyncRecord to retrieve
     * @return SyncRecord The SyncRecord or null if not found
     * @throws APIException
     */
    @Authorized({"View Synchronization Servers"})
    @Transactional(readOnly=true)
    public List<RemoteServer> getRemoteServers() throws APIException;

    /**
     * 
     * @param guid of the SyncRecord to retrieve
     * @return SyncRecord The SyncRecord or null if not found
     * @throws APIException
     */
    @Authorized({"View Synchronization Servers"})
    @Transactional(readOnly=true)
    public RemoteServer getParentServer() throws APIException;
    
    /**
     *  Retrieve globally unique id of the server.
     * @return guid of the server.
     * @throws APIException
     */
    @Authorized({"View Synchronization Servers"})
    @Transactional(readOnly=true)
    public String getServerGuid() throws APIException;

    /**
     * Create a new SyncClass
     * @param SyncClass The SyncClass to create
     * @throws APIException
     */
    //@Authorized({"Manage Synchronization"})
    public void createSyncClass(SyncClass syncClass) throws APIException;
    
    /**
     * Update a SyncClass
     * @param SyncClass The SyncClass to update
     * @throws APIException
     */
    //@Authorized({"Manage Synchronization"})
    public void updateSyncClass(SyncClass syncClass) throws APIException;
    
    /**
     * Delete a SyncClass
     * @param SyncClass The SyncClass to delete
     * @throws APIException
     */
    //@Authorized({"Manage Synchronization"})
    public void deleteSyncClass(SyncClass syncClass) throws APIException;

    /**
     * 
     * @param guid of the SyncClass to retrieve
     * @return SyncClass The SyncClass or null if not found
     * @throws APIException
     */
    //@Authorized({"Manage Synchronization"})
    @Transactional(readOnly=true)
    public SyncClass getSyncClass(Integer syncClassId) throws APIException;

    /**
     * 
     * @return SyncClass The latest SyncClass or null if not found
     * @throws APIException
     */
    //@Authorized({"Manage Synchronization"})
    @Transactional(readOnly=true)
    public List<SyncClass> getSyncClasses() throws APIException;


    
    
    /**
     * Dumps the entire database, much like what you'd get from the mysqldump command, and
     * adds a few lines to set the child's GUID, and delete sync history 
     * 
     * @param guidForChild if not null, use this as the guid for the child server, otherwise autogenerate one 
     * @param out write the sql here
     * @throws APIException
     */
    @Authorized({"Backup Entire Database"})
    @Transactional(readOnly=true)
    public void createDatabaseForChild(String guidForChild, OutputStream out) throws APIException;

    /**
     * Deletes instance of synchronizable from data storage.
     * 
     * @param o instance to delete
     * @throws APIException
     */
    //@Authorized({"Manage Synchronization"})
    @Transactional
    public void deleteSynchronizable(Synchronizable o) throws APIException;


    /**
     * Exposes ability to change persistence flush semantics.
     * 
     * @throws APIException
     * 
     * @see org.openmrs.api.db.SynchronizationDAO#setFlushModeManual()
     */
    public void setFlushModeManual() throws APIException;
    
    /**
     * Exposes ability to change persistence flush semantics.
     * 
     * @throws APIException
     * 
     * @see org.openmrs.api.db.SynchronizationDAO#setFlushModeAutomatic()
     */
    public void setFlushModeAutomatic() throws APIException;
    
    public void flushSession() throws APIException;
}
