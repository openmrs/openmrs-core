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
package org.openmrs.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import liquibase.ChangeSet;
import liquibase.ClassLoaderFileOpener;
import liquibase.CompositeFileOpener;
import liquibase.FileOpener;
import liquibase.FileSystemFileOpener;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;

/**
 * This class uses Liquibase to update the database. <br/>
 * <br/>
 * See /metadata/model/liquibase-update-to-latest.xml for the changes. This class will also run
 * arbitrary liquibase xml files on the associated database as well. Details for the database are
 * taken from the openmrs runtime properties.
 */
public class DatabaseUpdater {
	
	private static Log log = LogFactory.getLog(DatabaseUpdater.class);
	
	private final static String CHANGE_LOG_FILE = "liquibase-update-to-latest.xml";
	
	private final static String CONTEXT = "core";
	
	/**
	 * Convenience method to run the changesets using Liquibase to bring the database up to a
	 * version compatible with the code
	 * 
	 * @throws InputRequiredException if the changelog file requirest some sort of user input. The
	 *             error object will list of the user prompts and type of data for each prompt
	 * @see #update(Map)
	 * @see #executeChangelog(String, Map)
	 * @should always have a valid update to latest file
	 */
	public static void update() throws DatabaseUpdateException, InputRequiredException {
		update(null);
	}
	
	/**
	 * Run changesets on database using Liquibase to get the database up to the most recent version
	 * 
	 * @param userInput nullable map from question to user answer. Used if a call to update(null)
	 *            threw an {@link InputRequiredException}
	 * @throws InputRequiredException if the changelog file requirest some sort of user input. The
	 *             error object will list of the user prompts and type of data for each prompt
	 */
	public static void update(Map<String, Object> userInput) throws DatabaseUpdateException, InputRequiredException {
		log.debug("update database");
		
		executeChangelog(CHANGE_LOG_FILE, userInput);
		
		log.debug("update database finished");
	}
	
	/**
	 * Ask Liquibase if it needs to do any updates
	 * 
	 * @return true/false whether database updates are required
	 */
	public static boolean updatesRequired() {
		log.debug("checking for updates");
		
		Liquibase liquibase = null;
		try {
			liquibase = getLiquibase(null);
			List<ChangeSet> changesets = liquibase.listUnrunChangeSets(CONTEXT);
			return changesets.size() > 0;
			
		}
		catch (Exception e) {
			throw new RuntimeException("error checking for updates in the database", e);
		}
		finally {
			try {
				liquibase.getDatabase().getConnection().close();
			}
			catch (Throwable t) {
				//pass
			}
		}
	}
	
	/**
	 * Executes the given changelog file. This file is assumed to be on the classpath. If no file is
	 * given, the default {@link #CHANGE_LOG_FILE} is ran.
	 * 
	 * @param changelog The string filename of a liquibase changelog xml file to run
	 * @param userInput nullable map from question to user answer. Used if a call to
	 *            executeChangelog(<String>, null) threw an {@link InputRequiredException}
	 * @throws InputRequiredException if the changelog file requirest some sort of user input. The
	 *             error object will list of the user prompts and type of data for each prompt
	 */
	public static void executeChangelog(String changelog, Map<String, Object> userInput) throws DatabaseUpdateException,
	                                                                                    InputRequiredException {
		log.debug("installing the tables into the database");
		
		if (changelog == null)
			changelog = CHANGE_LOG_FILE;
		
		Liquibase liquibase = null;
		try {
			liquibase = getLiquibase(changelog);
			liquibase.update(CONTEXT);
		}
		catch (Exception e) {
			throw new DatabaseUpdateException("There was an error while updating the database to the latest. file: "
			        + changelog, e);
		}
		finally {
			try {
				liquibase.getDatabase().getConnection().close();
			}
			catch (Throwable t) {
				//pass
			}
		}
	}
	
	/**
	 * Takes the default properties defined in /metadata/api/hibernate/hibernate.default.properties
	 * and merges it into the user-defined runtime properties
	 * 
	 * @see org.openmrs.api.db.ContextDAO#mergeDefaultRuntimeProperties(java.util.Properties)
	 */
	private static void mergeDefaultRuntimeProperties(Properties runtimeProperties) {
		
		// loop over runtime properties and precede each with "hibernate" if
		// it isn't already
		Set<Object> runtimePropertyKeys = new HashSet<Object>();
		runtimePropertyKeys.addAll(runtimeProperties.keySet()); // must do it this way to prevent concurrent mod errors
		for (Object key : runtimePropertyKeys) {
			String prop = (String) key;
			String value = (String) runtimeProperties.get(key);
			log.trace("Setting property: " + prop + ":" + value);
			if (!prop.startsWith("hibernate") && !runtimeProperties.containsKey("hibernate." + prop))
				runtimeProperties.setProperty("hibernate." + prop, value);
		}
		
		// load in the default hibernate properties from hibernate.default.properties
		InputStream propertyStream = null;
		try {
			Properties props = new Properties();
			// TODO: This is a dumb requirement to have hibernate in here.  Clean this up
			propertyStream = DatabaseUpdater.class.getClassLoader().getResourceAsStream("hibernate.default.properties");
			props.load(propertyStream);
			
			// add in all default properties that don't exist in the runtime 
			// properties yet
			for (Map.Entry<Object, Object> entry : props.entrySet()) {
				if (!runtimeProperties.containsKey(entry.getKey()))
					runtimeProperties.put(entry.getKey(), entry.getValue());
			}
		}
		catch (IOException e) {
			log.fatal("Unable to load default hibernate properties", e);
		}
		finally {
			try {
				propertyStream.close();
			}
			catch (Throwable t) {
				// pass 
			}
		}
	}
	
	/**
	 * Get a connection to the database through Liquibase. The calling method /must/ close the
	 * database connection when finished with this Liquibase object.
	 * liquibase.getDatabase().getConnection().close()
	 * 
	 * @return Liquibase object based on the current connection settings
	 * @throws Exception
	 */
	private static Liquibase getLiquibase(String changeLogFile) throws Exception {
		Connection connection = null;
		try {
			Properties props = Context.getRuntimeProperties();
			mergeDefaultRuntimeProperties(props);
			
			String driver = props.getProperty("hibernate.connection.driver_class");
			String username = props.getProperty("hibernate.connection.username");
			String password = props.getProperty("hibernate.connection.password");
			String url = props.getProperty("hibernate.connection.url");
			
			Class.forName(driver);
			connection = DriverManager.getConnection(url, username, password);
			
			Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection);
			database.setDatabaseChangeLogTableName("liquibasechangelog");
			database.setDatabaseChangeLogLockTableName("liquibasechangeloglock");
			
			if (driver.startsWith("org.hsqldb")) {
				// a hack because hsqldb seems to be checking table names in the metadata section case sensitively
				database.setDatabaseChangeLogTableName(database.getDatabaseChangeLogTableName().toUpperCase());
				database.setDatabaseChangeLogLockTableName(database.getDatabaseChangeLogLockTableName().toUpperCase());
			}
			
			FileOpener clFO = new ClassLoaderFileOpener();
			FileOpener fsFO = new FileSystemFileOpener();
			
			if (changeLogFile == null)
				changeLogFile = CHANGE_LOG_FILE;
			
			return new Liquibase(changeLogFile, new CompositeFileOpener(clFO, fsFO), database);
			
		}
		catch (Exception e) {
			// if an error occurs, close the connection
			if (connection != null)
				connection.close();
			throw e;
		}
	}
}
