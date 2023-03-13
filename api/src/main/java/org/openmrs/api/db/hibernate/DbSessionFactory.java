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

import org.hibernate.SessionFactory;

/**
 * It should be used instead of SessionFactory for modules, which need to support
 * OpenMRS 1.12 and before. Please do not use it, if you only need to support
 * OpenMRS 1.12 and later.
 * 
 * See {@link DbSession} for more details.
 * 
 * @since 1.12, 1.11.3, 1.10.2, 1.9.9
 */
public class DbSessionFactory {
	
	private SessionFactory sessionFactory;
	
	public DbSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	public DbSession getCurrentSession() {
		return new DbSession(sessionFactory);
	}
	
	public SessionFactory getHibernateSessionFactory() {
		return sessionFactory;
	}
}
