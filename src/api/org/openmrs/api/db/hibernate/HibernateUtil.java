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
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.HSQLDialect;
import org.hibernate.engine.SessionFactoryImplementor;
import org.openmrs.BaseOpenmrsMetadata;
import org.openmrs.api.context.Context;
import org.openmrs.serialization.LocalizedStringSerializer;
import org.openmrs.util.LocalizedStringUtil;
import org.openmrs.util.OpenmrsUtil;

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
	 * Find Metadata object exactly(return only one record in one time) by database field which is
	 * specified by given columnName.
	 * 
	 * @param <T>
	 * @param value - value to match
	 * @param columnName - column to match in
	 * @param searchClazz - the class related to the searched database table
	 * @param sessionFactory - SessionFactory to create Criteria from
	 * @return A object extends {@link BaseOpenmrsMetadata} if exist, otherwise null
	 */
	@SuppressWarnings("unchecked")
	public static <T extends BaseOpenmrsMetadata> T findMetadataExactlyInLocalizedColumn(String value, String columnName,
	                                                                                     Class<? extends T> searchClazz,
	                                                                                     SessionFactory sessionFactory) {
		if (value == null)
			return null;
		
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(searchClazz);
		// firstly, exact comparison with column specified by passed columnName
		// this search is for those metadatas havent been localized at all
		crit.add(Expression.eq("retired", false));
		crit.add(Expression.sql(columnName + " = ?", LocalizedStringUtil.escapeDelimiter(value), Hibernate.STRING));
		T t = (T) crit.uniqueResult();
		if (t != null)
			return t;
		
		// if no records found in exact search, then fuzzy search with column specified by passed columnName
		// this search is for those metadatas have been localized
		crit = sessionFactory.getCurrentSession().createCriteria(searchClazz);
		crit.add(Expression.eq("retired", false));
		crit.add(Expression.sql(columnName + " like ?", "%" + LocalizedStringSerializer.PARTITION
		        + LocalizedStringUtil.escapeDelimiter(value) + LocalizedStringSerializer.SPLITTER + "%", Hibernate.STRING));
		List<T> list = crit.list();
		if (list == null || list.isEmpty())
			return null;
		if (list.size() == 1)
			return list.get(0);
		else {
			// this is a less-frequent use case, more than one records are found
			// and we should return the record which's name match within user's current locale firstly if exist
			// , otherwise return the first found one
			Locale userLocale = Context.getLocale();
			for (T tt : list) {
				Map<Locale, String> variants = tt.getLocalizedName().getVariants();
				if (variants != null && !variants.isEmpty())
					if (value.equals(variants.get(userLocale)))
						return tt;
			}
			return list.get(0);
		}
	}
	
	/**
	 * Find Metadata object inexactly(return a list in one time) by database field which is
	 * specified by given columnName.
	 * 
	 * @param <T>
	 * @param value - value to match
	 * @param columnName - column to match in
	 * @param propertyName - the property name identical to columnName which is used by Hibernate
	 * @param searchClazz - the class related to the searched database table
	 * @param orderDef - store all fields which need to add order for and their related order(asc or
	 *            desc), like such a form {localizedName=asc, retired=desc}
	 * @param sessionFactory - SessionFactory to create Criteria from
	 * @return A list of objects extend {@link BaseOpenmrsMetadata} if exist, otherwise null
	 */
	@SuppressWarnings("unchecked")
	public static <T extends BaseOpenmrsMetadata> List<T> findMetadataInexactlyInLocalizedColumn(
	                                                                                             String value,
	                                                                                             String columnName,
	                                                                                             String propertyName,
	                                                                                             Class<? extends T> searchClazz,
	                                                                                             LinkedHashMap<String, String> orderDef,
	                                                                                             SessionFactory sessionFactory) {
		List<T> results = null;
		
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(searchClazz);
		
		// do case insensitive search
		// firstly, search those metadatas havent been localized at all
		crit.add(Expression.sql("UPPER(" + columnName + ") like ?", LocalizedStringUtil.escapeDelimiter(value).toUpperCase()
		        + "%", Hibernate.STRING));
		// add order by
		for (Map.Entry<String, String> entry : orderDef.entrySet()) {
			if ("asc".equals(entry.getValue()))
				crit.addOrder(Order.asc(entry.getKey()));
			else
				crit.addOrder(Order.desc(entry.getKey()));
		}
		
		results = crit.list();
		
		// seconly, search those metadatas have been localized
		crit = sessionFactory.getCurrentSession().createCriteria(searchClazz);
		crit.add(Expression.sql("UPPER(" + columnName + ") like ?", "%" + LocalizedStringSerializer.PARTITION
		        + LocalizedStringUtil.escapeDelimiter(value).toUpperCase() + "%", Hibernate.STRING));
		// add order by
		for (Map.Entry<String, String> entry : orderDef.entrySet()) {
			if ("asc".equals(entry.getValue()))
				crit.addOrder(Order.asc(entry.getKey()));
			else
				crit.addOrder(Order.desc(entry.getKey()));
		}
		
		List<T> secondResults = crit.list();
		results.addAll(secondResults);
		
		if (orderDef.containsKey(propertyName) && !secondResults.isEmpty()) {
			// order by localized column which is specified by columnName once more
			// because now the results list includes some metadatas have been localized
			if ("asc".equals(orderDef.get(propertyName))) {
				Collections.sort(results, new Comparator<BaseOpenmrsMetadata>() {
					
					@Override
					public int compare(BaseOpenmrsMetadata left, BaseOpenmrsMetadata right) {
						return OpenmrsUtil.compareWithNullAsLowest(left.getName(), right.getName());
					}
				});
			} else {
				Collections.sort(results, new Comparator<BaseOpenmrsMetadata>() {
					
					@Override
					public int compare(BaseOpenmrsMetadata left, BaseOpenmrsMetadata right) {
						return (OpenmrsUtil.compareWithNullAsLowest(left.getName(), right.getName()) * -1);
					}
				});
			}
		}
		
		return results;
	}
}
