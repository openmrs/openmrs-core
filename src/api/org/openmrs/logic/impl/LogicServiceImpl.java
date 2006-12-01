package org.openmrs.logic.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.logic.Aggregation;
import org.openmrs.logic.ClassRule;
import org.openmrs.logic.ConceptRule;
import org.openmrs.logic.DateConstraint;
import org.openmrs.logic.LogicDataSource;
import org.openmrs.logic.LogicException;
import org.openmrs.logic.LogicService;
import org.openmrs.logic.PatientCohort;
import org.openmrs.logic.PatientCohortDataSource;
import org.openmrs.logic.PatientDataSource;
import org.openmrs.logic.Result;
import org.openmrs.logic.Rule;

public class LogicServiceImpl implements LogicService {

	protected final Log log = LogFactory.getLog(getClass());

	private HashMap<String, Rule> tokenMap = new HashMap<String, Rule>();

	public Rule getRule(String token) {
		return tokenMap.get(token);
	}
	
	public void addToken(String token, Class clazz) throws LogicException {
		if (!Rule.class.isAssignableFrom(clazz)) {
			log
					.warn("Attempt to register Logic token with class that does not extend Rule : "
							+ clazz.getName());
			throw new LogicException("Class is not a Rule (must extend "
					+ Rule.class.getName() + ") : " + clazz.getName());
		}
		tokenMap.put(token, new ClassRule(clazz));
	}

	public void addToken(String token, Concept concept) throws LogicException {
		tokenMap.put(token, new ConceptRule(concept));
	}

	public void removeToken(String token) {
		tokenMap.remove(token);
	}

	public Result eval(Patient who, String token) {
		return eval(who, token, null);
	}
	
	public Result eval(Patient who, String token, Object[] args) {
		Rule rule = getRule(token);
		if (rule == null)
			return Result.NULL_RESULT;
		return PatientDataSource.getInstance().eval(who, rule, args);
	}

	public Result eval(Patient who, Aggregation aggregation, String token,
			DateConstraint constraint, Object[] args) {
		Rule rule = getRule(token);
		if (rule == null)
			return Result.NULL_RESULT;
		return PatientDataSource.getInstance().eval(who, aggregation, rule,
				constraint, args);
	}

	public Result eval(Patient who, Concept concept) {
		return PatientDataSource.getInstance().eval(who, null, concept, null);
	}

	public Result eval(Patient who, Aggregation aggregation, Concept concept,
			DateConstraint constraint) {
		return PatientDataSource.getInstance().eval(who, aggregation, concept,
				constraint);
	}

	public HashMap<Patient, HashMap<String, Result>> eval(PatientCohort cohort,
			String[] tokenList, Object[] args) {
		PatientCohortDataSource dataSource = new PatientCohortDataSource(cohort);
		HashMap<Patient, HashMap<String, Result>> resultMap = new HashMap<Patient, HashMap<String, Result>>();
		for (Patient patient : cohort.getPatients()) {
			HashMap<String, Result> m = new HashMap<String, Result>();
			for (String token : tokenList) {
				Rule rule = getRule(token);
				if (rule != null)
					m.put(token, dataSource.eval(patient, rule, args));
			}
			resultMap.put(patient, m);
		}
		return resultMap;
	}

}
