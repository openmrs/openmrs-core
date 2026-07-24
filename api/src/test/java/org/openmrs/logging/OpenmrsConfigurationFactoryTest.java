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
import java.nio.file.Files;
import java.nio.file.Path;

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
import org.mockito.InOrder;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.ServiceNotFoundException;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ServiceContext;
import org.openmrs.util.ConfigUtil;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.util.PrivilegeConstants;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;

/**
 * Tests for {@link OpenmrsConfigurationFactory}.
 * <p/>
 * These tests focus on configuration file discovery and the static customisation logic. Full
 * integration with Log4J2 context lifecycle is covered by the XML/YAML/JSON config subclasses
 * implicitly through the build's plugin processor.
 */
class OpenmrsConfigurationFactoryTest {

	@TempDir
	Path tempDir;

	private MockedStatic<OpenmrsUtil> openmrsUtilMock;

	@BeforeEach
	void setUp() {
		openmrsUtilMock = mockStatic(OpenmrsUtil.class);
	}

	@AfterEach
	void tearDown() {
		openmrsUtilMock.close();
	}

	@Test
	void getConfigurationFiles_shouldReturnEmptyListWhenNoConfigFilesExist() {
		openmrsUtilMock.when(OpenmrsUtil::getApplicationDataDirectoryAsFile).thenReturn(tempDir.toFile());
		openmrsUtilMock.when(() -> OpenmrsUtil.getDirectoryInApplicationDataDirectory("configuration"))
		        .thenReturn(tempDir.resolve("configuration").toFile());

		OpenmrsConfigurationFactory factory = new OpenmrsConfigurationFactory();

		assertThat(factory.getConfigurationFiles(), empty());
	}

	@Test
	void getConfigurationFiles_shouldFindXmlConfigFile() throws IOException {
		Path configDir = tempDir.resolve("configuration");
		Files.createDirectories(configDir);
		Files.writeString(configDir.resolve("log4j2.xml"), "<Configuration/>");

		openmrsUtilMock.when(OpenmrsUtil::getApplicationDataDirectoryAsFile).thenReturn(tempDir.toFile());
		openmrsUtilMock.when(() -> OpenmrsUtil.getDirectoryInApplicationDataDirectory("configuration"))
		        .thenReturn(configDir.toFile());

		OpenmrsConfigurationFactory factory = new OpenmrsConfigurationFactory();

		assertThat(factory.getConfigurationFiles(), hasSize(1));
	}

	@Test
	void getConfigurationFiles_shouldFindYamlAndJsonConfigFiles() throws IOException {
		Path configDir = tempDir.resolve("configuration");
		Files.createDirectories(configDir);
		Files.writeString(configDir.resolve("log4j2.yaml"), "Configuration: {}");
		Files.writeString(configDir.resolve("log4j2.json"), "{}");

		openmrsUtilMock.when(OpenmrsUtil::getApplicationDataDirectoryAsFile).thenReturn(tempDir.toFile());
		openmrsUtilMock.when(() -> OpenmrsUtil.getDirectoryInApplicationDataDirectory("configuration"))
		        .thenReturn(configDir.toFile());

		OpenmrsConfigurationFactory factory = new OpenmrsConfigurationFactory();

		assertThat(factory.getConfigurationFiles(), hasSize(2));
	}

	@Test
	void getConfigurationFiles_shouldIgnoreNonConfigFiles() throws IOException {
		Path configDir = tempDir.resolve("configuration");
		Files.createDirectories(configDir);
		Files.writeString(configDir.resolve("log4j2.xml"), "<Configuration/>");
		Files.writeString(configDir.resolve("other-config.properties"), "key=value");
		Files.writeString(configDir.resolve("readme.txt"), "not a config");

		openmrsUtilMock.when(OpenmrsUtil::getApplicationDataDirectoryAsFile).thenReturn(tempDir.toFile());
		openmrsUtilMock.when(() -> OpenmrsUtil.getDirectoryInApplicationDataDirectory("configuration"))
		        .thenReturn(configDir.toFile());

		OpenmrsConfigurationFactory factory = new OpenmrsConfigurationFactory();

		assertThat(factory.getConfigurationFiles(), hasSize(1));
	}

	@Test
	void getConfigurationFiles_shouldFindConfigInApplicationDataDir() throws IOException {
		Files.writeString(tempDir.resolve("log4j2.xml"), "<Configuration/>");

		openmrsUtilMock.when(OpenmrsUtil::getApplicationDataDirectoryAsFile).thenReturn(tempDir.toFile());
		openmrsUtilMock.when(() -> OpenmrsUtil.getDirectoryInApplicationDataDirectory("configuration"))
		        .thenReturn(tempDir.resolve("nonexistent").toFile());

		OpenmrsConfigurationFactory factory = new OpenmrsConfigurationFactory();

		assertThat(factory.getConfigurationFiles(), hasSize(1));
	}

	@Test
	void getConfigurationFiles_shouldSortFilesByName() throws IOException {
		Path configDir = tempDir.resolve("configuration");
		Files.createDirectories(configDir);
		Files.writeString(configDir.resolve("log4j2-b.xml"), "<Configuration/>");
		Files.writeString(configDir.resolve("log4j2-a.xml"), "<Configuration/>");

		openmrsUtilMock.when(OpenmrsUtil::getApplicationDataDirectoryAsFile).thenReturn(tempDir.toFile());
		openmrsUtilMock.when(() -> OpenmrsUtil.getDirectoryInApplicationDataDirectory("configuration"))
		        .thenReturn(configDir.toFile());

		OpenmrsConfigurationFactory factory = new OpenmrsConfigurationFactory();

		assertThat("Should find both files, sorted by name", factory.getConfigurationFiles(), hasSize(2));
		assertThat("First file should be log4j2-a.xml",
		    factory.getConfigurationFiles().get(0).getName().endsWith("log4j2-a.xml"));
	}

	@Test
	void getConfigurationFiles_shouldIgnoreUnreadableFiles() throws IOException {
		Path configDir = tempDir.resolve("configuration");
		Files.createDirectories(configDir);
		Path unreadable = configDir.resolve("log4j2.xml");
		Files.writeString(unreadable, "<Configuration/>");
		Assumptions.assumeTrue(unreadable.toFile().setReadable(false),
		    "Cannot make file unreadable on this platform; skipping test");

		try {
			openmrsUtilMock.when(OpenmrsUtil::getApplicationDataDirectoryAsFile).thenReturn(tempDir.toFile());
			openmrsUtilMock.when(() -> OpenmrsUtil.getDirectoryInApplicationDataDirectory("configuration"))
			        .thenReturn(configDir.toFile());

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
		Path configDir = tempDir.resolve("configuration");
		Files.createDirectories(configDir);
		Files.writeString(configDir.resolve("log4j2-a.xml"), MINIMAL_LOG4J2_XML);
		Files.writeString(configDir.resolve("log4j2-b.xml"), MINIMAL_LOG4J2_XML);

		openmrsUtilMock.when(OpenmrsUtil::getApplicationDataDirectoryAsFile).thenReturn(tempDir.toFile());
		openmrsUtilMock.when(() -> OpenmrsUtil.getDirectoryInApplicationDataDirectory("configuration"))
		        .thenReturn(configDir.toFile());

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
	 * Verifies that {@code doOpenmrsCustomisations} consults the system property first and skips the
	 * runtime and global property paths when it returns a non-null value. This guards the precedence
	 * chain ({@code system → runtime → global}) introduced by this PR.
	 */
	@Test
	void doOpenmrsCustomisations_shouldUseSystemPropertyForLogLevel() {
		AbstractConfiguration configuration = new DefaultConfiguration();
		String loggerName = "org.openmrs.logging.test.factory.sysprop";

		try (MockedStatic<Context> contextMock = mockStatic(Context.class);
		        MockedStatic<ConfigUtil> configUtilMock = mockStatic(ConfigUtil.class)) {
			contextMock.when(Context::isSessionOpen).thenReturn(true);
			configUtilMock.when(() -> ConfigUtil.getSystemProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL))
			        .thenReturn(loggerName + ":debug");

			OpenmrsConfigurationFactory.doOpenmrsCustomisations(configuration);

			LoggerConfig loggerConfig = configuration.getLoggerConfig(loggerName);
			assertThat("Logger should have been configured from the system property", loggerConfig.getName(),
			    equalTo(loggerName));
			assertThat(loggerConfig.getLevel(), equalTo(Level.DEBUG));

			// Runtime/global lookups must not have happened — system property short-circuits the chain
			configUtilMock.verify(() -> ConfigUtil.getRuntimeProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL), never());
			configUtilMock.verify(() -> ConfigUtil.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL), never());
			contextMock.verify(() -> Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES), never());
		}
	}

	@Test
	void doOpenmrsCustomisations_shouldFallBackToRuntimePropertyWhenSystemPropertyAbsent() {
		AbstractConfiguration configuration = new DefaultConfiguration();
		String loggerName = "org.openmrs.logging.test.factory.runtime";

		try (MockedStatic<Context> contextMock = mockStatic(Context.class);
		        MockedStatic<ConfigUtil> configUtilMock = mockStatic(ConfigUtil.class)) {
			contextMock.when(Context::isSessionOpen).thenReturn(true);
			configUtilMock.when(() -> ConfigUtil.getSystemProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL))
			        .thenReturn(null);
			configUtilMock.when(() -> ConfigUtil.getRuntimeProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL))
			        .thenReturn(loggerName + ":error");

			OpenmrsConfigurationFactory.doOpenmrsCustomisations(configuration);

			LoggerConfig loggerConfig = configuration.getLoggerConfig(loggerName);
			assertThat(loggerConfig.getName(), equalTo(loggerName));
			assertThat(loggerConfig.getLevel(), equalTo(Level.ERROR));

			// Verify the precedence ORDER: system must be checked before runtime. Re-ordering of
			// ConfigUtil calls would break the documented precedence — fail the test, not silently work.
			InOrder inOrder = Mockito.inOrder(ConfigUtil.class);
			inOrder.verify(configUtilMock, () -> ConfigUtil.getSystemProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL));
			inOrder.verify(configUtilMock, () -> ConfigUtil.getRuntimeProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL));

			// Runtime wins over global; no privilege bracket needed
			configUtilMock.verify(() -> ConfigUtil.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL), never());
			contextMock.verify(() -> Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES), never());
		}
	}

	@Test
	void doOpenmrsCustomisations_shouldSkipGlobalPropertyWhenSessionClosed() {
		AbstractConfiguration configuration = new DefaultConfiguration();

		try (MockedStatic<Context> contextMock = mockStatic(Context.class);
		        MockedStatic<ConfigUtil> configUtilMock = mockStatic(ConfigUtil.class)) {
			contextMock.when(Context::isSessionOpen).thenReturn(false);
			configUtilMock.when(() -> ConfigUtil.getSystemProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL))
			        .thenReturn(null);
			configUtilMock.when(() -> ConfigUtil.getRuntimeProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL))
			        .thenReturn(null);

			OpenmrsConfigurationFactory.doOpenmrsCustomisations(configuration);

			// Critical: when no session is open we must not even touch the global property path —
			// this is the startup-safety guarantee the factory provides.
			configUtilMock.verify(() -> ConfigUtil.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL), never());
			contextMock.verify(() -> Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES), never());
		}
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

		try (MockedStatic<Context> contextMock = mockStatic(Context.class);
		        MockedStatic<ConfigUtil> configUtilMock = mockStatic(ConfigUtil.class)) {
			contextMock.when(Context::isSessionOpen).thenReturn(false);
			configUtilMock.when(() -> ConfigUtil.getSystemProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL))
			        .thenReturn(null);
			configUtilMock.when(() -> ConfigUtil.getRuntimeProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL))
			        .thenReturn(null);

			OpenmrsConfigurationFactory.doOpenmrsCustomisations(configuration);

			assertThat(configuration.getAppender(OpenmrsConstants.MEMORY_APPENDER_NAME), notNullValue());
			assertThat("Root logger should reference the memory appender",
			    configuration.getRootLogger().getAppenders().get(OpenmrsConstants.MEMORY_APPENDER_NAME), notNullValue());
		}
	}

	/**
	 * Regression test for the re-entrant logging initialization that broke downstream modules. While
	 * log4j2 is being configured, {@code doOpenmrsCustomisations} resolves {@code log.level} from the
	 * service layer, which may be unavailable or mid-initialization and therefore throw an unchecked
	 * exception (e.g. a re-entrant {@code ServiceContext} initialization NPE). That must be swallowed
	 * so configuration completes, rather than aborting it (and, in turn, {@code ServiceContext} class
	 * initialization). Without the broadened catch the NPE escapes and aborts log4j2 configuration.
	 */
	@Test
	void doOpenmrsCustomisations_shouldNotPropagateRuntimeExceptionWhenReadingGlobalPropertyFails() {
		AbstractConfiguration configuration = new DefaultConfiguration();

		try (MockedStatic<Context> contextMock = mockStatic(Context.class);
		        MockedStatic<ConfigUtil> configUtilMock = mockStatic(ConfigUtil.class)) {
			contextMock.when(Context::isSessionOpen).thenReturn(true);
			configUtilMock.when(() -> ConfigUtil.getSystemProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL))
			        .thenReturn(null);
			configUtilMock.when(() -> ConfigUtil.getRuntimeProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL))
			        .thenReturn(null);
			configUtilMock.when(() -> ConfigUtil.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL))
			        .thenThrow(new NullPointerException("ServiceContext not initialized"));

			// Must not throw — logging configuration has to tolerate a mid-initialization service layer
			OpenmrsConfigurationFactory.doOpenmrsCustomisations(configuration);

			// The proxy-privilege bracket must still be balanced even when the read fails
			contextMock.verify(() -> Context.removeProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES));
		}
	}

	/**
	 * Guards the existing behavior that the broadened catch must not erode: a
	 * {@link ServiceNotFoundException} for a service other than {@code AdministrationService} is
	 * unexpected during log-level application and must still propagate, rather than being swallowed by
	 * the catch-all that tolerates a mid-initialization service layer.
	 */
	@Test
	void doOpenmrsCustomisations_shouldPropagateServiceNotFoundExceptionForOtherServices() {
		AbstractConfiguration configuration = new DefaultConfiguration();

		try (MockedStatic<Context> contextMock = mockStatic(Context.class);
		        MockedStatic<ConfigUtil> configUtilMock = mockStatic(ConfigUtil.class)) {
			contextMock.when(Context::isSessionOpen).thenReturn(true);
			configUtilMock.when(() -> ConfigUtil.getSystemProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL))
			        .thenReturn(null);
			configUtilMock.when(() -> ConfigUtil.getRuntimeProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL))
			        .thenReturn(null);
			configUtilMock.when(() -> ConfigUtil.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL))
			        .thenThrow(new ServiceNotFoundException(PatientService.class));

			assertThrows(ServiceNotFoundException.class,
			    () -> OpenmrsConfigurationFactory.doOpenmrsCustomisations(configuration));
		}
	}

	/**
	 * The other half of the {@code ServiceNotFoundException} branch: when {@code AdministrationService}
	 * itself is not yet available (the expected startup case), the overrides are skipped and
	 * configuration completes rather than aborting. Together with the two tests above this locks all
	 * three outcomes of the catch block (other-service rethrow, AdministrationService skip, and the
	 * re-entrant runtime-exception skip).
	 */
	@Test
	void doOpenmrsCustomisations_shouldSkipOverridesWhenAdministrationServiceNotFound() {
		AbstractConfiguration configuration = new DefaultConfiguration();

		try (MockedStatic<Context> contextMock = mockStatic(Context.class);
		        MockedStatic<ConfigUtil> configUtilMock = mockStatic(ConfigUtil.class)) {
			contextMock.when(Context::isSessionOpen).thenReturn(true);
			configUtilMock.when(() -> ConfigUtil.getSystemProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL))
			        .thenReturn(null);
			configUtilMock.when(() -> ConfigUtil.getRuntimeProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL))
			        .thenReturn(null);
			configUtilMock.when(() -> ConfigUtil.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL))
			        .thenThrow(new ServiceNotFoundException(AdministrationService.class));

			// Must not throw — a missing AdministrationService during startup is expected and tolerated
			OpenmrsConfigurationFactory.doOpenmrsCustomisations(configuration);

			contextMock.verify(() -> Context.removeProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES));
		}
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
