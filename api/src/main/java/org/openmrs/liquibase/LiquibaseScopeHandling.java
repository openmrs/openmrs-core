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

import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;

import liquibase.Scope;
import liquibase.ui.LoggerUIService;

public class LiquibaseScopeHandling {
	
	public static String enterLiquibaseUILoggingService() throws Exception {
		// Temp workaround to silence liquibase writing to stdout - https://github.com/liquibase/liquibase/issues/2396,
		// https://github.com/liquibase/liquibase/issues/3651
		LoggerUIService loggerUIService = new LoggerUIService();
		loggerUIService.setStandardLogLevel(Level.INFO);
		Map<String, Object> m = Collections.singletonMap(Scope.Attr.ui.name(), loggerUIService);
		return Scope.enter(m);
	}
	
	public static void exitLiquibaseScope(String scopeId) throws Exception {
		Scope.exit(scopeId);
	}
}
