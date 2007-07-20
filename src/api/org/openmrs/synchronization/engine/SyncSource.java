package org.openmrs.synchronization.engine;

import java.util.List;

import org.openmrs.synchronization.engine.SyncPoint;
import org.openmrs.synchronization.engine.SyncException;
import org.openmrs.synchronization.engine.SyncItem;
import org.openmrs.synchronization.engine.SyncItemKey;


/**
 * Represents a source of sync items; can be either 'child' or 'parent'.
 *
 */
public interface SyncSource {

    //setup methods
    public void beginSync() throws SyncException;
    public void endSync() throws SyncException;

    //sync point helpers
    public SyncPoint getLastSyncLocal();
    public void setLastSyncLocal(SyncPoint p);
    public SyncPoint getLastSyncRemote();
    public void setLastSyncRemote(SyncPoint p);
    public SyncPoint moveSyncPoint();
    
    //change set methods
    public List<SyncItem> getDeleted(SyncPoint from , SyncPoint to) throws SyncException ;
    public List<SyncItem> getChanged(SyncPoint from , SyncPoint to) throws SyncException ; //note this has new items also
    
    //Methods used to apply changes
    public SyncItem addSyncItem(SyncItem syncInstance) throws SyncException ;
    public SyncItem updateSyncItem(SyncItem syncInstance) throws SyncException;
    public void removeSyncItem(SyncItemKey itemKey) throws SyncException;
    public void commitSync() throws SyncException;
}
