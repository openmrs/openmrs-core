package org.openmrs.synchronization.engine;

import java.util.List;
import java.util.Date;

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
public interface SyncRecord {

    //sync state:
    //PENDING-SEND - sync record is being sent to target sync source however it's
    // transmission to the target sync source has not been confirmed
    //SENT - the record has been successful trasmitted to the target source, note it may not
    //yet be committed
    //SEND_FAILED: attempted send failed
    //PENDING_COMMIT: record is being committed at parent; outcome not yet known
    //COMMITTED: the record was successfuly committed at target source
    //COMMIT_FAILED: attempted commit of the record failed
    //FAILED: the record reached the final failed state: no more retries will be attempted
    public enum SyncRecordState {
        PENDING_SEND,
        SENT,
        SEND_FAILED,
        PENDING_COMMIT,
        COMMITTED,
        ABORTED,
        FAILED};

    //unique id of the record
    public String getGuid();
    public void setGuid(String key);

    //timestamp of last operation
    public Date getTimestamp();
    public void setTimestamp(Date timestamp);
    
    //retry count
    public int getRetryCount();
   public void setState(int retry);        
        
    //state
    public SyncRecordState getState();
    public void setState(SyncRecordState state);

    //list of sync items
    public List<SyncItem> getSyncItems();
    public void setSyncItems(List<SyncItem> items);
    
}
