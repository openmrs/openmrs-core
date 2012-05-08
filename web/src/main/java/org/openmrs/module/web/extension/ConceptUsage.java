/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.web.extension;

/**
 * <p>
 * This class models a single instance of where a metadata type in
 * OpenMRS is using a particular concept, and all the usages of this
 * type are aggregated by a {@link ConceptUsageExtension}.
 * </p>
 * <p>
 * This merely has a label and href capable of generating a link to
 * the object. Note that the href should be relative to
 * http://openmrsserver/openmrs/ and only include the portion of
 * the link that would come after that prefix.
 * </p>
 * <pre>
 * ie. href = /admin/concepts/conceptDrug.form?drugId=4
 * </pre>
 */
public class ConceptUsage {
	
	private String shortLabel;
	
	private String href;
	
	public ConceptUsage() {
		
	}
	
	public ConceptUsage(String shortLabel, String href) {
		this.shortLabel = shortLabel;
		this.href = href;
	}
	
	public String getShortLabel() {
		return shortLabel;
	}
	
	public void setShortLabel(String shortLabel) {
		this.shortLabel = shortLabel;
	}
	
	public String getHref() {
		return href;
	}
	
	public void setHref(String href) {
		this.href = href;
	}
	
}
