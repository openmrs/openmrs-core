/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.filter.startuperror;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openmrs.web.filter.StartupFilter;
import org.openmrs.web.filter.update.UpdateFilter;

/**
 * The {@link UpdateFilter} uses this model object to hold all properties that are edited by the
 * user in the wizard. All attributes on this model object are added to all templates rendered by
 * the {@link StartupFilter}.
 */
public class StartupErrorFilterModel {
	
	public Throwable errorAtStartup;
	
	public String stacktrace;
	
	/**
	 * Default constructor that sets up some of the properties
	 */
	public StartupErrorFilterModel(Throwable t) {
		errorAtStartup = t;
		stacktrace = ExceptionUtils.getStackTrace(t);
	}
	
}
