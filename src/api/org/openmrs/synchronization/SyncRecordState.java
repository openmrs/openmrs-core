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
        
    /** the record was successfully committed at target source */
    COMMITTED, 
        
    /** the record reached the failed state during ingest: will retry next time */
    FAILED, 

    /** the record reached the final failed state: max retry attempt was reached, no more retries will be attempted */
    FAILED_AND_STOPPED, 

    
    /** we are trying again to send this record */
    SENT_AGAIN, 
    
    /** this record has already been committed */
    ALREADY_COMMITTED,
    
    /** this record is set not to sync with the referenced server */
    NOT_SUPPOSED_TO_SYNC, 
    
    /** record was sent to server, but server does not accept this type of record for sync'ing */
    REJECTED
};
