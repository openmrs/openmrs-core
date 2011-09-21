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
package org.openmrs.web.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
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
		if (log.isDebugEnabled())
			log.debug("In PatientVisitsPortletController...");
		
		PortletControllerUtil.addFormToEditAndViewUrlMaps(model);
		
		List<Encounter> otherEncounters = new ArrayList<Encounter>();
		Patient patient = (Patient) model.get("patient");
		if (patient.getPatientId() != null) {
			otherEncounters = Context.getEncounterService().getEncountersByPatient(patient);
			
			//remove all encounters that are already associated to visits
			CollectionUtils.filter(otherEncounters, new Predicate() {
				
				@Override
				public boolean evaluate(Object object) {
					Encounter e = (Encounter) object;
					return e.getVisit() == null;
				}
			});
		}
		
		model.put("otherEncounters", otherEncounters);
	}
	
}
