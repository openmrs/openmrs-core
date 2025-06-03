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

import javax.validation.constraints.NotNull;
import java.io.File;
import java.net.URI;
import java.util.Locale;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.AbstractConfiguration;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.Order;
import org.apache.logging.log4j.core.config.json.JsonConfiguration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.xml.XmlConfiguration;
import org.apache.logging.log4j.core.config.yaml.YamlConfiguration;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ServiceNotFoundException;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.slf4j.LoggerFactory;

/**
 * {@link ConfigurationFactory} to handle OpenMRS's logging configuration.
 * <p/>
 * Functionality provided by this {@link ConfigurationFactory}:
 * <ul>
 *     <li>Load log4j2 configuration files from the OpenMRS application directory</li>
 *     <li>Ensures that the configuration includes the MEMORY_APPENDER to keep log files in memory</li>
 *     <li>Allows the <tt>log.level</tt> setting to override logger settings</li>
 * </ul>
 */
@Plugin(name = "OpenmrsConfigurationFactory", category = ConfigurationFactory.CATEGORY)
@Order(10)
@SuppressWarnings("unused")
public class OpenmrsConfigurationFactory extends ConfigurationFactory {
	
	private static final org.slf4j.Logger log = LoggerFactory.getLogger(OpenmrsConfigurationFactory.class);
	
	public static final String[] SUFFIXES = new String[] { ".xml", ".yml", ".yaml", ".json", "*" };
	
	@Override
	public Configuration getConfiguration(LoggerContext loggerContext, String name, URI configLocation) {
		if (!isActive()) {
			return null;
		}
		
		if (configLocation == null) {
			// Try to load the configuration from the application directory
			for (File applicationDirectory : new File[] {
				OpenmrsUtil.getDirectoryInApplicationDataDirectory("configuration"),
				OpenmrsUtil.getApplicationDataDirectoryAsFile()
 			}) {
				for (String suffix : getSupportedTypes()) {
					if (suffix.equals("*")) {
						continue;
					}
					
					File configFile = new File(applicationDirectory, getDefaultPrefix() + suffix);
					if (configFile.exists() && configFile.canRead()) {
						return super.getConfiguration(loggerContext, name, configFile.toURI());
					}
				}
			}
		}

		return super.getConfiguration(loggerContext, name, configLocation);
	}

	@Override
	public Configuration getConfiguration(LoggerContext loggerContext, ConfigurationSource source) {
		if (source != null && source.getFile() != null) {
			switch (FilenameUtils.getExtension(source.getFile().getName()).toLowerCase(Locale.ROOT)) {
				case "xml":
					return new OpenmrsXmlConfiguration(loggerContext, source);
				case "yaml":
				case "yml":
					return new OpenmrsYamlConfiguration(loggerContext, source);
				case "json":
					return new OpenmrsJsonConfiguration(loggerContext, source);
				default:
					throw new IllegalArgumentException(
						OpenmrsConfigurationFactory.class.getName() + " does not know how to handle source " + source.getFile());
			}
		}
		return null;
	}
	
	@Override
	protected String[] getSupportedTypes() {
		return SUFFIXES;
	}
	
	protected static void doOpenmrsCustomisations(AbstractConfiguration configuration) {
		// if we don't have an in-memory appender, add it
		MemoryAppender memoryAppender = configuration.getAppender(OpenmrsConstants.MEMORY_APPENDER_NAME);
		if (memoryAppender == null) {
			memoryAppender = MemoryAppender.newBuilder().build();
			memoryAppender.start();
			
			configuration.addAppender(memoryAppender);
		}
		
		LoggerConfig rootLogger = configuration.getRootLogger();
		if (rootLogger.getAppenders().get(OpenmrsConstants.MEMORY_APPENDER_NAME) == null) {
			rootLogger.addAppender(memoryAppender, null, memoryAppender.getFilter());
		}
		
		try {
			AdministrationService adminService = Context.getAdministrationService();
			applyLogLevels(configuration, adminService);
		} catch (ServiceNotFoundException e) {
			// if AdministrativeService is not available, we'll assume we're starting up and everything is ok
			if (!e.getServiceClass().isAssignableFrom(AdministrationService.class)) {
				throw e;
			}
		}
	}
	
	private static void applyLogLevels(AbstractConfiguration configuration, AdministrationService adminService) {
		String logLevel = adminService.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL, "");
		
		for (String level : logLevel.split(",")) {
			String[] classAndLevel = level.split(":");
			if (classAndLevel.length == 0) {
				break;
			} else if (classAndLevel.length == 1) {
				applyLogLevel(configuration, OpenmrsConstants.LOG_CLASS_DEFAULT, classAndLevel[0].trim());
			} else {
				applyLogLevel(configuration, classAndLevel[0].trim(), classAndLevel[1].trim());
			}
		}
	}
	
	private static void applyLogLevel(AbstractConfiguration configuration, @NotNull String loggerName, String loggerLevel) {
		if (StringUtils.isBlank(loggerLevel)) {
			return;
		}
		
		if (loggerName == null) {
			loggerName = OpenmrsConstants.LOG_CLASS_DEFAULT;
		}
		
		LoggerConfig loggerConfig = configuration.getLogger(loggerName);
		if (loggerConfig != null) {
			switch (loggerLevel.toLowerCase(Locale.ROOT)) {
				case OpenmrsConstants.LOG_LEVEL_TRACE:
					loggerConfig.setLevel(Level.TRACE);
					break;
				case OpenmrsConstants.LOG_LEVEL_DEBUG:
					loggerConfig.setLevel(Level.DEBUG);
					break;
				case OpenmrsConstants.LOG_LEVEL_INFO:
					loggerConfig.setLevel(Level.INFO);
					break;
				case OpenmrsConstants.LOG_LEVEL_WARN:
					loggerConfig.setLevel(Level.WARN);
					break;
				case OpenmrsConstants.LOG_LEVEL_ERROR:
					loggerConfig.setLevel(Level.ERROR);
					break;
				case OpenmrsConstants.LOG_LEVEL_FATAL:
					loggerConfig.setLevel(Level.FATAL);
					break;
				default:
					log.warn("Log level {} is invalid. " +
						"Valid values are trace, debug, info, warn, error or fatal", loggerLevel);
					break;
			}
		}
	}
	
	private static class OpenmrsXmlConfiguration extends XmlConfiguration {
		
		public OpenmrsXmlConfiguration(LoggerContext loggerContext, ConfigurationSource configSource) {
			super(loggerContext, configSource);
		}
		
		@Override
		protected void doConfigure() {
			super.doConfigure();
			doOpenmrsCustomisations(this);
		}
	}
	
	private static class OpenmrsYamlConfiguration extends YamlConfiguration {
		
		public OpenmrsYamlConfiguration(LoggerContext loggerContext, ConfigurationSource configSource) {
			super(loggerContext, configSource);
		}
		
		@Override
		protected void doConfigure() {
			super.doConfigure();
			doOpenmrsCustomisations(this);
		}
	}
	
	private static class OpenmrsJsonConfiguration extends JsonConfiguration {
		
		public OpenmrsJsonConfiguration(LoggerContext loggerContext, ConfigurationSource configSource) {
			super(loggerContext, configSource);
		}
		
		@Override
		protected void doConfigure() {
			super.doConfigure();
			doOpenmrsCustomisations(this);
		}
	}
}
