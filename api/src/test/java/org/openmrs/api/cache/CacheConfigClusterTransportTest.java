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

import java.lang.reflect.Field;

import org.infinispan.configuration.global.TransportConfiguration;
import org.infinispan.configuration.parsing.ConfigurationBuilderHolder;
import org.infinispan.configuration.parsing.ParserRegistry;
import org.infinispan.remoting.transport.jgroups.JGroupsTransport;
import org.jgroups.JChannel;
import org.jgroups.protocols.TCP;
import org.jgroups.protocols.TP;
import org.jgroups.protocols.TUNNEL;
import org.jgroups.protocols.UDP;
import org.jgroups.protocols.dns.DNS_PING;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Covers the cluster-mode transport wiring of {@link CacheConfig}. The api cache runs on its own
 * JGroups channel one port above the hibernate cache channel; these tests build the channel
 * (without connecting it, so no networking is needed) and assert it binds the api port without
 * mutating the shared {@code jgroups.bind.port} property.
 */
class CacheConfigClusterTransportTest {

	private static final String BIND_PORT = "jgroups.bind.port";

	private static final String DNS_QUERY = "jgroups.dns.query";

	private String previousBindPort;

	private String previousDnsQuery;

	@BeforeEach
	void rememberProperty() {
		previousBindPort = System.getProperty(BIND_PORT);
		previousDnsQuery = System.getProperty(DNS_QUERY);
	}

	@AfterEach
	void restoreProperty() {
		restore(BIND_PORT, previousBindPort);
		restore(DNS_QUERY, previousDnsQuery);
	}

	private static void restore(String property, String previousValue) {
		if (previousValue == null) {
			System.clearProperty(property);
		} else {
			System.setProperty(property, previousValue);
		}
	}

	private CacheConfig cacheConfigWithStack(String stack) {
		CacheConfig cacheConfig = new CacheConfig();
		ReflectionTestUtils.setField(cacheConfig, "cacheStack", stack);
		return cacheConfig;
	}

	@Test
	void nextPortAfterHibernateChannelDefaultsToBasePlusOne() {
		System.clearProperty(BIND_PORT);
		assertEquals("7801", new CacheConfig().nextPortAfterHibernateChannel());
	}

	@Test
	void nextPortAfterHibernateChannelIsOneAboveTheConfiguredPort() {
		System.setProperty(BIND_PORT, "8000");
		assertEquals("8001", new CacheConfig().nextPortAfterHibernateChannel());
	}

	@Test
	void apiProtocolClassMapsTheStackToItsTransport() {
		assertSame(TCP.class, cacheConfigWithStack("tcp").apiProtocolClass());
		assertSame(TCP.class, cacheConfigWithStack("kubernetes").apiProtocolClass());
		assertSame(UDP.class, cacheConfigWithStack("udp").apiProtocolClass());
		assertSame(UDP.class, cacheConfigWithStack("").apiProtocolClass());
		assertSame(TUNNEL.class, cacheConfigWithStack("tunnel").apiProtocolClass());
	}

	@Test
	void buildApiChannelBindsTheGivenPortWithoutMutatingTheGlobalProperty() throws Exception {
		System.setProperty(BIND_PORT, "7800");
		CacheConfig cacheConfig = new CacheConfig();
		String config = cacheConfig.getJChannelConfig("tcp");

		try (JChannel channel = cacheConfig.buildApiChannel(config, "7801")) {
			TP transport = channel.getProtocolStack().findProtocol(TCP.class);
			assertEquals(7801, transport.getBindPort(), "the channel must be built on the api port");
		}
		assertEquals("7800", System.getProperty(BIND_PORT),
		    "building the api channel must not touch the hibernate channel's jgroups.bind.port");
	}

	@Test
	void buildApiChannelDoesNotSetTheGlobalPropertyWhenItWasUnset() throws Exception {
		System.clearProperty(BIND_PORT);
		CacheConfig cacheConfig = new CacheConfig();
		String config = cacheConfig.getJChannelConfig("tcp");

		try (JChannel channel = cacheConfig.buildApiChannel(config, "7801")) {
			TP transport = channel.getProtocolStack().findProtocol(TCP.class);
			assertEquals(7801, transport.getBindPort(), "the channel must be built on the api port");
		}
		assertNull(System.getProperty(BIND_PORT), "an unset jgroups.bind.port must stay unset after the build");
	}

	@Test
	void rejectsANonNumericBindPort() {
		System.setProperty(BIND_PORT, "not-a-port");
		CacheConfig cacheConfig = new CacheConfig();
		assertThrows(IllegalArgumentException.class, () -> cacheConfig.nextPortAfterHibernateChannel());
	}

	@Test
	void rejectsABindPortOutOfRange() throws Exception {
		CacheConfig cacheConfig = cacheConfigWithStack("tcp");
		ReflectionTestUtils.setField(cacheConfig, "apiCacheBindPort", "70000");
		ConfigurationBuilderHolder holder = new ParserRegistry().parseFile("infinispan-api.xml");

		assertThrows(IllegalArgumentException.class, () -> cacheConfig.configureApiClusterTransport(holder));
	}

	@Test
	void rejectsADerivedPortThatExceedsRange() {
		System.setProperty(BIND_PORT, "65535"); // base + 1 = 65536, out of range
		CacheConfig cacheConfig = new CacheConfig();
		assertThrows(IllegalArgumentException.class, () -> cacheConfig.nextPortAfterHibernateChannel());
	}

	@Test
	void toleratesWhitespaceAroundAnExplicitBindPort() throws Exception {
		CacheConfig cacheConfig = cacheConfigWithStack("tcp");
		ReflectionTestUtils.setField(cacheConfig, "apiCacheBindPort", " 7801 ");
		ConfigurationBuilderHolder holder = new ParserRegistry().parseFile("infinispan-api.xml");

		cacheConfig.configureApiClusterTransport(holder); // must not fail the channel build on the whitespace

		JGroupsTransport transport = (JGroupsTransport) holder.getGlobalConfigurationBuilder().build().transport()
		        .transport();
		try (JChannel channel = transport.getChannel()) {
			TP tp = channel.getProtocolStack().findProtocol(TCP.class);
			assertEquals(7801, tp.getBindPort());
		}
	}

	@Test
	void configureApiClusterTransportWiresTheApiChannelOntoItsOwnPort() throws Exception {
		System.setProperty(BIND_PORT, "7800");
		CacheConfig cacheConfig = cacheConfigWithStack("tcp");

		ConfigurationBuilderHolder holder = new ParserRegistry().parseFile("infinispan-api.xml");
		cacheConfig.configureApiClusterTransport(holder);

		assertEquals("7800", System.getProperty(BIND_PORT),
		    "wiring the api transport must not mutate the hibernate channel's jgroups.bind.port");

		TransportConfiguration transportConfig = holder.getGlobalConfigurationBuilder().build().transport();
		assertEquals("infinispan-api-cluster", transportConfig.clusterName());
		JGroupsTransport transport = (JGroupsTransport) transportConfig.transport();
		assertNotNull(transport, "a JGroups transport must be wired for the api cluster");
		try (JChannel channel = transport.getChannel()) {
			TP tp = channel.getProtocolStack().findProtocol(TCP.class);
			assertEquals(7801, tp.getBindPort(), "the api channel must bind one port above the hibernate channel");
		}
	}

	/**
	 * Regression guard for the discovery-port bug. On the kubernetes stack, DNS_PING uses A records
	 * (which carry no port) and caches the transport bind port at init(), when the channel is built.
	 * Applying the api port only afterwards (TP.setBindPort) moves the transport socket but leaves
	 * DNS_PING probing the base port, so the api cluster never forms under DNS discovery.
	 * buildApiChannel must therefore build the channel already on the api port. Asserting the transport
	 * bind port alone does not catch this, because the pre-fix approach also ends up with the transport
	 * on the api port.
	 */
	@Test
	void buildApiChannelPointsDnsPingDiscoveryAtTheApiPort() throws Exception {
		System.setProperty(BIND_PORT, "7800");
		System.setProperty(DNS_QUERY, "openmrs-headless.default.svc.cluster.local");
		CacheConfig cacheConfig = new CacheConfig();
		String config = cacheConfig.getJChannelConfig("kubernetes");

		// The pre-fix approach: build on the base port, then move only the transport. DNS_PING has
		// already cached the base port at init(), so discovery would probe the wrong port.
		try (JChannel builtThenMoved = new JChannel(config)) {
			builtThenMoved.getProtocolStack().getTransport().setBindPort(7801);
			assertEquals(7801, builtThenMoved.getProtocolStack().getTransport().getBindPort());
			assertEquals(7800, dnsPingDiscoveryPort(builtThenMoved),
			    "setting the port only on the transport leaves DNS_PING on the base port - this was the bug");
		}

		// buildApiChannel builds on the api port, so DNS_PING caches the api port for discovery.
		try (JChannel channel = cacheConfig.buildApiChannel(config, "7801")) {
			assertEquals(7801, dnsPingDiscoveryPort(channel),
			    "DNS_PING must probe the api port so the api cluster forms under DNS discovery");
		}
	}

	/** Reads DNS_PING's cached discovery port - the transport bind port it snapshots at init(). */
	private static int dnsPingDiscoveryPort(JChannel channel) throws Exception {
		DNS_PING dnsPing = channel.getProtocolStack().findProtocol(DNS_PING.class);
		Field transportPort;
		try {
			transportPort = DNS_PING.class.getDeclaredField("transportPort");
		} catch (NoSuchFieldException e) {
			throw new AssertionError("JGroups DNS_PING no longer exposes a 'transportPort' field; this test reads it "
			        + "by reflection to verify the discovery port. Update the field name for the current JGroups version.",
			        e);
		}
		transportPort.setAccessible(true);
		return transportPort.getInt(dnsPing);
	}
}
