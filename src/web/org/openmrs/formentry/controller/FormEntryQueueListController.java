package org.openmrs.formentry.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.formentry.FormEntryQueue;
import org.openmrs.formentry.FormEntryService;
import org.openmrs.web.WebConstants;
import org.springframework.web.servlet.mvc.SimpleFormController;

public class FormEntryQueueListController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());

    protected Object formBackingObject(HttpServletRequest request) throws ServletException {

    	HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		//default empty Object
		List<FormEntryQueue> queueList = new Vector<FormEntryQueue>();
		
		//only fill the Object is the user has authenticated properly
		if (context != null && context.isAuthenticated()) {
			FormEntryService fs = context.getFormEntryService();
	    	return fs.getFormEntryQueues();
		}
    	
        return queueList;
    }

	protected Map referenceData(HttpServletRequest request) throws Exception {
		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		//default empty Objects
		Integer queueSize = 0;
		Integer archiveSize = 0;
		Integer errorSize = 0;
		
		//only fill the Objects if the user has authenticated properly
		if (context != null && context.isAuthenticated()) {
			FormEntryService fs = context.getFormEntryService();
	    	queueSize = fs.getFormEntryQueueSize();
	    	archiveSize = fs.getFormEntryArchiveSize();
	    	errorSize = fs.getFormEntryErrorSize();
		}
    	
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("queueSize", queueSize);
		map.put("archiveSize", archiveSize);
		map.put("errorSize", errorSize);
		
        return map;
	}
    
    
    
}