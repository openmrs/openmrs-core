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

import java.util.Arrays;
import java.util.List;

/**
 * Defines which Liquibase snapshot and update change logs are available at all. The information
 * provided by this class needs to be updated when new Liquibase snapshots and updates are added to
 * openmrs-api resources.
 *
 * @since 2.4
 */
public class ChangeLogVersions {
	
	/**
	 * This definition of Liquibase snapshot versions needs to be kept in sync with the actual change
	 * log files in openmrs-core/api/src/main/resources/liquibase/snapshots/core-data and
	 * openmrs-core/api/src/main/resources/liquibase/snapshots/schema-only. If the actual change log
	 * files and this list get out of sync, org.openmrs.liquibase.ChangeLogVersionsTest fails.
	 */
	private static final List<String> SNAPSHOT_VERSIONS = Arrays.asList("1.9.x", "2.1.x", "2.2.x", "2.3.x", "2.4.x");
	
	/**
	 * This definition of Liquibase update versions needs to be kept in sync with the actual change log
	 * files in openmrs-core/api/src/main/resources/liquibase/updates. If the actual change log files
	 * and this list get out of sync, org.openmrs.liquibase.ChangeLogVersionsTest fails.
	 */
	private static final List<String> UPDATE_VERSIONS = Arrays.asList("1.9.x", "2.0.x", "2.1.x", "2.2.x", "2.3.x", "2.4.x", "2.5.x");
	
	public List<String> getSnapshotVersions() {
		return SNAPSHOT_VERSIONS;
	}
	
	public List<String> getUpdateVersions() {
		return UPDATE_VERSIONS;
	}
}
