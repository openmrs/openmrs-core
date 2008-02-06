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
package org.openmrs.synchronization;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.synchronization.engine.SyncRecord;
import org.openmrs.synchronization.engine.SyncSource;
import org.openmrs.synchronization.engine.SyncSourceJournal;
import org.openmrs.synchronization.engine.SyncStrategyFile;
import org.openmrs.synchronization.engine.SyncTransmission;
import org.openmrs.synchronization.ingest.SyncImportRecord;
import org.openmrs.synchronization.ingest.SyncIngestException;
import org.openmrs.synchronization.ingest.SyncTransmissionResponse;
import org.openmrs.synchronization.server.ConnectionResponse;
import org.openmrs.synchronization.server.RemoteServer;
import org.openmrs.synchronization.server.RemoteServerType;
import org.openmrs.synchronization.server.ServerConnection;
import org.openmrs.synchronization.server.SyncServerRecord;

/**
 *
 */
public class SyncUtilTransmission {

	private static Log log = LogFactory.getLog(SyncUtil.class);		


	/*
	 * TODO: consolidate this method with createSyncTransmission(RemoteServer server); has unnecessary dulication of
	 * logic
	 * this method is called when sync via file is used
	 * createSyncTransmission(RemoteServer server) is called when send via Web or scheduled tx are used
	 */
    public static SyncTransmission createSyncTransmission() {
        SyncTransmission tx = null;

        try {
            SyncSource source = new SyncSourceJournal();
            SyncStrategyFile strategy = new SyncStrategyFile();

            try {
                tx = strategy.createStateBasedSyncTransmission(source, true);
            } catch (Exception e) {
                e.printStackTrace();

                // difference is that this time we'll do this without trying to create a file (just getting the output)
                // if it works, that probably means that there was a problem writing file to disk
                tx = strategy.createStateBasedSyncTransmission(source, false);
            } finally {
                if ( tx != null ) {
                    // let's update SyncRecords to reflect the fact that we now have tried to sync them, by setting state to SENT or SENT_AGAIN
                    if ( tx.getSyncRecords() != null ) {
                        for ( SyncRecord record : tx.getSyncRecords() ) {
                            record.setRetryCount(record.getRetryCount() + 1);
                            if ( record.getState().equals(SyncRecordState.NEW ) ) record.setState(SyncRecordState.SENT);
                            else record.setState(SyncRecordState.SENT_AGAIN);
                            Context.getSynchronizationService().updateSyncRecord(record);
                        }
                    }
                }
            }
        } catch ( Exception e ) {
            e.printStackTrace();
            tx = null;
        }

        return tx;
    }

    public static SyncTransmission createSyncTransmissionRequest(RemoteServer server) {
        SyncTransmission tx = null;
        
        try {
            SyncSource source = new SyncSourceJournal();
            tx = new SyncTransmission(source.getSyncSourceGuid(), true);
            if ( server.getGuid() != null ) {
                tx.setSyncTargetGuid(server.getGuid());
            }
            tx.create(false);
        } catch ( Exception e ) {
            e.printStackTrace();
            tx = null;
        }

        return tx;
    }
    
    /**
     * Prepares a sync transmission containing local changes to be send to the remote server.
     * 
     * @param server
     * @return
     */
    public static SyncTransmission createSyncTransmission(RemoteServer server) {
        SyncTransmission tx = null;
        long maxRetryCount = 0;
        boolean maxRetryCountReached = false;

        try {
            SyncSource source = new SyncSourceJournal();
            SyncStrategyFile strategy = new SyncStrategyFile();

            try {
                tx = strategy.createStateBasedSyncTransmission(source, true, server);
            } catch (Exception e) {
                e.printStackTrace();

                // difference is that this time we'll do this without trying to create a file (just getting the output)
                // if it works, that probably means that there was a problem writing file to disk
                tx = strategy.createStateBasedSyncTransmission(source, false, server);
            } finally {
                if ( tx != null ) {
                    if ( server != null ) {
                        tx.setSyncTargetGuid(server.getGuid());
                    }
                    // let's update SyncRecords to reflect the fact that we now have tried to sync them, by setting state to SENT or SENT_AGAIN
                    maxRetryCount = Long.parseLong(Context.getAdministrationService().getGlobalProperty(SyncConstants.PROPERTY_NAME_MAX_RETRY_COUNT));
                    if ( tx.getSyncRecords() != null ) {
                        for ( SyncRecord record : tx.getSyncRecords() ) {
                        	//if max re-try was reached stop now: 
                        	//a) mark the record as failed save it to DB, 
                        	//b) clear out the Tx we are creating
                        	//c) decrement the retry count on records 'after' the one that failed -- they never really got a chance
                        	//  and if offending record is fixed, the dudes that follow would fail with max retry error
                        	if (record.getRetryCount() >= maxRetryCount) {
                        		record.setState(SyncRecordState.FAILED_AND_STOPPED);
                        		Context.getSynchronizationService().updateSyncRecord(record);
                        		maxRetryCountReached = true;
                        		continue;
                        	}
                            if ( record.getServerRecords() != null && !server.getServerType().equals(RemoteServerType.PARENT)) {
                            	//parent -> child: this Tx is part of exchange where where parent is sending its changes down to child
                            	//mark row in the synchronization_server_record table as being sent, 
                                for ( SyncServerRecord serverRecord : record.getServerRecords() ) {
                                    if ( serverRecord.getSyncServer().equals(server)) {
                                        serverRecord.setRetryCount(serverRecord.getRetryCount() + 1);
                                        if ( serverRecord.getState().equals(SyncRecordState.NEW ) ) serverRecord.setState(SyncRecordState.SENT);
                                        else serverRecord.setState(SyncRecordState.SENT_AGAIN);
                                    }
                                }
                                Context.getSynchronizationService().updateSyncRecord(record);
                            } else if ( server.getServerType().equals(RemoteServerType.PARENT)) {
                            	//child -> parent scenario: we are about to send data from child to parent
                                record.setRetryCount(record.getRetryCount() + 1);
                                if ( record.getState().equals(SyncRecordState.NEW ) ) record.setState(SyncRecordState.SENT);
                                else record.setState(SyncRecordState.SENT_AGAIN);
                                Context.getSynchronizationService().updateSyncRecord(record);
                            } else {
                                log.error("Odd state: trying to get syncRecords for a non-parent server with no corresponding server-records");
                            }
                        }
                        if (tx.getIsMaxRetryReached() || maxRetryCountReached) {
                        	tx.setSyncRecords(null);
                        }
                    }
                }
            }
        } catch ( Exception e ) {
            e.printStackTrace();
            if (SyncUtil.getSyncStatus() == SyncStatusState.ENABLED_STRICT) {
                throw(new SyncException("Error while performing synchronization, see log messages and callstack.", e));
            }
        }

        return tx;
    }

    public static SyncTransmissionResponse sendSyncTranssmission() {
        // sends to parent server (by default)
        SyncTransmissionResponse response = new SyncTransmissionResponse();
        response.setErrorMessage(SyncConstants.ERROR_NO_PARENT_DEFINED.toString());
        response.setFileName(SyncConstants.FILENAME_NO_PARENT_DEFINED);
        response.setGuid(SyncConstants.GUID_UNKNOWN);
        response.setState(SyncTransmissionState.NO_PARENT_DEFINED);
        
        RemoteServer parent = Context.getSynchronizationService().getParentServer();
        if ( parent != null ) {
            response = SyncUtilTransmission.sendSyncTranssmission(parent); 
        }
                
        return response;
    }
        
    public static SyncTransmissionResponse sendSyncTranssmission(RemoteServer server) {

        SyncTransmissionResponse response = new SyncTransmissionResponse();
        response.setErrorMessage(SyncConstants.ERROR_TRANSMISSION_CREATION.toString());
        response.setFileName(SyncConstants.FILENAME_NOT_CREATED);
        response.setGuid(SyncConstants.GUID_UNKNOWN);
        response.setState(SyncTransmissionState.TRANSMISSION_CREATION_FAILED);
        
        try {
            if ( server != null ) {
                SyncTransmission tx = SyncUtilTransmission.createSyncTransmission(server);
                
                // record last attempt to synchronize
                server.setLastSync(new Date());
                Context.getSynchronizationService().updateRemoteServer(server);
                
                if ( tx != null ) {
                    response = SyncUtilTransmission.sendSyncTranssmission(server, tx);
                    if ( response != null ) {
                        // but let's try to find out the guid of this remote server and make sure we have it stored
                        String remoteGuid = response.getSyncTargetGuid();
                        if ( remoteGuid != null && remoteGuid.length() > 0 ) {
                            if ( !remoteGuid.equals(server.getGuid()) ) {
                                server.setGuid(remoteGuid);
                                Context.getSynchronizationService().updateRemoteServer(server);
                            }
                        }
                    }
                } // no need for handling else - the correct error messages, etc have been written already
            } else {
                response.setErrorMessage(SyncConstants.ERROR_INVALID_SERVER.toString());
                response.setFileName(SyncConstants.FILENAME_INVALID_SERVER);
                response.setGuid(SyncConstants.GUID_UNKNOWN);
                response.setState(SyncTransmissionState.INVALID_SERVER);                
            }
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        
        return response;
    }
        
    public static SyncTransmissionResponse sendSyncTranssmission(RemoteServer server, SyncTransmission transmission) {
        return SyncUtilTransmission.sendSyncTranssmission(server, transmission, null);
    }

    public static SyncTransmissionResponse sendSyncTranssmission(RemoteServer server, SyncTransmission transmission, SyncTransmissionResponse responseInstead) {
        SyncTransmissionResponse response = new SyncTransmissionResponse();
        response.setErrorMessage(SyncConstants.ERROR_SEND_FAILED.toString());
        response.setFileName(SyncConstants.FILENAME_SEND_FAILED);
        response.setGuid(SyncConstants.GUID_UNKNOWN);
        response.setState(SyncTransmissionState.SEND_FAILED);
        
        try {
        	//handle the case of getting to too many retries
        	if(transmission != null) if(transmission.getIsMaxRetryReached()) {
        		response.setState(SyncTransmissionState.MAX_RETRY_REACHED);
        		return response;
        	}
        	
            if ( server != null ) {
                String toTransmit = null;
                if ( responseInstead != null ) {
                    toTransmit = responseInstead.getFileOutput();
                    log.warn("Sending a response (with tx inside): " + toTransmit);
                } else if ( transmission != null ){
                    toTransmit = transmission.getFileOutput();
                    log.warn("Sending an actual tx: " + toTransmit);
                }
                
                if ( toTransmit != null && toTransmit.length() > 0 ) {
                    if ( responseInstead == null && transmission != null 
                            && transmission.getSyncRecords() != null && transmission.getSyncRecords().size() == 0 ) {
                        response.setState(SyncTransmissionState.OK_NOTHING_TO_DO);
                        response.setErrorMessage("");
                        response.setFileName(transmission.getFileName() + SyncConstants.RESPONSE_SUFFIX);
                        response.setGuid(transmission.getGuid());
                        response.setTimestamp(transmission.getTimestamp());
                    } else {
                        ConnectionResponse connResponse = null;
                        boolean isResponse = responseInstead != null;

                        try {
                            connResponse = ServerConnection.sendExportedData(server, toTransmit, isResponse);
                        } catch ( Exception e ) {
                            e.printStackTrace();
                            // no need to change state or error message - it's already set properly
                        }

                        if ( connResponse != null ) {
                            // constructor for SyncTransmissionResponse is null-safe
                            response = new SyncTransmissionResponse(connResponse);
                            
                            if ( response.getSyncImportRecords() == null ) {
                                log.debug("No records to process in response");
                            } else {
                                // process each incoming syncImportRecord
                                for ( SyncImportRecord importRecord : response.getSyncImportRecords() ) {
                                    Context.getSynchronizationIngestService().processSyncImportRecord(importRecord, server);
                                }
                            }
                        }
                    }
                } else {
                    response.setErrorMessage(SyncConstants.ERROR_TRANSMISSION_CREATION.toString());
                    response.setFileName(SyncConstants.FILENAME_NOT_CREATED);
                    response.setGuid(SyncConstants.GUID_UNKNOWN);
                    response.setState(SyncTransmissionState.TRANSMISSION_CREATION_FAILED);
                }
            } else {
                if ( server == null ) {
                    response.setErrorMessage(SyncConstants.ERROR_INVALID_SERVER.toString());
                    response.setFileName(SyncConstants.FILENAME_INVALID_SERVER);
                    response.setGuid(SyncConstants.GUID_UNKNOWN);
                    response.setState(SyncTransmissionState.INVALID_SERVER);                
                } else if ( transmission == null ) {
                    response.setErrorMessage(SyncConstants.ERROR_TRANSMISSION_CREATION.toString());
                    response.setFileName(SyncConstants.FILENAME_NOT_CREATED);
                    response.setGuid(SyncConstants.GUID_UNKNOWN);
                    response.setState(SyncTransmissionState.TRANSMISSION_CREATION_FAILED);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return response;
    }    

    @Deprecated
    private static SyncTransmissionResponse sendSyncTranssmissionRequest(RemoteServer server) {

        SyncTransmissionResponse response = new SyncTransmissionResponse();
        response.setErrorMessage(SyncConstants.ERROR_TRANSMISSION_CREATION.toString());
        response.setFileName(SyncConstants.FILENAME_NOT_CREATED);
        response.setGuid(SyncConstants.GUID_UNKNOWN);
        response.setState(SyncTransmissionState.TRANSMISSION_CREATION_FAILED);
        
        try {
            if ( server != null ) {
                SyncTransmission tx = SyncUtilTransmission.createSyncTransmissionRequest(server);
                
                // record last attempt to synchronize
                server.setLastSync(new Date());
                Context.getSynchronizationService().updateRemoteServer(server);
                
                if ( tx != null ) {
                    response = SyncUtilTransmission.sendSyncTranssmission(server, tx);
                    if ( response != null ) {
                        //  let's try to find out the guid of this remote server and make sure we have it stored
                        String remoteGuid = response.getSyncTargetGuid();
                        if ( remoteGuid != null && remoteGuid.length() > 0 ) {
                            if ( !remoteGuid.equals(server.getGuid()) ) {
                                server.setGuid(remoteGuid);
                                Context.getSynchronizationService().updateRemoteServer(server);
                            }
                        }
                    }
                } // no need for handling else - the correct error messages, etc have been written already
            } else {
                response.setErrorMessage(SyncConstants.ERROR_INVALID_SERVER.toString());
                response.setFileName(SyncConstants.FILENAME_INVALID_SERVER);
                response.setGuid(SyncConstants.GUID_UNKNOWN);
                response.setState(SyncTransmissionState.INVALID_SERVER);                
            }
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        
        return response;
    }


    /**
     * 
     * Performs 'full' synchronization (from a child perspective) with parent server identified by parent parameter.
     *  
     * @param parent
     * @return
     */
    public static SyncTransmissionResponse doFullSynchronize(RemoteServer parent) {
        SyncTransmissionResponse response = new SyncTransmissionResponse();
        response.setErrorMessage(SyncConstants.ERROR_TRANSMISSION_CREATION.toString());
        response.setFileName(SyncConstants.FILENAME_NOT_CREATED);
        response.setGuid(SyncConstants.GUID_UNKNOWN);
        response.setState(SyncTransmissionState.TRANSMISSION_CREATION_FAILED);
        
        try {
            if ( parent != null ) {
            	
            	//this is the initial handshake only; no state sent
                SyncTransmission tx = SyncUtilTransmission.createSyncTransmissionRequest(parent);
                
                if ( tx != null ) {
                	if (log.isInfoEnabled()) {
                		log.info("sync tx created was: " + tx.getFileOutput());
                	}
                    // start by sending request to parent server
                    SyncTransmissionResponse initialResponse = SyncUtilTransmission.sendSyncTranssmission(parent, tx);
                    if ( initialResponse != null ) {
                        // get syncTx from that response, and process it
                        SyncTransmission initialTxFromParent = initialResponse.getSyncTransmission();
                        SyncTransmissionResponse str = null;
                        if ( initialTxFromParent != null ) {
                            // since we know what server this should be from, 
                        	//let's check to make sure we've got the guid - we'll need it later
                            String remoteGuid = initialTxFromParent.getSyncSourceGuid();
                            if ( parent.getGuid() == null ) {
                            	parent.setGuid(remoteGuid);
                                Context.getSynchronizationService().updateRemoteServer(parent);
                            }
                            
                            // process syncTx from parent, and generate response
                            // tx may be null - meaning no updates from parent
                            str = SyncUtilTransmission.processSyncTransmission(initialTxFromParent);
                        } else {
                            log.warn("initialTxFromParent was null coming back from parent(?)");
                            initialResponse.createFile(true, "requestResponse");
                            log.warn("response was: " + initialResponse.getFileOutput());
                        }

                        // now get local changes destined for parent, and package those inside
                        SyncTransmission st = SyncUtilTransmission.createSyncTransmission(parent);
                        if ( str != null ) {
                            log.info("Received updates from parent, so replying and sending updates of our own: " + st.getFileOutput());
                            str.setSyncTransmission(st);
                            str.createFile(true, "/receiveAndSend");
                            response = SyncUtilTransmission.sendSyncTranssmission(parent, null, str);
                        } else {
                            log.info("No updates from parent, generating our own transmission");
                            response = SyncUtilTransmission.sendSyncTranssmission(parent, st, null);
                        }

                    } else {
                        log.warn("INITIAL RESPONSE CAME BACK AS NULL IN DOFULLSYNCHRONIZATION(SERVER)");
                        log.warn("TX was: " + tx.getFileOutput());
                    }
                } else {
                    log.warn("SEEMS WE COULND'T CREATE A NEW SYNC TRANMISSION FOR SERVER: " + parent.getNickname());
                    // no need for handling else - the correct error messages, etc have been written already
                }
            } else {
                response.setErrorMessage(SyncConstants.ERROR_INVALID_SERVER.toString());
                response.setFileName(SyncConstants.FILENAME_INVALID_SERVER);
                response.setGuid(SyncConstants.GUID_UNKNOWN);
                response.setState(SyncTransmissionState.INVALID_SERVER);                
            }
        } catch ( Exception e ) {
            e.printStackTrace();
            if (SyncUtil.getSyncStatus() == SyncStatusState.ENABLED_STRICT) {
                throw(new SyncException("Error while performing synchronization, see log messages and callstack.", e));
            }
        }
        
        return response;
    }
    
    
    
    
    public static SyncTransmissionResponse processSyncTransmission(SyncTransmission st) {
        SyncTransmissionResponse str = new SyncTransmissionResponse(st);

        //fill-in the server guid for the response AGAIN
        str.setSyncTargetGuid(Context.getSynchronizationService().getServerGuid());
        String sourceGuid = st.getSyncSourceGuid();
        RemoteServer origin = Context.getSynchronizationService().getRemoteServer(sourceGuid);

        User authenticatedUser = Context.getAuthenticatedUser();
        if ( origin == null && authenticatedUser != null ) {
            // make a last-ditch effort to try to figure out what server this is coming from, so we can behave appropriately.
            String username = authenticatedUser.getUsername();
            log.warn("CANNOT GET ORIGIN SERVER FOR THIS REQUEST, get by username " + username + " instead");
            origin = Context.getSynchronizationService().getRemoteServerByUsername(username);
            if ( origin != null && sourceGuid != null && sourceGuid.length() > 0 ) {
                // take this opportunity to save the guid, now we've identified which server this is
                origin.setGuid(sourceGuid);
                Context.getSynchronizationService().updateRemoteServer(origin);
            } else {
                log.warn("STILL UNABLE TO GET ORIGIN WITH username " + username + " and sourceguid " + sourceGuid);
            }
        } else {
            if ( origin != null ) log.warn("ORIGIN SERVER IS " + origin.getNickname());
            else log.warn("ORIGIN SERVER IS STILL NULL");
        }
        
        List<SyncImportRecord> importRecords = new ArrayList<SyncImportRecord>();
        
        if ( st.getSyncRecords() != null ) {
        	SyncImportRecord importRecord = null;
            for ( SyncRecord record : st.getSyncRecords() ) {
            	try {
            		//pre-create import record in case we get exception
                	importRecord = new SyncImportRecord();
                    importRecord.setState(SyncRecordState.FAILED);  // by default, until we know otherwise
                    importRecord.setRetryCount(record.getRetryCount());
                    importRecord.setTimestamp(record.getTimestamp());
                    
            		importRecord = Context.getSynchronizationIngestService().processSyncRecord(record, origin);
            	} catch (SyncIngestException e) {
            		e.printStackTrace();
            		importRecord = e.getSyncImportRecord();
            	} catch (Exception e) {
            		//just report error, import record already set to failed
            		e.printStackTrace();
            	}
                importRecords.add(importRecord);
                
                //if the record update failed for any reason, do not continue on, stop now
                if (importRecord.getState() != SyncRecordState.COMMITTED && importRecord.getState() != SyncRecordState.ALREADY_COMMITTED) {
                	break;
                }
            }
        }
        if ( importRecords.size() > 0 ) str.setSyncImportRecords(importRecords);

        // now we're ready to see if we need to fire back a response transmission
        if ( origin != null ) {
            if ( !origin.getDisabled() && st.getIsRequestingTransmission() ) {
                SyncTransmission tx = SyncUtilTransmission.createSyncTransmission(origin);
                if ( tx != null ) {
                    str.setSyncTransmission(tx);
                }
            }
        }
        
        return str;
    }

    /**
     * Main method to initiate data synchronization from a child to its parent.
     *
     * @return
     * 
     * @see #doFullSynchronize(RemoteServer)
     */
    public static SyncTransmissionResponse doFullSynchronize() {
        // sends to parent server (by default)
        SyncTransmissionResponse response = new SyncTransmissionResponse();
        response.setErrorMessage(SyncConstants.ERROR_NO_PARENT_DEFINED.toString());
        response.setFileName(SyncConstants.FILENAME_NO_PARENT_DEFINED);
        response.setGuid(SyncConstants.GUID_UNKNOWN);
        response.setState(SyncTransmissionState.NO_PARENT_DEFINED);
        
        RemoteServer parent = Context.getSynchronizationService().getParentServer();
        
        if ( parent != null ) {
            response = SyncUtilTransmission.doFullSynchronize(parent); 
        }
                
        return response;
    }
}