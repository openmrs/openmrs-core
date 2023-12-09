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

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

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
}
