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
package org.openmrs.web.dwr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.synchronization.SyncConstants;
import org.openmrs.synchronization.SyncTransmissionState;
import org.openmrs.synchronization.SyncUtil;
import org.openmrs.synchronization.ingest.SyncTransmissionResponse;
import org.openmrs.synchronization.server.ConnectionResponse;
import org.openmrs.synchronization.server.ServerConnection;
import org.openmrs.synchronization.server.ServerConnectionState;

/**
 *
 */
public class DWRSynchronizationService {

	protected final Log log = LogFactory.getLog(getClass());

	public SyncConnectionTestItem testConnection(String address, String username, String password) {
		SyncConnectionTestItem item = new SyncConnectionTestItem();
		item.setConnectionState(ServerConnectionState.NO_ADDRESS.toString());
		
		if ( address != null && address.length() > 0 ) {
			ConnectionResponse connResponse = ServerConnection.test(address, username, password);
	
	   		// constructor for SyncTransmissionResponse is null-safe
	    	SyncTransmissionResponse str = new SyncTransmissionResponse(connResponse);

	    	// constructor for SyncConnectionTestItem is null-safe
	    	item = new SyncConnectionTestItem(str);
		}
		
		return item;
	}

	public SyncTransmissionResponseItem syncToParent() {
		SyncTransmissionResponseItem transmissionResponse = new SyncTransmissionResponseItem(); 
    	transmissionResponse.setErrorMessage(SyncConstants.ERROR_SEND_FAILED.toString());
    	transmissionResponse.setFileName(SyncConstants.FILENAME_SEND_FAILED);
    	transmissionResponse.setGuid(SyncConstants.GUID_UNKNOWN);
    	transmissionResponse.setTransmissionState(SyncTransmissionState.SEND_FAILED.toString());

    	SyncTransmissionResponse response = SyncUtil.sendSyncTranssmission();
    	
    	if ( response != null ) {
    		transmissionResponse = new SyncTransmissionResponseItem(response);
    	}
    	
    	return transmissionResponse;
	}

}
