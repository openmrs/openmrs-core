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
package org.openmrs.notification;

import java.io.Serializable;
import java.util.Map;

import org.openmrs.BaseOpenmrsObject;

public class Template extends BaseOpenmrsObject implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1782906754736853557L;
	
	// Persisted
	private Integer id;
	
	private String name;
	
	private String template;
	
	private Integer ordinal;
	
	private String sender;
	
	private String recipients;
	
	private String subject;
	
	// Not persisted
	@SuppressWarnings("unchecked")
	private Map data;
	
	private String content;
	
	public Template() {
	}
	
	public Template(int id, String name, String template) {
		this.id = id;
		this.name = name;
		this.template = template;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public Integer getId() {
		return id;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setTemplate(String template) {
		this.template = template;
	}
	
	public String getTemplate() {
		return template;
	}
	
	public void setRecipients(String recipients) {
		this.recipients = recipients;
	}
	
	public String getRecipients() {
		return this.recipients;
	}
	
	public void setSender(String sender) {
		this.sender = sender;
	}
	
	public String getSender() {
		return this.sender;
	}
	
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	public String getSubject() {
		return this.subject;
	}
	
	public void setOrdinal(Integer ordinal) {
		this.ordinal = ordinal;
	}
	
	public Integer getOrdinal() {
		return ordinal;
	}
	
	@SuppressWarnings("unchecked")
	public void setData(Map data) {
		this.data = data;
	}
	
	@SuppressWarnings("unchecked")
	public Map getData() {
		return this.data;
	}
	
	public String getContent() {
		return this.content;
	}
	
	public void setContent(String content) {
		this.content = content;
	}
	
}
