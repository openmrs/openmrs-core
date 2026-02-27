/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.ai;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Represents a response from an AI service to a clinical query about a patient's chart.
 *
 * @since 3.0.0
 */
public class AiChatResponse {
	
	private String content;
	
	private Date timestamp;
	
	private List<String> sourceReferences = new ArrayList<>();
	
	public AiChatResponse() {
		this.timestamp = new Date();
	}
	
	public AiChatResponse(String content) {
		this.content = content;
		this.timestamp = new Date();
	}
	
	/**
	 * @return the AI-generated response content
	 */
	public String getContent() {
		return content;
	}
	
	/**
	 * @param content the AI-generated response content
	 */
	public void setContent(String content) {
		this.content = content;
	}
	
	/**
	 * @return the timestamp when this response was generated
	 */
	public Date getTimestamp() {
		return timestamp;
	}
	
	/**
	 * @param timestamp the timestamp when this response was generated
	 */
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	
	/**
	 * Returns references to the clinical data sources used to generate this response.
	 * These are typically UUIDs of the observations, encounters, or other entities
	 * that the AI system used as evidence.
	 *
	 * @return the source references
	 */
	public List<String> getSourceReferences() {
		return sourceReferences;
	}
	
	/**
	 * @param sourceReferences references to clinical data sources used in this response
	 */
	public void setSourceReferences(List<String> sourceReferences) {
		this.sourceReferences = sourceReferences;
	}
}
