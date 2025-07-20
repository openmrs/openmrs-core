/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.hibernate;

import org.hibernate.boot.model.TypeContributor;
import org.hibernate.boot.model.TypeContributions;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.BasicType;

/**
 * Registers the custom `large_text` type globally with the name `large_text`, 
 * allowing it to be used in entity classes.
 * @since 2.8.0
 */
public class ClobOrTextTypeContributor implements TypeContributor {

    @Override
    public void contribute(TypeContributions typeContributions, ServiceRegistry serviceRegistry) {
        Dialect dialect = getDialect(serviceRegistry);
        BasicType type = new ClobOrTextType(dialect);
        typeContributions.contributeType(type);
    }

    private Dialect getDialect(ServiceRegistry serviceRegistry) {
        JdbcServices jdbcServices = serviceRegistry.getService(JdbcServices.class);
        if (jdbcServices == null) {
            throw new RuntimeException("Unexpected: Cannot find a 'JdbcServices' in Hibernate's ServiceRegistry.");
        }
        Dialect dialect = jdbcServices.getDialect();
        if (dialect == null) {
            throw new RuntimeException("Unexpected: Hibernate's 'JdbcServices' has no 'JdbcEnvironment'. Dialect cannot be determined.");
        }
        return dialect;
    }
} 
