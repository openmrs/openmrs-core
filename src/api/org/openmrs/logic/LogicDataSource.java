package org.openmrs.logic;

import org.openmrs.Concept;
import org.openmrs.Patient;

public interface LogicDataSource {
	
	public Result eval(Patient patient, String token);
	
	public Result eval(Patient patient, String token, Object[] args);

	public Result eval(Patient patient, Rule rule, Object[] args);
	
	public Result eval(Patient patient, Aggregation aggregation, Concept concept, Constraint constraint);
	
	public Result eval(Patient patient, Aggregation aggregation, String token, Constraint constraint, Object[] args);

	public Result eval(Patient patient, Aggregation aggregation, Rule rule, Constraint constraint, Object[] args);
	
}
