package org.openmrs.synchronization;

/**
 * State for SyncRecords. Has to be in a separate "class"/file due to Hibernate issues with restoring the objects
 */
public enum SyncRecordState {
    
    /** initial state of a sync record */
    NEW, 
    
    /** sync record is being sent to target sync source however it's transmission to the target sync source has not been confirmed */
    PENDING_SEND, 
    
    /** the record has been successful transmitted to the target source, note it may not yet be committed */
    SENT,
    
    /** attempted send failed */
    SEND_FAILED,
    
    /** record is being committed at parent; outcome not yet known */
    PENDING_COMMIT, 
    
    /** the record was successfully committed at target source */
    COMMITTED, 
    
    /** attempted commit of the record failed */
    ABORTED, 
    
    /** the record reached the final failed state: no more retries will be attempted */
    FAILED, 
    
    /** we are trying again to send this record */
    SENT_AGAIN, 
    
    /** this record has already been committed */
    ALREADY_COMMITTED 
};
