/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.test.SkipBaseSetup;
import org.openmrs.test.jupiter.BaseContextSensitiveNonTransactionalTest;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.context.TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS;

/**
 * Runs {@link ContextDAO#authenticate(String, String)} against a real database (Testcontainers,
 * never the in-memory H2) with genuinely concurrent sessions, to prove that failed login attempts
 * for one user are serialized on that user's row and that the lock does not spill over to other
 * users. The schema is created by Liquibase, so this also covers the fresh-install path of the
 * users.username / users.system_id index changesets.
 */
@SkipBaseSetup
@TestExecutionListeners(listeners = ContextDAOConcurrentLockoutIT.DiscardApplicationContextAroundClass.class, mergeMode = MERGE_WITH_DEFAULTS)
public class ContextDAOConcurrentLockoutIT extends BaseContextSensitiveNonTransactionalTest {

	/**
	 * The session factory reads its connection settings when the application context is built, so a
	 * context cached by an in-memory test class must be discarded before this class and the one built
	 * against the container discarded after it. {@code @DirtiesContext} cannot express both, hence this
	 * listener.
	 */
	public static class DiscardApplicationContextAroundClass extends AbstractTestExecutionListener {

		@Override
		public void beforeTestClass(TestContext testContext) {
			testContext.markApplicationContextDirty(DirtiesContext.HierarchyMode.EXHAUSTIVE);
		}

		@Override
		public void afterTestClass(TestContext testContext) {
			testContext.markApplicationContextDirty(DirtiesContext.HierarchyMode.EXHAUSTIVE);
		}
	}

	private static final String LOCKED_USERNAME = "correct";

	private static final String LOCKED_USER_SYSTEM_ID = "7-5";

	private static final String LOCKED_USER_SYSTEM_ID_WITHOUT_HYPHEN = "75";

	private static final String LOCKED_USER_PASSWORD = "test";

	private static final String WRONG_PASSWORD = "not the right password";

	private static final String LOCKOUT_MESSAGE = "Invalid number of connection attempts";

	private static final int ALLOWED_FAILED_LOGINS = 3;

	private static final int CONCURRENT_ATTEMPTS = 20;

	private static final int OTHER_USER_LOGIN_TIMEOUT_SECONDS = 10;

	private static final int LOCK_HOLD_TIMEOUT_SECONDS = 30;

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Override
	public Boolean useInMemoryDatabase() {
		return false;
	}

	/**
	 * The MySQL image pinned in {@link org.openmrs.test.Containers#newMySQLContainer()} is too old for
	 * JobRunr's schema migrations (they use common table expressions), so start MariaDB instead when no
	 * database was chosen.
	 */
	@Override
	protected void ensureDatabaseRunning() {
		if (System.getProperty("database") == null) {
			System.setProperty("database", "mariadb");
		}
		super.ensureDatabaseRunning();
	}

	@BeforeEach
	public void loadDataIntoRealDatabase() throws Exception {
		deleteAllData();
		executeDataSet(INITIAL_XML_DATASET_PACKAGE_PATH);
		executeDataSet("org/openmrs/api/db/include/contextDAOTest.xml");
		getConnection().commit();
		authenticate();
		Context.getAdministrationService().setGlobalProperty(OpenmrsConstants.GP_ALLOWED_FAILED_LOGINS_BEFORE_LOCKOUT,
		    String.valueOf(ALLOWED_FAILED_LOGINS));
		Context.clearSession();
	}

	/**
	 * @see ContextDAO#authenticate(String,String)
	 */
	@Test
	public void authenticate_shouldLockOutUserWhenConcurrentFailedAttemptsExceedTheLimit() throws Exception {
		List<String> logins = new ArrayList<>();
		for (int i = 0; i < CONCURRENT_ATTEMPTS; i++) {
			logins.add(List.of(LOCKED_USERNAME, LOCKED_USER_SYSTEM_ID, LOCKED_USER_SYSTEM_ID_WITHOUT_HYPHEN).get(i % 3));
		}
		List<String> outcomes = authenticateConcurrently(logins, WRONG_PASSWORD, 60);

		long lockedOut = outcomes.stream().filter(m -> m.contains(LOCKOUT_MESSAGE)).count();
		assertEquals(CONCURRENT_ATTEMPTS - (ALLOWED_FAILED_LOGINS + 1), lockedOut,
		    "only the attempts up to and including the one that locks the account may check the password; outcomes: "
		            + outcomes);

		User user = Context.getUserService().getUserByUsername(LOCKED_USERNAME);
		assertEquals(String.valueOf(ALLOWED_FAILED_LOGINS),
		    user.getUserProperty(OpenmrsConstants.USER_PROPERTY_LOGIN_ATTEMPTS));
		assertNotNull(user.getUserProperty(OpenmrsConstants.USER_PROPERTY_LOCKOUT_TIMESTAMP, null));

		String stillLocked = authenticateInSeparateSession(LOCKED_USERNAME, LOCKED_USER_PASSWORD);
		assertTrue(stillLocked.contains(LOCKOUT_MESSAGE),
		    "the correct password must not be checked while the account is locked; outcome: " + stillLocked);
	}

	/**
	 * @see ContextDAO#authenticate(String,String)
	 */
	@Test
	public void authenticate_shouldNotBlockOtherUsersWhileOneUsersRowIsLocked() throws Exception {
		Integer lockedUserId = Context.getUserService().getUserByUsername(LOCKED_USERNAME).getUserId();
		Context.clearSession();

		CountDownLatch rowLocked = new CountDownLatch(1);
		CountDownLatch release = new CountDownLatch(1);
		List<Throwable> lockHolderErrors = new ArrayList<>();
		Thread lockHolder = new Thread(() -> {
			Context.openSession();
			try {
				new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
					sessionFactory.getCurrentSession()
					        .createNativeQuery("select user_id from users where user_id = ?1 for update", Integer.class)
					        .setParameter(1, lockedUserId).getResultList();
					rowLocked.countDown();
					try {
						release.await(LOCK_HOLD_TIMEOUT_SECONDS, TimeUnit.SECONDS);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				});
			} catch (Throwable t) {
				lockHolderErrors.add(t);
				rowLocked.countDown();
			} finally {
				Context.closeSession();
			}
		});
		lockHolder.start();
		assertTrue(rowLocked.await(30, TimeUnit.SECONDS), "the lock holder never acquired the row lock");
		assertTrue(lockHolderErrors.isEmpty(), "the lock holder failed: " + lockHolderErrors);

		try {
			List<String> outcomes = authenticateConcurrently("admin", "test", 1, OTHER_USER_LOGIN_TIMEOUT_SECONDS);
			assertEquals("authenticated", outcomes.get(0),
			    "a login for a different user must complete while another user's row is locked");
		} finally {
			release.countDown();
			lockHolder.join(TimeUnit.SECONDS.toMillis(LOCK_HOLD_TIMEOUT_SECONDS));
		}
		assertFalse(lockHolder.isAlive(), "the lock holder did not finish");
		assertTrue(lockHolderErrors.isEmpty(), "the lock holder failed: " + lockHolderErrors);
	}

	/**
	 * @see ContextDAO#authenticate(String,String)
	 */
	@Test
	public void authenticate_shouldCountEveryAttemptWhenConcurrentAttemptsStayWithinTheLimit() throws Exception {
		List<String> outcomes = authenticateConcurrently(LOCKED_USERNAME, WRONG_PASSWORD, ALLOWED_FAILED_LOGINS);

		assertTrue(outcomes.stream().noneMatch(m -> m.contains(LOCKOUT_MESSAGE)), outcomes.toString());

		User user = Context.getUserService().getUserByUsername(LOCKED_USERNAME);
		assertEquals(String.valueOf(ALLOWED_FAILED_LOGINS),
		    user.getUserProperty(OpenmrsConstants.USER_PROPERTY_LOGIN_ATTEMPTS));
		assertNull(user.getUserProperty(OpenmrsConstants.USER_PROPERTY_LOCKOUT_TIMESTAMP, null));

		assertEquals("authenticated", authenticateInSeparateSession(LOCKED_USERNAME, LOCKED_USER_PASSWORD));
	}

	@Test
	public void liquibase_shouldCreateIndexesOnUsersUsernameAndSystemId() throws Exception {
		Set<String> indexedColumns = new HashSet<>();
		DatabaseMetaData metaData = getConnection().getMetaData();
		try (ResultSet indexes = metaData.getIndexInfo(getConnection().getCatalog(), null, "users", false, false)) {
			while (indexes.next()) {
				String indexName = indexes.getString("INDEX_NAME");
				if ("users_username".equalsIgnoreCase(indexName) || "users_system_id".equalsIgnoreCase(indexName)) {
					indexedColumns.add(indexes.getString("COLUMN_NAME").toLowerCase());
				}
			}
		}

		assertEquals(new HashSet<>(List.of("username", "system_id")), indexedColumns);
	}

	private List<String> authenticateConcurrently(String username, String password, int attempts) throws Exception {
		return authenticateConcurrently(username, password, attempts, 60);
	}

	private List<String> authenticateConcurrently(String username, String password, int attempts, int timeoutSeconds)
	        throws Exception {
		return authenticateConcurrently(Collections.nCopies(attempts, username), password, timeoutSeconds);
	}

	/**
	 * Starts one session per login, releases them against {@link Context#authenticate(String, String)}
	 * at the same instant and returns one outcome per attempt: "authenticated", or the message of the
	 * {@link ContextAuthenticationException}.
	 */
	private List<String> authenticateConcurrently(List<String> logins, String password, int timeoutSeconds)
	        throws Exception {
		CountDownLatch ready = new CountDownLatch(logins.size());
		CountDownLatch start = new CountDownLatch(1);
		ExecutorService pool = Executors.newFixedThreadPool(logins.size());
		try {
			List<Future<String>> futures = new ArrayList<>();
			for (String login : logins) {
				futures.add(pool.submit(() -> {
					ready.countDown();
					start.await();
					return authenticateInThisThread(login, password);
				}));
			}
			assertTrue(ready.await(30, TimeUnit.SECONDS), "not all attempts were ready to start");
			start.countDown();

			List<String> outcomes = new ArrayList<>();
			for (int i = 0; i < futures.size(); i++) {
				try {
					outcomes.add(futures.get(i).get(timeoutSeconds, TimeUnit.SECONDS));
				} catch (TimeoutException e) {
					fail("login for " + logins.get(i) + " did not complete within " + timeoutSeconds + "s; outcomes so far: "
					        + outcomes);
				}
			}
			return outcomes;
		} finally {
			pool.shutdownNow();
		}
	}

	private String authenticateInSeparateSession(String username, String password) throws Exception {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		try {
			return executor.submit(() -> authenticateInThisThread(username, password)).get(60, TimeUnit.SECONDS);
		} catch (ExecutionException e) {
			if (e.getCause() instanceof Exception) {
				throw (Exception) e.getCause();
			}
			throw e;
		} finally {
			executor.shutdownNow();
		}
	}

	private static String authenticateInThisThread(String login, String password) {
		try {
			Context.openSession();
			Context.authenticate(login, password);
			return "authenticated";
		} catch (ContextAuthenticationException e) {
			return e.getMessage();
		} finally {
			Context.closeSession();
		}
	}
}
