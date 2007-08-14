package org.openmrs.api.impl;

import java.util.Date;
import java.util.List;

import org.openmrs.api.APIException;
import org.openmrs.api.SynchronizationService;
import org.openmrs.api.db.SynchronizationDAO;
import org.openmrs.synchronization.engine.SyncRecord;
import org.openmrs.synchronization.engine.SyncRecordState;

public class SynchronizationServiceImpl implements SynchronizationService {

    private SynchronizationDAO dao;
    
    private SynchronizationDAO getSynchronizationDAO() {
        return dao;
    }
    
    public void setSynchronizationDAO(SynchronizationDAO dao) {
        this.dao = dao;
    }
    
    /**
     * @see org.openmrs.api.SynchronizationService#createSyncRecord(org.openmrs.synchronization.engine.SyncRecord)
     */
    public void createSyncRecord(SyncRecord record) throws APIException {
        getSynchronizationDAO().createSyncRecord(record);
    }

    /**
     * @see org.openmrs.api.SynchronizationService#getNextSyncRecord()
     */
    public SyncRecord getFirstSyncRecordInQueue() throws APIException {
        return getSynchronizationDAO().getFirstSyncRecordInQueue();
    }

    /**
     * @see org.openmrs.api.SynchronizationService#getSyncRecord(java.lang.String)
     */
    public SyncRecord getSyncRecord(String guid) throws APIException {
        return getSynchronizationDAO().getSyncRecord(guid);
    }

    /**
     * @see org.openmrs.api.SynchronizationService#getSyncRecords()
     */
    public List<SyncRecord> getSyncRecords() throws APIException {
        return getSynchronizationDAO().getSyncRecords();
    }

    /**
     * @see org.openmrs.api.SynchronizationService#getSyncRecords(org.openmrs.synchronization.engine.SyncRecordState)
     */
    public List<SyncRecord> getSyncRecords(SyncRecordState state)
            throws APIException {
        return getSynchronizationDAO().getSyncRecords(state);
    }

    /**
     * @see org.openmrs.api.SynchronizationService#updateSyncRecord(org.openmrs.synchronization.engine.SyncRecord)
     */
    public void updateSyncRecord(SyncRecord record) throws APIException {
        getSynchronizationDAO().updateSyncRecord(record);
    }

    /**
     * @see org.openmrs.api.SynchronizationService#deleteSyncRecord(org.openmrs.synchronization.engine.SyncRecord)
     */
    public void deleteSyncRecord(SyncRecord record) throws APIException {
        System.out.println("*** Calling Service impl: delete: " + record.getGuid());
        getSynchronizationDAO().deleteSyncRecord(record);
    }

    /**
     * @see org.openmrs.api.SynchronizationService#getSyncRecordsSince(java.util.Date)
     */
    public List<SyncRecord> getSyncRecordsSince(Date timestamp) throws APIException {
        return getSynchronizationDAO().getSyncRecordsSince(timestamp);
    }

    /**
     * @see org.openmrs.api.SynchronizationService#getSyncRecordsBetween(java.util.Date, java.util.Date)
     */
    public List<SyncRecord> getSyncRecordsBetween(Date from, Date to)
            throws APIException {
        return getSynchronizationDAO().getSyncRecordsBetween(from, to);
    }
}
