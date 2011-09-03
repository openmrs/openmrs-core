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
package org.openmrs.web.controller.encounter;

import java.util.Collection;

import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.api.handler.EncounterVisitHandler;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.PrivilegeConstants;
import org.openmrs.web.form.encounter.EncounterVisitHandlerForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * This class controls the encounterVisitHandler.form jsp page. See
 * /web/WEB-INF/view/admin/encounters/encounterVisitHandler.jsp
 */
@Controller
public class EncounterVisitHandlerFormController {
	
	public static final String MANAGE_ENCOUNTER_VISIT_HANDLERS_PATH = "/admin/encounters/encounterVisitHandler";
	
	public static final String ENCOUNTER_VISIT_HANDLER_FORM = "encounterVisitHandlerForm";
	
	public static final String ENCOUNTER_VISIT_HANDLERS = "encounterVisitHandlers";
	
	@Autowired
	private EncounterService encounterService;
	
	@Autowired
	@Qualifier("adminService")
	private AdministrationService administrationService;
	
	@ModelAttribute(ENCOUNTER_VISIT_HANDLERS)
	public Collection<EncounterVisitHandler> getEncounterVisitHandlers() {
		return encounterService.getEncounterVisitHandlers();
	}
	
	@RequestMapping(value = MANAGE_ENCOUNTER_VISIT_HANDLERS_PATH, method = RequestMethod.GET)
	public void manageEncounterVisitHandlers(Model model) {
		Context.requirePrivilege(PrivilegeConstants.MANAGE_ENCOUNTER_VISITS);
		
		String encounterVisitHandler = administrationService.getGlobalProperty(OpenmrsConstants.GP_VISIT_ASSIGNMENT_HANDLER);
		String enableVisits = administrationService.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_ENABLE_VISITS,
		    Boolean.FALSE.toString());
		
		EncounterVisitHandlerForm form = new EncounterVisitHandlerForm();
		form.setEnableVisits(Boolean.valueOf(enableVisits));
		for (EncounterVisitHandler visitHandler : getEncounterVisitHandlers()) {
			if (visitHandler.getClass().getName().equals(encounterVisitHandler)) {
				form.setEncounterVisitHandler(visitHandler.getClass().getName());
				break;
			}
		}
		
		model.addAttribute(ENCOUNTER_VISIT_HANDLER_FORM, form);
	}
	
	@RequestMapping(value = MANAGE_ENCOUNTER_VISIT_HANDLERS_PATH, method = RequestMethod.POST)
	public void manageEncounterVisitHandlers(@ModelAttribute(ENCOUNTER_VISIT_HANDLER_FORM) EncounterVisitHandlerForm form,
	        Errors errors) {
		Context.requirePrivilege(PrivilegeConstants.MANAGE_ENCOUNTER_VISITS);
		
		if (errors.hasErrors()) {
			return;
		}
		
		GlobalProperty gpEnableVisits = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_ENABLE_VISITS, Boolean
		        .toString(form.isEnableVisits()));
		administrationService.saveGlobalProperty(gpEnableVisits);
		if (form.isEnableVisits()) {
			String type = form.getEncounterVisitHandler();
			GlobalProperty gpEncounterVisitHandler = new GlobalProperty(OpenmrsConstants.GP_VISIT_ASSIGNMENT_HANDLER, type);
			administrationService.saveGlobalProperty(gpEncounterVisitHandler);
		} else {
			form.setEncounterVisitHandler(administrationService
			        .getGlobalProperty(OpenmrsConstants.GP_VISIT_ASSIGNMENT_HANDLER));
		}
	}
}
