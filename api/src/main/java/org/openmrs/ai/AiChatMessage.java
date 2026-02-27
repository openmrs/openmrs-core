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

import java.util.Date;

/**
 * Represents a single message in an AI chat conversation about a patient's clinical data.
 *
 * @since 3.0.0
 */
public class AiChatMessage {
	
	/**
	 * The role of the message sender.
	 */
	public enum Role {
		/** The clinician asking a question */
		USER,
		/** The AI system responding */
		ASSISTANT,
		/** System-level instructions or context */
		SYSTEM
	}
	
	private Role role;
	
	private String content;
	
	private Date timestamp;
	
	public AiChatMessage() {
		this.timestamp = new Date();
	}
	
	public AiChatMessage(Role role, String content) {
		this.role = role;
		this.content = content;
		this.timestamp = new Date();
	}
	
	/**
	 * @return the role of the message sender
	 */
	public Role getRole() {
		return role;
	}
	
	/**
	 * @param role the role of the message sender
	 */
	public void setRole(Role role) {
		this.role = role;
	}
	
	/**
	 * @return the message content
	 */
	public String getContent() {
		return content;
	}
	
	/**
	 * @param content the message content
	 */
	public void setContent(String content) {
		this.content = content;
	}
	
	/**
	 * @return the timestamp when this message was created
	 */
	public Date getTimestamp() {
		return timestamp;
	}
	
	/**
	 * @param timestamp the timestamp when this message was created
	 */
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
}
