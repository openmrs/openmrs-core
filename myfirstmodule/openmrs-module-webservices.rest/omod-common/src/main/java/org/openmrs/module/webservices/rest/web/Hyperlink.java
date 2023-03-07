/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A link to another resource
 */
@XStreamAlias("link")
public class Hyperlink {
	
	private String rel;
	
	private String uri;
	
	private transient String resourceAlias;
	
	public Hyperlink(String rel, String uri) {
		this.rel = rel;
		this.uri = uri;
	}
	
	/**
	 * @return the rel
	 */
	public String getRel() {
		return rel;
	}
	
	/**
	 * @param rel the rel to set
	 */
	public void setRel(String rel) {
		this.rel = rel;
	}
	
	/**
	 * @return the uri
	 */
	public String getUri() {
		return uri;
	}
	
	/**
	 * @param uri the uri to set
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}
	
	@JsonIgnore
	public String getResourceAlias() {
		return resourceAlias;
	}
	
	@JsonIgnore
	public void setResourceAlias(String resourceAlias) {
		this.resourceAlias = resourceAlias;
	}
	
}
