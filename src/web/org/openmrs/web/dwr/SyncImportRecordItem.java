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

import java.text.SimpleDateFormat;
import java.util.Vector;

import org.openmrs.serialization.TimestampNormalizer;
import org.openmrs.synchronization.SyncRecordState;
import org.openmrs.synchronization.ingest.SyncImportItem;
import org.openmrs.synchronization.ingest.SyncImportRecord;

/**
 *
 */
public class SyncImportRecordItem {
    private String guid;
    private String timestampRaw;
    private String timestampDisplay;
    private Integer retryCount;
    private String state;
    private String errorMessage;
    private Vector<SyncImportItemItem> syncImportItems;

	public SyncImportRecordItem() {}
    
    public SyncImportRecordItem(SyncImportRecord importRecord) {
    	if ( importRecord != null ) {
    		this.guid = importRecord.getGuid();

    		if ( importRecord.getTimestamp() != null ) this.timestampRaw = new TimestampNormalizer().toString(importRecord.getTimestamp());
    		else this.timestampRaw = "";
    		
    		SimpleDateFormat sdf = new SimpleDateFormat(TimestampNormalizer.DATETIME_DISPLAY_FORMAT);
    		if ( importRecord.getTimestamp() != null ) this.timestampDisplay = sdf.format(importRecord.getTimestamp());
    		else this.timestampDisplay = "";
    		
    		this.retryCount = importRecord.getRetryCount();
    		
    		if ( importRecord.getState() != null ) this.state = importRecord.getState().toString();
    		else this.state = SyncRecordState.FAILED.toString();
    		
    		this.errorMessage = importRecord.getErrorMessage();
    		
    		if ( importRecord.getItems() != null ) {
    			this.syncImportItems = new Vector<SyncImportItemItem>();
    			for ( SyncImportItem item : importRecord.getItems() ) {
    				this.syncImportItems.add(new SyncImportItemItem(item));
    			}
    		}
    	} else {
    		this.state = SyncRecordState.FAILED.toString();
    	}
    }

    public Vector<SyncImportItemItem> getSyncImportItems() {
    	return syncImportItems;
    }

	public void setSyncImportItems(Vector<SyncImportItemItem> syncImportItems) {
    	this.syncImportItems = syncImportItems;
    }

    public String getErrorMessage() {
    	return errorMessage;
    }
	
    public void setErrorMessage(String errorMessage) {
    	this.errorMessage = errorMessage;
    }
	
    public String getGuid() {
    	return guid;
    }
	
    public void setGuid(String guid) {
    	this.guid = guid;
    }
	
    public Integer getRetryCount() {
    	return retryCount;
    }
	
    public void setRetryCount(Integer retryCount) {
    	this.retryCount = retryCount;
    }
	
    public String getState() {
    	return state;
    }
	
    public void setState(String state) {
    	this.state = state;
    }

    public String getTimestampDisplay() {
    	return timestampDisplay;
    }
	
    public void setTimestampDisplay(String timestampDisplay) {
    	this.timestampDisplay = timestampDisplay;
    }
	
    public String getTimestampRaw() {
    	return timestampRaw;
    }
	
    public void setTimestampRaw(String timestampRaw) {
    	this.timestampRaw = timestampRaw;
    }
    
}
