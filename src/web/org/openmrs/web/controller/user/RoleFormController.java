package org.openmrs.web.controller.user;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
import org.openmrs.api.APIException;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.WebConstants;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
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
		
		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		Role role = (Role)obj;
		
		if (context != null && context.isAuthenticated()) {
			log.debug("Editing Role: " + role.getRole());
			
			// retrieving the inheritedRoles from the request
			String[] inheritedRoles = request.getParameterValues("inheritedRoles");
			Set<Role> inheritedRoleObjs = new HashSet<Role>();
			if (inheritedRoles != null) {
				for(String r : inheritedRoles) {
					Role tmprole = context.getUserService().getRole(r);
					inheritedRoleObjs.add(tmprole);
				}
			}
			role.setInheritedRoles(inheritedRoleObjs);
	
			// retrieving the privileges from the request
			String[] privs = request.getParameterValues("privileges");
			Set<Privilege> privObjs = new HashSet<Privilege>();
			if (privs != null) {
				for(String p : privs) {
					privObjs.add(new Privilege(p));
				}
			}
			role.setPrivileges(privObjs);
		}
		
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
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		String view = getFormView();
		
		if (context != null && context.isAuthenticated()) {
			Role role = (Role)obj;
			try {
				context.getAdministrationService().updateRole(role);
				view = getSuccessView();
				httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Role.saved");
			}
			catch (APIException e) {
				errors.reject(e.getMessage());
				return showForm(request, response, errors);
			}
		}
		
		return new ModelAndView(new RedirectView(view));
	}

	protected Map referenceData(HttpServletRequest request, Object object, Errors errors) throws Exception {
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		Role role = (Role)object;
		
		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		if (context != null && context.isAuthenticated()) {
			List<Role> allRoles = context.getUserService().getRoles();
			Set<Role> inheritingRoles = new HashSet<Role>();
			allRoles.remove(role);
			for (Role r : allRoles) {
				if (r.getInheritedRoles().contains(role))
					inheritingRoles.add(r);
			}
			
			for (String s : OpenmrsConstants.AUTO_ROLES()) {
				Role r = context.getUserService().getRole(s);
				allRoles.remove(r);
			}
			
			map.put("allRoles", allRoles);
			map.put("inheritingRoles", inheritingRoles);
			map.put("privileges", context.getUserService().getPrivileges());
			map.put("superuser", OpenmrsConstants.SUPERUSER_ROLE);
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
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
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