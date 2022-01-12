/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package liquibase.ext.logging.slf4j;

import java.util.HashMap;
import java.util.Map;

import liquibase.logging.Logger;
import liquibase.logging.core.AbstractLogService;

/**
 * An implementation of {@link liquibase.logging.LogService} to use SLF4J for Liquibase logging
 * 
 * @since 2.5.1, 2.6.0
 */
public class Slf4JLogService extends AbstractLogService {
	
	private final Map<Class<?>, Slf4JLogger> loggers = new HashMap<>();
	
	@Override
	public int getPriority() {
		return PRIORITY_SPECIALIZED;
	}
	
	@Override
	public Logger getLog(Class clazz) {
		return loggers.computeIfAbsent(clazz, c -> new Slf4JLogger(c, getFilter()));
	}
}
