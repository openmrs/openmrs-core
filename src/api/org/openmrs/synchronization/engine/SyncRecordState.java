package org.openmrs.synchronization.engine;

/**
 * State for SyncRecords. Has to be in a separate "class"/file due to Hibernate issues with restoring the objects
 */
public enum SyncRecordState {
    NEW, /* initial state of a sync record */
    PENDING_SEND, /* sync record is being sent to target sync source however it's transmission to the target sync source has not been confirmed */
    SENT, /* the record has been successful transmitted to the target source, note it may not yet be committed */
    SEND_FAILED, /* attempted send failed */
    PENDING_COMMIT, /* record is being committed at parent; outcome not yet known */
    COMMITTED, /* the record was successfully committed at target source */
    ABORTED, /* attempted commit of the record failed */
    FAILED /* the record reached the final failed state: no more retries will be attempted */
};
