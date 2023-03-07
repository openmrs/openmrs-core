/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest;

import java.util.Comparator;
import java.util.Map;

import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

public class OpenmrsPathMatcher implements PathMatcher {
	
	private final AntPathMatcher delegate = new AntPathMatcher();
	
	@Override
	public boolean isPattern(String path) {
		return this.delegate.isPattern(path);
	}
	
	@Override
	public boolean match(String pattern, String path) {
		return this.delegate.match(pattern, path);
	}
	
	@Override
	public boolean matchStart(String pattern, String path) {
		return this.delegate.matchStart(pattern, path);
	}
	
	@Override
	public String extractPathWithinPattern(String pattern, String path) {
		return this.delegate.extractPathWithinPattern(pattern, path);
	}
	
	@Override
	public Map<String, String> extractUriTemplateVariables(String pattern, String path) {
		//see RESTWS-606
		if (path != null && pattern != null && path.startsWith("/rest/") && path.contains(".") && !path.endsWith("/")
		        && pattern.endsWith(".*")) {
			//a pattern like /rest/v1/{resource}/{uuid}.* needs to be replaced with /rest/v1/{resource}/{uuid}
			//in order to match a path like /rest/v1/systemsetting/concept.defaultConceptMapType
			pattern = pattern.replace(".*", "");
		}
		return this.delegate.extractUriTemplateVariables(pattern, path);
	}
	
	@Override
	public Comparator<String> getPatternComparator(final String path) {
		return delegate.getPatternComparator(path);
	}
	
	@Override
	public String combine(String pattern1, String pattern2) {
		return this.delegate.combine(pattern1, pattern2);
	}
}
