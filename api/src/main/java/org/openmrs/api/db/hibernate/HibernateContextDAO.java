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
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.persistence.LockTimeoutException;
import javax.persistence.PessimisticLockException;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.hibernate.search.mapper.orm.work.SearchIndexingPlan;
import org.hibernate.stat.QueryStatistics;
import org.hibernate.stat.Statistics;
import org.hibernate.type.StandardBasicTypes;
import org.openmrs.GlobalProperty;
import org.openmrs.OpenmrsObject;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.api.context.Daemon;
import org.openmrs.api.db.ContextDAO;
import org.openmrs.api.db.UserDAO;
import org.openmrs.api.db.hibernate.search.session.SearchSessionFactory;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.util.PrivilegeConstants;
import org.openmrs.util.Security;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.SessionFactoryUtils;
import org.springframework.orm.hibernate5.SessionHolder;
import org.springframework.transaction.annotation.Isolation;
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
	
	private static final Long DEFAULT_INITIAL_LOCKOUT_DURATION_MS = TimeUnit.MILLISECONDS.convert(10L, TimeUnit.MINUTES);

	private static final Long DEFAULT_MAX_LOCKOUT_DURATION_MS = TimeUnit.MILLISECONDS.convert(24L, TimeUnit.HOURS);

	/**
	 * The capability that proves to {@link Daemon} this class is allowed to act with daemon
     * permissions.
	 */
	private static volatile Daemon.CallerKey daemonCallerKey;

	/**
	 * Hibernate session factory
	 */
	private SessionFactory sessionFactory;
	
	@Autowired
	private SearchSessionFactory searchSessionFactory;
	
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
	@Transactional(noRollbackFor = ContextAuthenticationException.class, isolation = Isolation.READ_COMMITTED)
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

			session.flush();
			Integer lockedUserId;
			try {
				lockedUserId = (Integer) session
				        .createNativeQuery("select user_id from users where user_id = ?1 for update")
				        .addScalar("user_id", StandardBasicTypes.INTEGER)
				        .setParameter(1, candidateUser.getUserId()).uniqueResult();
			} catch (LockTimeoutException | PessimisticLockException | org.hibernate.PessimisticLockException e) {
				log.warn("Could not lock the user row for login (login={}, userId={})", login, candidateUser.getUserId(), e);
				throw new ContextAuthenticationException(errorMsg);
			} catch (RuntimeException e) {
				log.error("Could not lock the user row for login (login={}, userId={})", login, candidateUser.getUserId(),
				    e);
				throw new ContextAuthenticationException(errorMsg);
			}
			if (lockedUserId == null) {
				log.info("Failed login attempt (login={}) - {}", login, errorMsg);
				throw new ContextAuthenticationException(errorMsg);
			}
			session.refresh(candidateUser);

			String lockoutTimeString = candidateUser.getUserProperty(OpenmrsConstants.USER_PROPERTY_LOCKOUT_TIMESTAMP, null);
			long lockoutTime = -1;
			if (StringUtils.isNotBlank(lockoutTimeString) && !"0".equals(lockoutTimeString)) {
				try {
					// putting this in a try/catch in case the admin decided to put junk into the property
					lockoutTime = Long.parseLong(lockoutTimeString);
				}
				catch (NumberFormatException e) {
					log.warn("bad value stored in {} user property: {}", OpenmrsConstants.USER_PROPERTY_LOCKOUT_TIMESTAMP,
						lockoutTimeString);
				}
			}

			// if they've been locked out, don't continue with the authentication
			if (lockoutTime > 0) {
				// the lockout duration depends on how many consecutive lockouts this account has had,
				// and the lockout timestamp is left untouched while the account is locked so that it
				// can expire automatically
				final long lockoutDuration = getLockoutDurationMs(getLockoutCount(candidateUser));
				if (System.currentTimeMillis() - lockoutTime > lockoutDuration) {
					// keep the lockout count so that the next lockout doubles the duration
					candidateUser.setUserProperty(OpenmrsConstants.USER_PROPERTY_LOGIN_ATTEMPTS, OpenmrsConstants.ZERO_LOGIN_ATTEMPTS_VALUE);
					candidateUser.removeUserProperty(OpenmrsConstants.USER_PROPERTY_LOCKOUT_TIMESTAMP);
					saveUserProperties(candidateUser);
				} else {
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
			if (passwordOnRecord != null && Security.checkPassword(passwordOnRecord, password + saltOnRecord)) {
				// hydrate the user object
				candidateUser.getAllRoles().size();
				candidateUser.getUserProperties().size();
				candidateUser.getPrivileges().size();

				// a successful login clears the lockout history, so the next lockout starts at the
				// initial duration again
				candidateUser.removeUserProperty(OpenmrsConstants.USER_PROPERTY_LOCKOUT_COUNT);

				// only clean up if the were some login failures, otherwise all should be clean
				int attempts = getUsersLoginAttempts(candidateUser);
				if (attempts > 0) {
					candidateUser.setUserProperty(OpenmrsConstants.USER_PROPERTY_LOGIN_ATTEMPTS, OpenmrsConstants.ZERO_LOGIN_ATTEMPTS_VALUE);
					candidateUser.removeUserProperty(OpenmrsConstants.USER_PROPERTY_LOCKOUT_TIMESTAMP);
				}
				setLastLoginTime(candidateUser);
				saveUserProperties(candidateUser);

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
					Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
					allowedFailedLoginCount = Integer.parseInt(Context.getAdministrationService().getGlobalProperty(
						OpenmrsConstants.GP_ALLOWED_FAILED_LOGINS_BEFORE_LOCKOUT).trim());
				}
				catch (Exception ex) {
					log.error("Unable to convert the global property {} to a valid integer. Using default value of 7.",
						OpenmrsConstants.GP_ALLOWED_FAILED_LOGINS_BEFORE_LOCKOUT);
				}
				finally {
					Context.removeProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
				}

				if (attempts > allowedFailedLoginCount) {
					// set the user as locked out at this exact time and record the lockout so that
					// each consecutive lockout lasts twice as long as the previous one
					candidateUser.setUserProperty(OpenmrsConstants.USER_PROPERTY_LOCKOUT_TIMESTAMP, String.valueOf(System
						.currentTimeMillis()));
					candidateUser.setUserProperty(OpenmrsConstants.USER_PROPERTY_LOCKOUT_COUNT, String
						.valueOf(getLockoutCount(candidateUser) + 1));
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
	
	private void setLastLoginTime(User candidateUser) {
		candidateUser.setUserProperty(
			OpenmrsConstants.USER_PROPERTY_LAST_LOGIN_TIMESTAMP,
			String.valueOf(System.currentTimeMillis())
		);
	}

	/**
	 * Calculates the duration of a lockout. The first lockout lasts {@code initialDurationMs}, and
	 * every consecutive lockout doubles that duration until {@code maximumDurationMs} is reached.
	 * The loop stops once the maximum is reached so that a corrupt or unusually large lockout count
	 * cannot overflow the calculation.
	 *
	 * @param lockoutCount the number of consecutive lockouts, where the first lockout is 1
	 * @param initialDurationMs the duration of the first lockout in milliseconds
	 * @param maximumDurationMs the maximum lockout duration in milliseconds
	 * @return the lockout duration in milliseconds
	 */
	static long calculateLockoutDurationMs(long lockoutCount, long initialDurationMs, long maximumDurationMs) {
		if (initialDurationMs <= 0) {
			return 0L;
		}

		long maximum = maximumDurationMs > 0 ? maximumDurationMs : initialDurationMs;
		long durationMs = Math.min(initialDurationMs, maximum);

		for (long lockout = 1; lockout < lockoutCount && durationMs < maximum; lockout++) {
			durationMs = durationMs > maximum / 2 ? maximum : durationMs * 2;
		}
		return Math.min(durationMs, maximum);
	}

	private long getLockoutDurationMs(int lockoutCount) {
		long initialDurationMs = DEFAULT_INITIAL_LOCKOUT_DURATION_MS;
		long maximumDurationMs = DEFAULT_MAX_LOCKOUT_DURATION_MS;

		try {
			Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
			initialDurationMs = getMinutesGlobalProperty(OpenmrsConstants.GP_UNLOCK_ACCOUNT_WAITING_TIME,
				initialDurationMs);
			maximumDurationMs = getMinutesGlobalProperty(OpenmrsConstants.GP_MAX_ACCOUNT_LOCKOUT_DURATION,
				maximumDurationMs);
		}
		catch (Exception e) {
			log.error("Unable to read the account lockout global properties. Using the default initial duration "
				+ "of 10 minutes and maximum duration of 24 hours.", e);
		}
		finally {
			Context.removeProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
		}

		return calculateLockoutDurationMs(lockoutCount, initialDurationMs, maximumDurationMs);
	}

	private long getMinutesGlobalProperty(String property, long defaultDurationMs) {
		String configuredValue = Context.getAdministrationService().getGlobalProperty(property);
		if (StringUtils.isNotBlank(configuredValue)) {
			try {
				long minutes = Long.parseLong(configuredValue.trim());
				if (minutes >= 0) {
					return TimeUnit.MINUTES.toMillis(minutes);
				}
			}
			catch (NumberFormatException e) {
				log.error("Unable to convert the global property {} to a valid number of minutes. Using the default.",
					property, e);
			}
		}
		return defaultDurationMs;
	}

	/**
	 * Get the number of consecutive lockouts stored for the given user.
	 *
	 * @param user the user to check
	 * @return the number of consecutive lockouts, defaulting to zero if none is defined
	 */
	private int getLockoutCount(User user) {
		String lockoutCountString = user.getUserProperty(OpenmrsConstants.USER_PROPERTY_LOCKOUT_COUNT, "0");
		int lockoutCount = 0;
		try {
			lockoutCount = Integer.parseInt(lockoutCountString);
		}
		catch (NumberFormatException e) {
			log.warn("bad value stored in {} user property: {}", OpenmrsConstants.USER_PROPERTY_LOCKOUT_COUNT,
				lockoutCountString);
		}
		return Math.max(lockoutCount, 0);
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
		
		User u = HibernateUtil.getUniqueEntityByUUID(sessionFactory, User.class, uuid);
		
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
		return Daemon.createUser(user, password, roleNames, daemonCallerKey());
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
	@Override
	public void openSession() {
		log.debug("HibernateContext: Opening Hibernate Session");
		Object value = TransactionSynchronizationManager.getResource(sessionFactory);
		if (value instanceof ContextSessionHolder) {
			log.debug("Participating in existing session ({})", sessionFactory.hashCode());
			((ContextSessionHolder) value).openCount++;
		} else if (value != null) {
			log.debug("Participating in session bound by another owner ({})", sessionFactory.hashCode());
		} else {
			log.debug("Registering session with synchronization manager ({})", sessionFactory.hashCode());
			Session session = sessionFactory.openSession();
			session.setHibernateFlushMode(FlushMode.MANUAL);
			TransactionSynchronizationManager.bindResource(sessionFactory, new ContextSessionHolder(session));
		}
	}
	
	/**
	 * @see org.openmrs.api.context.Context#closeSession()
	 */
	@Override
	public void closeSession() {
		log.debug("HibernateContext: closing Hibernate Session");
		Object value = TransactionSynchronizationManager.getResource(sessionFactory);
		if (value instanceof ContextSessionHolder) {
			ContextSessionHolder holder = (ContextSessionHolder) value;
			if (holder.openCount == 1 && holder.isInSpringTransaction()) {
				log.warn("Ignoring closeSession() without a matching openSession() inside a transaction",
				    new IllegalStateException("Unmatched closeSession() call"));
				return;
			}

			if (--holder.openCount > 0) {
				log.debug("Participating in existing session, so not releasing session through synchronization manager");
				return;
			}

			log.debug("Unbinding session from synchronization manager ({})", sessionFactory.hashCode());
			TransactionSynchronizationManager.unbindResource(sessionFactory);
			try {
				holder.getSession().close();
			} catch (RuntimeException e) {
				log.error("Unexpected exception on closing Hibernate Session", e);
			}
		} else if (value != null) {
			log.debug("Not closing a session this thread did not open ({})", sessionFactory.hashCode());
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
	 * @see org.openmrs.api.db.ContextDAO#evictEntity(OpenmrsObject)
	 */
	@Override
	public void evictEntity(OpenmrsObject obj) {
		sessionFactory.getCache().evictEntity(obj.getClass(), obj.getId());
	}

	/**
	 * @see org.openmrs.api.db.ContextDAO#evictAllEntities(Class)
	 */
	@Override
	public void evictAllEntities(Class<?> entityClass) {
		sessionFactory.getCache().evictEntityRegion(entityClass);
		sessionFactory.getCache().evictCollectionRegions();
		sessionFactory.getCache().evictQueryRegions();
	}

	/**
	 * @see org.openmrs.api.db.ContextDAO#clearEntireCache()
	 */
	@Override
	public void clearEntireCache() {
		sessionFactory.getCache().evictAllRegions();
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
		Session session = sessionFactory.getCurrentSession();
		SearchSession searchSession = searchSessionFactory.getSearchSession();
		SearchIndexingPlan indexingPlan = searchSession.indexingPlan();
		
		//Prepare session for batch work
		session.flush();
		indexingPlan.execute();
		session.clear();

		//Purge all search indexes of the given type
		Search.mapping(sessionFactory).scope(type).workspace().purge();
		
		FlushMode flushMode = session.getHibernateFlushMode();
		CacheMode cacheMode = session.getCacheMode();
		try {
			session.setHibernateFlushMode(FlushMode.MANUAL);
			session.setCacheMode(CacheMode.IGNORE);

			//Scrollable results will avoid loading too many objects in memory
			try (ScrollableResults results = HibernateUtil.getScrollableResult(sessionFactory, type, 1000)) {
				int index = 0;
				while (results.next()) {
					index++;
					//index each element
					indexingPlan.addOrUpdate(results.get(0));
					if (index % 1000 == 0) {
						//apply changes to search indexes
						indexingPlan.execute();
						//free memory since the queue is processed
						session.clear();
						// reset index to avoid overflows
						index = 0;
					}
				}
			} finally {
				indexingPlan.execute();
				session.clear();
			}
		}
		finally {
			session.setHibernateFlushMode(flushMode);
			session.setCacheMode(cacheMode);
		}
	}

	@Override
	@Transactional
	public void updateSearchIndex(Class<?>... types) {
		try {
			searchSessionFactory.getSearchSession().massIndexer(types).startAndWait();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @see org.openmrs.api.db.ContextDAO#updateSearchIndexForObject(java.lang.Object)
	 */
	@Override
	@Transactional
	public void updateSearchIndexForObject(Object object) {
		SearchIndexingPlan indexingPlan = searchSessionFactory.getSearchSession().indexingPlan();
		indexingPlan.addOrUpdate(object);
		indexingPlan.execute();
	}
	
	/**
	 * @see org.openmrs.api.db.ContextDAO#setupSearchIndex()
	 */
	@Override
	public void setupSearchIndex() {
		try {
			Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
			String gp = Context.getAdministrationService().getGlobalProperty(OpenmrsConstants.GP_SEARCH_INDEX_VERSION, "");

			if (!OpenmrsConstants.SEARCH_INDEX_VERSION.toString().equals(gp)) {
				updateSearchIndex();
			}
		}
		finally {
			Context.removeProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
		}
	}
	
	/**
	 * @see ContextDAO#updateSearchIndex()
	 */
	@Override
	public void updateSearchIndex() {
		try {
			log.warn("Updating the search index... It may take a few minutes.");
			searchSessionFactory.getSearchSession().massIndexer().startAndWait();
			Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
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
		finally {
			Context.removeProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
		}
	}

	/**
	 * @see ContextDAO#updateSearchIndexAsync()
	 */
	@Override
	public Future<?> updateSearchIndexAsync() {
		try {
			log.info("Started asynchronously updating the search index...");
			return searchSessionFactory.getSearchSession().massIndexer().start().toCompletableFuture();
		}
		catch (Exception e) {
			throw new RuntimeException("Failed to start asynchronous search index update", e);
		}
	}

	/**
	 * @see ContextDAO#getDatabaseConnection() 
	 */
	public Connection getDatabaseConnection() {
		try {
			return SessionFactoryUtils.getDataSource(sessionFactory).getConnection();
		}
		catch (SQLException e) {
			throw new RuntimeException("Unable to retrieve a database connection", e);
		}
	}

	/**
	 * Receives the {@link Daemon} caller key. Called only by {@link Daemon} during its initialization.
	 *
	 * @param callerKey the caller key issued by {@link Daemon}
	 * @since 3.0.0, 2.9.0, 2.8.9
	 */
	public static void setDaemonCallerKey(Daemon.CallerKey callerKey) {
		if (callerKey != null && daemonCallerKey == null) {
			daemonCallerKey = callerKey;
		}
	}

	private static Daemon.CallerKey daemonCallerKey() {
		if (daemonCallerKey == null) {
			// Guarantee Daemon has initialized and therefore handed us the key, regardless of the order in
			// which the two classes were first loaded.
			Daemon.ensureInitialized();
		}
		return daemonCallerKey;
	}

	/**
	 * Marks a session bound by {@link #openSession()}, the only kind {@link #closeSession()} unbinds
	 * and closes. Any other session bound to the thread belongs to whoever bound it, such as a Spring
	 * transaction or {@code OpenSessionInViewFilter}, which releases it. Closing a transaction's
	 * session here would leave the JDBC connection bound alongside it and break the next transaction on
	 * the thread.
	 * <p>
	 * The holder counts the {@link #openSession()} calls it has not yet seen closed. The count lives on
	 * the holder, rather than in a thread-local, so an unmatched open on some other session cannot keep
	 * this one from closing. It needs no synchronization, as a bound resource is only reachable from
	 * its own thread.
	 */
	private static final class ContextSessionHolder extends SessionHolder {

		private int openCount = 1;

		private ContextSessionHolder(Session session) {
			super(session);
		}

		/**
		 * Whether a Spring transaction manager is using this session. The flag is set when the transaction
		 * begins and cleared only once its cleanup has finished, after the completion callbacks.
		 */
		private boolean isInSpringTransaction() {
			return isTransactionActive();
		}
	}
}
