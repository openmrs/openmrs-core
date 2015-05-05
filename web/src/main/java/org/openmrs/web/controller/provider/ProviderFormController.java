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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

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
	        @RequestParam(required = false) String purgeProviderButton, @ModelAttribute("provider") Provider provider,
	        BindingResult errors, ModelMap model) throws Exception {
		
		// manually handle the attribute parameters
		List<ProviderAttributeType> attributeTypes = (List<ProviderAttributeType>) model.get("providerAttributeTypes");
		WebAttributeUtil
		        .handleSubmittedAttributesForType(provider, errors, ProviderAttribute.class, request, attributeTypes);
		if (Context.isAuthenticated()) {
			ProviderService service = Context.getProviderService();
			String message = "Provider.saved";
			
			if (purgeProviderButton != null) {
				try {
					service.purgeProvider(provider);
					message = "Provider.purged";
				}
				catch (DataIntegrityViolationException e) {
					request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "error.object.inuse.cannot.purge");
					return showForm(provider.getId());
				}
			} else {
				new ProviderValidator().validate(provider, errors);
			}
			
			if (!errors.hasErrors()) {
				if (saveProviderButton != null) {
					service.saveProvider(provider);
				} else if (retireProviderButton != null) {
					service.retireProvider(provider, provider.getRetireReason());
					message = "Provider.retired";
				} else if (unretireProviderButton != null) {
					service.unretireProvider(provider);
					message = "Provider.unretired";
				}
				
			}
			
			request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, message);
			return "redirect:index.htm";
		}
		
		return showForm();
	}
	
	@ModelAttribute("provider")
	public Provider formBackingObject(@RequestParam(required = false) Integer providerId) throws ServletException {
		Provider provider = new Provider();
		if (Context.isAuthenticated() && providerId != null) {
			ProviderService ps = Context.getProviderService();
			return ps.getProvider(providerId);
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
