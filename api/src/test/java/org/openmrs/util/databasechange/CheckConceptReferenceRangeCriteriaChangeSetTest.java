/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util.databasechange;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

import liquibase.database.core.H2Database;
import liquibase.database.jvm.JdbcConnection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CheckConceptReferenceRangeCriteriaChangeSetTest extends BaseContextSensitiveTest {

	private static final String RANGE_UUID = "d8ad3ceb-00a4-4484-a1ff-700f7ac3d10d";

	@Test
	public void describeProblem_shouldReturnNullForAllowedCriteria() {
		assertNull(CheckConceptReferenceRangeCriteriaChangeSet
		        .describeProblem("$patient.getAge() &gt;= 1 &amp;&amp; $fn.getLatestObs('CIEL:1', $patient) != null"));
	}

	@Test
	public void describeProblem_shouldReturnNullForBlankCriteria() {
		assertNull(CheckConceptReferenceRangeCriteriaChangeSet.describeProblem(" "));
	}

	@Test
	public void describeProblem_shouldReportCriteriaThatDoesNotParse() {
		String problem = CheckConceptReferenceRangeCriteriaChangeSet.describeProblem("$patient.getAge( >");
		assertTrue(problem.contains("does not parse"));
	}

	@Test
	public void describeProblem_shouldAskForARewriteWhenOnlyReadOnlyCallsAreDisallowed() {
		String problem = CheckConceptReferenceRangeCriteriaChangeSet
		        .describeProblem("$patient.getGender().equals('M') && $patient.getAttribute('Race') != null");
		assertTrue(problem.contains("Rewrite it") && problem.contains("because it calls equals('M')."));
	}

	@Test
	public void describeProblem_shouldAskForARewriteForReadOnlyComparisonsAndCollectionCalls() {
		String problem = CheckConceptReferenceRangeCriteriaChangeSet.describeProblem(
		    "$patient.getBirthdate().after($date) && $patient.getNames().size() > 0 && $patient.gender.toUpperCase() == 'M'");
		assertTrue(problem.contains("Rewrite it") && !problem.contains("may modify data"));
	}

	@Test
	public void describeProblem_shouldFlagCallsThatMayModifyData() {
		String problem = CheckConceptReferenceRangeCriteriaChangeSet.describeProblem("$patient.setDead(true) || true");
		assertTrue(problem.contains("may modify data (setDead(true))"));
	}

	@Test
	public void describeProblem_shouldFlagAssignmentsAsPossiblyModifyingData() {
		String problem = CheckConceptReferenceRangeCriteriaChangeSet
		        .describeProblem("$encounter.creator.userProperties['x'] = 'y'");
		assertTrue(problem.contains("may modify data"));
	}

	@Test
	public void findWarnings_shouldReportOnlyRangesWithProblems() throws Exception {
		setCriteria("$encounter.creator.getRoles().clear() == null");

		List<String> warnings = new CheckConceptReferenceRangeCriteriaChangeSet()
		        .findWarnings(new JdbcConnection(getConnection()));

		assertEquals(1, warnings.size());
		assertTrue(warnings.get(0).startsWith("Concept reference range 32 (uuid " + RANGE_UUID + ") for concept 4089 "));
	}

	@Test
	public void execute_shouldCompleteAgainstTheDatabase() throws Exception {
		setCriteria("$patient.getGender().equals('M')");
		H2Database database = new H2Database();
		database.setConnection(new JdbcConnection(getConnection()));

		new CheckConceptReferenceRangeCriteriaChangeSet().execute(database);
	}

	private void setCriteria(String criteria) {
		Context.getAdministrationService().executeSQL("UPDATE concept_reference_range SET criteria = '"
		        + criteria.replace("'", "''") + "' WHERE uuid = '" + RANGE_UUID + "'",
		    false);
	}
}
