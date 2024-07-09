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

import org.hibernate.EmptyInterceptor;
import org.hibernate.envers.Audited;
import org.hibernate.type.Type;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.Daemon;
import org.openmrs.api.context.UserContext;
import org.openmrs.api.db.ReadAuditDAO;
import org.openmrs.api.db.hibernate.envers.OpenmrsReadAuditEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Date;

@Component
public class AuditInterceptor extends EmptyInterceptor {
	
	private Logger logger = LoggerFactory.getLogger(AuditInterceptor.class);
	
	private final ReadAuditDAO readAuditDAO;

	public AuditInterceptor(@Lazy ReadAuditDAO readAuditDAO) {
		this.readAuditDAO = readAuditDAO;
	}

	@Override
	public boolean onLoad(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
		if(!entity.getClass().isAnnotationPresent(Audited.class)) {
			return super.onLoad(entity, id, state, propertyNames, types);
		}
		if(Context.getUserContext().isAuthenticated()) {
			User authenticatedUser = Context.getUserContext().getAuthenticatedUser();
			if(authenticatedUser.getId() != null) {
				readAuditDAO.save(new OpenmrsReadAuditEntity(authenticatedUser.getUserId(), new Date(), entity.getClass().getName()));
			} 
		}
		else if(Daemon.getDaemonThreadUser() != null) {
			readAuditDAO.save(new OpenmrsReadAuditEntity(Daemon.getDaemonThreadUser().getUserId(), new Date(), entity.getClass().getName()));
		}
		return super.onLoad(entity, id, state, propertyNames, types);
	}
}
