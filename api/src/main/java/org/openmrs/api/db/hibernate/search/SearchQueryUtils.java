/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db.hibernate.search;

/**
 * Provides utils for working with queries in Hibernate Search
 * 
 * @since 2.8.0
 */
public class SearchQueryUtils {
	
	private SearchQueryUtils() {
	}
	
	/**
	 * Escapes special characters such as: <code>()*-+|~"</code>
	 * 
	 * @param query the query
	 * @return escaped query
	 */
	public static String escapeQuery(String query) {
		return query.replace("(", "\\(").replace(")", "\\)".replace("+", "\\+")).replace("-", "\\-").replace("|", "\\|")
		        .replace("*", "\\*").replace("~", "\\~").replace("\"", "\\\"");
	}
}
