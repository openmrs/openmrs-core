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
package org.openmrs.synchronization.server;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.openmrs.serialization.IItem;
import org.openmrs.serialization.Item;
import org.openmrs.serialization.Record;
import org.openmrs.serialization.TimestampNormalizer;
import org.openmrs.synchronization.SyncRecordState;
import org.openmrs.synchronization.engine.SyncItem;
import org.openmrs.synchronization.engine.SyncRecord;

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
public class SyncServerRecord implements Serializable {

    public static final long serialVersionUID = 1L;

    // Fields
    private Integer serverRecordId;
    private RemoteServer syncServer;
    private SyncRecord syncRecord;
    private SyncRecordState state = SyncRecordState.NEW;
    private int retryCount = 0;

    // Constructors
    /** default constructor */
    public SyncServerRecord() {
    }

    /**
     * @param server
     * @param record
     */
    public SyncServerRecord(RemoteServer server, SyncRecord record) {
        this.syncServer = server;
        this.syncRecord = record;
    }

    //state
    public SyncRecordState getState() {
        return state;
    }

    public void setState(SyncRecordState state) {
        this.state = state;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public Integer getServerRecordId() {
        return serverRecordId;
    }

    public void setServerRecordId(Integer serverRecordId) {
        this.serverRecordId = serverRecordId;
    }

    public SyncRecord getSyncRecord() {
        return syncRecord;
    }

    public void setSyncRecord(SyncRecord syncRecord) {
        this.syncRecord = syncRecord;
    }

    public RemoteServer getSyncServer() {
        return syncServer;
    }

    public void setSyncServer(RemoteServer syncServer) {
        this.syncServer = syncServer;
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + retryCount;
        result = PRIME * result + ((serverRecordId == null) ? 0 : serverRecordId.hashCode());
        result = PRIME * result + ((state == null) ? 0 : state.hashCode());
        result = PRIME * result + ((syncRecord == null) ? 0 : syncRecord.hashCode());
        result = PRIME * result + ((syncServer == null) ? 0 : syncServer.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final SyncServerRecord other = (SyncServerRecord) obj;
        if (retryCount != other.retryCount)
            return false;
        if (serverRecordId == null) {
            if (other.serverRecordId != null)
                return false;
        } else if (!serverRecordId.equals(other.serverRecordId))
            return false;
        if (state == null) {
            if (other.state != null)
                return false;
        } else if (!state.equals(other.state))
            return false;
        if (syncRecord == null) {
            if (other.syncRecord != null)
                return false;
        } else if (!syncRecord.equals(other.syncRecord))
            return false;
        if (syncServer == null) {
            if (other.syncServer != null)
                return false;
        } else if (!syncServer.equals(other.syncServer))
            return false;
        return true;
    }

    
}
