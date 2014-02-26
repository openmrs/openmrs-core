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
package org.openmrs.web.controller.form;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Form;
import org.openmrs.FormResource;
import org.openmrs.api.context.Context;
import org.openmrs.customdatatype.CustomDatatypeUtil;
import org.openmrs.web.attribute.WebAttributeUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for listing resources on a form, and adding and deleting them
 */
@Controller
public class FormResourceController {
	
	@RequestMapping(method = RequestMethod.GET, value = "admin/forms/formResources")
	public void manageFormResources(@RequestParam("formId") Form form, Model model) {
		model.addAttribute("form", form);
		model.addAttribute("resources", Context.getFormService().getFormResourcesForForm(form));
		model.addAttribute("datatypes", CustomDatatypeUtil.getDatatypeClassnames());
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "admin/forms/deleteFormResource")
	public String deleteFormResource(@RequestParam("formId") Form form, @RequestParam("name") String name) {
		FormResource resource = Context.getFormService().getFormResource(form, name);
		if (resource != null) {
			Context.getFormService().purgeFormResource(resource);
		}
		return "redirect:formResources.form?formId=" + form.getFormId();
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "admin/forms/addFormResource")
	public void addFormResource(@RequestParam("formId") Form form, @RequestParam("datatype") String datatype,
	        @RequestParam(required = false, value = "handler") String handler, Model model) {
		model.addAttribute("form", form);
		model.addAttribute("datatype", datatype);
		model.addAttribute("handler", handler);
		model.addAttribute("handlers", CustomDatatypeUtil.getHandlerClassnames());
		
		if (StringUtils.isNotEmpty(handler)) {
			FormResource resource = new FormResource();
			resource.setForm(form);
			resource.setDatatypeClassname(datatype);
			if (!"DEFAULT".equals(handler)) {
				resource.setPreferredHandlerClassname(handler);
			}
			model.addAttribute("resource", resource);
			model.addAttribute("customDatatype", CustomDatatypeUtil.getDatatype(resource));
			model.addAttribute("customDatatypeHandler", CustomDatatypeUtil.getHandler(resource));
		}
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "admin/forms/addFormResource")
	public String handleAddFormResource(@ModelAttribute("resource") FormResource resource, Errors errors,
	        HttpServletRequest request) {
		try {
			Object value = WebAttributeUtil.getValue(request, resource, "resourceValue");
			resource.setValue(value);
		}
		catch (Exception ex) {
			errors.rejectValue("value", "error.general");
		}
		if (errors.hasErrors()) {
			throw new RuntimeException("Error handling not yet implemented");
		} else {
			Context.getFormService().saveFormResource(resource);
			return "redirect:formResources.form?formId=" + resource.getForm().getId();
		}
	}
}
