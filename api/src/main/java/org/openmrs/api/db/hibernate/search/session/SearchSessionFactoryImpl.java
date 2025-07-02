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

import org.hibernate.SessionFactory;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * @since 2.8.0
 */
@Component("searchSessionFactory")
public class SearchSessionFactoryImpl implements SearchSessionFactory {
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Autowired
	private ApplicationEventPublisher eventPublisher;
	
	/**
	 * @see SearchSessionFactory#getSearchSession()
	 */
	@Override
	public SearchSession getSearchSession() {
		SearchSession delegateSession = Search.session(sessionFactory.getCurrentSession());
		return new EventPublisherDelegatingSearchSession(delegateSession, eventPublisher);
	}
}
