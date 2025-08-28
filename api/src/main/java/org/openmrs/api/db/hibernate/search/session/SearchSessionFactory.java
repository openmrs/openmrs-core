/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db.hibernate.search.session;

import org.hibernate.Session;
import org.hibernate.search.mapper.orm.session.SearchSession;

/**
 * Interface to be implemented by objects that are factories of {@link SearchSession} instances. A
 * factory has to be registered as a spring, it is highly recommended to use a factory to create
 * instances of the {@link SearchSession} rather than directly calling
 * {@link org.hibernate.search.mapper.orm.Search#session(Session)} for proper functionality.
 * 
 * @since 2.8.0
 */
public interface SearchSessionFactory {
	
	/**
	 * Obtains a {@link SearchSession} instance.
	 * 
	 * @return {@link SearchSession} object
	 */
	SearchSession getSearchSession();
}
