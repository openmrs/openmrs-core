/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.logic;

import java.util.Map;
import java.util.Set;

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
	 * @param context the context this rule is being evaluated in
	 * @param patientId id of the patient for whom rule is to be calculated
	 * @param parameters parameters passed to this rule
	 * @return result of the rule for the given patient with given criteria applied
	 * @throws LogicException TODO
	 */
	public Result eval(LogicContext context, Integer patientId, Map<String, Object> parameters) throws LogicException;
	
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
