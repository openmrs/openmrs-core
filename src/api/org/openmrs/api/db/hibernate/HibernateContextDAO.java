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
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.api.db.ContextDAO;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.Security;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class HibernateContextDAO implements ContextDAO {

	private static Log log = LogFactory.getLog(HibernateContextDAO.class);

	/**
	 * Hibernate session factory
	 */
	private SessionFactory sessionFactory;

	/**
	 * Default public constructor
	 */
	public HibernateContextDAO() {
	}

	/**
	 * Set session factory
	 * 
	 * @param sessionFactory
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/**
	 * Authenticate the user for this context.
	 * 
	 * @param username
	 * @param password
	 * 
	 * @see org.openmrs.api.context.Context#authenticate(String, String)
	 * @throws ContextAuthenticationException
	 */
	public User authenticate(String login, String password)
			throws ContextAuthenticationException {
		
		String errorMsg = "Invalid username and/or password: " + login;
		
		Session session = sessionFactory.getCurrentSession();
		
		User candidateUser = null;
		
		if (login != null) {
			
			// loginWithoutDash is used to compare to the system id
			String loginWithoutDash = login;
			if (login.length() >= 3 && login.charAt(login.length() - 2) == '-')
				loginWithoutDash = login.substring(0, login.length() - 2)
						+ login.charAt(login.length() - 1);
	
			try {
				candidateUser = (User) session
						.createQuery(
							"from User u where (u.username = ? or u.systemId = ? or u.systemId = ?) and u.voided = 0")
						.setString(0, login)
						.setString(1, login)
						.setString(2, loginWithoutDash)
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

		if (candidateUser == null)
			throw new ContextAuthenticationException("User not found: " + login);
		
		if (log.isDebugEnabled())
			log.debug("Candidate user id: " + candidateUser.getUserId());
		
		if (password == null)
			throw new ContextAuthenticationException("Password cannot be null");
		
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
		
		User user = null;
		
		if (hashedPassword != null && hashedPassword.equals(passwordOnRecord))
			user = candidateUser;

		if (user == null) {
			log.info("Failed login attempt (login=" + login + ") - "
						+ errorMsg);
			throw new ContextAuthenticationException(errorMsg);
		}

		// hydrate the user object
		user.getAllRoles().size();
		user.getUserProperties().size();
		user.getPrivileges().size();
		//

		return user;
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
	 * Perform cleanup on current session
	 */
	public void clearSession() {
		sessionFactory.getCurrentSession().clear();
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

			psSelect = conn
					.prepareStatement("SELECT * FROM role WHERE UPPER(role) = UPPER(?)");
			psInsert = conn.prepareStatement("INSERT INTO role VALUES (?, ?)");

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

			psSelect = conn
					.prepareStatement("SELECT * FROM privilege WHERE UPPER(privilege) = UPPER(?)");
			psInsert = conn
					.prepareStatement("INSERT INTO privilege VALUES (?, ?)");

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

			psSelect = conn
					.prepareStatement("SELECT * FROM global_property WHERE UPPER(property) = UPPER(?)");
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