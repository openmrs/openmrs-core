/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.logging;

import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractFileAppender;
import org.apache.logging.log4j.core.appender.AbstractOutputStreamAppender;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.appender.MemoryMappedFileAppender;
import org.apache.logging.log4j.core.appender.RandomAccessFileAppender;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.RollingRandomAccessFileAppender;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.openmrs.annotation.Logging;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.slf4j.LoggerFactory;

/**
 * Utility methods related to logging.
 * In general, module-level code should likely only call {@link #getMemoryAppender()} and
 * {@link #getOpenmrsLogLocation()}, however, the methods to update loggers are also exposed if necessary.
 *
 * @since 2.4.4, 2.5.1, 2.6.0
 */
public final class OpenmrsLoggingUtil {
	
	private static final org.slf4j.Logger log = LoggerFactory.getLogger(OpenmrsLoggingUtil.class);
	
	private OpenmrsLoggingUtil() {
	}
	
	/**
	 * Gets the in-memory log appender. This method needed to be added as it is much more difficult to
	 * get a specific appender in the Log4J2 architecture. This method is called in places where we need
	 * to display logging message.
	 *
	 * @since 2.4.4, 2.5.1, 2.6.0
	 */
	@Logging(ignore = true)
	public static MemoryAppender getMemoryAppender() {
		MemoryAppender memoryAppender = ((LoggerContext) LogManager.getContext(true)).getConfiguration()
			.getAppender(OpenmrsConstants.MEMORY_APPENDER_NAME);
		
		if (memoryAppender != null && !memoryAppender.isStarted()) {
			memoryAppender.start();
		}
		
		return memoryAppender;
	}
	
	/**
	 * Returns the location of the OpenMRS log file.
	 * <p/>
	 * <strong>Warning:</strong> the result of this call can return null if either the file appender uses a name other than
	 * {@link OpenmrsConstants#LOG_OPENMRS_FILE_APPENDER} or if the appender with that name is not one of the default log4j2
	 * file appending types.
	 *
	 * @return the path to the OpenMRS log file
	 * */
	public static String getOpenmrsLogLocation() {
		Appender fileAppender = ((LoggerContext) LogManager.getRootLogger()).getConfiguration()
			.getAppender(OpenmrsConstants.LOG_OPENMRS_FILE_APPENDER);
		
		String fileName = null;
		if (fileAppender instanceof AbstractOutputStreamAppender) {
			if (fileAppender instanceof RollingFileAppender) {
				fileName = ((RollingFileAppender) fileAppender).getFileName();
			} else if (fileAppender instanceof FileAppender) {
				fileName = ((FileAppender) fileAppender).getFileName();
			} else if (fileAppender instanceof MemoryMappedFileAppender) {
				fileName = ((MemoryMappedFileAppender) fileAppender).getFileName();
			} else if (fileAppender instanceof RollingRandomAccessFileAppender) {
				fileName = ((RollingRandomAccessFileAppender) fileAppender).getFileName();
			} else if (fileAppender instanceof RandomAccessFileAppender) {
				fileName = ((RandomAccessFileAppender) fileAppender).getFileName();
			} else if (fileAppender instanceof AbstractFileAppender) {
				fileName = ((AbstractFileAppender<?>) fileAppender).getFileName();
			} else {
				return null;
			}
		}
		
		return fileName == null ? null : Paths.get("", fileName).toAbsolutePath().toString();
	}
	
	/**
	 * Sets the org.openmrs Log4J logger's level if global property log.level.openmrs (
	 * OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL ) exists. Valid values for global property are
	 * trace, debug, info, warn, error or fatal.
	 */
	@Logging(ignore = true)
	public static void applyLogLevels() {
		String logLevel = Context.getAdministrationService()
			.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL, "");
		
		synchronized (OpenmrsLoggingUtil.class) {
			for (String level : logLevel.split(",")) {
				String[] classAndLevel = level.split(":");
				if (classAndLevel.length == 0) {
					break;
				} else if (classAndLevel.length == 1) {
					applyLogLevelInternal(OpenmrsConstants.LOG_CLASS_DEFAULT, classAndLevel[0].trim());
				} else {
					applyLogLevelInternal(classAndLevel[0].trim(), classAndLevel[1].trim());
				}
			}
			
			// DO NOT USE LogManager#getContext() here as this might reset the logger context
			((Logger) LogManager.getRootLogger()).getContext().updateLoggers();
		}
	}
	
	/**
	 * Set the log4j log level for class <code>logClass</code> to <code>logLevel</code>.
	 *
	 * @param logClass optional string giving the class level to change. Defaults to
	 *                 {@link OpenmrsConstants#LOG_CLASS_DEFAULT} . Should be something like org.openmrs.___
	 * @param logLevel one of <tt>OpenmrsConstants.LOG_LEVEL_*</tt> constants
	 */
	public static void applyLogLevel(String logClass, String logLevel) {
		if (StringUtils.isNotBlank(logLevel)) {
			synchronized (OpenmrsLoggingUtil.class) {
				applyLogLevelInternal(logClass, logLevel);
				// DO NOT USE LogManager#getContext() here as this might reset the logger context
				((Logger) LogManager.getRootLogger()).getContext().updateLoggers();
			}
		}
	}
	
	/**
	 * This method is the implementation of applying a level to a logger. It is intended to be called in an
	 * already synchronized context. Note these changes will only be applied once a call to
	 * {@link LoggerContext#updateLoggers()} is made.
	 *
	 * @param logClass optional string giving the class level to change. Defaults to
	 *                 *            OpenmrsConstants.LOG_CLASS_DEFAULT . Should be something like org.openmrs.___
	 * @param logLevel one of OpenmrsConstants.LOG_LEVEL_* constants
	 */
	private static void applyLogLevelInternal(String logClass, String logLevel) {
		if (StringUtils.isNotBlank(logLevel)) {
			// the default log class is org.openmrs.api
			if (StringUtils.isEmpty(logClass)) {
				logClass = OpenmrsConstants.LOG_CLASS_DEFAULT;
			}
			
			// DO NOT USE LogManager#getContext() here as this will reset the logger context
			LoggerContext context = ((Logger) LogManager.getRootLogger()).getContext();
			LoggerConfig configuration = context.getConfiguration().getLoggerConfig(logClass);
			
			logLevel = logLevel.toLowerCase();
			switch (logLevel) {
				case OpenmrsConstants.LOG_LEVEL_TRACE:
					configuration.setLevel(Level.TRACE);
					break;
				case OpenmrsConstants.LOG_LEVEL_DEBUG:
					configuration.setLevel(Level.DEBUG);
					break;
				case OpenmrsConstants.LOG_LEVEL_INFO:
					configuration.setLevel(Level.INFO);
					break;
				case OpenmrsConstants.LOG_LEVEL_WARN:
					configuration.setLevel(Level.WARN);
					break;
				case OpenmrsConstants.LOG_LEVEL_ERROR:
					configuration.setLevel(Level.ERROR);
					break;
				case OpenmrsConstants.LOG_LEVEL_FATAL:
					configuration.setLevel(Level.FATAL);
					break;
				default:
					log.warn("Log level {} is invalid. " +
						"Valid values are trace, debug, info, warn, error or fatal", logLevel);
					break;
			}
		}
	}
	
	/**
	 * Reloads the logging configuration
	 */
	public static void reloadLoggingConfiguration() {
		// Works, but it might be necessary to verify this in the future
		((LoggerContext) LogManager.getContext(true)).reconfigure();
	}
	
}
