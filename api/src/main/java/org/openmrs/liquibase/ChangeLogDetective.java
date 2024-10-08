/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.liquibase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.changelog.ChangeSet;
import liquibase.command.core.StatusCommandStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Figures out which Liquibase change logs were used to initialise an OpenMRS database and which
 * change logs need to be run on top of that when updating the database.
 * 
 * @since 2.4
 */
public class ChangeLogDetective {
	
	/*
	 * Log statements from this class are to be logged underneath 'org.openmrs.api' as the log level for this
	 * package is 'INFO', hence the deviation of the actual package and the logger name.
	 */
	private static final Logger log = LoggerFactory.getLogger("org.openmrs.api.ChangeLogDetective");
	
	private static final String BEN = "ben";
	
	private static final String DEFAULT_SNAPSHOT_VERSION = "1.9.x";
	
	private static final String DISABLE_FOREIGN_KEY_CHECKS = "disable-foreign-key-checks";
	
	private static final String ENABLE_FOREIGN_KEY_CHECKS = "enable-foreign-key-checks";
	
	private static final int MAX_NUMBER_OF_CHANGE_SETS_TO_LOG = 10;
	
	private static final String LIQUIBASE_CORE_DATA_1_9_X_FILENAME = "liquibase-core-data-1.9.x.xml";
	
	private static final String LIQUIBASE_SCHEMA_ONLY_1_9_X_FILENAME = "liquibase-schema-only-1.9.x.xml";
	
	private ChangeLogVersionFinder changeLogVersionFinder;
	
	public ChangeLogDetective() {
		changeLogVersionFinder = new ChangeLogVersionFinder();
	}
	
	/**
	 * Returns the version of the Liquibase snapshot that had been used to initialise the OpenMRS
	 * database. The version is needed to determine which Liquibase update files need to be checked for
	 * un-run change sets and may need to be (re-)run to apply the latest changes to the OpenMRS
	 * database.
	 * 
	 * @param liquibaseProvider provides access to a Liquibase instance
	 * @return the version of the Liquibase snapshot that had been used to initialise the OpenMRS
	 *         database
	 * @throws Exception
	 */
	public String getInitialLiquibaseSnapshotVersion(String context, LiquibaseProvider liquibaseProvider) throws Exception {
		log.info("identifying the Liquibase snapshot version that had been used to initialize the OpenMRS database...");
		Map<String, List<String>> snapshotCombinations = changeLogVersionFinder.getSnapshotCombinations();
		
		if (snapshotCombinations.isEmpty()) {
			throw new IllegalStateException(
			        "identifying the Liqubase snapshot version that had been used to initialize the OpenMRS database failed as no candidate change sets were found");
		}
		
		List<String> snapshotVersions = getSnapshotVersionsInDescendingOrder(snapshotCombinations);
		
		for (String version : snapshotVersions) {
			int unrunChangeSetsCount = 0;
			
			log.info("looking for un-run change sets in snapshot version '{}'", version);
			List<String> changeSets = snapshotCombinations.get(version);
			
			Contexts contexts = new Contexts(context);
			for (String filename : changeSets) {
				List<ChangeSet> rawUnrunChangeSets = getUnrunChangeSets(filename, contexts, liquibaseProvider);
				
				List<ChangeSet> refinedUnrunChangeSets = excludeVintageChangeSets(filename, rawUnrunChangeSets);
				
				log.info("file '{}' contains {} un-run change sets", filename, refinedUnrunChangeSets.size());
				logUnRunChangeSetDetails(filename, refinedUnrunChangeSets);
				
				unrunChangeSetsCount += refinedUnrunChangeSets.size();
			}
			
			if (unrunChangeSetsCount == 0) {
				log.info("the Liquibase snapshot version that had been used to initialize the OpenMRS database is '{}'",
				    version);
				return version;
			}
		}
		
		log.info(
		    "the snapshot version that had been used to initialize the OpenMRS database could not be identified, falling back to the default version '{}'",
		    DEFAULT_SNAPSHOT_VERSION);
		
		return DEFAULT_SNAPSHOT_VERSION;
	}
	
	/**
	 * Returns a list of Liquibase update files that contain un-run change sets.
	 *
	 * @param snapshotVersion the snapshot version that had been used to initialise the OpenMRS database
	 * @param liquibaseProvider provides access to a Liquibase instance
	 * @return a list of Liquibase update files that contain un-run change sets.
	 * @throws Exception
	 */
	public List<String> getUnrunLiquibaseUpdateFileNames(String snapshotVersion, String context,
	        LiquibaseProvider liquibaseProvider) throws Exception {
		List<String> unrunLiquibaseUpdates = new ArrayList<>();
		
		List<String> updateVersions = changeLogVersionFinder.getUpdateVersionsGreaterThan(snapshotVersion);
		List<String> updateFileNames = changeLogVersionFinder.getUpdateFileNames(updateVersions);
		
		Contexts contexts = new Contexts(context);
		for (String filename : updateFileNames) {
			List<ChangeSet> unrunChangeSets = getUnrunChangeSets(filename, contexts, liquibaseProvider);

			log.info("file '{}' contains {} un-run change sets", filename, unrunChangeSets.size());
			logUnRunChangeSetDetails(filename, unrunChangeSets);
			
			if (!unrunChangeSets.isEmpty()) {
				unrunLiquibaseUpdates.add(filename);
			}
		}
		
		return unrunLiquibaseUpdates;
	}
	
	List<String> getSnapshotVersionsInDescendingOrder(Map<String, List<String>> snapshotCombinations) {
		List<String> versions = new ArrayList<>(snapshotCombinations.keySet());
		versions.sort(Collections.reverseOrder());
		return versions;
	}
	
	List<ChangeSet> excludeVintageChangeSets(String filename, List<ChangeSet> changeSets) {
		List<ChangeSet> result = new ArrayList<>();
		for (ChangeSet changeSet : changeSets) {
			if (!isVintageChangeSet(filename, changeSet)) {
				result.add(changeSet);
			}
		}
		return result;
	}
	
	List<ChangeSet> getUnrunChangeSets(String filename, Contexts context, LiquibaseProvider liquibaseProvider) throws Exception {
		String scopeId = LiquibaseScopeHandling.enterLiquibaseUILoggingService();
		Liquibase liquibase = liquibaseProvider.getLiquibase(filename);

		List<ChangeSet> unrunChangeSets;
		try {
			unrunChangeSets = new StatusCommandStep()
				.listUnrunChangeSets(context,
					new LabelExpression(), liquibase.getDatabaseChangeLog(), liquibase.getDatabase());

		} finally {
			LiquibaseScopeHandling.exitLiquibaseScope(scopeId);
			liquibase.close();
		}
		
		return unrunChangeSets;
	}
	
	boolean isVintageChangeSet(String filename, ChangeSet changeSet) {
		if (filename != null && filename.contains(LIQUIBASE_CORE_DATA_1_9_X_FILENAME) && changeSet.getAuthor().equals(BEN)) {
			return changeSet.getId().equals(DISABLE_FOREIGN_KEY_CHECKS) || changeSet.getId().equals(ENABLE_FOREIGN_KEY_CHECKS);
		}
		
		return false;
	}
	
	/**
	 * Logs un-run change sets no more than a given number and only for the 1.9.x Liquibase snapshots.
	 * 
	 * @return a boolean value indicating whether the change sets were logged. The value is used for
	 *         testing.
	 */
	boolean logUnRunChangeSetDetails(String filename, List<ChangeSet> changeSets) {
		if (changeSets.size() < MAX_NUMBER_OF_CHANGE_SETS_TO_LOG && (filename.contains(LIQUIBASE_CORE_DATA_1_9_X_FILENAME)
		        || filename.contains(LIQUIBASE_SCHEMA_ONLY_1_9_X_FILENAME))) {
			if (log.isInfoEnabled()) {
				for (ChangeSet changeSet : changeSets) {
					log.info("file '{}' contains un-run change set with id '{}' by author '{}'", filename, changeSet.getId(),
						changeSet.getAuthor());
				}
			}
			
			return true;
		}
		return false;
	}
}
