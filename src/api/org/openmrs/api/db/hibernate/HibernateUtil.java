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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Expression;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.HSQLDialect;
import org.hibernate.engine.SessionFactoryImplementor;
import org.openmrs.BaseOpenmrsMetadata;
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
	 * Find metadatas (return a list in one time) by exactly searching the localized database field
	 * which is specified by given columnName.
	 * <p>
	 * Note:
	 * <ol>
	 * <li>The search range is just those metadatas which's name property has been localized
	 * <li>This method returns a list because of some metadatas allow duplicated values in one
	 * field(e.g, {@link org.openmrs.Form} allows duplicated names)</li>
	 * <li>The search logic in this method doesn't have order by option</li>
	 * <ol>
	 * 
	 * @param <T>
	 * @param value - value to match
	 * @param columnName - column to match in
	 * @param includeRetired - if includeRetired is true, also get retired metadatas.
	 * @param searchClazz - the class related to the searched database table
	 * @param sessionFactory - SessionFactory to create Criteria from
	 * @return A object extends {@link BaseOpenmrsMetadata} if exist, otherwise null
	 * @since 1.9
	 */
	@SuppressWarnings("unchecked")
	public static <T extends BaseOpenmrsMetadata> List<T> findMetadatasExactlyByLocalizedColumn(
	                                                                                            String value,
	                                                                                            String columnName,
	                                                                                            boolean includeRetired,
	                                                                                            Class<? extends T> searchClazz,
	                                                                                            SessionFactory sessionFactory) {
		List<T> results = new ArrayList<T>();
		
		if (value != null) {
			// Search in those metadatas have been localized
			Criteria crit = sessionFactory.getCurrentSession().createCriteria(searchClazz);
			if (includeRetired == false)
				crit.add(Expression.eq("retired", false));
			crit.add(Expression.sql(columnName + " like ?", "%" + LocalizedStringUtil.PARTITION
			        + LocalizedStringUtil.escapeDelimiter(value) + LocalizedStringUtil.SPLITTER + "%",
			    Hibernate.STRING));
			results = crit.list();
		}
		
		return results;
	}
	
	/**
	 * Get the unique metadata object by searching the localized database field which is specified
	 * by given columnName.
	 * <p>
	 * Note: The search range is just those metadatas which's name property has been localized
	 * 
	 * @param <T>
	 * @param value - value to match
	 * @param columnName - column to match in
	 * @param includeRetired - if includeRetired is true, also get retired metadatas.
	 * @param searchClazz - the class related to the searched database table
	 * @param sessionFactory - SessionFactory to create Criteria from
	 * @return A object extends {@link BaseOpenmrsMetadata} if exist, otherwise null
	 * @since 1.9
	 * @see HibernateUtil#findMetadatasExactlyByLocalizedColumn(String, String, boolean, Class,
	 *      SessionFactory)
	 */
	public static <T extends BaseOpenmrsMetadata> T getUniqueMetadataByLocalizedColumn(String value, String columnName,
	                                                                                   boolean includeRetired,
	                                                                                   Class<? extends T> searchClazz,
	                                                                                   SessionFactory sessionFactory) {
		List<T> results = findMetadatasExactlyByLocalizedColumn(value, columnName, includeRetired, searchClazz,
		    sessionFactory);
		
		if (results == null || results.isEmpty())
			return null;
		if (results.size() == 1)
			return results.get(0);
		else {
			// this is a less-frequent use case, more than one records are found
			// and we should return the record which's name match within user's current locale firstly if exist
			// , otherwise return the first found one
			for (T tt : results) {
				if (value.equals(tt.getName()))
					return tt;
			}
			
			// if no record matches user's current locale, then return the first found one
			return results.get(0);
		}
	}
	
	/**
	 * Find metadatas (return a list in one time) by fuzzily searching the localized database field
	 * which is specified by given columnName.
	 * <p>
	 * Note:
	 * <ul>
	 * <li>The search logic in this method doesn't have order by option</li>
	 * <li>The search range is just those metadatas which's name property has been localized</li>
	 * </ul>
	 * 
	 * @param <T>
	 * @param value - value to match
	 * @param columnName - column to match in
	 * @param includeRetired - if includeRetired is true, also get retired metadatas.
	 * @param caseSensitive - if caseSensitive is false, do sql query similar to hibernate's "ilike"
	 * @param searchClazz - the class related to the searched database table
	 * @param sessionFactory - SessionFactory to create Criteria from
	 * @return A list of objects extend {@link BaseOpenmrsMetadata} if exist, otherwise null
	 * @since 1.9
	 */
	@SuppressWarnings("unchecked")
	public static <T extends BaseOpenmrsMetadata> List<T> findMetadatasFuzzilyByLocalizedColumn(
	                                                                                            String value,
	                                                                                            String columnName,
	                                                                                            boolean includeRetired,
	                                                                                            boolean caseSensitive,
	                                                                                            Class<? extends T> searchClazz,
	                                                                                            SessionFactory sessionFactory) {
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(searchClazz);
		
		// search those metadatas have been localized
		if (includeRetired == false)
			crit.add(Expression.eq("retired", false));
		
		String queryStr = "";
		String queryValue = "";
		if (caseSensitive == false) {
			queryStr = "UPPER(" + columnName + ") like ?";
			queryValue = "%" + LocalizedStringUtil.PARTITION
			        + LocalizedStringUtil.escapeDelimiter(value).toUpperCase() + "%";
		} else {
			queryStr = columnName + " like ?";
			queryValue = "%" + LocalizedStringUtil.PARTITION + LocalizedStringUtil.escapeDelimiter(value) + "%";
		}
		crit.add(Expression.sql(queryStr, queryValue, Hibernate.STRING));
		
		return crit.list();
	}
	
}
