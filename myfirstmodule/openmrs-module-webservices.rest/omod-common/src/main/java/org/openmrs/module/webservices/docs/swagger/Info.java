/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.docs.swagger;

import com.fasterxml.jackson.annotation.JsonGetter;
import org.codehaus.jackson.annotate.JsonProperty;

/* The object provides metadata about the API.*/
public class Info {
	
	private String version;
	
	private String title;
	
	private String description;
	
	private Contact contact;
	
	private License license;
	
	//An object to hold data about platform and module versions
	@JsonProperty("x-versions")
	private Versions versions;
	
	public Info() {
		
	}
	
	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}
	
	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}
	
	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}
	
	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	
	/**
	 * @return the contact
	 */
	public Contact getContact() {
		return contact;
	}
	
	/**
	 * @param contact the contact to set
	 */
	public void setContact(Contact contact) {
		this.contact = contact;
	}
	
	/**
	 * @return the license
	 */
	public License getLicense() {
		return license;
	}
	
	/**
	 * @param license the license to set
	 */
	public void setLicense(License license) {
		this.license = license;
	}
	
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	@JsonGetter("x-versions")
	public Versions getVersions() {
		return versions;
	}
	
	public void setVersions(Versions versions) {
		this.versions = versions;
	}
}
