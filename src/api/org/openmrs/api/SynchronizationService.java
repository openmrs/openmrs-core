package org.openmrs.api;

import java.util.Date;
import java.util.List;

import org.openmrs.synchronization.engine.SyncRecord;
import org.openmrs.synchronization.engine.SyncRecordState;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface SynchronizationService {

    /**
     * Create a new SyncRecord
     * @param SyncRecord to create
     * @throws APIException
     */
    //FIXME: @Authorized({Add Synchronization Record (Admin/System)?})
    public void createSyncRecord(SyncRecord record) throws APIException;
    
    /**
     * Update a SyncRecord
     * @param SyncRecord to update
     * @throws APIException
     */
    //FIXME: @Authorized({Edit Synchronization Record (Admin/System?)})
    public void updateSyncRecord(SyncRecord record) throws APIException;
    
    /**
     * Delete a SyncRecord
     * @param SyncRecord to delete
     * @throws APIException
     */
    //FIXME: @Authorized({Delete Synchronization Record (Admin/System?)})
    public void deleteSyncRecord(SyncRecord record) throws APIException;
    
    /**
     * 
     * @param guid of the SyncRecord to retrieve
     * @return SyncRecord
     * @throws APIException
     */
    //@Transactional(readOnly=true)
    public SyncRecord getSyncRecord(String guid) throws APIException;
    
    /**
     * Returns the first SyncRecord in either Pending_Send or New state
     * @return SyncRecord First SyncRecord matching the criteria, or null if none matches
     * @throws APIException
     */
    //@Transactional(readOnly=true)
    public SyncRecord getFirstSyncRecordInQueue() throws APIException;
    
    /**
     * Get all SyncRecords
     * @return SyncRecord list
     * @throws APIException
     */
    //@Transactional(readOnly=true)
    public List<SyncRecord> getSyncRecords() throws APIException;
    
    /**
     * Get all SyncRecords in a specific SyncRecordState
     * @param state SyncRecordState for the SyncRecords to be returned
     * @return SyncRecord list
     * @throws APIException
     */
    //@Transactional(readOnly=true)
    public List<SyncRecord> getSyncRecords(SyncRecordState state) throws APIException;
    
    /**
     * Get all SyncRecords since a timestamp
     * @param timestamp
     * @return SyncRecord list
     * @throws APIException
     */
    //@Transactional(readOnly=true)
    public List<SyncRecord> getSyncRecordsSince(Date timestamp) throws APIException;
    
    /**
     * Get all SyncRecords between two timestamps, including the to-timestamp.
     * @param from
     * @param to
     * @return SyncRecord list
     * @throws APIException
     */
    //@Transactional(readOnly=true)
    public List<SyncRecord> getSyncRecordsBetween(Date from, Date to) throws APIException;
}
