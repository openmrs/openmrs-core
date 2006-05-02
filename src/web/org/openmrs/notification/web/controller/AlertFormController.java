package org.openmrs.notification.web.controller;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Role;
import org.openmrs.api.context.Context;
import org.openmrs.notification.Alert;
import org.openmrs.notification.AlertService;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.WebConstants;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class AlertFormController extends SimpleFormController {

	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());

	SimpleDateFormat dateFormat;

	/**
	 * 
	 * Allows for Integers to be used as values in input tags. Normally, only
	 * strings and lists are expected
	 * 
	 * @see org.springframework.web.servlet.mvc.BaseCommandController#initBinder(javax.servlet.http.HttpServletRequest,
	 *      org.springframework.web.bind.ServletRequestDataBinder)
	 */
	protected void initBinder(HttpServletRequest request,
			ServletRequestDataBinder binder) throws Exception {
		super.initBinder(request, binder);
		Context context = (Context) request.getSession().getAttribute(
				WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);

		Locale locale = context.getLocale();
		NumberFormat nf = NumberFormat.getInstance(locale);
		dateFormat = new SimpleDateFormat(OpenmrsConstants
				.OPENMRS_LOCALE_DATE_PATTERNS().get(
						context.getLocale().toString().toLowerCase()), context
				.getLocale());

		// NumberFormat nf = NumberFormat.getInstance(new Locale("en_US"));
		binder.registerCustomEditor(java.lang.Integer.class,
				new CustomNumberEditor(java.lang.Integer.class, nf, true));
		binder.registerCustomEditor(java.util.Date.class, new CustomDateEditor(
				dateFormat, true, 10));

	}

	protected ModelAndView processFormSubmission(HttpServletRequest request,
			HttpServletResponse reponse, Object obj, BindException errors)
			throws Exception {

		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession
				.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);

		Alert alert = (Alert) obj;

		context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_USERS);
		try {
			if (context != null && context.isAuthenticated()) {
				String userId = request.getParameter("userId");
				if (userId != null && !userId.equals(""))
					alert.setUser(context.getUserService().getUser(
							Integer.valueOf(userId)));
				
				String role = request.getParameter("roleStr");
				if (role != null && !role.equals(""))
					alert.setRole(context.getUserService().getRole(role));

				if (alert.getRole() == null && alert.getUser() == null) {
					errors.rejectValue("user", "Alert.userOrRoleRequired");
				}
			}
		} finally {
			context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_USERS);
		}

		return super.processFormSubmission(request, reponse, alert, errors);
	}

	/**
	 * 
	 * The onSubmit function receives the form/command object that was modified
	 * by the input form and saves it to the db
	 * 
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, java.lang.Object,
	 *      org.springframework.validation.BindException)
	 */
	protected ModelAndView onSubmit(HttpServletRequest request,
			HttpServletResponse response, Object obj, BindException errors)
			throws Exception {

		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession
				.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		String view = getFormView();

		if (context != null && context.isAuthenticated()) {
			Alert alert = (Alert) obj;
			context.getAlertService().updateAlert(alert);
			view = getSuccessView();
			httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR,
					"Alert.saved");
		}

		return new ModelAndView(new RedirectView(view));
	}

	/**
	 * 
	 * This is called prior to displaying a form for the first time. It tells
	 * Spring the form/command object to load into the request
	 * 
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
	protected Object formBackingObject(HttpServletRequest request)
			throws Exception {

		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession
				.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);

		Alert alert = null;

		if (context != null && context.isAuthenticated()) {
			AlertService as = context.getAlertService();
			String a = request.getParameter("alertId");
			if (a != null)
				alert = as.getAlert(Integer.valueOf(a));
		}

		if (alert == null)
			alert = new Alert();

		return alert;
	}

	protected Map referenceData(HttpServletRequest request, Object object,
			Errors errors) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();

		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession
				.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);

		if (context != null && context.isAuthenticated()) {
			List<Role> allRoles = context.getUserService().getRoles();

			map.put("allRoles", allRoles);
		}

		return map;
	}

}