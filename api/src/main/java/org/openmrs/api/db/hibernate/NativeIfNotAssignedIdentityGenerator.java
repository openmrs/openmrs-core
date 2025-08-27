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

import java.util.EnumSet;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.EventType;
import org.hibernate.id.Configurable;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.id.IdentifierGeneratorHelper;
import org.hibernate.id.IdentityGenerator;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;
import org.openmrs.Concept;

/**
 * <b>native-if-not-assigned</b><br>
 * <br>
 * By setting the Hibernate configuration's primary key column to use a "native" implementation,
 * Hibernate ALWAYS generates the entity's id when it is being saved. There is no way to "override"
 * the generated id. <br>
 * <br>
 * This IdentityGenerator allows a programmer to override the "generated" id, with an "assigned" id
 * at runtime by simply setting the primary key property.
 * 
 * @author paul.shemansky@gmail.com
 */
public class NativeIfNotAssignedIdentityGenerator extends IdentityGenerator implements IdentifierGenerator, Configurable {
	
	private String entityName;
	
	@Override
	public Object generate(SharedSessionContractImplementor session, Object entity) throws HibernateException {
		EntityPersister persister = session.getEntityPersister(entityName, entity);
		// Determine if an ID has been assigned.
		return persister.getIdentifier(entity, session);
	}

	@Override
	public void configure(Type type, Properties params, ServiceRegistry serviceRegistry) throws MappingException {
		this.entityName = params.getProperty(ENTITY_NAME);
		if (entityName == null) {
			throw new MappingException("no entity name");
		}
	}

	@Override
	public EnumSet<EventType> getEventTypes() {
		return super.getEventTypes();
	}

	@Override
	public boolean generatedOnExecution() {
		return super.generatedOnExecution();
	}

	@Override
	public boolean generatedOnExecution(Object entity, SharedSessionContractImplementor session) {
		EntityPersister persister = session.getEntityPersister(entityName, entity);
		return persister.getIdentifier(entity, session) == null;
	}
}
