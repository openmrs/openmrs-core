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
package org.openmrs.logic.db;

import java.util.List;
import java.util.Set;

import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.logic.LogicRuleToken;
import org.openmrs.logic.LogicService;

/**
 * LogicRuleToken specific function definition. The function is not meant to be used directly.
 * Use methods available in the LogicService instead.
 * 
 * @see {@link LogicService}
 * @see {@link Context}
 */
public interface LogicRuleTokenDAO {
	
	/**
	 * Save a logic token definition from the database.
	 * 
	 * @param logicToken
	 * @return
	 */
	public LogicRuleToken saveLogicRuleToken(LogicRuleToken logicToken) throws DAOException;
	
	/**
	 * Remove a logic token from the database. This will erase the logic token entry from the
	 * database.
	 * 
	 * @param logicToken
	 */
	public void deleteLogicRuleToken(LogicRuleToken logicToken) throws DAOException;
	
	/**
	 * Search for a particular logic token based on the token itself
	 * 
	 * @param token
	 * @return
	 */
	public LogicRuleToken getLogicRuleToken(String token);
	
	/**
	 * Method to get all tags for the registered tokens
	 * 
	 * @return
	 */
	public List<String> getAllTags();
	
	/**
	 * Method to get all registered tokens
	 * 
	 * @return list of all registered rule tokens
	 */
	public List<String> getAllTokens();
	
	/**
	 * Get a list of tags matching an input tag. The list will also contains partially matched tags
	 * from all know tags for the registered token. Example: - hiv will match "hiv positive",
	 * "hiv negative", "positive hiv"
	 * 
	 * This search method will be useful to get a group of token related to a certain tag,
	 * assuming the user put a correct tag to the token.
	 * 
	 * @param partialTag
	 * @return
	 */
	public List<String> getTags(String partialTag);
	
	/**
	 * Get a list of tokens matching an input token. The list will also contains partially matched
	 * token from set of registered tokens. Example: - AGE will match "AGE", "PAGE", "MAGE", "AGED"
	 * 
	 * @param partialToken
	 * @return
	 */
	public List<String> getTokens(String partialToken);
	
	/**
	 * Get a list of tags associated with a set of tokens
	 * 
	 * @param tokens set of tokens that might have set of tags
	 * @return list of all tags associated with the set of tokens
	 */
	public List<String> getTagsByTokens(Set<String> tokens);
	
	/**
	 * Get a list of token that is associated with a set of tags
	 * 
	 * @param tags set of tags related to a token
	 * @return list of tokens related to the tags
	 */
	public List<String> getTokensByTags(Set<String> tags);
	
	/**
	 * Get a list of tokens that is associated to a certain tag.
	 * 
	 * @param tag String related to a token
	 * @return list of token related to the tag
	 */
	public List<String> getTokensByTag(String tag);
}
