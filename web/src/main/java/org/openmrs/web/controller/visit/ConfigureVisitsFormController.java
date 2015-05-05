/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.visit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.openmrs.GlobalProperty;
import org.openmrs.VisitType;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.api.handler.EncounterVisitHandler;
import org.openmrs.scheduler.SchedulerException;
import org.openmrs.scheduler.TaskDefinition;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.PrivilegeConstants;
import org.openmrs.web.WebConstants;
import org.openmrs.web.form.visit.ConfigureVisitsForm;
import org.openmrs.web.form.visit.ConfigureVisitsFormValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * This class controls the configureVisits.form jsp page. See
 * /web/WEB-INF/view/admin/visits/configureVisits.jsp
 */
@Controller
public class ConfigureVisitsFormController {
	
	public static final String CONFIGURE_VISITS_PATH = "/admin/visits/configureVisits";
	
	public static final String VISIT_ENCOUNTER_HANDLER_FORM = "configureVisitsForm";
	
	public static final String VISIT_ENCOUNTER_HANDLERS = "visitEncounterHandlers";
	
	@Autowired
	private EncounterService encounterService;
	
	@Autowired
	@Qualifier("adminService")
	private AdministrationService administrationService;
	
	@Autowired
	private VisitService visitService;
	
	@ModelAttribute(VISIT_ENCOUNTER_HANDLERS)
	public Collection<EncounterVisitHandler> getEncounterVisitHandlers() {
		return encounterService.getEncounterVisitHandlers();
	}
	
	@ModelAttribute("visitTypes")
	public List<VisitType> getVisitTypes() {
		return visitService.getAllVisitTypes();
	}
	
	@RequestMapping(value = CONFIGURE_VISITS_PATH, method = RequestMethod.GET)
	public void manageEncounterVisitHandlers(Model model) {
		Context.requirePrivilege(PrivilegeConstants.CONFIGURE_VISITS);
		
		String visitEncounterHandler = administrationService.getGlobalProperty(OpenmrsConstants.GP_VISIT_ASSIGNMENT_HANDLER);
		String enableVisits = administrationService.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_ENABLE_VISITS,
		    Boolean.FALSE.toString());
		TaskDefinition closeVisitsTask = Context.getSchedulerService().getTaskByName(
		    OpenmrsConstants.AUTO_CLOSE_VISITS_TASK_NAME);
		
		ConfigureVisitsForm form = new ConfigureVisitsForm();
		form.setEnableVisits(Boolean.valueOf(enableVisits));
		if (closeVisitsTask != null) {
			form.setCloseVisitsTaskStarted(closeVisitsTask.getStarted());
		}
		for (EncounterVisitHandler visitHandler : getEncounterVisitHandlers()) {
			if (visitHandler.getClass().getName().equals(visitEncounterHandler)) {
				form.setVisitEncounterHandler(visitHandler.getClass().getName());
				break;
			}
		}
		
		String gpValue = Context.getAdministrationService().getGlobalProperty(OpenmrsConstants.GP_VISIT_TYPES_TO_AUTO_CLOSE);
		if (StringUtils.isNotBlank(gpValue)) {
			List<VisitType> visitTypes = new ArrayList<VisitType>();
			String[] visitTypeNames = StringUtils.split(gpValue.trim(), ",");
			for (int i = 0; i < visitTypeNames.length; i++) {
				String currName = visitTypeNames[i];
				visitTypeNames[i] = currName.trim().toLowerCase();
			}
			
			List<VisitType> allVisitTypes = visitService.getAllVisitTypes();
			for (VisitType visitType : allVisitTypes) {
				if (ArrayUtils.contains(visitTypeNames, visitType.getName().toLowerCase())) {
					visitTypes.add(visitType);
				}
			}
			form.setVisitTypesToClose(visitTypes);
		}
		
		model.addAttribute(VISIT_ENCOUNTER_HANDLER_FORM, form);
	}
	
	@RequestMapping(value = CONFIGURE_VISITS_PATH, method = RequestMethod.POST)
	public void manageEncounterVisitHandlers(@ModelAttribute(VISIT_ENCOUNTER_HANDLER_FORM) ConfigureVisitsForm form,
	        Errors errors, HttpServletRequest request) {
		Context.requirePrivilege(PrivilegeConstants.CONFIGURE_VISITS);
		new ConfigureVisitsFormValidator().validate(form, errors);
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
		
		StringBuilder visitTypeNames = new StringBuilder();
		boolean isFirst = true;
		for (VisitType vt : form.getVisitTypesToClose()) {
			if (isFirst) {
				visitTypeNames.append(vt.getName());
				isFirst = false;
				continue;
			}
			visitTypeNames.append(",").append(vt.getName());
		}
		//save the GP for visit types to close
		GlobalProperty gpVisitTypesToClose = administrationService
		        .getGlobalPropertyObject(OpenmrsConstants.GP_VISIT_TYPES_TO_AUTO_CLOSE);
		if (gpVisitTypesToClose == null) {
			gpVisitTypesToClose = new GlobalProperty(OpenmrsConstants.GP_VISIT_TYPES_TO_AUTO_CLOSE);
		}
		gpVisitTypesToClose.setPropertyValue(visitTypeNames.toString());
		administrationService.saveGlobalProperty(gpVisitTypesToClose);
		
		TaskDefinition closeVisitsTask = Context.getSchedulerService().getTaskByName(
		    OpenmrsConstants.AUTO_CLOSE_VISITS_TASK_NAME);
		if (closeVisitsTask != null) {
			try {
				if (form.getCloseVisitsTaskStarted() && !closeVisitsTask.getStarted()) {
					Context.getSchedulerService().scheduleTask(closeVisitsTask);
				} else if (!form.getCloseVisitsTaskStarted() && closeVisitsTask.getStarted()) {
					Context.getSchedulerService().shutdownTask(closeVisitsTask);
				}
			}
			catch (SchedulerException e) {
				errors.rejectValue("closeVisitsTaskStarted",
				    (form.getCloseVisitsTaskStarted()) ? "Visit.configure.closeVisitsTask.failedToStart"
				            : "Visit.configure.closeVisitsTask.failedToStop");
			}
		}
		
		request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Encounter.visits.configuration.savedSuccessfully");
	}
}
