/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.web.extension;

import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.Extension;
import org.openmrs.module.web.extension.provider.Link;

/**
 * <p>
 * This class models an extension point that is used to show how a certain 
 * type of metadata in OpenMRS uses an existing Concept. This is initially
 * intended for pages that view a concept, where an extension could be 
 * something like Forms or Programs that use a concept, and the conceptUsage
 * member variable is the list of all such things (Forms or Programs for this
 * example) that are currently using the concept in OpenMRS.
 * </p>
 * <p>
 * While this class is geared towards openmrs jsp extension points (hence the
 * mediatype and requried privilege), it's hopeful that it can also be used
 * programmatically to generate reports and other things in the background.
 * </p>
 */
public class ConceptUsageExtension extends Extension {
	
	private String header = "";
	
	private List<Link> conceptUsage = new ArrayList<Link>();
	
	private MEDIA_TYPE mediaType;
	
	private String requiredPrivilege = "";
	
	public ConceptUsageExtension() {
		this.mediaType = Extension.MEDIA_TYPE.html;
	}
	
	public ConceptUsageExtension(String header, List<Link> conceptUsage) {
		this.header = header;
		this.conceptUsage = conceptUsage;
		this.mediaType = Extension.MEDIA_TYPE.html;
	}
	
	public ConceptUsageExtension(String header, List<Link> conceptUsage, String requiredPrivilege) {
		this.header = header;
		this.conceptUsage = conceptUsage;
		this.requiredPrivilege = requiredPrivilege;
		this.mediaType = Extension.MEDIA_TYPE.html;
	}
	
	public List<Link> getConceptUsage() {
		return conceptUsage;
	}
	
	@Override
	public MEDIA_TYPE getMediaType() {
		return mediaType;
	}
	
	public void setMediaType(MEDIA_TYPE mediaType) {
		this.mediaType = mediaType;
	}
	
	public String getHeader() {
		return header;
	}
	
	public void setHeader(String header) {
		this.header = header;
	}
	
	public void setConceptUsage(List<Link> conceptUsage) {
		this.conceptUsage = conceptUsage;
	}
	
	/**
	 * Returns the required privilege in order to see this section. Can be a comma delimited list of
	 * privileges. If the default empty string is returned, only an authenticated user is required
	 * 
	 * @return Privilege string
	 */
	public String getRequiredPrivilege() {
		return this.requiredPrivilege;
	}
	
	public void setRequiredPrivilege(String requiredPrivilege) {
		this.requiredPrivilege = requiredPrivilege;
	}
	
}
