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

import java.util.List;
import java.util.stream.Stream;

import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.openmrs.api.context.Context;
import org.openmrs.test.SkipBaseSetup;
import org.openmrs.test.jupiter.BaseContextSensitiveNonTransactionalTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.context.TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS;

/**
 * Runs {@link AdministrationService#executeSQL(String, boolean)} against real database engines
 * (Testcontainers), verifying the read-only enforcement of the select-only call.
 */
@SkipBaseSetup
@TestExecutionListeners(listeners = AdministrationServiceExecuteSQLReadOnlyIT.DiscardApplicationContextAroundClass.class, mergeMode = MERGE_WITH_DEFAULTS)
public class AdministrationServiceExecuteSQLReadOnlyIT extends BaseContextSensitiveNonTransactionalTest {

	/** Builds the context against the configured container and discards it after the class. */
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

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Autowired
	private SessionFactory sessionFactory;

	@Override
	public Boolean useInMemoryDatabase() {
		return false;
	}

	/**
	 * The pinned MySQL image cannot boot the context, so no engine choice means the standard reference
	 * database.
	 */
	@Override
	protected void ensureDatabaseRunning() {
		if (System.getProperty("database") == null) {
			System.setProperty("database", "mariadb");
		}
		super.ensureDatabaseRunning();
	}

	@BeforeEach
	void loadDataForAuthentication() throws Exception {
		deleteAllData();
		executeDataSet(INITIAL_XML_DATASET_PACKAGE_PATH);
		getConnection().commit();
		authenticate();
		Context.clearSession();
	}

	/** Per engine query whose answer shows the read-only state of the executed statement. */
	private String readOnlyTransactionProbe() {
		String database = System.getProperty("database");
		if ("postgres".equals(database)) {
			return "SELECT current_setting('transaction_read_only') AS state";
		}
		// the read-only state under either connector name
		return "SELECT @@SESSION.tx_read_only";
	}

	private List<List<Object>> readOnlyTransactionRows() {
		try {
			return Context.getAdministrationService().executeSQL(readOnlyTransactionProbe(), true);
		} catch (Exception e) {
			return Context.getAdministrationService().executeSQL("SELECT @@SESSION.transaction_read_only", true);
		}
	}

	private String expectedProbeAnswer() {
		return "postgres".equals(System.getProperty("database")) ? "on" : "1";
	}

	@Test
	void executeSQL_withSelectOnly_shouldRunTheStatementInsideReadOnlyTransaction() {
		List<List<Object>> rows = readOnlyTransactionRows();
		assertEquals(expectedProbeAnswer(), String.valueOf(rows.get(0).get(0)).toLowerCase());
	}

	@Test
	void executeSQL_withSelectOnly_shouldReturnResultsForOrdinarySelects() {
		List<List<Object>> rows = Context.getAdministrationService()
		        .executeSQL("SELECT COUNT(*) AS property_count FROM global_property", true);
		assertEquals(1, rows.size(), "a plain select returns its one result row");
		assertNotNull(rows.get(0).get(0));

		rows = Context.getAdministrationService()
		        .executeSQL("WITH types AS (SELECT COUNT(*) AS type_count FROM visit_type) SELECT * FROM types", true);
		assertEquals(1, rows.size(), "a select through a read-only common table expression returns its result row");
		assertNotNull(rows.get(0).get(0));
	}

	/** Write forms that must be rejected with the data unchanged. */
	static class WritingStatements implements ArgumentsProvider {

		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
			return Stream.of(Arguments.of("/*x*/DELETE FROM obs"),
			    Arguments.of("REPLACE INTO global_property (property, property_value) VALUES ('a', 'b')"),
			    Arguments.of("TRUNCATE TABLE obs"), Arguments.of("SELECT 1; DELETE FROM obs"),
			    Arguments.of("SELECT * FROM obs INTO OUTFILE 'outside.csv'"),
			    Arguments.of("SELECT lo_import('/tmp/data.txt')"), Arguments.of("SELECT pg_read_file('PG_VERSION')"),
			    Arguments.of("SELECT pg_advisory_lock(998877)"), Arguments.of("SELECT $$handled here$$ AS piece FROM obs"),
			    Arguments.of("SELECT 'x\\'; DELETE FROM obs; -- tail'"),
			    Arguments.of("SELECT 'x\\' || pg_read_file('PG_VERSION') FROM obs -- tail'"),
			    Arguments.of("SELECT pg_catalog.\"pg_read_file\"('PG_VERSION')"),
			    Arguments.of("SELECT 1; -- comment\rDELETE FROM obs"));
		}
	}

	@ParameterizedTest(name = "{0} throws with the data unchanged")
	@ArgumentsSource(WritingStatements.class)
	void executeSQL_withSelectOnly_shouldRejectEachWriteStatementLeavingDataUnchanged(String sql) {
		AdministrationService adminService = Context.getAdministrationService();

		List<List<Object>> before = adminService.executeSQL(
		    "SELECT (SELECT COUNT(*) FROM obs) AS obs_count, (SELECT COUNT(*) FROM global_property) AS property_count",
		    true);
		int obsRows = ((Number) before.get(0).get(0)).intValue();
		int propertyRows = ((Number) before.get(0).get(1)).intValue();

		assertThrows(IllegalArgumentException.class, () -> adminService.executeSQL(sql, true));

		List<List<Object>> after = adminService.executeSQL(
		    "SELECT (SELECT COUNT(*) FROM obs) AS obs_count, (SELECT COUNT(*) FROM global_property) AS property_count",
		    true);
		assertEquals(obsRows, ((Number) after.get(0).get(0)).intValue(), "the observation rows must be unchanged");
		assertEquals(propertyRows, ((Number) after.get(0).get(1)).intValue(), "the property rows must be unchanged");
	}

	@Test
	void executeSQL_withSelectOnly_shouldRejectStatementsThatWrite() {
		assertThrows(IllegalArgumentException.class, () -> Context.getAdministrationService().executeSQL(
		    "INSERT INTO global_property (property, property_value, uuid) VALUES ('a', 'b', 'uuid-a')", true));
	}

	/**
	 * A data-modifying WITH runs as an ordinary statement, so the engine refuses it inside the read
	 * only transaction.
	 */
	@Test
	void executeSQL_withSelectOnly_shouldLeaveDataModifyingWithFormsToTheEngine() {
		AdministrationService adminService = Context.getAdministrationService();

		List<List<Object>> before = adminService.executeSQL("SELECT COUNT(*) FROM obs", true);
		assertThrows(RuntimeException.class, () -> adminService.executeSQL("WITH t AS (SELECT 1) DELETE FROM obs", true));

		List<List<Object>> after = adminService.executeSQL("SELECT COUNT(*) FROM obs", true);
		assertEquals(((Number) before.get(0).get(0)).intValue(), ((Number) after.get(0).get(0)).intValue());
	}

	@Test
	void readOnlyTransaction_shouldKeepTheEngineFromWriting() {
		TransactionTemplate template = new TransactionTemplate(transactionManager);
		template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		template.setReadOnly(true);

		assertThat(template.getPropagationBehavior(), is(TransactionDefinition.PROPAGATION_REQUIRES_NEW));

		RuntimeException thrown = assertThrows(RuntimeException.class, () -> template.executeWithoutResult(status -> {
			// the same session bound by the transaction manager receives the statement
			sessionFactory.getCurrentSession().doWork(conn -> conn.createStatement().executeUpdate(
			    "INSERT INTO global_property (property, property_value, uuid) VALUES ('test', '1', 'uuid-test')"));
		}));

		String message = deepestCause(thrown).getMessage();
		message = message == null ? "" : message.toLowerCase().replace('-', ' ');
		assertTrue(message.contains("read only"),
		    "the engine has to refuse writing inside the transaction but said: " + message);
	}

	private static Throwable deepestCause(Throwable thrown) {
		Throwable cause = thrown;
		while (cause.getCause() != null) {
			cause = cause.getCause();
		}
		return cause;
	}

	@Test
	void executeSQL_withSelectOnlyFalse_shouldStayWritable() {
		assertDoesNotThrow(() -> Context.getAdministrationService()
		        .executeSQL("INSERT INTO global_property (property, property_value, uuid) "
		                + "VALUES ('test.writable.globalproperty', '1', 'uuid-writable')",
		            false));
	}
}
