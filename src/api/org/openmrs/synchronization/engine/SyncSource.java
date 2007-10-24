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

import java.util.List;

import org.openmrs.synchronization.server.RemoteServer;


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
    
    //unique ID of the source
    public String getSyncSourceGuid();
    public void setSyncSourceGuid(String guid);
    
    //sync status: true - sync is enabled, false - disabled
    public boolean getSyncStatus();
    public void setSyncStatus(boolean status);
    
    //change set methods
    public List<SyncRecord> getDeleted(SyncPoint<?> from , SyncPoint<?> to) throws SyncException ;
    public List<SyncRecord> getChanged(SyncPoint<?> from , SyncPoint<?> to) throws SyncException ; //note this has new items also

    //state-based changeset methods
    public List<SyncRecord> getDeleted() throws SyncException ;
    public List<SyncRecord> getChanged() throws SyncException ; //note this has new items also
    public List<SyncRecord> getChanged(RemoteServer server) throws SyncException ; //note this has new items also

    //Methods used to apply changes
    public void applyDeleted(List<SyncRecord> records) throws SyncException ;
    public void applyChanged(List<SyncRecord> records) throws SyncException ;
}
