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
package org.openmrs.synchronization.ingest;

import org.openmrs.api.context.Context;
import org.openmrs.synchronization.SyncConstants;
import org.openmrs.synchronization.SyncItemState;
import org.openmrs.synchronization.SyncRecordState;
import org.openmrs.synchronization.engine.SyncItem;
import org.openmrs.synchronization.engine.SyncRecord;

@Deprecated
public class SyncRecordIngest {
			
	public static SyncImportRecord processSyncRecord(SyncRecord record) {

		SyncImportRecord importRecord = new SyncImportRecord();
		importRecord.setState(SyncRecordState.FAILED);  // by default, until we know otherwise
		importRecord.setRetryCount(record.getRetryCount());
		importRecord.setTimestamp(record.getTimestamp());
		
		try {
			// first, let's see if this SyncRecord has already been imported
			importRecord = Context.getSynchronizationService().getSyncImportRecord(record.getGuid());
			boolean isUpdateNeeded = false;
			
			if ( importRecord == null ) {
				isUpdateNeeded = true;
				importRecord = new SyncImportRecord(record);
				Context.getSynchronizationService().createSyncImportRecord(importRecord);
			} else {
				SyncRecordState state = importRecord.getState();
				if ( state != SyncRecordState.COMMITTED ) {
					isUpdateNeeded = true;
				} else {
					// apparently, the remote/child server exporting to this server doesn't realize it's
					// committed, so let's remind by sending back this import record with already_committed
					importRecord.setState(SyncRecordState.ALREADY_COMMITTED);
				}
			}
			
			if ( isUpdateNeeded ) {
				
				boolean isError = false;
				
				// set transaction boundaries
				Context.openSession();
	
				// for each sync item, process it and insert/update the database
				for ( SyncItem item : record.getItems() ) {
					// this could be done differently - just passing actual item to processSyncItem
					String syncItem = item.getContent();
					SyncImportItem importedItem = SyncItemIngest.processSyncItem(syncItem);
					importedItem.setKey(item.getKey());
					importRecord.addItem(importedItem);
					if ( !importedItem.getState().equals(SyncItemState.SYNCHRONIZED)) isError = true;
					//importRecord.setResultingRecordGuid(resultingRecordGuid);
				}
				if ( !isError ) {
					importRecord.setState(SyncRecordState.COMMITTED);
				} else {
					// rollback!!
				}
				// finish the transaction
				Context.closeSession();
				
				Context.getSynchronizationService().updateSyncImportRecord(importRecord);
			}
		} catch (Exception e ) {
			
		}

		return importRecord;
	}

	/**
     * Auto generated method comment
     * 
     * @param importRecord
     */
    public static void processSyncImportRecord(SyncImportRecord importRecord) {
    	if ( importRecord != null ) {
    		if ( importRecord.getGuid() != null && importRecord.getState() != null ) {
        		SyncRecord record = Context.getSynchronizationService().getSyncRecord(importRecord.getGuid());
        		if ( importRecord.getState().equals(SyncRecordState.ALREADY_COMMITTED) ) record.setState(SyncRecordState.COMMITTED);
        		else record.setState(importRecord.getState());
        		
        		Context.getSynchronizationService().updateSyncRecord(record);
    		}
    	}
    }
    
    /**
     * 
     * Retrieve the unique id of the server where sync records are to be processed.
     * 
     * @return
     */
    public static String getSyncParentGuid() {   
        return Context.getAdministrationService().getGlobalProperty(SyncConstants.SERVER_GUID);
    }
}
