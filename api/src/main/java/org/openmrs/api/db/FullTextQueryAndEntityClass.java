/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db;

import org.hibernate.search.FullTextQuery;

/**
 * Wrapper class around a {@link FullTextQuery} object and the Type of the entities to be returned
 * by the query. An instance of this class is set as the source of a
 * {@link FullTextQueryCreatedEvent} object.
 * 
 * @since 2.3.0
 */
public class FullTextQueryAndEntityClass {
	
	private FullTextQuery query;
	
	private Class<?> entityClass;
	
	public FullTextQueryAndEntityClass(FullTextQuery query, Class<?> entityClass) {
		this.query = query;
		this.entityClass = entityClass;
	}
	
	/**
	 * Gets the query
	 *
	 * @return the query
	 */
	public FullTextQuery getQuery() {
		return query;
	}
	
	/**
	 * Gets the entityClass
	 *
	 * @return the entityClass
	 */
	public Class<?> getEntityClass() {
		return entityClass;
	}
	
}
