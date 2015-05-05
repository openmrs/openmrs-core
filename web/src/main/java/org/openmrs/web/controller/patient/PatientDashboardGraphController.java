/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.patient;

import java.util.List;

import org.openmrs.ConceptNumeric;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for returning flot aware JSON data
 */
@Controller
@RequestMapping(value = "/patientGraphJson.form")
public class PatientDashboardGraphController {
	
	/**
	 * Method to formulate a JSON string used by flot for rendering the patient graph
	 * 
	 * @param patientId identifier for the patient
	 * @param conceptId identifier of the concept for which the graph has to be plotted
	 * @param map
	 * @return form which will render the JSON data
	 * @should return json data with observation details and critical values for the concept
	 * @should return form for rendering the json data
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(method = RequestMethod.GET)
	public String showGraphData(@RequestParam(required = true, value = "patientId") Integer patientId,
	        @RequestParam(required = true, value = "conceptId") Integer conceptId, ModelMap map) {
		Patient patient = Context.getPatientService().getPatient(patientId);
		ConceptNumeric concept = Context.getConceptService().getConceptNumeric(conceptId);
		
		List<Obs> observations = Context.getObsService().getObservationsByPersonAndConcept(patient, concept);
		
		PatientGraphData graph = new PatientGraphData();
		graph.setCriticalHigh(concept.getHiCritical());
		graph.setCriticalLow(concept.getLowCritical());
		graph.setAbsoluteHigh(concept.getHiAbsolute());
		graph.setAbsoluteLow(concept.getLowAbsolute());
		graph.setNormalHigh(concept.getHiNormal());
		graph.setNormalLow(concept.getLowNormal());
		graph.setUnits(concept.getUnits() != null ? concept.getUnits() : "");
		graph.setConceptName(concept.getName().getName());
		
		for (Obs obs : observations) {
			graph.addValue(obs.getObsDatetime().getTime(), obs.getValueNumeric());
		}
		
		map.put("graph", graph);
		
		return "patientGraphJsonForm";
	}
}
