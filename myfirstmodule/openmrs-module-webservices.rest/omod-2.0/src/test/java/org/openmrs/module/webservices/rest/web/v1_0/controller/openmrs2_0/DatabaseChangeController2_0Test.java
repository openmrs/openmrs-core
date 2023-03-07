/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs2_0;

import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.openmrs.util.DatabaseUpdater;

public class DatabaseChangeController2_0Test extends MainResourceControllerTest {

	@Override
	public String getURI() {
		return "databasechange";
	}

	@Override
	public String getUuid() {
		try {
			return DatabaseUpdater.getDatabaseChanges().get(0).getId();
		}
		catch (Exception e) {
			return null;
		}
	}

	@Override
	public long getAllCount() {
		try {
			return Math.min(DatabaseUpdater.getDatabaseChanges().size(), 50); // page is max 50
		}
		catch (Exception e) {
			return 0;
		}
	}
}
