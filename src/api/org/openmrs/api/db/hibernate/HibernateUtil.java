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

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.MatchMode;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.HSQLDialect;
import org.hibernate.engine.SessionFactoryImplementor;
import org.openmrs.OpenmrsMetadata;
import org.openmrs.api.context.Context;
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
	 * Escapes all sql wildcards in the given string, returns the same string if it doesn't contain
	 * any sql wildcards
	 * 
	 * @param oldString the string in which to escape the sql wildcards
	 * @return the string with sql wildcards escaped if any found otherwise the original string is
	 *         returned
	 */
	public static String escapeSqlWildcards(String oldString, SessionFactory sessionFactory) {
		
		//replace all sql wildcards if any
		if (!StringUtils.isBlank(oldString)) {
			
			String escapeCharacter = "";
			try {
				//get the database specific escape character from the metadata
				escapeCharacter = sessionFactory.getCurrentSession().connection().getMetaData().getSearchStringEscape();
				
			}
			catch (HibernateException e) {
				
				log.warn("Error generated", e);
			}
			catch (SQLException e) {
				
				log.warn("Error generated", e);
			}
			//insert an escape character before each sql wildcard in the search phrase
			return StringUtils.replaceEach(oldString, new String[] { "%", "_", "*" }, new String[] { escapeCharacter + "%",
			        escapeCharacter + "_", escapeCharacter + "*" });
		} else
			return oldString;
		
	}
	
	/**
	 * Add equal criterion for the localized column of {@link OpenmrsMetadata} object.
	 * 
	 * @param value - value to match
	 * @param columnName - column to match in
	 * @param criteria - criteria to append search conditions
	 * @since 1.9
	 */
	public static void addEqCriterionForLocalizedColumn(String value, String columnName, Criteria criteria) {
		// append expression for those unlocalized metadata
		Criterion leftExp = Expression.sql(columnName + " = ?", value, Hibernate.STRING);
		
		// append expression for those localized metadata
		String searchValue = "%" + LocalizedStringUtil.PARTITION + LocalizedStringUtil.escapeDelimiter(value)
		        + LocalizedStringUtil.SPLITTER + "%";
		Criterion rightExp = Expression.sql(columnName + " like ?", searchValue, Hibernate.STRING);
		
		criteria.add(Expression.or(leftExp, rightExp));
	}
	
	/**
	 * Add like criterion for the localized column of {@link OpenmrsMetadata} object.
	 * 
	 * @param value - value to match
	 * @param columnName - column to match in
	 * @param criteria - criteria to append search conditions
	 * @param caseSensitive - if caseSensitive is false, do sql query similar to hibernate's "ilike"
	 * @param mode - specify match mode, match from start, end, anywhere, or exact
	 * @since 1.9
	 */
	public static void addLikeCriterionForLocalizedColumn(String value, String columnName, Criteria criteria,
	                                                      boolean caseSensitive, MatchMode mode) {
		Criterion leftExp = null;
		Criterion rigthExp = null;
		String searchValue = null;
		
		if (MatchMode.START.equals(mode)) {
			if (caseSensitive == true) {
				// append expression for unlocalized metadata
				leftExp = Expression.sql(columnName + " like ?", value + "%", Hibernate.STRING);
				// append expression for localized metadata
				searchValue = "%" + LocalizedStringUtil.PARTITION + LocalizedStringUtil.escapeDelimiter(value) + "%";
				rigthExp = Expression.sql(columnName + " like ?", searchValue, Hibernate.STRING);
				criteria.add(Expression.or(leftExp, rigthExp));
			} else {
				// append expression for unlocalized metadata
				leftExp = Expression.sql("UPPER(" + columnName + ") like ?", (value + "%").toUpperCase(), Hibernate.STRING);
				// append expression for localized metadata
				searchValue = "%" + LocalizedStringUtil.PARTITION + LocalizedStringUtil.escapeDelimiter(value) + "%";
				rigthExp = Expression.sql("UPPER(" + columnName + ") like ?", searchValue.toUpperCase(), Hibernate.STRING);
				criteria.add(Expression.or(leftExp, rigthExp));
			}
		} else if (MatchMode.END.equals(mode)) {
			if (caseSensitive == true) {
				// append expression for unlocalized metadata
				leftExp = Expression.sql(columnName + " like ?", "%" + value, Hibernate.STRING);
				// append expression for localized metadata
				searchValue = "%" + LocalizedStringUtil.escapeDelimiter(value) + LocalizedStringUtil.SPLITTER + "%";
				rigthExp = Expression.sql(columnName + " like ?", searchValue, Hibernate.STRING);
				criteria.add(Expression.or(leftExp, rigthExp));
			} else {
				// append expression for unlocalized metadata
				leftExp = Expression.sql("UPPER(" + columnName + ") like ?", ("%" + value).toUpperCase(), Hibernate.STRING);
				// append expression for localized metadata
				searchValue = "%" + LocalizedStringUtil.escapeDelimiter(value) + LocalizedStringUtil.SPLITTER + "%";
				rigthExp = Expression.sql("UPPER(" + columnName + ") like ?", searchValue.toUpperCase(), Hibernate.STRING);
				criteria.add(Expression.or(leftExp, rigthExp));
			}
		} else if (MatchMode.ANYWHERE.equals(mode)) {
			if (caseSensitive == true) {
				// use one expression
				leftExp = Expression.sql(columnName + " like ?", "%" + value + "%", Hibernate.STRING);
				criteria.add(leftExp);
			} else {
				leftExp = Expression.sql("UPPER(" + columnName + ") like ?", ("%" + value + "%").toUpperCase(),
				    Hibernate.STRING);
				criteria.add(leftExp);
			}
		} else {//MatchMode.EXACT.equals(mode)
			if (caseSensitive == true) {
				// append expression for unlocalized metadata
				leftExp = Expression.sql(columnName + " = ?", value, Hibernate.STRING);
				// append expression for localized metadata
				searchValue = "%" + LocalizedStringUtil.PARTITION + LocalizedStringUtil.escapeDelimiter(value)
				        + LocalizedStringUtil.SPLITTER + "%";
				rigthExp = Expression.sql(columnName + " like ?", searchValue, Hibernate.STRING);
				criteria.add(Expression.or(leftExp, rigthExp));
			} else {
				// append expression for unlocalized metadata
				leftExp = Expression.sql("UPPER(" + columnName + ") = ?", value.toUpperCase(), Hibernate.STRING);
				// append expression for localized metadata
				searchValue = "%" + LocalizedStringUtil.PARTITION + LocalizedStringUtil.escapeDelimiter(value)
				        + LocalizedStringUtil.SPLITTER + "%";
				rigthExp = Expression.sql("UPPER(" + columnName + ") like ?", searchValue.toUpperCase(), Hibernate.STRING);
				criteria.add(Expression.or(leftExp, rigthExp));
			}
		}
	}
	
	/**
	 * Get a unique metadata from passed foundMetadata list, this is a less-frequent case in which
	 * there are more than one found metadata while searching metadata by name column, and these
	 * found metadata match name value in different locale.
	 * <p>
	 * Return strategy:
	 * <ol>
	 * <li>Retrun the metadata which's name match within user's current locale(if exist)</li>
	 * <li>Return the first found metadata(if no metadata found in previous step)</li>
	 * </ol>
	 * 
	 * @param <T>
	 * @param foundMetadata - a list to get a unique metadata from
	 * @param nameValue - value to compare with for each metadata's variant name which is within
	 *            user's current locale
	 * @return a unique metadata
	 */
	public static <T extends OpenmrsMetadata> T getUniqueMetadataByLocalizedName(List<T> foundMetadata, String nameValue) {
		if (foundMetadata == null || foundMetadata.isEmpty())
			return null;
		else if (foundMetadata.size() == 1)
			return foundMetadata.get(0);
		else {
			// this is a less-frequent use case, more than one metadata have variant name matching passed nameValue
			// and we should return the metadata which's name match within user's current locale firstly if exist
			// , otherwise return the first found one
			Locale userLocale = Context.getLocale();
			for (T metadata : foundMetadata) {
				if (nameValue.equals(metadata.getLocalizedName().getValue(userLocale)))
					return metadata;
			}
			
			// if no metadata matches user's current locale, then return the first metadata in passed foundMetadata list
			return foundMetadata.get(0);
		}
	}
}
