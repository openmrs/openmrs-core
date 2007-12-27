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

import java.lang.reflect.Field;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.openmrs.api.SynchronizationIngestService;
import org.openmrs.api.context.Context;
import org.openmrs.synchronization.SyncConstants;
import org.openmrs.synchronization.SyncItemState;
import org.openmrs.synchronization.SyncRecordState;
import org.openmrs.synchronization.SyncUtil;
import org.openmrs.synchronization.Synchronizable;
import org.openmrs.synchronization.engine.SyncItem;
import org.openmrs.synchronization.engine.SyncRecord;
import org.openmrs.synchronization.ingest.SyncImportItem;
import org.openmrs.synchronization.ingest.SyncImportRecord;
import org.openmrs.synchronization.ingest.SyncItemIngestException;
import org.openmrs.synchronization.server.RemoteServer;
import org.openmrs.synchronization.server.RemoteServerType;
import org.openmrs.synchronization.server.SyncServerRecord;
import org.w3c.dom.NodeList;

public class SynchronizationIngestServiceImpl implements SynchronizationIngestService {

    private Log log = LogFactory.getLog(this.getClass());
     
    /**
     * 
     * @see org.openmrs.api.SynchronizationIngestService#processSyncImportRecord(SyncImportRecord importRecord)
     * 
     * @param importRecord
     * @throws APIException
     */
    public void processSyncImportRecord(SyncImportRecord importRecord, RemoteServer server) throws APIException {
        if ( importRecord != null ) {
            if ( importRecord.getGuid() != null && importRecord.getState() != null ) {
                SyncRecord record = Context.getSynchronizationService().getSyncRecordByOriginalGuid(importRecord.getGuid());
                if ( server.getServerType().equals(RemoteServerType.PARENT) ) {
                    // with parents, we set the actual state of the record
                    if ( importRecord.getState().equals(SyncRecordState.ALREADY_COMMITTED) ) record.setState(SyncRecordState.COMMITTED);
                    else if ( importRecord.getState().equals(SyncRecordState.NOT_SUPPOSED_TO_SYNC) ) record.setState(SyncRecordState.REJECTED);
                    else record.setState(importRecord.getState());
                } else {
                    // with non-parents we set state in the server-record
                    SyncServerRecord serverRecord = record.getServerRecord(server);
                    if ( importRecord.getState().equals(SyncRecordState.ALREADY_COMMITTED) ) serverRecord.setState(SyncRecordState.COMMITTED);
                    else if ( importRecord.getState().equals(SyncRecordState.NOT_SUPPOSED_TO_SYNC) ) serverRecord.setState(SyncRecordState.REJECTED);
                    else serverRecord.setState(importRecord.getState());
                }
                
                Context.getSynchronizationService().updateSyncRecord(record);
            }
        }        
    }
    
    /**
     * 
     * TODO
     * 
     * @param record
     * @return
     */
    public SyncImportRecord processSyncRecord(SyncRecord record, RemoteServer server) throws APIException {
        SyncImportRecord importRecord = new SyncImportRecord();
        importRecord.setState(SyncRecordState.FAILED);  // by default, until we know otherwise
        importRecord.setRetryCount(record.getRetryCount());
        importRecord.setTimestamp(record.getTimestamp());
        
        try {
            // first, let's see if this server even accepts this kind of syncRecord
            if ( !server.getClassesReceived().containsAll(record.getContainedClassSet())) {
                importRecord.setState(SyncRecordState.NOT_SUPPOSED_TO_SYNC);
                log.warn("\nNOT INGESTING RECORD with " + record.getContainedClasses() + " BECAUSE SERVER IS NOT READY TO ACCEPT ALL CONTAINED OBJECTS\n");
            } else {
                //log.warn("\nINGESTING ALL CLASSES: " + recordClasses + " BECAUSE SERVER IS READY TO ACCEPT ALL");
                // second, let's see if this SyncRecord has already been imported
                // use the original record id to locate import_record copy
                log.warn("AT THIS POINT, ORIGINALGUID FOR RECORD IS " + record.getOriginalGuid());
                importRecord = Context.getSynchronizationService().getSyncImportRecord(record.getOriginalGuid());
                boolean isUpdateNeeded = false;
                
                if ( importRecord == null ) {
                    isUpdateNeeded = true;
                    importRecord = new SyncImportRecord(record);
                    importRecord.setGuid(record.getOriginalGuid());
                    Context.getSynchronizationService().createSyncImportRecord(importRecord);
                    log.warn("ImportRecord was just created");
                } else {
                    log.warn("ImportRecord already exists and has state: " + importRecord.getState());
                    SyncRecordState state = importRecord.getState();
                    if ( state.equals(SyncRecordState.COMMITTED) ) {
                        // apparently, the remote/child server exporting to this server doesn't realize it's
                        // committed, so let's remind by sending back this import record with already_committed
                        importRecord.setState(SyncRecordState.ALREADY_COMMITTED);
                    } else if (state.equals(SyncRecordState.FAILED)) {
                        // apparently, this record was already sent and full-failed - let's not attempt again
                        // TODO: eventually we should allow you to override this with a -force option
                    }else {
                        isUpdateNeeded = true;
                    }
                }
                
                if ( isUpdateNeeded ) {
                    
                    boolean isError = false;
                    
                    // set transaction boundaries
                    //Context.openSession();
        
                    // for each sync item, process it and insert/update the database
                    for ( SyncItem item : record.getItems() ) {
                        // this could be done differently - just passing actual item to processSyncItem
                        String syncItem = item.getContent();
                        SyncImportItem importedItem = this.processSyncItem(syncItem, record.getOriginalGuid() + "|" + server.getGuid());
                        importedItem.setKey(item.getKey());
                        importRecord.addItem(importedItem);
                        if ( !importedItem.getState().equals(SyncItemState.SYNCHRONIZED)) isError = true;
                        //importRecord.setResultingRecordGuid(resultingRecordGuid);
                    }
                    if ( !isError ) {
                        importRecord.setState(SyncRecordState.COMMITTED);
                        
                        // now that we know there's no error, we have to prevent this change from being sent back to the originating server
                        /*
                         * This actually can't be done here, since hibernate may not yet commit the record - instead we have to get hacky
                        SyncRecord newRecord = Context.getSynchronizationService().getSyncRecord(record.getOriginalGuid());
                        if ( newRecord != null ) {
                            if ( server.getServerType().equals(RemoteServerType.PARENT)) {
                                newRecord.setState(SyncRecordState.COMMITTED);
                            } else {
                                SyncServerRecord serverRecord = newRecord.getServerRecord(server);
                                if ( serverRecord != null ) {
                                    serverRecord.setState(SyncRecordState.COMMITTED);
                                    
                                } else {
                                    log.warn("No server record was created for server " + server.getNickname() + " and record " + record.getOriginalGuid());
                                }
                            }
                            Context.getSynchronizationService().updateSyncRecord(newRecord);

                        } else {
                            log.warn("Can't find newly created record on system by originalGuid" + record.getOriginalGuid());
                        }
                        */
                    } else {
                        // rollback!!
                        // also, set to failure.  if we've come this far and record failed to commit, it will likely never commit
                        importRecord.setState(SyncRecordState.FAILED);
                    }
                    // finish the transaction
                    //Context.closeSession();
                    
                    Context.getSynchronizationService().updateSyncImportRecord(importRecord);
                }
            }
        } catch (Exception e ) {
            e.printStackTrace();
        }

        return importRecord;
    }
    
    public SyncImportItem processSyncItem(String incoming, String originalGuid)  throws APIException {
        SyncImportItem ret = new SyncImportItem();
        ret.setContent(incoming);
        ret.setState(SyncItemState.UNKNOWN);

        try {
            Object o = null;
            String className = null;
            boolean isUpdateNotCreate = false;
            ArrayList<Field> allFields = null;
            NodeList nodes = null;
            
            try {
                if (log.isDebugEnabled())
                    log.debug("STARTING TO PROCESS: " + incoming);
                
                o = SyncUtil.getRootObject(incoming);
                className = o.getClass().getName();
                allFields = SyncUtil.getAllFields(o);  // get fields, both in class and superclass - we'll need to know what type each field is
                nodes = SyncUtil.getChildNodes(incoming);  // get all child nodes of the root object
            } catch (Exception e) {
            	e.printStackTrace();
                throw new SyncItemIngestException(SyncConstants.ERROR_ITEM_BADXML_ROOT, null, incoming);
            }

            if ( o != null && className != null && allFields != null && nodes != null ) {
                String guid = SyncUtil.getAttribute(nodes, "guid", allFields);
                Object objOld = SyncUtil.getOpenmrsObj(className, guid);
                if ( objOld != null ) {
                    o = objOld;
                    isUpdateNotCreate = true;
                }
                
                for ( int i = 0; i < nodes.getLength(); i++ ) {
                    try {
                        SyncUtil.setProperty(o, nodes.item(i), allFields);
                    } catch ( Exception e ) {
                    	log.error("Error when trying to set " + nodes.item(i).getNodeName() + ", which is a " + className);
                    	e.printStackTrace();
                        throw new SyncItemIngestException(SyncConstants.ERROR_ITEM_UNSET_PROPERTY, nodes.item(i).getNodeName() + "," + className, incoming);
                    }
                }
                // now try to commit this fully inflated object
                try {
                    log.warn("setting lastRecordGuid to " + originalGuid);
                    ((Synchronizable)o).setLastRecordGuid(originalGuid);
                    SyncUtil.updateOpenmrsObject(o, className, guid, isUpdateNotCreate);
                    ret.setState(SyncItemState.SYNCHRONIZED);
                } catch ( Exception e ) {
                	e.printStackTrace();
                    throw new SyncItemIngestException(SyncConstants.ERROR_ITEM_NOT_COMMITTED, className, incoming);
                }
                
                if (log.isDebugEnabled())
                    log.debug("We now have an object " + o.getClass().getName() + " to INSERT with possible GUID of " + ((Synchronizable)o).getGuid());
                
            } else {
                throw new SyncItemIngestException(SyncConstants.ERROR_ITEM_NOCLASS, className, incoming);
            }
        } catch (SyncItemIngestException siie) {
            ret.setErrorMessage(siie.getItemError());
            ret.setErrorMessageArgs(siie.getItemErrorArgs());
            ret.setState(SyncItemState.CONFLICT);
        } catch (Exception e) {
            ret.setErrorMessage(SyncConstants.ERROR_ITEM_NOT_PROCESSED);
        }       
        
        return ret;        
    }
}
