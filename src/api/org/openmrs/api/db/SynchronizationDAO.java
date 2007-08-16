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
     * @param SyncRecord The SyncRecord to create
     * @throws DAOException
     */
    public void createSyncRecord(SyncRecord record) throws DAOException;
    
    /**
     * Update a SyncRecord
     * @param SyncRecord The SyncRecord to update
     * @throws DAOException
     */
    public void updateSyncRecord(SyncRecord record) throws DAOException;
    
    /**
     * Delete a SyncRecord
     * @param SyncRecord The SyncRecord to delete
     * @throws DAOException
     */
    public void deleteSyncRecord(SyncRecord record) throws DAOException;
    
    /**
     * 
     * @param guid of the SyncRecord to retrieve
     * @return SyncRecord The SyncRecord or null if not found
     * @throws DAOException
     */
    public SyncRecord getSyncRecord(String guid) throws DAOException;
    
    /**
     * Returns the first SyncRecord in either the PENDING SEND or the NEW state
     * @return SyncRecord The first SyncRecord matching the criteria, or null if none matches
     * @throws DAOException
     */
    public SyncRecord getFirstSyncRecordInQueue() throws DAOException;
    
    /**
     * Get all SyncRecords
     * @return SyncRecord A list containing all SyncRecords
     * @throws DAOException
     */
    public List<SyncRecord> getSyncRecords() throws DAOException;
    
    /**
     * Get all SyncRecords in a specific SyncRecordState
     * @param state SyncRecordState for the SyncRecords to be returned
     * @return SyncRecord A list containing all SyncRecords with the given state
     * @throws DAOException
     */
    public List<SyncRecord> getSyncRecords(SyncRecordState state) throws DAOException;
    
    /**
     * Get all SyncRecords since a timestamp
     * @param from Timestamp specifying lower bound, not included.
     * @return SyncRecord A list containing all SyncRecords with a timestamp after the given timestamp
     * @throws DAOException
     */
    public List<SyncRecord> getSyncRecordsSince(Date from) throws DAOException;
    
    /**
     * Get all SyncRecords between two timestamps, including the to-timestamp.
     * @param from Timestamp specifying lower bound, not included.
     * @param to Timestamp specifying upper bound, included.
     * @return SyncRecord A list containing all SyncRecords with a timestamp between the from timestamp and up to and including the to timestamp
     * @throws DAOException
     */
    public List<SyncRecord> getSyncRecordsBetween(Date from, Date to) throws DAOException;
}
