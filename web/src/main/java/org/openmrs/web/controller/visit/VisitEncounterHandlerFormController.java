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
package org.openmrs.web.controller.visit;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.api.handler.EncounterVisitHandler;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.PrivilegeConstants;
import org.openmrs.web.WebConstants;
import org.openmrs.web.form.visit.VisitEncounterHandlerForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * This class controls the visitEncounterHandler.form jsp page. See
 * /web/WEB-INF/view/admin/visits/visitEncounterHandler.jsp
 */
@Controller
public class VisitEncounterHandlerFormController {
	
	public static final String MANAGE_VISIT_ENCOUNTER_HANDLERS_PATH = "/admin/visits/visitEncounterHandler";
	
	public static final String VISIT_ENCOUNTER_HANDLER_FORM = "visitEncounterHandlerForm";
	
	public static final String VISIT_ENCOUNTER_HANDLERS = "visitEncounterHandlers";
	
	@Autowired
	private EncounterService encounterService;
	
	@Autowired
	@Qualifier("adminService")
	private AdministrationService administrationService;
	
	@ModelAttribute(VISIT_ENCOUNTER_HANDLERS)
	public Collection<EncounterVisitHandler> getEncounterVisitHandlers() {
		return encounterService.getEncounterVisitHandlers();
	}
	
	@RequestMapping(value = MANAGE_VISIT_ENCOUNTER_HANDLERS_PATH, method = RequestMethod.GET)
	public void manageEncounterVisitHandlers(Model model) {
		Context.requirePrivilege(PrivilegeConstants.CONFIGURE_VISITS);
		
		String visitEncounterHandler = administrationService.getGlobalProperty(OpenmrsConstants.GP_VISIT_ASSIGNMENT_HANDLER);
		String enableVisits = administrationService.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_ENABLE_VISITS,
		    Boolean.FALSE.toString());
		
		VisitEncounterHandlerForm form = new VisitEncounterHandlerForm();
		form.setEnableVisits(Boolean.valueOf(enableVisits));
		for (EncounterVisitHandler visitHandler : getEncounterVisitHandlers()) {
			if (visitHandler.getClass().getName().equals(visitEncounterHandler)) {
				form.setVisitEncounterHandler(visitHandler.getClass().getName());
				break;
			}
		}
		
		model.addAttribute(VISIT_ENCOUNTER_HANDLER_FORM, form);
	}
	
	@RequestMapping(value = MANAGE_VISIT_ENCOUNTER_HANDLERS_PATH, method = RequestMethod.POST)
	public void manageEncounterVisitHandlers(@ModelAttribute(VISIT_ENCOUNTER_HANDLER_FORM) VisitEncounterHandlerForm form,
	        Errors errors, HttpServletRequest request) {
		Context.requirePrivilege(PrivilegeConstants.CONFIGURE_VISITS);
		
		if (errors.hasErrors()) {
			return;
		}
		
		GlobalProperty gpEnableVisits = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_ENABLE_VISITS, Boolean
		        .toString(form.isEnableVisits()));
		administrationService.saveGlobalProperty(gpEnableVisits);
		if (form.isEnableVisits()) {
			String type = form.getVisitEncounterHandler();
			GlobalProperty gpVisitEncounterHandler = new GlobalProperty(OpenmrsConstants.GP_VISIT_ASSIGNMENT_HANDLER, type);
			administrationService.saveGlobalProperty(gpVisitEncounterHandler);
		} else {
			form.setVisitEncounterHandler(administrationService
			        .getGlobalProperty(OpenmrsConstants.GP_VISIT_ASSIGNMENT_HANDLER));
		}
		
		request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Encounter.visits.configuration.savedSuccessfully");
	}
}
