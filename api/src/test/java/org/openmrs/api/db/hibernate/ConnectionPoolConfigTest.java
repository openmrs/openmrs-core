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

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.junit.jupiter.api.Test;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import com.mchange.v2.c3p0.PoolBackedDataSource;
import com.mchange.v2.c3p0.WrapperConnectionPoolDataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Verifies that the connection pool limits configured in hibernate.default.properties actually
 * reach the live c3p0 pool. Guards the checkout timeout added for TRUNK-6465: with c3p0's default
 * of 0 an exhausted pool blocks every waiting thread forever, so if a change to the property
 * loading in {@link HibernateSessionFactoryBean} silently dropped the value, pool exhaustion would
 * once again freeze the server instead of failing recoverably.
 */
public class ConnectionPoolConfigTest extends BaseContextSensitiveTest {

	@Autowired
	private SessionFactory sessionFactory;

	@Test
	public void shouldBoundConnectionCheckoutWaitsAsConfigured() {
		ConnectionProvider connectionProvider = ((SessionFactoryImplementor) sessionFactory).getServiceRegistry()
		        .getService(ConnectionProvider.class);
		DataSource dataSource = connectionProvider.unwrap(DataSource.class);
		WrapperConnectionPoolDataSource pool = (WrapperConnectionPoolDataSource) ((PoolBackedDataSource) dataSource)
		        .getConnectionPoolDataSource();

		assertEquals(30000, pool.getCheckoutTimeout());
		assertEquals(50, pool.getMaxPoolSize());
	}
}
