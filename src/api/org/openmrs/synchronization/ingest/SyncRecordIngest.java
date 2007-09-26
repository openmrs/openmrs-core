package org.openmrs.synchronization.ingest;

import org.openmrs.api.context.Context;
import org.openmrs.synchronization.engine.SyncItem;
import org.openmrs.synchronization.engine.SyncRecord;
import org.openmrs.synchronization.engine.SyncRecordState;
import org.openmrs.synchronization.ingest.SyncImportRecord;

public class SyncRecordIngest {
			
	public static SyncImportRecord processSyncRecord(SyncRecord record) {

		// first, let's see if this SyncRecord has already been imported
		SyncImportRecord importRecord = Context.getSynchronizationService().getSyncImportRecord(record.getGuid());
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
				// committed, so let's remind by sending back this import record
				// we can achieve this by doing nothing here, and just returning the record
			}
		}
		
		if ( isUpdateNeeded ) {
			
			boolean isError = false;
			String errMessage = "";
			
			// for each sync item, process it and insert/update the database
			for ( SyncItem item : record.getItems() ) {
				String syncItem = item.getContent();
				try {
					String resultingRecordGuid = SyncItemIngest.processSyncItem(syncItem, item.getState());
					importRecord.setResultingRecordGuid(resultingRecordGuid);
				} catch (SyncItemIngestException siie) {
					isError = true;
					errMessage = siie.getSyncItemContent();
				}
			}
			
			if( !isError ) {
				importRecord.setState(SyncRecordState.COMMITTED);
			} else {
				importRecord.setState(SyncRecordState.FAILED);
				importRecord.setItemContent(errMessage);
			}
			Context.getSynchronizationService().updateSyncImportRecord(importRecord);
		}

		return importRecord;
	}
}
