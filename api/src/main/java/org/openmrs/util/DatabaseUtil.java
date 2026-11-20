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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	private static final Pattern LOAD_DATA = Pattern.compile("\\bload\\s+data\\b");

	private static final Pattern INTO_OUTFILE = Pattern.compile("\\binto\\s+outfile\\b");

	private static final Pattern INTO_DUMPFILE = Pattern.compile("\\binto\\s+dumpfile\\b");

	private static final Pattern LOAD_FILE = Pattern.compile("\\bload_file\\s*\\(");

	private static final Pattern FIRST_TOKEN = Pattern.compile("\\A\\s*([a-z]+)");

	/**
	 * Server-side functions that change data or server state without a writing command keyword. The
	 * call form is matched, so a column of the same name without parentheses stays readable.
	 */
	private static final Pattern SERVER_SIDE_FUNCTIONS = Pattern
	        .compile("\\b(lo_(import|export|unlink)|pg_(read_file|read_file_text|read_binary_file|stat_file|ls_dir"
	                + "|reload_conf|rotate_logfile|terminate_backend|cancel_backend|advisory_\\w+|stat_reset\\w*"
	                + "|stat_clear_snapshot|create_restore_point|switch_wal|switch_xlog|start_backup|backup_start)"
	                + "|file_read|csvread|csvwrite)\\s*\\(");

	/**
	 * A quoted server side function invocation; the closing quote anchors the match so a longer quoted
	 * column sharing the prefix stays readable.
	 */
	private static final Pattern QUOTED_SERVER_SIDE_FUNCTIONS = Pattern.compile(
	    "\\\"(lo_(import|export|unlink)|pg_(read_file|read_file_text|read_binary_file|stat_file|ls_dir"
	            + "|reload_conf|rotate_logfile|terminate_backend|cancel_backend|advisory_\\w+|stat_reset\\w*"
	            + "|stat_clear_snapshot|create_restore_point|switch_wal|switch_xlog|start_backup|backup_start))\\\"\\s*\\(");

	/**
	 * PostgreSQL U& escape forms decode to arbitrary text, so they are rejected instead of resolved.
	 */
	private static final Pattern UNICODE_ESCAPE_FORM = Pattern.compile("(?i)\\bu\\&\\s*[\\\"']");

	/**
	 * Dollar quoted strings are PostgreSQL only and rejected instead of resolved. The look-behind keeps
	 * identifiers spanning the dollar sign readable.
	 */
	private static final Pattern DOLLAR_QUOTE_TAG = Pattern
	        .compile("(?<![a-zA-Z0-9_])\\$\\$|(?<![a-zA-Z0-9_])\\$[a-zA-Z0-9_]+\\$");

	/** Commands that only read data. The leading keyword must match one of these exactly. */
	private static final Set<String> READ_ONLY_COMMANDS = Collections
	        .unmodifiableSet(new HashSet<>(Arrays.asList("select", "with", "values", "show", "describe", "desc", "table")));

	/** The bare keyword forms of the non parenthesized explain option list. */
	private static final Set<String> EXPLAIN_OPTION_KEYWORDS = Collections
	        .unmodifiableSet(new HashSet<>(Arrays.asList("analyze", "verbose")));

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
		if (sql == null) {
			throw new DAOException("executeSQL.error.nullQuery", new Object[0]);
		}
		sql = sql.trim();
		if (selectOnly) {
			validateReadOnlyQuery(sql);
		}

		final List<List<Object>> result = new ArrayList<>();
		final String query = sql;

		session.doWork(conn -> populateResultsFromSQLQuery(conn, query, result));

		return result;
	}

	/**
	 * Executes the passed SQL query, enforcing select only if that parameter is set for given
	 * Connection
	 */
	public static List<List<Object>> executeSQL(Connection conn, String sql, boolean selectOnly) throws DAOException {
		if (sql == null) {
			throw new DAOException("executeSQL.error.nullQuery", new Object[0]);
		}
		sql = sql.trim();
		if (selectOnly) {
			validateReadOnlyQuery(sql);
		}
		List<List<Object>> result = new ArrayList<>();
		populateResultsFromSQLQuery(conn, sql, result);
		return result;
	}

	/**
	 * Validates that the given SQL can only read data.
	 *
	 * @param sql the statement to validate
	 * @throws DAOException when the statement cannot be shown to be read-only
	 */
	static void validateReadOnlyQuery(String sql) {
		if (sql == null) {
			throw new DAOException("executeSQL.error.nullQuery", new Object[0]);
		}

		SqlText text = analyzeSql(sql);
		if (text.hasMultipleStatements) {
			throw new DAOException("executeSQL.error.multipleStatements", new Object[0]);
		}
		// structures that the engines resolve differently fail closed
		if (text.hasWritableComment || text.hasHashOutsideString || text.hasUnclosedQuote || text.hasAmbiguousQuoteEscape
		        || text.hasAmbiguousCommentMarker || text.hasNestedCommentDivider) {
			throw new DAOException("executeSQL.error.unresolvedSyntax", new Object[0]);
		}

		if (!isReadOnlyStatement(text)) {
			throw new DAOException("executeSQL.error.notReadOnly", new Object[0]);
		}

		// the identifier visible pass keeps quoted function names in scope
		if (containsForbiddenPattern(text.blankedOut.toLowerCase(Locale.ROOT),
		    text.identifiersKept.toLowerCase(Locale.ROOT))) {
			throw new DAOException("executeSQL.error.forbiddenFunction", new Object[0]);
		}
	}

	/**
	 * One pass produces the structure flags and the text views the checks use.
	 */
	static SqlText analyzeSql(String sql) {
		SqlText text = new SqlText();
		StringBuilder withoutComments = new StringBuilder(sql.length());
		StringBuilder blankedOut = new StringBuilder(sql.length());
		StringBuilder identifiersKept = new StringBuilder(sql.length());

		boolean inSingleQuote = false;
		boolean inDoubleQuote = false;
		boolean inIdentifierQuote = false;
		boolean inLineComment = false;
		boolean inBlockComment = false;

		for (int i = 0; i < sql.length(); i++) {
			char c = sql.charAt(i);
			char next = i + 1 < sql.length() ? sql.charAt(i + 1) : '\0';

			if (inLineComment) {
				if (c == '\n' || c == '\r') {
					inLineComment = false;
					withoutComments.append(c);
					blankedOut.append(c);
					identifiersKept.append(c);
				} else {
					blankedOut.append(' ');
					identifiersKept.append(' ');
				}
				continue;
			}

			if (inBlockComment) {
				if (c == '*' && next == '/') {
					withoutComments.append(' ');
					blankedOut.append("  ");
					identifiersKept.append("  ");
					inBlockComment = false;
					i++;
					continue;
				}
				if (c == '/' && next == '*') {
					// nested dividers fail closed across engines
					text.hasNestedCommentDivider = true;
				}
				blankedOut.append(' ');
				identifiersKept.append(' ');
				continue;
			}

			if (inSingleQuote || inDoubleQuote) {
				withoutComments.append(c);
				blankedOut.append(' ');
				if (inSingleQuote) {
					// single quoted content stays out of the quoted pass
					identifiersKept.append(' ');
				} else {
					// double quoted identifiers stay in the quoted pass, delimiters included
					identifiersKept.append(c);
				}
				if (c == '\\') {
					if ((inSingleQuote && next == '\'') || (inDoubleQuote && next == '"')) {
						// engines disagree where the string ends; fail closed
						text.hasAmbiguousQuoteEscape = true;
					}
					i++;
					continue;
				}
				if (c == '\'' || c == '"') {
					if (next == c) {
						withoutComments.append(next);
						blankedOut.append(' ');
						if (inSingleQuote) {
							identifiersKept.append(' ');
						} else {
							identifiersKept.append(next);
						}
						i++;
						continue;
					}
					if (inSingleQuote && c == '\'') {
						inSingleQuote = false;
					} else if (inDoubleQuote && c == '"') {
						inDoubleQuote = false;
					}
				}
				continue;
			}

			if (inIdentifierQuote) {
				withoutComments.append(c);
				if (c == '`') {
					if (next == '`') {
						blankedOut.append(' ');
						identifiersKept.append(next);
						i++;
						continue;
					}
					inIdentifierQuote = false;
				} else {
					identifiersKept.append(c);
				}
				blankedOut.append(' ');
				continue;
			}

			if (c == '\'') {
				inSingleQuote = true;
			} else if (c == '"') {
				inDoubleQuote = true;
			} else if (c == '`') {
				inIdentifierQuote = true;
			}

			if (c == '-' && next == '-') {
				if (i + 2 >= sql.length() || Character.isWhitespace(sql.charAt(i + 2))) {
					inLineComment = true;
					withoutComments.append(' ');
					blankedOut.append("  ");
					identifiersKept.append("  ");
					i++;
					continue;
				}
				// unresolved across engines
				text.hasAmbiguousCommentMarker = true;
			}

			if (c == '#') {
				// resolved differently across engines; fail closed
				text.hasHashOutsideString = true;
			}

			if (c == '/' && next == '*') {
				if (isExecutableCommentStart(sql, i)) {
					text.hasWritableComment = true;
				}
				inBlockComment = true;
				withoutComments.append(' ');
				blankedOut.append("  ");
				identifiersKept.append("  ");
				i++;
				continue;
			}

			if (c == ';') {
				// a semicolon at the very end is allowed; anything beyond it makes the statement a list
				String remainder = stripLeadingCommentsAndWhitespace(sql.substring(i + 1));
				if (StringUtils.hasText(remainder)) {
					text.hasMultipleStatements = true;
				}
			}

			withoutComments.append(c);
			blankedOut.append(c);
			identifiersKept.append(c);
		}

		text.withoutComments = withoutComments.toString();
		text.blankedOut = blankedOut.toString();
		text.identifiersKept = identifiersKept.toString();
		text.hasUnclosedQuote = inSingleQuote || inDoubleQuote || inIdentifierQuote;
		return text;
	}

	private static boolean isExecutableCommentStart(String sql, int position) {
		if (position + 2 >= sql.length()) {
			return false;
		}
		// MySQL: /*!...*/. MariaDB uses the same form plus /*M!nnnnnn...*/; both run as code
		return sql.charAt(position + 2) == '!'
		        || (sql.charAt(position + 2) == 'M' && position + 3 < sql.length() && sql.charAt(position + 3) == '!');
	}

	static boolean isReadOnlyStatement(String sql) {
		SqlText text = analyzeSql(sql);
		return isReadOnlyStatement(text);
	}

	private static boolean isReadOnlyStatement(SqlText text) {
		String normalized = text.withoutComments.trim();
		if (!StringUtils.hasText(normalized)) {
			return false;
		}

		normalized = stripLeadingParentheses(normalized);
		if (!StringUtils.hasText(normalized)) {
			return false;
		}

		String lower = normalized.toLowerCase(Locale.ROOT);

		if (lower.startsWith("explain")) {
			// EXPLAIN ANALYZE runs the explained statement on some engines, so the explained command has
			// to pass the same check as a standalone statement
			String explained = stripExplainOptions(normalized);
			String peeled = stripLeadingParentheses(explained);
			return isReadOnlyCommand(peeled);
		}

		return isReadOnlyCommand(normalized);
	}

	private static String stripExplainOptions(String sql) {
		String remainder = sql.substring("explain".length()).trim();
		if (remainder.startsWith("(")) {
			int depth = 0;
			boolean closed = false;
			for (int i = 0; i < remainder.length(); i++) {
				char c = remainder.charAt(i);
				if (c == '(') {
					depth++;
				} else if (c == ')') {
					depth--;
					if (depth == 0) {
						remainder = remainder.substring(i + 1).trim();
						closed = true;
						break;
					}
				}
			}
			if (!closed) {
				// the option list never closes, so no explained command can be located
				return "";
			}
		}
		return removeLeadingOptionKeywords(remainder);
	}

	/**
	 * Removes leading option keywords of the non parenthesized explain forms, e.g. EXPLAIN ANALYZE
	 * SELECT ...; the option that runs the statement itself belongs to it.
	 */
	private static String removeLeadingOptionKeywords(String sql) {
		String remainder = sql;
		boolean changed = true;
		while (changed) {
			changed = false;
			for (String option : EXPLAIN_OPTION_KEYWORDS) {
				if (remainder.equalsIgnoreCase(option)) {
					return "";
				}
				if (remainder.length() > option.length() && Character.isWhitespace(remainder.charAt(option.length()))
				        && remainder.regionMatches(true, 0, option, 0, option.length())) {
					remainder = remainder.substring(option.length()).trim();
					changed = true;
				}
			}
		}
		return remainder;
	}

	/**
	 * Checks the leading command keyword, requiring it to be a whole token so that a different word
	 * sharing the same prefix is never accepted.
	 */
	private static boolean isReadOnlyCommand(String input) {
		if (!StringUtils.hasText(input)) {
			return false;
		}

		Matcher matcher = FIRST_TOKEN.matcher(input.toLowerCase(Locale.ROOT));
		if (!matcher.find()) {
			return false;
		}

		// the greedy letter run swallows the whole leading word, so an allowed short keyword can
		// never be the start of a longer letter word (descrypto, selection...); sql punctuation like
		// the star of SELECT* FROM belongs to the statement body and stays readable
		return READ_ONLY_COMMANDS.contains(matcher.group(1));
	}

	private static String stripLeadingParentheses(String sql) {
		String result = sql;
		while (StringUtils.hasText(result)) {
			String trimmed = result.trim();
			if (trimmed.startsWith("(")) {
				result = stripLeadingCommentsAndWhitespace(trimmed.substring(1).trim());
			} else {
				break;
			}
		}
		return result;
	}

	/**
	 * Strips leading whitespace and leading comments, including a whole line for line comments.
	 */
	private static String stripLeadingCommentsAndWhitespace(String sql) {
		if (sql == null) {
			return "";
		}
		String result = sql;
		boolean changed = true;
		while (changed) {
			changed = false;
			String trimmed = result.trim();
			if (trimmed.startsWith("--") && isLineCommentStart(trimmed)) {
				// both line endings end the comment on the engines
				int idx = trimmed.indexOf('\n');
				int idxCr = trimmed.indexOf('\r');
				if (idxCr >= 0 && (idx < 0 || idxCr < idx)) {
					idx = idxCr;
				}
				if (idx >= 0) {
					result = trimmed.substring(idx + 1);
					changed = true;
				} else {
					return "";
				}
			} else if (trimmed.startsWith("/*")) {
				if (isExecutableCommentStart(trimmed, 0)) {
					return result;
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

	/** Line comments are only recognised in the whitespace form, matching MySQL. */
	private static boolean isLineCommentStart(String sql) {
		return sql.length() == 2 || Character.isWhitespace(sql.charAt(2));
	}

	/** Checks the statement against the blanked copy and the identifier visible copy. */
	private static boolean containsForbiddenPattern(String lower, String identifiersKeptLower) {
		// the identifier visible pass keeps quoted function names and PostgreSQL escape forms in scope
		if (SERVER_SIDE_FUNCTIONS.matcher(identifiersKeptLower).find() || LOAD_FILE.matcher(identifiersKeptLower).find()
		        || QUOTED_SERVER_SIDE_FUNCTIONS.matcher(identifiersKeptLower).find()
		        || UNICODE_ESCAPE_FORM.matcher(identifiersKeptLower).find()) {
			return true;
		}
		return LOAD_DATA.matcher(lower).find() || INTO_OUTFILE.matcher(lower).find() || INTO_DUMPFILE.matcher(lower).find()
		        || DOLLAR_QUOTE_TAG.matcher(lower).find();
	}

	/** Runs the statement on the connection and fills the results. */
	private static void populateResultsFromSQLQuery(Connection conn, String sql, List<List<Object>> results) {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(sql);
			if (ps.execute()) {
				ResultSet resultSet = ps.getResultSet();

				ResultSetMetaData rmd = resultSet.getMetaData();
				int columnCount = rmd.getColumnCount();

				while (resultSet.next()) {
					List<Object> rowObjects = new ArrayList<>();
					for (int x = 1; x <= columnCount; x++) {
						rowObjects.add(resultSet.getObject(x));
					}
					results.add(rowObjects);
				}
			} else {
				List<Object> row = new ArrayList<>();
				row.add(ps.getUpdateCount());
				results.add(row);
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

	/**
	 * Holds what one scan of the statement has to answer before it may run.
	 */
	private static final class SqlText {

		private String withoutComments;

		private String blankedOut;

		private String identifiersKept;

		private boolean hasMultipleStatements;

		private boolean hasWritableComment;

		private boolean hasHashOutsideString;

		private boolean hasUnclosedQuote;

		private boolean hasAmbiguousQuoteEscape;

		private boolean hasAmbiguousCommentMarker;

		private boolean hasNestedCommentDivider;
	}
}
