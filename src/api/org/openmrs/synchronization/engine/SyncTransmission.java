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
package org.openmrs.synchronization.engine;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.serialization.FilePackage;
import org.openmrs.serialization.IItem;
import org.openmrs.serialization.Item;
import org.openmrs.serialization.Record;
import org.openmrs.serialization.TimestampNormalizer;
import org.openmrs.synchronization.SyncConstants;
import org.openmrs.synchronization.SyncException;

/**
 * SyncTransmission a collection of sync records to be sent to the parent.
 */
public class SyncTransmission implements IItem {

    // consts

    // fields
    private final Log log = LogFactory.getLog(getClass());
     
    private String fileName = null;
    private Date timestamp = null;
    private List<SyncRecord> syncRecords = null;
    private String guid = null;
    private String fileOutput = "";
    private String syncSourceGuid = null; //this is GUID of a server where Tx is coming from
    private String syncTargetGuid = null; //this is GUID of server where Tx is headed TO
    private Boolean isRequestingTransmission = false;
    private Boolean isMaxRetryReached = false; 

    // constructor(s)
    public SyncTransmission() {
    }

    /* 
     * Create new SyncTransmission as a SyncTxRequest
     */
    public SyncTransmission(String sourceGuid, boolean isRequestingTransmission) {

        guid = UUID.randomUUID().toString();        
        fileName = "sync_tx_" + SyncConstants.SYNC_FILENAME_MASK.format(new Date()) + "_request";
        this.syncSourceGuid  = sourceGuid;
        this.isRequestingTransmission = isRequestingTransmission;
    }
    public SyncTransmission(String sourceGuid, boolean isRequestingTransmission, String targetGuid) {

        guid = UUID.randomUUID().toString();        
        fileName = "sync_tx_" + SyncConstants.SYNC_FILENAME_MASK.format(new Date()) + "_request";
        this.syncSourceGuid  = sourceGuid;
        this.syncSourceGuid  = targetGuid;
        this.isRequestingTransmission = isRequestingTransmission;
    }

    /* 
     * Take passed in records and create a new sync_tx file
     */
    public SyncTransmission(String sourceGuid, List<SyncRecord> valRecords) {
    	init(sourceGuid,valRecords, null);
    }
    public SyncTransmission(String sourceGuid, List<SyncRecord> valRecords,String targetGuid ) {
    	init(sourceGuid,valRecords,targetGuid);
    }
    private void init(String sourceGuid, List<SyncRecord> valRecords,String targetGuid) {

        guid = UUID.randomUUID().toString();        
        fileName = "sync_tx_" + SyncConstants.SYNC_FILENAME_MASK.format(new Date());
        this.syncRecords = valRecords;
        this.syncSourceGuid  = sourceGuid;
        this.syncTargetGuid  = targetGuid;
    }

    public Boolean getIsMaxRetryReached() {
        return isMaxRetryReached;
    }

    public void setIsMaxRetryReached(boolean value) {
        isMaxRetryReached = value;
    }

    public Boolean getIsRequestingTransmission() {
        return isRequestingTransmission;
    }

    public void setIsRequestingTransmission(Boolean isRequestingTransmission) {
        this.isRequestingTransmission = isRequestingTransmission;
    }

    public String getSyncSourceGuid() {
        return syncSourceGuid;
    }
    public void setSyncSourceGuid(String value) {
        syncSourceGuid = value;
    }
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
    
    public List<SyncRecord> getSyncRecords() {
        return syncRecords;
    }
    public void setSyncRecords(List<SyncRecord> value) {
        this.syncRecords = value;
    }

    /** Creates a new transmission from records: use org.openmrs.serial to make a file
     *  also, give option to write to a file or not.
     *  <p> When writeFile is true, files are created in 'journal' dir under Application Data 
     *  (see openmrs documentation for more information about setting Application Data).
     *  Files are created using the following mask: sync_tx_yyyy_MM_dd_HH_mm_ss_S_request.xml
     *  
     *  @param writeFile if true, local file for this transmission will be created.  
     */
    public void create(boolean writeFile) {

        try {
            
            if (timestamp == null) this.timestamp = new Date(); //set timestamp of this export, if not already set
            
            FilePackage pkg = new FilePackage();
            Record xml = pkg.createRecordForWrite(this.getClass().getName());
            Item root = xml.getRootItem();

            //serialize
            this.save(xml,root);

            //now dump to file if needed
            fileOutput = pkg.savePackage(org.openmrs.util.OpenmrsUtil
                    .getApplicationDataDirectory()
                    + "/journal/" + fileName, writeFile);

        } catch (Exception e) {
            log.error("Cannot create sync transmission.",e);
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
        if (syncSourceGuid != null) xml.setAttribute(me, "syncSourceGuid", syncSourceGuid);
        if (timestamp != null) xml.setAttribute(me, "timestamp", new TimestampNormalizer().toString(timestamp));
        if (this.isRequestingTransmission != null) xml.setAttribute(me, "isRequestingTransmission", this.isRequestingTransmission.toString());
        if (this.isMaxRetryReached != null) xml.setAttribute(me, "isMaxRetryReached", this.isMaxRetryReached.toString());

        if (syncTargetGuid != null) xml.setAttribute(me, "syncTargetGuid", syncTargetGuid);
        else xml.setAttribute(me, "syncTargetGuid", SyncConstants.GUID_UNKNOWN);
        
        //serialize Records list
        Item itemsCollection = xml.createItem(me, "records");
        
        if (syncRecords != null) {
            me.setAttribute("itemCount", Integer.toString(syncRecords.size()));
            Iterator<SyncRecord> iterator = syncRecords.iterator();
            while (iterator.hasNext()) {
                iterator.next().save(xml, itemsCollection);
            }
        };

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
        this.isRequestingTransmission = Boolean.valueOf(me.getAttribute("isRequestingTransmission"));
        this.isMaxRetryReached =  Boolean.valueOf(me.getAttribute("isMaxRetryReached"));

        if (me.getAttribute("timestamp") == null)
            this.timestamp = null;
        else
            this.timestamp = (Date)new TimestampNormalizer().fromString(Date.class,me.getAttribute("timestamp"));
        
        //now get items
        Item itemsCollection = xml.getItem(me, "records");
        
        if (itemsCollection.isEmpty()) {
            this.syncRecords = null;
        } else {
            this.syncRecords = new ArrayList<SyncRecord>();
            List<Item> serItems = xml.getItems(itemsCollection);
            for (int i = 0; i < serItems.size(); i++) {
                Item serItem = serItems.get(i);
                SyncRecord syncRecord = new SyncRecord();
                syncRecord.load(xml, serItem);
                syncRecords.add(syncRecord);
            }
        }

    }

    /** Two instances of SyncTransmission are equal if all properties are equal, including the SyncRecords list.
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SyncTransmission) || o == null)
            return false;

        
        SyncTransmission oSync = (SyncTransmission) o;
        boolean same = ((oSync.getTimestamp() == null) ? (this.getTimestamp() == null) : oSync.getTimestamp().equals(this.getTimestamp()))
                && ((oSync.getGuid() == null) ? (this.getGuid() == null) : oSync.getGuid().equals(this.getGuid()))
                && ((oSync.getFileName() == null) ? (this.getFileName() == null) : oSync.getFileName().equals(this.getFileName()))
                && ((oSync.getFileOutput() == null) ? (this.getFileOutput() == null) : oSync.getFileOutput().equals(this.getFileOutput()))
                && ((oSync.getSyncSourceGuid() == null) ? (this.getSyncSourceGuid() == null) : oSync.getSyncSourceGuid().equals(this.getSyncSourceGuid()))
                && ((oSync.getSyncRecords() == null) ? (this.getSyncRecords() == null) : oSync.getSyncRecords().equals(this.getSyncRecords()));
        
        return same;
    }

    public String getSyncTargetGuid() {
        return syncTargetGuid;
    }

    public void setSyncTargetGuid(String syncTargetGuid) {
        this.syncTargetGuid = syncTargetGuid;
    }
    
}