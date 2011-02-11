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

import java.util.Map;
import java.util.Set;

import org.openmrs.Patient;
import org.openmrs.logic.result.Result;
import org.openmrs.logic.result.Result.Datatype;
import org.openmrs.logic.rule.RuleParameterInfo;

/**
 * Base class for all logic rules.
 */
public interface Rule {
	
	/**
	 * Evaluate rule for a given patient and applying the given criteria.
	 * 
	 * @param patient a patient for whom rule is to be calculated
	 * @return result of the rule for the given patient with given criteria applied
	 * @throws LogicException TODO
	 */
	public Result eval(LogicContext context, Patient patient, Map<String, Object> parameters) throws LogicException;
	
	/**
	 * Returns the list of arguments.
	 * 
	 * @return list of arguments or null if no arguments
	 */
	public Set<RuleParameterInfo> getParameterList();
	
	/**
	 * Returns a list of dependencies (tokens for rules upon which this rule may depend).
	 * 
	 * @return tokens for all rules that may be called by this rule
	 */
	// TODO: it would be better to be able to query for dependency on both rules
	// and/or data source keys
	public String[] getDependencies();
	
	/**
	 * Gets the time (in seconds) during which the Rule's results are considered to be valid. This
	 * is used to prevent the use of invalid (old) items from a cache
	 * 
	 * @return duration (in seconds) the results are considered valid for this rule
	 */
	public int getTTL();
	
	/**
	 * Gets the default datatype that the rule returns, when supplied with a given token. While
	 * results are loosely typed, this method allows rules to declare a default datatype to simplify
	 * user interfaces and defaults when working with rules.
	 * 
	 * @return datatype
	 */
	public Datatype getDefaultDatatype();
	
}
