package org.openmrs.notification.mail;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.domain.Message;
import org.openmrs.notification.MessageSender;
import javax.mail.internet.MimeMessage;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.Address;
import javax.mail.internet.InternetAddress;
import javax.naming.Context;
import javax.naming.InitialContext;

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
	public void setSession(Session session) { 
		this.session = session;
	}
	
	public void send(Message message) throws Exception {
		
		log.info("Trying to send message " + message);
		try { 
				
			MimeMessage mimeMessage = getMimeMessage( message );
			Transport.send(mimeMessage);		
			log.info("Message sent " + mimeMessage.getContent() );
		
		} catch(Exception e) { 
			e.printStackTrace();		
		}
		
	}	
	
	
	public MimeMessage getMimeMessage( Message message ) throws Exception {
		
		MimeMessage mimeMessage = new MimeMessage(session);
		mimeMessage.setSender( new InternetAddress( message.getSender() ) );
		mimeMessage.setRecipients( javax.mail.Message.RecipientType.TO, InternetAddress.parse( message.getRecipients(), false ));
		mimeMessage.setSubject( message.getSubject() );
		mimeMessage.setContent( message.getContent(), "text/plain");
		return mimeMessage;
	}
	
}