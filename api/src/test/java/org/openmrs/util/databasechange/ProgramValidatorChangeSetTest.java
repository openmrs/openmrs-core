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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProgramValidatorChangeSetTest extends BaseContextSensitiveTest {

	@Test
	void buildWarnings_shouldReportEachFindingAsASeparatePlainTextWarning() {
		Context.getAdministrationService().executeSQL(
		    "UPDATE program_workflow_state SET initial = true, terminal = true WHERE program_workflow_state_id = 1", false);

		List<String> warnings = new ProgramValidatorChangeSet().buildWarnings(getConnection());

		assertTrue(warnings.size() >= 3);
		assertTrue(warnings.stream().anyMatch(w -> w.contains("is configured as both initial and terminal")));
		assertFalse(warnings.stream().anyMatch(w -> w.contains("<")));
	}
}
