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
import org.openmrs.Group;
import org.openmrs.Role;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.WebConstants;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class GroupFormController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());

	/**
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#processFormSubmission(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object obj, BindException errors) throws Exception {
		
		Group group = (Group)obj;
		log.debug("Editing Group: " + group.getGroup());
		String[] roles = request.getParameterValues("roles");
		Set roleObjs = new HashSet();
		if (roles != null) {
			for(String r : roles) {
				roleObjs.add(new Role(r));
			}
		}
		group.setRoles(roleObjs);
		
		return super.processFormSubmission(request, response, group, errors);
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
			Group group = (Group)obj;
			AdministrationService as = context.getAdministrationService();
			
			String newGroupName = request.getParameter("new_group"); 
			if (newGroupName != null && !newGroupName.equals(group.getGroup())) {
				as.renameGroup(group, newGroupName);
			}
			
			context.getAdministrationService().updateGroup(group);
			view = getSuccessView();
			httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Group.saved");
		}
		
		return new ModelAndView(new RedirectView(view));
	}

	protected Map referenceData(HttpServletRequest request) throws Exception {
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		List<Role> roles = context.getUserService().getRoles();
		
		for (String s : OpenmrsConstants.AUTO_ROLES()) {
			Role r = new Role(s);
			roles.remove(r);
		}
		
		if (context != null && context.isAuthenticated()) {
			map.put("roles", roles);
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
		
		Group group = null;
		
		if (context != null && context.isAuthenticated()) {
			UserService us = context.getUserService();
			String r = request.getParameter("group");
	    	if (r != null)
	    		group = us.getGroup(r);	
		}
		
		if (group == null)
			group = new Group();
    	
        return group;
    }
    
}