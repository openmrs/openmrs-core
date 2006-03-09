package org.openmrs.api.db.hibernate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {

	private static Log log = LogFactory.getLog("org.openmrs.api.db.hibernate.HibernateUtil");
	
	private static SessionFactory sessionFactory;
	private static ThreadLocal<Session> threadLocalSession = new ThreadLocal<Session>();
	private static ThreadLocal<Transaction> threadLocalTransaction = new ThreadLocal<Transaction>();

	public static Session currentSession() throws HibernateException {
		
		log.debug("getting session");
		Session s = (Session) threadLocalSession.get();
		// Open a new threadLocalSession, if this Thread has none yet
		if (s == null) {
			s = sessionFactory.openSession();
			s.setFlushMode(FlushMode.COMMIT);
			threadLocalSession.set(s);
		}
		else if (!s.isOpen() || !s.isConnected()) {
			s.reconnect(s.connection());
		}

		return s;
	}

	public static void disconnectSession() throws HibernateException {
		Session s = (Session) threadLocalSession.get();
		if (s != null) {
			log.debug("disconnecting threadLocalSession");
			s.disconnect();
		}
	}
	
	public static void closeSession() throws HibernateException {
		
		if (log.isDebugEnabled() && sessionFactory.getStatistics().isStatisticsEnabled()) {
			log.debug("Session Entity Count: " + currentSession().getStatistics().getEntityCount());

			log.debug("Displaying second level cache stats");
			log.debug("Cache Load count: " + sessionFactory.getStatistics().getEntityLoadCount());
			log.debug("Cache Put count: " + sessionFactory.getStatistics().getSecondLevelCachePutCount());
			log.debug("Cache Hit count: " + sessionFactory.getStatistics().getSecondLevelCacheHitCount());
			log.debug("Cache Miss count: " + sessionFactory.getStatistics().getSecondLevelCacheMissCount());
			
			/*
			String[] regions = sessionFactory.getStatistics()
	        	.getSecondLevelCacheRegionNames();
			for (String region : regions) {
				log.debug("region: " + region);
				Map<Object, Object> cacheEntries = sessionFactory.getStatistics()
		        	.getSecondLevelCacheStatistics(region)
		        	.getEntries();
				for (Object key : cacheEntries.keySet()) {
					log.debug("entries.key: " + key);
					log.debug("entries.value: " + cacheEntries.get(key));
				}
			}
			*/
		}
		
		log.debug("attempting to close session");
		
		//if committing errors out, too bad: the session gets closed anyway.
		try {
			commitTransaction();
		}
		catch (Exception e) {
			log.error(e);
		}
		finally {
			//close session
			Session s = (Session) threadLocalSession.get();
			if (s != null) {
				log.debug("Closing session");
				s.close();
			}
			else
				log.debug("Couldn't close session");
			threadLocalSession.remove();
		}
	}
	
	public static void beginTransaction() throws HibernateException {
		
		log.debug("beginning transaction");
		
		Transaction tx = (Transaction) threadLocalTransaction.get();
		if (tx == null) {
			tx = currentSession().beginTransaction();
			threadLocalTransaction.set(tx);
		}
	}
	
	public static void commitTransaction() throws HibernateException {
		
		log.debug("committing transaction");
		
		Transaction tx = (Transaction) threadLocalTransaction.get();
		try {
			if (tx != null && !tx.wasCommitted() && !tx.wasRolledBack())
				tx.commit();
			threadLocalTransaction.set(null);
		}
		catch (HibernateException e) {
			rollbackTransaction();
			throw e;
		}
	}
	
	public static void rollbackTransaction() throws HibernateException {
		
		log.debug("rolling back transaction");
		
		Transaction tx = (Transaction) threadLocalTransaction.get();
		if (tx != null && !tx.wasCommitted() && !tx.wasRolledBack()) {
			tx.rollback();
		}
		threadLocalTransaction.set(null);
	}
	
	public static void startup() throws HibernateException {
		try {
			// Create the sessionFactory
			log.debug("Creating sessionFactory");
			sessionFactory = new Configuration().configure()
					.buildSessionFactory();
		} catch (Throwable ex) {
			// Make sure you log the exception, as it might be swallowed
			log.error("Initial sessionFactory creation failed." + ex);
			throw new ExceptionInInitializerError(ex);
		}
	}
	
	public static void shutdown() throws HibernateException {
		if (sessionFactory != null) {
			
			log.debug("Closing any open sessions");	
			closeSession();
			log.debug("Shutting down threadLocalSession factory");
			
			sessionFactory.close();
			log.debug("The threadLocalSession has been closed");
			
			log.debug("Setting static variables to null");
			sessionFactory = null;
		}
		else
			log.error("SessionFactory is null");
		
		threadLocalSession.remove();
		threadLocalTransaction.remove();
		
		threadLocalSession = null;
		threadLocalTransaction = null;
	}
	
}