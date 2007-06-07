package org.openmrs.logic;

import java.util.List;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;

public class PatientDataSource implements LogicDataSource {
	
	private static PatientDataSource singleton = new PatientDataSource();
	
	private PatientDataSource() {
	}
	
	public static PatientDataSource getInstance() {
		return singleton;
	}

	public Result eval(Patient patient, Aggregation aggregation, Concept concept, Constraint constraint) {
		List<Obs> obsList = Context.getObsService().getObservations(patient, aggregation, concept, constraint);
		return new Result(obsList);
	}

	public Result eval(Patient patient, Aggregation aggregation, String token, Constraint constraint, Object[] args) {
		Rule rule = Context.getLogicService().getRule(token);
		if (rule instanceof ConceptRule)
			return eval(patient, aggregation, ((ConceptRule)rule).getConcept(), constraint);
		// TODO: apply aggregation/constraint to rule result
		return rule.eval(this, patient, args);
	}

	public Result eval(Patient patient, Aggregation aggregation, Rule rule, Constraint constraint, Object[] args) {
		if (rule instanceof ConceptRule)
			return eval(patient, aggregation, ((ConceptRule)rule).getConcept(), constraint);
		// TODO: apply aggregation/constraint to rule result
		return rule.eval(this, patient, args);
	}
	
	public Result eval(Patient patient, String token) {
		return eval(patient, token, null);
	}

	public Result eval(Patient patient, String token, Object[] args) {
		return eval(patient, null, token, null, args);
	}

	public Result eval(Patient patient, Rule rule, Object[] args) {
		return eval(patient, null, rule, null, args);
	}
}
