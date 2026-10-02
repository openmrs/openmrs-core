/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db.hibernate.search.elasticsearch;

import java.util.Locale;
import java.util.Properties;
import java.util.stream.Stream;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.hibernate.search.backend.elasticsearch.index.layout.IndexLayoutStrategy;
import org.hibernate.search.backend.elasticsearch.index.layout.impl.SimpleIndexLayoutStrategy;
import org.hibernate.search.util.common.SearchException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.openmrs.api.context.Context;
import org.openmrs.logging.MemoryAppender;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

/**
 * Unit tests for {@link PrefixIndexLayoutStrategy}. These do not need a Spring context: the naming
 * tests use the package-visible prefix constructor, and the
 * {@link PrefixIndexLayoutStrategy#configureIndexLayout(Properties)} tests mock the static
 * {@link Context} to supply runtime properties.
 */
class PrefixIndexLayoutStrategyTest {

	private static final String PREFIX = "coast_";

	// A representative spread of the bare @Indexed entity index names in OpenMRS core.
	private static final String[] INDEX_NAMES = { "personname", "conceptname", "patientidentifier", "personattribute",
	        "drug" };

	// MemoryAppender keys its backing buffer on the appender name, so each test needs its own name.
	private MemoryAppender memoryAppender;

	private Logger log4jLogger;

	private Level originalLevel;

	private boolean originalAdditive;

	@BeforeEach
	void attachMemoryAppender(TestInfo testInfo) {
		memoryAppender = MemoryAppender.newBuilder().setName("PrefixIndexLayoutStrategyTest-" + testInfo.getDisplayName())
		        .setLayout(PatternLayout.newBuilder().withPattern("%m").build()).build();
		memoryAppender.start();

		log4jLogger = (Logger) LogManager.getLogger(PrefixIndexLayoutStrategy.class);
		originalLevel = log4jLogger.getLevel();
		originalAdditive = log4jLogger.isAdditive();
		// NB This needs to come before the setLevel() call
		log4jLogger.setAdditive(false);
		log4jLogger.setLevel(Level.WARN);
		log4jLogger.addAppender(memoryAppender);
	}

	@AfterEach
	void detachMemoryAppender() {
		log4jLogger.removeAppender(memoryAppender);
		log4jLogger.setLevel(originalLevel);
		log4jLogger.setAdditive(originalAdditive);
		((Logger) LogManager.getRootLogger()).getContext().updateLoggers();
		memoryAppender.stop();
		memoryAppender = null;
		log4jLogger = null;
		originalLevel = null;
	}

	private boolean loggedWarning(String fragment) {
		return memoryAppender.getLogLines().stream().anyMatch(line -> line.contains(fragment));
	}

	@Test
	void createNames_shouldPrefixConcreteIndexAndAliasesForEveryIndex() {
		PrefixIndexLayoutStrategy strategy = new PrefixIndexLayoutStrategy(PREFIX);

		for (String index : INDEX_NAMES) {
			assertEquals("coast_" + index + "-000001", strategy.createInitialElasticsearchIndexName(index));
			assertEquals("coast_" + index + "-write", strategy.createWriteAlias(index));
			assertEquals("coast_" + index + "-read", strategy.createReadAlias(index));
		}
	}

	@Test
	void extractUniqueKeyFromElasticsearchIndexName_shouldRoundTripEveryCreatedName() {
		PrefixIndexLayoutStrategy strategy = new PrefixIndexLayoutStrategy(PREFIX);

		for (String index : INDEX_NAMES) {
			String concrete = strategy.createInitialElasticsearchIndexName(index);
			assertEquals(index, strategy.extractUniqueKeyFromElasticsearchIndexName(concrete));
		}
	}

	@Test
	void extractUniqueKeyFromElasticsearchIndexName_shouldHandleReindexGenerationsBeyondTheFirst() {
		PrefixIndexLayoutStrategy strategy = new PrefixIndexLayoutStrategy(PREFIX);

		assertEquals("personname", strategy.extractUniqueKeyFromElasticsearchIndexName("coast_personname-000002"));
		assertEquals("personname", strategy.extractUniqueKeyFromElasticsearchIndexName("coast_personname-000042"));
	}

	@Test
	void extractUniqueKeyFromHibernateSearchIndexName_shouldReturnNameUnchangedWithoutThrowing() {
		// Regression guard: the Hibernate Search 8.x interface default throws UnsupportedOperationException.
		PrefixIndexLayoutStrategy strategy = new PrefixIndexLayoutStrategy(PREFIX);

		for (String index : INDEX_NAMES) {
			assertEquals(index, strategy.extractUniqueKeyFromHibernateSearchIndexName(index));
		}
	}

	@Test
	void extractUniqueKeyFromElasticsearchIndexName_shouldThrowOnNameMissingGenerationSuffix() {
		PrefixIndexLayoutStrategy strategy = new PrefixIndexLayoutStrategy(PREFIX);

		assertThrows(SearchException.class, () -> strategy.extractUniqueKeyFromElasticsearchIndexName("coast_personname"));
	}

	@Test
	void extractUniqueKeyFromElasticsearchIndexName_shouldStillParseNameWithoutTheConfiguredPrefix() {
		// Extraction is not restricted to names this strategy produced.
		PrefixIndexLayoutStrategy strategy = new PrefixIndexLayoutStrategy(PREFIX);

		assertEquals("nyanza_personname", strategy.extractUniqueKeyFromElasticsearchIndexName("nyanza_personname-000001"));
	}

	@Test
	void constructor_shouldLowerCaseThePrefix() {
		PrefixIndexLayoutStrategy strategy = new PrefixIndexLayoutStrategy("Coast_");

		assertEquals("coast_personname-000001", strategy.createInitialElasticsearchIndexName("personname"));
	}

	@Test
	void constructor_shouldTrimSurroundingWhitespace() {
		PrefixIndexLayoutStrategy strategy = new PrefixIndexLayoutStrategy("  coast_  ");

		assertEquals("coast_personname-000001", strategy.createInitialElasticsearchIndexName("personname"));
	}

	@Test
	void constructor_shouldAcceptLegalPrefixes() {
		String[] legal = { "coast_", "coast", "tenant-1_", "t1_", "1coast_", "a", "coast_region_" };

		for (String prefix : legal) {
			PrefixIndexLayoutStrategy strategy = new PrefixIndexLayoutStrategy(prefix);
			String expected = prefix.trim().toLowerCase(Locale.ROOT) + "personname-000001";
			assertEquals(expected, strategy.createInitialElasticsearchIndexName("personname"),
			    "prefix '" + prefix + "' should be accepted");
		}
	}

	@Test
	void constructor_shouldRejectIllegalPrefixes() {
		// These would produce an illegal ES index name: spaces, path separators, or a leading '-'/'_'/'+'.
		String[] illegal = { "bad prefix", "bad/prefix", "-coast", "_coast", "+coast", "coast.", "co*st", "coast#" };

		for (String prefix : illegal) {
			assertThrows(SearchException.class, () -> new PrefixIndexLayoutStrategy(prefix),
			    "prefix '" + prefix + "' should be rejected");
		}
	}

	@Test
	void constructor_shouldRejectAPrefixThatWouldOverflowTheElasticsearchNameLimit() {
		// Long enough that prefix + entity name + "-000001" exceeds Elasticsearch's 255-character limit.
		String tooLong = "a".repeat(250);

		assertThrows(SearchException.class, () -> new PrefixIndexLayoutStrategy(tooLong));
	}

	@Test
	void constructor_shouldAcceptALongButBoundedPrefix() {
		String longButValid = "a".repeat(100);

		PrefixIndexLayoutStrategy strategy = new PrefixIndexLayoutStrategy(longButValid);

		assertEquals(longButValid + "personname-000001", strategy.createInitialElasticsearchIndexName("personname"));
	}

	/**
	 * With no prefix the strategy must match Hibernate Search's built-in
	 * {@link SimpleIndexLayoutStrategy} byte for byte, so a future default-layout change fails the
	 * build instead of silently renaming indices.
	 */
	@Test
	void noPrefix_shouldMatchSimpleLayoutStrategyByteForByte() {
		IndexLayoutStrategy simple = new SimpleIndexLayoutStrategy();

		for (String blankPrefix : new String[] { null, "", "   " }) {
			PrefixIndexLayoutStrategy strategy = new PrefixIndexLayoutStrategy(blankPrefix);

			for (String index : INDEX_NAMES) {
				assertEquals(simple.createInitialElasticsearchIndexName(index),
				    strategy.createInitialElasticsearchIndexName(index));
				assertEquals(simple.createWriteAlias(index), strategy.createWriteAlias(index));
				assertEquals(simple.createReadAlias(index), strategy.createReadAlias(index));

				String concrete = simple.createInitialElasticsearchIndexName(index);
				assertEquals(simple.extractUniqueKeyFromElasticsearchIndexName(concrete),
				    strategy.extractUniqueKeyFromElasticsearchIndexName(concrete));
				assertEquals(simple.extractUniqueKeyFromHibernateSearchIndexName(index),
				    strategy.extractUniqueKeyFromHibernateSearchIndexName(index));
			}
		}
	}

	@ParameterizedTest
	@MethodSource("elasticsearchBackendAndPrefix")
	void configureIndexLayout_shouldRegisterForTheElasticsearchBackend(String backendType, String prefix) {
		Properties runtimeProps = new Properties();
		runtimeProps.setProperty(PrefixIndexLayoutStrategy.INDEX_PREFIX_RUNTIME_PROPERTY, prefix);

		try (MockedStatic<Context> contextMock = mockStatic(Context.class)) {
			contextMock.when(Context::getRuntimeProperties).thenReturn(runtimeProps);

			Properties config = backend(backendType);

			PrefixIndexLayoutStrategy.configureIndexLayout(config);

			assertEquals(PrefixIndexLayoutStrategy.BEAN_NAME,
			    config.getProperty(PrefixIndexLayoutStrategy.LAYOUT_STRATEGY_PROPERTY));
		}
	}

	@Test
	void configureIndexLayout_shouldWarnAndNotRegisterWhenBackendTypeIsAbsent() {
		Properties runtimeProps = new Properties();
		runtimeProps.setProperty(PrefixIndexLayoutStrategy.INDEX_PREFIX_RUNTIME_PROPERTY, "coast_");

		try (MockedStatic<Context> contextMock = mockStatic(Context.class)) {
			contextMock.when(Context::getRuntimeProperties).thenReturn(runtimeProps);

			Properties config = new Properties();
			PrefixIndexLayoutStrategy.configureIndexLayout(config);

			assertFalse(config.containsKey(PrefixIndexLayoutStrategy.LAYOUT_STRATEGY_PROPERTY));
			assertTrue(loggedWarning("the Elasticsearch backend is not in use"));
		}
	}

	@Test
	void configureIndexLayout_shouldNotRegisterOrWarnWhenNoPrefixIsConfigured() {
		try (MockedStatic<Context> contextMock = mockStatic(Context.class)) {
			contextMock.when(Context::getRuntimeProperties).thenReturn(new Properties());

			Properties config = backend("elasticsearch");
			PrefixIndexLayoutStrategy.configureIndexLayout(config);

			assertFalse(config.containsKey(PrefixIndexLayoutStrategy.LAYOUT_STRATEGY_PROPERTY));
			assertFalse(loggedWarning("is not applied"));
		}
	}

	@Test
	void configureIndexLayout_shouldNotRegisterOrWarnWhenPrefixIsBlank() {
		// A present-but-blank prefix means "no prefix"; it must neither register nor warn.
		Properties runtimeProps = new Properties();
		runtimeProps.setProperty(PrefixIndexLayoutStrategy.INDEX_PREFIX_RUNTIME_PROPERTY, "   ");

		try (MockedStatic<Context> contextMock = mockStatic(Context.class)) {
			contextMock.when(Context::getRuntimeProperties).thenReturn(runtimeProps);

			Properties config = backend("elasticsearch");
			PrefixIndexLayoutStrategy.configureIndexLayout(config);

			assertFalse(config.containsKey(PrefixIndexLayoutStrategy.LAYOUT_STRATEGY_PROPERTY));
			assertFalse(loggedWarning("is not applied"));
		}
	}

	@Test
	void configureIndexLayout_shouldNotResolveOrValidateThePrefixWhenBackendIsNotElasticsearch() {
		// A malformed prefix must not throw on the Lucene backend, where the strategy never applies.
		Properties runtimeProps = new Properties();
		runtimeProps.setProperty(PrefixIndexLayoutStrategy.INDEX_PREFIX_RUNTIME_PROPERTY, "bad prefix!");

		try (MockedStatic<Context> contextMock = mockStatic(Context.class)) {
			contextMock.when(Context::getRuntimeProperties).thenReturn(runtimeProps);

			Properties config = new Properties();
			config.setProperty(PrefixIndexLayoutStrategy.BACKEND_TYPE_PROPERTY, "lucene");

			PrefixIndexLayoutStrategy.configureIndexLayout(config);

			assertFalse(config.containsKey(PrefixIndexLayoutStrategy.LAYOUT_STRATEGY_PROPERTY));
		}
	}

	@Test
	void configureIndexLayout_shouldWarnWhenPrefixIsIgnoredBecauseBackendIsNotElasticsearch() {
		// A set prefix with the Lucene backend logs a warning, so a misconfigured tenant is not silent.
		Properties runtimeProps = new Properties();
		runtimeProps.setProperty(PrefixIndexLayoutStrategy.INDEX_PREFIX_RUNTIME_PROPERTY, "coast_");

		try (MockedStatic<Context> contextMock = mockStatic(Context.class)) {
			contextMock.when(Context::getRuntimeProperties).thenReturn(runtimeProps);

			Properties config = new Properties();
			config.setProperty(PrefixIndexLayoutStrategy.BACKEND_TYPE_PROPERTY, "lucene");
			config.setProperty(PrefixIndexLayoutStrategy.INDEX_PREFIX_RUNTIME_PROPERTY, "coast_");

			PrefixIndexLayoutStrategy.configureIndexLayout(config);

			assertFalse(config.containsKey(PrefixIndexLayoutStrategy.LAYOUT_STRATEGY_PROPERTY));
			assertTrue(loggedWarning("the Elasticsearch backend is not in use"));
		}
	}

	@Test
	void configureIndexLayout_shouldWarnWhenPrefixIsIgnoredBecauseStrategyAlreadyConfigured() {
		Properties runtimeProps = new Properties();
		runtimeProps.setProperty(PrefixIndexLayoutStrategy.INDEX_PREFIX_RUNTIME_PROPERTY, "coast_");

		try (MockedStatic<Context> contextMock = mockStatic(Context.class)) {
			contextMock.when(Context::getRuntimeProperties).thenReturn(runtimeProps);

			Properties config = backend("elasticsearch");
			config.setProperty(PrefixIndexLayoutStrategy.INDEX_PREFIX_RUNTIME_PROPERTY, "coast_");
			config.setProperty(PrefixIndexLayoutStrategy.LAYOUT_STRATEGY_PROPERTY, "myCustomStrategy");

			PrefixIndexLayoutStrategy.configureIndexLayout(config);

			assertEquals("myCustomStrategy", config.getProperty(PrefixIndexLayoutStrategy.LAYOUT_STRATEGY_PROPERTY));
			assertTrue(loggedWarning("a Hibernate Search layout strategy is already configured"));
		}
	}

	@Test
	void configureIndexLayout_shouldNotWarnWhenTheConfiguredStrategyIsThisBean() {
		// Operators may set layout.strategy to this bean explicitly alongside a prefix (as the BEAN_NAME
		// javadoc describes). The prefix is applied, so the "not applied" warning must not fire.
		Properties runtimeProps = new Properties();
		runtimeProps.setProperty(PrefixIndexLayoutStrategy.INDEX_PREFIX_RUNTIME_PROPERTY, "coast_");

		try (MockedStatic<Context> contextMock = mockStatic(Context.class)) {
			contextMock.when(Context::getRuntimeProperties).thenReturn(runtimeProps);

			Properties config = backend("elasticsearch");
			config.setProperty(PrefixIndexLayoutStrategy.INDEX_PREFIX_RUNTIME_PROPERTY, "coast_");
			config.setProperty(PrefixIndexLayoutStrategy.LAYOUT_STRATEGY_PROPERTY, PrefixIndexLayoutStrategy.BEAN_NAME);

			PrefixIndexLayoutStrategy.configureIndexLayout(config);

			assertEquals(PrefixIndexLayoutStrategy.BEAN_NAME,
			    config.getProperty(PrefixIndexLayoutStrategy.LAYOUT_STRATEGY_PROPERTY));
			assertFalse(loggedWarning("a Hibernate Search layout strategy is already configured"));
		}
	}

	@Test
	void configureIndexLayout_shouldDropTheMirroredHibernateSearchIndexPrefix() {
		// HibernateSessionFactoryBean mirrors runtime properties under "hibernate."; the prefix mirror
		// is dropped in configureIndexLayout, so Hibernate Search does not warn about an unknown property.
		Properties runtimeProps = new Properties();
		runtimeProps.setProperty(PrefixIndexLayoutStrategy.INDEX_PREFIX_RUNTIME_PROPERTY, PREFIX);

		try (MockedStatic<Context> contextMock = mockStatic(Context.class)) {
			contextMock.when(Context::getRuntimeProperties).thenReturn(runtimeProps);

			Properties config = backend("elasticsearch");
			config.setProperty("hibernate." + PrefixIndexLayoutStrategy.INDEX_PREFIX_RUNTIME_PROPERTY, PREFIX);

			PrefixIndexLayoutStrategy.configureIndexLayout(config);

			assertFalse(config.containsKey("hibernate." + PrefixIndexLayoutStrategy.INDEX_PREFIX_RUNTIME_PROPERTY));
			assertEquals(PrefixIndexLayoutStrategy.BEAN_NAME,
			    config.getProperty(PrefixIndexLayoutStrategy.LAYOUT_STRATEGY_PROPERTY));
		}
	}

	@Test
	void configureIndexLayout_shouldAcceptTheHibernatePrefixedRuntimeProperty() {
		// hibernate.default.properties uses "hibernate.search.backend.*" keys, so operators may
		// naturally set search.index.prefix with the "hibernate." prefix; both spellings must work.
		Properties runtimeProps = new Properties();
		runtimeProps.setProperty("hibernate." + PrefixIndexLayoutStrategy.INDEX_PREFIX_RUNTIME_PROPERTY, PREFIX);

		try (MockedStatic<Context> contextMock = mockStatic(Context.class)) {
			contextMock.when(Context::getRuntimeProperties).thenReturn(runtimeProps);

			Properties config = backend("elasticsearch");

			PrefixIndexLayoutStrategy.configureIndexLayout(config);

			assertEquals(PrefixIndexLayoutStrategy.BEAN_NAME,
			    config.getProperty(PrefixIndexLayoutStrategy.LAYOUT_STRATEGY_PROPERTY));
		}
	}

	@Test
	void configureIndexLayout_shouldWarnWhenPrefixIsPresentInTheConfigOnly() {
		// A prefix that exists in the Hibernate config but not in the runtime properties (e.g. set via
		// module config properties, which configureIndexLayout intentionally does not read) must warn
		// instead of silently not applying.
		Properties runtimeProps = new Properties();

		try (MockedStatic<Context> contextMock = mockStatic(Context.class)) {
			contextMock.when(Context::getRuntimeProperties).thenReturn(runtimeProps);

			Properties config = backend("elasticsearch");
			config.setProperty(PrefixIndexLayoutStrategy.INDEX_PREFIX_RUNTIME_PROPERTY, PREFIX);

			PrefixIndexLayoutStrategy.configureIndexLayout(config);

			assertFalse(config.containsKey(PrefixIndexLayoutStrategy.LAYOUT_STRATEGY_PROPERTY));
			assertTrue(loggedWarning("set in the Hibernate configuration, but not as the"));
		}
	}

	@Test
	void configureIndexLayout_shouldWarnWhenOnlyTheHibernatePrefixedConfigKeyIsPresent() {
		Properties runtimeProps = new Properties();

		try (MockedStatic<Context> contextMock = mockStatic(Context.class)) {
			contextMock.when(Context::getRuntimeProperties).thenReturn(runtimeProps);

			Properties config = backend("elasticsearch");
			config.setProperty("hibernate." + PrefixIndexLayoutStrategy.INDEX_PREFIX_RUNTIME_PROPERTY, PREFIX);

			PrefixIndexLayoutStrategy.configureIndexLayout(config);

			assertFalse(config.containsKey(PrefixIndexLayoutStrategy.LAYOUT_STRATEGY_PROPERTY));
			assertTrue(loggedWarning("set in the Hibernate configuration, but not as the"));
		}
	}

	private static Stream<Arguments> elasticsearchBackendAndPrefix() {
		// The Elasticsearch backend value is matched case-insensitively and with surrounding
		// whitespace tolerated, and the configured prefix is normalised on registration.
		return Stream.of(Arguments.of("Elasticsearch", "coast_"), Arguments.of("  elasticsearch  ", "coast_"),
		    Arguments.of("elasticsearch", "Coast_ "));
	}

	private static Properties backend(String type) {
		Properties config = new Properties();
		config.setProperty(PrefixIndexLayoutStrategy.BACKEND_TYPE_PROPERTY, type);
		return config;
	}
}
