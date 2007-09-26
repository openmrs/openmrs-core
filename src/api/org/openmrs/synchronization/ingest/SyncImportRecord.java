package org.openmrs.synchronization.ingest;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.serial.IItem;
import org.openmrs.serial.Item;
import org.openmrs.serial.Record;
import org.openmrs.serial.TimestampNormalizer;
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
    private String itemContent;
    private String errorMessage;
    private String resultingRecordGuid;

	// Constructors
    /** default constructor */
    public SyncImportRecord() {
    }

    public SyncImportRecord(SyncRecord record) {
    	if ( record != null ) {
    		this.guid = record.getGuid();
    		this.timestamp = record.getTimestamp();
    		this.retryCount = record.getRetryCount();
    		this.state = record.getState();
    		this.itemContent = null;
    		this.resultingRecordGuid = null;
    	}
    }

    public String getResultingRecordGuid() {
    	return resultingRecordGuid;
    }

	public void setResultingRecordGuid(String resultingRecordGuid) {
    	this.resultingRecordGuid = resultingRecordGuid;
    }

    public String getItemContent() {
    	return itemContent;
    }

	public void setItemContent(String itemContent) {
    	this.itemContent = itemContent;
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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SyncImportRecord) || o == null)
            return false;

        SyncImportRecord oSync = (SyncImportRecord) o;
        boolean same = ((oSync.getTimestamp() == null) ? (this.getTimestamp() == null) : oSync.getTimestamp().equals(this.getTimestamp()))
                && ((oSync.getGuid() == null) ? (this.getGuid() == null) : oSync.getGuid().equals(this.getGuid()))
                && ((oSync.getState() == null) ? (this.getState() == null) : oSync.getState().equals(this.getState()))
                && ((oSync.getItemContent() == null) ? (this.getItemContent() == null) : oSync.getItemContent().equals(this.getItemContent()))
                && (oSync.getRetryCount() == this.getRetryCount())
                && ((oSync.getResultingRecordGuid() == null) ? (this.getResultingRecordGuid() == null) : oSync.getResultingRecordGuid().equals(this.getResultingRecordGuid()));
        return same;
    }


    public Item save(Record xml, Item parent) throws Exception {
        Item me = xml.createItem(parent, this.getClass().getName());
        
        //serialize primitives
        xml.setAttribute(me, "guid", this.guid);
        xml.setAttribute(me, "retryCount", Integer.toString(this.retryCount));
        xml.setAttribute(me, "state", this.state.toString());
        xml.setAttribute(me, "resultingRecordGuid", this.resultingRecordGuid);
        if (timestamp != null) {
        	xml.setAttribute(me, "timestamp", new TimestampNormalizer().toString(timestamp));
        }
        
        Item content = xml.createItem(me, "itemContent");
        if (this.itemContent != null) {
            xml.createTextAsCDATA(content, this.itemContent);
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
        
        //now get items content
        Item content = xml.getItem(me, "itemContent");
        if (content.isEmpty()) {
            this.itemContent = null;
        } else {
        	this.itemContent = content.getText();
        }
    }

	public String getErrorMessage() {
    	return errorMessage;
    }

	public void setErrorMessage(String errorMessage) {
    	this.errorMessage = errorMessage;
    }
}
