package org.openmrs.notification;

public interface MessageSender { 
	public void send(Message message) throws MessageException; 
} 