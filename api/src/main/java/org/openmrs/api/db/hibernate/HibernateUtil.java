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
package org.openmrs.api.db.hibernate;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.MatchMode;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.HSQLDialect;
import org.hibernate.engine.SessionFactoryImplementor;
import org.openmrs.OpenmrsMetadata;
import org.openmrs.util.LocalizedStringUtil;

/**
 * This class holds common methods and utilities that are used across the hibernate related classes
 */
public class HibernateUtil {
	
	private static Log log = LogFactory.getLog(HibernateUtil.class);
	
	private static Dialect dialect = null;
	
	private static Boolean isHSQLDialect = null;
	
	/**
	 * Check and cache whether the currect dialect is HSQL or not. This is needed because some
	 * queries are different if in the hsql world as opposed to the mysql/postgres world
	 * 
	 * @param sessionFactory
	 * @return true/false whether we're in hsql right now or not
	 */
	public static boolean isHSQLDialect(SessionFactory sessionFactory) {
		
		if (isHSQLDialect == null)
			// check and cache the dialect
			isHSQLDialect = HSQLDialect.class.getName().equals(getDialect(sessionFactory).getClass().getName());
		
		return isHSQLDialect;
	}
	
	/**
	 * Fetch the current Dialect of the given SessionFactory
	 * 
	 * @param sessionFactory SessionFactory to pull the dialect from
	 * @return Dialect of sql that this connection/session is using
	 */
	public static Dialect getDialect(SessionFactory sessionFactory) {
		
		// return cached dialect
		if (dialect != null)
			return dialect;
		
		SessionFactoryImplementor implementor = (SessionFactoryImplementor) sessionFactory;
		dialect = implementor.getDialect();
		
		if (log.isDebugEnabled())
			log.debug("Getting dialect for session: " + dialect);
		
		return dialect;
	}
	
	/**
	 * @see HibernateUtil#escapeSqlWildcards(String, Connection)
	 */
	public static String escapeSqlWildcards(String oldString, SessionFactory sessionFactory) {
		return escapeSqlWildcards(oldString, sessionFactory.getCurrentSession().connection());
	}
	
	/**
	 * Escapes all sql wildcards in the given string, returns the same string if it doesn't contain
	 * any sql wildcards
	 * 
	 * @param oldString the string in which to escape the sql wildcards
	 * @param connection The underlying database connection
	 * @return the string with sql wildcards escaped if any found otherwise the original string is
	 *         returned
	 */
	public static String escapeSqlWildcards(String oldString, Connection connection) {
		
		//replace all sql wildcards if any
		if (!StringUtils.isBlank(oldString)) {
			String escapeCharacter = "";
			
			try {
				//get the database specific escape character from the metadata
				escapeCharacter = connection.getMetaData().getSearchStringEscape();
			}
			catch (SQLException e) {
				log.warn("Error generated", e);
			}
			//insert an escape character before each sql wildcard in the search phrase
			return StringUtils.replaceEach(oldString, new String[] { "%", "_", "*", "'" }, new String[] {
			        escapeCharacter + "%", escapeCharacter + "_", escapeCharacter + "*", escapeCharacter + "'" });
		} else
			return oldString;
	}
	
	/**
	 * Add equal criterion for the localized column of {@link OpenmrsMetadata} object.
	 * 
	 * @param value - value to match
	 * @param propertyName - column to match in
	 * @param criteria - criteria to append search conditions
	 * @see #getEqCriterionForLocalizedColumn(String, String)
	 * @since 1.9
	 */
	public static void addEqCriterionForLocalizedColumn(String value, String propertyName, Criteria criteria) {
		criteria.add(getEqCriterionForLocalizedColumn(value, propertyName));
	}
	
	/**
	 * Add like criterion for the localized column of {@link OpenmrsMetadata} object.
	 * 
	 * @param value - value to match
	 * @param propertyName - column to match in
	 * @param criteria - criteria to append search conditions
	 * @param caseSensitive - if caseSensitive is false, do sql query similar to hibernate's "ilike"
	 * @param mode - specify match mode, match from start, end, anywhere, or exact
	 * @see #getLikeCriterionForLocalizedColumn(String, String, boolean, MatchMode)
	 * @since 1.9
	 */
	public static void addLikeCriterionForLocalizedColumn(String value, String propertyName, Criteria criteria,
	                                                      boolean caseSensitive, MatchMode mode) {
		criteria.add(getLikeCriterionForLocalizedColumn(value, propertyName, caseSensitive, mode));
	}
	
	/**
	 * Get equal criterion for the localized column of {@link OpenmrsMetadata} object.
	 * 
	 * @param value - value to match
	 * @param propertyName - column to match in
	 * @return criterion to be used as hibernate's equal query
	 * @since 1.9
	 * @should return correct criterion when has unlocalized value only
	 * @should return correct criterion when has localized values only
	 * @should return correct criterion when has unlocalized and localized values
	 */
	public static Criterion getEqCriterionForLocalizedColumn(String value, String propertyName) {
		Criterion leftExp = Expression.eq(propertyName, value);
		String searchValue = LocalizedStringUtil.PARTITION + LocalizedStringUtil.escapeDelimiter(value)
		        + LocalizedStringUtil.SPLITTER;
		Criterion rightExp = Expression.like(propertyName, searchValue, MatchMode.ANYWHERE);
		return Expression.or(leftExp, rightExp);
	}
	
	/**
	 * Get like criterion for the localized column of {@link OpenmrsMetadata} object.
	 * 
	 * @param value - value to match
	 * @param propertyName - column to match in
	 * @param caseSensitive - if caseSensitive is false, return sql query similar to hibernate's
	 *            "ilike"
	 * @param mode - specify match mode, match from start, end, anywhere, or exact
	 * @return criterion to be used as hibernate's like or ilike query
	 * @since 1.9
	 * @should return correct criterion when has unlocalized value only
	 * @should return correct criterion when has localized values only
	 * @should return correct criterion when has unlocalized and localized values
	 */
	public static Criterion getLikeCriterionForLocalizedColumn(String value, String propertyName, boolean caseSensitive,
	                                                           MatchMode mode) {
		Criterion leftExp = null;
		Criterion rigthExp = null;
		String searchValue = null;
		
		if (MatchMode.START.equals(mode)) {
			searchValue = LocalizedStringUtil.PARTITION + LocalizedStringUtil.escapeDelimiter(value);
			if (caseSensitive == true) {
				// append expression for unlocalized metadata
				leftExp = Expression.like(propertyName, value, mode);
				// append expression for localized metadata
				rigthExp = Expression.like(propertyName, searchValue, MatchMode.ANYWHERE);
			} else {
				leftExp = Expression.ilike(propertyName, value, mode);
				rigthExp = Expression.ilike(propertyName, searchValue, MatchMode.ANYWHERE);
			}
			return Expression.or(leftExp, rigthExp);
		} else if (MatchMode.END.equals(mode)) {
			searchValue = LocalizedStringUtil.escapeDelimiter(value) + LocalizedStringUtil.SPLITTER;
			if (caseSensitive == true) {
				leftExp = Expression.like(propertyName, value, mode);
				rigthExp = Expression.like(propertyName, searchValue, MatchMode.ANYWHERE);
			} else {
				leftExp = Expression.ilike(propertyName, value, mode);
				rigthExp = Expression.ilike(propertyName, searchValue, MatchMode.ANYWHERE);
			}
			return Expression.or(leftExp, rigthExp);
		} else if (MatchMode.ANYWHERE.equals(mode)) {
			if (caseSensitive == true)// use one expression
				leftExp = Expression.like(propertyName, value, mode);
			else
				leftExp = Expression.ilike(propertyName, value, mode);
			return leftExp;
		} else {/*MatchMode.EXACT.equals(mode)*/
			searchValue = LocalizedStringUtil.PARTITION + LocalizedStringUtil.escapeDelimiter(value)
			        + LocalizedStringUtil.SPLITTER;
			if (caseSensitive == true) {
				leftExp = Expression.like(propertyName, value, mode);
				rigthExp = Expression.like(propertyName, searchValue, MatchMode.ANYWHERE);
				
			} else {
				leftExp = Expression.ilike(propertyName, value, mode);
				rigthExp = Expression.ilike(propertyName, searchValue, MatchMode.ANYWHERE);
			}
			return Expression.or(leftExp, rigthExp);
		}
	}
}
