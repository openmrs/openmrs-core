package org.openmrs.synchronization.ingest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.serial.IItem;
import org.openmrs.serial.Item;
import org.openmrs.serial.Record;
import org.openmrs.serial.TimestampNormalizer;
import org.openmrs.synchronization.engine.SyncItem;
import org.openmrs.synchronization.engine.SyncRecord;
import org.openmrs.synchronization.engine.SyncRecordState;

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

    public Log log = LogFactory.getLog(this.getClass());

    // Fields
    private Integer recordId;
    private String guid = null;
    private Date timestamp = null;
    private int retryCount;
    private SyncRecordState state = SyncRecordState.NEW;
    private List<SyncItem> items = null;

    // Constructors
    /** default constructor */
    public SyncImportRecord() {
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
    public List<SyncItem> getItems() {
        return items;
    }

    public void setItems(List<SyncItem> items) {
        this.items = items;
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
        Item me = xml.createItem(parent, this.getClass().getName());
        
        //serialize primitives
        xml.setAttribute(me, "guid", guid);
        xml.setAttribute(me, "retryCount", Integer.toString(retryCount));
        xml.setAttribute(me, "state", state.toString());
        
        if (timestamp != null) {
        	xml.setAttribute(me, "timestamp", new TimestampNormalizer().toString(timestamp));
        }
        
        //serialize IItem children
        Item itemsCollection = xml.createItem(me, "items");
        if (items != null) {
            Iterator<SyncItem> iterator = items.iterator();
            while (iterator.hasNext()) {
                iterator.next().save(xml, itemsCollection);
            }
        };

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
            items = null;
        } else {
            items = new ArrayList<SyncItem>();
            List<Item> serItems = xml.getItems(itemsCollection);
            for (int i = 0; i < serItems.size(); i++) {
                Item serItem = serItems.get(i);
                SyncItem syncItem = new SyncItem();
                syncItem.load(xml, serItem);
                items.add(syncItem);
            }
        }
    }
}
