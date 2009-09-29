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
package org.openmrs.logic.db.hibernate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.logic.LogicRuleToken;
import org.openmrs.logic.LogicService;
import org.openmrs.logic.db.LogicRuleTokenDAO;

/**
 * Implementation of methods defined in the {@link RuleTokenDAO}. The function is not meant to be used directly.
 * Use methods available in the LogicService instead.
 * 
 * @see {@link LogicService}
 * @see {@link Context}
 */
public class HibernateLogicRuleTokenDAO implements LogicRuleTokenDAO {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * Hibernate session factory
	 */
	private SessionFactory sessionFactory;
	
	/**
	 * Set session factory
	 * 
	 * @param sessionFactory
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	/**
	 * @see org.openmrs.logic.db.LogicRuleTokenDAO#deleteLogicRuleToken(org.openmrs.logic.LogicRuleToken)
	 */
	public void deleteLogicRuleToken(LogicRuleToken logicToken) throws DAOException {
		sessionFactory.getCurrentSession().delete(logicToken);
	}
	
	/**
	 * @see org.openmrs.logic.db.LogicRuleTokenDAO#saveLogicRuleToken(org.openmrs.logic.LogicRuleToken)
	 */
	public LogicRuleToken saveLogicRuleToken(LogicRuleToken logicToken) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(logicToken);
		return logicToken;
	}
	
	/**
	 * @see org.openmrs.logic.db.LogicRuleTokenDAO#getLogicRuleToken(java.lang.String)
	 */
	public LogicRuleToken getLogicRuleToken(String token) {
		return (LogicRuleToken) sessionFactory.getCurrentSession().createQuery(
		    "from LogicRuleToken logicRuleToken where logicRuleToken.token = :token").setString("token", token)
		        .uniqueResult();
	}
	
	/**
	 * @see org.openmrs.logic.db.LogicRuleTokenDAO#getAllTags()
	 */
	public List<String> getAllTags() {
		List<String> allTags = new ArrayList<String>();
		Query query = sessionFactory.getCurrentSession().createQuery(
		    "select logicRuleToken from LogicRuleToken logicRuleToken where exists elements(logicRuleToken.ruleTokenTags) ");
		Iterator<?> logicTokens = query.iterate();
		while (logicTokens.hasNext()) {
			LogicRuleToken logicToken = (LogicRuleToken) logicTokens.next();
			allTags.addAll(logicToken.getRuleTokenTags());
		}
		return allTags;
	}
	
	/**
	 * @see org.openmrs.logic.db.LogicRuleTokenDAO#getAllTokens()
	 */
	@SuppressWarnings("unchecked")
	public List<String> getAllTokens() {
		String strQuery = "select logicRuleToken.token from LogicRuleToken logicRuleToken";
		Query query = sessionFactory.getCurrentSession().createQuery(strQuery);
		return query.list();
	}
	
	/**
	 * @see org.openmrs.logic.db.LogicRuleTokenDAO#getTags(java.lang.String)
	 */
	public List<String> getTags(String partialTag) {
		List<String> allTags = new ArrayList<String>();
		Query query = sessionFactory.getCurrentSession().createQuery(
		    "select logicRuleToken from LogicRuleToken logicRuleToken where exists elements(logicRuleToken.ruleTokenTags) ");
		Iterator<?> logicTokens = query.iterate();
		while (logicTokens.hasNext()) {
			LogicRuleToken logicToken = (LogicRuleToken) logicTokens.next();
			for (String tag : logicToken.getRuleTokenTags()) {
				if (tag.matches("^.*" + partialTag + ".*$")) {
					allTags.add(tag);
				}
			}
		}
		return allTags;
	}
	
	/**
	 * @see org.openmrs.logic.db.LogicRuleTokenDAO#getTagsByTokens(java.util.Set)
	 */
	public List<String> getTagsByTokens(Set<String> tokens) {
		List<String> allTags = new ArrayList<String>();
		String strQuery = "select logicRuleToken from LogicRuleToken logicRuleToken where logicRuleToken.token in :tokens";
		Query query = sessionFactory.getCurrentSession().createQuery(strQuery);
		query.setParameterList("tokens", tokens, Hibernate.STRING);
		Iterator<?> logicTokens = query.iterate();
		while (logicTokens.hasNext()) {
			LogicRuleToken logicToken = (LogicRuleToken) logicTokens.next();
			allTags.addAll(logicToken.getRuleTokenTags());
		}
		return allTags;
	}
	
	/**
	 * @see org.openmrs.logic.db.LogicRuleTokenDAO#getTokens(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public List<String> getTokens(String partialToken) {
		String strQuery = "select logicRuleToken.token from LogicRuleToken logicRuleToken where logicRuleToken.token like :partialToken";
		Query query = sessionFactory.getCurrentSession().createQuery(strQuery);
		query.setString("partialToken", "%" + partialToken + "%");
		return query.list();
	}
	
	/**
	 * @see org.openmrs.logic.db.LogicRuleTokenDAO#getTokensByTag(java.lang.String)
	 */
	public List<String> getTokensByTag(String tag) {
		Set<String> tags = new HashSet<String>();
		tags.add(tag);
		
		return getTokensByTags(tags);
	}
	
	/**
	 * @see org.openmrs.logic.db.LogicRuleTokenDAO#getTokensByTags(java.util.Set)
	 */
	@SuppressWarnings("unchecked")
	public List<String> getTokensByTags(Set<String> tags) {
		String strQuery = "select logicRuleToken.token from LogicRuleToken logicRuleToken where :tags in elements(logicRuleToken.ruleTokenTags)";
		Query query = sessionFactory.getCurrentSession().createQuery(strQuery);
		query.setParameterList("tags", tags, Hibernate.STRING);
		return query.list();
	}
}
