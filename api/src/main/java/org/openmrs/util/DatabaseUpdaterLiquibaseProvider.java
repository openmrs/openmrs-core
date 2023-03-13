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

import liquibase.Liquibase;
import org.openmrs.liquibase.LiquibaseProvider;

/**
 * Provides a wrapper for org.openmrs.util.DatabaseUpdater#getLiquibase(String) that can be injected
 * into helper methods provided by the org.openmrs,liquibase.ChangeLogDetective class.
 *
 * @since 2.4
 */
public class DatabaseUpdaterLiquibaseProvider implements LiquibaseProvider {
	
	@Override
	public Liquibase getLiquibase(String changeLogFile) throws Exception {
		return DatabaseUpdater.getLiquibase(changeLogFile);
	}
}
