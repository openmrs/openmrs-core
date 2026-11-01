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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Session;
import org.openmrs.api.db.DAOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * Utility class that provides database related methods
 *
 * @since 1.6
 */
public class DatabaseUtil {

	private static final String ILLEGAL_COMMAND = "Illegal command(s) found in query string";

	private static final java.util.regex.Pattern EXECUTABLE_COMMENT_MYSQL = java.util.regex.Pattern
	        .compile("^/\\*[M!][0-9].*");

	private static final java.util.regex.Pattern LOAD_DATA = java.util.regex.Pattern.compile("\\bload\\s+data\\b");

	private static final java.util.regex.Pattern INTO_OUTFILE = java.util.regex.Pattern.compile("\\binto\\s+outfile\\b");

	private static final java.util.regex.Pattern INTO_DUMPFILE = java.util.regex.Pattern.compile("\\binto\\s+dumpfile\\b");

	private DatabaseUtil() {
	}

	private static final String MYSQL_DRIVER = "com.mysql.cj.jdbc.Driver";

	private static final String MYSQL_LEGACY_DRIVER = "com.mysql.jdbc.Driver";

	private static final String MARIADB_DRIVER = "org.mariadb.jdbc.Driver";

	private static final String POSTGRESQL_DRIVER = "org.postgresql.Driver";

	private static final String H2_DRIVER = "org.h2.Driver";

	private static final String HSQLDB_DRIVER = "org.hsqldb.jdbcDriver";

	private static final String ORACLE_DRIVER = "oracle.jdbc.driver.OracleDriver";

	private static final String JTDS_DRIVER = "net.sourceforge.jtds.jdbc.Driver";

	private static final String SQLSERVER_DRIVER = "com.microsoft.jdbc.sqlserver.SQLServerDriver";

	private static final Set<String> ALLOWED_JDBC_DRIVERS = Set.of(MYSQL_DRIVER, MYSQL_LEGACY_DRIVER, MARIADB_DRIVER,
	    POSTGRESQL_DRIVER, H2_DRIVER, HSQLDB_DRIVER, ORACLE_DRIVER, SQLSERVER_DRIVER, JTDS_DRIVER);

	private static final Logger log = LoggerFactory.getLogger(DatabaseUtil.class);

	public static final String ORDER_ENTRY_UPGRADE_SETTINGS_FILENAME = "order_entry_upgrade_settings.txt";

	/**
	 * Executes the passed SQL query, enforcing select only if that parameter is set Load the jdbc
	 * driver class for the database which is specified by the connectionUrl and connectionDriver
	 * parameters <br>
	 * <br>
	 * This is only needed when loading up a jdbc connection manually for the first time. This is not
	 * needed by most users and development practices with the openmrs API.
	 *
	 * @param connectionUrl the connection url for the database, such as
	 *            "jdbc:mysql://localhost:3306/..."
	 * @param connectionDriver the database driver class name, such as "com.mysql.cj.jdbc.Driver"
	 * @throws ClassNotFoundException
	 */
	public static String loadDatabaseDriver(String connectionUrl, String connectionDriver) throws ClassNotFoundException {
		if (StringUtils.hasText(connectionDriver)) {
			if (!ALLOWED_JDBC_DRIVERS.contains(connectionDriver)) {
				log.error("Attempted to load an unauthorized database driver: {}", connectionDriver);
				throw new IllegalArgumentException("Database driver '" + connectionDriver + "' is not an allowed driver.");
			}
			Class.forName(connectionDriver);
			log.debug("set user defined Database driver class: " + connectionDriver);
		} else {
			if (connectionUrl.contains("jdbc:mysql")) {
				Class.forName(MYSQL_DRIVER);
				connectionDriver = MYSQL_DRIVER;
			} else if (connectionUrl.contains("jdbc:mariadb")) {
				Class.forName(MARIADB_DRIVER);
				connectionDriver = MARIADB_DRIVER;
			} else if (connectionUrl.contains("jdbc:hsqldb")) {
				Class.forName(HSQLDB_DRIVER);
				connectionDriver = HSQLDB_DRIVER;
			} else if (connectionUrl.contains("jdbc:postgresql")) {
				Class.forName(POSTGRESQL_DRIVER);
				connectionDriver = POSTGRESQL_DRIVER;
			} else if (connectionUrl.contains("jdbc:oracle")) {
				Class.forName(ORACLE_DRIVER);
				connectionDriver = ORACLE_DRIVER;
			} else if (connectionUrl.contains("jdbc:jtds")) {
				Class.forName(JTDS_DRIVER);
				connectionDriver = JTDS_DRIVER;
			} else if (connectionUrl.contains("sqlserver")) {
				Class.forName(SQLSERVER_DRIVER);
				connectionDriver = SQLSERVER_DRIVER;
			} else if (connectionUrl.contains("jdbc:h2")) {
				Class.forName(H2_DRIVER);
				connectionDriver = H2_DRIVER;
			}
		}
		log.info("Set database driver class as " + connectionDriver);
		return connectionDriver;
	}

	/**
	 * Executes the passed SQL query, enforcing select only if that parameter is set for given Session
	 */
	public static List<List<Object>> executeSQL(Session session, String sql, boolean selectOnly) throws DAOException {
		sql = sql.trim();
		boolean dataManipulation = checkQueryForManipulationCommands(sql, selectOnly);

		final List<List<Object>> result = new ArrayList<>();
		final String query = sql;
		final boolean sessionDataManipulation = dataManipulation;

		session.doWork(conn -> populateResultsFromSQLQuery(conn, query, sessionDataManipulation, result));

		return result;
	}

	/**
	 * Executes the passed SQL query, enforcing select only if that parameter is set for given
	 * Connection
	 */
	public static List<List<Object>> executeSQL(Connection conn, String sql, boolean selectOnly) throws DAOException {
		sql = sql.trim();
		boolean dataManipulation = checkQueryForManipulationCommands(sql, selectOnly);
		List<List<Object>> result = new ArrayList<>();
		populateResultsFromSQLQuery(conn, sql, dataManipulation, result);
		return result;
	}

	private static boolean checkQueryForManipulationCommands(String sql, boolean selectOnly) {
		if (selectOnly) {
			validateReadOnlyQuery(sql);
		}

		boolean dataManipulation = false;

		String sqlLower = sql.toLowerCase();
		if (sqlLower.startsWith("insert") || sqlLower.startsWith("update") || sqlLower.startsWith("delete")
		        || sqlLower.startsWith("alter") || sqlLower.startsWith("drop") || sqlLower.startsWith("create")
		        || sqlLower.startsWith("rename")) {
			dataManipulation = true;
		}

		if (selectOnly && dataManipulation) {
			throw new IllegalArgumentException("Illegal command(s) found in query string");
		}
		return dataManipulation;
	}

	private static void validateReadOnlyQuery(String sql) {
		if (sql == null) {
			throw new IllegalArgumentException(ILLEGAL_COMMAND);
		}

		if (hasMultipleStatements(sql) || hasExecutableCommentPrefix(sql)) {
			throw new IllegalArgumentException(ILLEGAL_COMMAND);
		}

		if (!isReadOnlyStatement(sql)) {
			throw new IllegalArgumentException(ILLEGAL_COMMAND);
		}

		if (containsForbiddenPattern(sql)) {
			throw new IllegalArgumentException(ILLEGAL_COMMAND);
		}
	}

	private static boolean isReadOnlyStatement(String sql) {
		String normalized = stripLeadingCommentsAndWhitespace(sql);
		if (!org.springframework.util.StringUtils.hasText(normalized)) {
			return false;
		}

		normalized = stripLeadingParentheses(normalized);
		if (!org.springframework.util.StringUtils.hasText(normalized)) {
			return false;
		}

		String lower = normalized.toLowerCase();

		if (lower.startsWith("with")) {
			if (!lower.contains("select")) {
				return false;
			}
			String withoutStrings = removeStringLiterals(normalized).toLowerCase();
			if (containsStatementKeyword(withoutStrings, "delete") || containsStatementKeyword(withoutStrings, "update")
			        || containsStatementKeyword(withoutStrings, "insert")
			        || containsStatementKeyword(withoutStrings, "replace")
			        || containsStatementKeyword(withoutStrings, "truncate")) {
				return false;
			}
			return true;
		}

		return lower.startsWith("select") || lower.startsWith("values") || lower.startsWith("show")
		        || lower.startsWith("explain") || lower.startsWith("describe") || lower.startsWith("desc");
	}

	private static String stripLeadingParentheses(String sql) {
		String result = sql;
		while (org.springframework.util.StringUtils.hasText(result)) {
			String trimmed = result.trim();
			if (trimmed.startsWith("(")) {
				result = stripLeadingCommentsAndWhitespace(trimmed.substring(1).trim());
			} else {
				break;
			}
		}
		return result;
	}

	private static String removeStringLiterals(String sql) {
		StringBuilder sb = new StringBuilder(sql.length());
		boolean inSingle = false;
		boolean inDouble = false;
		for (int i = 0; i < sql.length(); i++) {
			char c = sql.charAt(i);
			char next = i + 1 < sql.length() ? sql.charAt(i + 1) : 0;
			if (inSingle) {
				if (c == '\\') {
					i++;
					continue;
				}
				if (c == '\'') {
					if (next == '\'') {
						i++;
						continue;
					}
					inSingle = false;
				}
				continue;
			}
			if (inDouble) {
				if (c == '\\') {
					i++;
					continue;
				}
				if (c == '"') {
					if (next == '"') {
						i++;
						continue;
					}
					inDouble = false;
				}
				continue;
			}
			if (c == '\'') {
				inSingle = true;
				continue;
			}
			if (c == '"') {
				inDouble = true;
				continue;
			}
			sb.append(c);
		}
		return sb.toString();
	}

	private static boolean containsStatementKeyword(String sql, String keyword) {
		java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\b" + keyword + "\\b").matcher(sql);
		while (m.find()) {
			// Make sure it's not part of a function call
			int end = m.end();
			if (end < sql.length() && sql.charAt(end) == '(') {
				continue;
			}
			return true;
		}
		return false;
	}

	private static boolean hasExecutableCommentPrefix(String sql) {
		String trimmed = sql.trim();
		return trimmed.startsWith("/*!") || EXECUTABLE_COMMENT_MYSQL.matcher(trimmed).matches();
	}

	private static boolean hasMultipleStatements(String sql) {
		boolean inSingleQuote = false;
		boolean inDoubleQuote = false;
		boolean inLineComment = false;
		boolean inBlockComment = false;

		for (int i = 0; i < sql.length(); i++) {
			char c = sql.charAt(i);
			char nextChar = i + 1 < sql.length() ? sql.charAt(i + 1) : 0;

			if (inLineComment) {
				if (c == '\n' || c == '\r') {
					inLineComment = false;
				}
				continue;
			}

			if (inBlockComment) {
				if (c == '*' && nextChar == '/') {
					inBlockComment = false;
					i++; // skip /
					continue;
				}
				continue;
			}

			if (inSingleQuote) {
				if (c == '\\') {
					i++; // skip escaped char
					continue;
				}
				if (c == '\'') {
					if (nextChar == '\'') {
						i++; // skip escaped quote
						continue;
					}
					inSingleQuote = false;
				}
				continue;
			}

			if (inDoubleQuote) {
				if (c == '\\') {
					i++; // skip escaped char
					continue;
				}
				if (c == '"') {
					if (nextChar == '"') {
						i++; // skip escaped quote
						continue;
					}
					inDoubleQuote = false;
				}
				continue;
			}

			if (c == '-' && nextChar == '-') {
				inLineComment = true;
				i++;
				continue;
			}
			if (c == '#') {
				inLineComment = true;
				continue;
			}
			if (c == '/' && nextChar == '*') {
				// Check if it's executable comment
				if (i + 2 < sql.length() && (sql.charAt(i + 2) == '!' || sql.charAt(i + 2) == 'M')) {
					// Executable comment - will be caught separately
				}
				inBlockComment = true;
				i++;
				continue;
			}
			if (c == '\'') {
				inSingleQuote = true;
				continue;
			}
			if (c == '"') {
				inDoubleQuote = true;
				continue;
			}

			if (c == ';') {
				// Check if it's just trailing whitespace/comments
				String after = sql.substring(i + 1).trim();
				// Remove comments from after
				after = stripCommentsAndWhitespace(after);
				if (!after.isEmpty()) {
					return true;
				}
				return false; // trailing semicolon is ok
			}
		}

		return false;
	}

	private static String stripLeadingCommentsAndWhitespace(String sql) {
		if (sql == null)
			return "";
		String result = sql;
		boolean changed = true;
		while (changed) {
			changed = false;
			String trimmed = result.trim();
			if (trimmed.startsWith("--")) {
				int idx = trimmed.indexOf('\n');
				if (idx >= 0) {
					result = trimmed.substring(idx + 1);
					changed = true;
				} else {
					return "";
				}
			} else if (trimmed.startsWith("#")) {
				int idx = trimmed.indexOf('\n');
				if (idx >= 0) {
					result = trimmed.substring(idx + 1);
					changed = true;
				} else {
					return "";
				}
			} else if (trimmed.startsWith("/*")) {
				// Check if executable comment
				if (trimmed.startsWith("/*!") || trimmed.startsWith("/*M!")) {
					return result; // will be caught
				}
				int idx = trimmed.indexOf("*/");
				if (idx >= 0) {
					result = trimmed.substring(idx + 2);
					changed = true;
				} else {
					return "";
				}
			} else if (!trimmed.equals(result)) {
				result = trimmed;
				changed = true;
			}
		}
		return result.trim();
	}

	private static String stripCommentsAndWhitespace(String sql) {
		if (sql == null || sql.trim().isEmpty())
			return "";
		String result = stripLeadingCommentsAndWhitespace(sql);
		// Also strip trailing comments
		return result;
	}

	private static boolean containsForbiddenPattern(String sql) {
		String lower = sql.toLowerCase();

		int loadFileIdx = lower.indexOf("load_file");
		if (loadFileIdx >= 0 && !isInStringLiteral(sql, loadFileIdx)) {
			return true;
		}

		if (LOAD_DATA.matcher(lower).find()) {
			int idx = lower.indexOf("load");
			if (idx >= 0 && !isInStringLiteral(sql, idx)) {
				return true;
			}
		}

		if (INTO_OUTFILE.matcher(lower).find() || INTO_DUMPFILE.matcher(lower).find()) {
			return true;
		}

		String stripped = stripLeadingParentheses(stripLeadingCommentsAndWhitespace(sql));
		if (!org.springframework.util.StringUtils.hasText(stripped)) {
			return true;
		}
		String sl = stripped.toLowerCase();
		if (sl.startsWith("replace") || sl.startsWith("truncate")) {
			if (sl.length() == 7 || Character.isWhitespace(sl.charAt(7)) || sl.charAt(7) == '(') {
				if (sl.length() >= 8 && sl.charAt(7) == '(') {
					return false;
				}
				return sl.length() == 7 || Character.isWhitespace(sl.charAt(7));
			}
		}

		return false;
	}

	private static boolean isInStringLiteral(String sql, int index) {
		if (index < 0)
			return false;
		boolean inSingleQuote = false;
		boolean inDoubleQuote = false;
		for (int i = 0; i < index && i < sql.length(); i++) {
			char c = sql.charAt(i);
			if (c == '\\') {
				i++;
				continue;
			}
			if (c == '\'' && !inDoubleQuote) {
				if (i + 1 < sql.length() && sql.charAt(i + 1) == '\'') {
					i++;
					continue;
				}
				inSingleQuote = !inSingleQuote;
			} else if (c == '"' && !inSingleQuote) {
				if (i + 1 < sql.length() && sql.charAt(i + 1) == '"') {
					i++;
					continue;
				}
				inDoubleQuote = !inDoubleQuote;
			}
		}
		return inSingleQuote || inDoubleQuote;
	}

	private static void populateResultsFromSQLQuery(Connection conn, String sql, boolean dataManipulation,
	        List<List<Object>> results) {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(sql);
			if (dataManipulation) {
				Integer i = ps.executeUpdate();
				List<Object> row = new ArrayList<>();
				row.add(i);
				results.add(row);
			} else {
				ResultSet resultSet = ps.executeQuery();

				ResultSetMetaData rmd = resultSet.getMetaData();
				int columnCount = rmd.getColumnCount();

				while (resultSet.next()) {
					List<Object> rowObjects = new ArrayList<>();
					for (int x = 1; x <= columnCount; x++) {
						rowObjects.add(resultSet.getObject(x));
					}
					results.add(rowObjects);
				}
			}
		} catch (Exception e) {
			log.debug("Error while running sql: " + sql, e);
			throw new DAOException("Error while running sql: " + sql + " . Message: " + e.getMessage(), e);
		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
					log.error("Error generated while closing statement", e);
				}
			}
		}
	}

	/**
	 * Gets all unique values excluding nulls in the specified column and table
	 *
	 * @param columnName the column
	 * @param tableName the table
	 * @param connection
	 * @return set of unique values
	 * @throws Exception
	 */
	public static <T> Set<T> getUniqueNonNullColumnValues(String columnName, String tableName, Class<T> type,
	        Connection connection) throws Exception {
		Set<T> uniqueValues = new HashSet<>();
		final String alias = "unique_values";
		String select = "SELECT DISTINCT " + columnName + " AS " + alias + " FROM " + tableName + " WHERE " + columnName
		        + " IS NOT NULL";
		List<List<Object>> rows = DatabaseUtil.executeSQL(connection, select, true);
		for (List<Object> row : rows) {
			//There can only be one column since we are selecting one
			uniqueValues.add((T) row.get(0));
		}

		return uniqueValues;
	}
}
