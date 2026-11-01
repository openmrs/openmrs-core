/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.openmrs.api.db.DAOException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for the read-only enforcement of
 * {@link DatabaseUtil#executeSQL(java.sql.Connection, String, boolean)}. The validator is checked
 * directly, with no database involved. The statement executions that remain use an in-memory
 * database and record behavior that callers already rely on, so it stays unchanged.
 */
public class DatabaseUtilTest {

	/** Statements that a select-only call allows. */
	static class AllowedReadOnlyQueries implements ArgumentsProvider {

		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
			return Stream.of(Arguments.of("SELECT 1"), Arguments.of("SELECT * FROM person WHERE person_id = 1"),
			    // plain INTO forms, engine read only state covers any writing variant
			    Arguments.of("SELECT * FROM t INTO @variable"), Arguments.of("SELECT * FROM other INTO t2"),
			    Arguments.of("WITH c AS (SELECT 1) SELECT * FROM c INTO destination"),
			    Arguments.of("        SELECT DISTINCT a INTO t2 FROM t"), Arguments.of("select nightingale from song"),
			    Arguments.of("  select * from t  "), Arguments.of("((SELECT * FROM t))"),
			    Arguments.of("(SELECT current_patient FROM patient)"), Arguments.of("SELECT 'a;b' AS mixed_separator"),
			    Arguments.of("SELECT \"quoted;value\" AS mixed_separator"), Arguments.of("SELECT 'it''s ok'"),
			    Arguments.of("SELECT \"it\"\"s ok\""), Arguments.of("SELECT 'load_file(' FROM t"),
			    Arguments.of("SELECT deletions, updated_by FROM t"), Arguments.of("SELECT 'lo_import(' FROM t"),
			    Arguments.of("SELECT 'pg_read_file(' FROM t"), Arguments.of("SELECT mydelete, withreturning FROM t"),
			    Arguments.of("SELECT `update` FROM t"), Arguments.of("SELECT * FROM t;"), Arguments.of("SELECT * FROM t;  "),
			    Arguments.of("SELECT 1 -- only\nAND related FROM t"),
			    Arguments.of("WITH cte AS (SELECT replace ('a','a','b')) SELECT * FROM cte"),
			    Arguments.of("WITH cte AS (SELECT replace /*x*/ ('a','a','b')) SELECT * FROM cte"),
			    Arguments.of("SELECT \"pg_read_file\" FROM t"), Arguments.of("SELECT 'U&\"example\"' FROM t"),
			    Arguments.of("EXPLAIN WITH c AS (SELECT 1) SELECT * FROM c"), Arguments.of("SELECT* FROM t"),
			    Arguments.of("SELECT* FROM t ORDER BY actor_id DESC;"),
			    Arguments.of("WITH cte AS (SELECT 1) SELECT * FROM cte"),
			    Arguments.of("WITH total AS (SELECT replace('a','a','b')) SELECT * FROM total"),
			    Arguments.of("SELECT replace('a','a','b')"), Arguments.of("SELECT truncate(1.5, 0)"),
			    Arguments.of("SELECT UPDATE_TIMESTAMP FROM t"),
			    Arguments.of("WITH c AS (SELECT 1) SELECT /* delete */ * FROM c"), Arguments.of("VALUES (1, 2, 3)"),
			    Arguments.of("SHOW TABLES"), Arguments.of("TABLE t"), Arguments.of("EXPLAIN SELECT * FROM t"),
			    Arguments.of("EXPLAIN ANALYZE SELECT * FROM t"),
			    Arguments.of("EXPLAIN(ANALYZE, FORMAT JSON) SELECT * FROM t"),
			    Arguments.of("EXPLAIN VERBOSE SELECT * FROM t"), Arguments.of("DESC t"), Arguments.of("DESCRIBE t"),
			    Arguments.of("DESCRIBE   t"));
		}
	}

	/** Statements that a select-only call rejects. */
	static class RejectedQueries implements ArgumentsProvider {

		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
			return Stream.of(Arguments.of("DELETE FROM t"), Arguments.of("delete from t where id = ((select 1))"),
			    Arguments.of("((DELETE FROM t))"), Arguments.of("/* x */ DELETE FROM t"),
			    Arguments.of("-- comments\nDELETE FROM t"), Arguments.of("INSERT INTO t VALUES (1)"),
			    Arguments.of("UPDATE t SET id = 1"), Arguments.of("CREATE TABLE x (id INT)"),
			    Arguments.of("ALTER TABLE t ADD col INT"), Arguments.of("DROP TABLE t"),
			    Arguments.of("RENAME TABLE t TO t2"), Arguments.of("TRUNCATE TABLE t"), Arguments.of("TRUNCATE t"),
			    Arguments.of("REPLACE INTO t VALUES (1)"),
			    Arguments.of("MERGE INTO t USING s ON (1=1) WHEN MATCHED THEN DELETE"),
			    Arguments.of("CALL some_procedure()"), Arguments.of("GRANT ALL PRIVILEGES ON *.* TO 'someone'"),
			    Arguments.of("SET autocommit = 0"), Arguments.of("SET @running = 1"),
			    Arguments.of("SET GLOBAL max_connections = 1000"), Arguments.of("LOCK TABLES t READ"),
			    Arguments.of("DELIMITER ;"), Arguments.of("VACUUM ANALYZE t"), Arguments.of("DROP FUNCTION IF EXISTS f"),
			    // server side functions
			    Arguments.of("SELECT lo_import('/tmp/data.txt')"), Arguments.of("SELECT lo_export(3, '/tmp/data.bin')"),
			    Arguments.of("SELECT lo_unlink(12345)"), Arguments.of("SELECT pg_read_file('PG_VERSION')"),
			    Arguments.of("SELECT pg_read_binary_file('postmaster.pid')"), Arguments.of("SELECT pg_ls_dir('/')"),
			    Arguments.of("SELECT pg_stat_file('PG_VERSION')"), Arguments.of("SELECT pg_reload_conf()"),
			    Arguments.of("SELECT pg_rotate_logfile()"), Arguments.of("SELECT pg_terminate_backend(12345)"),
			    Arguments.of("SELECT pg_cancel_backend(12345)"), Arguments.of("SELECT pg_advisory_lock(998877)"),
			    Arguments.of("SELECT pg_advisory_unlock_all()"), Arguments.of("SELECT pg_stat_reset()"),
			    Arguments.of("SELECT pg_stat_clear_snapshot()"), Arguments.of("SELECT pg_create_restore_point('x')"),
			    Arguments.of("SELECT pg_switch_wal()"), Arguments.of("SELECT pg_start_backup('base')"),
			    Arguments.of("SELECT file_read('/etc/hosts')"), Arguments.of("SELECT csvwrite('t', '/tmp/data.csv')"),
			    // dollar quote forms
			    Arguments.of("SELECT $$handled by dollar$$ FROM t"),
			    Arguments.of("SELECT $tag$; DELETE FROM t;$tag$ AS piece FROM t"), Arguments.of("SELECT 1; DELETE FROM t"),
			    Arguments.of("SELECT 1;  DELETE FROM t"), Arguments.of("SELECT (1; SELECT 2)"),
			    Arguments.of("SELECT 1;/* both */ SELECT 2"), Arguments.of("(();;"), Arguments.of("/* only a comment */"),
			    Arguments.of("-- only a comment"), Arguments.of("/* unmatched SELECT 1"), Arguments.of("SELECT 'unmatched"),
			    Arguments.of("SELECT `unmatched"), Arguments.of("DESCrypto"), Arguments.of("selection made; no keyword"),
			    Arguments.of("value; starts with another word"), Arguments.of("EXPLAIN DELETE FROM t"),
			    Arguments.of("EXPLAIN UPDATE t SET id = 2"), Arguments.of("EXPLAIN INSERT INTO t VALUES (2)"),
			    Arguments.of("EXPLAIN TRUNCATE t"), Arguments.of("EXPLAIN ANALYZE DELETE FROM t"),
			    Arguments.of("EXPLAIN (ANALYZE) DELETE FROM t"),
			    Arguments.of("EXPLAIN (ANALYZE, VERBOSE) UPDATE t SET id = 3"),
			    Arguments.of("EXPLAIN VERBOSE ANALYZE UPDATE t SET id = 3"),
			    Arguments.of("EXPLAIN MERGE INTO t USING s ON (1=1) WHEN MATCHED THEN UPDATE SET id = 3"),
			    Arguments.of("EXPLAIN (SELECT 1)"), Arguments.of("EXPLAIN "), Arguments.of("EXPLAIN"),
			    Arguments.of("WITH c AS (SELECT 1) DELETE FROM t"), Arguments.of("WITH c AS (SELECT 1) UPDATE t SET id = 2"),
			    Arguments.of("WITH c AS (SELECT 1) INSERT INTO t VALUES (2)"),
			    Arguments.of("WITH c AS (SELECT 1) TRUNCATE TABLE t"),
			    Arguments.of("WITH c AS (SELECT 1) REPLACE INTO t VALUES (2)"),
			    Arguments.of("EXPLAIN WITH c AS (SELECT 1) DELETE FROM t"),
			    Arguments.of("EXPLAIN ANALYZE WITH c AS (SELECT 1) DELETE FROM t"),
			    Arguments.of("WITH c AS (SELECT 1) MERGE INTO t USING s ON (1 = 1) WHEN MATCHED THEN DELETE"),
			    Arguments.of("WITH c AS (SELECT 1) DELETE FROM t WHERE id IN (SELECT 2)"),
			    Arguments.of("WITH deleteFirst AS (SELECT 1) DELETE FROM t"),
			    Arguments.of("WITH c AS (SELECT 'remote' DELETE FROM t) SELECT * FROM c"),
			    Arguments.of("SELECT LOAD_FILE('/etc/interfaces') FROM t"),
			    Arguments.of("SELECT a, LOAD_FILE('/etc/interfaces') FROM t"),
			    Arguments.of("SELECT 'load_file inside a sentence', LOAD_FILE('/etc/interfaces') FROM t"),
			    Arguments.of("LOAD DATA INFILE 'x' INTO TABLE t"), Arguments.of("SELECT * FROM t INTO OUTFILE '/tmp/data'"),
			    Arguments.of("SELECT * FROM t INTO DUMPFILE '/tmp/data'"),
			    Arguments.of("SELECT mark INTO OUTFILE '/tmp/data' FROM t"), Arguments.of("# comments\nDELETE FROM t"),
			    Arguments.of("/* junk */    DELETE FROM t"),
			    // quoted function names
			    Arguments.of("SELECT pg_catalog.\\\"pg_read_file\\\"('PG_VERSION')"),
			    Arguments.of("SELECT U&\"pg_read_\\0066ile\"('PG_VERSION')"), Arguments.of("SELECT u&\"pg_ls_dir\"('/')"),
			    Arguments.of("SELECT \\\"lo_import\\\"('x')"), Arguments.of("SELECT `load_file`('/etc/passwd')"),
			    // carriage-return line comment
			    Arguments.of("SELECT 1; -- comment\rDELETE FROM t"),
			    // nested block comment form
			    Arguments.of("SELECT 'x' /* outer /* inner */ ' */ || pg_read_file('PG_VERSION') -- '"),
			    Arguments.of("SELECT*; DELETE FROM t"));
		}
	}

	/** Statement forms that cannot be resolved across engines are rejected. */
	static class AmbiguousEscapeQueries implements ArgumentsProvider {

		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
			return Stream.of(Arguments.of("SELECT 'x\\'; DELETE FROM t; -- tail' AND filler = 1"),
			    Arguments.of("SELECT 'x\\' || pg_read_file('PG_VERSION') FROM t -- tail'"));
		}
	}

	@ParameterizedTest(name = "{0} cannot be resolved across engines")
	@ArgumentsSource(AmbiguousEscapeQueries.class)
	void validateReadOnlyQuery_shouldRejectWhenABackslashMaySwallowAQuote(String sql) {
		assertThrows(IllegalArgumentException.class, () -> DatabaseUtil.validateReadOnlyQuery(sql));
	}

	/** Dash comment forms that differ across engines are rejected. */
	static class AmbiguousCommentQueries implements ArgumentsProvider {

		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
			return Stream.of(Arguments.of("SELECT 'x' --'\n || pg_read_file('PG_VERSION') --'"),
			    Arguments.of("SELECT 'x\'; --'\n DELETE FROM t --'"), Arguments.of("SELECT 'x' --x"));
		}
	}

	@ParameterizedTest(name = "{0} cannot be resolved across engines")
	@ArgumentsSource(AmbiguousCommentQueries.class)
	void validateReadOnlyQuery_shouldRejectUnresolvedDashCommentForms(String sql) {
		assertThrows(IllegalArgumentException.class, () -> DatabaseUtil.validateReadOnlyQuery(sql));
	}

	@Test
	void validateReadOnlyQuery_shouldAllowTheDashCommentWhitespaceForm() {
		assertDoesNotThrow(() -> DatabaseUtil.validateReadOnlyQuery("SELECT 1 -- only\nAND related FROM t"));
	}

	@Test
	void validateReadOnlyQuery_shouldKeepIdentifiersThatContinueThroughTheDollarSign() {
		assertDoesNotThrow(() -> DatabaseUtil.validateReadOnlyQuery("SELECT gross$total$ FROM t"));
		assertDoesNotThrow(() -> DatabaseUtil.validateReadOnlyQuery("SELECT value_key FROM t WHERE money$unit$ = 1"));
		assertThrows(IllegalArgumentException.class,
		    () -> DatabaseUtil.validateReadOnlyQuery("SELECT $$handled$$ AS piece FROM t"));
	}

	@ParameterizedTest(name = "{0} is read-only")
	@ArgumentsSource(AllowedReadOnlyQueries.class)
	void validateReadOnlyQuery_shouldAllowReadOnlyStatements(String sql) {
		assertDoesNotThrow(() -> DatabaseUtil.validateReadOnlyQuery(sql));
	}

	@ParameterizedTest(name = "{0} is not read-only")
	@ArgumentsSource(RejectedQueries.class)
	void validateReadOnlyQuery_shouldRejectEveryOtherStatement(String sql) {
		assertThrows(IllegalArgumentException.class, () -> DatabaseUtil.validateReadOnlyQuery(sql));
	}

	@ParameterizedTest
	@ValueSource(strings = { "# MySQL style comment\nSELECT 1", "SELECT 1 # MySQL style comment",
	        "SELECT bit '01' # bit '11'" })
	void validateReadOnlyQuery_shouldRejectHashForms(String sql) {
		assertThrows(IllegalArgumentException.class, () -> DatabaseUtil.validateReadOnlyQuery(sql));
	}

	@ParameterizedTest
	@ValueSource(strings = { "/*! SELECT 1 */", "/*M!50000 SELECT 1 */", "SELECT 1/*!; DELETE FROM t */",
	        "SELECT 1 /*M!100005 DELETE FROM t */", "SELECT 1; /*! DELETE FROM t */" })
	void validateReadOnlyQuery_shouldRejectExecutableCommentForms(String sql) {
		assertThrows(IllegalArgumentException.class, () -> DatabaseUtil.validateReadOnlyQuery(sql));
	}

	@Test
	void validateReadOnlyQuery_shouldRejectNull() {
		assertThrows(IllegalArgumentException.class, () -> DatabaseUtil.validateReadOnlyQuery(null));
	}

	@Test
	void validateReadOnlyQuery_shouldTreatCommentsAndStringLiteralsAsContent() {
		assertDoesNotThrow(() -> DatabaseUtil.validateReadOnlyQuery("SELECT 1 /* please do not delete me */"));
		assertDoesNotThrow(() -> DatabaseUtil.validateReadOnlyQuery("SELECT 'i\"; DELETE FROM anything' AS _"));
	}

	@Test
	void validateReadOnlyQuery_shouldQuoteIdentifiersWithoutLosingStructure() {
		assertDoesNotThrow(() -> DatabaseUtil.validateReadOnlyQuery("SELECT `update` FROM t"));
		assertThrows(IllegalArgumentException.class, () -> DatabaseUtil.validateReadOnlyQuery("SELECT `update FROM t"));
	}

	/**
	 * @see DatabaseUtil#executeSQL(java.sql.Connection, String, boolean)
	 */
	@Test
	void executeSQL_withSelectOnlyFalse_shouldKeepExecutingDataRemoval() throws Exception {
		try (Connection conn = DriverManager.getConnection("jdbc:h2:mem:audit;DB_CLOSE_DELAY=-1", "sa", "");
		        Statement st = conn.createStatement()) {
			st.execute("CREATE TABLE t (id INT)");
			st.execute("INSERT INTO t VALUES (1)");
		}

		try (Connection conn = DriverManager.getConnection("jdbc:h2:mem:audit;DB_CLOSE_DELAY=-1", "sa", "")) {
			List<List<Object>> updateCounts = DatabaseUtil.executeSQL(conn, "DELETE FROM t", false);
			assertEquals(1, ((Number) updateCounts.get(0).get(0)).intValue());
		}
	}

	/**
	 * @see DatabaseUtil#executeSQL(java.sql.Connection, String, boolean)
	 */
	@Test
	void executeSQL_withSelectOnlyFalse_shouldKeepExecutingDataAddition() throws Exception {
		try (Connection conn = DriverManager.getConnection("jdbc:h2:mem:audit2;DB_CLOSE_DELAY=-1", "sa", "");
		        Statement st = conn.createStatement()) {
			st.execute("CREATE TABLE t (id INT)");
		}

		try (Connection conn = DriverManager.getConnection("jdbc:h2:mem:audit2;DB_CLOSE_DELAY=-1", "sa", "")) {
			DatabaseUtil.executeSQL(conn, "INSERT INTO t VALUES (1)", false);
		}

		try (Connection conn = DriverManager.getConnection("jdbc:h2:mem:audit2;DB_CLOSE_DELAY=-1", "sa", "")) {
			List<List<Object>> rows = DatabaseUtil.executeSQL(conn, "SELECT COUNT(*) FROM t", true);
			assertEquals(1, ((Number) rows.get(0).get(0)).intValue());
		}
	}

	/**
	 * The validator runs when selectOnly is on only. Its absence on the other path is the behavior that
	 * decides the run against a leading comment being hidden from the legacy prefix check; that
	 * statement reaches the database and the database answers, outside of the validator.
	 */
	@Test
	void executeSQL_withSelectOnlyFalse_shouldKeepSkippingValidation() throws Exception {
		try (Connection conn = DriverManager.getConnection("jdbc:h2:mem:audit3;DB_CLOSE_DELAY=-1", "sa", "");
		        Statement st = conn.createStatement()) {
			st.execute("CREATE TABLE t (id INT)");
			st.execute("INSERT INTO t VALUES (7)");
		}

		try (Connection conn = DriverManager.getConnection("jdbc:h2:mem:audit3;DB_CLOSE_DELAY=-1", "sa", "")) {
			List<List<Object>> updateCounts = DatabaseUtil.executeSQL(conn, "UPDATE t SET id = 2 WHERE id = 7", false);
			assertEquals(1, ((Number) updateCounts.get(0).get(0)).intValue());
		}

		try (Connection conn = DriverManager.getConnection("jdbc:h2:mem:audit3;DB_CLOSE_DELAY=-1", "sa", "")) {
			assertThrows(DAOException.class,
			    () -> DatabaseUtil.executeSQL(conn, "/* comment */UPDATE t SET id = 3 WHERE id = 2", false));
		}
	}

	/**
	 * @see DatabaseUtil#executeSQL(java.sql.Connection, String, boolean)
	 */
	@Test
	void executeSQL_withSelectOnly_shouldRejectWritesBeforeExecuting() throws Exception {
		try (Connection conn = DriverManager.getConnection("jdbc:h2:mem:audit4;DB_CLOSE_DELAY=-1", "sa", "");
		        Statement st = conn.createStatement()) {
			st.execute("CREATE TABLE t (id INT)");
			st.execute("INSERT INTO t VALUES (1)");
		}

		try (Connection conn = DriverManager.getConnection("jdbc:h2:mem:audit4;DB_CLOSE_DELAY=-1", "sa", "")) {
			assertThrows(IllegalArgumentException.class, () -> DatabaseUtil.executeSQL(conn, "TRUNCATE TABLE t", true));
		}

		try (Connection conn = DriverManager.getConnection("jdbc:h2:mem:audit4;DB_CLOSE_DELAY=-1", "sa", "")) {
			List<List<Object>> rows = DatabaseUtil.executeSQL(conn, "SELECT COUNT(*) FROM t", true);
			assertEquals(1, ((Number) rows.get(0).get(0)).intValue(), "nothing was executed");
		}
	}

	/**
	 * @see DatabaseUtil#executeSQL(java.sql.Connection, String, boolean)
	 */
	@Test
	void executeSQL_withSelectOnlyFalse_shouldLetTheDatabaseAnswerInvalidStatements() throws Exception {
		try (Connection conn = DriverManager.getConnection("jdbc:h2:mem:audit5;DB_CLOSE_DELAY=-1", "sa", "")) {
			assertThrows(DAOException.class, () -> DatabaseUtil.executeSQL(conn, "SELECT * FROM missing_table", false),
			    "the statement runs so an engine error reaches the caller as before");
		}
	}
}
