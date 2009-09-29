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
package org.openmrs.logic.rule;

import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.logic.LogicContext;
import org.openmrs.logic.LogicCriteria;
import org.openmrs.logic.LogicException;
import org.openmrs.logic.StatefulRule;
import org.openmrs.logic.datasource.LogicDataSource;
import org.openmrs.logic.datasource.ObsDataSource;
import org.openmrs.logic.result.Result;
import org.openmrs.logic.result.Result.Datatype;

/**
 * 
 */
public class ReferenceRule implements StatefulRule {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	private LogicDataSource dataSource;
	
	private String key;
	
	private String reference;
	
	public ReferenceRule() {
	}
	
	public ReferenceRule(String reference) throws InvalidReferenceRuleException {
		this.reference = reference;
		parse(reference);
	}
	
	private void parse(String reference) throws InvalidReferenceRuleException {
		
		log.info("Parsing reference string " + reference);
		int firstDotIndex = reference.indexOf('.');
		if (firstDotIndex == -1)
			throw new InvalidReferenceRuleException("Missing dot notation");
		if (firstDotIndex < 1)
			throw new InvalidReferenceRuleException("References cannot start with a period");
		if (firstDotIndex >= reference.length() - 1)
			throw new InvalidReferenceRuleException("Missing key name following period; expecting 'datasource.key'");
		String dataSourceName = reference.substring(0, firstDotIndex);
		key = reference.substring(firstDotIndex + 1);
		dataSource = Context.getLogicService().getLogicDataSource(dataSourceName);
		// TODO: hack to load the keys of obs data source
		if (dataSource.getClass().isAssignableFrom(ObsDataSource.class))
			((ObsDataSource) dataSource).addKey(key);
		if (dataSource == null)
			throw new InvalidReferenceRuleException("Invalid logic data source: " + dataSourceName);
		if (key == null || !dataSource.hasKey(key))
			throw new InvalidReferenceRuleException("Invalid key (" + key + ") for LogicDataSource (" + dataSourceName
			        + ").  Key attempted to be pulled from reference: " + reference);
	}
	
	/**
	 * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, org.openmrs.Patient,
	 *      java.util.Map)
	 */
	public Result eval(LogicContext context, Patient patient, Map<String, Object> parameters) throws LogicException {
		
		log.info("Evaluating " + key + " ... ");
		return context.read(patient, dataSource, new LogicCriteria(key));
	}
	
	public Result eval(LogicContext context, Patient patient, LogicCriteria criteria) throws LogicException {
		
		return context.read(patient, dataSource, criteria);
	}
	
	/**
	 * @see org.openmrs.logic.Rule#getParameterList()
	 */
	public Set<RuleParameterInfo> getParameterList() {
		return null;
	}
	
	/**
	 * @see org.openmrs.logic.Rule#getDependencies()
	 */
	public String[] getDependencies() {
		return null;
	}
	
	/**
	 * @see org.openmrs.logic.Rule#getDefaultDatatype()
	 */
	public Datatype getDefaultDatatype() {
		// TODO: data type for reference rule depends on key...for now just use
		// string
		return Datatype.TEXT;
	}
	
	/**
	 * @see org.openmrs.logic.Rule#getTTL()
	 */
	public int getTTL() {
		return dataSource.getDefaultTTL();
	}

	/**
     * @see org.openmrs.logic.StatefulRule#restoreFromString(java.lang.String)
     */
    public void restoreFromString(String state) {
    	try {
    		this.reference = state;
	        parse(state);
        }
        catch (InvalidReferenceRuleException e) {
	        log.error("Error generated", e);
        }
    }

	/**
     * @see org.openmrs.logic.StatefulRule#saveToString()
     */
    public String saveToString() {
    	return reference;
    }
	
}
