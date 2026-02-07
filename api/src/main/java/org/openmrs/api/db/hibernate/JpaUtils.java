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

import jakarta.persistence.NoResultException;
import jakarta.persistence.NonUniqueResultException;
import jakarta.persistence.Query;
import org.hibernate.Session;
import org.openmrs.BaseOpenmrsObject;

public class JpaUtils {

	/**
	 * Tries to get a single result from a JPA query, similar to Hibernate's uniqueResult.
	 * Returns null if no result is found, the single result if one result is found,
	 * and throws an exception if more than one result is found.
	 *
	 * @param query the JPA query to execute
	 * @param <T> the type of the query result
	 * @return the single result or null if no result is found
	 * @throws NonUniqueResultException if more than one result is found
	 */
	public static <T> T getSingleResultOrNull(Query query) {
		try {
			return (T) query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}
	
	/**
	 * Saves or updates the given entity using the provided Hibernate session.
	 * If the entity has a non-null ID, it is merged (updated); otherwise, it is persisted (saved).
	 *
	 * @param session the Hibernate session to use for saving or updating
	 * @param entity the entity to save or update
	 * @param <T> the type of the entity, which must extend BaseOpenmrsObject
	 * @return the saved or updated entity
	 */
	public static <T extends BaseOpenmrsObject> T saveOrUpdate(Session session, T entity) {
		if (entity == null) {
			throw new IllegalArgumentException("attempt to create saveOrUpdate event with null identifier");
		}

		if (entity.getId() != null) {
			return session.merge(entity);
		}
		session.persist(entity);
		return entity;
	}
}
