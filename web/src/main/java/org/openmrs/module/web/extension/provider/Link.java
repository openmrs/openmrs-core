/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.web.extension.provider;

import java.util.Map;

/**
 *
 */
public class Link {
	
	private String label;
	
	private String description;
	
	private String url;
	
	private Map<String, String> queryParameters;
	
	private Boolean strike;
	
	public Link() {
	}
	
	/**
	 * @since 1.10 
	 */
	public Link(String label, String url) {
		this.label = label;
		this.url = url;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public Map<String, String> getQueryParameters() {
		return queryParameters;
	}
	
	public void setQueryParameters(Map<String, String> queryParameters) {
		this.queryParameters = queryParameters;
	}
	
	/**
	 * @since 1.10 
	 */
	public Boolean getStrike() {
		return strike;
	}
	
	/**
	 * @since 1.10 
	 */
	public void setStrike(Boolean strike) {
		this.strike = strike;
	}
	
}
