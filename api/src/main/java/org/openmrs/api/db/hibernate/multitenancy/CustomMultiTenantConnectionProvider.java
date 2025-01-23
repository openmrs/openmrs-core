/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db.hibernate.multitenancy;

import org.hibernate.HibernateException;
import org.hibernate.cfg.Environment;
import org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl;
import org.hibernate.engine.jdbc.connections.spi.AbstractMultiTenantConnectionProvider;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.hibernate.service.UnknownUnwrapTypeException;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

//@Component
public class CustomMultiTenantConnectionProvider implements MultiTenantConnectionProvider {

//	private final DataSource dataSource;
//
//	public CustomMultiTenantConnectionProvider(DataSource dataSource) {
//		this.dataSource = dataSource;
//	}
//
//	@Override
//	public Connection getAnyConnection() throws SQLException {
//		return dataSource.getConnection();
//	}
//
//
////	@Autowired
////	private SessionFactory sessionFactory;
////	
////
////    @Override
////    public Connection getAnyConnection() throws SQLException {
////		return SessionFactoryUtils.getDataSource(sessionFactory).getConnection();
////	}
//
//    @Override
//    public void releaseAnyConnection(Connection connection) throws SQLException {
//        connection.close();
//    }
//
//    @Override
//    public Connection getConnection(String tenantIdentifier) throws SQLException {
//        final Connection connection = getAnyConnection();
//        connection.createStatement().execute("SET SCHEMA '" + tenantIdentifier + "'");
//        return connection;
//    }
//
//    @Override
//    public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
//        connection.createStatement().execute("SET SCHEMA 'public'");
//        releaseAnyConnection(connection);
//    }
//
//    @Override
//    public boolean supportsAggressiveRelease() {
//        return false;
//    }
//
//    @Override
//    public boolean isUnwrappableAs(Class unwrapType) {
//        return false;
//    }
//
//    @Override
//    public <T> T unwrap(Class<T> unwrapType) {
//        return null;
//    }


	private static final long serialVersionUID = -6022082859572861041L;
	private static final Logger log = LoggerFactory.getLogger(CustomMultiTenantConnectionProvider.class);
	Map<String, ConnectionProvider> providers = new HashMap<>();
	
	
	public static Properties getConnectionProviderProperties(String dbName) {
		log.info("Inside getConnectionProviderProperties");
		Properties props = Context.getRuntimeProperties();
		String driver = props.getProperty("connection.driver_class");
		String username = props.getProperty("connection.username");
		String password = props.getProperty("connection.password");
		String url = props.getProperty("connection.url");
		
//		// hack for mysql to make sure innodb tables are created
//		if (url.contains("mysql") && !url.contains("InnoDB")) {
//			url = url + "&sessionVariables=default_storage_engine=InnoDB";
//		}
		
		url = url.replace("@DBNAME@", dbName);
		
		Properties connProps = new Properties(null);
		connProps.put(Environment.DRIVER, driver);
		connProps.put(Environment.URL, url);
		connProps.put(Environment.USER, username);
		connProps.put(Environment.PASS, password);
		return connProps;
	}
	
	private static DriverManagerConnectionProviderImpl buildConnectionProvider(String dbName,
																			   final boolean allowAggressiveRelease) {
		log.info("Inside buildConnectionProvider");
		DriverManagerConnectionProviderImpl connectionProvider = new DriverManagerConnectionProviderImpl() {
			public boolean supportsAggressiveRelease() {
				return allowAggressiveRelease;
			}
		};
		connectionProvider.configure(getConnectionProviderProperties(dbName));
		return connectionProvider;
	}
	
	public Connection getAnyConnection() throws SQLException {
		return getConnection(OpenmrsConstants.DATABASE_NAME);
	}
	
	@Override
	public void releaseAnyConnection(Connection connection) throws SQLException {
		log.info("Inside releaseAnyConnection");
		log.info("Schema:: {}", connection.getSchema());
		
		releaseConnection(OpenmrsConstants.DATABASE_NAME, connection);
	}
	
	@Override
	public Connection getConnection(String tenantId) {
		log.info("Inside getConnection");
		log.info("TenantId:: {}", tenantId);
		
		try {
			if (providers.containsKey(tenantId)) {

				final Connection connection =  providers.get(tenantId).getConnection();
				log.info("Before setting up::: Schema:: {}", connection.getSchema());

				connection.createStatement().execute("SET SCHEMA '" + tenantId + "'");

				log.info("After setting up::: Schema:: {}", connection.getSchema());
				return connection;
			}
			ConnectionProvider pool = buildConnectionProvider(tenantId, true);
			providers.put(tenantId, pool);
			
			final Connection connection =  pool.getConnection();
			log.info("Before setting up::: Schema:: {}", connection.getSchema());
			
			connection.createStatement().execute("SET SCHEMA '" + tenantId + "'");
			
			log.info("After setting up::: Schema:: {}", connection.getSchema());
			return connection;
			
		} catch (SQLException e) {
			log.error("Error occurred while connecting to tenant database", e);
			throw new HibernateException("Error occurred while connecting to tenant database [" + tenantId + "]", e);
		}
	}
	
	@Override
	public void releaseConnection(String tenantId, Connection connection) throws SQLException {
		log.info("Inside releaseConnection");
		log.info("tenantId::{}", tenantId);
//		connection.createStatement().execute("SET SCHEMA 'public'");
		providers.get(tenantId).closeConnection(connection);
//		connection.createStatement().execute("SET SCHEMA 'public'");
//		releaseAnyConnection(connection);
	}
	
	@Override
	public boolean supportsAggressiveRelease() {
		return Boolean.TRUE;
	}
	
	@Override
	public boolean isUnwrappableAs(Class unwrapType) {
		return MultiTenantConnectionProvider.class.equals(unwrapType)
			|| AbstractMultiTenantConnectionProvider.class.isAssignableFrom(unwrapType);
	}
	
	@Override
	public <T> T unwrap(Class<T> unwrapType) {
		if (isUnwrappableAs(unwrapType))
			return (T) this;
		else
			throw new UnknownUnwrapTypeException(unwrapType);
	}
}
