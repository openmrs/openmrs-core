/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.test;

import java.util.List;

import org.openmrs.liquibase.ChangeLogVersionFinder;
import org.openmrs.liquibase.ChangeSetExecutorCallback;
import org.openmrs.util.DatabaseUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import liquibase.changelog.ChangeSet;

public class LiquibaseUtil {

	private static final Logger log = LoggerFactory.getLogger(LiquibaseUtil.class);
	
	private static boolean schemaCreated = false;
	
	public static void ensureSchemaCreated() {
		if (schemaCreated) {
			return;
		}
		
		createSchema();
		
		schemaCreated = true;
	}
	
	private static void createSchema() {
		
		try {
			ChangeLogVersionFinder changeLogVersionFinder = new ChangeLogVersionFinder();
			
			String liquibaseSchemaFileName = changeLogVersionFinder.getLatestSchemaSnapshotFilename().get();

			log.info("Executing Liquibase file "+ liquibaseSchemaFileName);
			
			try {
				DatabaseUpdater.executeChangelog(liquibaseSchemaFileName, new PrintingChangeSetExecutorCallback("OpenMRS schema file"));
			}
			catch(Exception ex) {
				log.error("Failed to run the liquibase schema file", ex);
			}			
			
			log.info("Now updating the database to the latest version");
			
			String version = changeLogVersionFinder.getLatestSnapshotVersion().get();
			log.info(String.format("updating the database with versions of liquibase-update-to-latest files greater than %s", version));
				
			List<String> changelogs = changeLogVersionFinder
			        .getUpdateFileNames(changeLogVersionFinder.getUpdateVersionsGreaterThan(version));
			
			for (String changelog : changelogs) {
				log.debug("applying Liquibase changelog '{}'", changelog);
				
				try {
					DatabaseUpdater.executeChangelog(changelog,
					    new PrintingChangeSetExecutorCallback("executing Liquibase changelog " + changelog));
				}
				catch(Exception ex) {
					log.error("Failed to run the latest changesets", ex);
				}
			}
			
		}
		catch (Exception e) {
			log.error("Error while trying to create tables and demo data", e);
		}
	}
	
	/**
	 * A callback class that prints out info about liquibase changesets
	 */
	private static class PrintingChangeSetExecutorCallback implements ChangeSetExecutorCallback {
		
		private int i = 1;
		
		private String message;
		
		public PrintingChangeSetExecutorCallback(String message) {
			this.message = message;
		}
		
		/**
		 * @see ChangeSetExecutorCallback#executing(liquibase.changelog.ChangeSet, int)
		 */
		@Override
		public void executing(ChangeSet changeSet, int numChangeSetsToRun) {
			log.info(message + " (" + i++ + "/" + numChangeSetsToRun + "): Author: "
			        + changeSet.getAuthor() + " Comments: " + changeSet.getComments() + " Description: "
			        + changeSet.getDescription());
		}
	}
}
