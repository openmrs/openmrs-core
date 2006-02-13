package org.openmrs.form;

public class FormEntryConstants {

	/* FormEntry Queue baked-in prileges */
	public static final String PRIV_VIEW_FORMENTRY_QUEUE	= "View FormEntry Queue";
	public static final String PRIV_ADD_FORMENTRY_QUEUE		= "Add FormEntry Queue";
	public static final String PRIV_EDIT_FORMENTRY_QUEUE	= "Edit FormEntry Queue";
	public static final String PRIV_DELETE_FORMENTRY_QUEUE	= "Delete FormEntry Queue";

	/* FormEntry Queue status values for entries in the queue */
	public static final int FORMENTRY_QUEUE_STATUS_PENDING = 0;
	public static final int FORMENTRY_QUEUE_STATUS_PROCESSING = 1;
	public static final int FORMENTRY_QUEUE_STATUS_PROCESSED = 2;
	public static final int FORMENTRY_QUEUE_STATUS_ERROR = 3;
	
}
