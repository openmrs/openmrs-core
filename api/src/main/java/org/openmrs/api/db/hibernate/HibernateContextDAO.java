/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db.hibernate;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Future;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.search.FullTextSession;
import org.hibernate.stat.QueryStatistics;
import org.hibernate.stat.Statistics;
import org.hibernate.type.StandardBasicTypes;
import org.openmrs.GlobalProperty;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.api.context.Daemon;
import org.openmrs.api.db.ContextDAO;
import org.openmrs.api.db.FullTextSessionFactory;
import org.openmrs.api.db.UserDAO;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.util.Security;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.SessionFactoryUtils;
import org.springframework.orm.hibernate5.SessionHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Hibernate specific implementation of the {@link ContextDAO}. These methods should not be used
 * directly, instead, the methods on the static {@link Context} file should be used.
 * 
 * @see ContextDAO
 * @see Context
 */
public class HibernateContextDAO implements ContextDAO {
	
	private static final Logger log = LoggerFactory.getLogger(HibernateContextDAO.class);
	
	/**
	 * Hibernate session factory
	 */
	private SessionFactory sessionFactory;
	
	@Autowired
	private FullTextSessionFactory fullTextSessionFactory;
	
	private UserDAO userDao;
	
	/**
	 * Session factory to use for this DAO. This is usually injected by spring and its application
	 * context.
	 * 
	 * @param sessionFactory
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	public void setUserDAO(UserDAO userDao) {
		this.userDao = userDao;
	}

	/**
	 * @see org.openmrs.api.db.ContextDAO#authenticate(java.lang.String, java.lang.String)
	 */
	@Override
	@Transactional(noRollbackFor = ContextAuthenticationException.class)
	public User authenticate(String login, String password) throws ContextAuthenticationException {
		String errorMsg = "Invalid username and/or password: " + login;

		Session session = sessionFactory.getCurrentSession();

		User candidateUser = null;

		if (StringUtils.isNotBlank(login)) {
			// loginWithoutDash is used to compare to the system id
			String loginWithDash = login;
			if (login.matches("\\d{2,}")) {
				loginWithDash = login.substring(0, login.length() - 1) + "-" + login.charAt(login.length() - 1);
			}

			try {
				candidateUser = session.createQuery(
					"from User u where (u.username = ?1 or u.systemId = ?2 or u.systemId = ?3) and u.retired = false",
					User.class)
					.setParameter(1, login).setParameter(2, login).setParameter(3, loginWithDash).uniqueResult();
			}
			catch (HibernateException he) {
				log.error("Got hibernate exception while logging in: '{}'", login, he);
			}
			catch (Exception e) {
				log.error("Got regular exception while logging in: '{}'", login, e);
			}
		}

		// only continue if this is a valid username and a nonempty password
		if (candidateUser != null && password != null) {
			log.debug("Candidate user id: {}", candidateUser.getUserId());

			String lockoutTimeString = candidateUser.getUserProperty(OpenmrsConstants.USER_PROPERTY_LOCKOUT_TIMESTAMP, null);
			Long lockoutTime = null;
			if (lockoutTimeString != null && !"0".equals(lockoutTimeString)) {
				try {
					// putting this in a try/catch in case the admin decided to put junk into the property
					lockoutTime = Long.valueOf(lockoutTimeString);
				}
				catch (NumberFormatException e) {
					log.warn("bad value stored in {} user property: {}", OpenmrsConstants.USER_PROPERTY_LOCKOUT_TIMESTAMP,
						lockoutTimeString);
				}
			}

			// if they've been locked out, don't continue with the authentication
			if (lockoutTime != null) {
				// unlock them after 5 mins, otherwise reset the timestamp
				// to now and make them wait another 5 mins
				if (System.currentTimeMillis() - lockoutTime > 300000) {
					candidateUser.setUserProperty(OpenmrsConstants.USER_PROPERTY_LOGIN_ATTEMPTS, "0");
					candidateUser.removeUserProperty(OpenmrsConstants.USER_PROPERTY_LOCKOUT_TIMESTAMP);
					saveUserProperties(candidateUser);
				} else {
					candidateUser.setUserProperty(OpenmrsConstants.USER_PROPERTY_LOCKOUT_TIMESTAMP, String.valueOf(System
						.currentTimeMillis()));
					throw new ContextAuthenticationException(
						"Invalid number of connection attempts. Please try again later.");
				}
			}

			Object[] passwordAndSalt = (Object[]) session
				.createNativeQuery("select password, salt from users where user_id = ?1")
				.addScalar("password", StandardBasicTypes.STRING).addScalar("salt", StandardBasicTypes.STRING)
				.setParameter(1, candidateUser.getUserId()).uniqueResult();

			String passwordOnRecord = (String) passwordAndSalt[0];
			String saltOnRecord = (String) passwordAndSalt[1];

			// if the username and password match, hydrate the user and return it
			if (passwordOnRecord != null && Security.hashMatches(passwordOnRecord, password + saltOnRecord)) {
				// hydrate the user object
				candidateUser.getAllRoles().size();
				candidateUser.getUserProperties().size();
				candidateUser.getPrivileges().size();

				// only clean up if the were some login failures, otherwise all should be clean
				int attempts = getUsersLoginAttempts(candidateUser);
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
				int attempts = getUsersLoginAttempts(candidateUser);

				attempts++;

				int allowedFailedLoginCount = 7;
				try {
					allowedFailedLoginCount = Integer.parseInt(Context.getAdministrationService().getGlobalProperty(
						OpenmrsConstants.GP_ALLOWED_FAILED_LOGINS_BEFORE_LOCKOUT).trim());
				}
				catch (Exception ex) {
					log.error("Unable to convert the global property {} to a valid integer. Using default value of 7.",
						OpenmrsConstants.GP_ALLOWED_FAILED_LOGINS_BEFORE_LOCKOUT);
				}

				if (attempts > allowedFailedLoginCount) {
					// set the user as locked out at this exact time
					candidateUser.setUserProperty(OpenmrsConstants.USER_PROPERTY_LOCKOUT_TIMESTAMP, String.valueOf(System
						.currentTimeMillis()));
				} else {
					candidateUser.setUserProperty(OpenmrsConstants.USER_PROPERTY_LOGIN_ATTEMPTS, String.valueOf(attempts));
				}

				saveUserProperties(candidateUser);
			}
		}

		// throw this exception only once in the same place with the same
		// message regardless of username/pw combo entered
		log.info("Failed login attempt (login={}) - {}", login, errorMsg);
		throw new ContextAuthenticationException(errorMsg);
	}
	
	/**
	 * @see org.openmrs.api.db.ContextDAO#getUserByUuid(java.lang.String)
	 */
	@Override
	@Transactional(readOnly = true)
	public User getUserByUuid(String uuid) {
		
		// don't flush here in case we're in the AuditableInterceptor.  Will cause a StackOverflowEx otherwise
		FlushMode flushMode = sessionFactory.getCurrentSession().getHibernateFlushMode();
		sessionFactory.getCurrentSession().setHibernateFlushMode(FlushMode.MANUAL);
		
		User u = (User) sessionFactory.getCurrentSession().createQuery("from User u where u.uuid = :uuid").setString("uuid",
		    uuid).uniqueResult();
		
		// reset the flush mode to whatever it was before
		sessionFactory.getCurrentSession().setHibernateFlushMode(flushMode);
		
		return u;
	}
	
	/**
	 * @see org.openmrs.api.db.ContextDAO#getUserByUsername(String)
	 */
	@Override
	@Transactional(readOnly = true)
	public User getUserByUsername(String username) {
		return userDao.getUserByUsername(username);
	}
	
	/**
	 * @throws Exception 
	 * @see org.openmrs.api.db.ContextDAO#createUser(User, String)
	 */
	@Override
	@Transactional
	public User createUser(User user, String password, List<String> roleNames) throws Exception {
		return Daemon.createUser(user, password, roleNames);
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
	private int getUsersLoginAttempts(User user) {
		String attemptsString = user.getUserProperty(OpenmrsConstants.USER_PROPERTY_LOGIN_ATTEMPTS, "0");
		int attempts = 0;
		try {
			attempts = Integer.parseInt(attemptsString);
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
	
	@Override
	public void openSession() {
		log.debug("HibernateContext: Opening Hibernate Session");
		if (TransactionSynchronizationManager.hasResource(sessionFactory)) {
			log.debug("Participating in existing session ({})", sessionFactory.hashCode());
			participate = true;
		} else {
			log.debug("Registering session with synchronization manager ({})", sessionFactory.hashCode());
			Session session = sessionFactory.openSession();
			session.setHibernateFlushMode(FlushMode.MANUAL);
			TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(session));
		}
	}
	
	/**
	 * @see org.openmrs.api.context.Context#closeSession()
	 */
	@Override
	public void closeSession() {
		log.debug("HibernateContext: closing Hibernate Session");
		if (!participate) {
			log.debug("Unbinding session from synchronization manager (" + sessionFactory.hashCode() + ")");
			
			if (TransactionSynchronizationManager.hasResource(sessionFactory)) {
				Object value = TransactionSynchronizationManager.unbindResource(sessionFactory);
				try {
					if (value instanceof SessionHolder) {
						Session session = ((SessionHolder) value).getSession();
						SessionFactoryUtils.closeSession(session);
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
	@Override
	@Transactional
	public void clearSession() {
		sessionFactory.getCurrentSession().clear();
	}
	
	/**
	 * @see org.openmrs.api.db.ContextDAO#evictFromSession(java.lang.Object)
	 */
	@Override
	public void evictFromSession(Object obj) {
		sessionFactory.getCurrentSession().evict(obj);
	}
	
	/**
	 * @see org.openmrs.api.db.ContextDAO#refreshEntity(Object)
	 */
	@Override
	public void refreshEntity(Object obj) {
		sessionFactory.getCurrentSession().refresh(obj);
	}

	/**
	 * @see org.openmrs.api.db.ContextDAO#flushSession()
	 */
	@Override
	@Transactional
	public void flushSession() {
		sessionFactory.getCurrentSession().flush();
	}
	
	/**
	 * @see org.openmrs.api.context.Context#startup(Properties)
	 */
	@Override
	@Transactional
	public void startup(Properties properties) {
	}
	
	/**
	 * @see org.openmrs.api.context.Context#shutdown()
	 */
	@Override
	public void shutdown() {
		if (log.isInfoEnabled()) {
			showUsageStatistics();
		}
		
		if (sessionFactory != null) {
			
			log.debug("Closing any open sessions");
			closeSession();
			
			log.debug("Shutting down threadLocalSession factory");
			if (!sessionFactory.isClosed()) {
				sessionFactory.close();
			}
			
			log.debug("The threadLocalSession has been closed");
			
		} else {
			log.error("SessionFactory is null");
		}
		
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
	 * <strong>Should</strong> merge default runtime properties
	 */
	@Override
	public void mergeDefaultRuntimeProperties(Properties runtimeProperties) {
		
		Map<String, String> cache = new HashMap<>();
		// loop over runtime properties and precede each with "hibernate" if
		// it isn't already
		for (Map.Entry<Object, Object> entry : runtimeProperties.entrySet()) {
			Object key = entry.getKey();
			String prop = (String) key;
			String value = (String) entry.getValue();
			log.trace("Setting property: " + prop + ":" + value);
			if (!prop.startsWith("hibernate") && !runtimeProperties.containsKey("hibernate." + prop)) {
				cache.put("hibernate." + prop, value);
			}
		}
		runtimeProperties.putAll(cache);
		
		// load in the default hibernate properties from hibernate.default.properties
		Properties props = new Properties();
		URL url = getClass().getResource("/hibernate.default.properties");
		File file = new File(url.getPath());
		OpenmrsUtil.loadProperties(props, file);
		
		// add in all default properties that don't exist in the runtime
		// properties yet
		for (Map.Entry<Object, Object> entry : props.entrySet()) {
			if (!runtimeProperties.containsKey(entry.getKey())) {
				runtimeProperties.put(entry.getKey(), entry.getValue());
			}
		}
	}
	
	@Override
	@Transactional
	public void updateSearchIndexForType(Class<?> type) {
		//From http://docs.jboss.org/hibernate/search/3.3/reference/en-US/html/manual-index-changes.html#search-batchindex-flushtoindexes
		FullTextSession session = fullTextSessionFactory.getFullTextSession();
		session.purgeAll(type);
		
		//Prepare session for batch work
		session.flush();
		session.clear();
		
		FlushMode flushMode = session.getHibernateFlushMode();
		CacheMode cacheMode = session.getCacheMode();
		try {
			session.setHibernateFlushMode(FlushMode.MANUAL);
			session.setCacheMode(CacheMode.IGNORE);
			
			//Scrollable results will avoid loading too many objects in memory
			ScrollableResults results = session.createCriteria(type).setFetchSize(1000).scroll(ScrollMode.FORWARD_ONLY);
			int index = 0;
			while (results.next()) {
				index++;
				//index each element
				session.index(results.get(0));
				if (index % 1000 == 0) {
					//apply changes to indexes
					session.flushToIndexes();
					//free memory since the queue is processed
					session.clear();
				}
			}
			session.flushToIndexes();
			session.clear();
		}
		finally {
			session.setHibernateFlushMode(flushMode);
			session.setCacheMode(cacheMode);
		}
	}
	
	/**
	 * @see org.openmrs.api.db.ContextDAO#updateSearchIndexForObject(java.lang.Object)
	 */
	@Override
	@Transactional
	public void updateSearchIndexForObject(Object object) {
		FullTextSession session = fullTextSessionFactory.getFullTextSession();
		session.index(object);
		session.flushToIndexes();
	}
	
	/**
	 * @see org.openmrs.api.db.ContextDAO#setupSearchIndex()
	 */
	@Override
	public void setupSearchIndex() {
		String gp = Context.getAdministrationService().getGlobalProperty(OpenmrsConstants.GP_SEARCH_INDEX_VERSION, "");
		
		if (!OpenmrsConstants.SEARCH_INDEX_VERSION.toString().equals(gp)) {
			updateSearchIndex();
		}
	}
	
	/**
	 * @see ContextDAO#updateSearchIndex()
	 */
	@Override
	public void updateSearchIndex() {
		try {
			log.info("Updating the search index... It may take a few minutes.");
			fullTextSessionFactory.getFullTextSession().createIndexer().startAndWait();
			GlobalProperty gp = Context.getAdministrationService().getGlobalPropertyObject(
			    OpenmrsConstants.GP_SEARCH_INDEX_VERSION);
			if (gp == null) {
				gp = new GlobalProperty(OpenmrsConstants.GP_SEARCH_INDEX_VERSION);
			}
			gp.setPropertyValue(OpenmrsConstants.SEARCH_INDEX_VERSION.toString());
			Context.getAdministrationService().saveGlobalProperty(gp);
			log.info("Finished updating the search index");
		}
		catch (Exception e) {
			throw new RuntimeException("Failed to update the search index", e);
		}
	}

	/**
	 * @see ContextDAO#updateSearchIndexAsync()
	 */
	@Override
	public Future<?> updateSearchIndexAsync() {
		try {
			log.info("Started asynchronously updating the search index...");
			return fullTextSessionFactory.getFullTextSession().createIndexer().start();
		}
		catch (Exception e) {
			throw new RuntimeException("Failed to start asynchronous search index update", e);
		}
	}
}
