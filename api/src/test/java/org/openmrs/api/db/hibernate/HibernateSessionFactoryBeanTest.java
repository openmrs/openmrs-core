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

import java.sql.Connection;
import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.spi.BootstrapContext;
import org.hibernate.boot.spi.SessionFactoryOptions;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HibernateSessionFactoryBeanTest {

	private void integrate(boolean settingOn, boolean poolAutoCommit, ConnectionProvider provider) throws SQLException {
		SessionFactoryOptions options = mock(SessionFactoryOptions.class);
		when(options.doesConnectionProviderDisableAutoCommit()).thenReturn(settingOn);
		SessionFactoryImplementor sessionFactory = mock(SessionFactoryImplementor.class);
		when(sessionFactory.getSessionFactoryOptions()).thenReturn(options);

		Connection connection = mock(Connection.class);
		when(connection.getAutoCommit()).thenReturn(poolAutoCommit);
		when(provider.getConnection()).thenReturn(connection);

		StandardServiceRegistry registry = mock(StandardServiceRegistry.class);
		when(registry.getService(ConnectionProvider.class)).thenReturn(provider);
		BootstrapContext bootstrapContext = mock(BootstrapContext.class);
		when(bootstrapContext.getServiceRegistry()).thenReturn(registry);

		new HibernateSessionFactoryBean().integrate(mock(Metadata.class), bootstrapContext, sessionFactory);
	}

	@Test
	void shouldRefuseToStartWhenThePoolHandsOutAutoCommitEnabledConnections() throws SQLException {
		ConnectionProvider provider = mock(ConnectionProvider.class);

		assertThrows(HibernateException.class, () -> integrate(true, true, provider));

		verify(provider).closeConnection(any());
	}

	@Test
	void shouldStartWhenThePoolHandsOutAutoCommitDisabledConnections() throws SQLException {
		ConnectionProvider provider = mock(ConnectionProvider.class);

		assertDoesNotThrow(() -> integrate(true, false, provider));

		verify(provider).closeConnection(any());
	}

	@Test
	void shouldNotTouchThePoolWhenTheSettingIsOff() throws SQLException {
		ConnectionProvider provider = mock(ConnectionProvider.class);

		assertDoesNotThrow(() -> integrate(false, true, provider));

		verify(provider, never()).getConnection();
	}
}
