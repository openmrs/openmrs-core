package org.openmrs.api.db.hibernate;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.stat.QueryStatistics;
import org.hibernate.stat.Statistics;
import org.hibernate.util.ConfigHelper;
import org.openmrs.util.OpenmrsConstants;

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
	
	public static void clear() {
		currentSession().clear();
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
	
	public static void startup(Properties properties) throws HibernateException {
		log.debug("Loading override properties into hibernate configuration");
		// load the default configuration from (hibernate.cfg.xml)
		Configuration cfg = new Configuration();
		
		log.debug("Setting properties");
		
		// loop over properties and override each in the configuration
		for (Object key : properties.keySet()) {
			String prop = (String)key;
			String value = (String)properties.get(key);
			log.debug("Setting property: " + prop + ":" + value);
			cfg.setProperty(prop, value);
			if (!prop.startsWith("hibernate"))
				cfg.setProperty("hibernate." + prop, value);
		}
		
		startupWithConfig(cfg);
	}

	public static void startup() throws HibernateException {
		log.debug("Loading default configuration.");
		startupWithConfig(new Configuration());
	}
	
	public static void startupWithConfig(Configuration cfg) throws HibernateException {
		try {
			// Must use hibernate.default.properties because Hibernate doesn't obey 
			//  the standard Properties override rules.  Hibernate will not fix this 'bug':
			//  http://opensource.atlassian.com/projects/hibernate/browse/HHH-1676
			log.debug("Loading /hibernate.default.properties");
			InputStream propertyStream = ConfigHelper.getResourceAsStream("/hibernate.default.properties");
			Properties props = new Properties();
			props.load(propertyStream);
			propertyStream.close();

			// Only load in the default properties if they don't exist
			cfg.mergeProperties(props);
			
			cfg.configure();
			
			if (log.isDebugEnabled()) {
				Properties ps = cfg.getProperties();
				for (Object key : ps.keySet()) {
					String prop = (String)key;
					String value = (String)ps.get(key);
					log.debug("hibernate properties: " + prop + ":" + value);
				}
			}
			
			// Create the sessionFactory
			log.debug("Creating sessionFactory");
			sessionFactory = cfg.buildSessionFactory();
		} catch (Throwable ex) {
			// Make sure you log the exception, as it might be swallowed
			log.error("Initial sessionFactory creation failed.", ex);
			throw new ExceptionInInitializerError(ex);
		}
		
		// TODO generify and/or move these to business type layer
		checkDatabaseVersion();
		checkCoreDataSet();
	}
	
	public static void shutdown() throws HibernateException {
		
		if (log.isInfoEnabled())
			showUsageStatistics();
		
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
	
	/**
	 * Selects the current database version out of the database from
	 * global_property.property = 'database_version'
	 * 
	 * Sets OpenmrsConstants.DATABASE_VERSION accordingly
	 *
	 */
	private static void checkDatabaseVersion() {
		String sql = "SELECT property_value FROM global_property WHERE property = 'database_version'";
		try {
			// Get the properties 
			ResultSet result = currentSession().connection().prepareStatement(sql).executeQuery();
			
			while (result.next()) {
				// assumes only the property_value was selected
				OpenmrsConstants.DATABASE_VERSION = result.getString(1);
			}
		} catch (SQLException e) {
			log.error("Error while getting database version", e);
		}
	}
	
	private static void checkCoreDataSet() {
		Connection conn = currentSession().connection();
		
		try {
			PreparedStatement psSelect = conn.prepareStatement("SELECT * FROM role WHERE UPPER(role) = UPPER(?)");  
			PreparedStatement psInsert = conn.prepareStatement("INSERT INTO role VALUES (?, 'Core Role')");
			
			for (String role : OpenmrsConstants.CORE_ROLES()) {
				psSelect.setString(1, role);
				ResultSet result = psSelect.executeQuery();
				if (!result.next()) {
					psInsert.setString(1, role);
					psInsert.execute();
				}
			}
			
			psSelect = conn.prepareStatement("SELECT * FROM privilege WHERE UPPER(privilege) = UPPER(?)");  
			psInsert = conn.prepareStatement("INSERT INTO privilege VALUES (?, 'Core Privilege')");
			
			for (String priv : OpenmrsConstants.CORE_PRIVILEGES()) {
				psSelect.setString(1, priv);
				ResultSet result = psSelect.executeQuery();
				if (!result.next()) {
					psInsert.setString(1, priv);
					psInsert.execute();
				}
			}
			
			conn.commit();
		}
		catch (SQLException e) {
			log.error("Error while setting core dataset", e);
		}
	}
	
	private static void showUsageStatistics() {
		 if (sessionFactory.getStatistics().isStatisticsEnabled()) {
				log.debug("Getting query statistics: ");
				Statistics stats = sessionFactory.getStatistics();
				for (String query : stats.getQueries()) {
					log.info("QUERY: " + query);
					QueryStatistics qstats = stats.getQueryStatistics(query);
					log.info("Cache Hit Count : " + qstats.getCacheHitCount());
					log.info("Cache Miss Count: " + qstats.getCacheMissCount());
					log.info("Cache Put Count : " + qstats.getCachePutCount());
					log.info("Execution Count : " + qstats.getExecutionCount());
					log.info("Average time    : " + qstats.getExecutionAvgTime());
					log.info("Row Count       : " + qstats.getExecutionRowCount());
				}
		 }
		 
	}
	
}