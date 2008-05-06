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
import java.util.Date;

public class Message implements Serializable {    

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5392076713513109152L;

	private Integer id;	
	private StringBuffer recipients = new StringBuffer();    
	private String sender;
	private String subject;
	private String content;
	private Date sentDate;
	
	
	public Message() {  
	}
	
	public Message(Integer id, String recipients, String sender, String subject, String content) { 
		this.id = id;
		this.recipients.append(recipients);
		this.sender = sender;
		this.subject = subject;
		this.content = content;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public Integer getId() { 
		return this.id;
	}

	public void setRecipients(String recipients) {  
		if (recipients != null)
			this.recipients = new StringBuffer(recipients);
	}    
	
	public String getRecipients() {        
		return this.recipients.toString();    
	}
	
	public void addRecipient(String recipient) { 
		if (recipient != null) {
			this.recipients.append(",").append( recipient );
		}
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

	public void setSentDate(Date sentDate) {        
		this.sentDate = sentDate;    
	}    
	
	public Date getSentDate() {        
		return this.sentDate;    
	}

	public void setContent(String content) { 
		this.content = content;
	}
	
	public String getContent() { 
		return this.content;
	}
	
}
