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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.EncounterRole;
import org.openmrs.api.APIException;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.validator.EncounterRoleValidator;
import org.openmrs.web.WebConstants;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.util.StringUtils.hasText;

/**
 * This class controls the encounter.form jsp page. See
 * /web/WEB-INF/view/admin/encounters/encounterForm.jsp
 */

@Controller
@RequestMapping("/admin/encounters")
public class EncounterRoleFormController {
	
	/**
	 * Logger for this class and subclasses
	 */
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * @param session       HttpSession for the user
	 * @param encounterRole encounterRole object submitted from the form
	 * @param errors        list of errors if exists   @return logical view name to resolve
	 * @throws Exception
	 * @should save a new encounter role object
	 * @should raise an error if validation of encounter role fails
	 * @should edit and save an existing encounter
	 */
	@RequestMapping(value = "/encounterRole.form", method = RequestMethod.POST, params = "saveEncounterRole")
	public String save(HttpSession session, @ModelAttribute("encounterRole") EncounterRole encounterRole,
	        BindingResult errors) throws Exception {
		new EncounterRoleValidator().validate(encounterRole, errors);
		if (!errors.hasErrors() && Context.isAuthenticated()) {
			EncounterService service = Context.getEncounterService();
			String message = saveEncounterRole(encounterRole, service);
			session.setAttribute(WebConstants.OPENMRS_MSG_ATTR, message);
			return showEncounterList();
		}
		
		return showForm();
	}
	
	/**
	 * @param session       HttpSession for the user
	 * @param encounterRole encounterRole object submitted from the form
	 * @param errors        list of errors if exists   @return logical view name to resolve
	 * @throws Exception
	 * @should retire an existing encounter
	 * @should raise an error if retire reason is not filled
	 */
	@RequestMapping(value = "/encounterRole.form", method = RequestMethod.POST, params = "retire")
	public String retire(HttpSession session, @ModelAttribute("encounterRole") EncounterRole encounterRole,
	        BindingResult errors) throws Exception {
		new EncounterRoleValidator().validate(encounterRole, errors);
		if (encounterRole.getEncounterRoleId() != null && !(hasText(encounterRole.getRetireReason()))) {
			errors.reject("retireReason", "general.retiredReason.empty");
		}
		if (!errors.hasErrors() && Context.isAuthenticated()) {
			EncounterService service = Context.getEncounterService();
			String message = retireEncounterRole(encounterRole, service);
			session.setAttribute(WebConstants.OPENMRS_MSG_ATTR, message);
			return showEncounterList();
		}
		
		return showForm();
	}
	
	/**
	 * @param session       HttpSession for the user
	 * @param encounterRole encounterRole object submitted from the form
	 * @param errors        list of errors if exists   @return logical view name to resolve
	 * @throws Exception
	 * @should unretire an existing encounter
	 */
	@RequestMapping(value = "/encounterRole.form", method = RequestMethod.POST, params = "unretire")
	public String unretire(HttpSession session, @ModelAttribute("encounterRole") EncounterRole encounterRole,
	        BindingResult errors) throws Exception {
		new EncounterRoleValidator().validate(encounterRole, errors);
		if (!errors.hasErrors() && Context.isAuthenticated()) {
			EncounterService service = Context.getEncounterService();
			unRetireEncounterRole(encounterRole, service, session);
			return showEncounterList();
		}
		
		return showForm();
	}
	
	/**
	 * @param session       HttpSession for the user
	 * @param encounterRole encounterRole object submitted from the form
	 * @param errors        list of errors if exists   @return logical view name to resolve
	 * @throws Exception
	 * @should retire and unretire an existing encounter
	 * @should raise an error if retire reason is not filled
	 * @should purge an existing encounter
	 */
	@RequestMapping(value = "/encounterRole.form", method = RequestMethod.POST, params = "purge")
	public String purge(HttpSession session, @ModelAttribute("encounterRole") EncounterRole encounterRole,
	        BindingResult errors) throws Exception {
		new EncounterRoleValidator().validate(encounterRole, errors);
		if (!errors.hasErrors() && Context.isAuthenticated()) {
			EncounterService service = Context.getEncounterService();
			purgeEncounterRole(session, encounterRole, service);
			return showEncounterList();
		}
		
		return showForm();
	}
	
	@ModelAttribute("encounterRole")
	public EncounterRole formBackingObject(@RequestParam(required = false) Integer encounterRoleId) throws ServletException {
		EncounterRole encounterRole = new EncounterRole();
		if (Context.isAuthenticated() && encounterRoleId != null) {
			EncounterService encounterService = Context.getEncounterService();
			encounterRole = encounterService.getEncounterRole(encounterRoleId);
		}
		return encounterRole;
	}
	
	@RequestMapping(value = "/encounterRole.form", method = RequestMethod.GET)
	public String showForm() {
		return "admin/encounters/encounterRoleForm";
	}
	
	/**
	 * @param modelMap
	 * @return logical view for the encounter list
	 * @should add list of encounter role objects to the model
	 */
	@RequestMapping(value = "/encounterRole.list", method = RequestMethod.GET)
	public String getEncounterList(ModelMap modelMap) {
		List<EncounterRole> encounterRoles = new ArrayList<EncounterRole>();
		if (Context.isAuthenticated()) {
			EncounterService encounterService = Context.getEncounterService();
			encounterRoles = encounterService.getAllEncounterRoles(true);
		}
		modelMap.addAttribute("encounterRoles", encounterRoles);
		return "admin/encounters/encounterRoleList";
	}
	
	private String showForm(Integer encounterRoleId) {
		return "redirect:encounterRole.form?encounterRoleId=" + encounterRoleId;
	}
	
	private String showEncounterList() {
		return "redirect:encounterRole.list";
	}
	
	private String retireEncounterRole(EncounterRole encounterRole, EncounterService service) {
		service.retireEncounterRole(encounterRole, encounterRole.getRetireReason());
		return "EncounterRole.retiredSuccessfully";
	}
	
	private String saveEncounterRole(EncounterRole encounterRole, EncounterService service) {
		String message;
		service.saveEncounterRole(encounterRole);
		message = "EncounterRole.saved";
		return message;
	}
	
	private void purgeEncounterRole(HttpSession session, EncounterRole encounterRole, EncounterService service) {
		try {
			service.purgeEncounterRole(encounterRole);
			session.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "EncounterRole.purgedSuccessfully");
		}
		catch (DataIntegrityViolationException e) {
			session.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "error.object.inuse.cannot.purge");
		}
		catch (APIException e) {
			session.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "error.general: " + e.getLocalizedMessage());
		}
	}
	
	private void unRetireEncounterRole(EncounterRole encounterRole, EncounterService service, HttpSession session) {
		service.unretireEncounterRole(encounterRole);
		session.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "EncounterRole.unretired");
	}
}
