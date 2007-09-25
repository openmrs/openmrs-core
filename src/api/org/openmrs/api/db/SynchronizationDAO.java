package org.openmrs.api.db;

import java.util.Date;
import java.util.List;

import org.openmrs.api.APIException;
import org.openmrs.synchronization.engine.SyncRecord;
import org.openmrs.synchronization.engine.SyncRecordState;
import org.openmrs.synchronization.ingest.SyncImportRecord;

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
     * Create a new SyncImportRecord
     * @param SyncImportRecord The SyncImportRecord to create
     * @throws DAOException
     */
    public void createSyncImportRecord(SyncImportRecord record) throws DAOException;
    
    /**
     * Update a SyncImportRecord
     * @param SyncImportRecord The SyncImportRecord to update
     * @throws DAOException
     */
    public void updateSyncImportRecord(SyncImportRecord record) throws DAOException;
    
    /**
     * Delete a SyncImportRecord
     * @param SyncImportRecord The SyncImportRecord to delete
     * @throws DAOException
     */
    public void deleteSyncImportRecord(SyncImportRecord record) throws DAOException;
    
    /**
     * 
     * @param guid of the SyncImportRecord to retrieve
     * @return SyncImportRecord The SyncImportRecord or null if not found
     * @throws DAOException
     */
    public SyncImportRecord getSyncImportRecord(String guid) throws DAOException;

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
     * Get all SyncRecords in specific SyncRecordStates
     * @param state SyncRecordStates for the SyncRecords to be returned
     * @return SyncRecord A list containing all SyncRecords with the given states
     * @throws DAOException
     */
    public List<SyncRecord> getSyncRecords(SyncRecordState[] states) throws DAOException;

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

    /**
     * 
     * Retrieve value of given global property using synchronization data access meachnisms.
     * 
     * @param propertyName
     * @return
     */
    public String getGlobalProperty(String propertyName);
    
    /**
     * Set global property related to synchronization; notably bypasses any changeset recording mechanisms.
     * @param propertyName String specifying property name which value is to be set.
     * @param propertyValue String specifying property value to be set.
     * @throws APIException
     */
    public void setGlobalProperty(String propertyName, String propertyValue) throws APIException;    
}
