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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
 * Guards the v8 checksums of the changelogs touched by TRUNK-6634 against accidental change.
 * <p>
 * The baseline file was generated from the changelogs as they were before TRUNK-6634, i.e. it
 * records the checksums that existing production databases have stored in their DATABASECHANGELOG
 * table. TRUNK-6634 extended dbms="mysql" changesets to dbms="mysql,mariadb", which does not change
 * checksums; this test proves that claim for every single changeset and fails on any future edit
 * that would break "liquibase validate" for existing deployments.
 */
public class ChangeSetChecksumBaselineTest {

	private static final String BASELINE = "org/openmrs/liquibase/liquibase-v8-checksum-baseline.tsv";

	private static final Set<String> CHANGESETS_ADDED_BY_TRUNK_6634 = Set.of("TRUNK-6634-2026-07-03-1000",
	    "TRUNK-6634-2026-07-03-1002");

	/**
	 * Changesets that must remain restricted to mysql (and oracle where declared) because extending
	 * them requires modifySql blocks, which are part of the checksum. MariaDB is covered instead by the
	 * GenerateUuid fallback in 20090831-1041-scheduler_task_config (which runs for every dbms other
	 * than mysql/oracle/mssql) and by the dedicated changeset TRUNK-6634-2026-07-03-1002 for the
	 * 'System Developer' role.
	 */
	private static final Set<String> MYSQL_ONLY_BY_DESIGN = Set.of("20100128-1", "20090831-1040-scheduler_task_config");

	private static final String[] CHANGELOGS = { "org/openmrs/liquibase/updates/liquibase-update-to-latest-2.0.x.xml",
	        "org/openmrs/liquibase/updates/liquibase-update-to-latest-2.1.x.xml",
	        "org/openmrs/liquibase/snapshots/core-data/liquibase-core-data-1.9.x.xml" };

	@Test
	public void shouldKeepEveryHistoricalChecksumIdenticalToBaseline() throws Exception {
		Map<String, String> baseline = loadBaseline();
		int verified = 0;
		List<String> unexpectedNew = new ArrayList<>();
		for (String changelog : CHANGELOGS) {
			for (ChangeSet changeSet : parse(changelog)) {
				String key = changelog + "::" + changeSet.getId() + "::" + changeSet.getAuthor();
				String expected = baseline.get(key);
				if (expected == null) {
					if (!CHANGESETS_ADDED_BY_TRUNK_6634.contains(changeSet.getId())) {
						unexpectedNew.add(key);
					}
					continue;
				}
				assertEquals(expected, changeSet.generateCheckSum(ChecksumVersion.V8).toString(),
				    "checksum of " + key + " drifted from the value stored in existing databases");
				verified++;
			}
		}
		assertEquals(718, verified, "some baseline changesets disappeared from the changelogs");
		assertEquals(List.of(), unexpectedNew,
		    "new changesets in guarded changelogs must be added to the baseline deliberately");
	}

	@Test
	public void shouldRunMysqlChangeSetsOnMariaDbUnlessExcludedByDesign() throws Exception {
		List<String> violations = new ArrayList<>();
		for (String changelog : CHANGELOGS) {
			for (ChangeSet changeSet : parse(changelog)) {
				Set<String> dbms = changeSet.getDbmsSet();
				if (dbms == null || !dbms.contains("mysql")) {
					continue;
				}
				if (MYSQL_ONLY_BY_DESIGN.contains(changeSet.getId())) {
					assertTrue(!dbms.contains("mariadb"),
					    changeSet.getId() + " is documented as mysql-only; update MYSQL_ONLY_BY_DESIGN if this is intended");
					continue;
				}
				if (!dbms.contains("mariadb")) {
					violations.add(changelog + "::" + changeSet.getId());
				}
			}
		}
		assertEquals(List.of(), violations,
		    "dbms=\"mysql\" changesets are silently skipped on MariaDB 11+ (TRUNK-6634); include mariadb "
		            + "or document the exclusion in MYSQL_ONLY_BY_DESIGN");
	}

	@Test
	public void shouldOrderMariaDbRepairChangeSetsBeforeTheirDependentChangeSets() throws Exception {
		List<ChangeSet> changeSets = parse("org/openmrs/liquibase/updates/liquibase-update-to-latest-2.0.x.xml");
		int schedulerBackfill = indexOf(changeSets, "20090831-1040-scheduler_task_config");
		int schedulerJavaFallback = indexOf(changeSets, "20090831-1041-scheduler_task_config");
		int schedulerConstraint = indexOf(changeSets, "20090831-1042-scheduler_task_config");
		int roleInsert = indexOf(changeSets, "20100128-1");
		int mariaDbRoleInsert = indexOf(changeSets, "TRUNK-6634-2026-07-03-1002");
		int roleSwitch = indexOf(changeSets, "20100128-2");

		// scheduler_task_config needs no dedicated mariadb changeset: the java fallback in
		// 20090831-1041 runs for every dbms other than mysql/oracle/mssql, including mariadb,
		// and must stay ordered between the mysql/oracle backfill and the not-null constraint.
		assertTrue(schedulerBackfill < schedulerJavaFallback,
		    "scheduler_task_config java fallback should follow the mysql/oracle backfill");
		assertTrue(schedulerJavaFallback < schedulerConstraint,
		    "scheduler_task_config java fallback must run before the uuid not-null constraint");

		assertTrue(roleInsert < mariaDbRoleInsert,
		    "mariadb 'System Developer' insert should follow the historical mysql insert");
		assertTrue(mariaDbRoleInsert < roleSwitch,
		    "mariadb 'System Developer' insert must run before 20100128-2 reassigns users to that role");
	}

	private static Map<String, String> loadBaseline() throws Exception {
		Map<String, String> baseline = new HashMap<>();
		try (InputStream in = ChangeSetChecksumBaselineTest.class.getClassLoader().getResourceAsStream(BASELINE)) {
			assertNotNull(in, "baseline resource not found: " + BASELINE);
			BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.isBlank()) {
					continue;
				}
				String[] parts = line.split("\t");
				baseline.put(parts[0] + "::" + parts[1] + "::" + parts[2], parts[3]);
			}
		}
		assertEquals(718, baseline.size(), "baseline file should contain 718 unique changesets");
		return baseline;
	}

	private static List<ChangeSet> parse(String changelog) throws Exception {
		DatabaseChangeLog log = new XMLChangeLogSAXParser().parse(changelog, new ChangeLogParameters(),
		    new ClassLoaderResourceAccessor(ChangeSetChecksumBaselineTest.class.getClassLoader()));
		return log.getChangeSets();
	}

	private static int indexOf(List<ChangeSet> changeSets, String id) {
		for (int i = 0; i < changeSets.size(); i++) {
			if (id.equals(changeSets.get(i).getId())) {
				return i;
			}
		}
		throw new AssertionError("changeset " + id + " not found in 2.0.x changelog");
	}
}
