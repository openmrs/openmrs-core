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
import org.openmrs.util.OpenmrsConstants;

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
		log.debug("Message content type : " + OpenmrsConstants.DEFAULT_CONTENT_TYPE);		
		mimeMessage.setContent( message.getContent(), OpenmrsConstants.DEFAULT_CONTENT_TYPE);		
		return mimeMessage;
	}
	
}