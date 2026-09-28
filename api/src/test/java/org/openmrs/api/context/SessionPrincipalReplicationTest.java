/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.context;

import java.util.Locale;

import org.infinispan.commons.configuration.ClassAllowList;
import org.infinispan.commons.marshall.JavaSerializationMarshaller;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.parsing.ConfigurationBuilderHolder;
import org.infinispan.configuration.parsing.ParserRegistry;
import org.infinispan.manager.DefaultCacheManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Proves the single riskiest part of distributed sessions: that a {@link SessionPrincipal} actually
 * survives Infinispan's marshalling/allow-list and can cross to another node. Pure Infinispan - no
 * Spring context or database - so it is deterministic and fast.
 */
class SessionPrincipalReplicationTest {

	private static ClassAllowList allowListFor(String... entries) {
		ClassAllowList allowList = new ClassAllowList();
		for (String e : entries) {
			allowList.addClasses(e);
		}
		return allowList;
	}

	/**
	 * The core proof: with the class on the allow-list, the principal serializes to bytes and
	 * deserializes back to an equal object through Infinispan's Java marshaller - i.e. it can be
	 * replicated across the network.
	 */
	@Test
	void principalRoundTripsThroughInfinispanMarshallerWithAllowList() throws Exception {
		JavaSerializationMarshaller marshaller = new JavaSerializationMarshaller(
		        allowListFor(SessionPrincipal.class.getName()));

		SessionPrincipal original = new SessionPrincipal("a1b2c3-uuid", Locale.forLanguageTag("en-GB"), 7);

		byte[] bytes = marshaller.objectToByteBuffer(original);
		Object restored = marshaller.objectFromByteBuffer(bytes);

		assertEquals(original, restored, "principal must survive marshal/unmarshal unchanged");
		SessionPrincipal p = (SessionPrincipal) restored;
		assertEquals("a1b2c3-uuid", p.getUserUuid());
		assertEquals(Locale.forLanguageTag("en-GB"), p.getLocale());
		assertEquals(Integer.valueOf(7), p.getLocationId());
		assertTrue(p.isAuthenticated());
	}

	/**
	 * An anonymous principal (no user id) must also round-trip and report itself unauthenticated.
	 */
	@Test
	void anonymousPrincipalRoundTrips() throws Exception {
		JavaSerializationMarshaller marshaller = new JavaSerializationMarshaller(
		        allowListFor(SessionPrincipal.class.getName()));

		SessionPrincipal anon = new SessionPrincipal((String) null, null, null);
		SessionPrincipal restored = (SessionPrincipal) marshaller.objectFromByteBuffer(marshaller.objectToByteBuffer(anon));

		assertEquals(anon, restored);
		assertFalse(restored.isAuthenticated());
		assertNull(restored.getUserUuid());
	}

	/**
	 * Security: the allow-list must actually be enforced. Without the class allow-listed,
	 * deserialization is rejected - this is what stops a replicated cache from being a Java
	 * deserialization gadget sink.
	 */
	@Test
	void deserializationIsBlockedWithoutAllowList() throws Exception {
		JavaSerializationMarshaller writer = new JavaSerializationMarshaller(allowListFor(SessionPrincipal.class.getName()));
		byte[] bytes = writer.objectToByteBuffer(new SessionPrincipal("uuid-1", Locale.ENGLISH, null));

		// A marshaller whose allow-list does NOT include SessionPrincipal
		JavaSerializationMarshaller strict = new JavaSerializationMarshaller(allowListFor("java.lang.*"));

		assertThrows(Exception.class, () -> strict.objectFromByteBuffer(bytes),
		    "a non-allow-listed class must be rejected on deserialization");
	}

	/**
	 * Deterministic end-to-end proof of the <em>production</em> config: loads the real
	 * {@code infinispan-api-local.xml}, and shows the {@code sessions} cache actually marshals a
	 * {@link SessionPrincipal} through its {@code application/x-java-serialized-object} encoding and
	 * the file's allow-list - the value read back is equal but a different instance, i.e. it survived
	 * serialization. No networking required, so it is deterministic in any environment.
	 */
	@Test
	void productionSessionsCacheMarshalsPrincipalViaEncodingAndAllowList() throws Exception {
		org.infinispan.manager.DefaultCacheManager manager = new org.infinispan.manager.DefaultCacheManager(
		        "infinispan-api-local.xml");
		try {
			org.infinispan.Cache<String, Object> sessions = manager.getCache("sessions");
			SessionPrincipal original = new SessionPrincipal("uuid-5", Locale.GERMAN, 2);
			sessions.put("sid", original);

			Object restored = sessions.get("sid");
			assertEquals(original, restored, "principal read back from the production sessions cache must be equal");
			assertNotSame(original, restored, "the sessions cache's x-java-serialized-object encoding must store a "
			        + "marshalled copy - proof it survives serialization under the production config + allow-list");
		} finally {
			manager.stop();
		}
	}

	/**
	 * Verifies the <em>clustered</em> production config parses and defines {@code sessions} as a
	 * synchronously replicated cache with the principal on the serialization allow-list. Parsing does
	 * not start a transport, so this is deterministic even though the file's other caches are clustered
	 * - closing the gap that only the local file had been exercised.
	 */
	@Test
	void productionClusterConfigDefinesReplicatedSessionsCacheWithAllowList() throws Exception {
		ParserRegistry parser = new ParserRegistry();
		ConfigurationBuilderHolder holder = parser.parseFile("infinispan-api.xml");

		Configuration sessions = holder.getNamedConfigurationBuilders().get("sessions").build();
		assertEquals(CacheMode.REPL_SYNC, sessions.clustering().cacheMode(),
		    "sessions must be a synchronously replicated cache in the clustered config");

		ClassAllowList allowList = holder.getGlobalConfigurationBuilder().build().serialization().allowList().create();
		assertTrue(allowList.isSafeClass(SessionPrincipal.class.getName()),
		    "SessionPrincipal must be on the serialization allow-list");
	}

	/**
	 * Failure-mode documentation: a module that stores a non-serializable object as a session attribute
	 * would break replication. The sessions cache's serialized encoding rejects it up front (on write)
	 * rather than failing silently later on another node.
	 */
	@Test
	void nonSerializableSessionAttributeIsRejected() throws Exception {
		try (DefaultCacheManager manager = new DefaultCacheManager("infinispan-api-local.xml")) {
			org.infinispan.Cache<String, Object> sessions = manager.getCache("sessions");
			assertThrows(Exception.class, () -> sessions.put("sid", new Object()),
			    "a non-serializable attribute must be rejected by the sessions cache");
		}
	}

	/**
	 * Pins the <em>actual production</em> allow-list rather than a hand-built one. The
	 * {@code SessionPrincipal} entry is load-bearing: {@code org.openmrs.*} is not in Infinispan's
	 * default allow-list, so without it our own type would be rejected on read. An arbitrary
	 * {@code org.openmrs} class stays blocked, confirming the entry is what admits the principal (not a
	 * blanket rule).
	 */
	@Test
	void productionSessionsCacheAllowListAdmitsSessionPrincipalButNotArbitraryOpenmrsClasses() throws Exception {
		try (DefaultCacheManager manager = new DefaultCacheManager("infinispan-api-local.xml")) {
			ClassAllowList allowList = manager.getClassAllowList();
			assertTrue(allowList.isSafeClass(SessionPrincipal.class.getName()),
			    "the configured allow-list must admit our own SessionPrincipal");
			assertFalse(allowList.isSafeClass("org.openmrs.Cohort"),
			    "org.openmrs.* is not blanket-allowed, so the SessionPrincipal entry is load-bearing");
		}
	}

}
