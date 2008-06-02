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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.api.context.Context;
import org.openmrs.logic.result.Result.Datatype;
import org.openmrs.logic.rule.AgeRule;
import org.openmrs.logic.rule.HIVPositiveRule;
import org.openmrs.logic.rule.ReferenceRule;
import org.openmrs.logic.rule.RuleParameterInfo;

/**
 * A helper class used internally by the logic service to fetch
 * <code>Rule</code>s by token and to keep track of tags and tokens assigned
 * to rules. If a token starts with "%%", then it is treated as a special
 * <em>reference rule</em>, which means that the token name is expected to be
 * in the form: <code>%%source.key</code>. Where the source is the name of a
 * registered logic data source and the key is a valid key for that data source.
 * 
 * @see org.openmrs.logic.datasource.LogicDataSource
 */
public class RuleFactory {

    protected final Log log = LogFactory.getLog(getClass());

    /**
     * Maps tokens to instances of rules.  Do not use this directly;
     * instead, call getRuleMap()
     */
    private Map<String, Rule> ruleMap = null;

    /**
     * Maps tokens to 1-n tags
     */
    private Map<String, Set<String>> tagsByToken = new HashMap<String, Set<String>>();

    /**
     * Maps tags to 1-n tokens (inverse of the tagsByToken, maintained for
     * speedy lookups)
     */
    private Map<String, Set<String>> tokensByTag = new HashMap<String, Set<String>>();

    /**
     * Default constructor
     */
    public RuleFactory() {
    }

    private Map<String, Rule> getRuleMap() {
    	if (ruleMap == null) {
    		ruleMap = new HashMap<String, Rule>();
    		init();
    	}
    	return ruleMap;
    }
    /**
     * 
     * Internal initialization. Used as a hack to get some rules registered for
     * testing as the logic service is being developed. This method should be
     * replaced by external initialization steps to register concepts and other
     * sources of rules
     * 
     */
    public void init() {
        // TODO: temporary cheat to get some stuff loaded
        try {
            // TODO this is temporary; it should be read from persistent storage
            ConceptClass cc = Context.getConceptService()
                    .getConceptClassByName("Test");
            List<Concept> testConcepts = Context.getConceptService()
                    .getConceptsByClass(cc);
            for (Concept c : testConcepts) {
                String name = c.getName().getName();
                ruleMap.put(name, new ReferenceRule("obs." + name));
            }
        	cc = Context.getConceptService()
            .getConceptClassByName("Finding");
            List<Concept> findingConcepts = Context.getConceptService()
            .getConceptsByClass(cc);
		    for (Concept c : findingConcepts) {
		        String name = c.getName().getName();
		        ruleMap.put(name, new ReferenceRule("obs." + name));
		    }
    
		    cc = Context.getConceptService()
            .getConceptClassByName("Question");
            List<Concept> questionConcepts = Context.getConceptService()
            .getConceptsByClass(cc);
		    for (Concept c : questionConcepts) {
		        String name = c.getName().getName();
		        ruleMap.put(name, new ReferenceRule("obs." + name));
		    }
		   
            ruleMap.put("HIV POSITIVE", new HIVPositiveRule());

            ruleMap.put("GENDER", getRule("%%person.gender"));
            ruleMap.put("BIRTHDATE", getRule("%%person.birthdate"));
            ruleMap.put("BIRTHDATE ESTIMATED",
                    getRule("%%person.birthdate estimated"));
            ruleMap.put("DEAD", getRule("%%person.dead"));
            ruleMap.put("DEATH DATE", getRule("%%person.death date"));
            ruleMap.put("CAUSE OF DEATH", getRule("%%person.cause of death"));

            ruleMap.put("AGE", new AgeRule());
        } catch (LogicException e) {
            log.error("Error during RuleFactory initialization", e);
        }
    }

    /**
     * 
     * Gets the rule registered under a given token
     * 
     * @param token token under which the rule was registered
     * @return the rule registered with the given token
     * @throws LogicException
     */
    public Rule getRule(String token) throws LogicException {
    	if (token == null)
    		throw new LogicException("Token cannot be null");
    	
        if (token.startsWith("%%"))
            return new ReferenceRule(token.substring(2));

        if (!getRuleMap().containsKey(token))
            throw new LogicException("No token \"" + token + "\" registered");

        return getRuleMap().get(token);
    }

    /**
     * 
     * Returns all known tokens
     * 
     * @return all known tokens
     */
    public Set<String> getTokens() {
        return getRuleMap().keySet();
    }

    /**
     * 
     * Returns tokens containing a particular string
     * 
     * @param token lookup string
     * @return all tokens that contain the lookup string
     */
    public Set<String> findTokens(String token) {
        Set<String> matches = new HashSet<String>();
        for (String t : getTokens()) {
            if (t.contains(token))
                matches.add(token);
        }
        return matches;
    }

    /**
     * 
     * Registers a rule under the given token
     * 
     * @param token token under which to register the rule
     * @param rule the rule to be registered
     * @throws LogicException
     */
    public void addRule(String token, Rule rule) throws LogicException {
        if (getRuleMap().containsKey(token))
            throw new LogicException("Duplicate token \"" + token + "\"");
        getRuleMap().put(token, rule);
    }

    /**
     * 
     * Updates a rule that was previously registered
     * 
     * @param token token under which the rule was originally registered
     * @param rule new instance of the rule to replace the current version
     * @throws LogicException
     */
    public void updateRule(String token, Rule rule) throws LogicException {
        if (!getRuleMap().containsKey(token))
            throw new LogicException("Cannot update missing token \"" + token
                    + "\"");
        getRuleMap().put(token, rule);
    }

    /**
     * 
     * Unregister a rule
     * 
     * @param token token under which the rule was registered
     * @throws LogicException
     */
    public void removeRule(String token) throws LogicException {
        if (getRuleMap().containsKey(token))
            getRuleMap().remove(token);
        else
            throw new LogicException("Cannot delete missing token \"" + token
                    + "\"");
    }

    /**
     * 
     * Registers a rule and, at the same time, assigns 1-to-n tags to the rule
     * 
     * @param token unique token under which the rule will be registered. This
     *        token is used to retrieve the rule later.
     * @param tags 1-to-n tags (words/phrases) to be attached to the rule. Tags
     *        can be used to categorize rules for easier lookup and presentation
     *        within user interfaces. Tags do not need to be unique.
     * @param rule the rule being registered
     * @throws LogicException
     */
    public void addRule(String token, String[] tags, Rule rule)
            throws LogicException {
        for (int i = 0; i < tags.length; i++)
            addTokenTag(token, tags[i]);
        addRule(token, rule);
    }

    /**
     * 
     * Adds a tag to a previously registered token
     * 
     * @param token previous registered token
     * @param tag new tag (word/phrase) to further categorize or organize the token
     */
    public void addTokenTag(String token, String tag) {
        if (!tagsByToken.containsKey(token))
            tagsByToken.put(token, new HashSet<String>());
        if (!tagsByToken.get(token).contains(tag))
            tagsByToken.get(token).add(tag);
        if (!tokensByTag.containsKey(tag))
            tokensByTag.put(tag, new HashSet<String>());
        if (!tokensByTag.get(tag).contains(token))
            tokensByTag.get(tag).add(token);
    }

    /**
     * 
     * Returns all tags that match a given string
     * 
     * @param partialTag any tags containing this string will be returned
     * @return
     */
    public Set<String> findTags(String partialTag) {

        Set<String> resultTags = new HashSet<String>();
        for (String tag : tokensByTag.keySet()) {
            if (tag.contains(partialTag))
                resultTags.add(tag);
        }

        return resultTags;
    }

    /**
     * 
     * Returns all tags attached to a given token
     * 
     * @param token all tags attached to this token will be returned
     * @return
     */
    public Set<String> getTagsByToken(String token) {
        return tagsByToken.get(token);
    }

    /**
     * 
     * Returns all tokens related to a given tag
     * 
     * @param tag all tokens that have been tagged with the given tag will be returned
     * @return
     */
    public Set<String> getTokensByTag(String tag) {
        return tokensByTag.get(tag);
    }

    /**
     * 
     * Removes a tag from a token 
     * 
     * @param token token that was previously tagged
     * @param tag the tag to be removed from the token
     */
    public void removeTokenTag(String token, String tag) {
        if (tagsByToken.containsKey(token)
                && tagsByToken.get(token).contains(tag))
            tagsByToken.get(token).remove(tag);
        if (tokensByTag.containsKey(tag)
                && tokensByTag.get(tag).contains(token))
            tokensByTag.get(tag).remove(token);
    }

    /**
     * 
     * Returns the default data type for a rule associated with a given token.  While results
     * are loosely typed, a default data type can be helpful in managing rules within a user
     * interface or providing defaults
     * 
     * @param token the default data type of the rule registered under this token will be returned
     * @return
     */
    public Datatype getDefaultDatatype(String token) {
        if (getRuleMap().containsKey(token))
            return getRuleMap().get(token).getDefaultDatatype();
        return null;
    }

    /**
     * 
     * Returns the expected parameters for a given rule
     * 
     * @param token token under which the rule was registered
     * @return
     */
    public Set<RuleParameterInfo> getParameterList(String token) {
        if (getRuleMap().containsKey(token))
            return getRuleMap().get(token).getParameterList();
        return null;
    }

}
