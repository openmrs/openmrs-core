/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * An instance of this class provides a factory for obtaining {@link FullTextSession} instances.
 * Having this factory as a spring bean provides a mechanism that allows external code and modules
 * to advise the factory. It is highly recommended to use this factory to create instances of the
 * {@link FullTextSession} rather than directly calling {@link Search#getFullTextSession(Session)}
 * for proper functionality.
 * 
 * @since 2.2.1
 */
@Component("fullTextSessionFactory")
public class FullTextSessionFactory {
	
	@Autowired
	private SessionFactory sessionFactory;
	
	/**
	 * Obtains a {@link FullTextSession} instance.
	 * 
	 * @return {@link FullTextSession} object
	 */
	public FullTextSession getFullTextSession() {
		return Search.getFullTextSession(sessionFactory.getCurrentSession());
	}
	
}
