/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.provider;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.ProviderAttributeType;
import org.openmrs.api.APIException;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.customdatatype.CustomDatatypeUtil;
import org.openmrs.web.WebConstants;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Controller for editing visit attribute types.
 *
 * @since 1.9
 */
public class ProviderAttributeTypeFormController extends SimpleFormController {
	
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * The onSubmit function receives the form/command object that was modified by the input form
	 * and saves it to the db
	 *
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, java.lang.Object,
	 *      org.springframework.validation.BindException)
	 */
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object obj,
	        BindException errors) throws Exception {
		
		HttpSession httpSession = request.getSession();
		
		String view = getFormView();
		
		if (Context.isAuthenticated()) {
			ProviderAttributeType providerAttributeType = (ProviderAttributeType) obj;
			ProviderService providerService = Context.getProviderService();
			
			if (request.getParameter("save") != null) {
				providerService.saveProviderAttributeType(providerAttributeType);
				view = getSuccessView();
				httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "ProviderAttributeType.saved");
			}

			// if the user is retiring out the ProviderAttributeType
			else if (request.getParameter("retire") != null) {
				String retireReason = request.getParameter("retireReason");
				if (providerAttributeType.getProviderAttributeTypeId() != null && !(StringUtils.hasText(retireReason))) {
					errors.reject("retireReason", "general.retiredReason.empty");
					return showForm(request, response, errors);
				}
				
				providerService.retireProviderAttributeType(providerAttributeType, retireReason);
				httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "ProviderAttributeType.retiredSuccessfully");
				
				view = getSuccessView();
			}

			// if the user is purging the providerAttributeType
			else if (request.getParameter("purge") != null) {
				
				try {
					providerService.purgeProviderAttributeType(providerAttributeType);
					httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "ProviderAttributeType.purgedSuccessfully");
					view = getSuccessView();
				}
				catch (DataIntegrityViolationException e) {
					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "error.object.inuse.cannot.purge");
					view = "providerAttributeType.form?providerAttributeTypeId="
					        + providerAttributeType.getProviderAttributeTypeId();
				}
				catch (APIException e) {
					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "error.general: " + e.getLocalizedMessage());
					view = "providerAttributeType.form?providerAttributeTypeId="
					        + providerAttributeType.getProviderAttributeTypeId();
				}
			} else if (request.getParameter("unretire") != null) {
				try {
					providerService.unretireProviderAttributeType(providerAttributeType);
					httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "ProviderAttributeType.unretiredSuccessfully");
					view = getSuccessView();
				}
				catch (APIException e) {
					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "error.general: " + e.getLocalizedMessage());
					view = "providerAttributeType.form?providerAttributeTypeId="
					        + providerAttributeType.getProviderAttributeTypeId();
				}
			}
		}
		
		return new ModelAndView(new RedirectView(view));
	}
	
	/**
	 * This is called prior to displaying a form for the first time. It tells Spring the
	 * form/command object to load into the request
	 *
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
	protected Object formBackingObject(HttpServletRequest request) throws ServletException {
		
		ProviderAttributeType providerAttributeType = null;
		
		if (Context.isAuthenticated()) {
			ProviderService os = Context.getProviderService();
			String providerAttributeTypeId = request.getParameter("providerAttributeTypeId");
			if (providerAttributeTypeId != null) {
				providerAttributeType = os.getProviderAttributeType(Integer.valueOf(providerAttributeTypeId));
			}
		}
		
		if (providerAttributeType == null) {
			providerAttributeType = new ProviderAttributeType();
		}
		
		return providerAttributeType;
	}
	
	/**
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest,
	 *      java.lang.Object, org.springframework.validation.Errors)
	 */
	protected Map<String, Object> referenceData(HttpServletRequest request, Object obj, Errors errors) throws Exception {
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("datatypes", CustomDatatypeUtil.getDatatypeClassnames());
		map.put("handlers", CustomDatatypeUtil.getHandlerClassnames());
		
		return map;
	}
}
