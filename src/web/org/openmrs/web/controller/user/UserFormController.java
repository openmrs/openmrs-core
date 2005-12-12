package org.openmrs.web.controller.user;

import java.text.DateFormat;
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
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.web.Constants;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class UserFormController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());
    
	/**
	 * 
	 * Allows for other Objects to be used as values in input tags.
	 *   Normally, only strings and lists are expected 
	 * 
	 * @see org.springframework.web.servlet.mvc.BaseCommandController#initBinder(javax.servlet.http.HttpServletRequest, org.springframework.web.bind.ServletRequestDataBinder)
	 */
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
		super.initBinder(request, binder);
        //NumberFormat nf = NumberFormat.getInstance(request.getLocale());
        //binder.registerCustomEditor(java.lang.Integer.class,
        //        new CustomNumberEditor(java.lang.Integer.class, nf, true));
		binder.registerCustomEditor(java.lang.Integer.class, 
				new CustomNumberEditor(java.lang.Integer.class, true));
        binder.registerCustomEditor(java.util.Date.class, 
        		new CustomDateEditor(DateFormat.getDateInstance(DateFormat.SHORT), true));
	}

	/**
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#processFormSubmission(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object obj, BindException errors) throws Exception {
		
		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		User user = (User)obj;
		UserService us = context.getUserService();
		
		if (context != null && context.isAuthenticated()) {
			// check if username is already in the database
				if (us.isDuplicateUsername(user)) {
					errors.rejectValue("username", "error.username.taken");
				}
				
			// check if password and password confirm are identical
				String password = request.getParameter("password");
				if (password == null) password = "";
				String confirm = request.getParameter("confirm");
				if (confirm == null) confirm = "";
				
				if (!password.equals(confirm))
					errors.reject("error.password.match");
				
				if (password.equals("") && user.getUserId() == null)
					errors.reject("error.password.weak");
					
			// check strength of password?
				
			// add Roles to user (because spring can't handle lists as properties...)
				String[] roles = request.getParameterValues("roles");
				Set<Role> set = new HashSet<Role>();
				if (roles != null) {
					for (String role : roles) {
						set.add(us.getRole(role));
					}
				}
				user.setRoles(set);
		}
				
		return super.processFormSubmission(request, response, obj, errors);
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
		User user = (User)obj;
		String view = getFormView();
		
		if (context != null && context.isAuthenticated()) {
			
			boolean isNew = (user.getUserId() == null);
			
			view = getSuccessView();
			if (isNew)
				context.getUserService().createUser(user, request.getParameter("password"));
			else
				context.getUserService().updateUser(user);
			
			httpSession.setAttribute(Constants.OPENMRS_MSG_ATTR, "User.saved");
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
		Context context = (Context) httpSession.getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		User user = null;
		
		if (context != null && context.isAuthenticated()) {
			UserService us = context.getUserService();
			String userId = request.getParameter("userId");
	    	if (userId != null)
	    		user = us.getUser(Integer.valueOf(userId));
		}
		
		if (user == null)
			user = new User();
		
        return user;
    }
    
    protected Map referenceData(HttpServletRequest request) throws Exception {
		
		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		Map<String, Object> map = new HashMap<String, Object>();
		
		if (context != null && context.isAuthenticated()) {
			map.put("roles", context.getUserService().getRoles());
		}	
		return map;
    }
    
}