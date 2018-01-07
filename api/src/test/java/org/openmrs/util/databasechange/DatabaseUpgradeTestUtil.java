/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util.databasechange;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.ext.h2.H2DataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

/**
 * Allows to test database upgrade. It accepts initialDatabasePath which should point to the h2
 * liqubaseConnection that will be used for upgrade.
 */
public class DatabaseUpgradeTestUtil {
	
	private final Connection connection;
	
	private final Database liqubaseConnection;
	
	private final DatabaseConnection dbUnitConnection;
	
	private final File tempDir;
	
	private final File tempDBFile;
	
	private final String connectionUrl;
	
	public DatabaseUpgradeTestUtil(String initialDatabasePath) throws IOException, SQLException {
		InputStream databaseInputStream = getClass().getResourceAsStream(initialDatabasePath);
		
		tempDir = File.createTempFile("openmrs-tests-temp-", "");
		tempDir.delete();
		tempDir.mkdir();
		tempDir.deleteOnExit();
		
		tempDBFile = new File(tempDir, "openmrs.h2.db");
		tempDBFile.delete();
		try {
			tempDBFile.createNewFile();
		}
		catch (IOException e) {
			tempDir.delete();
			throw e;
		}
		tempDBFile.deleteOnExit();
		
		FileOutputStream tempDBOutputStream = new FileOutputStream(tempDBFile);
		
		try {
			IOUtils.copy(databaseInputStream, tempDBOutputStream);
			
			databaseInputStream.close();
			tempDBOutputStream.close();
		}
		catch (IOException e) {
			tempDBFile.delete();
			tempDir.delete();
			
			throw e;
		}
		finally {
			IOUtils.closeQuietly(databaseInputStream);
			IOUtils.closeQuietly(tempDBOutputStream);
		}
		
		String databaseUrl = tempDir.getAbsolutePath().replace("\\", "/") + "/openmrs";
		
		connectionUrl = "jdbc:h2:" + databaseUrl + ";AUTO_RECONNECT=TRUE;DB_CLOSE_DELAY=-1";
		
		connection = DriverManager.getConnection(connectionUrl, "sa", "sa");
		connection.setAutoCommit(true);
		
		try {
			liqubaseConnection = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(
			    new JdbcConnection(connection));
			liqubaseConnection.setDatabaseChangeLogTableName("LIQUIBASECHANGELOG");
			liqubaseConnection.setDatabaseChangeLogLockTableName("LIQUIBASECHANGELOGLOCK");
		}
		catch (LiquibaseException e) {
			tempDir.delete();
			tempDBFile.delete();
			
			throw new SQLException(e);
		}
		
		try {
			dbUnitConnection = new DatabaseConnection(connection);
			dbUnitConnection.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new H2DataTypeFactory());
		}
		catch (DatabaseUnitException e) {
			tempDir.delete();
			tempDBFile.delete();
			
			throw new SQLException(e);
		}
	}
	
	public void close() throws SQLException {
		try {
			connection.close();
		}
		finally {
			tempDBFile.delete();
			tempDir.delete();
		}
	}
	
	public Connection getConnection() {
		return connection;
	}
	
	public void executeDataset(String path) throws IOException, SQLException {
		InputStream inputStream = getClass().getResourceAsStream(path);
		ReplacementDataSet replacementDataSet;
		try {
			replacementDataSet = new ReplacementDataSet(new FlatXmlDataSet(new InputStreamReader(inputStream), false, true,
			        false));
			
			inputStream.close();
		}
		catch (DataSetException e) {
			throw new IOException(e);
		}
		finally {
			IOUtils.closeQuietly(inputStream);
		}
		replacementDataSet.addReplacementObject("[NULL]", null);
		
		try {
			DatabaseOperation.REFRESH.execute(dbUnitConnection, replacementDataSet);
			
			connection.commit();
		}
		catch (DatabaseUnitException e) {
			throw new IOException(e);
		}
	}
	
	public List<Map<String, String>> select(String tableName, String where, String columnName, String... columnNames)
	        throws SQLException {
		String[] allColumnNames = ArrayUtils.addAll(new String[] { columnName }, columnNames);
		
		String sql = "select " + StringUtils.join(allColumnNames, ", ") + " from " + tableName;
		if (!StringUtils.isBlank(where)) {
			sql += " where " + where;
		}
		PreparedStatement query = connection.prepareStatement(sql);
		ResultSet resultSet = query.executeQuery();
		
		List<Map<String, String>> results = new ArrayList<>();
		while (resultSet.next()) {
			Map<String, String> columns = new HashMap<>();
			results.add(columns);
			
			for (int i = 0; i < allColumnNames.length; i++) {
				Object object = resultSet.getObject(i + 1);
				columns.put(allColumnNames[i], object != null ? object.toString() : null);
			}
		}
		
		query.close();
		
		return results;
	}
	
	public void insertGlobalProperty(String globalProperty, String value) throws SQLException {
		PreparedStatement insert = connection
		        .prepareStatement("insert into global_property (property, property_value, uuid) values (?, ?, ?)");
		insert.setString(1, globalProperty);
		insert.setString(2, value);
		insert.setString(3, UUID.randomUUID().toString());
		
		insert.executeUpdate();
		
		insert.close();
		
		connection.commit();
	}
	
	public void upgrade() throws IOException, SQLException {
		upgrade("liquibase-update-to-latest.xml");
	}

	public void upgrade(String filename) throws IOException, SQLException {
		try {
			Liquibase liquibase = new Liquibase(filename, new ClassLoaderResourceAccessor(getClass()
			        .getClassLoader()), liqubaseConnection);
			liquibase.update(null);
			
			connection.commit();
		}
		catch (LiquibaseException e) {
			throw new IOException(e);
		}
	}
	
	public SessionFactory buildSessionFactory() {
		Configuration config = new Configuration().configure();
		//H2 version we use behaves differently from H2Dialect in Hibernate so we provide our implementation
		config.setProperty(Environment.DIALECT, H2LessStrictDialect.class.getName());
		config.setProperty(Environment.URL, connectionUrl);
		config.setProperty(Environment.DRIVER, "org.h2.Driver");
		config.setProperty(Environment.USER, "sa");
		config.setProperty(Environment.PASS, "sa");
		config.setProperty(Environment.USE_SECOND_LEVEL_CACHE, "false");
		config.setProperty(Environment.USE_QUERY_CACHE, "false");
		
		//Let's validate HBMs against the actual schema
		config.setProperty(Environment.HBM2DDL_AUTO, "validate");
		
		return config.buildSessionFactory();
	}
}
