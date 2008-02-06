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

import org.openmrs.synchronization.SyncException;
import org.openmrs.synchronization.ingest.SyncImportRecord;

public class SyncIngestException extends SyncException {

	private static final long serialVersionUID = -4034873434558271005L;

	String syncItemContent;
	String itemError;
	String itemErrorArgs;
	SyncImportRecord syncImportRecord;

	public SyncIngestException(Throwable t, String errorMessage, String errorMessageArgs, String syncItemContent, SyncImportRecord sir) {
		super(t);
		this.setItemError(errorMessage);
		this.setItemErrorArgs(errorMessageArgs);
		this.setSyncItemContent(syncItemContent);
		this.setSyncImportRecord(sir);
	}	
	public SyncIngestException(String errorMessage, String errorMessageArgs, String syncItemContent, SyncImportRecord sir) {
		super();
		this.setItemError(errorMessage);
		this.setItemErrorArgs(errorMessageArgs);
		this.setSyncItemContent(syncItemContent);
		this.setSyncImportRecord(sir);
	}

	public String getSyncItemContent() {
		return syncItemContent;
	}

	public void setSyncItemContent(String syncItemContent) {
		this.syncItemContent = syncItemContent;
	}

	public String getItemErrorArgs() {
    	return itemErrorArgs;
    }

	public void setItemErrorArgs(String itemErrorArgs) {
    	this.itemErrorArgs = itemErrorArgs;
    }

	public String getItemError() {
    	return itemError;
    }

	public void setItemError(String itemError) {
    	this.itemError = itemError;
    }

	//TODO in 0.3 release, see ticket #603: not used until ticket #603
	public SyncImportRecord getSyncImportRecord() {
		return syncImportRecord;
	}

	//TODO in 0.3 release, see ticket #603: not used until ticket #603
	public void setSyncImportRecord(SyncImportRecord value) {
		this.syncImportRecord = value;
	}
	
}
