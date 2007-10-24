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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openmrs.serialization.IItem;
import org.openmrs.serialization.Item;
import org.openmrs.serialization.Record;
import org.openmrs.serialization.TimestampNormalizer;
import org.openmrs.synchronization.SyncRecordState;
import org.openmrs.synchronization.engine.SyncRecord;

/**
 * SyncRecord is a collection of sync items that represents a smallest transactional unit.
 * In other words, all sync items within a record must be:
 * - transfered from/to sync source 
 * - committed/rolled back together
 * 
 * Information about sync records -- what was sent, received should be stored in DB by each
 * sync source. Minimally, each source source should keep track of history of sync records that were
 * sent 'up' to parent. 
 * 
 * Consequently a sync 'transmission' is nothing more than a transport of a set of sync records from 
 * source A to source B.
 * 
 */
public class SyncImportRecord implements Serializable, IItem {

    public static final long serialVersionUID = 0L;

    // Fields
    private Integer recordId;
    private String guid = null;
    private String creator = null;
    private String databaseVersion = null;
    private Date timestamp = null;
    private int retryCount;
    private SyncRecordState state = SyncRecordState.NEW;
    private String errorMessage;
    private List<SyncImportItem> items = null;

	// Constructors
    /** default constructor */
    public SyncImportRecord() {
    }

    public SyncImportRecord(SyncRecord record) {
    	if ( record != null ) {
            // the guid should be set to original guid - this way all subsequent attempts to execute this change are matched to this import
    		this.guid = record.getOriginalGuid();
    		this.creator = record.getCreator();
    		this.databaseVersion = record.getDatabaseVersion();
    		this.timestamp = record.getTimestamp();
    		this.retryCount = record.getRetryCount();
    		this.state = record.getState();
    	}
    }

	public Integer getRecordId() {
    	return recordId;
    }

	public void setRecordId(Integer recordId) {
    	this.recordId = recordId;
    }

	// Properties
    // globally unique id of the record
    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    // The guid of the creator of the record
    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }
    
    // The database version used when creating this record
    public String getDatabaseVersion() {
        return databaseVersion;
    }

    public void setDatabaseVersion(String databaseVersion) {
        this.databaseVersion = databaseVersion;
    }

    // timestamp of last operation
    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    // retry count
    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    //state
    public SyncRecordState getState() {
        return state;
    }

    public void setState(SyncRecordState state) {
        this.state = state;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SyncImportRecord) || o == null)
            return false;

        SyncImportRecord oSync = (SyncImportRecord) o;
        boolean same = ((oSync.getTimestamp() == null) ? (this.getTimestamp() == null) : oSync.getTimestamp().equals(this.getTimestamp()))
                && ((oSync.getGuid() == null) ? (this.getGuid() == null) : oSync.getGuid().equals(this.getGuid()))
                && ((oSync.getState() == null) ? (this.getState() == null) : oSync.getState().equals(this.getState()))
                && (oSync.getRetryCount() == this.getRetryCount());
        return same;
    }


    public Item save(Record xml, Item parent) throws Exception {
        Item me = xml.createItem(parent, this.getClass().getName());
        
        //serialize primitives
        xml.setAttribute(me, "guid", this.guid);
        xml.setAttribute(me, "retryCount", Integer.toString(this.retryCount));
        xml.setAttribute(me, "state", this.state.toString());
        if (timestamp != null) {
        	xml.setAttribute(me, "timestamp", new TimestampNormalizer().toString(timestamp));
        }
        
        //serialize items list
        Item itemsCollection = xml.createItem(me, "items");
        if (this.items != null) {
            me.setAttribute("itemCount", Integer.toString(this.items.size()));
            for ( SyncImportItem importItem : this.items ) {
            	importItem.save(xml, itemsCollection);
            }
        }
        
        return me;
    }

    public void load(Record xml, Item me) throws Exception {
        
        //deserialize primitives
        this.guid = me.getAttribute("guid");
        this.retryCount = Integer.parseInt(me.getAttribute("retryCount"));
        this.state = SyncRecordState.valueOf(me.getAttribute("state"));
        
        if (me.getAttribute("timestamp") == null)
            this.timestamp = null;
        else {
            this.timestamp = (Date)new TimestampNormalizer().fromString(Date.class,me.getAttribute("timestamp"));
        }
        
        //now get items
        Item itemsCollection = xml.getItem(me, "items");
        
        if (itemsCollection.isEmpty()) {
            this.items = null;
        } else {
            this.items = new ArrayList<SyncImportItem>();
            List<Item> serItems = xml.getItems(itemsCollection);
            for (int i = 0; i < serItems.size(); i++) {
                Item serItem = serItems.get(i);
                SyncImportItem syncImportItem = new SyncImportItem();
                syncImportItem.load(xml, serItem);
                this.addItem(syncImportItem);
            }
        }

    }

	public String getErrorMessage() {
    	return errorMessage;
    }

	public void setErrorMessage(String errorMessage) {
    	this.errorMessage = errorMessage;
    }

	@Override
    public String toString() {
	    // TODO Auto-generated method stub
	    return "SyncRecord (guid:" + this.guid + ") - " + this.state;
    }

	public List<SyncImportItem> getItems() {
    	return items;
    }

	public void setItems(List<SyncImportItem> items) {
    	this.items = items;
    }	
	
	public void addItem(SyncImportItem item) {
		if ( this.items == null ) this.items = new ArrayList<SyncImportItem>();
		this.items.add(item);
	}
}
