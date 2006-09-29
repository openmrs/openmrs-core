package org.openmrs.formentry.controller;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.formentry.FormEntryQueue;
import org.openmrs.formentry.FormEntryService;
import org.springframework.web.servlet.mvc.SimpleFormController;

public class FormEntryQueueFormController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());

	/**
	 * 
	 * This is called prior to displaying a form for the first time.  It tells Spring
	 *   the form/command object to load into the request
	 * 
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
    protected Object formBackingObject(HttpServletRequest request) throws ServletException {
    	FormEntryQueue q = null;
		
		if (Context.isAuthenticated()) {
			FormEntryService fs = Context.getFormEntryService();
			String id = request.getParameter("formEntryQueueId");
	    	if (id != null)
	    		q = fs.getFormEntryQueue(new Integer(id));	
		}
		
		if (q == null)
			q = new FormEntryQueue();
    	
        return q;
    }
    
}