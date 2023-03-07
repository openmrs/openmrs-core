/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs2_0;

import org.mockito.Mockito;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;
import org.openmrs.util.DatabaseUpdater;

import java.util.Date;

import static liquibase.changelog.ChangeSet.RunStatus.ALREADY_RAN;

public class DatabaseChangeResource2_0Test extends BaseDelegatingResourceTest<DatabaseChangeResource2_0, DatabaseUpdater.OpenMRSChangeSet> {

	@Override
	public DatabaseUpdater.OpenMRSChangeSet newObject() {
		DatabaseUpdater.OpenMRSChangeSet changeSet = Mockito.mock(DatabaseUpdater.OpenMRSChangeSet.class, Mockito.CALLS_REAL_METHODS);
		changeSet.setId("id");
		changeSet.setAuthor("author");
		changeSet.setComments("comments");
		changeSet.setDescription("description");
		changeSet.setRanDate(new Date(1625683652));
		changeSet.setRunStatus(ALREADY_RAN);
		return changeSet;
	}

	@Override
	public String getDisplayProperty() {
		return "author description";
	}

	@Override
	public String getUuidProperty() {
		return "id";
	}
}
