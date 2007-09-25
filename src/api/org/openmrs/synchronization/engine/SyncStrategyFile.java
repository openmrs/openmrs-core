package org.openmrs.synchronization.engine;

import java.util.List;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * sync strategy that implements sync-ing via disconnected push/pull.
 */
public class SyncStrategyFile {
    
    //fields
    private final Log log = LogFactory.getLog(getClass());
    
    //constructor
    public SyncStrategyFile() {}
    
    /** 
     * Using the sourceChild sync source, create a sync transmission using JournalManager   
     */
    public SyncTransmission createSyncTransmission(SyncSource source) {

        SyncTransmission tx = new SyncTransmission();
        
        List<SyncRecord> changeset = null;
        
        //retrieve value of the last sync timestamps
        SyncPoint lastSyncLocal = source.getLastSyncLocal();
        
        //establish the 'new' sync point; this will be new sync local after transmission was 'exported'
        SyncPoint lastSyncLocalNew = source.moveSyncPoint();

        //get changeset for sourceA
        changeset = this.getChangeset(source, lastSyncLocal,lastSyncLocalNew);
        
        //pack it into transmission
        SyncTransmission syncTx = new SyncTransmission(changeset);
        syncTx.CreateFile();
        
        //set new SyncPoint
        source.setLastSyncLocal(lastSyncLocalNew);
        
        return syncTx;
    }

    public SyncTransmission createStateBasedSyncTransmission(SyncSource source) {

        SyncTransmission tx = new SyncTransmission();
        
        List<SyncRecord> changeset = null;
        
        //retrieve value of the last sync timestamps
        SyncPoint lastSyncLocal = source.getLastSyncLocal();
        
        //establish the 'new' sync point; this will be new sync local after transmission was 'exported'
        SyncPoint lastSyncLocalNew = source.moveSyncPoint();

        //get changeset for sourceA
        changeset = this.getStateBasedChangeset(source);
        
        //pack it into transmission
        SyncTransmission syncTx = new SyncTransmission(changeset);
        syncTx.CreateFile();
        
        //set new SyncPoint
        source.setLastSyncLocal(lastSyncLocalNew);
        
        return syncTx;
    }

    /** 
     * Update status of a given sync transmission
     */
    public void updateSyncTransmission(SyncTransmission Tx) {
        
        //TODO
        
        return;

    }
    
    /**
     * 
     * TODO: Review the 'exported' transmissions and return the list of the ones that did not receive 
     * a confirmation from the server; these are in the 'pending' state.
     *
     */
    public List<String> getPendingTransmissions() {
        //TODO
        List<String> pending = new ArrayList<String>();
        
        return pending;
    }

    /**
     * Apply given sync tx to source. 
     */
    public void applySyncTransmission(SyncSource source, SyncTransmission tx) {

        //TODO
        
        return;
    }
    
    
    private List<SyncRecord> getChangeset(SyncSource source, SyncPoint from,SyncPoint to) {
        List<SyncRecord> deleted = null;
        List<SyncRecord> changed = null;
        List<SyncRecord> changeset = null;
                
        //get all local deletes, inserts and updates
        deleted = source.getDeleted(from,to);
        changed = source.getChanged(from,to);
        
        //merge
        changeset = deleted;
        changeset.addAll(changed);
 
        return changeset;
    }

    private List<SyncRecord> getStateBasedChangeset(SyncSource source) {
        List<SyncRecord> deleted = null;
        List<SyncRecord> changed = null;
        List<SyncRecord> changeset = null;
                
        //get all local deletes, inserts and updates
        deleted = source.getDeleted();
        changed = source.getChanged();
        
        //merge
        changeset = deleted;
        changeset.addAll(changed);
 
        return changeset;
    }

    //apply items to source
    private void applyChangeset(SyncSource source, List<SyncRecord> items  ) {
        
        //TODO
        return;
    }

}
