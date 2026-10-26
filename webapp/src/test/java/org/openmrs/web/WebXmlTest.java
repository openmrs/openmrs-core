/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openmrs.util.XmlUtils;
import org.openmrs.web.filter.RepeatedSlashFilter;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the order this module's {@code web.xml} puts its filters in, which a test that assembles
 * its own filter chain cannot see. For a request, the container chains the filters whose
 * {@code <url-pattern>} matches it in the order of their {@code <filter-mapping>} elements, ahead
 * of those mapped by {@code <servlet-name>}.
 */
class WebXmlTest {

	private static final Path WEB_XML = Path.of("src", "main", "webapp", "WEB-INF", "web.xml");

	private static List<Element> filters;

	private static List<Element> filterMappings;

	@BeforeAll
	static void parseWebXml() throws Exception {
		Element webApp = XmlUtils.createDocumentBuilder().parse(WEB_XML.toFile()).getDocumentElement();
		filters = children(webApp, "filter");
		filterMappings = children(webApp, "filter-mapping");
	}

	/**
	 * A repeated slash at or before {@code /ws} fails a request with a 500 once it reaches
	 * {@code springSecurityFilterChain}, unless {@link RepeatedSlashFilter} has collapsed it first
	 * (TRUNK-6824). This fails if no {@code <filter>} declares that class, or if its mapping is removed
	 * or commented out, placed after {@code springSecurityFilterChain}'s, narrowed from {@code /*},
	 * made a {@code <servlet-name>} mapping, or given dispatcher types without {@code REQUEST}. It
	 * reads the descriptor's source, not the copy the build filters into the war.
	 */
	@Test
	void repeatedSlashFilter_shouldFilterEveryRequestBeforeSpringSecurityFilterChain() {
		List<String> names = new ArrayList<>();
		for (Element filter : filters) {
			if (values(filter, "filter-class").contains(RepeatedSlashFilter.class.getName())) {
				names.addAll(values(filter, "filter-name"));
			}
		}
		assertFalse(names.isEmpty(), "no <filter> declares " + RepeatedSlashFilter.class.getName());

		int springSecurityFilterChain = firstMapping(
		    mapping -> values(mapping, "filter-name").contains("springSecurityFilterChain"));
		int repeatedSlashFilter = firstMapping(mapping -> !Collections.disjoint(names, values(mapping, "filter-name"))
		        && values(mapping, "url-pattern").contains("/*") && appliesToRequests(mapping));

		assertNotEquals(-1, springSecurityFilterChain, "no <filter-mapping> names springSecurityFilterChain");
		assertNotEquals(-1, repeatedSlashFilter, "no <filter-mapping> sends every request to RepeatedSlashFilter");
		assertTrue(repeatedSlashFilter < springSecurityFilterChain,
		    "RepeatedSlashFilter's <filter-mapping> comes after springSecurityFilterChain's");
	}

	/**
	 * @return whether the mapping applies to a request as the container received it, which is all a
	 *         mapping without a {@code <dispatcher>} applies to
	 */
	private static boolean appliesToRequests(Element mapping) {
		List<String> dispatchers = values(mapping, "dispatcher");
		return dispatchers.isEmpty() || dispatchers.contains("REQUEST");
	}

	/**
	 * @return the index of the first {@code <filter-mapping>} that matches, or -1 if none does
	 */
	private static int firstMapping(Predicate<Element> matching) {
		for (int i = 0; i < filterMappings.size(); i++) {
			if (matching.test(filterMappings.get(i))) {
				return i;
			}
		}
		return -1;
	}

	private static List<Element> children(Element parent, String name) {
		List<Element> children = new ArrayList<>();
		for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child instanceof Element element && element.getTagName().equals(name)) {
				children.add(element);
			}
		}
		return children;
	}

	private static List<String> values(Element parent, String name) {
		List<String> values = new ArrayList<>();
		for (Element child : children(parent, name)) {
			values.add(child.getTextContent().trim());
		}
		return values;
	}
}
