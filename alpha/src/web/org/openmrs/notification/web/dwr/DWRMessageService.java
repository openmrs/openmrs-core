package org.openmrs.notification.web.dwr;

import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.notification.MessageService;
import org.openmrs.notification.NotificationConstants;
import org.openmrs.web.WebConstants;

import uk.ltd.getahead.dwr.WebContextFactory;

public class DWRMessageService {

	protected final Log log = LogFactory.getLog(getClass());
	
	public boolean sendFeedback(String sender, String subject, String content) {

		Context context = (Context) WebContextFactory.get().getSession()
				.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);

		HttpServletRequest request = WebContextFactory.get()
				.getHttpServletRequest();

		if (context != null) {
			try {
				MessageService messageService = context.getMessageService();

				String recipients = NotificationConstants.FEEDBACK_EMAIL_ADDRESS;
				if (subject == null || subject.equals(""))
					subject = NotificationConstants.FEEDBACK_EMAIL_SUBJECT;

				String referer = request.getPathTranslated();
				String userName = "an Anonymous User";
				if (context.isAuthenticated())
					userName = context.getAuthenticatedUser().getFirstName()
							+ " "
							+ context.getAuthenticatedUser().getLastName();

				content += "\n\n This email sent from: " + referer + " by: "
						+ userName;

				messageService.send(recipients, sender, subject, content);

				return true;

			} catch (Exception e) {
				log.error("Error sending feedback", e);
			}
		}

		return false;
	}	
	
	public Vector sendMessage( String recipients, String sender, String subject, String content ) {

		// List to return
		// Object type gives ability to return error strings
		Vector<Object> objectList = new Vector<Object>();	

		Context context = (Context) WebContextFactory.get().getSession()
				.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		HttpServletRequest request = WebContextFactory.get().getHttpServletRequest();
		
		if (context == null) {
			objectList.add("Your session has expired.");
			objectList.add("Please <a href='" + request.getContextPath() + "/logout'>log in</a> again.");
		}
		else {
			try {
				MessageService messageService = context.getMessageService();
				messageService.send(recipients, sender, subject, content);
				objectList.add("Message has been sent successfully.");				
			} catch (Exception e) {
				log.error(e);
				objectList.add("Error while attempting to send message: " + e.getMessage());
			}
		}
		return objectList;
	}

}
