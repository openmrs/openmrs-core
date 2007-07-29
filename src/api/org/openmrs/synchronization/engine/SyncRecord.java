package org.openmrs.synchronization.engine;

import java.io.Serializable;
import java.util.List;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
public class SyncRecord implements Serializable {

    public static final long serialVersionUID = 0L;
    public Log log = LogFactory.getLog(this.getClass());

    //sync state:
    //NEW - initial state of a sync record
    //PENDING-SEND - sync record is being sent to target sync source however it's
    // transmission to the target sync source has not been confirmed
    //SENT - the record has been successful transmitted to the target source, note it may not
    //yet be committed
    //SEND_FAILED: attempted send failed
    //PENDING_COMMIT: record is being committed at parent; outcome not yet known
    //COMMITTED: the record was successfully committed at target source
    //COMMIT_FAILED: attempted commit of the record failed
    //FAILED: the record reached the final failed state: no more retries will be attempted
    public enum SyncRecordState {
        NEW,
        PENDING_SEND,
        SENT,
        SEND_FAILED,
        PENDING_COMMIT,
        COMMITTED,
        ABORTED,
        FAILED
    };

    // Fields
    private String guid = null;
    private Date timestamp = null;
    private int retryCount;
    private SyncRecordState state = SyncRecordState.NEW;
    private List<SyncItem> items = null;
    
    // Properties
    //unique id of the record
    public String getGuid() {
        return guid;
    }
    
    public void setGuid(String guid) {
        this.guid = guid;
    }

    //timestamp of last operation
    public Date getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
    
    //retry count
    public int getRetryCount() {
        return retryCount;
    }
    
    public void setState(int retryCount) {
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
    public List<SyncItem> getSyncItems() {
        return items;
    }
    
    public void setSyncItems(List<SyncItem> items) {
        this.items = items;
    }
    
}
