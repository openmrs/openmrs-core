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

import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.logic.LogicException;
import org.openmrs.logic.LogicRuleToken;
import org.openmrs.logic.Rule;
import org.openmrs.logic.StatefulRule;
import org.openmrs.logic.db.LogicRuleTokenDAO;
import org.openmrs.logic.result.Result.Datatype;
import org.openmrs.logic.rule.ReferenceRule;
import org.openmrs.logic.rule.RuleParameterInfo;

/**
 * A helper class used internally by the logic service to fetch <code>Rule</code>s by token and to
 * keep track of tags and tokens assigned to rules. If a token starts with "%%", then it is treated
 * as a special <em>reference rule</em>, which means that the token name is expected to be in the
 * form: <code>%%source.key</code>. Where the source is the name of a registered logic data source
 * and the key is a valid key for that data source.
 * 
 * @see org.openmrs.logic.datasource.LogicDataSource
 */
class RuleFactory {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	private LogicRuleTokenDAO logicRuleTokenDAO;
	
	/**
	 * Default constructor
	 */
	public RuleFactory() {
	}
	
	/**
	 * @param logicRuleTokenDAO the logicRuleTokenDAO to set
	 */
	public void setLogicRuleTokenDAO(LogicRuleTokenDAO logicRuleTokenDAO) {
		this.logicRuleTokenDAO = logicRuleTokenDAO;
	}
	
	/**
	 * Gets the rule registered under a given token
	 * 
	 * @param token token under which the rule was registered
	 * @return the rule registered with the given token
	 * @throws LogicException
	 */
	public Rule getRule(String token) throws LogicException {
		Rule rule = null;
		if (token == null)
			throw new LogicException("Token cannot be null");
		
		if (token.startsWith("%%"))
			return new ReferenceRule(token.substring(2));
		
		LogicRuleToken logicToken = logicRuleTokenDAO.getLogicRuleToken(token);
		if (logicToken == null)
			throw new LogicException("No token \"" + token + "\" registered");
		
		try {
			
			Class<?> c = Context.loadClass(logicToken.getClassName());
			Object obj = c.newInstance();
			rule = (Rule) obj;
		}
		catch (InstantiationException e) {
			log.error("Error creating new instance of Rule", e);
		}
		catch (IllegalAccessException e) {
			log.error("Error creating new instance of Rule", e);
		}
		catch (ClassNotFoundException e) {
			log.error("Error creating new instance of Rule", e);
		}
		
		if (StringUtils.isNotBlank(logicToken.getState()))
			((StatefulRule) rule).restoreFromString(logicToken.getState());
		
		return rule;
		
	}
	
	/**
	 * Returns all registered tokens
	 * 
	 * @return all registered tokens
	 */
	public List<String> getAllTokens() {
		return logicRuleTokenDAO.getAllTokens();
	}
	
	/**
	 * Returns tokens containing a particular string
	 * 
	 * @param token lookup string
	 * @return all tokens that contain the lookup string
	 */
	public List<String> findTokens(String token) {
		return logicRuleTokenDAO.getTokens(token);
	}
	
	/**
	 * Registers a rule under the given token
	 * 
	 * @param token token under which to register the rule
	 * @param rule the rule to be registered
	 * @throws LogicException
	 */
	public void addRule(String token, Rule rule) throws LogicException {
		saveRule(token, null, rule);
	}
	
	/**
	 * Updates a rule that was previously registered
	 * 
	 * @param token token under which the rule was originally registered
	 * @param rule new instance of the rule to replace the current version
	 * @throws LogicException
	 */
	public void updateRule(String token, Rule rule) throws LogicException {
		saveRule(token, null, rule);
	}
	
	/*
	 * Internal process to save the rule. Get the logic token and then perform update
	 * or create a new entry in the database.
	 * 
	 * @param token
	 * @param tags
	 * @param rule
	 */
	private void saveRule(String token, String[] tags, Rule rule) {
		if (token != null) {
			LogicRuleToken logicToken = logicRuleTokenDAO.getLogicRuleToken(token);
			if (logicToken != null) {
				// the parameter for updating a rule are token and rule
				logicToken.setClassName(rule.getClass().getCanonicalName());
				if (StatefulRule.class.isAssignableFrom(rule.getClass()))
					logicToken.setState(((StatefulRule) rule).saveToString());
				// update the audit section
				// logicToken.setChangedBy(Context.getAuthenticatedUser());
				// logicToken.setDateChanged(new Date());
			} else {
				// create a new token and then put the associated tags to the logic token
				logicToken = new LogicRuleToken(token, rule);
				if (tags != null) {
					for (int i = 0; i < tags.length; i++) {
						logicToken.addTag(tags[i]);
					}
				}
				// fill the audit section
				// logicToken.setCreator(Context.getAuthenticatedUser());
				// logicToken.setDateCreated(new Date());
			}
			logicRuleTokenDAO.saveLogicRuleToken(logicToken);
		}
	}
	
	/**
	 * Unregister a rule
	 * 
	 * @param token token under which the rule was registered
	 * @throws LogicException
	 */
	public void removeRule(String token) throws LogicException {
		LogicRuleToken logicToken = logicRuleTokenDAO.getLogicRuleToken(token);
		if (logicToken != null)
			logicRuleTokenDAO.deleteLogicRuleToken(logicToken);
		else
			throw new LogicException("Cannot delete missing token \"" + token + "\"");
	}
	
	/**
	 * Registers a rule and, at the same time, assigns 1-to-n tags to the rule
	 * 
	 * @param token unique token under which the rule will be registered. This token is used to
	 *            retrieve the rule later.
	 * @param tags 1-to-n tags (words/phrases) to be attached to the rule. Tags can be used to
	 *            categorize rules for easier lookup and presentation within user interfaces. Tags
	 *            do not need to be unique.
	 * @param rule the rule being registered
	 * @throws LogicException
	 */
	public void addRule(String token, String[] tags, Rule rule) throws LogicException {
		saveRule(token, tags, rule);
	}
	
	/**
	 * Adds a tag to a previously registered token
	 * 
	 * @param token previous registered token
	 * @param tag new tag (word/phrase) to further categorize or organize the token
	 */
	public void addTokenTag(String token, String tag) {
		LogicRuleToken logicToken = logicRuleTokenDAO.getLogicRuleToken(token);
		if (logicToken != null) {
			logicToken.addTag(tag);
			logicRuleTokenDAO.saveLogicRuleToken(logicToken);
		}
	}
	
	/**
	 * Returns all tags that match a given string
	 * 
	 * @param partialTag any tags containing this string will be returned
	 * @return <code>List<String></code> of the matching tags
	 */
	public List<String> findTags(String partialTag) {
		return logicRuleTokenDAO.getTags(partialTag);
	}
	
	/**
	 * Returns all tags attached to a given token
	 * 
	 * @param token <code>String</code> token to search for
	 * @return <code>List<String></code> object of all tags attached to given token
	 */
	public Set<String> getTagsByToken(String token) {
		LogicRuleToken logicToken = logicRuleTokenDAO.getLogicRuleToken(token);
		return logicToken.getRuleTokenTags();
	}
	
	/**
	 * Returns all tokens related to a given tag
	 * 
	 * @param tag <code>String</code> tag to search for
	 * @return <code>List<String></code> object of all tokens related to the given tag
	 */
	public List<String> getTokensByTag(String tag) {
		return logicRuleTokenDAO.getTokensByTag(tag);
	}
	
	/**
	 * Removes a tag from a token
	 * 
	 * @param token token that was previously tagged
	 * @param tag the tag to be removed from the token
	 */
	public void removeTokenTag(String token, String tag) {
		LogicRuleToken logicToken = logicRuleTokenDAO.getLogicRuleToken(token);
		if (logicToken.getRuleTokenTags().contains(tag)) {
			logicToken.removeTag(tag);
			logicRuleTokenDAO.saveLogicRuleToken(logicToken);
		}
	}
	
	/**
	 * Returns the default data type for a rule associated with a given token. While results are
	 * loosely typed, a default data type can be helpful in managing rules within a user interface
	 * or providing defaults
	 * 
	 * @param token String token to match
	 * @return The default <code>Datatype</code> registered under given token
	 */
	public Datatype getDefaultDatatype(String token) {
		try {
			Rule rule = getRule(token);
			if (rule != null)
				return rule.getDefaultDatatype();
		}
		catch (LogicException e) {
			log.error("Error generating rule from logic token", e);
		}
		return null;
	}
	
	/**
	 * Returns the expected parameters for a given rule
	 * 
	 * @param token <code>String</code> token under which the rule was registered
	 * @return <code>Set<RuleParameterInfo></code> of the expected parameters for the given rule
	 */
	public Set<RuleParameterInfo> getParameterList(String token) {
		try {
			Rule rule = getRule(token);
			if (rule != null)
				return rule.getParameterList();
		}
		catch (LogicException e) {
			log.error("Error generating rule from logic token", e);
		}
		return null;
	}
	
}
