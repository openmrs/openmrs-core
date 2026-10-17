/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db.hibernate;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.openmrs.Diagnosis;
import org.openmrs.Encounter;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Loading an encounter's diagnoses must not pull the whole object graph into one statement through
 * default-eager to-one associations. MySQL refuses any statement that joins more than 61 tables.
 */
public class EncounterDiagnosesFetchTest extends BaseContextSensitiveTest {

	private static final String DATASET = "org/openmrs/api/db/hibernate/include/EncounterDiagnosesFetchTest.xml";

	private static final Pattern JOIN = Pattern.compile("\\bjoin\\b", Pattern.CASE_INSENSITIVE);

	// well under MySQL's limit of 61 tables per join
	private static final int MAX_TABLES_PER_STATEMENT = 30;

	@Autowired
	private SessionFactory sessionFactory;

	@Test
	public void getDiagnoses_shouldNotJoinAnUnboundedNumberOfTables() {
		executeDataSet(DATASET);
		Session current = sessionFactory.getCurrentSession();
		current.flush();
		current.clear();
		sessionFactory.getCache().evictAllRegions();

		List<String> statements = new ArrayList<>();
		UnaryOperator<String> recorder = sql -> {
			statements.add(sql);
			return sql;
		};
		// a separate session on the test's connection, so it sees the uncommitted dataset
		Connection connection = current.doReturningWork(c -> c);
		try (Session session = sessionFactory.withOptions().connection(connection).statementInspector(recorder)
		        .openSession()) {
			Encounter encounter = session.get(Encounter.class, 3);
			Hibernate.initialize(encounter.getDiagnoses());

			assertEquals(1, encounter.getDiagnoses().size());
			Diagnosis diagnosis = encounter.getDiagnoses().iterator().next();
			assertEquals(102, diagnosis.getCondition().getConditionId());
			assertEquals(101, diagnosis.getCondition().getPreviousVersion().getConditionId());
		}

		assertFalse(statements.isEmpty(), "no statements were recorded");
		for (String sql : statements) {
			int tables = countTables(sql);
			assertTrue(tables <= MAX_TABLES_PER_STATEMENT,
			    "statement joins " + tables + " tables (max " + MAX_TABLES_PER_STATEMENT + "): " + sql);
		}
	}

	private static int countTables(String sql) {
		Matcher matcher = JOIN.matcher(sql);
		int tables = 1;
		while (matcher.find()) {
			tables++;
		}
		return tables;
	}
}
