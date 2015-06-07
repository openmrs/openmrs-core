/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.encounter;

import org.apache.commons.beanutils.PropertyUtils;
import org.openmrs.LocationTag;
import org.openmrs.PersonAddress;
import org.openmrs.api.context.Context;
import org.openmrs.layout.address.AddressTemplate;
import org.openmrs.web.WebConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.context.request.WebRequest;

import java.util.List;

/**
 * Controller for managing {@link LocationTag}s
 */
@Controller
@SessionAttributes("addressTemplate")
public class AddressTemplateController {
	
	/**
	 * Show AddressTemplate
	 */
	@RequestMapping("/admin/locations/addressTemplate")
	public void show(ModelMap model) {
		model.addAttribute("addressTemplateXml", Context.getLocationService().getAddressTemplate());
	}
	
	/**
	 * Add a new AddressTemplate (quickly, without a dedicated page)
	 */
	@RequestMapping("/admin/locations/addressTemplateAdd")
	public String add(@RequestParam("xml") String xml, WebRequest request) {
		
		if (!StringUtils.hasText(xml) || "".equals(xml.trim())) {
			request.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, Context.getMessageSourceService().getMessage(
			    "AddressTemplate.error.empty"), WebRequest.SCOPE_SESSION);
		} else {
			try {
				//To test whether this is a valid conversion
				AddressTemplate test = Context.getSerializationService().getDefaultSerializer().deserialize(xml,
				    AddressTemplate.class);
				
				List<String> requiredElements = test.getRequiredElements();
				if (requiredElements != null) {
					for (String fieldName : requiredElements) {
						try {
							PropertyUtils.getProperty(new PersonAddress(), fieldName);
						}
						catch (Exception e) {
							//wrong field declared in template
							request.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, Context.getMessageSourceService()
							        .getMessage("AddressTemplate.error.fieldNotDeclaredInTemplate",
							            new Object[] { fieldName }, Context.getLocale()), WebRequest.SCOPE_SESSION);
							return "redirect:addressTemplate.form";
						}
					}
				}
				
				Context.getLocationService().saveAddressTemplate(xml);
				request.setAttribute(WebConstants.OPENMRS_MSG_ATTR, Context.getMessageSourceService().getMessage(
				    "AddressTemplate.saved"), WebRequest.SCOPE_SESSION);
			}
			catch (Exception e) {
				String errmsg1 = e.getCause().toString();
				
				if (errmsg1.contains("must be terminated by the matching")) {
					String errmsg2 = e.getCause().getCause().toString();
					
					request.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, Context.getMessageSourceService().getMessage(
					    "AddressTemplate.error.elementInvalid",
					    new Object[] { errmsg1.split("\"")[1], errmsg2.split(";")[1].split(":")[1] }, Context.getLocale()),
					    WebRequest.SCOPE_SESSION);
				} else if (errmsg1.split("\n")[0].endsWith("null")) {
					for (String part : errmsg1.split("\n")) {
						if (part.startsWith("path")) {
							request.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, Context.getMessageSourceService()
							        .getMessage("AddressTemplate.error.nameOrValueInvalid",
							            new Object[] { part.split(":")[1] }, Context.getLocale()), WebRequest.SCOPE_SESSION);
							break;
						}
					}
				} else if (errmsg1.contains("UnknownFieldException")
				        || errmsg1.contains("must be terminated by the matching")) {
					for (String part : errmsg1.split("\n")) {
						if (part.startsWith("path")) {
							request.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, Context.getMessageSourceService()
							        .getMessage("AddressTemplate.error.wrongFieldName",
							            new Object[] { part.split("/")[part.split("/").length - 1] }, Context.getLocale()),
							    WebRequest.SCOPE_SESSION);
							break;
						}
					}
				} else {
					request.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, Context.getMessageSourceService().getMessage(
					    "AddressTemplate.error"), WebRequest.SCOPE_SESSION);
				}
				
				request.setAttribute(WebConstants.OPENMRS_ADDR_TMPL, xml, WebRequest.SCOPE_SESSION);
			}
			
		}
		return "redirect:addressTemplate.form";
	}
	
}
