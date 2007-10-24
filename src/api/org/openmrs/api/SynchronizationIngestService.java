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
package org.openmrs.api;

import org.openmrs.synchronization.engine.SyncRecord;
import org.openmrs.synchronization.ingest.SyncImportItem;
import org.openmrs.synchronization.ingest.SyncImportRecord;
import org.openmrs.synchronization.server.RemoteServer;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface SynchronizationIngestService {

    /**
     * Process SyncRecord and create corresponding sync import record.
     * @param SyncRecord The SyncRecord to create
     * @throws APIException
     */
    //@Authorized({"Manage Synchronization Records"})
    public SyncImportRecord processSyncRecord(SyncRecord record, RemoteServer server) throws APIException;
    
    /**
     * Process SyncImportRecord.
     * @param SyncRecord The SyncRecord to update
     * @throws APIException
     */
    //@Authorized({"Manage Synchronization Records"})
    public void processSyncImportRecord(SyncImportRecord importRecord, RemoteServer server) throws APIException;
    
    /**
     * 
     * Auto generated method comment
     * 
     * @param incoming
     * @throws APIException
     */
    //@Authorized({"Manage Synchronization Records"})
    public SyncImportItem processSyncItem(String incoming, String originalGuid)  throws APIException;
}
