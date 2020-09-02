/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.filter.update;

import java.util.List;
import org.openmrs.liquibase.LiquibaseProvider;
import org.openmrs.util.DatabaseUpdater;

public class DatabaseUpdaterWrapper {
	public List<DatabaseUpdater.OpenMRSChangeSet> getUnrunDatabaseChanges( LiquibaseProvider liquibaseProvider) throws Exception {
		return DatabaseUpdater.getUnrunDatabaseChanges( liquibaseProvider );
	}
	
	public boolean isLocked() {
		return DatabaseUpdater.isLocked();
	}

	public boolean updatesRequired() throws Exception {
		return DatabaseUpdater.updatesRequired();
	}
}
