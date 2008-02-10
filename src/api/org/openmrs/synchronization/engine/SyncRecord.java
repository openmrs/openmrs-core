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

import java.io.Serializable;
import java.util.Date;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openmrs.serialization.IItem;
import org.openmrs.serialization.Item;
import org.openmrs.serialization.Record;
import org.openmrs.serialization.TimestampNormalizer;
import org.openmrs.synchronization.SyncRecordState;
import org.openmrs.synchronization.server.RemoteServer;
import org.openmrs.synchronization.server.RemoteServerType;
import org.openmrs.synchronization.server.SyncServerRecord;

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
public class SyncRecord implements Serializable, IItem {

    public static final long serialVersionUID = 0L;

    // Fields
    private Integer recordId;
    private String guid = null;
    private String creator = null;
    private String databaseVersion = null;
    private Date timestamp = null;
    private int retryCount;
    private SyncRecordState state = SyncRecordState.NEW;
    private LinkedHashMap<String, SyncItem> items = null;
    private String containedClasses = "";
    private Set<SyncServerRecord> serverRecords = null;
    private RemoteServer forServer = null;
    private String originalGuid = null;

    public String getOriginalGuid() {
        return originalGuid;
    }

    public void setOriginalGuid(String originalGuid) {
        this.originalGuid = originalGuid;
    }

    // Constructors
    /** default constructor */
    public SyncRecord() {
    }

    public String getContainedClasses() {
        return containedClasses;
    }

    public void setContainedClasses(String containedClasses) {
        if ( containedClasses != null ) {
            String[] splits = containedClasses.split(",");
            for ( String split : splits ) {
                this.addContainedClass(split);
            }
        } else {
            this.containedClasses = containedClasses;
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

    //list of sync items
    public Collection<SyncItem> getItems() {
        return ((items == null) ? null : items.values());
    }

    public void addItem(SyncItem syncItem) {
        if (items == null) {
            items = new LinkedHashMap<String,SyncItem>();
        }
        items.put(syncItem.getKey().getKeyValue().toString(),syncItem);
    }

    /**
     * If there is already an item with same key, replace it with passed in value, else add it.
     * 
     * @param syncItem
     */
    public void addOrReplaceItem(SyncItem syncItem) {
        if (items == null) {
            items = new LinkedHashMap<String,SyncItem>();
        } else {
        	if (items.containsKey(syncItem.getKey().getKeyValue().toString())) {
    			items.remove(syncItem.getKey().getKeyValue().toString());
        	}
        }
        
        //now add it
        this.addItem(syncItem);     
    }

    public void setItems(Collection<SyncItem> newItems) {
    	items = new LinkedHashMap<String,SyncItem>();
    	for(SyncItem newItem : newItems) {
    		this.addItem(newItem);
    	}
   }

    public boolean hasItems() {
    	if (items == null) return false;
    	if (items.size() > 0) 
    		return true;
    	else
    		return false;
   }

    // Methods
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SyncRecord) || o == null)
            return false;

        SyncRecord oSync = (SyncRecord) o;
        boolean same = ((oSync.getTimestamp() == null) ? (this.getTimestamp() == null) : oSync.getTimestamp().equals(this.getTimestamp()))
                && ((oSync.getGuid() == null) ? (this.getGuid() == null) : oSync.getGuid().equals(this.getGuid()))
                && ((oSync.getState() == null) ? (this.getState() == null) : oSync.getState().equals(this.getState()))
                && ((oSync.getItems() == null) ? (this.getItems() == null) : oSync.getItems().equals(this.getItems()))
                && (oSync.getRetryCount() == this.getRetryCount());
        return same;
    }


    public Item save(Record xml, Item parent) throws Exception {
        Item me = xml.createItem(parent, this.getClass().getSimpleName());
        
        //serialize primitives
        xml.setAttribute(me, "guid", guid);
        xml.setAttribute(me, "retryCount", Integer.toString(retryCount));
        xml.setAttribute(me, "containedClasses", this.containedClasses);
        if ( this.originalGuid != null ) {
            xml.setAttribute(me, "originalGuid", originalGuid);
        }
        xml.setAttribute(me, "guid", guid);

        if ( this.getForServer() != null ) {
            if ( !this.getForServer().getServerType().equals(RemoteServerType.PARENT)) {
                SyncServerRecord serverRecord = this.getServerRecord(this.getForServer());
                xml.setAttribute(me, "state", serverRecord.getState().toString());
                xml.setAttribute(me, "retryCount", Integer.toString(serverRecord.getRetryCount()));
            } else {
                xml.setAttribute(me, "state", state.toString());
                xml.setAttribute(me, "retryCount", Integer.toString(retryCount));
            }
        } else {
            xml.setAttribute(me, "state", state.toString());
            xml.setAttribute(me, "retryCount", Integer.toString(retryCount));
        }
        
        if (timestamp != null) {
        	xml.setAttribute(me, "timestamp", new TimestampNormalizer().toString(timestamp));
        }
        
        //serialize IItem children
        Item itemsCollection = xml.createItem(me, "items");
        if (items != null) {
        	for(SyncItem item : items.values()) {
        		item.save(xml, itemsCollection);
        	}
        };

        return me;
    }

    public void load(Record xml, Item me) throws Exception {
        
        //deserialize primitives
        this.guid = me.getAttribute("guid");
        this.retryCount = Integer.parseInt(me.getAttribute("retryCount"));
        this.state = SyncRecordState.valueOf(me.getAttribute("state"));
        this.containedClasses = me.getAttribute("containedClasses");
        
        if (me.getAttribute("timestamp") == null)
            this.timestamp = null;
        else {
            this.timestamp = (Date)new TimestampNormalizer().fromString(Date.class,me.getAttribute("timestamp"));
        }

        if (me.getAttribute("originalGuid") == null)
            this.originalGuid = null;
        else {
            this.originalGuid = me.getAttribute("originalGuid");
        }

        //now get items
        Item itemsCollection = xml.getItem(me, "items");
        
        if (itemsCollection.isEmpty()) {
            items = null;
        } else {
            items = new LinkedHashMap<String,SyncItem>();
            List<Item> serItems = xml.getItems(itemsCollection);
            for (int i = 0; i < serItems.size(); i++) {
                Item serItem = serItems.get(i);
                SyncItem syncItem = new SyncItem();
                syncItem.load(xml, serItem);
                items.put(syncItem.getKey().getKeyValue().toString(),syncItem);
            }
        }
    }

    public Set<String> getContainedClassSet() {
        Set<String> ret = new HashSet<String>();
        
        if ( this.containedClasses != null ) {
            String[] classes = this.containedClasses.split(",");
            for ( String clazz : classes ) {
                if ( !ret.contains(clazz) ) ret.add(clazz);
            }
        }
        
        return ret;
    }
    
    public void setContainedClassSet(Set<String> classes) {
        if ( classes != null ) {
            this.containedClasses = "";
            for ( String clazz : classes ) {
                clazz = clazz.trim();
                if ( clazz.length() > 0 ) {
                    if ( this.containedClasses.length() == 0 ) this.containedClasses = clazz;
                    else this.containedClasses += "," + clazz;
                }
            }
        }
    }
    
    /**
     * Auto generated method comment
     * 
     * @param simpleName
     */
    public void addContainedClass(String simpleName) {
        if ( simpleName != null && simpleName.length() > 0 ) {
            Set<String> classes = this.getContainedClassSet();
            if ( classes == null ) classes = new HashSet<String>();
            if ( !classes.contains(simpleName) ) classes.add(simpleName);
            this.setContainedClassSet(classes);
        }
    }

    public Set<SyncServerRecord> getServerRecords() {
        return serverRecords;
    }

    public void setServerRecords(Set<SyncServerRecord> serverRecords) {
        this.serverRecords = serverRecords;
    }

    public SyncServerRecord getServerRecord(RemoteServer server) {
        SyncServerRecord ret = null;
        
        if ( server != null && this.serverRecords != null ) {
            for ( SyncServerRecord record : this.serverRecords ) {
                if ( record.getSyncServer().equals(server)) {
                    ret = record;
                }
            }
        }
        
        return ret;
    }
    
    public RemoteServer getForServer() {
        return forServer;
    }

    public void setForServer(RemoteServer forServer) {
        this.forServer = forServer;
    }
    
    public Map<RemoteServer, SyncServerRecord> getRemoteRecords() {
    	Map<RemoteServer, SyncServerRecord> ret = new LinkedHashMap<RemoteServer, SyncServerRecord>();
    	
    	if ( this.serverRecords != null ) {
    		for ( SyncServerRecord serverRecord : this.serverRecords ) {
    			ret.put(serverRecord.getSyncServer(), serverRecord);
    		}
    	}
    	
    	return ret;
    }
}
