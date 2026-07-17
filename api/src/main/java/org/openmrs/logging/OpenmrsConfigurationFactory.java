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

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import jakarta.validation.constraints.NotNull;

import org.apache.commons.io.FileUtils;
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
import org.apache.logging.log4j.core.config.composite.CompositeConfiguration;
import org.apache.logging.log4j.core.config.json.JsonConfiguration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.xml.XmlConfiguration;
import org.apache.logging.log4j.core.config.yaml.YamlConfiguration;
import org.apache.logging.log4j.status.StatusLogger;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ServiceNotFoundException;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ServiceContext;
import org.openmrs.util.ConfigUtil;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.util.PrivilegeConstants;
import org.slf4j.LoggerFactory;

import static org.openmrs.logging.OpenmrsLoggingUtil.stringToLevel;

/**
 * {@link ConfigurationFactory} to handle OpenMRS's logging configuration.
 * <p/>
 * Functionality provided by this {@link ConfigurationFactory}:
 * <ul>
 * <li>Load log4j2 configuration files from the OpenMRS application directory</li>
 * <li>Ensures that the configuration includes the MEMORY_APPENDER to keep log files in memory</li>
 * <li>Allows the <tt>log.level</tt> setting to override logger settings</li>
 * </ul>
 */
@Plugin(name = "OpenmrsConfigurationFactory", category = ConfigurationFactory.CATEGORY)
@Order(10)
@SuppressWarnings("unused")
public class OpenmrsConfigurationFactory extends ConfigurationFactory {

	private static final org.slf4j.Logger log = LoggerFactory.getLogger(OpenmrsConfigurationFactory.class);

	public static final String[] SUFFIXES = new String[] { ".xml", ".yml", ".yaml", ".json", "*" };

	// Extensions are all SUFFIXES except wildcard, with leading "." removed
	public static final String[] EXTENSIONS = Arrays.stream(SUFFIXES).filter(s -> !s.equals("*")).map(s -> s.substring(1))
	        .toArray(String[]::new);

	@Override
	public Configuration getConfiguration(LoggerContext loggerContext, String name, URI configLocation) {
		if (!isActive()) {
			return null;
		}

		// try to load the configuration from the application data directory
		if (configLocation == null) {
			List<File> configurationFiles = getConfigurationFiles();
			if (!configurationFiles.isEmpty()) {
				if (configurationFiles.size() == 1) {
					StatusLogger.getLogger().info("Adding log4j2 configuration file: {}",
					    configurationFiles.get(0).getPath());
					return super.getConfiguration(loggerContext, name, configurationFiles.get(0).toURI());
				} else {
					List<AbstractConfiguration> abstractConfigurations = new ArrayList<>();
					for (File configFile : configurationFiles) {
						Configuration configuration = super.getConfiguration(loggerContext, name, configFile.toURI());
						if (configuration instanceof AbstractConfiguration) {
							StatusLogger.getLogger().info("Adding log4j2 configuration file: {}", configFile.getPath());
							abstractConfigurations.add((AbstractConfiguration) configuration);
						} else {
							StatusLogger.getLogger().error("Unable to add log4j2 configuration file: {}",
							    configFile.getPath());
						}
					}
					return new OpenmrsCompositeConfiguration(abstractConfigurations);
				}
			}
		}

		return super.getConfiguration(loggerContext, name, configLocation);
	}

	@Override
	public Configuration getConfiguration(LoggerContext loggerContext, ConfigurationSource source) {
		if (source != null && source.getLocation() != null) {
			switch (FilenameUtils.getExtension(source.getLocation()).toLowerCase(Locale.ROOT)) {
				case "xml":
					return new OpenmrsXmlConfiguration(loggerContext, source);
				case "yaml":
				case "yml":
					return new OpenmrsYamlConfiguration(loggerContext, source);
				case "json":
					return new OpenmrsJsonConfiguration(loggerContext, source);
				default:
					throw new IllegalArgumentException(OpenmrsConfigurationFactory.class.getName()
					        + " does not know how to handle source " + source.getFile());
			}
		}
		return null;
	}

	@Override
	protected String[] getSupportedTypes() {
		return SUFFIXES;
	}

	public List<File> getConfigurationFiles() {
		List<File> configurationFiles = new ArrayList<>();
		for (File configDir : new File[] { OpenmrsUtil.getDirectoryInApplicationDataDirectory("configuration"),
		        OpenmrsUtil.getApplicationDataDirectoryAsFile() }) {
			for (File configFile : FileUtils.listFiles(configDir, EXTENSIONS, false)) {
				if (configFile.getName().startsWith(getDefaultPrefix()) && configFile.canRead()) {
					configurationFiles.add(configFile);
				}
			}
		}
		configurationFiles.sort(Comparator.comparing(File::getName));
		return configurationFiles;
	}

	protected static void doOpenmrsCustomisations(AbstractConfiguration configuration) {
		// if we don't have an in-memory appender, add it
		MemoryAppender memoryAppender = configuration.getAppender(OpenmrsConstants.MEMORY_APPENDER_NAME);
		if (memoryAppender == null) {
			memoryAppender = MemoryAppender.newBuilder().build();
			configuration.addAppender(memoryAppender);
		}

		LoggerConfig rootLogger = configuration.getRootLogger();
		if (rootLogger.getAppenders().get(OpenmrsConstants.MEMORY_APPENDER_NAME) == null) {
			rootLogger.addAppender(memoryAppender, null, memoryAppender.getFilter());
		}

		// Check system and runtime properties first — these do not require a session
		String logLevel = ConfigUtil.getSystemProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL);
		if (logLevel == null) {
			logLevel = ConfigUtil.getRuntimeProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL);
		}

		if (logLevel != null) {
			applyLogLevels(configuration, logLevel);
		} else if (Context.isSessionOpen() && ServiceContext.isInstantiated()) {
			try {
				applyLogLevels(configuration);
			} catch (ServiceNotFoundException e) {
				// if AdministrationService is not available, we'll assume we're starting up and everything is ok
				if (!AdministrationService.class.isAssignableFrom(e.getServiceClass())) {
					throw e;
				} else {
					StatusLogger.getLogger()
					        .debug("AdministrationService is not yet available; skipping log-level overrides");
				}
			}
		} else if (Context.isSessionOpen()) {
			StatusLogger.getLogger()
			        .debug("ServiceContext is not yet instantiated; deferring AdministrationService log-level overrides");
		}
	}

	private static void applyLogLevels(AbstractConfiguration configuration) {
		String logLevel;
		Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
		try {
			logLevel = ConfigUtil.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL);
		} finally {
			Context.removeProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
		}

		if (logLevel != null) {
			applyLogLevels(configuration, logLevel);
		}
	}

	private static void applyLogLevels(AbstractConfiguration configuration, String logLevel) {
		for (String level : logLevel.split(",")) {
			String[] classAndLevel = level.split(":", 3);
			if (classAndLevel.length == 0) {
				break;
			} else if (classAndLevel.length == 1) {
				applyLogLevel(configuration, OpenmrsConstants.LOG_CLASS_DEFAULT, classAndLevel[0].trim());
			} else {
				if (classAndLevel.length > 2) {
					StatusLogger.getLogger().warn(
					    "Could not properly parse \"{}\" into a class and level due to too many colons. Expected format is <class>:<level>, e.g., org.openmrs.api:INFO",
					    level);
				}
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

		LoggerConfig loggerConfig = configuration.getLoggerConfig(loggerName);
		Level level = stringToLevel(loggerLevel, loggerName);

		if (loggerConfig == null || !loggerConfig.getName().equals(loggerName)) {
			loggerConfig = new LoggerConfig(loggerName, level, true);
			configuration.addLogger(loggerName, loggerConfig);
		} else {
			loggerConfig.setLevel(level);
		}
	}

	private static class OpenmrsCompositeConfiguration extends CompositeConfiguration {

		public OpenmrsCompositeConfiguration(final List<? extends AbstractConfiguration> configurations) {
			super(configurations);
		}

		@Override
		protected void doConfigure() {
			super.doConfigure();
			doOpenmrsCustomisations(this);
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
