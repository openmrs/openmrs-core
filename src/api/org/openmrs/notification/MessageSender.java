package org.openmrs.notification;

import org.openmrs.domain.Message;

public interface MessageSender { 
	public void send(Message message) throws Exception; 
} 