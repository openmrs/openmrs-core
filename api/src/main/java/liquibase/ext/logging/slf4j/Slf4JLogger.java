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

import java.util.logging.Level;

import liquibase.logging.LogMessageFilter;
import liquibase.logging.core.AbstractLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple layer to ensure liquibase logs get written to our standard loggers
 * 
 * @since 2.5.1, 2.6.0
 */
public class Slf4JLogger extends AbstractLogger {
	
	private final Logger logger;
	
	public Slf4JLogger(Class<?> clazz, LogMessageFilter filter) {
		super(filter);
		logger = LoggerFactory.getLogger(clazz);
	}
	
	@Override
	public void log(Level level, String message, Throwable e) {
		// NB java.util.logging supports a couple of levels not replicable through SLF4J
		// These messages are attempted to be routed to their closest level
		
		if (level == Level.SEVERE) {
			logger.error(message, e);
		} else if (level == Level.WARNING) {
			logger.warn(message, e);
		} else if (level == Level.INFO) {
			logger.info(message, e);
		} else if (level == Level.CONFIG) {
			logger.debug(message, e);
		} else if (level == Level.FINE) {
			logger.debug(message, e);
		} else if (level == Level.FINER) {
			logger.trace(message, e);
		} else {
			logger.trace(message, e);
		}
	}
}
