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

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.Location;
import org.openmrs.Program;
import org.openmrs.api.context.Context;

public class PatientProgramsPortletController extends PortletController {
	
	protected void populateModel(HttpServletRequest request, Map<String, Object> model) {
		if (!model.containsKey("programs")) {
			List<Program> programs = Context.getProgramWorkflowService().getAllPrograms();
			model.put("programs", programs);
		}
		if (!model.containsKey("locations")) {
			List<Location> locations = Context.getLocationService().getAllLocations();
			model.put("locations", locations);
		}
	}
	
}
