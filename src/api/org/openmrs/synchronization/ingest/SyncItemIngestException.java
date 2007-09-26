package org.openmrs.synchronization.ingest;

public class SyncItemIngestException extends Exception {

	private static final long serialVersionUID = -4034873434558271005L;

	String syncItemContent;

	public SyncItemIngestException(Throwable t, String syncItemContent) {
		super(t);
		this.setSyncItemContent(syncItemContent);
	}
	
	public String getSyncItemContent() {
		return syncItemContent;
	}

	public void setSyncItemContent(String syncItemContent) {
		this.syncItemContent = syncItemContent;
	}
}
