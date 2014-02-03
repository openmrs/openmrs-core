/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
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
