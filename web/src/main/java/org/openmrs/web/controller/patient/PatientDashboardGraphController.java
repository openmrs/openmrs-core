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
		
		for (Obs obs : observations) {
			graph.addValue(obs.getObsDatetime().getTime(), obs.getValueNumeric());
		}
		
		map.put("graph", graph);
		
		return "patientGraphJsonForm";
	}
}
