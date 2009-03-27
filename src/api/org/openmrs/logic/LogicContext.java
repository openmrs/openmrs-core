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

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.logic.datasource.LogicDataSource;
import org.openmrs.logic.result.Result;
import org.openmrs.logic.rule.ReferenceRule;

/**
 * The context within which logic rule and data source evaluations are made. The logic context is
 * responsible for maintaining context-sensitive information &mdash; e.g., the index date and global
 * parameters &mdash; as well as handling caching of results. <strong>Index date</strong> is the
 * date used as "today" for any calculations or queries. This allows the same rule to be evaluated
 * retrospectively. For example, a rule calculating the "maximum CD4 count in the past six months"
 * can be calculated as if it were 4-July-2005.
 */
public class LogicContext {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * Hold the index date for this context, representing the value for "today" and thereby allowing
	 * the same rules to be run today as well as retrospectively
	 */
	private Date indexDate;
	
	/**
	 * Globally available parameters within this logic context. Global parameters are available to
	 * all evaluations performed within this context
	 */
	private Map<String, Object> globalParameters;
	
	/**
	 * If this context was constructed from another logic context, this references the original
	 * context; otherwise, this is null
	 */
	private LogicContext parentContext = null;
	
	/**
	 * Patients being processed within this logic context
	 */
	private Cohort patients;
	
	/**
	 * Cache used by this log context
	 * 
	 * @see org.openmrs.logic.LogicCache
	 */
	private LogicCache cache;
	
	/**
	 * Constructs a logic context applied to a single patient
	 * 
	 * @param patient
	 */
	public LogicContext(Patient patient) {
		this.patients = new Cohort();
		this.globalParameters = new HashMap<String, Object>();
		patients.addMember(patient.getPatientId());
		setIndexDate(new Date());
	}
	
	/**
	 * Constructs a logic context applied to a cohort of patients
	 * 
	 * @param patients
	 */
	public LogicContext(Cohort patients) {
		this.patients = patients;
		this.globalParameters = new HashMap<String, Object>();
		setIndexDate(new Date());
	}
	
	/**
	 * Evaluate a rule for a single patient
	 * 
	 * @param patient
	 * @param token
	 * @return <code>Result</code> of the evaluation
	 * @throws LogicException
	 * @see org.openmrs.logic.LogicService#eval(Patient, String)
	 */
	public Result eval(Patient patient, String token) throws LogicException {
		return eval(patient, new LogicCriteria(token), null);
	}
	
	/**
	 * Evaluate a rule with parameters for a single patient
	 * 
	 * @param patient
	 * @param token
	 * @param parameters
	 * @return <code>Result</code> of the evaluation
	 * @throws LogicException
	 * @see org.openmrs.logic.LogicService#eval(Patient, String, Map)
	 */
	public Result eval(Patient patient, String token, Map<String, Object> parameters) throws LogicException {
		return eval(patient, new LogicCriteria(token), parameters);
	}
	
	/**
	 * Evaluate a rule with criteria and parameters for a single patient
	 * 
	 * @param patient
	 * @param criteria
	 * @param parameters
	 * @return A <code>Result</code> object with the result of the evaluation
	 * @throws LogicException
	 * @see org.openmrs.logic.LogicService#eval(Patient, LogicCriteria, Map)
	 */
	public Result eval(Patient patient, LogicCriteria criteria, Map<String, Object> parameters) throws LogicException {
		Result result = getCache().get(patient, criteria, parameters);
		PatientService patientService = Context.getPatientService();
		
		if (result == null) {
			Integer targetPatientId = patient.getPatientId();
			log.debug("Context database read (pid = " + targetPatientId + ")");
			Rule rule = Context.getLogicService().getRule(criteria.getRootToken());
			Map<Integer, Result> resultMap = new Hashtable<Integer, Result>();
			for (Integer pid : patients.getMemberIds()) {
				Patient currPatient = patientService.getPatient(pid);
				Result r = Result.emptyResult();
				if (rule instanceof ReferenceRule) {
					r = ((ReferenceRule) rule).eval(this, currPatient, criteria);
				} else {
					r = rule.eval(this, currPatient, parameters);
					r = applyCriteria(r, criteria);
				}
				
				resultMap.put(pid, r);
				if (pid.equals(targetPatientId))
					result = resultMap.get(pid);
			}
			getCache().put(criteria, parameters, rule.getTTL(), resultMap);
		}
		
		return result;
	}
	
	/**
	 * Criteria are applied to results of rules <em>after</em> the rule has been evaluated, since
	 * rules are not expected to interpret all possible criteria
	 * 
	 * @param result
	 * @param criteria
	 * @return
	 */
	private Result applyCriteria(Result result, LogicCriteria criteria) {
		// TODO: apply criteria to result
		return result;
	}
	
	/**
	 * Fetches a logic data source by name
	 * 
	 * @param name
	 * @return the requested <code>LogicDataSource</code>
	 */
	public LogicDataSource getLogicDataSource(String name) {
		return Context.getLogicService().getLogicDataSource(name);
	}
	
	/**
	 * Reads a key from a logic data source
	 * 
	 * @param patient
	 * @param dataSource
	 * @param key
	 * @return <code>Result</code> of the read operation
	 * @throws LogicException
	 */
	public Result read(Patient patient, LogicDataSource dataSource, String key) throws LogicException {
		return read(patient, dataSource, new LogicCriteria(key));
	}
	
	/**
	 * Auto generated method comment
	 * 
	 * @param patient
	 * @param key
	 * @return <code>Result</code> of the read operation
	 * @throws LogicException
	 */
	public Result read(Patient patient, String key) throws LogicException {
		
		LogicService logicService = Context.getLogicService();
		LogicDataSource dataSource = logicService.getLogicDataSource("obs");
		return read(patient, dataSource, key);
	}
	
	/**
	 * Reads a key with criteria from a logic data source
	 * 
	 * @param patient
	 * @param criteria
	 * @return <code>Result</code> of the read
	 * @throws LogicException
	 */
	public Result read(Patient patient, LogicCriteria criteria) throws LogicException {
		LogicService logicService = Context.getLogicService();
		LogicDataSource dataSource = logicService.getLogicDataSource("obs");
		return read(patient, dataSource, criteria);
	}
	
	/**
	 * Reads a key with criteria from a logic data source
	 * 
	 * @param patient
	 * @param dataSource
	 * @param criteria
	 * @return <code>Result</code> of the read
	 * @throws LogicException
	 */
	public Result read(Patient patient, LogicDataSource dataSource, LogicCriteria criteria) throws LogicException {
		Result result = getCache().get(patient, dataSource, criteria);
		log
		        .debug("Reading from data source: " + criteria.getRootToken() + " (" + (result == null ? "NOT" : "")
		                + " cached)");
		if (result == null) {
			Map<Integer, Result> resultMap = dataSource.read(this, patients, criteria);
			getCache().put(dataSource, criteria, resultMap);
			result = resultMap.get(patient.getPatientId());
		}
		if (result == null)
			result = Result.emptyResult();
		return result;
	}
	
	/**
	 * Changes the index date for this logic context
	 * 
	 * @param indexDate the new <code>Date</code> value for "today" to be used by rules within this
	 *            logic context
	 */
	public void setIndexDate(Date indexDate) {
		this.indexDate = indexDate;
	}
	
	/**
	 * @return the value of "today" within this logic context
	 */
	public Date getIndexDate() {
		return indexDate;
	}
	
	/**
	 * @return the index date for the logic context (effective value of "today")
	 * @see #getIndexDate()
	 */
	public Date today() {
		return getIndexDate();
	}
	
	/**
	 * Assigns a value to a global parameters within this logic context
	 * 
	 * @param id
	 * @param value
	 * @return the value of the parameter that was set
	 */
	public Object setGlobalParameter(String id, Object value) {
		return globalParameters.put(id, value);
	}
	
	/**
	 * Fetches a global parameter value by name
	 * 
	 * @param id
	 * @return The requested Global parameter <code>Object</code>
	 */
	public Object getGlobalParameter(String id) {
		return globalParameters.get(id);
	}
	
	/**
	 * @return all global parameters defined within this logic context
	 */
	public Collection<String> getGlobalParameters() {
		return globalParameters.keySet();
	}
	
	/**
	 * @return the cache for this logic context
	 */
	private LogicCache getCache() {
		if (cache == null)
			cache = new LogicCache();
		return cache;
	}
	
}
