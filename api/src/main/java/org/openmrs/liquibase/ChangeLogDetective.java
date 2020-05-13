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
	
	private static final String DISABLE_FOREIGN_KEY_CHECKS = "disable-foreign-key-checks";
	
	private static final String ENABLE_FOREIGN_KEY_CHECKS = "enable-foreign-key-checks";
	
	private static final int MAX_NUMBER_OF_CHANGE_SETS_TO_LOG = 10;
	
	private static final String LIQUIBASE_CORE_DATA_1_9_X_FILENAME = "liquibase-core-data-1.9.x.xml";
	
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
			
			for (String filename : changeSets) {
				Liquibase liquibase = liquibaseProvider.getLiquibase(filename);
				List<ChangeSet> rawUnrunChangeSets = liquibase.listUnrunChangeSets(new Contexts(context),
				    new LabelExpression());
				List<ChangeSet> refinedUnrunChangeSets = excludeVintageChangeSets(filename, rawUnrunChangeSets);
				
				log.info("file '{}' contains {} un-run change sets", filename, refinedUnrunChangeSets.size());
				logUnrunChangeSetDetails(filename, refinedUnrunChangeSets);
				
				unrunChangeSetsCount += refinedUnrunChangeSets.size();
			}
			
			if (unrunChangeSetsCount == 0) {
				log.info("the Liquibase snapshot version that had been used to initialize the OpenMRS database is '{}'",
				    version);
				return version;
			}
		}
		
		throw new IllegalStateException(
		        "identifying the snapshot version that had been used to initialize the OpenMRS database failed as no candidate change set resulted in zero un-run changes");
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
		
		for (String filename : updateFileNames) {
			Liquibase liquibase = liquibaseProvider.getLiquibase(filename);
			List<ChangeSet> unrunChangeSets = liquibase.listUnrunChangeSets(new Contexts(context), new LabelExpression());
			log.info("file '{}}' contains {} un-run change sets", filename, unrunChangeSets.size());
			
			if (unrunChangeSets.size() > 0) {
				unrunLiquibaseUpdates.add(filename);
			}
		}
		
		return unrunLiquibaseUpdates;
	}
	
	List<String> getSnapshotVersionsInDescendingOrder(Map<String, List<String>> snapshotCombinations) {
		List<String> versions = new ArrayList<>();
		versions.addAll(snapshotCombinations.keySet());
		Collections.sort(versions, Collections.reverseOrder());
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
	
	boolean isVintageChangeSet(String filename, ChangeSet changeSet) {
		if (filename != null && filename.contains(LIQUIBASE_CORE_DATA_1_9_X_FILENAME)
		        && changeSet.getId().equals(DISABLE_FOREIGN_KEY_CHECKS) && changeSet.getAuthor().equals(BEN)) {
			return true;
		}
		if (filename != null && filename.contains(LIQUIBASE_CORE_DATA_1_9_X_FILENAME)
		        && changeSet.getId().equals(ENABLE_FOREIGN_KEY_CHECKS) && changeSet.getAuthor().equals(BEN)) {
			return true;
		}
		return false;
	}
	
	private void logUnrunChangeSetDetails(String filename, List<ChangeSet> filteredUnrunChangeSets) {
		if (filteredUnrunChangeSets.size() < MAX_NUMBER_OF_CHANGE_SETS_TO_LOG) {
			for (ChangeSet changeSet : filteredUnrunChangeSets) {
				log.debug("file '{}' contains un-run change set with id '{}' by author '{}'", filename, changeSet.getId(),
				    changeSet.getAuthor());
			}
		}
	}
}
