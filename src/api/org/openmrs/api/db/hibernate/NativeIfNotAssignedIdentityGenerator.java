/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.api.db.hibernate;

import java.io.Serializable;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.id.Configurable;
import org.hibernate.id.IdentifierGeneratorFactory;
import org.hibernate.id.IdentityGenerator;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.type.Type;

/**
 * <b>native-if-not-assigned</b><br>
 * <br>
 * By setting the Hibernate configuration's primary key column to use a "native" implementation,
 * Hibernate ALWAYS generates the entity's id when it is being saved. There is no way to "override"
 * the generated id. <br/>
 * <br/>
 * This IdentityGenerator allows a programmer to override the "generated" id, with an "assigned" id
 * at runtime by simply setting the primary key property.
 * 
 * @author paul.shemansky@gmail.com
 */
public class NativeIfNotAssignedIdentityGenerator extends IdentityGenerator implements Configurable {
	
	private String entityName;
	
	public Serializable generate(SessionImplementor session, Object entity) throws HibernateException {
		Serializable id;
		EntityPersister persister = session.getEntityPersister(entityName, entity);
		// Determine if an ID has been assigned.
		id = persister.getIdentifier(entity, session.getEntityMode());
		if (id == null) {
			// If the id was NOT assigned, return the POST_INSERT_INDICATOR,
			// which will determine and use the natively generated identifier.
			id = IdentifierGeneratorFactory.POST_INSERT_INDICATOR;
		}
		return id;
	}
	
	/**
	 * @see org.hibernate.id.Configurable#configure(org.hibernate.type.Type, java.util.Properties,
	 *      org.hibernate.dialect.Dialect)
	 */
	public void configure(Type type, Properties params, Dialect dialect) throws MappingException {
		this.entityName = params.getProperty(ENTITY_NAME);
		if (entityName == null) {
			throw new MappingException("no entity name");
		}
	}
	
}
