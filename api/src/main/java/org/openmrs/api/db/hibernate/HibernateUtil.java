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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.HSQLDialect;
import org.hibernate.dialect.PostgreSQL82Dialect;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.proxy.HibernateProxy;
import org.openmrs.Location;
import org.openmrs.LocationAttribute;
import org.openmrs.api.db.DAOException;
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
	 * Constructs a list of predicates for attribute value criteria for use in a JPA Criteria query.
	 *
	 * @param cb The CriteriaBuilder used to construct the CriteriaQuery
	 * @param locationRoot The root of the CriteriaQuery for the Location entity
	 * @param serializedAttributeValues A map of AttributeType to serialized attribute values
	 * @param <AT> The type of the attribute
	 * @return A list of Predicate objects for use in a CriteriaQuery
	 */
	public static <AT extends AttributeType> List<Predicate> getAttributePredicate(CriteriaBuilder cb,
	        Root<Location> locationRoot, Map<AT, String> serializedAttributeValues) {
		List<Predicate> predicates = new ArrayList<>();
		
		for (Map.Entry<AT, String> entry : serializedAttributeValues.entrySet()) {
			Subquery<Integer> subquery = cb.createQuery().subquery(Integer.class);
			Root<Location> locationSubRoot = subquery.from(Location.class);
			Join<Location, LocationAttribute> attributeJoin = locationSubRoot.join("attributes");
			
			Predicate[] attributePredicates = new Predicate[] { cb.equal(attributeJoin.get("attributeType"), entry.getKey()),
			        cb.equal(attributeJoin.get("valueReference"), entry.getValue()),
			        cb.isFalse(attributeJoin.get("voided")) };
			
			subquery.select(locationSubRoot.get("locationId")).where(attributePredicates);
			predicates.add(cb.in(locationRoot.get("locationId")).value(subquery));
		}
		
		return predicates;
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

	/**
	 * Retrieves a unique entity by its UUID.
	 *
	 * @param sessionFactory the session factory to create sessions.
	 * @param entityClass the class of the entity to retrieve.
	 * @param uuid the UUID of the entity.
	 * @return the entity if found, null otherwise.
	 * @throws DAOException if there's an issue in data access.
	 */
	public static <T> T getUniqueEntityByUUID(SessionFactory sessionFactory, Class<T> entityClass, String uuid) throws DAOException {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<T> query = cb.createQuery(entityClass);
		Root<T> root = query.from(entityClass);

		query.where(cb.equal(root.get("uuid"), uuid));
		return session.createQuery(query).uniqueResult();
	}

	/**
	 * Creates a ScrollableResults instance for the given entity type with the specified fetch size.
	 *
	 * @param sessionFactory the session factory to create sessions.
	 * @param type the class type of the entity for which the ScrollableResults is created.
	 * @param fetchSize the number of rows to fetch in a batch.
	 * @return ScrollableResults instance for batch processing.
	 */
	public static <T> ScrollableResults getScrollableResult(SessionFactory sessionFactory, Class<T> type, int fetchSize) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
		CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(type);
		Root<T> root = criteriaQuery.from(type);
		criteriaQuery.select(root);

		return session.createQuery(criteriaQuery)
			.setFetchSize(fetchSize)
			.scroll(ScrollMode.FORWARD_ONLY);
	}
}
