package org.openmrs.synchronization.engine;

import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.openmrs.serial.Item;
import org.openmrs.serial.IItem;
import org.openmrs.serial.Record;

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

    public Log log = LogFactory.getLog(this.getClass());

    // Fields
    private String guid = null;
    private Date timestamp = null;
    private int retryCount;
    private SyncRecordState state = SyncRecordState.NEW;
    private List<SyncItem> items = null;

    // Constructors
    /** default constructor */
    public SyncRecord() {
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

    public Item save(Record xml, Item parent) throws Exception {
        Item me = xml.createItem(parent, this.getClass().getName());
        
        //serialize primitives
        xml.setAttribute(me, "guid", guid);
        xml.setAttribute(me, "retryCount", Integer.toString(retryCount));
        xml.setAttribute(me, "state", state.toString());
        //TODO: xml.setAttribute(me, "timestamp", timestamp.);
        
        //serialize IItem children
        Item itemsCollection = xml.createItem(me,"items");
        if (items != null)
        {
            Iterator<SyncItem> iterator = items.iterator();
            while (iterator.hasNext()) {
                iterator.next().save(xml,itemsCollection);
            }
        };

        return me;
    }

    public void load(Record xml, Item me) throws Exception {
        // TODO
    }
}
