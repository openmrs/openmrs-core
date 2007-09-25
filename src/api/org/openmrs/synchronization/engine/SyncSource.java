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

    //sync point helpers
    public SyncPoint<?> getLastSyncLocal();
    public void setLastSyncLocal(SyncPoint<?> p);
    public SyncPoint<?> getLastSyncRemote();
    public void setLastSyncRemote(SyncPoint<?> p);
    public SyncPoint<?> moveSyncPoint();
    
    //change set methods
    public List<SyncRecord> getDeleted(SyncPoint<?> from , SyncPoint<?> to) throws SyncException ;
    public List<SyncRecord> getChanged(SyncPoint<?> from , SyncPoint<?> to) throws SyncException ; //note this has new items also

    //state-based changeset methods
    public List<SyncRecord> getDeleted() throws SyncException ;
    public List<SyncRecord> getChanged() throws SyncException ; //note this has new items also

    //Methods used to apply changes
    public void applyDeleted(List<SyncRecord> records) throws SyncException ;
    public void applyChanged(List<SyncRecord> records) throws SyncException ;
}
