package org.openmrs.web.controller;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.PatientService;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.web.Constants;
import org.openmrs.web.OptionsForm;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.servlet.view.RedirectView;

public class OptionsFormController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());

	/**
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#processFormSubmission(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object object, BindException errors) throws Exception {
	
		//HttpSession httpSession = request.getSession();
		//Context context = (Context) httpSession.getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		OptionsForm opts = (OptionsForm)object;
		
		if (!opts.getOldPassword().equals("")) {
			if (opts.getNewPassword().equals(""))
				errors.rejectValue("newPassword", "error.password.weak");
			else if (!opts.getNewPassword().equals(opts.getConfirmPassword())) {
				errors.rejectValue("newPassword", "error.password.match");
				errors.rejectValue("confirmPassword", "error.password.match");
			}
		}

		if (!opts.getSecretQuestionPassword().equals("")) {
			if (!opts.getSecretAnswerConfirm().equals(opts.getSecretAnswerNew())) {
				errors.rejectValue("secretAnswerNew", "error.options.secretAnswer.match");
				errors.rejectValue("secretAnswerConfirm", "error.options.secretAnswer.match");
			}
		}
		
		// TODO catch errors
		
		
		return super.processFormSubmission(request, response, object, errors); 
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
		
		if (context == null || !context.isAuthenticated())
			errors.rejectValue("opts", "auth.session.expired");

		User user = context.getAuthenticatedUser();
		UserService us = context.getUserService();
		OptionsForm opts = (OptionsForm)obj;
		
		if (!opts.getOldPassword().equals("")) {
			try {
				us.changePassword(opts.getOldPassword(), opts.getNewPassword());
			}
			catch (APIException e) {
				errors.rejectValue("oldPassword", "error.password.match");
			}
		}
		
		if (!opts.getSecretQuestionPassword().equals("") && !errors.hasErrors()) {
			try {
				us.changeQuestionAnswer(opts.getSecretQuestionPassword(), opts.getSecretQuestionNew(), opts.getSecretAnswerNew());
			}
			catch (APIException e) {
				errors.rejectValue("secretQuestionPassword", "error.password.match");
			}
		}
		
		if (!errors.hasErrors()) {
			httpSession.setAttribute(Constants.OPENMRS_MSG_ATTR, "options.saved");
		}
		
		view = getSuccessView();
		return new ModelAndView(new RedirectView(view));
	}

	/**
	 * This is called prior to displaying a form for the first time.  It tells Spring
	 *   the form/command object to load into the request
	 * 
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
    protected Object formBackingObject(HttpServletRequest request) throws ServletException {

		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		OptionsForm opts = new OptionsForm();
		
		if (context != null && context.isAuthenticated()) {
			UserService us = context.getUserService();
			User user = context.getAuthenticatedUser();

			// TODO - add user services 
			//opts.setDefaultLocation(us.getDefaultLocation());
			//opts.setDefaultLanguage(us.getDefaultLanguage());
			
		}
		
		return opts;
    }
    
	/**
	 * 
	 * Called prior to form display.  Allows for data to be put 
	 * 	in the request to be used in the view
	 * 
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest)
	 */
	protected Map referenceData(HttpServletRequest request) throws Exception {
		
		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		Locale locale = RequestContextUtils.getLocale(request);
		Map<String, Object> map = new HashMap<String, Object>();
		
		if (context != null && context.isAuthenticated()) {
			
			PatientService ps = context.getPatientService();
			
			// set location options
			map.put("locations", ps.getLocations());
			
	    	// make spring locale available to jsp
			map.put("locale", locale.getLanguage());
			
		}
		
		return map;
	} 
}