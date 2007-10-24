/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.api.db.hibernate;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openmrs.GlobalProperty;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.SynchronizationDAO;
import org.openmrs.synchronization.SyncRecordState;
import org.openmrs.synchronization.engine.SyncRecord;
import org.openmrs.synchronization.filter.SyncClass;
import org.openmrs.synchronization.ingest.SyncImportRecord;
import org.openmrs.synchronization.server.RemoteServer;
import org.openmrs.synchronization.server.RemoteServerType;

public class HibernateSynchronizationDAO implements SynchronizationDAO {

    protected final Log log = LogFactory.getLog(getClass());

    /**
     * Hibernate session factory
     */
    private SessionFactory sessionFactory;
    
    private HibernateSynchronizationInterceptor synchronizationInterceptor;
    
    public HibernateSynchronizationDAO() { }
    
    /**
     * Set session Factory interceptor
     * 
     * @param sessionFactory
     */
    public void setSessionFactory(SessionFactory sessionFactory) { 
        this.sessionFactory = sessionFactory;
    }
    
    /**
     * Set synchronization interceptor
     * 
     * @param sessionFactory
     */
    public void setSynchronizationInterceptor(HibernateSynchronizationInterceptor synchronizationInterceptor) { 
        this.synchronizationInterceptor = synchronizationInterceptor;
    }
        
    /**
     * @see org.openmrs.api.db.SynchronizationDAO#createSyncRecord(org.openmrs.synchronization.engine.SyncRecord)
     */
    public void createSyncRecord(SyncRecord record) throws DAOException {
        if (record.getGuid() == null) {
            //TODO: Create Guid if missing?
            throw new DAOException("SyncRecord must have a GUID");
        }
        
        Session session = sessionFactory.getCurrentSession();
        session.save(record);
    }

    /**
     * @see org.openmrs.api.db.SynchronizationDAO#updateSyncRecord(org.openmrs.synchronization.engine.SyncRecord)
     */
    public void updateSyncRecord(SyncRecord record) throws DAOException {
        Session session = sessionFactory.getCurrentSession();
        session.saveOrUpdate(record);
    }

    /**
     * @see org.openmrs.api.db.SynchronizationDAO#deleteSyncRecord(org.openmrs.synchronization.engine.SyncRecord)
     */
    public void deleteSyncRecord(SyncRecord record) throws DAOException {
        Session session = sessionFactory.getCurrentSession();
        session.delete(record);
    }

    /**
     * @see org.openmrs.api.db.SynchronizationDAO#createSyncImportRecord(org.openmrs.synchronization.engine.SyncImportRecord)
     */
    public void createSyncImportRecord(SyncImportRecord record) throws DAOException {
        if (record.getGuid() == null) {
            //TODO: Create Guid if missing?
            throw new DAOException("SyncImportRecord must have a GUID");
        }
        Session session = sessionFactory.getCurrentSession();
        session.save(record);
    }

    /**
     * @see org.openmrs.api.db.SynchronizationDAO#updateSyncImportRecord(org.openmrs.synchronization.engine.SyncImportRecord)
     */
    public void updateSyncImportRecord(SyncImportRecord record) throws DAOException {
        Session session = sessionFactory.getCurrentSession();
        session.merge(record);
    }

    /**
     * @see org.openmrs.api.db.SynchronizationDAO#deleteSyncImportRecord(org.openmrs.synchronization.engine.SyncImportRecord)
     */
    public void deleteSyncImportRecord(SyncImportRecord record) throws DAOException {
        Session session = sessionFactory.getCurrentSession();
        session.delete(record);
    }
    
    /**
     * @see org.openmrs.api.db.SynchronizationDAO#getNextSyncRecord()
     */
    @SuppressWarnings("unchecked")
    public SyncRecord getFirstSyncRecordInQueue() throws DAOException {
        List<SyncRecord> result = sessionFactory.getCurrentSession()
            .createCriteria(SyncRecord.class)
            .add(Restrictions.in("state", new SyncRecordState[]{SyncRecordState.NEW, SyncRecordState.PENDING_SEND}))
            .addOrder(Order.asc("timestamp"))
            .addOrder(Order.asc("recordId"))
            .setFetchSize(1)
            .list();
        
        if (result.size() < 1) {
            return null;
        } else {
            return result.get(0);
        }
    }

    /**
     * @see org.openmrs.api.db.SynchronizationDAO#getNextSyncRecord()
     */
    @SuppressWarnings("unchecked")
    public SyncRecord getLatestRecord() throws DAOException {
        List<SyncRecord> result = sessionFactory.getCurrentSession()
            .createCriteria(SyncRecord.class)
            .addOrder(Order.desc("timestamp"))
            .addOrder(Order.desc("recordId"))
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
        return (SyncRecord) sessionFactory.getCurrentSession()
        		.createCriteria(SyncRecord.class)
        		.add(Restrictions.eq("guid", guid)) 
        		.uniqueResult();
    }

    public SyncRecord getSyncRecordByOriginalGuid(String originalGuid) throws DAOException {
        return (SyncRecord) sessionFactory.getCurrentSession()
                .createCriteria(SyncRecord.class)
                .add(Restrictions.eq("originalGuid", originalGuid)) 
                .uniqueResult();
    }

    /**
     * @see org.openmrs.api.db.SynchronizationDAO#getSyncImportRecord(java.lang.String)
     */
    public SyncImportRecord getSyncImportRecord(String guid) throws DAOException {
        return (SyncImportRecord) sessionFactory.getCurrentSession()
        		.createCriteria(SyncImportRecord.class)
        		.add(Restrictions.eq("guid", guid))
        		.uniqueResult();
    }

    /**
     * @see org.openmrs.api.db.SynchronizationDAO#getSyncRecords()
     */
    @SuppressWarnings("unchecked")
    public List<SyncRecord> getSyncRecords() throws DAOException {
        return sessionFactory.getCurrentSession()
            .createCriteria(SyncRecord.class)
            .addOrder(Order.asc("timestamp"))
            .addOrder(Order.asc("recordId"))
            .list();
    }

    /**
     * @see org.openmrs.api.db.SynchronizationDAO#getSyncRecords(org.openmrs.synchronization.engine.SyncRecordState)
     */
    @SuppressWarnings("unchecked")
    public List<SyncRecord> getSyncRecords(SyncRecordState state) throws DAOException {
        return sessionFactory.getCurrentSession()
            .createCriteria(SyncRecord.class)
            .add(Restrictions.eq("state", state))
            .addOrder(Order.asc("timestamp"))
            .addOrder(Order.asc("recordId"))
            .list();
    }

    /**
     * @see org.openmrs.api.db.SynchronizationDAO#getSyncRecords(org.openmrs.synchronization.engine.SyncRecordState)
     */
    @SuppressWarnings("unchecked")
    public List<SyncRecord> getSyncRecords(SyncRecordState[] states, boolean inverse) throws DAOException {
    	if ( inverse ) {
            return sessionFactory.getCurrentSession()
            .createCriteria(SyncRecord.class)
            .add(Restrictions.not(Restrictions.in("state", states)))
            .addOrder(Order.asc("timestamp"))
            .addOrder(Order.asc("recordId"))
            .list();
    	} else {
            return sessionFactory.getCurrentSession()
            .createCriteria(SyncRecord.class)
            .add(Restrictions.in("state", states))
            .addOrder(Order.asc("timestamp"))
            .addOrder(Order.asc("recordId"))
            .list();
    	}
    }

    @SuppressWarnings("unchecked")
    public List<SyncRecord> getSyncRecords(SyncRecordState[] states, boolean inverse, RemoteServer server) throws DAOException {
        if ( inverse ) {
            return sessionFactory.getCurrentSession()
            .createCriteria(SyncRecord.class, "s")
            .createCriteria("serverRecords", "sr")
            .add(Restrictions.not(Restrictions.in("sr.state", states)))
            .add(Restrictions.eq("sr.syncServer", server))
            .addOrder(Order.asc("s.timestamp"))
            .addOrder(Order.asc("s.recordId"))
            .list();
        } else {
            return sessionFactory.getCurrentSession()
            .createCriteria(SyncRecord.class, "s")
            .createCriteria("serverRecords", "sr")
            .add(Restrictions.in("sr.state", states))
            .add(Restrictions.eq("sr.syncServer", server))
            .addOrder(Order.asc("s.timestamp"))
            .addOrder(Order.asc("s.recordId"))
            .list();
        }
    }

    /**
     * @see org.openmrs.api.db.SynchronizationDAO#getSyncRecordsSince(java.util.Date)
     */
    @SuppressWarnings("unchecked")
    public List<SyncRecord> getSyncRecordsSince(Date from) throws DAOException {
        return sessionFactory.getCurrentSession()
            .createCriteria(SyncRecord.class)
            .add(Restrictions.gt("timestamp", from)) // greater than
            .addOrder(Order.asc("timestamp"))
            .addOrder(Order.asc("recordId"))
            .list();
    }

    /**
     * @see org.openmrs.api.db.SynchronizationDAO#getSyncRecordsBetween(java.util.Date, java.util.Date)
     */
    @SuppressWarnings("unchecked")
    public List<SyncRecord> getSyncRecordsBetween(Date from, Date to)
            throws DAOException {
        return sessionFactory.getCurrentSession()
            .createCriteria(SyncRecord.class)
            .add(Restrictions.gt("timestamp", from)) // greater than
            .add(Restrictions.le("timestamp", to)) // less-than or equal
            .addOrder(Order.asc("timestamp"))
            .addOrder(Order.asc("recordId"))
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

        GlobalProperty gp = (GlobalProperty) sessionFactory.getCurrentSession().get(GlobalProperty.class, propertyName);
        
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

        Session session = sessionFactory.getCurrentSession();
        GlobalProperty gp = new GlobalProperty(propertyName,propertyValue);
        gp.setIsSynchronizable(false); //do *not* record this change for synchronization
        session.merge(gp);
    }

    /**
     * @see org.openmrs.api.db.SynchronizationDAO#createRemoteServer(org.openmrs.synchronization.engine.RemoteServer)
     */
    public void createRemoteServer(RemoteServer record) throws DAOException {
        Session session = sessionFactory.getCurrentSession();
        session.save(record);
    }

    /**
     * @see org.openmrs.api.db.SynchronizationDAO#updateRemoteServer(org.openmrs.synchronization.engine.RemoteServer)
     */
    public void updateRemoteServer(RemoteServer record) throws DAOException {
        Session session = sessionFactory.getCurrentSession();
        session.saveOrUpdate(record);
    }

    /**
     * @see org.openmrs.api.db.SynchronizationDAO#deleteRemoteServer(org.openmrs.synchronization.engine.RemoteServer)
     */
    public void deleteRemoteServer(RemoteServer record) throws DAOException {
        Session session = sessionFactory.getCurrentSession();
        session.delete(record);
    }

    /**
     * @see org.openmrs.api.db.SynchronizationDAO#getGlobalProperty(String propertyName)
     */
    @SuppressWarnings("unchecked")
    public RemoteServer getRemoteServer(Integer serverId) throws DAOException {        
        return (RemoteServer)sessionFactory.getCurrentSession().get(RemoteServer.class, serverId);
    }

    /**
     * @see org.openmrs.api.db.SynchronizationDAO#getGlobalProperty(String propertyName)
     */
    @SuppressWarnings("unchecked")
    public RemoteServer getRemoteServer(String guid) throws DAOException {        
        return (RemoteServer)sessionFactory.getCurrentSession()
        .createCriteria(RemoteServer.class)
        .add(Restrictions.eq("guid", guid))
        .uniqueResult();
    }

    /**
     * @see org.openmrs.api.db.SynchronizationDAO#getGlobalProperty(String propertyName)
     */
    @SuppressWarnings("unchecked")
    public RemoteServer getRemoteServerByUsername(String username) throws DAOException {        
        return (RemoteServer)sessionFactory.getCurrentSession()
        .createCriteria(RemoteServer.class)
        .add(Restrictions.eq("childUsername", username))
        .uniqueResult();
    }

    /**
     * @see org.openmrs.api.db.SynchronizationDAO#getGlobalProperty(String propertyName)
     */
    @SuppressWarnings("unchecked")
    public List<RemoteServer> getRemoteServers() throws DAOException {        
        return (List<RemoteServer>)sessionFactory.getCurrentSession().createCriteria(RemoteServer.class).list();
    }

    /**
     * @see org.openmrs.api.db.SynchronizationDAO#getGlobalProperty(String propertyName)
     */
    @SuppressWarnings("unchecked")
    public RemoteServer getParentServer() throws DAOException {        
        return (RemoteServer)sessionFactory.getCurrentSession()
        		.createCriteria(RemoteServer.class)
        		.add(Restrictions.eq("serverType", RemoteServerType.PARENT))
        		.uniqueResult();
    }

    /**
     * @see org.openmrs.api.db.SynchronizationDAO#createSyncClass(org.openmrs.synchronization.engine.SyncClass)
     */
    public void createSyncClass(SyncClass syncClass) throws DAOException {
        Session session = sessionFactory.getCurrentSession();
        session.save(syncClass);
    }

    /**
     * @see org.openmrs.api.db.SynchronizationDAO#updateSyncClass(org.openmrs.synchronization.engine.SyncClass)
     */
    public void updateSyncClass(SyncClass syncClass) throws DAOException {
        Session session = sessionFactory.getCurrentSession();
        session.saveOrUpdate(syncClass);
    }

    /**
     * @see org.openmrs.api.db.SynchronizationDAO#deleteSyncClass(org.openmrs.synchronization.engine.SyncClass)
     */
    public void deleteSyncClass(SyncClass syncClass) throws DAOException {
        Session session = sessionFactory.getCurrentSession();
        session.delete(syncClass);
    }

    /**
     * @see org.openmrs.api.db.SynchronizationDAO#getGlobalProperty(String propertyName)
     */
    @SuppressWarnings("unchecked")
    public SyncClass getSyncClass(Integer syncClassId) throws DAOException {        
        return (SyncClass)sessionFactory.getCurrentSession().get(SyncClass.class, syncClassId);
    }

    /**
     * @see org.openmrs.api.db.SynchronizationDAO#getGlobalProperty(String propertyName)
     */
    @SuppressWarnings("unchecked")
    public List<SyncClass> getSyncClasses() throws DAOException {        
        
        List<SyncClass> classes = (List<SyncClass>)sessionFactory.getCurrentSession()
                .createCriteria(SyncClass.class)
                .addOrder(Order.asc("type"))
                .addOrder(Order.asc("name"))
                .list();
        
        if ( classes == null ) {
            log.warn("IN DAO, SYNCCLASSES IS NULL");
        } else {
            log.warn("IN DAO, SYNCCLASSES IS SIZE " + classes.size());
        }
        
        return classes;
    }

}
