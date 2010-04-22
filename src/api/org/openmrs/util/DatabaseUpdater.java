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

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import liquibase.ChangeSet;
import liquibase.ClassLoaderFileOpener;
import liquibase.CompositeFileOpener;
import liquibase.DatabaseChangeLog;
import liquibase.FileOpener;
import liquibase.FileSystemFileOpener;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.exception.LiquibaseException;
import liquibase.exception.LockException;
import liquibase.lock.LockHandler;
import liquibase.parser.ChangeLogIterator;
import liquibase.parser.ChangeLogParser;
import liquibase.parser.filter.ContextChangeSetFilter;
import liquibase.parser.filter.DbmsChangeSetFilter;
import liquibase.parser.filter.ShouldRunChangeSetFilter;
import liquibase.parser.visitor.UpdateVisitor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.context.Context;

/**
 * This class uses Liquibase to update the database. <br/>
 * <br/>
 * See /metadata/model/liquibase-update-to-latest.xml for the changes. This class will also run
 * arbitrary liquibase xml files on the associated database as well. Details for the database are
 * taken from the openmrs runtime properties.
 * 
 * @since 1.5
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
	 */
	public static void executeChangelog() throws DatabaseUpdateException, InputRequiredException {
		executeChangelog(null, null);
	}
	
	/**
	 * Run changesets on database using Liquibase to get the database up to the most recent version
	 * 
	 * @param changelog the liquibase changelog file to use (or null to use the default file)
	 * @param userInput nullable map from question to user answer. Used if a call to update(null)
	 *            threw an {@link InputRequiredException}
	 * @throws DatabaseUpdateException
	 * @throws InputRequiredException
	 */
	public static void executeChangelog(String changelog, Map<String, Object> userInput) throws DatabaseUpdateException,
	                                                                                    InputRequiredException {
		
		log.debug("Executing changelog: " + changelog);
		
		// a call back that, well, does nothing
		ChangeSetExecutorCallback doNothingCallback = new ChangeSetExecutorCallback() {
			
			public void executing(ChangeSet changeSet, int numChangeSetsToRun) {
				log.debug("Executing changeset: " + changeSet.getId() + " numChangeSetsToRun: " + numChangeSetsToRun);
			}
			
		};
		
		executeChangelog(changelog, userInput, doNothingCallback);
	}
	
	/**
	 * Interface used for callbacks when updating the database. Implement this interface and pass it
	 * to {@link DatabaseUpdater#executeChangelog(String, Map, ChangeSetExecutorCallback)}
	 */
	public interface ChangeSetExecutorCallback {
		
		/**
		 * This method is called after each changeset is executed.
		 * 
		 * @param changeSet the liquibase changeset that was just run
		 * @param numChangeSetsToRun the total number of changesets in the current file
		 */
		public void executing(ChangeSet changeSet, int numChangeSetsToRun);
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
	public static void executeChangelog(String changelog, Map<String, Object> userInput, ChangeSetExecutorCallback callback)
	                                                                                                                        throws DatabaseUpdateException,
	                                                                                                                        InputRequiredException {
		log.debug("installing the tables into the database");
		
		if (changelog == null)
			changelog = CHANGE_LOG_FILE;
		
		try {
			executeChangelog(changelog, CONTEXT, userInput, callback);
		}
		catch (Exception e) {
			throw new DatabaseUpdateException("There was an error while updating the database to the latest. file: "
			        + changelog + ". Error: " + e.getMessage(), e);
		}
	}
	
	/**
	 * This code was borrowed from the liquibase jar so that we can call the given callback
	 * function.
	 * 
	 * @param changeLogFile the file to execute
	 * @param contexts the liquibase changeset context
	 * @param userInput answers given by the user
	 * @param callback the function to call after every changeset
	 * @throws Exception
	 */
	public static void executeChangelog(String changeLogFile, String contexts, Map<String, Object> userInput,
	                                    ChangeSetExecutorCallback callback) throws Exception {
		final class OpenmrsUpdateVisitor extends UpdateVisitor {
			
			private ChangeSetExecutorCallback callback;
			
			private int numChangeSetsToRun;
			
			public OpenmrsUpdateVisitor(Database database, ChangeSetExecutorCallback callback, int numChangeSetsToRun) {
				super(database);
				this.callback = callback;
				this.numChangeSetsToRun = numChangeSetsToRun;
			}
			
			@Override
			public void visit(ChangeSet changeSet, Database database) throws LiquibaseException {
				callback.executing(changeSet, numChangeSetsToRun);
				super.visit(changeSet, database);
			}
		}
		
		log.debug("Setting up liquibase object to run changelog: " + changeLogFile);
		Liquibase liquibase = getLiquibase(changeLogFile);
		int numChangeSetsToRun = liquibase.listUnrunChangeSets(contexts).size();
		Database database = liquibase.getDatabase();
		
		LockHandler lockHandler = LockHandler.getInstance(database);
		lockHandler.waitForLock();
		
		try {
			database.checkDatabaseChangeLogTable();
			
			FileOpener clFO = new ClassLoaderFileOpener();
			FileOpener fsFO = new FileSystemFileOpener();
			
			DatabaseChangeLog changeLog = new ChangeLogParser(new HashMap<String, Object>()).parse(changeLogFile,
			    new CompositeFileOpener(clFO, fsFO));
			changeLog.validate(database);
			ChangeLogIterator logIterator = new ChangeLogIterator(changeLog, new ShouldRunChangeSetFilter(database),
			        new ContextChangeSetFilter(contexts), new DbmsChangeSetFilter(database));
			
			logIterator.run(new OpenmrsUpdateVisitor(database, callback, numChangeSetsToRun), database);
		}
		catch (LiquibaseException e) {
			throw e;
		}
		finally {
			try {
				lockHandler.releaseLock();
			}
			catch (LockException e) {
				log.error("Could not release lock", e);
			}
			try {
				database.getConnection().close();
			}
			catch (Throwable t) {
				//pass
			}
		}
	}
	
	/**
	 * Ask Liquibase if it needs to do any updates
	 * 
	 * @return true/false whether database updates are required
	 * @should always have a valid update to latest file
	 */
	public static boolean updatesRequired() throws Exception {
		log.debug("checking for updates");
		
		List<OpenMRSChangeSet> changesets = getUnrunDatabaseChanges();
		return changesets.size() > 0;
	}
	
	/**
	 * Indicates whether automatic database updates are allowed by this server. Automatic updates
	 * are disabled by default. In order to enable automatic updates, the admin needs to add
	 * 'auto_update_database=true' to the runtime properties file.
	 * 
	 * @return true/false whether the 'auto_update_database' has been enabled.
	 */
	public static Boolean allowAutoUpdate() {
		String allowAutoUpdate = Context.getRuntimeProperties().getProperty(
		    OpenmrsConstants.AUTO_UPDATE_DATABASE_RUNTIME_PROPERTY, "false");
		
		return "true".equals(allowAutoUpdate);
		
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
			OpenmrsUtil.loadProperties(props, propertyStream);
			// add in all default properties that don't exist in the runtime 
			// properties yet
			for (Map.Entry<Object, Object> entry : props.entrySet()) {
				if (!runtimeProperties.containsKey(entry.getKey()))
					runtimeProperties.put(entry.getKey(), entry.getValue());
			}
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
			connection = getConnection();
		}
		catch (SQLException e) {
			throw new Exception("Unable to get a connection to the database.  Please check your openmrs runtime properties file and make sure you have the correct connection.username and connection.password set", e);
		}
		
		try {
			Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection);
			database.setDatabaseChangeLogTableName("liquibasechangelog");
			database.setDatabaseChangeLogLockTableName("liquibasechangeloglock");
			
			if (connection.getMetaData().getDatabaseProductName().contains("HSQL Database Engine")) {
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
	
	/**
	 * Gets a database connection for liquibase to do the updates
	 * 
	 * @return a java.sql.connection based on the current runtime properties
	 */
	public static Connection getConnection() throws Exception {
		Properties props = Context.getRuntimeProperties();
		mergeDefaultRuntimeProperties(props);
		
		String driver = props.getProperty("hibernate.connection.driver_class");
		String username = props.getProperty("hibernate.connection.username");
		String password = props.getProperty("hibernate.connection.password");
		String url = props.getProperty("hibernate.connection.url");
		
		// hack for mysql to make sure innodb tables are created
		if (url.contains("mysql") && !url.contains("InnoDB")) {
			url = url + "&sessionVariables=storage_engine=InnoDB";
		}
		
		Class.forName(driver);
		return DriverManager.getConnection(url, username, password);
	}
	
	/**
	 * Represents each change in the liquibase-update-to-latest
	 */
	public static class OpenMRSChangeSet {
		
		private String id;
		
		private String author;
		
		private String comments;
		
		private String description;
		
		private ChangeSet.RunStatus runStatus;
		
		private Date ranDate;
		
		/**
		 * Create an OpenmrsChangeSet from the given changeset
		 * 
		 * @param changeSet
		 * @param database
		 */
		public OpenMRSChangeSet(ChangeSet changeSet, Database database) throws Exception {
			setId(changeSet.getId());
			setAuthor(changeSet.getAuthor());
			setComments(changeSet.getComments());
			setDescription(changeSet.getDescription());
			setRunStatus(database.getRunStatus(changeSet));
			setRanDate(database.getRanDate(changeSet));
		}
		
		/**
		 * @return the author
		 */
		public String getAuthor() {
			return author;
		}
		
		/**
		 * @param author the author to set
		 */
		public void setAuthor(String author) {
			this.author = author;
		}
		
		/**
		 * @return the comments
		 */
		public String getComments() {
			return comments;
		}
		
		/**
		 * @param comments the comments to set
		 */
		public void setComments(String comments) {
			this.comments = comments;
		}
		
		/**
		 * @return the description
		 */
		public String getDescription() {
			return description;
		}
		
		/**
		 * @param description the description to set
		 */
		public void setDescription(String description) {
			this.description = description;
		}
		
		/**
		 * @return the runStatus
		 */
		public ChangeSet.RunStatus getRunStatus() {
			return runStatus;
		}
		
		/**
		 * @param runStatus the runStatus to set
		 */
		public void setRunStatus(ChangeSet.RunStatus runStatus) {
			this.runStatus = runStatus;
		}
		
		/**
		 * @return the ranDate
		 */
		public Date getRanDate() {
			return ranDate;
		}
		
		/**
		 * @param ranDate the ranDate to set
		 */
		public void setRanDate(Date ranDate) {
			this.ranDate = ranDate;
		}
		
		/**
		 * @return the id
		 */
		public String getId() {
			return id;
		}
		
		/**
		 * @param id the id to set
		 */
		public void setId(String id) {
			this.id = id;
		}
		
	}
	
	/**
	 * Looks at the current liquibase-update-to-latest.xml file and then checks the database to see
	 * if they have been run.
	 * 
	 * @return list of changesets that both have and haven't been run
	 */
	@Authorized(OpenmrsConstants.PRIV_VIEW_DATABASE_CHANGES)
	public static List<OpenMRSChangeSet> getDatabaseChanges() throws Exception {
		Database database = null;
		
		try {
			Liquibase liquibase = getLiquibase(CHANGE_LOG_FILE);
			database = liquibase.getDatabase();
			DatabaseChangeLog changeLog = new ChangeLogParser(new HashMap<String, Object>()).parse(CHANGE_LOG_FILE,
			    liquibase.getFileOpener());
			List<ChangeSet> changeSets = changeLog.getChangeSets();
			
			List<OpenMRSChangeSet> results = new ArrayList<OpenMRSChangeSet>();
			for (ChangeSet changeSet : changeSets) {
				OpenMRSChangeSet omrschangeset = new OpenMRSChangeSet(changeSet, database);
				results.add(omrschangeset);
			}
			
			return results;
		}
		finally {
			try {
				if (database != null) {
					database.getConnection().close();
				}
			}
			catch (Throwable t) {
				//pass
			}
		}
	}
	
	/**
	 * Looks at the current liquibase-update-to-latest.xml file returns all changesets in that file
	 * that have not been run on the database yet.
	 * 
	 * @return list of changesets that haven't been run
	 */
	@Authorized(OpenmrsConstants.PRIV_VIEW_DATABASE_CHANGES)
	public static List<OpenMRSChangeSet> getUnrunDatabaseChanges() throws Exception {
		log.debug("Getting unrun changesets");
		
		Database database = null;
		try {
			Liquibase liquibase = getLiquibase(null);
			database = liquibase.getDatabase();
			List<ChangeSet> changeSets = liquibase.listUnrunChangeSets(CONTEXT);
			
			List<OpenMRSChangeSet> results = new ArrayList<OpenMRSChangeSet>();
			for (ChangeSet changeSet : changeSets) {
				OpenMRSChangeSet omrschangeset = new OpenMRSChangeSet(changeSet, database);
				results.add(omrschangeset);
			}
			
			return results;
			
		}
		catch (Exception e) {
			throw new RuntimeException("Error occurred while trying to get the updates needed for the database. " + e.getMessage(), e);
		}
		finally {
			try {
				database.getConnection().close();
			}
			catch (Throwable t) {
				//pass
			}
		}
	}
}
