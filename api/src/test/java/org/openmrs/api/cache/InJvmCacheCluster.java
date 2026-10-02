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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.parsing.ConfigurationBuilderHolder;
import org.infinispan.configuration.parsing.ParserRegistry;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachemanagerlistener.annotation.ViewChanged;
import org.infinispan.notifications.cachemanagerlistener.event.ViewChangedEvent;
import org.infinispan.remoting.transport.jgroups.JGroupsTransport;
import org.infinispan.spring.embedded.provider.SpringEmbeddedCacheManager;
import org.jgroups.JChannel;

/**
 * Starts API cache managers that form a cluster within this JVM, built from the cluster
 * configuration in {@code infinispan-api.xml} and handing out the same Spring caches as
 * {@code apiCacheManager}.
 */
final class InJvmCacheCluster {

	private InJvmCacheCluster() {
	}

	/**
	 * Starts a two-node cluster and waits until both nodes have joined it.
	 *
	 * @param clusterName a name unique to the calling test class, so clusters from different classes do
	 *            not join each other
	 * @param caches pairs of cache name and the template in {@code infinispan-api.xml} to build it from
	 * @return the two nodes
	 */
	static ExternalReadSpringCacheManager[] start(String clusterName, String... caches) throws Exception {
		ExternalReadSpringCacheManager[] nodes = new ExternalReadSpringCacheManager[2];
		nodes[0] = startNode(clusterName, "node1", caches);
		EmbeddedCacheManager node1 = nodes[0].getNativeCacheManager();
		TwoMemberViewListener listener = new TwoMemberViewListener();
		node1.addListener(listener);
		try {
			nodes[1] = startNode(clusterName, "node2", caches);
			// the view may have changed before the listener was registered
			if (node1.getMembers().size() < 2 && !listener.formed.await(30, TimeUnit.SECONDS)) {
				throw new IllegalStateException("The two cache managers did not form a cluster");
			}
		} catch (Exception e) {
			stop(nodes);
			throw e;
		} finally {
			node1.removeListener(listener);
		}
		return nodes;
	}

	/** Opens a latch once a cache manager's view has two members. */
	@Listener
	public static final class TwoMemberViewListener {

		private final CountDownLatch formed = new CountDownLatch(1);

		@ViewChanged
		public void viewChanged(ViewChangedEvent event) {
			if (event.getNewMembers().size() >= 2) {
				formed.countDown();
			}
		}
	}

	static void stop(SpringEmbeddedCacheManager... nodes) {
		if (nodes == null) {
			return;
		}
		for (int i = nodes.length - 1; i >= 0; i--) {
			if (nodes[i] != null) {
				nodes[i].stop();
			}
		}
	}

	private static ExternalReadSpringCacheManager startNode(String clusterName, String nodeName, String... caches)
	        throws Exception {
		ConfigurationBuilderHolder holder = new ParserRegistry().parseFile("infinispan-api.xml");
		holder.getGlobalConfigurationBuilder().transport().clusterName(clusterName).nodeName(nodeName)
		        .transport(new JGroupsTransport(new JChannel("org/openmrs/api/cache/jgroups-in-jvm.xml")));

		DefaultCacheManager cacheManager = new DefaultCacheManager(holder, true);
		for (int i = 0; i + 1 < caches.length; i += 2) {
			cacheManager.defineConfiguration(caches[i],
			    new ConfigurationBuilder().read(cacheManager.getCacheConfiguration(caches[i + 1])).template(false).build());
		}
		return new ExternalReadSpringCacheManager(cacheManager, CacheConfig.EXTERNAL_READ_CACHES);
	}
}
