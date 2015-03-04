/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.location;

import java.util.Collection;

import org.openmrs.LocationAttributeType;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.customdatatype.CustomDatatypeUtil;
import org.openmrs.validator.LocationAttributeTypeValidator;
import org.openmrs.web.WebConstants;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;

/**
 * Controller for creating/editing a location attribute type.
 *
 * @since 1.9
 */
@Controller
public class LocationAttributeTypeFormController {
	
	@ModelAttribute("datatypes")
	public Collection<String> getDatatypes() {
		return CustomDatatypeUtil.getDatatypeClassnames();
	}
	
	@ModelAttribute("handlers")
	public Collection<String> getHandlers() {
		return CustomDatatypeUtil.getHandlerClassnames();
	}
	
	/**
	 * Put existing or newly-instantiated attribute type in the model
	 */
	@ModelAttribute("attributeType")
	public LocationAttributeType formBackingObject(
	        @RequestParam(value = "id", required = false) LocationAttributeType attrType) {
		if (attrType == null) {
			attrType = new LocationAttributeType();
		}
		return attrType;
	}
	
	/**
	 * Show existing (or instantiate blank)
	 */
	@RequestMapping(value = "/admin/locations/locationAttributeType", method = RequestMethod.GET)
	public void showForm() {
	}
	
	/**
	 * Handle submission for create or edit
	 */
	@RequestMapping(value = "/admin/locations/locationAttributeType", method = RequestMethod.POST)
	public String handleSubmit(WebRequest request, @ModelAttribute("attributeType") LocationAttributeType attributeType,
	        BindingResult errors) {
		
		LocationService service = Context.getLocationService();
		
		if (request.getParameter("purge") != null) {
			try {
				service.purgeLocationAttributeType(attributeType);
				request.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "LocationAttributeType.purgedSuccessfully",
				    WebRequest.SCOPE_SESSION);
			}
			catch (Exception e) {
				request.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "error.object.inuse.cannot.purge",
				    WebRequest.SCOPE_SESSION);
			}
			return "redirect:locationAttributeTypes.list";
		}
		
		new LocationAttributeTypeValidator().validate(attributeType, errors);
		
		if (errors.hasErrors()) {
			return null; // redisplay the form
			
		} else {
			
			if (request.getParameter("save") != null) {
				Context.getLocationService().saveLocationAttributeType(attributeType);
				request.setAttribute(WebConstants.OPENMRS_MSG_ATTR, Context.getMessageSourceService().getMessage(
				    "LocationAttributeType.saved"), WebRequest.SCOPE_SESSION);
			} else if (request.getParameter("retire") != null) {
				String retireReason = request.getParameter("retireReason");
				if (attributeType.getId() != null && !(StringUtils.hasText(retireReason))) {
					errors.reject("retireReason", "general.retiredReason.empty");
					return null;
				}
				Context.getLocationService().retireLocationAttributeType(attributeType, retireReason);
				request.setAttribute(WebConstants.OPENMRS_MSG_ATTR, Context.getMessageSourceService().getMessage(
				    "LocationAttributeType.retired"), WebRequest.SCOPE_SESSION);
			} else if (request.getParameter("unretire") != null) {
				Context.getLocationService().unretireLocationAttributeType(attributeType);
				request.setAttribute(WebConstants.OPENMRS_MSG_ATTR, Context.getMessageSourceService().getMessage(
				    "LocationAttributeType.unretired"), WebRequest.SCOPE_SESSION);
			}
			return "redirect:/admin/locations/locationAttributeTypes.list";
		}
	}
	
}
