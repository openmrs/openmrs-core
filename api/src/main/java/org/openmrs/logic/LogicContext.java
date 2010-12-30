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
import java.util.Map;

import org.openmrs.Patient;
import org.openmrs.logic.datasource.LogicDataSource;
import org.openmrs.logic.result.Result;

/**
 *
 */
public interface LogicContext {
	
	/**
	 * Evaluate a rule for a single patient
	 * 
	 * @param patient
	 * @param token
	 * @return <code>Result</code> of the evaluation
	 * @throws LogicException
	 * @see org.openmrs.logic.LogicService#eval(Patient, String)
	 */
	public Result eval(Patient patient, String token) throws LogicException;
	
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
	public Result eval(Patient patient, String token, Map<String, Object> parameters) throws LogicException;
	
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
	public Result eval(Patient patient, LogicCriteria criteria, Map<String, Object> parameters) throws LogicException;
	
	/**
	 * Fetches a logic data source by name
	 * 
	 * @param name
	 * @return the requested <code>LogicDataSource</code>
	 */
	public LogicDataSource getLogicDataSource(String name);
	
	/**
	 * Reads a key from a logic data source
	 * 
	 * @param patient
	 * @param dataSource
	 * @param key
	 * @return <code>Result</code> of the read operation
	 * @throws LogicException
	 */
	public Result read(Patient patient, LogicDataSource dataSource, String key) throws LogicException;
	
	/**
	 * Auto generated method comment
	 * 
	 * @param patient
	 * @param key
	 * @return <code>Result</code> of the read operation
	 * @throws LogicException
	 */
	public Result read(Patient patient, String key) throws LogicException;
	
	/**
	 * Reads a key with criteria from a logic data source
	 * 
	 * @param patient
	 * @param criteria
	 * @return <code>Result</code> of the read
	 * @throws LogicException
	 */
	public Result read(Patient patient, LogicCriteria criteria) throws LogicException;
	
	/**
	 * Reads a key with criteria from a logic data source
	 * 
	 * @param patient
	 * @param dataSource
	 * @param criteria
	 * @return <code>Result</code> of the read
	 * @throws LogicException
	 */
	public Result read(Patient patient, LogicDataSource dataSource, LogicCriteria criteria) throws LogicException;
	
	/**
	 * Changes the index date for this logic context
	 * 
	 * @param indexDate the new <code>Date</code> value for "today" to be used by rules within this
	 *            logic context
	 */
	public void setIndexDate(Date indexDate);
	
	/**
	 * @return the value of "today" within this logic context
	 */
	public Date getIndexDate();
	
	/**
	 * @return the index date for the logic context (effective value of "today")
	 * @see #getIndexDate()
	 */
	public Date today();
	
	/**
	 * Assigns a value to a global parameters within this logic context
	 * 
	 * @param id
	 * @param value
	 * @return the value of the parameter that was set
	 */
	public Object setGlobalParameter(String id, Object value);
	
	/**
	 * Fetches a global parameter value by name
	 * 
	 * @param id
	 * @return The requested Global parameter <code>Object</code>
	 */
	public Object getGlobalParameter(String id);
	
	/**
	 * @return all global parameters defined within this logic context
	 */
	public Collection<String> getGlobalParameters();
}
