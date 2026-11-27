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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.junit.jupiter.api.Test;
import org.openmrs.util.DatabaseIT;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the Liquibase changeset for TRUNK-6879 that restores default empty string value for
 * concept_proposal.locale.
 */
public class ConceptProposalChangeSetsDatabaseIT extends DatabaseIT {

	private static final String SNAPSHOT_VERSION = "2.9.x";

	private static final String UPDATE_TO_LATEST_3_0_X = "org/openmrs/liquibase/updates/liquibase-update-to-latest-3.0.x.xml";

	@Test
	public void shouldAllowInsertingConceptProposalWithoutLocaleOnAFreshInstall() throws Exception {
		updateDatabase(new ChangeLogVersionFinder().getSnapshotCombinations().get(SNAPSHOT_VERSION));
		updateDatabase(UPDATE_TO_LATEST_3_0_X);

		try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
			stmt.executeUpdate("INSERT INTO concept_proposal (original_text, state, creator, date_created, uuid) "
			        + "VALUES ('a proposed concept', 'UNMAPPED', 1, CURRENT_TIMESTAMP, 'b5a03e1e-1234-4567-8901-123456789abc')");

			try (ResultSet rs = stmt
			        .executeQuery("SELECT locale FROM concept_proposal WHERE original_text = 'a proposed concept'")) {
				assertTrue(rs.next());
				assertEquals("", rs.getString("locale"));
			}
		}
	}
}
