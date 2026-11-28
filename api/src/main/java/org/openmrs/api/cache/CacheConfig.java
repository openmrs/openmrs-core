/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.cache;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.CaseUtils;
import org.infinispan.commons.configuration.io.ConfigurationResourceResolver;
import org.infinispan.commons.dataconversion.MediaType;
import org.infinispan.configuration.parsing.ConfigurationBuilderHolder;
import org.infinispan.configuration.parsing.ParserRegistry;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.remoting.transport.jgroups.JGroupsTransport;
import org.infinispan.spring.embedded.provider.SpringEmbeddedCacheManager;
import org.jgroups.JChannel;
import org.jgroups.conf.ConfiguratorFactory;
import org.jgroups.conf.ProtocolConfiguration;
import org.jgroups.conf.ProtocolStackConfigurator;
import org.jgroups.protocols.TP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

/**
 * CacheConfig provides a cache manager for the @Cacheable annotation and uses Infinispan under the
 * hood. The config of Infinispan is loaded from infinispan-api-local.xml/infinispan-api.xml and can
 * be customized by providing a different file through the cache_config property. It is expected for
 * the config to contain a template named "entity" to be used to create caches.
 * <p>
 * Caches can be added by modules through a cache-api.yaml file in the classpath. The file shall
 * contain only the <b>caches</b> element as defined in Infinispan docs at <a href=
 * "https://infinispan.org/docs/13.0.x/titles/configuring/configuring.html#multiple_caches">multiple
 * caches</a>
 * <p>
 * Please note the underlying implementation changed from ehcache to Infinispan since 2.8.x to
 * support replicated/distributed caches.
 */
@Configuration
public class CacheConfig {

	private final static Logger log = LoggerFactory.getLogger(CacheConfig.class);

	/**
	 * The caches that are only filled on a miss, by {@code @Cacheable} or by their owning component,
	 * and never overwritten, so that a fill on one node can leave other nodes' entries in place. See
	 * {@link ExternalReadSpringCacheManager}.
	 */
	static final Set<String> EXTERNAL_READ_CACHES = Set.of("userSearchLocales", "conceptIdsByMapping",
	    "serializerWhiteListTypes", RolePrivilegeCache.CACHE_NAME, GlobalPropertyCache.CACHE_NAME);

	private static final String JGROUPS_BIND_PORT_PROPERTY = "jgroups.bind.port";

	private static final String DEFAULT_JGROUPS_BIND_PORT = "7800";

	@Value("${cache.type:local}")
	private String cacheType;

	@Value("${cache.config:}")
	private String cacheConfig;

	@Value("${cache.stack:}")
	private String cacheStack;

	@Value("${cache.api.bind.port:}")
	private String apiCacheBindPort;

	@Bean(name = "apiCacheManager", destroyMethod = "stop")
	public SpringEmbeddedCacheManager apiCacheManager() throws Exception {
		if (StringUtils.isBlank(cacheConfig)) {
			String local = "local".equalsIgnoreCase(cacheType.trim()) ? "-local" : "";
			cacheConfig = "infinispan-api" + local + ".xml";
		}

		ParserRegistry parser = new ParserRegistry();
		ConfigurationBuilderHolder baseConfigBuilder = parser.parseFile(cacheConfig);
		if (cacheType.trim().equals("cluster")) {
			configureApiClusterTransport(baseConfigBuilder);
		}
		// Determine cache type based on loaded template for "entity"
		String cacheType = baseConfigBuilder.getNamedConfigurationBuilders().get("entity").build().elementName();
		cacheType = StringUtils.removeEnd(cacheType, "-configuration");
		cacheType = CaseUtils.toCamelCase(cacheType, false, '-');

		DumperOptions options = new DumperOptions();
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		options.setPrettyFlow(true);
		Yaml yaml = new Yaml(options);

		for (URL configFile : getCacheConfigurations()) {
			// Apply cache type for caches using the 'entity' template
			// and add the 'infinispan.cacheContainer.caches' parent.
			// Skip already defined caches.
			InputStream fullConfig = buildFullConfig(yaml, configFile,
			    baseConfigBuilder.getNamedConfigurationBuilders().keySet(), cacheType);
			parser.parse(fullConfig, baseConfigBuilder, ConfigurationResourceResolver.DEFAULT, MediaType.APPLICATION_YAML);
		}

		DefaultCacheManager cacheManager = new DefaultCacheManager(baseConfigBuilder, true);
		return new ExternalReadSpringCacheManager(cacheManager, EXTERNAL_READ_CACHES);
	}

	/**
	 * Wires the api cache onto its own JGroups channel, one port above the hibernate cache channel, so
	 * the two clusters do not share a transport.
	 */
	void configureApiClusterTransport(ConfigurationBuilderHolder baseConfigBuilder) throws Exception {
		String channelConfig = getJChannelConfig(cacheStack);
		if (StringUtils.isBlank(apiCacheBindPort)) {
			apiCacheBindPort = nextPortAfterHibernateChannel();
		}
		int apiPort = Integer.parseInt(apiCacheBindPort);
		JChannel jchannel = buildApiChannel(channelConfig, apiPort);
		JGroupsTransport transport = new JGroupsTransport(jchannel);
		baseConfigBuilder.getGlobalConfigurationBuilder().transport().clusterName("infinispan-api-cluster")
		        .transport(transport);
	}

	// The api channel runs alongside the hibernate cache channel, one port above it.
	String nextPortAfterHibernateChannel() {
		String hibernateCacheBindPort = System.getProperty(JGROUPS_BIND_PORT_PROPERTY);
		if (hibernateCacheBindPort == null) {
			hibernateCacheBindPort = DEFAULT_JGROUPS_BIND_PORT;
		}
		return String.valueOf(Integer.parseInt(hibernateCacheBindPort) + 1);
	}

	/**
	 * Builds the api channel with its bind port set directly on the parsed configuration. DNS_PING
	 * caches the transport bind port at init() (when the channel is built), so setting it only on the
	 * transport afterwards would leave discovery on the old port and the api cluster would never form.
	 * Setting it on the parsed config - rather than the global jgroups.bind.port system property -
	 * means building this channel cannot disturb the hibernate cache channel built elsewhere in the
	 * same JVM. The channel is then checked against the requested port: a stack that never declared
	 * {@code bind_port} cannot be rewritten, and building on the wrong port would only show up much
	 * later as an api cluster that never forms, so fail here instead.
	 *
	 * @param channelConfig classpath or file location of the JGroups stack to build
	 * @param bindPort the port the api channel must be built on
	 * @return a channel bound to {@code bindPort}
	 * @throws IllegalStateException if the stack did not pick up {@code bindPort}
	 */
	JChannel buildApiChannel(String channelConfig, int bindPort) throws Exception {
		ProtocolStackConfigurator configurator = ConfiguratorFactory.getStackConfigurator(channelConfig);
		for (ProtocolConfiguration protocol : configurator.getProtocolStack()) {
			if (protocol.getProperties().containsKey("bind_port")) {
				protocol.getProperties().put("bind_port", Integer.toString(bindPort));
			}
		}
		JChannel channel = new JChannel(configurator);
		TP transport = channel.getProtocolStack().getTransport();
		if (transport.getBindPort() != bindPort) {
			channel.close();
			throw new IllegalStateException("bind_port " + bindPort + " was not applied to the api channel (transport is on "
			        + transport.getBindPort()
			        + "), so DNS_PING would discover on the wrong port and the api cluster would never form");
		}
		return channel;
	}

	private static InputStream buildFullConfig(Yaml yaml, URL configFile, Set<String> templateNames, String cacheType)
	        throws IOException {
		Map<String, Object> loadedConfig = yaml.load(configFile.openStream());

		Map<String, Object> config = new LinkedHashMap<>();
		Map<String, Object> cacheContainer = new LinkedHashMap<>();
		Map<String, Object> caches = new LinkedHashMap<>();
		Map<String, Object> cacheList = new LinkedHashMap<>();
		config.put("infinispan", cacheContainer);
		cacheContainer.put("cacheContainer", caches);

		@SuppressWarnings("unchecked")
		Map<String, Object> loadedCaches = (Map<String, Object>) loadedConfig.get("caches");
		for (Map.Entry<String, Object> entry : loadedCaches.entrySet()) {
			if (templateNames.contains(entry.getKey())) {
				// already defined by the base configuration; don't redefine it
				continue;
			}
			@SuppressWarnings("unchecked")
			Map<String, Object> value = (Map<String, Object>) entry.getValue();
			if (templateNames.contains(value.get("configuration"))) {
				// A cache referencing a base template must be wrapped in the mode's cache type
				// (local-cache/invalidation-cache) so it inherits that template's settings.
				Map<Object, Object> cache = new LinkedHashMap<>();
				cache.put(cacheType, value);
				cacheList.put(entry.getKey(), cache);
			} else {
				cacheList.put(entry.getKey(), value);
			}
		}
		if (!cacheList.isEmpty()) {
			caches.put("caches", cacheList);
		}

		String configDump = yaml.dump(config);
		return new ByteArrayInputStream(configDump.getBytes(StandardCharsets.UTF_8));
	}

	public List<URL> getCacheConfigurations() {
		Resource[] configResources;
		try {
			ResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();
			configResources = patternResolver.getResources("classpath*:cache-api.yaml");
		} catch (IOException e) {
			throw new IllegalStateException("Unable to find cache configurations", e);
		}

		List<URL> files = new ArrayList<>();
		for (Resource configResource : configResources) {
			try {
				URL file = configResource.getURL();
				files.add(file);
			} catch (IOException e) {
				log.error("Failed to get cache config file: {}", configResource, e);
			}
		}

		return files;
	}

	public String getJChannelConfig(String cacheStack) {
		String jChannelConfig;
		switch (cacheStack.trim()) {
			case "tcp":
				jChannelConfig = "default-configs/default-jgroups-tcp.xml";
				break;
			case "kubernetes":
				jChannelConfig = "default-configs/default-jgroups-kubernetes.xml";
				break;
			case "google":
				jChannelConfig = "default-configs/default-jgroups-google.xml";
				break;
			case "tunnel":
				jChannelConfig = "default-configs/default-jgroups-tunnel.xml";
				break;
			case "ec2":
				jChannelConfig = "default-configs/default-jgroups-ec2.xml";
				break;
			case "azure":
				jChannelConfig = "default-configs/default-jgroups-azure.xml";
				break;
			default:
				jChannelConfig = "default-configs/default-jgroups-udp.xml";
		}
		return jChannelConfig;
	}
}
