package org.openmrs.web.controller.user;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Privilege;
import org.openmrs.Role;
import org.openmrs.api.db.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.web.Constants;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class RoleFormController extends SimpleFormController {
	
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
        //NumberFormat nf = NumberFormat.getInstance(new Locale("en_US"));
        binder.registerCustomEditor(java.lang.Integer.class,
                new CustomNumberEditor(java.lang.Integer.class, true));
	}

	/**
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#processFormSubmission(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object obj, BindException errors) throws Exception {
		
		Role role = (Role)obj;
		log.debug("Editing Role: " + role.getRole());
		String[] privs = request.getParameterValues("privileges");
		Set privObjs = new HashSet();
		if (privs != null) {
			for(String p : privs) {
				privObjs.add(new Privilege(p));
			}
		}
		role.setPrivileges(privObjs);
		
		return super.processFormSubmission(request, response, role, errors);
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
		Context context = (Context) httpSession.getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		String view = getFormView();
		
		if (context != null && context.isAuthenticated()) {
			Role role = (Role)obj;
			context.getAdministrationService().updateRole(role);
			view = getSuccessView();
			httpSession.setAttribute(Constants.OPENMRS_MSG_ATTR, "Role.saved");
		}
		
		return new ModelAndView(new RedirectView(view));
	}

	protected Map referenceData(HttpServletRequest request) throws Exception {
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		if (context != null && context.isAuthenticated()) {
			map.put("privileges", context.getUserService().getPrivileges());
		}
		
		return map;
		
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
		Context context = (Context) httpSession.getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		Role role = null;
		
		if (context != null && context.isAuthenticated()) {
			UserService us = context.getUserService();
			String r = request.getParameter("role");
	    	if (r != null)
	    		role = us.getRole(r);	
		}
		
		if (role == null)
			role = new Role();
    	
        return role;
    }
    
}