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
package org.openmrs.logic;

import java.util.List;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;

public class PatientCohortDataSource implements LogicDataSource {

	private PatientCohort patientCohort;

	public PatientCohortDataSource(PatientCohort patientCohort) {
		this.patientCohort = patientCohort;
	}
	
	public Result eval(Patient patient, String token) {
		return eval(patient, token, null);
	}

	public Result eval(Patient patient, Aggregation aggregation,
			Concept concept, Constraint constraint) {
		List<Obs> obsList = Context.getObsService().getObservations(patient,
				aggregation, concept, constraint);
		return new Result(obsList);
	}

	public Result eval(Patient patient, Aggregation aggregation, String token,
			Constraint constraint, Object[] args) {
		Rule rule = Context.getLogicService().getRule(token);
		return eval(patient, aggregation, rule, constraint, args);
	}

	public Result eval(Patient patient, Aggregation aggregation, Rule rule,
			Constraint constraint, Object[] args) {
		if (rule instanceof ConceptRule)
			return eval(patient, aggregation,
					((ConceptRule) rule).getConcept(), constraint);
		return rule.eval(this, patient, args);
	}

	public Result eval(Patient patient, String token, Object[] args) {
		Rule rule = Context.getLogicService().getRule(token);
		return eval(patient, rule, args);
	}

	public Result eval(Patient patient, Rule rule, Object[] args) {
		return eval(patient, null, rule, null, args);
	}

}
