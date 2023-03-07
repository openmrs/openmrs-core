/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.test;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.Hyperlink;

import java.util.List;
import java.util.Map;

public class LinkMatcher {
	
	public static Matcher hasLink(final String rel, final String uriEndsWith) {
		return new BaseMatcher<Object>() {

			@Override
			public boolean matches(Object o) {
				List links = ((SimpleObject) o).get("links");
				for (Object candidate : links) {
					if (candidate instanceof Hyperlink) {
						Hyperlink link = (Hyperlink) candidate;
						if (link.getRel().equals(rel) && link.getUri().endsWith(uriEndsWith)) {
							return true;
						}
					} else {
						Map map = (Map) candidate;
						if (rel.equals(map.get("rel")) && ((String) map.get("uri")).endsWith(uriEndsWith)) {
							return true;
						}
					}
				}
				return false;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("representation with a link with rel=").appendValue(rel)
				        .appendText(" and uri ending with ").appendValue(uriEndsWith);
			}
		};
	}
}
