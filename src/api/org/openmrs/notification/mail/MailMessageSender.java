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
package org.openmrs.notification.mail;

import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.notification.Message;
import org.openmrs.notification.MessageException;
import org.openmrs.notification.MessageSender;

public class MailMessageSender implements MessageSender { 
	
	protected static final Log log = LogFactory.getLog( MailMessageSender.class );
	
	/**
	 * JavaMail session 
	 */
	private Session session;
	
	/**
	 * Default public constructor.
	 */
	public MailMessageSender() { }

	
	/**
	 * Public constructor.
	 * 
	 * @param session
	 */
	public MailMessageSender(Session session) { 
		this.session = session;
	}

	
	/**
	 * Set javamail session.
	 * 
	 * @param session
	 */
	public void setMailSession(Session session) { 
		this.session = session;
	}
	
	/**
	 * Send the message.
	 * 
	 * @param	message 	the message to be sent
	 */
	public void send(Message message) throws MessageException {
		try { 
			MimeMessage mimeMessage = createMimeMessage( message );
			Transport.send(mimeMessage);					
		} 
		catch (Exception e) {
			log.error("failed to send message", e);

			// catch mail-specific exception and re-throw it as app-specific exception
			throw new MessageException(e);
		}
	}	
	
	/**
	 *  Converts the message object to a mime message in order to prepare it to be sent.
	 *  
	 *   @param 	message
	 *   @return	MimeMessage 	
	 */
	public MimeMessage createMimeMessage( Message message ) throws Exception {

		if ( message.getRecipients() == null ) 
			throw new MessageException("Message must contain at least one recipient");
		
		MimeMessage mimeMessage = new MimeMessage(session);
		
		// TODO Need to test the null case.  
		// Transport should use default mail.from value defined in properties.
		if ( message.getSender() != null )
			mimeMessage.setSender( new InternetAddress( message.getSender() ) );
		
		mimeMessage.setRecipients( javax.mail.Message.RecipientType.TO, 
				InternetAddress.parse( message.getRecipients(), false ));
		mimeMessage.setSubject( message.getSubject() );
		
		// TODO	There should be a default and preference specified somewhere
		mimeMessage.setContent( message.getContent(), "text/plain");
		return mimeMessage;
	}
	
}