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

import java.util.Map;

import org.openmrs.Encounter;
import org.openmrs.api.context.Context;
import org.openmrs.web.controller.PortletControllerUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 */
@Controller
public class EncounterNativeFormController {
	
	@RequestMapping("/admin/encounters/encounterNativeForm")
	public void showEncountersNativeForm(@ModelAttribute Encounter encounter, Map<String, Object> model) {
		model.put("person", encounter.getPatient());
		PortletControllerUtil.addFormToEditAndViewUrlMaps(model);
	}
	
	@ModelAttribute
	public Encounter getEncounter(@RequestParam("encounterId") Integer encounterId) {
		return Context.getEncounterService().getEncounter(encounterId);
	}
}
