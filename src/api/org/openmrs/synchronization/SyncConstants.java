package org.openmrs.synchronization;

import java.text.SimpleDateFormat;

import org.openmrs.synchronization.auto.SynchronizationTask;


/**
 * Common sync consts.
 *
 */
public class SyncConstants {

    public static final SimpleDateFormat SYNC_FILENAME_MASK = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_S"); //used to format file names
	public static final String LAST_SYNC_LOCAL = "synchronization.last_sync_local";
	public static final String LAST_SYNC_REMOTE = "synchronization.last_sync_remote";
	
	public static final String DATA_IMPORT_SERVLET = "/admin/synchronization/synchronizationImport.list";
	
	public static final String TEST_MESSAGE = "test";
	
	public static final SyncRecordState[] SYNC_TO_PARENT_STATES = {
		SyncRecordState.NEW,	
		SyncRecordState.PENDING_SEND,
		SyncRecordState.SEND_FAILED,
		SyncRecordState.SENT,
		SyncRecordState.SENT_AGAIN,
		SyncRecordState.ABORTED,
		SyncRecordState.PENDING_COMMIT,
	};

	// error message codes
	public static final String ERROR_NO_RESPONSE = "SynchronizationStatus.transmission.noResponseError";
	public static final String ERROR_TRANSMISSION_CREATION = "SynchronizationStatus.transmission.createError";
	public static final String ERROR_NO_PARENT_DEFINED = "SynchronizationStatus.transmission.noParentError";
	public static final String ERROR_SEND_FAILED = "SynchronizationStatus.transmission.sendError";
	public static final String ERROR_RESPONSE_NOT_UNDERSTOOD = "SynchronizationStatus.transmission.corruptResponseError";
	public static final String ERROR_AUTH_FAILED = "SynchronizationStatus.transmission.noAuthError";
	public static final String ERROR_TX_NOT_UNDERSTOOD = "SynchronizationStatus.transmission.corruptTxError";
	public static final String ERROR_NO_CONNECTION = "SynchronizationStatus.transmission.noConnectionError";
	public static final String ERROR_INVALID_SERVER = "SynchronizationStatus.transmission.invalidServer";

	// error message codes - at the item level
	public static final String ERROR_ITEM_NOT_COMMITTED = "SynchronizationStatus.item.notCommitted";
	public static final String ERROR_ITEM_NOT_PROCESSED = "SynchronizationStatus.item.notProcessed";
	public static final String ERROR_ITEM_NOCLASS = "SynchronizationStatus.item.noClassFound";
	public static final String ERROR_ITEM_NOSETTER = "SynchronizationStatus.item.noSetter";
	public static final String ERROR_ITEM_BADXML_ROOT = "SynchronizationStatus.item.badXml.root";
	public static final String ERROR_ITEM_BADXML_MISSING = "SynchronizationStatus.item.badXml.missing";
	public static final String ERROR_ITEM_INVALID_FIELDVAL = "SynchronizationStatus.item.invalid.fieldVal";
	public static final String ERROR_ITEM_UNSET_PROPERTY = "SynchronizationStatus.item.unsetProperty";
	
	// error-induced filenames
	public static final String FILENAME_NO_RESPONSE = "no_response_from_server";
	public static final String FILENAME_NOT_CREATED = "unable_to_create_transmission";
	public static final String FILENAME_NO_PARENT_DEFINED = "no_parent_defined";
	public static final String FILENAME_SEND_FAILED = "send_failed";
	public static final String FILENAME_RESPONSE_NOT_UNDERSTOOD = "response_not_understood";
	public static final String FILENAME_AUTH_FAILED = "not_authenticated";
	public static final String FILENAME_TX_NOT_UNDERSTOOD = "transmission_not_understood";
	public static final String FILENAME_NO_CONNECTION = "no_connection";
	public static final String FILENAME_INVALID_SERVER = "invalid_server";
	public static final String FILENAME_TEST = "test";
	
	public static final String GUID_UNKNOWN = "";

	public static final String UTF8 = "UTF-8";
	public static final String POST_METHOD = "POST";
    
    public static final String SERVER_GUID = "synchronization.server_guid";
    public static final String SYNC_ENABLED = "synchronization.sync_enabled"; //boolean

	public static final String RESPONSE_SUFFIX = "_response";

	public static final String DIR_IMPORT = "import";
	public static final String DIR_JOURNAL = "journal";

	public static final String SCHEDULED_TASK_CLASS = SynchronizationTask.class.getName();
	public static final String SCHEDULED_TASK_PROPERTY_SERVER_ID = "serverId";

	public static final String DEFAULT_PARENT_SCHEDULE_NAME = "SynchronizationStatus.parent.schedule.default.name";
	public static final String DEFAULT_PARENT_SCHEDULE_DESCRIPTION = "SynchronizationStatus.parent.schedule.default.description";

    public static final String DEFAULT_CHILD_SERVER_USER_GENDER = "M";
    public static final String DEFAULT_CHILD_SERVER_USER_NAME = "SynchronizationConfig.child.user.name";
}
