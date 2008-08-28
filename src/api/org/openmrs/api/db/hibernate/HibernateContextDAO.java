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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
import org.openmrs.GlobalProperty;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.api.db.ContextDAO;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.Security;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Hibernate specific implementation of the {@link ContextDAO}.
 * 
 * These methods should not be used directly, instead, the methods
 * on the static {@link Context} file should be used.
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
	 * Session factory to use for this DAO.  This is usually
	 * injected by spring and its application context.
	 * 
	 * @param sessionFactory
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/**
	 * @see org.openmrs.api.db.ContextDAO#authenticate(java.lang.String, java.lang.String)
	 */
	public User authenticate(String login, String password)
			throws ContextAuthenticationException {
		
		String errorMsg = "Invalid username and/or password: " + login;
		
		Session session = sessionFactory.getCurrentSession();
		
		User candidateUser = null;
		
		if (login != null) {
			
			// loginWithoutDash is used to compare to the system id
			String loginWithDash = login;
			if (login.matches("\\d{2,}"))
				loginWithDash = login.substring(0, login.length() - 1) + "-" 
						+ login.charAt(login.length() - 1);
	
			try {
				candidateUser = (User) session
						.createQuery(
							"from User u where (u.username = ? or u.systemId = ? or u.systemId = ?) and u.voided = 0")
						.setString(0, login)
						.setString(1, login)
						.setString(2, loginWithDash)
						.uniqueResult();
			} catch (HibernateException he) {
				log.error("Got hibernate exception while logging in: '" + login
						+ "'", he);
			} catch (Exception e) {
				log.error(
						"Got regular exception while logging in: '" + login + "'",
						e);
			}
		}
		
		// only continue if this is a valid username and a nonempty password
		if (candidateUser != null && password != null) {
			if (log.isDebugEnabled())
				log.debug("Candidate user id: " + candidateUser.getUserId());
			
			String lockoutTimeString = candidateUser.getUserProperty(OpenmrsConstants.USER_PROPERTY_LOCKOUT_TIMESTAMP, null);
			Long lockoutTime = null;
			if (lockoutTimeString != null)
				lockoutTime = Long.valueOf(lockoutTimeString);
			
			// if they've been locked out, don't continue with the authentication
			if (lockoutTime != null) {
				// unlock them after 5 mins, otherwise reset the timestamp 
				// to now and make them wait another 5 mins 
				if (new Date().getTime() - lockoutTime > 300000)
					candidateUser.setUserProperty(OpenmrsConstants.USER_PROPERTY_LOGIN_ATTEMPTS, "0");
				else {
					candidateUser.setUserProperty(OpenmrsConstants.USER_PROPERTY_LOCKOUT_TIMESTAMP, String.valueOf(new Date().getTime()));
					throw new ContextAuthenticationException("Invalid number of connection attempts. Please try again later.");
				}
			}
			
			String passwordOnRecord = (String) session.createSQLQuery(
					"select password from users where user_id = ?")
						.addScalar("password", Hibernate.STRING)
						.setInteger(0, candidateUser.getUserId())
						.uniqueResult();
			
			String saltOnRecord = (String) session.createSQLQuery(
					"select salt from users where user_id = ?")
					.addScalar("salt", Hibernate.STRING)
					.setInteger(0, candidateUser.getUserId())
					.uniqueResult();
	
			String hashedPassword = Security.encodeString(password + saltOnRecord);
			
			// if the username and password match, hydrate the user and return it
			if (hashedPassword != null && hashedPassword.equals(passwordOnRecord)) {
				// hydrate the user object
				candidateUser.getAllRoles().size();
				candidateUser.getUserProperties().size();
				candidateUser.getPrivileges().size();
				
				// only clean up if the were some login failures, otherwise all should be clean
				Integer attempts = getUsersLoginAttempts(candidateUser);
				if (attempts > 0) {
					candidateUser.setUserProperty(OpenmrsConstants.USER_PROPERTY_LOGIN_ATTEMPTS, "0");
					candidateUser.setUserProperty(OpenmrsConstants.USER_PROPERTY_LOCKOUT_TIMESTAMP, "0");
					saveUserProperties(candidateUser);
				}
				
				// skip out of the method early (instead of throwing the exception)
				// to indicate that this is the valid user
				return candidateUser;
			}
			else {
				// the user failed the username/password, increment their
				// attempts here and set the "lockout" timestamp if necessary
				Integer attempts = getUsersLoginAttempts(candidateUser);
				
				attempts++;
				
				if (attempts >= 5) {
					// set the user as locked out at this exact time
					candidateUser.setUserProperty(OpenmrsConstants.USER_PROPERTY_LOCKOUT_TIMESTAMP, String.valueOf(new Date().getTime()));
				}
				else {
					candidateUser.setUserProperty(OpenmrsConstants.USER_PROPERTY_LOGIN_ATTEMPTS, String.valueOf(attempts));
				}
				
				saveUserProperties(candidateUser);
			}
		}
		
		// throw this exception only once in the same place with the same
		// message regardless of username/pw combo entered
		log.info("Failed login attempt (login=" + login + ") - "
					+ errorMsg);
		throw new ContextAuthenticationException(errorMsg);
		
	}
	
	/**
	 * Call the UserService to save the given user while proxying the
	 * privileges needed to do so.
	 * 
	 * @param user the User to save
	 */
	private void saveUserProperties(User user) {
		sessionFactory.getCurrentSession().update(user);
	}
	
	/**
	 * Get the integer stored for the given user that is their
	 * number of login attempts
	 * 
	 * @param user the user to check
	 * @return the # of login attempts for this user defaulting to zero if none defined
	 */
	private Integer getUsersLoginAttempts(User user) {
		String attemptsString = user.getUserProperty(OpenmrsConstants.USER_PROPERTY_LOGIN_ATTEMPTS, "0");
		Integer attempts = 0;
		try {
			attempts = Integer.valueOf(attemptsString);
		} catch (NumberFormatException e) {
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
				log.debug("Participating in existing session ("
						+ sessionFactory.hashCode() + ")");
			participate = true;
		} else {
			if (log.isDebugEnabled())
				log.debug("Registering session with synchronization manager ("
						+ sessionFactory.hashCode() + ")");
			Session session = SessionFactoryUtils.getSession(sessionFactory,
					true);
			session.setFlushMode(FlushMode.MANUAL);
			TransactionSynchronizationManager.bindResource(sessionFactory,
					new SessionHolder(session));
		}
	}

	/**
	 * @see org.openmrs.api.context.Context#closeSession()
	 */
	public void closeSession() {
		log.debug("HibernateContext: closing Hibernate Session");
		if (!participate) {
			log.debug("Unbinding session from synchronization mangaer ("
						+ sessionFactory.hashCode() + ")");

			if (TransactionSynchronizationManager.hasResource(sessionFactory)) {
				Object value = TransactionSynchronizationManager.unbindResource(sessionFactory);
				try {
					if (value instanceof SessionHolder) {
						Session session = ((SessionHolder)value).getSession();
						SessionFactoryUtils.releaseSession(session, sessionFactory);
					}
				} catch (RuntimeException e) {
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
			
			// session is closed by spring on session end

			log.debug("Closing any open sessions");
			// closeSession();
			log.debug("Shutting down threadLocalSession factory");

			// sessionFactory.close();
			log.debug("The threadLocalSession has been closed");

			log.debug("Setting static variables to null");
			// sessionFactory = null;
		} else
			log.error("SessionFactory is null");

	}

	/**
	 * Compares core data against the current database and inserts data into the
	 * database where necessary
	 */
	public void checkCoreDataset() {
		PreparedStatement psSelect;
		PreparedStatement psInsert;
		PreparedStatement psUpdate;
		Map<String, String> map;

		// setting core roles
		try {
			Connection conn = sessionFactory.getCurrentSession().connection();

			// Ticket #900 - Made explicit reference to columns
			psSelect = conn.prepareStatement("SELECT role, description FROM role WHERE UPPER(role) = UPPER(?)");
			psInsert = conn.prepareStatement("INSERT INTO role (role, description) VALUES (?, ?)");

			map = OpenmrsConstants.CORE_ROLES();
			for (String role : map.keySet()) {
				psSelect.setString(1, role);
				ResultSet result = psSelect.executeQuery();
				if (!result.next()) {
					psInsert.setString(1, role);
					psInsert.setString(2, map.get(role));
					psInsert.execute();
				}
			}

			conn.commit();
		} catch (Exception e) {
			log.error("Error while setting core roles for openmrs system", e);
		}

		// setting core privileges
		try {
			Connection conn = sessionFactory.getCurrentSession().connection();

			// Ticket #900 - Made explicit reference to columns
			psSelect = conn
					.prepareStatement("SELECT privilege, description FROM privilege WHERE UPPER(privilege) = UPPER(?)");
			psInsert = conn
					.prepareStatement("INSERT INTO privilege (privilege, description) VALUES (?, ?)");

			map = OpenmrsConstants.CORE_PRIVILEGES();
			for (String priv : map.keySet()) {
				psSelect.setString(1, priv);
				ResultSet result = psSelect.executeQuery();
				if (!result.next()) {
					psInsert.setString(1, priv);
					psInsert.setString(2, map.get(priv));
					psInsert.execute();
				}
			}

			conn.commit();
		} catch (SQLException e) {
			log.error("Error while setting core privileges", e);
		}

		// setting core global properties
		try {
			Connection conn = sessionFactory.getCurrentSession().connection();

			// Ticket #900 - Made explicit reference to columns
			psSelect = conn
					.prepareStatement("SELECT property, property_value, description FROM global_property WHERE UPPER(property) = UPPER(?)");
			psInsert = conn
					.prepareStatement("INSERT INTO global_property (property, property_value, description) VALUES (?, ?, ?)");
			// this update should only be temporary until everyone has the new global property description code 
			psUpdate = conn
					.prepareStatement("UPDATE global_property SET description = ? WHERE UPPER(property) = UPPER(?) AND description IS null");

			for (GlobalProperty gp : OpenmrsConstants.CORE_GLOBAL_PROPERTIES()) {
				psSelect.setString(1, gp.getProperty());
				ResultSet result = psSelect.executeQuery();
				if (!result.next()) {
					psInsert.setString(1, gp.getProperty());
					psInsert.setString(2, gp.getPropertyValue());
					psInsert.setString(3, gp.getDescription());
					psInsert.execute();
				}
				else {
					// should only be temporary 
					psUpdate.setString(1, gp.getDescription());
					psUpdate.setString(2, gp.getProperty());
					psUpdate.execute();
				}
			}

			conn.commit();
		} catch (SQLException e) {
			log.error("Error while setting core global properties", e);
		}

	}

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


	public void closeDatabaseConnection() {
		sessionFactory.close();
	}
	
}