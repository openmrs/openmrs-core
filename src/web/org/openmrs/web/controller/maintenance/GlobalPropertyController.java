package org.openmrs.web.controller.maintenance;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.GlobalProperty;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.reporting.Report;
import org.openmrs.reporting.ReportService;
import org.openmrs.web.WebConstants;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.validation.BindException;
import org.springframework.web.bind.RequestUtils;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class GlobalPropertyController extends SimpleFormController {
	
	public static final String PROP_PREFIX = "global.";
	public static final String PROP_NEW_PREFIX = "global_new.";
	
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
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		Locale locale = request.getLocale();
		String view = getFormView();
		if (context != null && context.isAuthenticated()) {
			String[] deleteList = request.getParameterValues("propDelete");
			
			Map<String,String> globals = new HashMap<String,String>();
			Map<String,String> newGlobals = new HashMap<String,String>();
			if ( request.getParameterNames() != null ) {
				for ( Enumeration eParams = request.getParameterNames(); eParams.hasMoreElements(); ) {
					String paramName = (String)eParams.nextElement();
					if ( paramName.startsWith(GlobalPropertyController.PROP_PREFIX)) {
						String paramValue = (String)request.getParameter(paramName);
						globals.put(paramName.substring(GlobalPropertyController.PROP_PREFIX.length()), paramValue);
					} else if ( paramName.startsWith(GlobalPropertyController.PROP_NEW_PREFIX)) {
						String paramValue = (String)request.getParameter(paramName);
						newGlobals.put(paramName.substring(GlobalPropertyController.PROP_NEW_PREFIX.length()), paramValue);
					}
				}
			}

			AdministrationService as = context.getAdministrationService();
			
			String success = "";
			String error = "";
			
			MessageSourceAccessor msa = getMessageSourceAccessor();

			if ( globals.size() > 0 ) {
				for ( Map.Entry<String,String> e : globals.entrySet() ) {
					as.setGlobalProperty(e.getKey(), e.getValue());
				}
			}
			
			if ( newGlobals.size() > 0 ) {
				for ( Map.Entry<String,String> e : newGlobals.entrySet() ) {
					as.addGlobalProperty(e.getKey(), e.getValue());
				}
			}

			if ( deleteList != null ) {
				for (String p : deleteList ) {

					try {
						as.deleteGlobalProperty(p);
						if (!success.equals("")) success += "<br>";
						Object[] args = {p};
						success += msa.getMessage("GlobalProperty.property.deleted", args);
					}
					catch (APIException e) {
						log.warn(e);
						if (!error.equals("")) error += "<br>";
						Object[] args = {p};
						success += msa.getMessage("GlobalProperty.property.notDeleted", args);
					}
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
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		//default empty Object
		List<GlobalProperty> globalPropList = new ArrayList<GlobalProperty>();
		
		//only fill the Object is the user has authenticated properly
		if (context != null && context.isAuthenticated()) {
			AdministrationService as = context.getAdministrationService();
			globalPropList = as.getGlobalProperties();
		}
    	
        return globalPropList;
    }
    
}