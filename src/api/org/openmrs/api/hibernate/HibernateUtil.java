package org.openmrs.api.hibernate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {

	private static final Log log = LogFactory.getLog("org.openmrs.api.hibernate.HibernateUtil");
	
	private static final SessionFactory sessionFactory;

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

	public static final ThreadLocal<Session> threadLocalSession = new ThreadLocal<Session>();
	public static final ThreadLocal<Transaction> threadLocalTransaction = new ThreadLocal<Transaction>();

	public static Session currentSession() throws HibernateException {
		Session s = (Session) threadLocalSession.get();
		// Open a new threadLocalSession, if this Thread has none yet
		if (s == null) {
			s = sessionFactory.openSession();
			threadLocalSession.set(s);
		}
		else if (!s.isOpen() || !s.isConnected()) {
			s.reconnect();
		}
		Transaction tx = (Transaction) threadLocalTransaction.get();
		if (tx == null) {
			tx = s.beginTransaction();
			threadLocalTransaction.set(tx);
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
		
		//commit transaction
		Transaction tx = (Transaction) threadLocalTransaction.get();
		if (tx != null && !tx.wasCommitted() && !tx.wasRolledBack())
			tx.commit();
		threadLocalTransaction.set(null);
		
		//clost session
		Session s = (Session) threadLocalSession.get();
		threadLocalSession.set(null);
		if (s != null) {
			log.debug("closing threadLocalSession");
			s.close();
		}
	}
	
	public static void shutdown() throws HibernateException {
		if (sessionFactory != null) {
			log.debug("shutting down threadLocalSession factory");
			sessionFactory.close();
		}
	}
}
