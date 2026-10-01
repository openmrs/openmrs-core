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

import java.util.concurrent.TimeUnit;

import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.parsing.ConfigurationBuilderHolder;
import org.infinispan.configuration.parsing.ParserRegistry;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.remoting.transport.jgroups.JGroupsTransport;
import org.infinispan.spring.embedded.provider.SpringEmbeddedCacheManager;
import org.jgroups.JChannel;

/**
 * Starts API cache managers that form a cluster within this JVM, built from the cluster
 * configuration in {@code infinispan-api.xml}.
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
		ExternalReadSpringCacheManager[] nodes = { startNode(clusterName, "node1", caches),
		        startNode(clusterName, "node2", caches) };

		long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
		while (nodes[0].getNativeCacheManager().getMembers().size() < 2) {
			if (System.nanoTime() > deadline) {
				stop(nodes);
				throw new IllegalStateException("The two cache managers did not form a cluster");
			}
			Thread.sleep(50);
		}
		return nodes;
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
		return new ExternalReadSpringCacheManager(cacheManager);
	}
}
