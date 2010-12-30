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
package org.openmrs.web.controller.patient;

import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PatientDashboardController {
	
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * render the patient dashboard model and direct to the view
	 */
	@RequestMapping("/patientDashboard.form")
	protected String renderDashboard(@RequestParam(required = true, value = "patientId") Integer patientId, ModelMap map)
	        throws Exception {
		
		// get the patient
		
		PatientService ps = Context.getPatientService();
		Patient patient = null;
		
		try {
			patient = ps.getPatient(patientId);
		}
		catch (ObjectRetrievalFailureException noPatientEx) {
			log.warn("There is no patient with id: '" + patientId + "'", noPatientEx);
		}
		
		if (patient == null)
			throw new ServletException("There is no patient with id: '" + patientId + "'");
		
		log.debug("patient: '" + patient + "'");
		map.put("patient", patient);
		
		// determine cause of death
		
		String causeOfDeathOther = "";
		
		if (Context.isAuthenticated()) {
			String propCause = Context.getAdministrationService().getGlobalProperty("concept.causeOfDeath");
			Concept conceptCause = Context.getConceptService().getConcept(propCause);
			
			if (conceptCause != null) {
				List<Obs> obssDeath = Context.getObsService().getObservationsByPersonAndConcept(patient, conceptCause);
				if (obssDeath.size() == 1) {
					Obs obsDeath = obssDeath.iterator().next();
					causeOfDeathOther = obsDeath.getValueText();
					if (causeOfDeathOther == null) {
						log.debug("cod is null, so setting to empty string");
						causeOfDeathOther = "";
					} else {
						log.debug("cod is valid: " + causeOfDeathOther);
					}
				} else {
					log.debug("obssDeath is wrong size: " + obssDeath.size());
				}
			} else {
				log.debug("No concept cause found");
			}
		}
		
		// determine patient variation
		
		String patientVariation = "";
		
		Concept reasonForExitConcept = Context.getConceptService().getConcept(
		    Context.getAdministrationService().getGlobalProperty("concept.reasonExitedCare"));
		
		if (reasonForExitConcept != null) {
			List<Obs> patientExitObs = Context.getObsService().getObservationsByPersonAndConcept(patient,
			    reasonForExitConcept);
			if (patientExitObs != null) {
				log.debug("Exit obs is size " + patientExitObs.size());
				if (patientExitObs.size() == 1) {
					Obs exitObs = patientExitObs.iterator().next();
					Concept exitReason = exitObs.getValueCoded();
					Date exitDate = exitObs.getObsDatetime();
					if (exitReason != null && exitDate != null) {
						patientVariation = "Exited";
					}
				} else if (patientExitObs.size() > 1) {
					log.error("Too many reasons for exit - not putting data into model");
				}
			}
		}
		
		map.put("patientVariation", patientVariation);
		
		// empty objects used to create blank template in the view
		
		map.put("emptyIdentifier", new PatientIdentifier());
		map.put("emptyName", new PersonName());
		map.put("emptyAddress", new PersonAddress());
		map.put("causeOfDeathOther", causeOfDeathOther);
		
		return "patientDashboardForm";
	}
	
}
