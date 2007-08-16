package org.openmrs.api;

import java.util.Date;
import java.util.List;

import org.openmrs.annotation.Authorized;
import org.openmrs.synchronization.engine.SyncRecord;
import org.openmrs.synchronization.engine.SyncRecordState;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface SynchronizationService {

    /**
     * Create a new SyncRecord
     * @param SyncRecord The SyncRecord to create
     * @throws APIException
     */
    @Authorized({"Manage Synchronization Records"})
    public void createSyncRecord(SyncRecord record) throws APIException;
    
    /**
     * Update a SyncRecord
     * @param SyncRecord The SyncRecord to update
     * @throws APIException
     */
    @Authorized({"Manage Synchronization Records"})
    public void updateSyncRecord(SyncRecord record) throws APIException;
    
    /**
     * Delete a SyncRecord
     * @param SyncRecord The SyncRecord to delete
     * @throws APIException
     */
    @Authorized({"Manage Synchronization Records"})
    public void deleteSyncRecord(SyncRecord record) throws APIException;
    
    /**
     * 
     * @param guid of the SyncRecord to retrieve
     * @return SyncRecord The SyncRecord or null if not found
     * @throws APIException
     */
    @Authorized({"View Synchronization Records"})
    @Transactional(readOnly=true)
    public SyncRecord getSyncRecord(String guid) throws APIException;
    
    /**
     * Returns the first SyncRecord in either the PENDING SEND or the NEW state
     * @return SyncRecord The first SyncRecord matching the criteria, or null if none matches
     * @throws APIException
     */
    @Authorized({"View Synchronization Records"})
    @Transactional(readOnly=true)
    public SyncRecord getFirstSyncRecordInQueue() throws APIException;
    
    /**
     * Get all SyncRecords
     * @return SyncRecord A list containing all SyncRecords
     * @throws APIException
     */
    @Authorized({"View Synchronization Records"})
    @Transactional(readOnly=true)
    public List<SyncRecord> getSyncRecords() throws APIException;
    
    /**
     * Get all SyncRecords in a specific SyncRecordState
     * @param state SyncRecordState for the SyncRecords to be returned
     * @return SyncRecord A list containing all SyncRecords with the given state
     * @throws APIException
     */
    @Authorized({"View Synchronization Records"})
    @Transactional(readOnly=true)
    public List<SyncRecord> getSyncRecords(SyncRecordState state) throws APIException;
    
    /**
     * Get all SyncRecords after a given timestamp
     * @param from Timestamp specifying lower bound, not included.
     * @return SyncRecord A list containing all SyncRecords with a timestamp after the given timestamp
     * @throws APIException
     */
    @Authorized({"View Synchronization Records"})
    @Transactional(readOnly=true)
    public List<SyncRecord> getSyncRecordsSince(Date from) throws APIException;
    
    /**
     * Get all SyncRecords between two timestamps, including the to-timestamp.
     * @param from Timestamp specifying lower bound, not included.
     * @param to Timestamp specifying upper bound, included.
     * @return SyncRecord A list containing all SyncRecords with a timestamp between the from timestamp and up to and including the to timestamp
     * @throws APIException
     */
    @Authorized({"View Synchronization Records"})
    @Transactional(readOnly=true)
    public List<SyncRecord> getSyncRecordsBetween(Date from, Date to) throws APIException;
}
