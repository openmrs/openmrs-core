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

import java.util.ArrayList;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.openmrs.api.SynchronizationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.SynchronizationDAO;
import org.openmrs.synchronization.SyncConstants;
import org.openmrs.synchronization.SyncRecordState;
import org.openmrs.synchronization.Synchronizable;
import org.openmrs.synchronization.engine.SyncRecord;
import org.openmrs.synchronization.filter.SyncClass;
import org.openmrs.synchronization.filter.SyncServerClass;
import org.openmrs.synchronization.ingest.SyncImportRecord;
import org.openmrs.synchronization.server.RemoteServer;
import org.openmrs.synchronization.server.RemoteServerType;
import org.openmrs.synchronization.server.SyncServerRecord;

public class SynchronizationServiceImpl implements SynchronizationService {

    private SynchronizationDAO dao;
    private final Log log = LogFactory.getLog(getClass());

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
        this.createSyncRecord(record, record.getOriginalGuid());
    }
    
    public void createSyncRecord(SyncRecord record, String originalGuidPassed) throws APIException {

        if ( record != null ) {
            // here is a hack to get around the fact that hibernate decides to commit transaction when it feels like it
            // otherwise, we could run this in the ingest methods
            RemoteServer origin = null;
            int idx = originalGuidPassed.indexOf("|"); 
            if ( idx > -1 ) {
                log.warn("originalPassed is " + originalGuidPassed);
                String originalGuid = originalGuidPassed.substring(0, idx);
                String serverGuid = originalGuidPassed.substring(idx + 1);
                log.warn("serverGuid is " + serverGuid + ", and originalGuid is " + originalGuid);
                record.setOriginalGuid(originalGuid);
                origin = Context.getSynchronizationService().getRemoteServer(serverGuid);
                if ( origin != null ) {
                    if ( origin.getServerType().equals(RemoteServerType.PARENT) ) {
                        record.setState(SyncRecordState.COMMITTED);
                    }
                } else {
                    log.warn("Could not get remote server by guid: " + serverGuid);
                }
            }
            
            // before creation, we need to make sure that we create matching entries for each server (server-record relationship)
            Set<SyncServerRecord> serverRecords = record.getServerRecords();
            if ( serverRecords == null ) {
                log.warn("IN createSyncRecord(), SERVERRECORDS ARE NULL, SO SETTING DEFAULTS");
                serverRecords = new HashSet<SyncServerRecord>();
                List<RemoteServer> servers = this.getRemoteServers();
                if ( servers != null ) {
                    for ( RemoteServer server : servers ) {
                        // we only need to create extra server-records for servers that are NOT the parent - the parent state is kept in the actual sync record
                        if ( !server.getServerType().equals(RemoteServerType.PARENT)) {
                            SyncServerRecord serverRecord = new SyncServerRecord(server, record);
                            if ( server.equals(origin) ) {
                                log.warn("this record came from server " + origin.getNickname() + ", so we will set its status to commmitted");
                                serverRecord.setState(SyncRecordState.COMMITTED);
                            }
                            serverRecords.add(serverRecord);
                        }
                    }
                }
                record.setServerRecords(serverRecords);
                //server.setServerClasses(serverClasses);
            }
            
            getSynchronizationDAO().createSyncRecord(record);
        }
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

    public SyncRecord getSyncRecordByOriginalGuid(String originalGuid) throws APIException {
        return getSynchronizationDAO().getSyncRecordByOriginalGuid(originalGuid);
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
    public List<SyncRecord> getSyncRecords(SyncRecordState[] states, RemoteServer server)
            throws APIException {
    	List<SyncRecord> temp = null;
        List<SyncRecord> ret = null; 
        
        if ( server != null ) {
            if ( server.getServerType().equals(RemoteServerType.PARENT ) ) {
                ret = this.getSyncRecords(states);
            } else {
                ret = getSynchronizationDAO().getSyncRecords(states, false, server);
            }
        }
        
        // filter out classes that are not supposed to be sent to the specified server
        // and update their status
        if ( ret != null ) {
            temp = new ArrayList<SyncRecord>();
            for ( SyncRecord record : ret ) {
                if ( server.getClassesSent().containsAll(record.getContainedClassSet()) ) {
                    record.setForServer(server);
                    temp.add(record);
                    
                } else {
                    log.warn("Omitting record with " + record.getContainedClasses() + " for server: " + server.getNickname() + " with server type: " + server.getServerType());
                    if ( server.getServerType().equals(RemoteServerType.PARENT)) {
                        record.setState(SyncRecordState.NOT_SUPPOSED_TO_SYNC);
                    } else {
                        // if not the parent, we have to update the record for this specific server
                        Set<SyncServerRecord> records = record.getServerRecords();
                        for ( SyncServerRecord serverRecord : records ) {
                            if ( serverRecord.getSyncServer().equals(server) ) {
                                serverRecord.setState(SyncRecordState.NOT_SUPPOSED_TO_SYNC);
                            }
                        }
                        record.setServerRecords(records);
                    }
                    this.updateSyncRecord(record);
                }
            }
            ret = temp;
        }
        
        return ret;
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
        if ( server != null ) {
            Set<SyncServerClass> serverClasses = server.getServerClasses();
            if ( serverClasses == null ) {
                log.warn("IN CREATEREMOTESERVER(), SERVERCLASSES ARE NULL, SO SETTING DEFAULTS");
                serverClasses = new HashSet<SyncServerClass>();
                List<SyncClass> classes = this.getSyncClasses();
                if ( classes != null ) {
                    for ( SyncClass syncClass : classes ) {
                        SyncServerClass serverClass = new SyncServerClass(server, syncClass);
                        serverClasses.add(serverClass);
                    }
                }
                server.setServerClasses(serverClasses);
            }
            
            getSynchronizationDAO().createRemoteServer(server);
        }
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

    public RemoteServer getRemoteServer(String guid) throws APIException {
        return getSynchronizationDAO().getRemoteServer(guid);
    }

    public RemoteServer getRemoteServerByUsername(String username) throws APIException {
        return getSynchronizationDAO().getRemoteServerByUsername(username);
    }

    public List<RemoteServer> getRemoteServers() throws APIException {
        return getSynchronizationDAO().getRemoteServers();
    }

    public RemoteServer getParentServer() throws APIException {
        return getSynchronizationDAO().getParentServer();
    }

    public String getServerGuid() {   
        return Context.getAdministrationService().getGlobalProperty(SyncConstants.SERVER_GUID);
    }

    
    /**
     * @see org.openmrs.api.SynchronizationService#createSyncClass(org.openmrs.synchronization.engine.SyncClass)
     */
    public void createSyncClass(SyncClass syncClass) throws APIException {
        getSynchronizationDAO().createSyncClass(syncClass);
    }
    
    /**
     * @see org.openmrs.api.SynchronizationService#updateSyncClass(org.openmrs.synchronization.engine.SyncClass)
     */
    public void updateSyncClass(SyncClass syncClass) throws APIException {
        getSynchronizationDAO().updateSyncClass(syncClass);
    }

    /**
     * @see org.openmrs.api.SynchronizationService#deleteSyncClass(org.openmrs.synchronization.engine.SyncClass)
     */
    public void deleteSyncClass(SyncClass syncClass) throws APIException {
        getSynchronizationDAO().deleteSyncClass(syncClass);
    }
    
    public SyncClass getSyncClass(Integer syncClassId) throws APIException {
        return getSynchronizationDAO().getSyncClass(syncClassId);
    }
    
    public List<SyncClass> getSyncClasses() throws APIException {
        return getSynchronizationDAO().getSyncClasses();
    }

    /**
     * @see org.openmrs.api.SynchronizationService#createDatabaseForChild(java.lang.String, java.io.Writer)
     */
    public void createDatabaseForChild(String guidForChild, OutputStream out) throws APIException {
       getSynchronizationDAO().createDatabaseForChild(guidForChild, out); 
    }
    
    /**
     * @see org.openmrs.api.SynchronizationService#deleteSynchronizable(org.openmrs.synchronization.Synchronizable)
     */
    public void deleteSynchronizable(Synchronizable o) throws APIException {
    	getSynchronizationDAO().deleteSynchronizable(o);
    }
 
    /**
     * Changes flush sematics, delegating directly to the corresponsing DAO method.
     * 
     * @see org.openmrs.api.SynchronizationService#setFlushModeManual()
     * @see org.openmrs.api.db.hibernate.HibernateSynchronizationDAO#setFlushModeManual()
     */
    public void setFlushModeManual() throws APIException {
    	getSynchronizationDAO().setFlushModeManual();
    }
    
    /**
     * Changes flush sematics, delegating directly to the corresponsing DAO method.
     * 
     * @see org.openmrs.api.SynchronizationService#setFlushModeAutomatic()
     * @see org.openmrs.api.db.hibernate.HibernateSynchronizationDAO#setFlushModeAutomatic()
     */
    public void setFlushModeAutomatic() throws APIException {
    	getSynchronizationDAO().setFlushModeAutomatic();
    }
    
	/**
     * Performs peristence layer flush, delegating directly to the corresponsing DAO method.
     * 
     * @see org.openmrs.api.SynchronizationService#flushSession()
     * @see org.openmrs.api.db.hibernate.HibernateSynchronizationDAO#flushSession()
     */    
    public void flushSession() throws APIException {
    	getSynchronizationDAO().flushSession();
    }

    /**
     * Processes save/update to instance of Synchronizable by persisting it into local persistance store.
     * 
     * @param object instance of Synchronizable to be processed.
     * @return
     * @throws APIException
     */
    //@Authorized({"Manage Synchronization Records"})
    public void saveOrUpdate(Synchronizable object)  throws APIException {
    	getSynchronizationDAO().saveOrUpdate(object);
    }
}


