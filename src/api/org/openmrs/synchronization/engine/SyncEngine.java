package org.openmrs.synchronization.engine;

import java.util.List;
import java.util.ArrayList;

/*
 * Main entry point to the sync functionality.
 */
public class SyncEngine {
    
    // constructor(s)
    public SyncEngine() {        
    }
    
    /**
     * 
     * Called to retrieve the SyncRecords that need to be sent to target sync.
     *  
     * usage: Used by child prepare to push changes to parent.
     * 
     */
    public List<SyncRecord> getLocalChanges() {
        
        List<SyncRecord> records = new ArrayList<SyncRecord>();
        
        //TODO:
        // 1. scan DB for changes since last sync-ed local and create SyncRecords for them, add to 
        //    sync records queue (simple fifo)
        // 2. move last_sync_local
        // 3. get sync records queue: will contain
        //      - any pending sync records that may have failed send before
        //      - newly created sync records
        
        return records;
    }

    /**
     * 
     * Variation on above:
     * 1. retrieve local changes since 'p'; note 'p' must be a SyncPoint in terms of the 
     * local sequencing scheme 
     * 2. Add results of above to the pending records queue
     * 3. return all records in the pending recrods queue
     * 
     * usage: used by parent: needed for 'pull' from parent since child stores last_sync_remote -- 
     * the last time the child sync-ed from parent; see SyncSource.getLastSyncRemote().
     * 
     * NOTE: p is in/out
     * 
     * @return
     */
    public List<SyncRecord> getLocalChangesSince(SyncPoint p) {
        List<SyncRecord> records = new ArrayList<SyncRecord> ();
        
        //TODO
        
        return records;
    }
    
    
    /**
     * 
     * Attempts to apply the list of records to local DB. 
     * Note: the result of the call is update to SyncRecord.SyncStatus in the List.
     * For convenience, the return value of is also provided as:
     * return = 1: all changes committed successfully
     * return = 0: at least one record commit failed
     * return = -1: all record commits failed
     * 
     * TODO: make enum  or constants for this
     *
     * BIG QUESTION: should there be a 'receive' queue for syncRecords on parent so that these
     * do not need to be re-sent next time by client if some failed the commit? Presumably as long as the records
     * have arrived to parent; they can/should be 'dealt' with on the parent
     *
     */
    public int applyRecords(List<SyncRecord> records) {
     
        int status = -1;
        
        //TODO:
        
        return status; 
    }
}
