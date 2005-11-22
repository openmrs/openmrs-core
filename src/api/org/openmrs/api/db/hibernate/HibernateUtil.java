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

	static {
		try {
			// Create the sessionFactory
			sessionFactory = new Configuration().configure()
					.buildSessionFactory();
			log.debug("Creating sessionFactory");
		} catch (Throwable ex) {
			// Make sure you log the exception, as it might be swallowed
			System.err.println("Initial sessionFactory creation failed." + ex);
			throw new ExceptionInInitializerError(ex);
		}
	}

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
			s.reconnect();
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
		
		log.debug("attempting to close session");
		
		//if committing errors out, too bad: the session gets closed anyway.
		try {
			commitTransaction();
		}
		finally {
			//close session
			Session s = (Session) threadLocalSession.get();
			threadLocalSession.set(null);
			if (s != null) {
				log.debug("closing threadLocalSession");
				s.close();
			}
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
		threadLocalTransaction.set(null);
		if (tx != null && !tx.wasCommitted() && !tx.wasRolledBack()) {
			tx.rollback();
		}
	}
	
	public static void shutdown() throws HibernateException {
		if (sessionFactory != null) {
			log.debug("shutting down threadLocalSession factory");
			sessionFactory.close();
			sessionFactory = null;
			threadLocalSession = null;
			threadLocalTransaction = null;
		}
	}
}
