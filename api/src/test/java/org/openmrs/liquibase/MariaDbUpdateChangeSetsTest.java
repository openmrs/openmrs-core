/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.liquibase;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import liquibase.ChecksumVersion;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.parser.core.xml.XMLChangeLogSAXParser;
import liquibase.resource.ClassLoaderResourceAccessor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for TRUNK-6634: changesets restricted to dbms="mysql" were silently skipped on MariaDB 11+,
 * because MariaDB 11 dropped the "5.5.5-" version prefix and is no longer detected as MySQL. The
 * cohort_member changesets in the 2.1.x update log must therefore run on MariaDB as well, and a
 * dedicated MariaDB changeset must backfill cohort_member.uuid before the unique constraint is
 * applied.
 * <p>
 * The historical checksums pinned here are the values stored in the DATABASECHANGELOG table of
 * existing production databases. Editing the dbms attribute of a changeset does not change its
 * checksum, which is what makes the in-place fix safe; these tests fail if anyone edits those
 * changesets in a way that would break checksum validation for existing deployments.
 */
public class MariaDbUpdateChangeSetsTest {

	private static final String UPDATE_2_1_X = "org/openmrs/liquibase/updates/liquibase-update-to-latest-2.1.x.xml";

	private static final String MARIADB_UUID_BACKFILL_ID = "TRUNK-6634-2026-07-03-1000";

	private static List<ChangeSet> changeSets;

	@BeforeAll
	public static void parseChangeLog() throws Exception {
		DatabaseChangeLog changeLog = new XMLChangeLogSAXParser().parse(UPDATE_2_1_X, new ChangeLogParameters(),
		    new ClassLoaderResourceAccessor(MariaDbUpdateChangeSetsTest.class.getClassLoader()));
		changeSets = changeLog.getChangeSets();
	}

	@Test
	public void shouldRunCohortMemberChangeSetsOnMariaDb() {
		for (String id : new String[] { "201609171146-2.1", "201609171146-2.2", "201609171146-2.3" }) {
			ChangeSet changeSet = findById(id);
			Set<String> dbms = changeSet.getDbmsSet();
			assertNotNull(dbms, id + " should declare a dbms attribute");
			assertTrue(dbms.contains("mysql"), id + " should still run on mysql");
			assertTrue(dbms.contains("mariadb"), id + " should run on mariadb (TRUNK-6634)");
		}
	}

	@Test
	public void shouldKeepHistoricalChecksumsOfEditedChangeSets() {
		// Values recorded in DATABASECHANGELOG of deployments that ran these changesets before
		// TRUNK-6634. If one of these assertions fails, the change being made will break
		// "liquibase validate" on every existing OpenMRS database and must not be merged.
		assertHistoricalChecksum("201609171146-2.1", "8:0958768d360dcd6a6f07dec01138c55d");
		assertHistoricalChecksum("201609171146-2.2", "8:efac82e8f62fe999ae8cdd851256a571");
		assertHistoricalChecksum("201609171146-2.3", "8:7c93380a4682620b5c0881d6b02bfbdf");
		assertHistoricalChecksum("201610042145-2.1", "8:5618aaee0ff5e7e349424bb341b955f7");
	}

	@Test
	public void shouldRunMariaDbUuidBackfillBetweenUuidCreationAndUniqueConstraint() {
		ChangeSet backfill = findById(MARIADB_UUID_BACKFILL_ID);
		Set<String> dbms = backfill.getDbmsSet();
		assertNotNull(dbms, "uuid backfill should declare a dbms attribute");
		assertEquals(Set.of("mariadb"), dbms, "uuid backfill must only run on mariadb");

		int uuidColumnAdded = indexOf("201610042145-2");
		int mysqlOracleBackfill = indexOf("201610042145-2.1");
		int mariaDbBackfill = indexOf(MARIADB_UUID_BACKFILL_ID);
		int uniqueConstraint = indexOf("201610042145-2.2");

		assertTrue(uuidColumnAdded < mariaDbBackfill, "uuid column must exist before the mariadb backfill");
		assertTrue(mysqlOracleBackfill < mariaDbBackfill,
		    "mariadb backfill should follow the historical mysql/oracle backfill");
		assertTrue(mariaDbBackfill < uniqueConstraint,
		    "mariadb backfill must run before the unique constraint on cohort_member.uuid");
	}

	@Test
	public void shouldNotEditHistoricalUuidBackfillForMariaDb() {
		// The mysql/oracle uuid backfill cannot be extended to mariadb in place: adding a
		// modifySql block changes the checksum (unlike dbms attribute or precondition edits).
		// MariaDB support is provided by the dedicated changeset instead.
		ChangeSet historical = findById("201610042145-2.1");
		assertEquals(2, historical.getSqlVisitors().size(),
		    "201610042145-2.1 must keep exactly its two historical modifySql blocks (mysql, oracle); "
		            + "extend MariaDB behavior via " + MARIADB_UUID_BACKFILL_ID + " instead");
	}

	private static void assertHistoricalChecksum(String id, String expected) {
		assertEquals(expected, findById(id).generateCheckSum(ChecksumVersion.V8).toString(),
		    id + " checksum drifted from the value stored in existing databases");
	}

	private static ChangeSet findById(String id) {
		ChangeSet result = changeSets.stream().filter(c -> id.equals(c.getId())).findFirst().orElse(null);
		assertNotNull(result, "changeset " + id + " not found in " + UPDATE_2_1_X);
		return result;
	}

	private static int indexOf(String id) {
		return changeSets.indexOf(findById(id));
	}
}
