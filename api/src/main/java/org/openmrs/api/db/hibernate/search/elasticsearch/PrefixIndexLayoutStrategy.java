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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.search.backend.elasticsearch.index.layout.IndexLayoutStrategy;
import org.hibernate.search.util.common.SearchException;
import org.openmrs.api.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * An {@link IndexLayoutStrategy} that prefixes all Elasticsearch index names and aliases with the
 * configured prefix, so each OpenMRS instance keeps its index names in its own namespace.
 * <p>
 * Registered only for the Elasticsearch backend when a non-empty prefix is set, and {@code @Lazy}
 * so the default Lucene install never constructs it. Without a prefix it produces the same names as
 * the built-in {@code simple} layout.
 *
 * @since 3.0.0
 */
@Lazy
@Component(PrefixIndexLayoutStrategy.BEAN_NAME)
public class PrefixIndexLayoutStrategy implements IndexLayoutStrategy {

	private static final Logger log = LoggerFactory.getLogger(PrefixIndexLayoutStrategy.class);

	/**
	 * Value of {@code hibernate.search.backend.layout.strategy}, resolved by Hibernate Search to a
	 * Spring bean.
	 */
	public static final String BEAN_NAME = "prefixIndexLayoutStrategy";

	/**
	 * Runtime property holding the index prefix; changing it requires a reindex. Extraction strips the
	 * first prefix match only, so one prefix must not be a prefix of another (e.g. {@code coast_} and
	 * {@code coast_extra_}).
	 */
	public static final String INDEX_PREFIX_RUNTIME_PROPERTY = "search.index.prefix";

	static final String BACKEND_TYPE_PROPERTY = "hibernate.search.backend.type";

	static final String LAYOUT_STRATEGY_PROPERTY = "hibernate.search.backend.layout.strategy";

	private static final String HIBERNATE_PROPERTY_PREFIX = "hibernate.";

	// Mirrors org.hibernate.search.backend.elasticsearch.index.layout.impl.SimpleIndexLayoutStrategy.
	private static final String INITIAL_INDEX_SUFFIX = "-000001";

	private static final String WRITE_ALIAS_SUFFIX = "-write";

	private static final String READ_ALIAS_SUFFIX = "-read";

	private static final Pattern UNIQUE_KEY_EXTRACTION_PATTERN = Pattern.compile("(.*)-\\d{6}");

	// Index names must be legal Elasticsearch index names: lower case, starting with a letter or
	// digit (a leading '-', '_' or '+' is forbidden in Elasticsearch).
	private static final Pattern VALID_PREFIX_PATTERN = Pattern.compile("[a-z0-9][a-z0-9_-]*");

	// Elasticsearch limits an index name to 255 bytes. A generated name is the prefix, the entity index
	// name and a suffix such as "-000001"; the prefix (ASCII only, so bytes equal characters) is capped
	// to leave generous room for the entity name and suffix within that limit.
	private static final int MAX_ES_INDEX_NAME_LENGTH = 255;

	private static final int RESERVED_FOR_INDEX_NAME = 100;

	private static final int MAX_PREFIX_LENGTH = MAX_ES_INDEX_NAME_LENGTH - INITIAL_INDEX_SUFFIX.length()
	        - RESERVED_FOR_INDEX_NAME;

	private final String prefix;

	/**
	 * Reads the prefix from the OpenMRS runtime properties; {@code @Lazy} defers construction until the
	 * Elasticsearch backend bootstraps. {@code @Autowired} marks this as the Spring constructor, the
	 * other one is for tests only.
	 */
	@Autowired
	public PrefixIndexLayoutStrategy() {
		this(getConfiguredPrefix());
	}

	/**
	 * @param prefix the prefix, or {@code null}/blank for none
	 */
	PrefixIndexLayoutStrategy(String prefix) {
		this.prefix = normalizePrefix(prefix);
	}

	@Override
	public String createInitialElasticsearchIndexName(String hibernateSearchIndexName) {
		return prefix + hibernateSearchIndexName + INITIAL_INDEX_SUFFIX;
	}

	@Override
	public String createWriteAlias(String hibernateSearchIndexName) {
		return prefix + hibernateSearchIndexName + WRITE_ALIAS_SUFFIX;
	}

	@Override
	public String createReadAlias(String hibernateSearchIndexName) {
		return prefix + hibernateSearchIndexName + READ_ALIAS_SUFFIX;
	}

	/**
	 * The logical name is the unique key and is never prefixed. Overridden because the 8.x interface
	 * default throws {@link UnsupportedOperationException}.
	 */
	@Override
	public String extractUniqueKeyFromHibernateSearchIndexName(String hibernateSearchIndexName) {
		return hibernateSearchIndexName;
	}

	/**
	 * Strips the prefix and the {@code -NNNNNN} generation suffix to recover the logical name.
	 */
	@Override
	public String extractUniqueKeyFromElasticsearchIndexName(String elasticsearchIndexName) {
		String name = elasticsearchIndexName;
		if (!prefix.isEmpty() && name.startsWith(prefix)) {
			name = name.substring(prefix.length());
		}

		Matcher matcher = UNIQUE_KEY_EXTRACTION_PATTERN.matcher(name);
		if (!matcher.matches()) {
			throw new SearchException("Unrecognized Elasticsearch index name '" + elasticsearchIndexName
			        + "'; expected an optional prefix followed by '<name>-NNNNNN'");
		}
		return matcher.group(1);
	}

	/**
	 * Reads and normalises the index prefix from the OpenMRS runtime properties.
	 *
	 * @return the normalised prefix, or an empty string if none is configured
	 * @since 3.0.0
	 */
	public static String getConfiguredPrefix() {
		return normalizePrefix(configuredPrefixRaw());
	}

	/**
	 * Registers the strategy for the Elasticsearch backend when a prefix is configured; a no-op
	 * otherwise. Called by {@code HibernateSessionFactoryBean}. The prefix is resolved from the same
	 * source ({@link Context} runtime properties) the strategy bean reads, so the registration decision
	 * and the constructed bean can never disagree. The prefix is validated only in the registration
	 * branch, so a malformed prefix cannot fail a non-Elasticsearch startup.
	 *
	 * @param config the Hibernate configuration properties, mutated in place
	 * @since 3.0.0
	 */
	public static void configureIndexLayout(Properties config) {
		// HibernateSessionFactoryBean mirrors the prefix to "hibernate."; Hibernate Search would
		// warn about that unknown property, so drop it.
		String hibernateSpelledPrefix = config.getProperty(HIBERNATE_PROPERTY_PREFIX + INDEX_PREFIX_RUNTIME_PROPERTY);
		config.remove(HIBERNATE_PROPERTY_PREFIX + INDEX_PREFIX_RUNTIME_PROPERTY);

		String rawPrefix = configuredPrefixRaw();
		if (rawPrefix == null || rawPrefix.trim().isEmpty()) {
			// No prefix in the runtime properties. If one is present in the Hibernate configuration
			// under either spelling anyway (e.g. set via module config properties, which
			// configureIndexLayout deliberately does not read), it would be silently ignored - warn.
			warnIfPrefixPresentButNotApplied(config.getProperty(INDEX_PREFIX_RUNTIME_PROPERTY), hibernateSpelledPrefix);
			return;
		}

		if (!isElasticsearchBackend(config)) {
			warnNotApplied("the Elasticsearch backend is not in use");
			return;
		}
		String configuredStrategy = config.getProperty(LAYOUT_STRATEGY_PROPERTY);
		if (configuredStrategy != null) {
			// An operator-configured strategy wins, so the prefix is not applied - unless it is this
			// strategy's own bean, where the prefix does apply and warning that it was ignored would be wrong.
			if (!BEAN_NAME.equals(configuredStrategy.trim())) {
				warnNotApplied("a Hibernate Search layout strategy is already configured");
			}
			return;
		}

		// Validate/normalise now so a malformed prefix fails fast (Elasticsearch only); the strategy
		// bean re-reads and normalises the same value identically when Hibernate Search constructs it.
		normalizePrefix(rawPrefix);
		config.setProperty(LAYOUT_STRATEGY_PROPERTY, BEAN_NAME);
	}

	/**
	 * Warns that a configured prefix is being ignored, so a misconfigured tenant does not silently run
	 * without index isolation.
	 */
	private static void warnNotApplied(String reason) {
		log.warn("The {} property is set, but the {} strategy is not applied because {}", INDEX_PREFIX_RUNTIME_PROPERTY,
		    BEAN_NAME, reason);
	}

	/**
	 * Warns when a prefix value is only present in the Hibernate configuration, which
	 * {@link #configureIndexLayout(Properties)} does not read, so it would otherwise be silently
	 * ignored. Also fires when the {@code hibernate.}-prefixed spelling is used, which Hibernate Search
	 * does not define as its own property.
	 */
	private static void warnIfPrefixPresentButNotApplied(String configPrefix, String hibernateSpelledPrefix) {
		boolean configHasPrefix = (configPrefix != null && !configPrefix.trim().isEmpty())
		        || (hibernateSpelledPrefix != null && !hibernateSpelledPrefix.trim().isEmpty());
		if (configHasPrefix) {
			log.warn("A search index prefix is set in the Hibernate configuration, but not as the {} runtime property; "
			        + "the {} strategy is not applied",
			    INDEX_PREFIX_RUNTIME_PROPERTY, BEAN_NAME);
		}
	}

	/**
	 * @return the raw, un-normalised index prefix from the OpenMRS runtime properties (the bare
	 *         property or its {@code hibernate.}-prefixed spelling), or {@code null} if none is
	 *         configured
	 */
	static String configuredPrefixRaw() {
		Properties runtimeProperties = Context.getRuntimeProperties();
		if (runtimeProperties == null) {
			return null;
		}
		String prefix = runtimeProperties.getProperty(INDEX_PREFIX_RUNTIME_PROPERTY);
		if (prefix != null && !prefix.trim().isEmpty()) {
			return prefix;
		}
		// Accept the "hibernate."-prefixed spelling too, since backend properties in
		// hibernate.default.properties follow the hibernate.search.backend.* convention.
		return runtimeProperties.getProperty(HIBERNATE_PROPERTY_PREFIX + INDEX_PREFIX_RUNTIME_PROPERTY);
	}

	private static boolean isElasticsearchBackend(Properties config) {
		return "elasticsearch".equalsIgnoreCase(config.getProperty(BACKEND_TYPE_PROPERTY, "").trim());
	}

	private static String normalizePrefix(String prefix) {
		if (prefix == null) {
			return "";
		}

		String normalized = prefix.trim().toLowerCase(Locale.ROOT);
		if (normalized.isEmpty()) {
			return "";
		}

		if (!VALID_PREFIX_PATTERN.matcher(normalized).matches()) {
			throw new SearchException("Invalid " + INDEX_PREFIX_RUNTIME_PROPERTY + " '" + prefix
			        + "': a search index prefix must start with a letter or digit and contain only "
			        + "lower-case letters, digits, '_' or '-'");
		}
		if (normalized.length() > MAX_PREFIX_LENGTH) {
			throw new SearchException("Invalid " + INDEX_PREFIX_RUNTIME_PROPERTY + " '" + prefix
			        + "': a search index prefix must be at most " + MAX_PREFIX_LENGTH
			        + " characters so generated Elasticsearch index names stay within the 255-character limit");
		}
		return normalized;
	}
}
