/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.test.SkipBaseSetup;
import org.openmrs.test.jupiter.BaseContextSensitiveNonTransactionalTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.AbstractTestExecutionListener;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.context.TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS;

/**
 * Runs the atomic operations of {@link UserSecurityStateService} against a real database
 * (Testcontainers, never the in-memory H2) from genuinely concurrent sessions. The schema is
 * created by Liquibase, so this also covers the fresh-install path of the user_security_state
 * changeset.
 */
@SkipBaseSetup
@TestExecutionListeners(listeners = UserSecurityStateServiceConcurrencyIT.DiscardApplicationContextAroundClass.class, mergeMode = MERGE_WITH_DEFAULTS)
public class UserSecurityStateServiceConcurrencyIT extends BaseContextSensitiveNonTransactionalTest {

	/**
	 * The session factory reads its connection settings when the application context is built, so a
	 * context cached by an in-memory test class must be discarded before this class and the one built
	 * against the container discarded after it.
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

	private static final String USERNAME = "correct";

	private static final String PROPERTY = "test.token";

	private static final String TOKEN = "single use token";

	private static final int CONCURRENT_CALLS = 20;

	private static final int TIMEOUT_SECONDS = 60;

	private Integer userId;

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
	void loadDataIntoRealDatabase() throws Exception {
		deleteAllData();
		executeDataSet(INITIAL_XML_DATASET_PACKAGE_PATH);
		executeDataSet("org/openmrs/api/db/include/contextDAOTest.xml");
		getConnection().commit();
		authenticate();
		userId = Context.getUserService().getUserByUsername(USERNAME).getUserId();
		Context.clearSession();
	}

	/**
	 * @see UserSecurityStateService#deleteIfValueMatches(User, String, String)
	 */
	@Test
	void deleteIfValueMatches_shouldSucceedForExactlyOneOfConcurrentCalls() throws Exception {
		Context.getUserSecurityStateService().setValue(user(), PROPERTY, TOKEN);

		List<Boolean> outcomes = runConcurrently(
		    () -> Context.getUserSecurityStateService().deleteIfValueMatches(user(), PROPERTY, TOKEN));

		assertEquals(1, outcomes.stream().filter(Boolean::booleanValue).count(), outcomes.toString());
	}

	/**
	 * @see UserSecurityStateService#compareAndSet(User, String, String, String)
	 */
	@Test
	void compareAndSet_shouldSucceedForExactlyOneOfConcurrentCallsCreatingTheEntry() throws Exception {
		List<Boolean> outcomes = runConcurrently(
		    () -> Context.getUserSecurityStateService().compareAndSet(user(), PROPERTY, null, TOKEN));

		assertEquals(1, outcomes.stream().filter(Boolean::booleanValue).count(), outcomes.toString());
	}

	/**
	 * @see UserSecurityStateService#increment(User, String)
	 */
	@Test
	void increment_shouldNotLoseConcurrentIncrements() throws Exception {
		List<Long> outcomes = runConcurrently(() -> Context.getUserSecurityStateService().increment(user(), PROPERTY));

		assertEquals(String.valueOf(CONCURRENT_CALLS), Context.getUserSecurityStateService().getValue(user(), PROPERTY),
		    outcomes.toString());
	}

	/**
	 * @see UserSecurityStateService#increment(User, String)
	 */
	@Test
	void increment_shouldReturnEveryValueOnceUnderConcurrentIncrements() throws Exception {
		List<Long> outcomes = runConcurrently(() -> Context.getUserSecurityStateService().increment(user(), PROPERTY));

		assertEquals(CONCURRENT_CALLS, outcomes.stream().distinct().count(), outcomes.toString());
	}

	@Test
	void liquibase_shouldCreateTheUserSecurityStateTable() throws Exception {
		List<String> columns = new ArrayList<>();
		DatabaseMetaData metaData = getConnection().getMetaData();
		try (ResultSet resultSet = metaData.getColumns(getConnection().getCatalog(), null, "user_security_state", "%")) {
			while (resultSet.next()) {
				columns.add(resultSet.getString("COLUMN_NAME").toLowerCase());
			}
		}

		assertEquals(List.of("user_security_state_id", "uuid", "user_id", "property", "property_value", "creator",
		    "date_created", "changed_by", "date_changed"), columns);
	}

	private User user() {
		return Context.getUserService().getUser(userId);
	}

	/**
	 * Runs {@code call} {@value #CONCURRENT_CALLS} times, each in its own session authenticated as the
	 * admin user, released at the same instant.
	 */
	private <T> List<T> runConcurrently(Callable<T> call) throws Exception {
		CountDownLatch ready = new CountDownLatch(CONCURRENT_CALLS);
		CountDownLatch start = new CountDownLatch(1);
		ExecutorService pool = Executors.newFixedThreadPool(CONCURRENT_CALLS);
		try {
			List<Future<T>> futures = new ArrayList<>();
			for (int i = 0; i < CONCURRENT_CALLS; i++) {
				futures.add(pool.submit(() -> {
					Context.openSession();
					try {
						Context.authenticate("admin", "test");
						ready.countDown();
						start.await();
						return call.call();
					} finally {
						Context.closeSession();
					}
				}));
			}
			assertTrue(ready.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), "not all calls were ready to start");
			start.countDown();

			List<T> outcomes = new ArrayList<>();
			for (Future<T> future : futures) {
				outcomes.add(future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS));
			}
			return outcomes;
		} finally {
			pool.shutdownNow();
		}
	}
}
