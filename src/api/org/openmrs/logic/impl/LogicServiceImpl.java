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
package org.openmrs.logic.impl;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.Patient;
import org.openmrs.logic.LogicContext;
import org.openmrs.logic.LogicCriteria;
import org.openmrs.logic.LogicException;
import org.openmrs.logic.LogicService;
import org.openmrs.logic.Rule;
import org.openmrs.logic.RuleClassLoader;
import org.openmrs.logic.RuleFactory;
import org.openmrs.logic.datasource.LogicDataSource;
import org.openmrs.logic.queryparser.LogicQueryBaseParser;
import org.openmrs.logic.queryparser.LogicQueryLexer;
import org.openmrs.logic.queryparser.LogicQueryTreeParser;
import org.openmrs.logic.result.Result;
import org.openmrs.logic.result.Result.Datatype;
import org.openmrs.logic.rule.RuleParameterInfo;

import antlr.BaseAST;

/**
 * Default implementation of the LogicService. This class should not be used directly. This class,
 * if chosen, is injected into the Context and served up as the LogicService of choice. Use: <code>
 *   LogicService logicService = Context.getLogicService();
 * </code>
 * 
 * @see org.openmrs.api.context.Context
 * @see org.openmrs.api.LogicService
 */
public class LogicServiceImpl implements LogicService {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	private RuleFactory ruleFactory;
	
	private static Map<String, LogicDataSource> dataSources;
	
	/**
	 * Default constructor. Creates a new RuleFactory (and populates it)
	 */
	public LogicServiceImpl() {
		ruleFactory = new RuleFactory();
	}
	
	/**
	 * Clean up after this class. Set the static var to null so that the classloader can reclaim the
	 * space.
	 * 
	 * @see org.openmrs.api.impl.BaseOpenmrsService#onShutdown()
	 */
	public void onShutdown() {
		dataSources = null;
	}
	
	/**
	 * @see org.openmrs.logic.LogicService#getTokens()
	 */
	public Set<String> getTokens() {
		return ruleFactory.getTokens();
	}
	
	/**
	 * @see org.openmrs.logic.LogicService#findToken(java.lang.String)
	 */
	public Set<String> findToken(String token) {
		return ruleFactory.findTokens(token);
	}
	
	/**
	 * @see org.openmrs.logic.LogicService#addRule(java.lang.String, org.openmrs.logic.Rule)
	 */
	public void addRule(String token, Rule rule) throws LogicException {
		ruleFactory.addRule(token, rule);
	}
	
	/**
	 * @see org.openmrs.logic.LogicService#getRule(java.lang.String)
	 */
	public Rule getRule(String token) throws LogicException {
		return ruleFactory.getRule(token);
	}
	
	/**
	 * @see org.openmrs.logic.LogicService#updateRule(java.lang.String, org.openmrs.logic.Rule)
	 */
	public void updateRule(String token, Rule rule) throws LogicException {
		ruleFactory.updateRule(token, rule);
	}
	
	/**
	 * @see org.openmrs.logic.LogicService#removeRule(java.lang.String)
	 */
	public void removeRule(String token) throws LogicException {
		ruleFactory.removeRule(token);
	}
	
	/**
	 * @see org.openmrs.logic.LogicService#eval(org.openmrs.Patient, java.lang.String)
	 */
	public Result eval(Patient who, String token) throws LogicException {
		return eval(who, new LogicCriteria(token));
	}
	
	/**
	 * @see org.openmrs.logic.LogicService#eval(org.openmrs.Patient, java.lang.String,
	 *      java.lang.Object[])
	 */
	public Result eval(Patient who, String token, Map<String, Object> parameters) throws LogicException {
		return eval(who, new LogicCriteria(token, parameters));
	}
	
	/**
	 * @see org.openmrs.logic.LogicService#eval(org.openmrs.Patient, java.lang.String,
	 *      org.openmrs.logic.LogicCriteria)
	 */
	public Result eval(Patient who, LogicCriteria criteria) throws LogicException {
		return eval(who, criteria, criteria.getLogicParameters());
	}
	
	/**
	 * @see org.openmrs.logic.LogicService#eval(org.openmrs.Patient,
	 *      org.openmrs.logic.LogicCriteria, java.lang.Object[])
	 */
	public Result eval(Patient who, LogicCriteria criteria, Map<String, Object> parameters) throws LogicException {
		LogicContext context = new LogicContext(who);
		Result result = context.eval(who, criteria, parameters);
		context = null;
		return result;
	}
	
	/**
	 * @see org.openmrs.logic.LogicService#eval(org.openmrs.Cohort, java.lang.String)
	 */
	public Map<Integer, Result> eval(Cohort who, String token) throws LogicException {
		return eval(who, new LogicCriteria(token));
	}
	
	/**
	 * @see org.openmrs.logic.LogicService#eval(org.openmrs.Cohort, java.lang.String, java.util.Map)
	 */
	public Map<Integer, Result> eval(Cohort who, String token, Map<String, Object> parameters) throws LogicException {
		return eval(who, new LogicCriteria(token, parameters));
	}
	
	/**
	 * @see org.openmrs.logic.LogicService#eval(org.openmrs.Cohort, org.openmrs.logic.LogicCriteria)
	 */
	public Map<Integer, Result> eval(Cohort who, LogicCriteria criteria) throws LogicException {
		return eval(who, criteria, criteria.getLogicParameters());
	}
	
	/**
	 * @see org.openmrs.logic.LogicService#eval(org.openmrs.Cohort, org.openmrs.logic.LogicCriteria,
	 *      java.util.Map)
	 */
	public Map<Integer, Result> eval(Cohort who, LogicCriteria criteria, Map<String, Object> parameters)
	                                                                                                    throws LogicException {
		LogicContext context = new LogicContext(who);
		Map<Integer, Result> resultMap = new Hashtable<Integer, Result>();
		for (Integer pid : who.getMemberIds())
			resultMap.put(pid, context.eval(new Patient(pid), criteria, parameters));
		context = null;
		return resultMap;
	}
	
	/**
	 * @see org.openmrs.logic.LogicService#eval(org.openmrs.Cohort, java.util.List)
	 */
	public Map<LogicCriteria, Map<Integer, Result>> eval(Cohort patients, List<LogicCriteria> criterias)
	                                                                                                    throws LogicException {
		Map<LogicCriteria, Map<Integer, Result>> result = new HashMap<LogicCriteria, Map<Integer, Result>>();
		
		for (LogicCriteria criteria : criterias) {
			result.put(criteria, eval(patients, criteria));
		}
		
		return result;
	}
	
	/**
	 * @see org.openmrs.logic.LogicService#addRule(java.lang.String, java.lang.String[],
	 *      org.openmrs.logic.rule.Rule)
	 */
	public void addRule(String token, String[] tags, Rule rule) throws LogicException {
		ruleFactory.addRule(token, tags, rule);
	}
	
	/**
	 * @see org.openmrs.logic.LogicService#addTokenTag(java.lang.String, java.lang.String)
	 */
	public void addTokenTag(String token, String tag) {
		ruleFactory.addTokenTag(token, tag);
	}
	
	/**
	 * @see org.openmrs.logic.LogicService#findTags(java.lang.String)
	 */
	public Set<String> findTags(String partialTag) {
		return ruleFactory.findTags(partialTag);
	}
	
	/**
	 * @see org.openmrs.logic.LogicService#getTagsByToken(java.lang.String)
	 */
	public Collection<String> getTagsByToken(String token) {
		return ruleFactory.getTagsByToken(token);
	}
	
	/**
	 * @see org.openmrs.logic.LogicService#getTokensByTag(java.lang.String)
	 */
	public Set<String> getTokensByTag(String tag) {
		return ruleFactory.getTokensByTag(tag);
	}
	
	/**
	 * @see org.openmrs.logic.LogicService#removeTokenTag(java.lang.String, java.lang.String)
	 */
	public void removeTokenTag(String token, String tag) {
		ruleFactory.removeTokenTag(token, tag);
	}
	
	/**
	 * @see org.openmrs.logic.LogicService#getDatatype(java.lang.String)
	 */
	public Datatype getDefaultDatatype(String token) {
		return ruleFactory.getDefaultDatatype(token);
	}
	
	public Set<RuleParameterInfo> getParameterList(String token) {
		return ruleFactory.getParameterList(token);
	}
	
	/**
	 * @see org.openmrs.logic.LogicService#registerLogicDataSource(java.lang.String,
	 *      org.openmrs.logic.datasource.LogicDataSource)
	 */
	public void registerLogicDataSource(String name, LogicDataSource dataSource) throws LogicException {
		getLogicDataSources().put(name, dataSource);
	}
	
	/**
	 * @see org.openmrs.logic.LogicService#getDataService()
	 */
	public LogicDataSource getLogicDataSource(String name) {
		return getLogicDataSources().get(name);
	}
	
	/**
	 * @see org.openmrs.logic.LogicService#getLogicDataSources()
	 */
	public Map<String, LogicDataSource> getLogicDataSources() {
		if (dataSources == null)
			dataSources = new Hashtable<String, LogicDataSource>();
		return dataSources;
	}
	
	/**
	 * @see org.openmrs.logic.LogicService#setLogicDataSources(Map)
	 */
	public void setLogicDataSources(Map<String, LogicDataSource> dataSources) throws LogicException {
		for (Map.Entry<String, LogicDataSource> entry : dataSources.entrySet()) {
			registerLogicDataSource(entry.getKey(), entry.getValue());
		}
	}
	
	/**
	 * @see org.openmrs.logic.LogicService#removeLogicDataSource(java.lang.String)
	 */
	public void removeLogicDataSource(String name) {
		dataSources.remove(name);
	}
	
	/**
	 * @see org.openmrs.logic.LogicService#loadRule(java.lang.String, java.lang.String)
	 */
	public void loadRule(String tokenName, String ruleClassName) throws Exception {
		RuleClassLoader ccl = new RuleClassLoader();
		
		Class<?> clas = ccl.loadClass(ruleClassName);
		
		if (clas == null) {
			throw new Exception("Could not load class for rule: " + ruleClassName);
		}
		
		Object obj = clas.newInstance();
		
		if (!(obj instanceof Rule)) {
			throw new Exception("Could not load class for rule: " + ruleClassName
			        + ". The rule must implement the Rule interface.");
		}
		
		this.updateRule(tokenName, (Rule) obj);
	}
	
	/**
	 * @see org.openmrs.logic.LogicService#parseString(java.lang.String)
	 */
	public LogicCriteria parseString(String inStr) {
		
		try {
			if (!inStr.endsWith(";")) {
				inStr += ";";
			}
			byte currentBytes[] = inStr.getBytes();
			
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(currentBytes);
			
			// Create a scanner that reads from the input stream passed to us
			LogicQueryLexer lexer = new LogicQueryLexer(byteArrayInputStream);
			
			// Create a parser that reads from the scanner
			LogicQueryBaseParser parser = new LogicQueryBaseParser(lexer);
			
			// start parsing at the compilationUnit rule
			parser.query_parse();
			
			BaseAST t = (BaseAST) parser.getAST();
			
			//System.out.println(t.toStringTree());     // prints Abstract Syntax Tree
			
			LogicQueryTreeParser treeParser = new LogicQueryTreeParser();
			
			LogicCriteria lc = treeParser.query_AST(t);
			// System.out.println(lc.toString());
			return lc;
		}
		catch (Exception e) {
			log.error(e.getStackTrace());
			return null;
		}
	}
}
