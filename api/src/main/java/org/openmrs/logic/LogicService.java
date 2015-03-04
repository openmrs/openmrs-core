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
import org.springframework.transaction.annotation.Transactional;

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
 * Patient myPatient = Context.getPatientService().getPatient(123);
 * LogicService logicService = Context.getLogicService();
 * Result result = logicService.eval(myPatient, &quot;HIV POSITIVE&quot;);
 * if (result.toBoolean()) {
 * 	// patient is HIV positive
 * }
 * </pre>
 * 
 * Results can be derived with specific criteria as well. For example, to fetch the maximum CD4
 * count within the past six months:
 * 
 * <pre>
 *   Result result = logicService.eval(myPatient, new LogicCriteria(&quot;CD4 COUNT&quot;)
 *     .within(Duration.months(6)).max();
 * </pre>
 * 
 * or within 6 months of 11-November-2006:
 * 
 * <pre>
 *   Calendar calendar = Calendar.getInstance();
 *   calendar.set(2006, 11, 11);
 *   Date targetDate = calendar.getTime();
 *   Result result = logicService.eval(myPatient, new LogicCriteria(&quot;CD4 COUNT&quot;)
 *     .asOf(targetDate).within(Duration.months(6)).max();
 * </pre>
 * 
 * @see org.openmrs.logic.Rule
 * @see org.openmrs.logic.LogicCriteria
 * @see org.openmrs.logic.datasource.LogicDataSource
 */
@Transactional
public interface LogicService {
	
	/**
	 * Fetch all known (registered) tokens
	 * 
	 * @return all known (registered) tokens
	 * @deprecated use {@link #getAllTokens()}
	 */
	@Deprecated
	public Set<String> getTokens();
	
	/**
	 * Fetch all known (registered) tokens
	 * 
	 * @return all known (registered) tokens
	 * @should return all registered token
	 */
	public List<String> getAllTokens();
	
	/**
	 * Fetch all known (registered) tokens matching a given string
	 * 
	 * @param token full or partial token name
	 * @return all tokens containing the given string
	 * @deprecated use {@link #getTokens(String)}
	 */
	@Deprecated
	public Set<String> findToken(String token);
	
	/**
	 * Fetch all known (registered) tokens matching a given string
	 * 
	 * @param token full or partial token name
	 * @return all tokens containing the given string
	 * @should return all registered token matching the input fully
	 * @should return all registered token matching the input partially
	 * @should not fail when input is null
	 */
	public List<String> getTokens(String partialToken);
	
	/**
	 * Registers a new rule with the logic service.
	 * 
	 * @param token the lookup key ("token") for this rule
	 * @param rule new rule to be registered
	 * @throws LogicException
	 * @see org.openmrs.logic.Rule
	 * @should not fail when another rule is registered on the same token
	 * @should persist the rule and associate it with the token
	 */
	public void addRule(String token, Rule rule) throws LogicException;
	
	/**
	 * Registers a new rule with the logic service, associating the tags with the given token
	 * 
	 * @param token the unique lookup key ("token") for this rule
	 * @param tags words or phrases associated with this token (do not need to be unique)
	 * @param rule new rule to be registered
	 * @throws LogicException
	 * @should not fail when no tags is specified
	 * @should persist rule with the tags
	 */
	public void addRule(String token, String[] tags, Rule rule) throws LogicException;
	
	/**
	 * Gets the rule registered under a given token
	 * 
	 * @param token lookup key ("token") under which the rule is registered
	 * @return rule registered under the given token
	 * @throws LogicException if no rule by that name is found
	 * @should return Rule associated with the input token
	 * @should fail when no Rule is associated with the input token
	 * @should return ReferenceRule
	 */
	public Rule getRule(String token) throws LogicException;
	
	/**
	 * Update a rule that has previously been registered
	 * 
	 * @param token lookup key ("token") for the rule to be updated
	 * @param rule new version of rule (replaces existing rule)
	 * @throws LogicException
	 * @should update Rule when another Rule is registered under the same token
	 */
	public void updateRule(String token, Rule rule) throws LogicException;
	
	/**
	 * Removes a rule from the logic service
	 * 
	 * @param token lookup key ("token") under which rule to be removed is registered
	 * @throws LogicException
	 * @should remove rule
	 */
	public void removeRule(String token) throws LogicException;
	
	/**
	 * Evaluates a rule for a given patient, given the token for the rule.
	 * 
	 * @param patientId patient for whom the rule is to be calculated
	 * @param expression expression to be parsed and evaluated
	 * @return patient-specific result from given rule
	 * @throws LogicException
	 * @see {@link #parse(String)}
	 * @since 1.6.3, 1.7.2, and 1.8
	 */
	public Result eval(Integer patientId, String expression) throws LogicException;
	
	/**
	 * Evaluates a rule for a given patient, given a token and parameters for the rule.
	 * 
	 * @param patientId patient for whom the rule is to be calculated
	 * @param expression expression to be parsed and evaluated
	 * @param parameters parameters to be passed to the rule
	 * @return patient-specific result from given rule
	 * @throws LogicException
	 * @see {@link #parse(String)}
	 * @since 1.6.3, 1.7.2, and 1.8
	 */
	public Result eval(Integer patientId, String expression, Map<String, Object> parameters) throws LogicException;
	
	/**
	 * Evaluates a query for a given patient
	 * 
	 * @param patientId patient for whom the query is to be run
	 * @param criteria question to be answered (along with the token) for the given patient
	 * @return result of query
	 * @throws LogicException
	 * @since 1.6.3, 1.7.2, and 1.8
	 */
	public Result eval(Integer patientId, LogicCriteria criteria) throws LogicException;
	
	/**
	 * Evaluates a query for a given patient
	 * 
	 * @param patientId <code>Patient</code> for whom the query is to be run
	 * @param criteria <code>Criteria</code> question to be answered (along with the token) for the
	 *            given patient
	 * @param parameters <code>Map</code> of arguments to be passed to the rule
	 * @return <code>Result</code> of query
	 * @throws LogicException
	 * @since 1.6.3, 1.7.2, and 1.8
	 */
	public Result eval(Integer patientId, LogicCriteria criteria, Map<String, Object> parameters) throws LogicException;
	
	/**
	 * Evaluates multiple logic expressions for a single patient.
	 * (The expressions argument is an array and comes last because using a List would give this method
	 * the same type erasure as the {@link LogicCriteria}... version.)  
	 * 
	 * @param patientId which patient to run the rules on 
	 * @param parameters global parameters to be passed to all rule evaluations
	 * @param expressions expressions to be parsed and run
	 * @return results of the rule evaluations
	 * @throws LogicException
	 * @see {@link #parse(String)}
	 * @since 1.6.3, 1.7.2, and 1.8
	 */
	public Map<String, Result> eval(Integer patientId, Map<String, Object> parameters, String... expressions)
	        throws LogicException;
	
	/**
	 * Evaluates multiple {@link LogicCriteria} for a single patient.
	 * (The criteria argument is an array and comes last because using a List would give this method
	 * the same type erasure as the {@link String}... version.)
	 * 
	 * @param patientId which patient to run the rules on 
	 * @param parameters global parameters to be passed to all rule evaluations
	 * @param criteria what criteria to run
	 * @return results of the rule evaluations
	 * @throws LogicException
	 * @since 1.6.3, 1.7.2, and 1.8
	 */
	public Map<LogicCriteria, Result> eval(Integer patientId, Map<String, Object> parameters, LogicCriteria... criteria)
	        throws LogicException;
	
	/**
	 * Evaluates a rule for a given patient, given the token for the rule.
	 * 
	 * @param who patient for whom the rule is to be calculated
	 * @param expression expression to be parsed and evaluated
	 * @return patient-specific result from given rule
	 * @throws LogicException
	 * @deprecated use {@link #eval(Integer, String)}
	 * @see {@link #parse(String)}
	 */
	@Deprecated
	public Result eval(Patient who, String expression) throws LogicException;
	
	/**
	 * Evaluates a rule for a given patient, given a token and parameters for the rule.
	 * 
	 * @param who patient for whom the rule is to be calculated
	 * @param expression expression to be parsed and evaluated
	 * @param parameters parameters to be passed to the rule
	 * @return patient-specific result from given rule
	 * @throws LogicException
	 * @deprecated use {@link #eval(Integer, String, Map)}
	 * @see {@link #parse(String)}
	 */
	@Deprecated
	public Result eval(Patient who, String expression, Map<String, Object> parameters) throws LogicException;
	
	/**
	 * Evaluates a query for a given patient
	 * 
	 * @param who patient for whom the query is to be run
	 * @param criteria question to be answered (along with the token) for the given patient
	 * @return result of query
	 * @throws LogicException
	 * @deprecated use {@link #eval(Integer, LogicCriteria)}
	 */
	@Deprecated
	public Result eval(Patient who, LogicCriteria criteria) throws LogicException;
	
	/**
	 * Evaluates a query for a given patient
	 * 
	 * @param who <code>Patient</code> for whom the query is to be run
	 * @param criteria <code>Criteria</code> question to be answered (along with the token) for the
	 *            given patient
	 * @param parameters <code>Map</code> of arguments to be passed to the rule
	 * @return <code>Result</code> of query
	 * @throws LogicException
	 * @deprecated use {@link #eval(Integer, LogicCriteria, Map)}
	 */
	@Deprecated
	public Result eval(Patient who, LogicCriteria criteria, Map<String, Object> parameters) throws LogicException;
	
	/**
	 * Evaluates a query over a list of patients
	 * 
	 * @param who patients for whom the query is to be run
	 * @param expression expression to be parsed and evaluated for each patient
	 * @return result for each patient
	 * @throws LogicException
	 * @see {@link #parse(String)}
	 */
	public Map<Integer, Result> eval(Cohort who, String expression) throws LogicException;
	
	/**
	 * Evaluates a query over a list of patients
	 * 
	 * @param who patients for whom the query is to be run
	 * @param expression expression to be parsed and evaluated for each patient
	 * @param parameters parameters to be passed to the rule
	 * @return result for each patient
	 * @throws LogicException
	 * @see {@link #parse(String)}
	 */
	public Map<Integer, Result> eval(Cohort who, String expression, Map<String, Object> parameters) throws LogicException;
	
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
	 * @param criterias parallel list of criteria to be evaluated on each patient
	 * @return results for each patient
	 * @throws LogicException
	 */
	
	public Map<LogicCriteria, Map<Integer, Result>> eval(Cohort who, List<LogicCriteria> criterias) throws LogicException;
	
	/**
	 * Adds a tag to the given token.
	 * 
	 * @param token
	 * @param tag
	 * @should add tag for a token
	 */
	public void addTokenTag(String token, String tag);
	
	/**
	 * Removes a token's previously assigned tag.
	 * 
	 * @param token
	 * @param tag
	 * @should remove tag from a token
	 */
	public void removeTokenTag(String token, String tag);
	
	/**
	 * Gets all tags associated with this token.
	 * 
	 * @param token token to look up by
	 * @return collection of tags
	 * @deprecated use {@link #getTokenTags(String)}
	 */
	@Deprecated
	public Collection<String> getTagsByToken(String token);
	
	/**
	 * Gets all tags associated with this token.
	 * 
	 * @param token token to look up by
	 * @return collection of tags
	 * @should return set of tags for a certain token
	 */
	public Set<String> getTokenTags(String token);
	
	/**
	 * Gets all tokens associated with this tag.
	 * 
	 * @param tag tag to look up by
	 * @return collection of tokens
	 * @deprecated use {@link #getTokensWithTag(String)}
	 */
	@Deprecated
	public Set<String> getTokensByTag(String tag);
	
	/**
	 * Gets all tokens associated with this tag.
	 * 
	 * @param tag tag to look up by
	 * @return collection of tokens
	 * @should return set of token associated with a tag
	 */
	public List<String> getTokensWithTag(String tag);
	
	/**
	 * Performs a partial match search for token tags among all known tokens.
	 * 
	 * @param partialTag partial match string
	 * @return collection of tags
	 * @deprecated use {@link #getTags(String)}
	 */
	@Deprecated
	public Set<String> findTags(String partialTag);
	
	/**
	 * Performs a partial match search for token tags among all known tokens.
	 * 
	 * @param partialTag partial match string
	 * @return collection of tags
	 * @should return set of tags matching input tag partially
	 */
	public List<String> getTags(String partialTag);
	
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
	 * @deprecated data sources are now auto-registered via Spring (since Logic module version 0.5)
	 */
	@Deprecated
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
	 * @deprecated data sources are now auto-registered via Spring (since Logic module version 0.5)
	 */
	@Deprecated
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
	 * @deprecated data sources are now auto-registered via Spring (since Logic module version 0.5)
	 */
	@Deprecated
	public void removeLogicDataSource(String name);
	
	/**
	 * Parse a criteria String to create a new LogicCriteria. <br />
	 * <br />
	 * Example: <br />
	 * <code>logicService.parseString("LAST 'CD4 COUNT' < 200");</code>
	 * 
	 * @param inStr LogicCriteria expression in a plain String object.
	 * @return LogicCriteria using all possible operand and operator from the String input
	 * @deprecated use {@link LogicService#parse(String)}
	 */
	@Deprecated
	public LogicCriteria parseString(String inStr);
	
	/**
	 * Parse a criteria String to create a new LogicCriteria. <br />
	 * <br />
	 * Example: <br />
	 * <code>logicService.parseString("LAST 'CD4 COUNT' < 200");</code>
	 * 
	 * @param criteria LogicCriteria expression in a plain String object.
	 * @return LogicCriteria using all possible operand and operator from the String input
	 */
	public LogicCriteria parse(String criteria);
	
}
