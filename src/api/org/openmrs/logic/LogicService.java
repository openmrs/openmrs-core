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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openmrs.Cohort;
import org.openmrs.Patient;
import org.openmrs.logic.datasource.LogicDataSource;
import org.openmrs.logic.result.Result;
import org.openmrs.logic.result.Result.Datatype;
import org.openmrs.logic.rule.RuleParameterInfo;

/**
 * The Logic Service provides a mechanism for both registering and consuming business logic in the
 * form of logic <em>rules</em>. Rules may be run against a single patient or a set of patients.
 * Rules are registered under a unique string <em>token</em>. Later evaluation and/or retrieval of
 * the rule is done through the token. Tokens can be tagged with any number of string word/phrases
 * to simplify organization and lookup of tokens. Data source results can be obtained directly by
 * using a token in the form <em>@foo.bar</em>, where <em>foo</em> is the logic data source name and
 * <em>bar</em> is the key for that data source. For example, the token <em>@person.gender</em> is a
 * direct reference to the <em>gender</em> key of the <em>person</em> logic data source. <h3>Example
 * Usage</h3>
 * 
 * <pre>
 *   Patient myPatient = Context.getPatientService().getPatient(123);
 *   LogicService logicService = Context.getLogicService();
 *   Result result = logicService.eval(myPatient, "HIV POSITIVE");
 *   if (result.toBoolean()) {
 *     // patient is HIV positive
 *   }
 * </pre>
 * 
 * Results can be derived with specific criteria as well. For example, to fetch the maximum CD4
 * count within the past six months:
 * 
 * <pre>
 *   Result result = logicService.eval(myPatient, new LogicCriteria("CD4 COUNT")
 *     .within(Duration.months(6)).max();
 * </pre>
 * 
 * or within 6 months of 11-November-2006:
 * 
 * <pre>
 *   Calendar calendar = Calendar.getInstance();
 *   calendar.set(2006, 11, 11);
 *   Date targetDate = calendar.getTime();
 *   Result result = logicService.eval(myPatient, new LogicCriteria("CD4 COUNT")
 *     .asOf(targetDate).within(Duration.months(6)).max();
 * </pre>
 * 
 * @see org.openmrs.logic.Rule
 * @see org.openmrs.logic.LogicCriteria
 * @see org.openmrs.logic.datasource.LogicDataSource
 */
public interface LogicService {
	
	/**
	 * Fetch all known (registered) tokens
	 * 
	 * @return all known (registered) tokens
	 */
	public Set<String> getTokens();
	
	/**
	 * Fetch all known (registered) tokens matching a given string
	 * 
	 * @param token full or partial token name
	 * @return all tokens containing the given string
	 */
	public Set<String> findToken(String token);
	
	/**
	 * Registers a new rule with the logic service.
	 * 
	 * @param token the lookup key ("token") for this rule
	 * @param rule new rule to be registered
	 * @throws LogicException
	 * @see org.openmrs.logic.Rule
	 */
	public void addRule(String token, Rule rule) throws LogicException;
	
	/**
	 * Registers a new rule with the logic service, associating the tags with the given token
	 * 
	 * @param token the unique lookup key ("token") for this rule
	 * @param tags words or phrases associated with this token (do not need to be unique)
	 * @param rule new rule to be registered
	 * @throws LogicException
	 */
	public void addRule(String token, String[] tags, Rule rule) throws LogicException;
	
	/**
	 * Gets the rule registered under a given token
	 * 
	 * @param token lookup key ("token") under which the rule is registered
	 * @return rule registered under the given token
	 * @throws LogicException if no rule by that name is found
	 */
	public Rule getRule(String token) throws LogicException;
	
	/**
	 * Update a rule that has previously been registered
	 * 
	 * @param token lookup key ("token") for the rule to be updated
	 * @param rule new version of rule (replaces existing rule)
	 * @throws LogicException
	 */
	public void updateRule(String token, Rule rule) throws LogicException;
	
	/**
	 * Removes a rule from the logic service
	 * 
	 * @param token lookup key ("token") under which rule to be removed is registered
	 * @throws LogicException
	 */
	public void removeRule(String token) throws LogicException;
	
	/**
	 * Evaluates a rule for a given patient, given the token for the rule.
	 * 
	 * @param who patient for whom the rule is to be calculated
	 * @param token lookup key for rule to be calculated
	 * @return patient-specific result from given rule
	 * @throws LogicException
	 */
	public Result eval(Patient who, String token) throws LogicException;
	
	/**
	 * Evaluates a rule for a given patient, given a token and parameters for the rule.
	 * 
	 * @param who patient for whom the rule is to be calculated
	 * @param token lookup key for rule to be calculated
	 * @param parameters parameters to be passed to the rule
	 * @return patient-specific result from given rule
	 * @throws LogicException
	 */
	public Result eval(Patient who, String token, Map<String, Object> parameters) throws LogicException;
	
	/**
	 * Evaluates a query for a given patient
	 * 
	 * @param who patient for whom the query is to be run
	 * @param criteria question to be answered (along with the token) for the given patient
	 * @return result of query
	 * @throws LogicException
	 */
	public Result eval(Patient who, LogicCriteria criteria) throws LogicException;
	
	/**
	 * Evaluates a query for a given patient
	 * 
	 * @param who patient for whom the query is to be run
	 * @param criteria question to be answered (along with the token) for the given patient
	 * @param args arguments to be passed to the rule
	 * @return result of query
	 * @throws LogicException
	 */
	public Result eval(Patient who, LogicCriteria criteria, Map<String, Object> parameters) throws LogicException;
	
	/**
	 * Evaluates a query over a list of patients
	 * 
	 * @param who patients for whom the query is to be run
	 * @param token concept to be looked up for each patient
	 * @return result for each patient
	 * @throws LogicException
	 */
	public Map<Integer, Result> eval(Cohort who, String token) throws LogicException;
	
	/**
	 * Evaluates a query over a list of patients
	 * 
	 * @param who patients for whom the query is to be run
	 * @param token concept to be looked up for each patient
	 * @param parameters parameters to be passed to the rule
	 * @return result for each patient
	 * @throws LogicException
	 */
	public Map<Integer, Result> eval(Cohort who, String token, Map<String, Object> parameters) throws LogicException;
	
	/**
	 * Evaluates a query over a list of patients
	 * 
	 * @param who patients for whom the query is to be run
	 * @param criteria question to be answered (along with the token) for each patient
	 * @return result for each patient
	 * @throws LogicException
	 */
	public Map<Integer, Result> eval(Cohort who, LogicCriteria criteria) throws LogicException;
	
	/**
	 * Evaluates a query over a list of patients
	 * 
	 * @param who patients for whom the query is to run
	 * @param criteria question to be answered (along with the token) for each patient
	 * @param parameters arguments to be passed to the rule
	 * @return result for each patient
	 * @throws LogicException
	 */
	public Map<Integer, Result> eval(Cohort who, LogicCriteria criteria, Map<String, Object> parameters)
	                                                                                                    throws LogicException;
	
	/**
	 * Evaluates a collection of queries for a set of patients
	 * 
	 * @param who patients for whom the queries are to be run
	 * @param tokens tokens to be calculated
	 * @param criterias parallel list of criteria to be applied for each token
	 * @return results for each patient
	 * @throws LogicException
	 */
	
	public Map<LogicCriteria, Map<Integer, Result>> eval(Cohort who, List<LogicCriteria> criterias) throws LogicException;
	
	/**
	 * Adds a tag to the given token.
	 * 
	 * @param token
	 * @param tag
	 */
	public void addTokenTag(String token, String tag);
	
	/**
	 * Removes a token's previously assigned tag.
	 * 
	 * @param token
	 * @param tag
	 */
	public void removeTokenTag(String token, String tag);
	
	/**
	 * Gets all tags associated with this token.
	 * 
	 * @param token token to look up by
	 * @return collection of tags
	 */
	public Collection<String> getTagsByToken(String token);
	
	/**
	 * Gets all tokens associated with this tag.
	 * 
	 * @param tag tag to look up by
	 * @return collection of tokens
	 */
	public Set<String> getTokensByTag(String tag);
	
	/**
	 * Performs a partial match search for token tags among all known tokens.
	 * 
	 * @param partialTag partial match string
	 * @return collection of tags
	 */
	public Set<String> findTags(String partialTag);
	
	/**
	 * Fetches the default datatype this token will return when fed to an eval() call. Results
	 * (returned by the logic service) are loosely typed by design; however, the default datatype
	 * can be a useful hint for managing user interfaces or providing default behavior when working
	 * with rules.
	 * 
	 * @param token token to look the datatype up for
	 * @return datatype of the given token
	 */
	public Datatype getDefaultDatatype(String token);
	
	/**
	 * Fetches the parameters expected by a given rule
	 * 
	 * @return list of parameters
	 */
	public Set<RuleParameterInfo> getParameterList(String token);
	
	/**
	 * Adds a data source to the logic service. Data sources provide access to granular data that
	 * can be combined by rules to derive higher level information.
	 * 
	 * @param name name for the data source
	 * @param logicDataSource the data source
	 * @throws LogicException
	 */
	public void registerLogicDataSource(String name, LogicDataSource logicDataSource) throws LogicException;
	
	/**
	 * Get all registered logic data sources
	 * 
	 * @return all registered logic data sources
	 */
	public Map<String, LogicDataSource> getLogicDataSources();
	
	/**
	 * Adds the given logic data sources to the list of current data sources on this logic service
	 * 
	 * @param logicDataSources
	 */
	public void setLogicDataSources(Map<String, LogicDataSource> logicDataSources) throws LogicException;
	
	/**
	 * Get a logic data source by name
	 * 
	 * @param name name of the desired logic data source
	 * @return the logic data source with the given name or <code>null</code> if there is no data
	 *         source registered under the given name (must be an exact match)
	 */
	public LogicDataSource getLogicDataSource(String name);
	
	/**
	 * Remove a logic data source by name
	 * 
	 * @param name name of the logic data source to be unregistered
	 */
	public void removeLogicDataSource(String name);
	
	public void loadRule(String tokenName, String ruleClassName) throws Exception;
	
	public LogicCriteria parseString(String inStr);
	
}
