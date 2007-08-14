package org.openmrs.api.db;

import java.util.Date;
import java.util.List;

import org.openmrs.synchronization.engine.SyncRecord;
import org.openmrs.synchronization.engine.SyncRecordState;

/**
 * Synchronization related database functions 
 */
public interface SynchronizationDAO {

    /**
     * Create a new SyncRecord
     * @param SyncRecord to create
     * @throws DAOException
     */
    public void createSyncRecord(SyncRecord record) throws DAOException;
    
    /**
     * Update a SyncRecord
     * @param SyncRecord to update
     * @throws DAOException
     */
    public void updateSyncRecord(SyncRecord record) throws DAOException;
    
    /**
     * Delete a SyncRecord
     * @param SyncRecord to delete
     * @throws DAOException
     */
    public void deleteSyncRecord(SyncRecord record) throws DAOException;
    
    /**
     * 
     * @param guid of the SyncRecord to retrieve
     * @return SyncRecord
     * @throws DAOException
     */
    public SyncRecord getSyncRecord(String guid) throws DAOException;
    
    /**
     * Returns the first SyncRecord in either Pending_Send or New state
     * @return SyncRecord First SyncRecord matching the criteria, or null if none matches
     * @throws DAOException
     */
    public SyncRecord getFirstSyncRecordInQueue() throws DAOException;
    
    /**
     * Get all SyncRecords
     * @return SyncRecord list
     * @throws DAOException
     */
    public List<SyncRecord> getSyncRecords() throws DAOException;
    
    /**
     * Get all SyncRecords in a specific SyncRecordState
     * @param state SyncRecordState for the SyncRecords to be returned
     * @return SyncRecord list
     * @throws DAOException
     */
    public List<SyncRecord> getSyncRecords(SyncRecordState state) throws DAOException;
    
    /**
     * Get all SyncRecords since a timestamp
     * @param timestamp
     * @return SyncRecord list
     * @throws DAOException
     */
    public List<SyncRecord> getSyncRecordsSince(Date timestamp) throws DAOException;
    
    /**
     * Get all SyncRecords between two timestamps, including the to-timestamp.
     * @param from
     * @param to
     * @return SyncRecord list
     * @throws DAOException
     */
    //@Transactional(readOnly=true)
    public List<SyncRecord> getSyncRecordsBetween(Date from, Date to) throws DAOException;
}
