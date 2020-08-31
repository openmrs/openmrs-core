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

import liquibase.Liquibase;

/**
 * Provides access to Liquibase instances. The interface was introduced for two reasons:
 * <li>to decouple the org.openmrs.liquibase.ChangeLogDetective class from the
 * org.openmrs.util.DatabaseUpdater class
 * <li>to support integration testing of the org.openmrs.liquibase.ChangeLogDetective class with
 * the H2 database.
 *
 * @since 2.4
 */
public interface LiquibaseProvider {
	
	/**
	 * Returns a Liquibase instance
	 * 
	 * @param changeLogFile name of a Liquibase changelog file
	 * @return a Liquibase instance
	 */
	Liquibase getLiquibase(String changeLogFile) throws Exception;
}
