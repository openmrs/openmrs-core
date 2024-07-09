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
import org.openmrs.api.db.ReadAuditDAO;
import org.openmrs.api.db.hibernate.envers.OpenmrsReadAuditEntity;
import org.springframework.transaction.annotation.Transactional;


public class HibernateReadAuditDAO implements ReadAuditDAO {
	
	private SessionFactory sessionFactory;

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	@Override
	public void save(OpenmrsReadAuditEntity receivedAudit) {
		sessionFactory.getCurrentSession().save(receivedAudit);
	}
}
