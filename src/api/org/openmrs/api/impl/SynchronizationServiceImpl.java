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
package org.openmrs.api.impl;

import java.util.Date;
import java.util.List;

import org.openmrs.api.APIException;
import org.openmrs.api.SynchronizationService;
import org.openmrs.api.db.SynchronizationDAO;
import org.openmrs.synchronization.SyncRecordState;
import org.openmrs.synchronization.engine.SyncRecord;
import org.openmrs.synchronization.ingest.SyncImportRecord;
import org.openmrs.synchronization.server.RemoteServer;

public class SynchronizationServiceImpl implements SynchronizationService {

    private SynchronizationDAO dao;
    
    private SynchronizationDAO getSynchronizationDAO() {
        return dao;
    }
    
    public void setSynchronizationDAO(SynchronizationDAO dao) {
        this.dao = dao;
    }
    
    /**
     * @see org.openmrs.api.SynchronizationService#createSyncRecord(org.openmrs.synchronization.engine.SyncRecord)
     */
    public void createSyncRecord(SyncRecord record) throws APIException {
        getSynchronizationDAO().createSyncRecord(record);
    }

    /**
     * @see org.openmrs.api.SynchronizationService#createSyncRecord(org.openmrs.synchronization.engine.SyncRecord)
     */
    public void createSyncImportRecord(SyncImportRecord record) throws APIException {
        getSynchronizationDAO().createSyncImportRecord(record);
    }

    /**
     * @see org.openmrs.api.SynchronizationService#getNextSyncRecord()
     */
    public SyncRecord getFirstSyncRecordInQueue() throws APIException {
        return getSynchronizationDAO().getFirstSyncRecordInQueue();
    }

    /**
     * @see org.openmrs.api.SynchronizationService#getSyncRecord(java.lang.String)
     */
    public SyncRecord getSyncRecord(String guid) throws APIException {
        return getSynchronizationDAO().getSyncRecord(guid);
    }

    /**
     * @see org.openmrs.api.SynchronizationService#getLatestRecord()
     */
    public SyncRecord getLatestRecord() throws APIException {
        return getSynchronizationDAO().getLatestRecord();
    }

    /**
     * @see org.openmrs.api.SynchronizationService#getSyncRecord(java.lang.String)
     */
    public SyncImportRecord getSyncImportRecord(String guid) throws APIException {
        return getSynchronizationDAO().getSyncImportRecord(guid);
    }

    /**
     * @see org.openmrs.api.SynchronizationService#getSyncRecords()
     */
    public List<SyncRecord> getSyncRecords() throws APIException {
        return getSynchronizationDAO().getSyncRecords();
    }

    /**
     * @see org.openmrs.api.SynchronizationService#getSyncRecords(org.openmrs.synchronization.engine.SyncRecordState)
     */
    public List<SyncRecord> getSyncRecords(SyncRecordState state)
            throws APIException {
        return getSynchronizationDAO().getSyncRecords(state);
    }

    /**
     * @see org.openmrs.api.SynchronizationService#getSyncRecords(org.openmrs.synchronization.engine.SyncRecordState)
     */
    public List<SyncRecord> getSyncRecords(SyncRecordState[] states)
            throws APIException {
        return this.getSyncRecords(states, false);
    }

    /**
     * @see org.openmrs.api.SynchronizationService#getSyncRecords(org.openmrs.synchronization.engine.SyncRecordState)
     */
    public List<SyncRecord> getSyncRecords(SyncRecordState[] states, boolean inverse)
            throws APIException {
        return getSynchronizationDAO().getSyncRecords(states, inverse);
    }

    /**
     * @see org.openmrs.api.SynchronizationService#updateSyncRecord(org.openmrs.synchronization.engine.SyncRecord)
     */
    public void updateSyncRecord(SyncRecord record) throws APIException {
        getSynchronizationDAO().updateSyncRecord(record);
    }

    /**
     * @see org.openmrs.api.SynchronizationService#deleteSyncRecord(org.openmrs.synchronization.engine.SyncRecord)
     */
    public void deleteSyncRecord(SyncRecord record) throws APIException {
        getSynchronizationDAO().deleteSyncRecord(record);
    }

    /**
     * @see org.openmrs.api.SynchronizationService#updateSyncRecord(org.openmrs.synchronization.engine.SyncRecord)
     */
    public void updateSyncImportRecord(SyncImportRecord record) throws APIException {
        getSynchronizationDAO().updateSyncImportRecord(record);
    }

    /**
     * @see org.openmrs.api.SynchronizationService#deleteSyncRecord(org.openmrs.synchronization.engine.SyncRecord)
     */
    public void deleteSyncImportRecord(SyncImportRecord record) throws APIException {
        getSynchronizationDAO().deleteSyncImportRecord(record);
    }

    /**
     * @see org.openmrs.api.SynchronizationService#getSyncRecordsSince(java.util.Date)
     */
    public List<SyncRecord> getSyncRecordsSince(Date from) throws APIException {
        return getSynchronizationDAO().getSyncRecordsSince(from);
    }

    /**
     * @see org.openmrs.api.SynchronizationService#getSyncRecordsBetween(java.util.Date, java.util.Date)
     */
    public List<SyncRecord> getSyncRecordsBetween(Date from, Date to)
            throws APIException {
        return getSynchronizationDAO().getSyncRecordsBetween(from, to);
    }

    /**
     * @see org.openmrs.api.SynchronizationService#getGlobalProperty(java.lang.String)
     */
    public String getGlobalProperty(String propertyName) throws APIException {
        return getSynchronizationDAO().getGlobalProperty(propertyName);
    }
    
    /**
     * @see org.openmrs.api.SynchronizationService#setGlobalProperty(String propertyName, String propertyValue)
     */
    public void setGlobalProperty(String propertyName, String propertyValue) 
        throws APIException {
        getSynchronizationDAO().setGlobalProperty(propertyName, propertyValue);
    }

    /**
     * @see org.openmrs.api.SynchronizationService#createRemoteServer(org.openmrs.synchronization.engine.RemoteServer)
     */
    public void createRemoteServer(RemoteServer server) throws APIException {
        getSynchronizationDAO().createRemoteServer(server);
    }
    
    /**
     * @see org.openmrs.api.SynchronizationService#updateRemoteServer(org.openmrs.synchronization.engine.RemoteServer)
     */
    public void updateRemoteServer(RemoteServer server) throws APIException {
        getSynchronizationDAO().updateRemoteServer(server);
    }

    /**
     * @see org.openmrs.api.SynchronizationService#deleteRemoteServer(org.openmrs.synchronization.engine.RemoteServer)
     */
    public void deleteRemoteServer(RemoteServer server) throws APIException {
        getSynchronizationDAO().deleteRemoteServer(server);
    }
    
    public RemoteServer getRemoteServer(Integer serverId) throws APIException {
        return getSynchronizationDAO().getRemoteServer(serverId);
    }

    public List<RemoteServer> getRemoteServers() throws APIException {
        return getSynchronizationDAO().getRemoteServers();
    }

    public RemoteServer getParentServer() throws APIException {
        return getSynchronizationDAO().getParentServer();
    }

}
