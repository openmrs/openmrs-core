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

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.AbstractConfiguration;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.composite.CompositeConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.api.context.ServiceContext;
import org.openmrs.util.ConfigUtil;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.util.PrivilegeConstants;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;

/**
 * Tests for {@link OpenmrsConfigurationFactory}.
 * <p/>
 * These tests focus on configuration file discovery and the static customisation logic. Full
 * integration with Log4J2 context lifecycle is covered by the XML/YAML/JSON config subclasses
 * implicitly through the build's plugin processor.
 * <p/>
 * Rather than mocking statics (which requires Mockito's inline mock maker), file discovery is driven by
 * pointing {@link OpenmrsUtil}'s application data directory at a {@link TempDir}, and the customisation
 * precedence logic is driven by the real configuration sources (system properties, runtime properties,
 * an open {@link UserContext}, and global values seeded into {@link ConfigUtil}'s cache).
 */
class OpenmrsConfigurationFactoryTest {

	@TempDir
	Path tempDir;

	private Properties originalRuntimeProperties;

	@BeforeEach
	void setUp() {
		// getConfigurationFiles() scans the "configuration" subdirectory of the application data
		// directory and the application data directory itself; pointing it at the temp dir isolates each
		// test from any real OpenMRS installation
		OpenmrsUtil.setApplicationDataDirectory(tempDir.toString());

		originalRuntimeProperties = Context.getRuntimeProperties();
		Context.setRuntimeProperties(new Properties());
		System.clearProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL);
	}

	@AfterEach
	void tearDown() {
		System.clearProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL);
		new ConfigUtil().globalPropertyDeleted(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL);
		Context.clearUserContext();
		Context.setRuntimeProperties(originalRuntimeProperties);
		OpenmrsUtil.setApplicationDataDirectory(null);
	}

	private static void openSession() {
		// the UserContext never authenticates here, so a no-op authentication scheme is sufficient
		Context.setUserContext(new UserContext(credentials -> null));
	}

	private static void seedGlobalLogLevel(String value) {
		new ConfigUtil().globalPropertyChanged(new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL, value));
	}

	private Path configurationDir() throws IOException {
		Path configDir = tempDir.resolve("configuration");
		Files.createDirectories(configDir);
		return configDir;
	}

	private static void write(Path file, String content) throws IOException {
		Files.write(file, content.getBytes(StandardCharsets.UTF_8));
	}

	@Test
	void getConfigurationFiles_shouldReturnEmptyListWhenNoConfigFilesExist() {
		OpenmrsConfigurationFactory factory = new OpenmrsConfigurationFactory();

		assertThat(factory.getConfigurationFiles(), empty());
	}

	@Test
	void getConfigurationFiles_shouldFindXmlConfigFile() throws IOException {
		write(configurationDir().resolve("log4j2.xml"), "<Configuration/>");

		OpenmrsConfigurationFactory factory = new OpenmrsConfigurationFactory();

		assertThat(factory.getConfigurationFiles(), hasSize(1));
	}

	@Test
	void getConfigurationFiles_shouldFindYamlAndJsonConfigFiles() throws IOException {
		Path configDir = configurationDir();
		write(configDir.resolve("log4j2.yaml"), "Configuration: {}");
		write(configDir.resolve("log4j2.json"), "{}");

		OpenmrsConfigurationFactory factory = new OpenmrsConfigurationFactory();

		assertThat(factory.getConfigurationFiles(), hasSize(2));
	}

	@Test
	void getConfigurationFiles_shouldIgnoreNonConfigFiles() throws IOException {
		Path configDir = configurationDir();
		write(configDir.resolve("log4j2.xml"), "<Configuration/>");
		write(configDir.resolve("other-config.properties"), "key=value");
		write(configDir.resolve("readme.txt"), "not a config");

		OpenmrsConfigurationFactory factory = new OpenmrsConfigurationFactory();

		assertThat(factory.getConfigurationFiles(), hasSize(1));
	}

	@Test
	void getConfigurationFiles_shouldFindConfigInApplicationDataDir() throws IOException {
		// place the config in the application data directory itself rather than the "configuration" subdir
		write(tempDir.resolve("log4j2.xml"), "<Configuration/>");

		OpenmrsConfigurationFactory factory = new OpenmrsConfigurationFactory();

		assertThat(factory.getConfigurationFiles(), hasSize(1));
	}

	@Test
	void getConfigurationFiles_shouldSortFilesByName() throws IOException {
		Path configDir = configurationDir();
		write(configDir.resolve("log4j2-b.xml"), "<Configuration/>");
		write(configDir.resolve("log4j2-a.xml"), "<Configuration/>");

		OpenmrsConfigurationFactory factory = new OpenmrsConfigurationFactory();

		assertThat("Should find both files, sorted by name", factory.getConfigurationFiles(), hasSize(2));
		assertThat("First file should be log4j2-a.xml",
		    factory.getConfigurationFiles().get(0).getName().endsWith("log4j2-a.xml"));
	}

	@Test
	void getConfigurationFiles_shouldIgnoreUnreadableFiles() throws IOException {
		Path unreadable = configurationDir().resolve("log4j2.xml");
		write(unreadable, "<Configuration/>");
		Assumptions.assumeTrue(unreadable.toFile().setReadable(false),
		    "Cannot make file unreadable on this platform; skipping test");

		try {
			OpenmrsConfigurationFactory factory = new OpenmrsConfigurationFactory();
			assertThat(factory.getConfigurationFiles(), empty());
		} finally {
			unreadable.toFile().setReadable(true);
		}
	}

	// Minimal but valid log4j2 XML used for the multi-file composite tests below
	private static final String MINIMAL_LOG4J2_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
	        + "<Configuration xmlns=\"http://logging.apache.org/log4j/2.0/config\">\n" + "  <Appenders>\n"
	        + "    <Console name=\"CONSOLE\" target=\"SYSTEM_OUT\">\n" + "      <PatternLayout pattern=\"%m%n\"/>\n"
	        + "    </Console>\n" + "  </Appenders>\n" + "  <Loggers>\n" + "    <Root level=\"OFF\">\n"
	        + "      <AppenderRef ref=\"CONSOLE\"/>\n" + "    </Root>\n" + "  </Loggers>\n" + "</Configuration>\n";

	/**
	 * Regression test for the composite-configuration handling fix. The old implementation returned a
	 * plain {@link CompositeConfiguration} which did not run {@code doOpenmrsCustomisations}, so the
	 * memory appender was silently missing when more than one log4j2 config file was present. The new
	 * implementation wraps the configurations in an internal subclass whose {@code doConfigure} runs
	 * the customisations.
	 */
	@Test
	void getConfiguration_shouldApplyCustomisationsWhenMultipleConfigFilesArePresent() throws IOException {
		Path configDir = configurationDir();
		write(configDir.resolve("log4j2-a.xml"), MINIMAL_LOG4J2_XML);
		write(configDir.resolve("log4j2-b.xml"), MINIMAL_LOG4J2_XML);

		OpenmrsConfigurationFactory factory = new OpenmrsConfigurationFactory();
		LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);

		Configuration config = factory.getConfiguration(loggerContext, "test-composite-customisations", (URI) null);

		assertThat("Multi-file path should produce a CompositeConfiguration", config,
		    instanceOf(CompositeConfiguration.class));
		config.initialize();
		config.start();
		try {
			assertThat("Memory appender should be added by doOpenmrsCustomisations",
			    config.getAppender(OpenmrsConstants.MEMORY_APPENDER_NAME), notNullValue());
		} finally {
			config.stop();
		}
	}

	/**
	 * A system property takes precedence over a conflicting runtime property. Setting both for the same
	 * logger and reading back the system-supplied level proves the precedence order.
	 */
	@Test
	void doOpenmrsCustomisations_shouldUseSystemPropertyForLogLevel() {
		AbstractConfiguration configuration = new DefaultConfiguration();
		String loggerName = "org.openmrs.logging.test.factory.sysprop";

		System.setProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL, loggerName + ":debug");
		Properties runtimeProperties = new Properties();
		runtimeProperties.setProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL, loggerName + ":error");
		Context.setRuntimeProperties(runtimeProperties);

		OpenmrsConfigurationFactory.doOpenmrsCustomisations(configuration);

		LoggerConfig loggerConfig = configuration.getLoggerConfig(loggerName);
		assertThat("Logger should have been configured from the system property", loggerConfig.getName(),
		    equalTo(loggerName));
		assertThat(loggerConfig.getLevel(), equalTo(Level.DEBUG));
	}

	/**
	 * With no system property, a runtime property wins over a conflicting session-scoped global property.
	 */
	@Test
	void doOpenmrsCustomisations_shouldFallBackToRuntimePropertyWhenSystemPropertyAbsent() {
		AbstractConfiguration configuration = new DefaultConfiguration();
		String loggerName = "org.openmrs.logging.test.factory.runtime";

		Properties runtimeProperties = new Properties();
		runtimeProperties.setProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL, loggerName + ":error");
		Context.setRuntimeProperties(runtimeProperties);

		openSession();
		seedGlobalLogLevel(loggerName + ":trace");

		OpenmrsConfigurationFactory.doOpenmrsCustomisations(configuration);

		LoggerConfig loggerConfig = configuration.getLoggerConfig(loggerName);
		assertThat(loggerConfig.getName(), equalTo(loggerName));
		assertThat(loggerConfig.getLevel(), equalTo(Level.ERROR));
	}

	/**
	 * When no session is open we must not consult the global property — this is the startup-safety
	 * guarantee the factory provides. A logger whose only configured source is the global property is
	 * therefore left unconfigured (it resolves to an ancestor/root logger config).
	 */
	@Test
	void doOpenmrsCustomisations_shouldSkipGlobalPropertyWhenSessionClosed() {
		AbstractConfiguration configuration = new DefaultConfiguration();
		String loggerName = "org.openmrs.logging.test.factory.sessionclosed";

		// session is closed by default; only the global property carries a value for this logger
		seedGlobalLogLevel(loggerName + ":debug");

		OpenmrsConfigurationFactory.doOpenmrsCustomisations(configuration);

		LoggerConfig loggerConfig = configuration.getLoggerConfig(loggerName);
		assertThat("No logger should have been configured from the global property when the session is closed",
		    loggerConfig.getName(), not(equalTo(loggerName)));
	}

	/**
	 * Regression test for TRUNK-6688. Reproduces the conditions of the re-entrant initialization path:
	 * Log4j2 configuration is triggered from within {@code ServiceContext}'s static initializer, so a
	 * session appears open but the {@code ServiceContext} singleton has not been created yet. In that
	 * state {@code doOpenmrsCustomisations} must not try to read global properties, which triggers a
	 * message logged using the {@code ServiceContext}'s logger.
	 */
	@Test
	void doOpenmrsCustomisations_shouldNotForceServiceContextInitializationWhenNotYetAvailable() {
		AbstractConfiguration configuration = new DefaultConfiguration();

		try (MockedStatic<Context> contextMock = mockStatic(Context.class);
		        MockedStatic<ServiceContext> serviceContextMock = mockStatic(ServiceContext.class);
		        MockedStatic<ConfigUtil> configUtilMock = mockStatic(ConfigUtil.class)) {
			contextMock.when(Context::isSessionOpen).thenReturn(true);
			serviceContextMock.when(ServiceContext::isInstantiated).thenReturn(false);
			configUtilMock.when(() -> ConfigUtil.getSystemProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL))
			        .thenReturn(null);
			configUtilMock.when(() -> ConfigUtil.getRuntimeProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL))
			        .thenReturn(null);

			OpenmrsConfigurationFactory.doOpenmrsCustomisations(configuration);

			// The heart of the fix: we must not force ServiceContext creation from the logging path
			configUtilMock.verify(() -> ConfigUtil.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL), never());
			contextMock.verify(Context::getAdministrationService, never());
			contextMock.verify(() -> Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES), never());

			// Customisations that do not depend on the context are still applied
			assertThat(configuration.getAppender(OpenmrsConstants.MEMORY_APPENDER_NAME), notNullValue());
		}
	}

	@Test
	void doOpenmrsCustomisations_shouldApplyGlobalPropertyOverridesWhenServiceContextAvailable() {
		AbstractConfiguration configuration = new DefaultConfiguration();
		String loggerName = "org.openmrs.logging.test.factory.global";

		try (MockedStatic<Context> contextMock = mockStatic(Context.class);
		        MockedStatic<ServiceContext> serviceContextMock = mockStatic(ServiceContext.class);
		        MockedStatic<ConfigUtil> configUtilMock = mockStatic(ConfigUtil.class)) {
			contextMock.when(Context::isSessionOpen).thenReturn(true);
			serviceContextMock.when(ServiceContext::isInstantiated).thenReturn(true);
			configUtilMock.when(() -> ConfigUtil.getSystemProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL))
			        .thenReturn(null);
			configUtilMock.when(() -> ConfigUtil.getRuntimeProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL))
			        .thenReturn(null);
			configUtilMock.when(() -> ConfigUtil.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL))
			        .thenReturn(loggerName + ":warn");

			OpenmrsConfigurationFactory.doOpenmrsCustomisations(configuration);

			configUtilMock.verify(() -> ConfigUtil.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL));
			LoggerConfig loggerConfig = configuration.getLoggerConfig(loggerName);
			assertThat(loggerConfig.getName(), equalTo(loggerName));
			assertThat(loggerConfig.getLevel(), equalTo(Level.WARN));
		}
	}

	@Test
	void doOpenmrsCustomisations_shouldAddMemoryAppenderWhenAbsent() {
		AbstractConfiguration configuration = new DefaultConfiguration();

		OpenmrsConfigurationFactory.doOpenmrsCustomisations(configuration);

		assertThat(configuration.getAppender(OpenmrsConstants.MEMORY_APPENDER_NAME), notNullValue());
		assertThat("Root logger should reference the memory appender",
		    configuration.getRootLogger().getAppenders().get(OpenmrsConstants.MEMORY_APPENDER_NAME), notNullValue());
	}

	@Test
	void extensions_shouldMatchSupportedSuffixesMinusWildcard() {
		String[] suffixes = OpenmrsConfigurationFactory.SUFFIXES;
		String[] extensions = OpenmrsConfigurationFactory.EXTENSIONS;

		assertThat("Extensions should be one fewer than suffixes (minus wildcard '*')", extensions.length,
		    org.hamcrest.Matchers.equalTo(suffixes.length - 1));

		for (String ext : extensions) {
			assertThat("Extension '" + ext + "' should not contain a dot", ext.indexOf('.') < 0);
		}
	}
}
