/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;

/**
 * Controller for the patientEncounters portlet. Provides a map telling which forms have their view
 * and edit links overridden by form entry modules
 */
public class PatientVisitsPortletController extends PortletController {
	
	private static final Log log = LogFactory.getLog(PatientVisitsPortletController.class);
	
	/**
	 * @see org.openmrs.web.controller.PortletController#populateModel(javax.servlet.http.HttpServletRequest,
	 *      java.util.Map)
	 */
	@Override
	protected void populateModel(HttpServletRequest request, Map<String, Object> model) {
		if (log.isDebugEnabled()) {
			log.debug("In PatientVisitsPortletController...");
		}
		
		PortletControllerUtil.addFormToEditAndViewUrlMaps(model);
		
		List<Encounter> unAssignedEncounters = new ArrayList<Encounter>();
		Patient patient = (Patient) model.get("patient");
		if (patient.getPatientId() != null) {
			unAssignedEncounters = Context.getEncounterService().getEncountersNotAssignedToAnyVisit(patient);
		}
		
		model.put("unAssignedEncounters", unAssignedEncounters);
		
		// determine whether it's need to show disclaimer on jsp page or not
		// as current user does not have enough permissions to view at least one
		// type of encounters
		model.put("showDisclaimer", !Context.getEncounterService().canViewAllEncounterTypes(Context.getAuthenticatedUser()));
	}
	
}
