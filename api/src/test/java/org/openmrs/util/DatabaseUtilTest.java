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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DatabaseUtilTest {

	@Test
	public void executeSQL_withSelectOnly_shouldRejectLeadingCommentDelete() throws Exception {
		Class.forName("org.h2.Driver");
		Connection conn = DriverManager.getConnection("jdbc:h2:mem:ro1;DB_CLOSE_DELAY=-1", "sa", "");
		Statement st = conn.createStatement();
		st.execute("CREATE TABLE t (id INT)");
		st.execute("INSERT INTO t VALUES (1)");
		st.close();

		assertThrows(IllegalArgumentException.class, () -> DatabaseUtil.executeSQL(conn, "/*x*/DELETE FROM t", true));
		conn.close();
	}

	@Test
	public void executeSQL_withSelectOnly_shouldRejectMultiStatement() throws Exception {
		Class.forName("org.h2.Driver");
		Connection conn = DriverManager.getConnection("jdbc:h2:mem:ro2;DB_CLOSE_DELAY=-1", "sa", "");
		Statement st = conn.createStatement();
		st.execute("CREATE TABLE t (id INT)");
		st.execute("INSERT INTO t VALUES (1)");
		st.close();

		assertThrows(IllegalArgumentException.class, () -> DatabaseUtil.executeSQL(conn, "SELECT 1; DELETE FROM t", true));
		conn.close();
	}

	@Test
	public void executeSQL_withSelectOnly_shouldRejectCteDelete() throws Exception {
		Class.forName("org.h2.Driver");
		Connection conn = DriverManager.getConnection("jdbc:h2:mem:ro3;DB_CLOSE_DELAY=-1", "sa", "");
		Statement st = conn.createStatement();
		st.execute("CREATE TABLE t (id INT)");
		st.execute("INSERT INTO t VALUES (1)");
		st.close();

		assertThrows(IllegalArgumentException.class,
		    () -> DatabaseUtil.executeSQL(conn, "WITH c AS (SELECT 1) DELETE FROM t", true));
		conn.close();
	}

	@Test
	public void executeSQL_withSelectOnly_shouldRejectExecutableComment() throws Exception {
		Class.forName("org.h2.Driver");
		Connection conn = DriverManager.getConnection("jdbc:h2:mem:ro4;DB_CLOSE_DELAY=-1", "sa", "");
		Statement st = conn.createStatement();
		st.execute("CREATE TABLE t (id INT)");
		st.execute("INSERT INTO t VALUES (1)");
		st.close();

		assertThrows(IllegalArgumentException.class, () -> DatabaseUtil.executeSQL(conn, "/*! DELETE FROM t */", true));
		conn.close();
	}

	@Test
	public void executeSQL_withSelectOnly_shouldRejectReplace() throws Exception {
		Class.forName("org.h2.Driver");
		Connection conn = DriverManager.getConnection("jdbc:h2:mem:ro5;DB_CLOSE_DELAY=-1", "sa", "");
		Statement st = conn.createStatement();
		st.execute("CREATE TABLE t (id INT)");
		st.close();

		assertThrows(IllegalArgumentException.class, () -> DatabaseUtil.executeSQL(conn, "REPLACE INTO t VALUES (1)", true));
		conn.close();
	}

	@Test
	public void executeSQL_withSelectOnly_shouldRejectTruncate() throws Exception {
		Class.forName("org.h2.Driver");
		Connection conn = DriverManager.getConnection("jdbc:h2:mem:ro6;DB_CLOSE_DELAY=-1", "sa", "");
		Statement st = conn.createStatement();
		st.execute("CREATE TABLE t (id INT)");
		st.close();

		assertThrows(IllegalArgumentException.class, () -> DatabaseUtil.executeSQL(conn, "TRUNCATE TABLE t", true));
		conn.close();
	}

	@Test
	public void executeSQL_withSelectOnly_shouldAllowSelect() throws Exception {
		Class.forName("org.h2.Driver");
		Connection conn = DriverManager.getConnection("jdbc:h2:mem:ro7;DB_CLOSE_DELAY=-1", "sa", "");
		Statement st = conn.createStatement();
		st.execute("CREATE TABLE t (id INT)");
		st.execute("INSERT INTO t VALUES (1)");
		st.close();

		assertDoesNotThrow(() -> DatabaseUtil.executeSQL(conn, "SELECT * FROM t", true));
		conn.close();
	}

	@Test
	public void executeSQL_withSelectOnly_shouldAllowSelectWithReplaceFunction() throws Exception {
		Class.forName("org.h2.Driver");
		Connection conn = DriverManager.getConnection("jdbc:h2:mem:ro8;DB_CLOSE_DELAY=-1", "sa", "");
		Statement st = conn.createStatement();
		st.close();

		assertDoesNotThrow(() -> DatabaseUtil.executeSQL(conn, "SELECT replace('a','a','b')", true));
		conn.close();
	}

	@Test
	public void executeSQL_withSelectOnly_shouldAllowSelectWithTruncateFunction() throws Exception {
		Class.forName("org.h2.Driver");
		Connection conn = DriverManager.getConnection("jdbc:h2:mem:ro9;DB_CLOSE_DELAY=-1", "sa", "");
		Statement st = conn.createStatement();
		st.close();

		assertDoesNotThrow(() -> DatabaseUtil.executeSQL(conn, "SELECT truncate(1.5,0)", true));
		conn.close();
	}

	@Test
	public void executeSQL_withSelectOnly_shouldAllowWithSelect() throws Exception {
		Class.forName("org.h2.Driver");
		Connection conn = DriverManager.getConnection("jdbc:h2:mem:ro10;DB_CLOSE_DELAY=-1", "sa", "");
		Statement st = conn.createStatement();
		st.execute("CREATE TABLE t (id INT)");
		st.execute("INSERT INTO t VALUES (1)");
		st.close();

		assertDoesNotThrow(() -> DatabaseUtil.executeSQL(conn, "WITH cte AS (SELECT 1) SELECT * FROM cte", true));
		conn.close();
	}

}

	@Test
	public void executeSQL_withSelectOnly_shouldRejectInsertStatement() throws Exception {
		Class.forName("org.h2.Driver");
		Connection conn = DriverManager.getConnection("jdbc:h2:mem:ro11;DB_CLOSE_DELAY=-1", "sa", "");
		Statement st = conn.createStatement();
		st.execute("CREATE TABLE t (id INT)");
		st.close();

		assertThrows(IllegalArgumentException.class, () -> DatabaseUtil.executeSQL(conn, "INSERT INTO t VALUES (1)", true));
		conn.close();
	}

	@Test
	public void executeSQL_withSelectOnly_shouldRejectUpdateStatement() throws Exception {
		Class.forName("org.h2.Driver");
		Connection conn = DriverManager.getConnection("jdbc:h2:mem:ro12;DB_CLOSE_DELAY=-1", "sa", "");
		Statement st = conn.createStatement();
		st.execute("CREATE TABLE t (id INT)");
		st.close();

		assertThrows(IllegalArgumentException.class, () -> DatabaseUtil.executeSQL(conn, "UPDATE t SET id=1", true));
		conn.close();
	}

	@Test
	public void executeSQL_withSelectOnly_shouldRejectLoadData() throws Exception {
		Class.forName("org.h2.Driver");
		Connection conn = DriverManager.getConnection("jdbc:h2:mem:ro13;DB_CLOSE_DELAY=-1", "sa", "");
		Statement st = conn.createStatement();
		st.execute("CREATE TABLE t (id INT)");
		st.close();

		assertThrows(IllegalArgumentException.class,
		    () -> DatabaseUtil.executeSQL(conn, "LOAD DATA INFILE '/tmp/x' INTO TABLE t", true));
		conn.close();
	}

	@Test
	public void executeSQL_withSelectOnly_shouldRejectLoadFile() throws Exception {
		Class.forName("org.h2.Driver");
		Connection conn = DriverManager.getConnection("jdbc:h2:mem:ro14;DB_CLOSE_DELAY=-1", "sa", "");
		Statement st = conn.createStatement();
		st.close();

		assertThrows(IllegalArgumentException.class,
		    () -> DatabaseUtil.executeSQL(conn, "SELECT LOAD_FILE('/etc/passwd')", true));
		conn.close();
	}

	@Test
	public void executeSQL_withSelectOnly_shouldRejectDrop() throws Exception {
		Class.forName("org.h2.Driver");
		Connection conn = DriverManager.getConnection("jdbc:h2:mem:ro15;DB_CLOSE_DELAY=-1", "sa", "");
		Statement st = conn.createStatement();
		st.execute("CREATE TABLE t (id INT)");
		st.close();

		assertThrows(IllegalArgumentException.class, () -> DatabaseUtil.executeSQL(conn, "DROP TABLE t", true));
		conn.close();
	}

	@Test
	public void executeSQL_withSelectOnly_shouldRejectSet() throws Exception {
		Class.forName("org.h2.Driver");
		Connection conn = DriverManager.getConnection("jdbc:h2:mem:ro16;DB_CLOSE_DELAY=-1", "sa", "");
		Statement st = conn.createStatement();
		st.close();

		assertThrows(IllegalArgumentException.class, () -> DatabaseUtil.executeSQL(conn, "SET autocommit=0", true));
		conn.close();
	}

	@Test
	public void executeSQL_withSelectOnly_shouldAllowValues() throws Exception {
		Class.forName("org.h2.Driver");
		Connection conn = DriverManager.getConnection("jdbc:h2:mem:ro17;DB_CLOSE_DELAY=-1", "sa", "");
		Statement st = conn.createStatement();
		st.close();

		assertDoesNotThrow(() -> DatabaseUtil.executeSQL(conn, "VALUES (1,2,3)", true));
		conn.close();
	}

	@Test
	public void executeSQL_withSelectOnly_shouldRejectNull() throws Exception {
		Class.forName("org.h2.Driver");
		Connection conn = DriverManager.getConnection("jdbc:h2:mem:ro18;DB_CLOSE_DELAY=-1", "sa", "");
		Statement st = conn.createStatement();
		st.close();

		assertThrows(IllegalArgumentException.class, () -> DatabaseUtil.executeSQL(conn, null, true));
		conn.close();
	}

	@Test
	public void executeSQL_withSelectOnly_shouldRejectEmptyAfterComments() throws Exception {
		Class.forName("org.h2.Driver");
		Connection conn = DriverManager.getConnection("jdbc:h2:mem:ro19;DB_CLOSE_DELAY=-1", "sa", "");
		Statement st = conn.createStatement();
		st.close();

		assertThrows(IllegalArgumentException.class, () -> DatabaseUtil.executeSQL(conn, "/* just a comment */", true));
		conn.close();
	}
