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

import org.junit.jupiter.api.Test;
import java.sql.Connection;

import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DatabaseUtilTest {
	
	@Test
	public void executeSQL_shouldBlockStackedQueriesWhenSelectOnlyIsTrue() {
		// Attack: Piggybacking a DROP TABLE command after a harmless SELECT
		String stackedQuery = "SELECT 1; DROP TABLE users;";
		
		// We pass 'null' for the connection because the validation should throw 
		// the exception before the connection is ever needed.
		assertThrows(IllegalArgumentException.class, () -> {
			DatabaseUtil.executeSQL((Connection) null, stackedQuery, true);
		}, "Should have blocked a stacked query containing a DROP TABLE statement.");
	}
	
	@Test
	public void executeSQL_shouldBlockIntoOutfileAttacksWhenSelectOnlyIsTrue() {
		// Attack: Data Exfiltration using INTO OUTFILE
		String exfiltrationQuery = "SELECT username FROM users INTO OUTFILE '/tmp/passwords.txt'";
		
		assertThrows(IllegalArgumentException.class, () -> {
			DatabaseUtil.executeSQL((Connection) null, exfiltrationQuery, true);
		}, "Should have blocked a SELECT ... INTO OUTFILE attack.");
	}
	
	@Test
	public void executeSQL_shouldBlockHiddenUpdatesWhenSelectOnlyIsTrue() {
		// Regression Test: Ensure the parser correctly identifies commands 
		// even with leading whitespace and mixed casing.
		String sneakyUpdate = "   uPdAtE patient SET voided = 1";
		
		assertThrows(IllegalArgumentException.class, () -> {
			DatabaseUtil.executeSQL((Connection) null, sneakyUpdate, true);
		}, "Should have blocked an UPDATE statement hidden by whitespace.");
	}
	
	@Test
	public void executeSQL_shouldBlockUpdateHiddenByCommentsWhenSelectOnlyIsTrue() {
		// Attack: Using a SQL block comment to bypass the startsWith check
		String commentBypass = "/* harmless comment */ UPDATE patient SET voided = 1";
		
		assertThrows(IllegalArgumentException.class, () -> {
			DatabaseUtil.executeSQL((Connection) null, commentBypass, true);
		}, "Should have blocked an UPDATE statement hidden behind a SQL comment.");
	}
	
	@Test
	public void executeSQL_shouldBlockTruncateWhenSelectOnlyIsTrue() {
		// Attack: Using a highly destructive command that the developer forgot to put on the blocklist
		String unlistedCommand = "TRUNCATE TABLE patient";
		
		assertThrows(IllegalArgumentException.class, () -> {
			DatabaseUtil.executeSQL((Connection) null, unlistedCommand, true);
		}, "Should have blocked a TRUNCATE statement because it wasn't on the original blocklist.");
	}
	
	@Test
	public void executeSQL_shouldNotThrowFalsePositiveOnSafeStringLiterals() {
		// A completely safe query that happens to contain the word "INTO" inside a string
		String safeQuery = "SELECT notes FROM patient WHERE notes LIKE '%moved into room%'";
		
		try {
			DatabaseUtil.executeSQL((Connection) null, safeQuery, true);
		} catch (IllegalArgumentException e) {
			fail("Should NOT have thrown an IllegalArgumentException. The word INTO is inside a string literal.");
		} catch (Exception e) {
			// We ignore NullPointer/DAOException because we passed a null connection intentionally.
			// As long as it reached this point, it means the security check passed!
		}
	}
	
}
