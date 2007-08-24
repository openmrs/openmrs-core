package org.openmrs.api.db.hibernate;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Interceptor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openmrs.api.APIException;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.SynchronizationDAO;
import org.openmrs.GlobalProperty;
import org.openmrs.synchronization.engine.SyncRecord;
import org.openmrs.synchronization.engine.SyncRecordState;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

public class HibernateSynchronizationDAO implements SynchronizationDAO {

    protected final Log log = LogFactory.getLog(getClass());

    /**
     * Hibernate session factory
     */
    private SessionFactory sessionFactory;
    
    private Interceptor nonSynchronizingInterceptor = new HibernateNonSynchronizingInterceptor();
    
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
        return SessionFactoryUtils.getNewSession(sessionFactory, nonSynchronizingInterceptor);
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
            .addOrder(Order.asc("timestamp"))
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
            .addOrder(Order.asc("timestamp"))
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
            .addOrder(Order.asc("timestamp"))
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
            .addOrder(Order.asc("timestamp"))
            .list();
    }

    
    /**
     * @see org.openmrs.api.db.SynchronizationDAO#getGlobalProperty(String propertyName)
     */
    @SuppressWarnings("unchecked")
    public String getGlobalProperty(String propertyName) 
        throws DAOException {
        
        if (propertyName == null)
            throw new DAOException("Cannot retrieve property with null property name.");

        GlobalProperty gp = (GlobalProperty)this.getNonSynchronizingSession().get(GlobalProperty.class, propertyName);
        
        if (gp == null)
            return null;

        return gp.getPropertyValue();    
        
    }
    
    /**
     * @see org.openmrs.api.db.SynchronizationDAO#setGlobalProperty(String propertyName, String propertyValue)
     */
    @SuppressWarnings("unchecked")
    public void setGlobalProperty(String propertyName, String propertyValue) 
        throws DAOException {
        
        if (propertyName == null)
            throw new DAOException("Cannot set property with null property name.");

        Session session = getNonSynchronizingSession();
        session.merge(new GlobalProperty(propertyName,propertyValue));
        session.flush();
    }

}
