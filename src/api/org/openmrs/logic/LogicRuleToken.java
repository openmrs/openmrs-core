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

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.openmrs.Auditable;
import org.openmrs.BaseOpenmrsObject;
import org.openmrs.User;

/**
 * This class will hold a single entry for any registered token from the logic service. Each token
 * will be associated will multiple tags. Token is not a
 * <code>Rule<code>. Token is  the ingredient to instantiate a rule. You can think of
 * a LogicToken is a serialized version of a <code>Rule</code>
 */
public class LogicRuleToken extends BaseOpenmrsObject implements Auditable {
	
	private Integer ruleTokenId;
	
	private User creator;
	
	private Date dateCreated;
	
	private User changedBy;
	
	private Date dateChanged;
	
	// token to be registered
	private String token;
	
	// class name for the registered token that will be used to instantiate the Rule object
	private String className;
	
	// initializer for the rule object
	private String state;
	
	// tags associated with this token
	private Set<String> ruleTokenTags;
	
	/**
     * 
     */
	public LogicRuleToken() {
	}
	
	/**
     * 
     */
	public LogicRuleToken(String token, Rule rule) {
		setToken(token);
		setClassName(rule.getClass().getCanonicalName());
		if (StatefulRule.class.isAssignableFrom(rule.getClass()))
			setState(((StatefulRule) rule).saveToString());
	}
	
	/**
	 * @see org.openmrs.Auditable#getChangedBy()
	 */
	public User getChangedBy() {
		return changedBy;
	}
	
	/**
	 * @see org.openmrs.Auditable#getCreator()
	 */
	public User getCreator() {
		return creator;
	}
	
	/**
	 * @see org.openmrs.Auditable#getDateChanged()
	 */
	public Date getDateChanged() {
		return dateChanged;
	}
	
	/**
	 * @see org.openmrs.Auditable#getDateCreated()
	 */
	public Date getDateCreated() {
		return dateCreated;
	}
	
	/**
	 * @see org.openmrs.Auditable#setChangedBy(org.openmrs.User)
	 */
	public void setChangedBy(User changedBy) {
		this.changedBy = changedBy;
	}
	
	/**
	 * @see org.openmrs.Auditable#setCreator(org.openmrs.User)
	 */
	public void setCreator(User creator) {
		this.creator = creator;
	}
	
	/**
	 * @see org.openmrs.Auditable#setDateChanged(java.util.Date)
	 */
	public void setDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
	}
	
	/**
	 * @see org.openmrs.Auditable#setDateCreated(java.util.Date)
	 */
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	public Integer getId() {
		return getRuleTokenId();
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	public void setId(Integer id) {
		setRuleTokenId(id);
	}
	
	/**
	 * @return the token
	 */
	public String getToken() {
		return token;
	}
	
	/**
	 * @param token the token to set
	 */
	public void setToken(String token) {
		this.token = token;
	}
	
	/**
	 * @return the className
	 */
	public String getClassName() {
		return className;
	}
	
	/**
	 * @param className the className to set
	 */
	public void setClassName(String className) {
		this.className = className;
	}
	
	/**
	 * @return the state
	 */
	public String getState() {
		return state;
	}
	
	/**
	 * @param state the state to set
	 */
	public void setState(String state) {
		this.state = state;
	}
	
	/**
	 * @return the tokenTags
	 */
	public Set<String> getRuleTokenTags() {
		if (ruleTokenTags == null)
			ruleTokenTags = new HashSet<String>();
		return ruleTokenTags;
	}
	
	/**
	 * @param ruleTokenTags the ruleTokenTags to set
	 */
	public void setRuleTokenTags(Set<String> ruleTokenTags) {
		this.ruleTokenTags = ruleTokenTags;
	}
	
	/**
	 * @return the tokenId
	 */
	public Integer getRuleTokenId() {
		return ruleTokenId;
	}
	
	/**
	 * @param tokenId the tokenId to set
	 */
	public void setRuleTokenId(Integer tokenId) {
		this.ruleTokenId = tokenId;
	}
	
	/**
	 * @param tag the tag that will be added to the set of tags for this token
	 */
	public void addTag(String tag) {
		getRuleTokenTags().add(tag);
	}
	
	/**
	 * @param tag the tag to be removed from set of tags for this token
	 */
	public void removeTag(String tag) {
		getRuleTokenTags().remove(tag);
	}
	
	/**
	 * @param tag
	 * @return
	 */
	public boolean hasTag(String tag) {
		return getRuleTokenTags().contains(tag);
	}
}
