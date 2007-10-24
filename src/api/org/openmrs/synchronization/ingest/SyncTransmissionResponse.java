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
package org.openmrs.synchronization.ingest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.serialization.FilePackage;
import org.openmrs.serialization.IItem;
import org.openmrs.serialization.Item;
import org.openmrs.serialization.Record;
import org.openmrs.serialization.TimestampNormalizer;
import org.openmrs.synchronization.SyncConstants;
import org.openmrs.synchronization.SyncTransmissionState;
import org.openmrs.synchronization.engine.SyncException;
import org.openmrs.synchronization.engine.SyncTransmission;
import org.openmrs.synchronization.server.ConnectionResponse;
import org.openmrs.synchronization.server.ServerConnectionState;

/**
 * SyncTransmission a collection of sync records to be sent to the parent.
 */
public class SyncTransmissionResponse implements IItem {

    // consts

    // fields
    private final Log log = LogFactory.getLog(getClass());
     
    private String fileName = null;
    private Date timestamp = null;
    private List<SyncImportRecord> syncImportRecords = null;
    private String guid = null;
    private String fileOutput = "";
    private SyncTransmissionState state;
    private String errorMessage;
    private String syncSourceGuid = null; //GUID of the node where the Tx came from
    private String syncTargetGuid = null; //GUID of the node where Tx is being applied to, and who is now sending a response
    private SyncTransmission syncTransmission = null;

    // constructor(s)
    public SyncTransmissionResponse() {
    	
    }

    public SyncTransmission getSyncTransmission() {
        return syncTransmission;
    }

    public void setSyncTransmission(SyncTransmission syncTransmission) {
        this.syncTransmission = syncTransmission;
    }

    /* 
     * Take passed in records and create a new sync_tx file
     */
    
    public SyncTransmissionResponse(SyncTransmission transmission) {
    	// needs to be null-safe
    	if ( transmission != null ) {
        	this.guid = transmission.getGuid();
            this.syncSourceGuid = transmission.getSyncSourceGuid();
            this.syncTargetGuid = SyncConstants.GUID_UNKNOWN;
        	fileName = transmission.getFileName();
        	int idx = fileName.lastIndexOf(".");
        	if ( idx > -1 ) fileName = fileName.substring(0, idx) + SyncConstants.RESPONSE_SUFFIX + fileName.substring(idx);
        	else fileName = fileName + SyncConstants.RESPONSE_SUFFIX;
        	this.state = SyncTransmissionState.OK;  // even though we really mean "OK so far" - it'll get overwritten later if there's a prob
    	} else {
    		this.guid = SyncConstants.GUID_UNKNOWN;
            this.syncSourceGuid = SyncConstants.GUID_UNKNOWN;
            this.syncTargetGuid = SyncConstants.GUID_UNKNOWN;
    		this.errorMessage = SyncConstants.ERROR_TX_NOT_UNDERSTOOD;
    		this.fileName = SyncConstants.FILENAME_TX_NOT_UNDERSTOOD;
    		this.state = SyncTransmissionState.TRANSMISSION_NOT_UNDERSTOOD;
    	}
    }

    /**
     * @param connResponse
     */
    public SyncTransmissionResponse(ConnectionResponse connResponse) {
	    // this needs to be bulletproof
    	if ( connResponse != null ) {
    		
    		System.out.println("RESPONSE PAYLOAD IS: " + connResponse.getResponsePayload());
    		
    		if ( connResponse.getState().equals(ServerConnectionState.OK) ) {
    			try {
    				// this method is null safe
    				SyncTransmissionResponse str = SyncDeserializer.xmlToSyncTransmissionResponse(connResponse.getResponsePayload());
    				this.errorMessage = str.getErrorMessage();
    				this.fileName = str.getFileName();
    				this.guid = str.getGuid();
                    this.syncSourceGuid = str.getSyncSourceGuid();
                    this.syncTargetGuid = str.getSyncTargetGuid();
    				this.state = str.getState();
    				this.syncImportRecords = str.getSyncImportRecords();
                    this.syncTransmission = str.getSyncTransmission();
    			} catch (Exception e) {
    				e.printStackTrace();
    	    		this.errorMessage = SyncConstants.ERROR_RESPONSE_NOT_UNDERSTOOD.toString();
    	        	this.fileName = SyncConstants.FILENAME_RESPONSE_NOT_UNDERSTOOD;
    	        	this.guid = SyncConstants.GUID_UNKNOWN;
                    this.syncSourceGuid = SyncConstants.GUID_UNKNOWN;
                    this.syncTargetGuid = SyncConstants.GUID_UNKNOWN;
    	        	this.state = SyncTransmissionState.RESPONSE_NOT_UNDERSTOOD;
    			} 
    		} else {
        		this.errorMessage = SyncConstants.ERROR_SEND_FAILED.toString();
            	this.fileName = SyncConstants.FILENAME_SEND_FAILED;
            	this.guid = SyncConstants.GUID_UNKNOWN;
                this.syncSourceGuid = SyncConstants.GUID_UNKNOWN;
                this.syncTargetGuid = SyncConstants.GUID_UNKNOWN;
            	this.state = SyncTransmissionState.SEND_FAILED;
            	if ( connResponse.getState().equals(ServerConnectionState.MALFORMED_URL)) this.state = SyncTransmissionState.MALFORMED_URL;
            	if ( connResponse.getState().equals(ServerConnectionState.CERTIFICATE_FAILED)) this.state = SyncTransmissionState.CERTIFICATE_FAILED;
    		}
    	} else {
    		this.errorMessage = SyncConstants.ERROR_SEND_FAILED.toString();
        	this.fileName = SyncConstants.FILENAME_SEND_FAILED;
        	this.guid = SyncConstants.GUID_UNKNOWN;
            this.syncSourceGuid = SyncConstants.GUID_UNKNOWN;
            this.syncTargetGuid = SyncConstants.GUID_UNKNOWN;
        	this.state = SyncTransmissionState.SEND_FAILED;
    	}
    }

	public List<SyncImportRecord> getSyncImportRecords() {
    	return syncImportRecords;
    }

	public void setSyncImportRecords(List<SyncImportRecord> syncImportRecords) {
    	this.syncImportRecords = syncImportRecords;
    }

	public String getErrorMessage() {
    	return errorMessage;
    }

	public void setErrorMessage(String errorMessage) {
    	this.errorMessage = errorMessage;
    }

    public String getSyncSourceGuid() {
        return syncSourceGuid;
    }

    public void setSyncSourceGuid(String value) {
        this.syncSourceGuid = value;
    }

    public String getSyncTargetGuid() {
        return syncTargetGuid;
    }

    public void setSyncTargetGuid(String value) {
        this.syncTargetGuid = value;
    }    
    
	// methods
    public String getFileOutput() {
    	return fileOutput;
    }

    public String getFileName() {
        return fileName;
    }
    public void setFileName(String value) {
        fileName = value;
    }
    public String getGuid() {
        return guid;
    }
    public void setGuid(String value) {
        guid = value;
    }
    public Date getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(Date value) {
        timestamp = value;
    }
    
    /** Create a new transmission from records: use org.openmrs.serial to make a file
     *  also, give option to write to a file or not 
     */
    public void CreateFile(boolean writeFileToo) {
    	CreateFile(writeFileToo, SyncConstants.DIR_IMPORT);
    }
    	
    /** Create a new transmission from records: use org.openmrs.serial to make a file
     *  also, give option to write to a file or not 
     */
    public void CreateFile(boolean writeFileToo, String path) {

    	if ( path == null ) path = SyncConstants.DIR_IMPORT;
    	if ( path.length() == 0 ) path = SyncConstants.DIR_IMPORT;
    	
        try {            
            if (timestamp == null) this.timestamp = new Date(); //set timestamp of this export, if not already set
            
            FilePackage pkg = new FilePackage();
            Record xml = pkg.createRecordForWrite(this.getClass().getName());
            Item root = xml.getRootItem();

            //serialize
            this.save(xml,root);

            //now dump to file
            fileOutput = pkg.savePackage(org.openmrs.util.OpenmrsUtil
                    .getApplicationDataDirectory()
                    + "/import/" + fileName, writeFileToo);

        } catch (Exception e) {
            log.error("Cannot create sync transmission.");
            throw new SyncException("Cannot create sync transmission", e);
        }
        return;

    }

    /** IItem.save() implementation
     * 
     */
    public Item save(Record xml, Item me) throws Exception {
        //Item me = xml.createItem(parent, this.getClass().getName());
        
        //serialize primitives
        if (guid != null) xml.setAttribute(me, "guid", guid);
        if (fileName != null) xml.setAttribute(me, "fileName", fileName);
        if (state != null) xml.setAttribute(me, "state", state.toString());
        if (errorMessage != null ) xml.setAttribute(me, "errorMessage", errorMessage);
        if (syncSourceGuid != null)  xml.setAttribute(me, "syncSourceGuid", syncSourceGuid);
        if (syncTargetGuid != null)  xml.setAttribute(me, "syncTargetGuid", syncTargetGuid);
        if (timestamp != null) xml.setAttribute(me, "timestamp", new TimestampNormalizer().toString(timestamp));
        
        //serialize Records list
        Item itemsCollection = xml.createItem(me, "records");
        
        if (syncImportRecords != null) {
            me.setAttribute("itemCount", Integer.toString(syncImportRecords.size()));
            for ( SyncImportRecord importRecord : syncImportRecords ) {
            	importRecord.save(xml, itemsCollection);
            }
        }

        Item syncTx = xml.createItem(me, "syncTransmission");
        
        if (syncTransmission != null) {
            syncTransmission.save(xml, syncTx);
        }

        return me;
    }

    /** IItem.load() implementation
     * 
     */
    public void load(Record xml, Item me) throws Exception {

        this.guid = me.getAttribute("guid");
        this.fileName = me.getAttribute("fileName");
        this.syncSourceGuid = me.getAttribute("syncSourceGuid");
        this.syncTargetGuid = me.getAttribute("syncTargetGuid");

        if (me.getAttribute("timestamp") == null)
            this.timestamp = null;
        else
            this.timestamp = (Date)new TimestampNormalizer().fromString(Date.class,me.getAttribute("timestamp"));
        
        try {
        	this.state = SyncTransmissionState.valueOf(me.getAttribute("state"));
        } catch ( Exception e ) {
        	System.out.println("STATE IS [" + me.getAttribute("state") + "]");
        	this.state = SyncTransmissionState.RESPONSE_NOT_UNDERSTOOD;
        }
        this.errorMessage = me.getAttribute("errorMessage");
        
        //now get items
        Item itemsCollection = xml.getItem(me, "records");
        
        if (itemsCollection.isEmpty()) {
            this.syncImportRecords = null;
        } else {
            this.syncImportRecords = new ArrayList<SyncImportRecord>();
            List<Item> serItems = xml.getItems(itemsCollection);
            for (int i = 0; i < serItems.size(); i++) {
                Item serItem = serItems.get(i);
                SyncImportRecord syncImportRecord = new SyncImportRecord();
                syncImportRecord.load(xml, serItem);
                this.syncImportRecords.add(syncImportRecord);
            }
        }

        Item syncTx = xml.getItem(me, "syncTransmission");
        if ( syncTx.isEmpty() ) {
            this.syncTransmission = null;
        } else {
            this.syncTransmission = new SyncTransmission();
            this.syncTransmission.load(xml, syncTx);
        }
    }

	public SyncTransmissionState getState() {
    	return state;
    }

	public void setState(SyncTransmissionState state) {
    	this.state = state;
    }
}