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
package org.openmrs.web.controller.provider;

import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Provider;
import org.openmrs.ProviderAttribute;
import org.openmrs.ProviderAttributeType;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.propertyeditor.PersonEditor;
import org.openmrs.validator.ProviderValidator;
import org.openmrs.web.WebConstants;
import org.openmrs.web.attribute.WebAttributeUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/provider/provider.form")
public class ProviderFormController {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	@InitBinder
	protected void initBinder(WebDataBinder binder) throws Exception {
		binder.registerCustomEditor(org.openmrs.Person.class, new PersonEditor());
	}
	
	@RequestMapping(method = RequestMethod.POST)
	public String onSubmit(HttpServletRequest request, @RequestParam(required = false) String saveProviderButton,
	        @RequestParam(required = false) String retireProviderButton,
	        @RequestParam(required = false) String unretireProviderButton,
	        @RequestParam(required = false) boolean linkToPerson, @ModelAttribute("provider") Provider provider,
	        BindingResult errors, ModelMap model) throws Exception {
		
		if (saveProviderButton != null) {
			//For existing providers, switch between linking to person or use name
			if (provider.getProviderId() != null) {
				if (linkToPerson) {
					provider.setName(null);
				} else {
					provider.setPerson(null);
				}
			}
		}
		
		// manually handle the attribute parameters
		List<ProviderAttributeType> attributeTypes = (List<ProviderAttributeType>) model.get("providerAttributeTypes");
		WebAttributeUtil
		        .handleSubmittedAttributesForType(provider, errors, ProviderAttribute.class, request, attributeTypes);
		
		new ProviderValidator().validate(provider, errors);
		
		if (!errors.hasErrors()) {
			if (Context.isAuthenticated()) {
				ProviderService service = Context.getProviderService();
				
				String message = "Provider.saved";
				if (saveProviderButton != null) {
					service.saveProvider(provider);
				} else if (retireProviderButton != null) {
					service.retireProvider(provider, provider.getRetireReason());
					message = "Provider.retired";
				} else if (unretireProviderButton != null) {
					service.unretireProvider(provider);
					message = "Provider.unretired";
				}
				
				request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, message);
				return "redirect:index.htm";
			}
		}
		
		return showForm();
	}
	
	@ModelAttribute("provider")
	public Provider formBackingObject(@RequestParam(required = false) Integer providerId) throws ServletException {
		Provider provider = new Provider();
		if (Context.isAuthenticated()) {
			if (providerId != null) {
				ProviderService ps = Context.getProviderService();
				return ps.getProvider(providerId);
			}
		}
		return provider;
	}
	
	@ModelAttribute("providerAttributeTypes")
	public List<ProviderAttributeType> getProviderAttributeTypes() throws Exception {
		return Context.getProviderService().getAllProviderAttributeTypes(true);
	}
	
	@RequestMapping(method = RequestMethod.GET)
	public String showForm() {
		return "admin/provider/providerForm";
	}
	
	public String showForm(Integer providerId) {
		return "redirect:provider.form?providerId=" + providerId;
	}
	
}
