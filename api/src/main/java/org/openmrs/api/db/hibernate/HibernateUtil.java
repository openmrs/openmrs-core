/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db.hibernate;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.HSQLDialect;
import org.hibernate.dialect.PostgreSQL82Dialect;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.proxy.HibernateProxy;
import org.openmrs.Location;
import org.openmrs.attribute.AttributeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class holds common methods and utilities that are used across the hibernate related classes
 */
public class HibernateUtil {

	private HibernateUtil() {
	}
	
	private static final Logger log = LoggerFactory.getLogger(HibernateUtil.class);
	
	private static Dialect dialect = null;
	
	private static Boolean isHSQLDialect = null;
	
	private static Boolean isPostgreSQLDialect = null;
	
	/**
	 * Check and cache whether the currect dialect is HSQL or not. This is needed because some
	 * queries are different if in the hsql world as opposed to the mysql/postgres world
	 *
	 * @param sessionFactory
	 * @return true/false whether we're in hsql right now or not
	 */
	public static boolean isHSQLDialect(SessionFactory sessionFactory) {
		
		if (isHSQLDialect == null) {
			// check and cache the dialect
			isHSQLDialect = HSQLDialect.class.getName().equals(getDialect(sessionFactory).getClass().getName());
		}
		
		return isHSQLDialect;
	}
	
	/**
	 * Check and cache whether the currect dialect is PostgreSQL or not. This is needed because some
	 * behaviors of PostgreSQL and MySQL are different and need to be handled separately.
	 *
	 * @param sessionFactory
	 * @return true/false whether we're in postgresql right now or not
	 */
	public static boolean isPostgreSQLDialect(SessionFactory sessionFactory) {
		
		if (isPostgreSQLDialect == null) {
			// check and cache the dialect
			isPostgreSQLDialect = PostgreSQL82Dialect.class.getName()
			        .equals(getDialect(sessionFactory).getClass().getName());
		}
		
		return isPostgreSQLDialect;
	}
	
	/**
	 * Fetch the current Dialect of the given SessionFactory
	 *
	 * @param sessionFactory SessionFactory to pull the dialect from
	 * @return Dialect of sql that this connection/session is using
	 */
	public static Dialect getDialect(SessionFactory sessionFactory) {
		
		// return cached dialect
		if (dialect != null) {
			return dialect;
		}
		
		SessionFactoryImplementor implementor = (SessionFactoryImplementor) sessionFactory;
		dialect = implementor.getDialect();
		
		log.debug("Getting dialect for session: {}", dialect);
		
		return dialect;
	}
	
	/**
	 * @see HibernateUtil#escapeSqlWildcards(String, Connection)
	 */
	public static String escapeSqlWildcards(final String oldString, SessionFactory sessionFactory) {
		return sessionFactory.getCurrentSession().doReturningWork(connection -> escapeSqlWildcards(oldString, connection));
		
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
			        escapeCharacter + "%", escapeCharacter + "_", escapeCharacter + "*", "''" });
		} else {
			return oldString;
		}
	}
	
	/**
	 * Adds attribute value criteria to the given criteria query
	 * 
	 * @param criteria the criteria
	 * @param serializedAttributeValues the serialized attribute values
	 * @param <AT> the attribute type
	 */
	public static <AT extends AttributeType> void addAttributeCriteria(Criteria criteria,
	        Map<AT, String> serializedAttributeValues) {
		Conjunction conjunction = Restrictions.conjunction();
		int a = 0;
		
		for (Map.Entry<AT, String> entry : serializedAttributeValues.entrySet()) {
			String alias = "attributes" + (a++);
			DetachedCriteria detachedCriteria = DetachedCriteria.forClass(Location.class).setProjection(Projections.id());
			detachedCriteria.createAlias("attributes", alias);
			detachedCriteria.add(Restrictions.eq(alias + ".attributeType", entry.getKey()));
			detachedCriteria.add(Restrictions.eq(alias + ".valueReference", entry.getValue()));
			detachedCriteria.add(Restrictions.eq(alias + ".voided", false));
			
			conjunction.add(Property.forName("id").in(detachedCriteria));
		}
		
		criteria.add(conjunction);
	}
	
	/**
	 * Gets an object as an instance of its persistent type if it is a hibernate proxy otherwise
	 * returns the same passed in object
	 * 
	 * @param persistentObject the object to unproxy
	 * @return the unproxied object
	 * @since 1.10
	 */
	public static <T> T getRealObjectFromProxy(T persistentObject) {
		if (persistentObject == null) {
			return null;
		}
		
		if (persistentObject instanceof HibernateProxy) {
			Hibernate.initialize(persistentObject);
			persistentObject = (T) ((HibernateProxy) persistentObject).getHibernateLazyInitializer().getImplementation();
		}
		
		return persistentObject;
	}
}
