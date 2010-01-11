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

import java.io.InputStream;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.FlushMode;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.stat.QueryStatistics;
import org.hibernate.stat.Statistics;
import org.hibernate.util.ConfigHelper;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.api.db.ContextDAO;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.util.Security;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Hibernate specific implementation of the {@link ContextDAO}. These methods should not be used
 * directly, instead, the methods on the static {@link Context} file should be used.
 * 
 * @see ContextDAO
 * @see Context
 */
public class HibernateContextDAO implements ContextDAO {
	
	private static Log log = LogFactory.getLog(HibernateContextDAO.class);
	
	/**
	 * Hibernate session factory
	 */
	private SessionFactory sessionFactory;
	
	/**
	 * Session factory to use for this DAO. This is usually injected by spring and its application
	 * context.
	 * 
	 * @param sessionFactory
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	/**
	 * @see org.openmrs.api.db.ContextDAO#authenticate(java.lang.String, java.lang.String)
	 */
	public User authenticate(String login, String password) throws ContextAuthenticationException {
		
		String errorMsg = "Invalid username and/or password: " + login;
		
		Session session = sessionFactory.getCurrentSession();
		
		User candidateUser = null;
		
		if (login != null) {
			
			// loginWithoutDash is used to compare to the system id
			String loginWithDash = login;
			if (login.matches("\\d{2,}"))
				loginWithDash = login.substring(0, login.length() - 1) + "-" + login.charAt(login.length() - 1);
			
			try {
				candidateUser = (User) session.createQuery(
				    "from User u where (u.username = ? or u.systemId = ? or u.systemId = ?) and u.retired = 0").setString(0,
				    login).setString(1, login).setString(2, loginWithDash).uniqueResult();
			}
			catch (HibernateException he) {
				log.error("Got hibernate exception while logging in: '" + login + "'", he);
			}
			catch (Exception e) {
				log.error("Got regular exception while logging in: '" + login + "'", e);
			}
		}
		
		// only continue if this is a valid username and a nonempty password
		if (candidateUser != null && password != null) {
			if (log.isDebugEnabled())
				log.debug("Candidate user id: " + candidateUser.getUserId());
			
			String lockoutTimeString = candidateUser.getUserProperty(OpenmrsConstants.USER_PROPERTY_LOCKOUT_TIMESTAMP, null);
			Long lockoutTime = null;
			if (lockoutTimeString != null && !lockoutTimeString.equals("0"))
				lockoutTime = Long.valueOf(lockoutTimeString);
			
			// if they've been locked out, don't continue with the authentication
			if (lockoutTime != null) {
				// unlock them after 5 mins, otherwise reset the timestamp 
				// to now and make them wait another 5 mins 
				if (new Date().getTime() - lockoutTime > 300000) {
					candidateUser.setUserProperty(OpenmrsConstants.USER_PROPERTY_LOGIN_ATTEMPTS, "0");
					candidateUser.removeUserProperty(OpenmrsConstants.USER_PROPERTY_LOCKOUT_TIMESTAMP);
					saveUserProperties(candidateUser);
				} else {
					candidateUser.setUserProperty(OpenmrsConstants.USER_PROPERTY_LOCKOUT_TIMESTAMP, String
					        .valueOf(new Date().getTime()));
					throw new ContextAuthenticationException(
					        "Invalid number of connection attempts. Please try again later.");
				}
			}
			
			String passwordOnRecord = (String) session.createSQLQuery("select password from users where user_id = ?")
			        .addScalar("password", Hibernate.STRING).setInteger(0, candidateUser.getUserId()).uniqueResult();
			
			String saltOnRecord = (String) session.createSQLQuery("select salt from users where user_id = ?").addScalar(
			    "salt", Hibernate.STRING).setInteger(0, candidateUser.getUserId()).uniqueResult();
			
			// if the username and password match, hydrate the user and return it
			if (passwordOnRecord != null && Security.hashMatches(passwordOnRecord, password + saltOnRecord)) {
				// hydrate the user object
				candidateUser.getAllRoles().size();
				candidateUser.getUserProperties().size();
				candidateUser.getPrivileges().size();
				
				// only clean up if the were some login failures, otherwise all should be clean
				Integer attempts = getUsersLoginAttempts(candidateUser);
				if (attempts > 0) {
					candidateUser.setUserProperty(OpenmrsConstants.USER_PROPERTY_LOGIN_ATTEMPTS, "0");
					candidateUser.removeUserProperty(OpenmrsConstants.USER_PROPERTY_LOCKOUT_TIMESTAMP);
					saveUserProperties(candidateUser);
				}
				
				// skip out of the method early (instead of throwing the exception)
				// to indicate that this is the valid user
				return candidateUser;
			} else {
				// the user failed the username/password, increment their
				// attempts here and set the "lockout" timestamp if necessary
				Integer attempts = getUsersLoginAttempts(candidateUser);
				
				attempts++;
				
				if (attempts >= 8) {
					// set the user as locked out at this exact time
					candidateUser.setUserProperty(OpenmrsConstants.USER_PROPERTY_LOCKOUT_TIMESTAMP, String
					        .valueOf(new Date().getTime()));
				} else {
					candidateUser.setUserProperty(OpenmrsConstants.USER_PROPERTY_LOGIN_ATTEMPTS, String.valueOf(attempts));
				}
				
				saveUserProperties(candidateUser);
			}
		}
		
		// throw this exception only once in the same place with the same
		// message regardless of username/pw combo entered
		log.info("Failed login attempt (login=" + login + ") - " + errorMsg);
		throw new ContextAuthenticationException(errorMsg);
		
	}
	
	/**
	 * Call the UserService to save the given user while proxying the privileges needed to do so.
	 * 
	 * @param user the User to save
	 */
	private void saveUserProperties(User user) {
		sessionFactory.getCurrentSession().update(user);
	}
	
	/**
	 * Get the integer stored for the given user that is their number of login attempts
	 * 
	 * @param user the user to check
	 * @return the # of login attempts for this user defaulting to zero if none defined
	 */
	private Integer getUsersLoginAttempts(User user) {
		String attemptsString = user.getUserProperty(OpenmrsConstants.USER_PROPERTY_LOGIN_ATTEMPTS, "0");
		Integer attempts = 0;
		try {
			attempts = Integer.valueOf(attemptsString);
		}
		catch (NumberFormatException e) {
			// skip over errors and leave the attempts at zero
		}
		return attempts;
	}
	
	/**
	 * @see org.openmrs.api.context.Context#openSession()
	 */
	private boolean participate = false;
	
	public void openSession() {
		log.debug("HibernateContext: Opening Hibernate Session");
		if (TransactionSynchronizationManager.hasResource(sessionFactory)) {
			if (log.isDebugEnabled())
				log.debug("Participating in existing session (" + sessionFactory.hashCode() + ")");
			participate = true;
		} else {
			if (log.isDebugEnabled())
				log.debug("Registering session with synchronization manager (" + sessionFactory.hashCode() + ")");
			Session session = SessionFactoryUtils.getSession(sessionFactory, true);
			session.setFlushMode(FlushMode.MANUAL);
			TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(session));
		}
	}
	
	/**
	 * @see org.openmrs.api.context.Context#closeSession()
	 */
	public void closeSession() {
		log.debug("HibernateContext: closing Hibernate Session");
		if (!participate) {
			log.debug("Unbinding session from synchronization manager (" + sessionFactory.hashCode() + ")");
			
			if (TransactionSynchronizationManager.hasResource(sessionFactory)) {
				Object value = TransactionSynchronizationManager.unbindResource(sessionFactory);
				try {
					if (value instanceof SessionHolder) {
						Session session = ((SessionHolder) value).getSession();
						SessionFactoryUtils.releaseSession(session, sessionFactory);
					}
				}
				catch (RuntimeException e) {
					log.error("Unexpected exception on closing Hibernate Session", e);
				}
			}
		} else {
			log.debug("Participating in existing session, so not releasing session through synchronization manager");
		}
	}
	
	/**
	 * @see org.openmrs.api.db.ContextDAO#clearSession()
	 */
	public void clearSession() {
		sessionFactory.getCurrentSession().clear();
	}
	
	/**
	 * @see org.openmrs.api.db.ContextDAO#evictFromSession(java.lang.Object)
	 */
	public void evictFromSession(Object obj) {
		sessionFactory.getCurrentSession().evict(obj);
	}
	
	/**
	 * @see org.openmrs.api.context.Context#startup(Properties)
	 */
	public void startup(Properties properties) {
		
	}
	
	/**
	 * @see org.openmrs.api.context.Context#shutdown()
	 */
	public void shutdown() {
		if (log.isInfoEnabled())
			showUsageStatistics();
		
		if (sessionFactory != null) {
			
			log.debug("Closing any open sessions");
			closeSession();
			
			log.debug("Shutting down threadLocalSession factory");
			if (!sessionFactory.isClosed())
				sessionFactory.close();
			
			log.debug("The threadLocalSession has been closed");
			
		} else
			log.error("SessionFactory is null");
		
	}
	
	/**
	 * Convenience method to print out the hibernate cache usage stats to the log
	 */
	private void showUsageStatistics() {
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
	
	/**
	 * Takes the default properties defined in /metadata/api/hibernate/hibernate.default.properties
	 * and merges it into the user-defined runtime properties
	 * 
	 * @see org.openmrs.api.db.ContextDAO#mergeDefaultRuntimeProperties(java.util.Properties)
	 */
	public void mergeDefaultRuntimeProperties(Properties runtimeProperties) {
		
		// loop over runtime properties and precede each with "hibernate" if
		// it isn't already
		for (Object key : runtimeProperties.keySet()) {
			String prop = (String) key;
			String value = (String) runtimeProperties.get(key);
			log.trace("Setting property: " + prop + ":" + value);
			if (!prop.startsWith("hibernate") && !runtimeProperties.containsKey("hibernate." + prop))
				runtimeProperties.setProperty("hibernate." + prop, value);
		}
		
		// load in the default hibernate properties from hibernate.default.properties
		InputStream propertyStream = null;
		try {
			Properties props = new Properties();
			propertyStream = ConfigHelper.getResourceAsStream("/hibernate.default.properties");
			OpenmrsUtil.loadProperties(props, propertyStream);
			
			// add in all default properties that don't exist in the runtime 
			// properties yet
			for (Map.Entry<Object, Object> entry : props.entrySet()) {
				if (!runtimeProperties.containsKey(entry.getKey()))
					runtimeProperties.put(entry.getKey(), entry.getValue());
			}
		}
		finally {
			try {
				propertyStream.close();
			}
			catch (Throwable t) {
				// pass 
			}
		}
		
	}
	
}
