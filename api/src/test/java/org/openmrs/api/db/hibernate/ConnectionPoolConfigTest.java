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

import java.util.stream.Stream;
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
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Verifies that the connection pool limits configured in hibernate.default.properties actually
 * reach the live c3p0 pool. Guards the checkout timeout added for TRUNK-6465: with c3p0's default
 * of 0 an exhausted pool blocks every waiting thread forever, so if a change to the property
 * loading in {@link HibernateSessionFactoryBean} silently dropped the value, pool exhaustion would
 * once again freeze the server instead of failing recoverably.
 * <p>
 * The max pool size is deliberately pinned as well: the bounded concurrency in
 * {@code OrderServiceTest.getNewOrderNumber_shouldAlwaysReturnUniqueOrderNumbersWhenCalledMultipleTimesWithoutSavingOrders}
 * is derived from it (two connections per call must stay below the pool cap), so anyone changing
 * hibernate.c3p0.max_size needs to revisit that test's threadCount before simply updating the
 * expected value here.
 */
public class ConnectionPoolConfigTest extends BaseContextSensitiveTest {

	@Autowired
	private SessionFactory sessionFactory;

	@Test
	public void shouldApplyConfiguredConnectionPoolLimits() {
		// a machine-local openmrs-runtime.properties may legitimately tune the pool and would
		// win over hibernate.default.properties; only pin the shipped defaults when it does not
		assumeTrue(
		    Stream.of("hibernate.c3p0.checkoutTimeout", "c3p0.checkoutTimeout", "hibernate.c3p0.max_size", "c3p0.max_size")
		            .noneMatch(runtimeProperties::containsKey),
		    "skipped because local runtime properties override the c3p0 pool defaults");

		ConnectionProvider connectionProvider = ((SessionFactoryImplementor) sessionFactory).getServiceRegistry()
		        .getService(ConnectionProvider.class);
		DataSource dataSource = connectionProvider.unwrap(DataSource.class);
		WrapperConnectionPoolDataSource pool = (WrapperConnectionPoolDataSource) ((PoolBackedDataSource) dataSource)
		        .getConnectionPoolDataSource();

		assertEquals(30000, pool.getCheckoutTimeout());
		assertEquals(50, pool.getMaxPoolSize());
	}
}
