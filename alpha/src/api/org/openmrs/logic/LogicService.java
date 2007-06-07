package org.openmrs.logic;

import java.util.HashMap;

import org.openmrs.Concept;
import org.openmrs.Patient;

public interface LogicService {
	
	public Rule getRule(String token);

	public void addToken(String token, Class clazz) throws LogicException;
	
	public void addToken(String token, Concept concept) throws LogicException;
	
	public void removeToken(String token);
	
	public Result eval(Patient who, String token);

	public Result eval(Patient who, String token, Object[] args);

	public Result eval(Patient who, Aggregation aggregation, String token,
			DateConstraint constraint, Object[] args);

	public Result eval(Patient who, Concept concept);

	public Result eval(Patient who, Aggregation aggregation, Concept concept,
			DateConstraint constraint);

	public HashMap<Patient, HashMap<String, Result>> eval(PatientCohort cohort,
			String[] tokenList, Object[] args);

}