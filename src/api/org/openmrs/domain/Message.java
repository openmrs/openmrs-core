package org.openmrs.domain;

import java.io.Serializable;
import java.util.Date;

public class Message implements Serializable {    

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5392076713513109152L;

	private Integer id;	
	private String recipients;    
	private String sender;
	private String subject;
	private String content;
	private Date sentDate;
	
	
	public Message() {  }
	
	public Message(Integer id, String recipients, String sender, String subject, String content) { 
		this.id = id;
		this.recipients = recipients;
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
