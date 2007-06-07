package org.openmrs.web.controller.observation;

import java.util.List;
import java.util.Locale;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.MimeType;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.web.WebConstants;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class MimeTypeListController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());
    
	/**
	 * 
	 * Allows for Integers to be used as values in input tags.
	 *   Normally, only strings and lists are expected 
	 * 
	 * @see org.springframework.web.servlet.mvc.BaseCommandController#initBinder(javax.servlet.http.HttpServletRequest, org.springframework.web.bind.ServletRequestDataBinder)
	 */
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
		super.initBinder(request, binder);
        binder.registerCustomEditor(java.lang.Integer.class,
                new CustomNumberEditor(java.lang.Integer.class, true));
	}

	/**
	 * 
	 * The onSubmit function receives the form/command object that was modified
	 *   by the input form and saves it to the db
	 * 
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object obj, BindException errors) throws Exception {
		
		HttpSession httpSession = request.getSession();
		
		Locale locale = request.getLocale();
		String view = getFormView();
		if (Context.isAuthenticated()) {
			String[] mimeTypeList = request.getParameterValues("mimeTypeId");
			AdministrationService as = Context.getAdministrationService();
			ObsService os = Context.getObsService();
			
			String success = "";
			String error = "";
			
			MessageSourceAccessor msa = getMessageSourceAccessor();
			String deleted = msa.getMessage("general.deleted");
			String notDeleted = msa.getMessage("general.cannot.delete");
			for (String m : mimeTypeList) {
				try {
					as.deleteMimeType(os.getMimeType(Integer.valueOf(m)));
					if (!success.equals("")) success += "<br>";
					success += m + " " + deleted;
				}
				catch (APIException e) {
					log.warn("Error deleting mime type", e);
					if (!error.equals("")) error += "<br>";
					error += m + " " + notDeleted;
				}
			}
			
			view = getSuccessView();
			if (!success.equals(""))
				httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, success);
			if (!error.equals(""))
				httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, error);
		}
			
		return new ModelAndView(new RedirectView(view));
	}

	/**
	 * 
	 * This is called prior to displaying a form for the first time.  It tells Spring
	 *   the form/command object to load into the request
	 * 
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
    protected Object formBackingObject(HttpServletRequest request) throws ServletException {

    	HttpSession httpSession = request.getSession();
		
		
		//default empty Object
		List<MimeType> mimeTypeList = new Vector<MimeType>();
		
		//only fill the Object is the user has authenticated properly
		if (Context.isAuthenticated()) {
			ObsService os = Context.getObsService();
	    	mimeTypeList = os.getMimeTypes();
		}
    	
        return mimeTypeList;
    }
    
}