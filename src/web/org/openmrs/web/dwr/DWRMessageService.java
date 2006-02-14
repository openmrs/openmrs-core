package org.openmrs.web.dwr;

import java.util.List;
import java.util.Locale;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.openmrs.api.MessageService;
import org.openmrs.api.context.Context;
import org.openmrs.web.WebConstants;
import org.openmrs.domain.Template;
import org.openmrs.domain.Message;

import uk.ltd.getahead.dwr.WebContextFactory;

public class DWRMessageService {

	protected final Log log = LogFactory.getLog(getClass());
	
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
			Locale locale = context.getLocale();
			try {
				MessageService messageService = context.getMessageService();
				messageService.send(recipients, sender, subject, content);
				objectList.add("Message has been sent successfully.");
				
			} catch (Exception e) {
				log.error(e);
				e.printStackTrace();
				objectList.add("Error while attempting to find encounter");
			}
		}
		return objectList;
	}

}
