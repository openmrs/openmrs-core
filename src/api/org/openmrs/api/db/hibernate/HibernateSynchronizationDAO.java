package org.openmrs.api.db.hibernate;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.SynchronizationDAO;
import org.openmrs.synchronization.engine.SyncRecord;
import org.openmrs.synchronization.engine.SyncRecordState;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

public class HibernateSynchronizationDAO implements SynchronizationDAO {

    protected final Log log = LogFactory.getLog(getClass());

    /**
     * Hibernate session factory
     */
    private SessionFactory sessionFactory;
    
    public HibernateSynchronizationDAO() { }
    
    /**
     * Set session factory
     * 
     * @param sessionFactory
     */
    public void setSessionFactory(SessionFactory sessionFactory) { 
        this.sessionFactory = sessionFactory;
    }
    
    private Session getNonSynchronizingSession() {
        Session session = SessionFactoryUtils.getNewSession(sessionFactory);
        return session;
    }
    
    /**
     * @see org.openmrs.api.db.SynchronizationDAO#createSyncRecord(org.openmrs.synchronization.engine.SyncRecord)
     */
    public void createSyncRecord(SyncRecord record) throws DAOException {
        if (record.getGuid() == null) {
            //TODO: Create Guid if missing?
            throw new DAOException("SyncRecord must have a GUID");
        }
        Session session = getNonSynchronizingSession();
        session.save(record);
        session.flush();
    }

    /**
     * @see org.openmrs.api.db.SynchronizationDAO#updateSyncRecord(org.openmrs.synchronization.engine.SyncRecord)
     */
    public void updateSyncRecord(SyncRecord record) throws DAOException {
        Session session = getNonSynchronizingSession();
        session.saveOrUpdate(record);
        session.flush();
    }

    /**
     * @see org.openmrs.api.db.SynchronizationDAO#deleteSyncRecord(org.openmrs.synchronization.engine.SyncRecord)
     */
    public void deleteSyncRecord(SyncRecord record) throws DAOException {
        Session session = getNonSynchronizingSession();
        session.delete(record);
        session.flush();
    }
    
    /**
     * @see org.openmrs.api.db.SynchronizationDAO#getNextSyncRecord()
     */
    @SuppressWarnings("unchecked")
    public SyncRecord getFirstSyncRecordInQueue() throws DAOException {
        List<SyncRecord> result = getNonSynchronizingSession()
            .createCriteria(SyncRecord.class)
            .add(Restrictions.in("state", new SyncRecordState[]{SyncRecordState.NEW, SyncRecordState.PENDING_SEND}))
            .addOrder(Order.asc("timestamp"))
            .setFetchSize(1)
            .list();
        
        if (result.size() < 1) {
            return null;
        } else {
            return result.get(0);
        }
    }

    /**
     * @see org.openmrs.api.db.SynchronizationDAO#getSyncRecord(java.lang.String)
     */
    public SyncRecord getSyncRecord(String guid) throws DAOException {
        return (SyncRecord) getNonSynchronizingSession().get(SyncRecord.class, guid);
    }

    /**
     * @see org.openmrs.api.db.SynchronizationDAO#getSyncRecords()
     */
    @SuppressWarnings("unchecked")
    public List<SyncRecord> getSyncRecords() throws DAOException {
        return getNonSynchronizingSession()
            .createCriteria(SyncRecord.class)
            .list();
    }

    /**
     * @see org.openmrs.api.db.SynchronizationDAO#getSyncRecords(org.openmrs.synchronization.engine.SyncRecordState)
     */
    @SuppressWarnings("unchecked")
    public List<SyncRecord> getSyncRecords(SyncRecordState state) throws DAOException {
        return getNonSynchronizingSession()
            .createCriteria(SyncRecord.class)
            .add(Restrictions.eq("state", state))
            .list();
    }
    
    /**
     * @see org.openmrs.api.db.SynchronizationDAO#getSyncRecordsSince(java.util.Date)
     */
    @SuppressWarnings("unchecked")
    public List<SyncRecord> getSyncRecordsSince(Date from) throws DAOException {
        return getNonSynchronizingSession()
            .createCriteria(SyncRecord.class)
            .add(Restrictions.gt("timestamp", from)) // greater than
            .list();
    }

    /**
     * @see org.openmrs.api.db.SynchronizationDAO#getSyncRecordsBetween(java.util.Date, java.util.Date)
     */
    @SuppressWarnings("unchecked")
    public List<SyncRecord> getSyncRecordsBetween(Date from, Date to)
            throws DAOException {
        return getNonSynchronizingSession()
            .createCriteria(SyncRecord.class)
            .add(Restrictions.gt("timestamp", from)) // greater than
            .add(Restrictions.le("timestamp", to)) // less-than or equal
            .list();
    }
}
