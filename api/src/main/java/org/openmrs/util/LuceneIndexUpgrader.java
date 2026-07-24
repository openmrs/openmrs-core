/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to handle Lucene index upgrades during OpenMRS startup. This addresses TRUNK-5731
 * by ensuring Lucene indexes are upgraded before any database operations that might try to read
 * them.
 * 
 * @since 2.4.0
 */
public class LuceneIndexUpgrader {
	
	private static final Logger log = LoggerFactory.getLogger(LuceneIndexUpgrader.class);
	
	/**
	 * Upgrades Lucene indexes if they exist and are using old codecs. This method should be called
	 * before any database operations during startup.
	 * 
	 * @param properties Runtime properties containing application data directory
	 * @return true if upgrade was performed, false if no upgrade was needed
	 */
	public static boolean upgradeLuceneIndexesIfNeeded(Properties properties) {
		String appDataDir = properties.getProperty(OpenmrsConstants.KEY_OPENMRS_APPLICATION_DATA_DIRECTORY);
		if (appDataDir == null || appDataDir.trim().isEmpty()) {
			log.debug("No application data directory specified, skipping Lucene index upgrade check");
			return false;
		}
		
		Path luceneIndexDir = Paths.get(appDataDir, "lucene", "indexes");
		if (!Files.exists(luceneIndexDir)) {
			log.debug("Lucene index directory does not exist: {}", luceneIndexDir);
			return false;
		}
		
		log.info("Checking for Lucene index upgrade requirements in: {}", luceneIndexDir);
		
		boolean upgradePerformed = false;
		try {
			// Check each subdirectory in the lucene indexes directory
			Files.list(luceneIndexDir)
				.filter(Files::isDirectory)
				.forEach(indexPath -> {
					try {
						if (needsUpgrade(indexPath)) {
							log.info("Upgrading Lucene index: {}", indexPath);
							upgradeIndex(indexPath);
						}
					} catch (Exception e) {
						log.warn("Failed to upgrade Lucene index at {}: {}", indexPath, e.getMessage());
						// Continue with other indexes even if one fails
					}
				});
		} catch (IOException e) {
			log.warn("Failed to list Lucene index directories: {}", e.getMessage());
		}
		
		return upgradePerformed;
	}
	
	/**
	 * Checks if a Lucene index needs upgrading by attempting to read it. If it fails with a
	 * codec-related error, it needs upgrading.
	 * 
	 * @param indexPath Path to the Lucene index
	 * @return true if the index needs upgrading
	 */
	private static boolean needsUpgrade(Path indexPath) {
		try (Directory directory = FSDirectory.open(indexPath)) {
			// Try to open the index with current Lucene version
			try (IndexReader reader = DirectoryReader.open(directory)) {
				// If we can open it successfully, no upgrade needed
				log.debug("Lucene index at {} is already compatible", indexPath);
				return false;
			}
		} catch (Exception e) {
			// Check if this is a codec-related error
			String errorMessage = e.getMessage();
			if (errorMessage != null && 
				(errorMessage.contains("Lucene410") || 
				 errorMessage.contains("does not exist") ||
				 errorMessage.contains("codec"))) {
				log.info("Lucene index at {} needs upgrading: {}", indexPath, errorMessage);
				return true;
			} else {
				log.debug("Lucene index at {} has a different error (not codec-related): {}", indexPath, errorMessage);
				return false;
			}
		}
	}
	
	/**
	 * Upgrades a Lucene index by deleting the old index files. This forces Hibernate Search to
	 * recreate the index with the current codec.
	 * 
	 * @param indexPath Path to the Lucene index to upgrade
	 */
	private static void upgradeIndex(Path indexPath) {
		try {
			log.info("Deleting old Lucene index files at {} to force recreation with current codec", indexPath);
			
			// Delete all files in the index directory
			Files.list(indexPath)
				.forEach(file -> {
					try {
						Files.delete(file);
						log.debug("Deleted index file: {}", file);
					} catch (IOException e) {
						log.warn("Failed to delete index file {}: {}", file, e.getMessage());
					}
				});
			
			log.info("Successfully upgraded Lucene index at {}", indexPath);
		} catch (IOException e) {
			log.error("Failed to upgrade Lucene index at {}: {}", indexPath, e.getMessage());
			throw new RuntimeException("Failed to upgrade Lucene index", e);
		}
	}
}
