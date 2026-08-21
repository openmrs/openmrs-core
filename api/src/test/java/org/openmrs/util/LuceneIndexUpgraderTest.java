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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for {@link LuceneIndexUpgrader}
 * 
 * @since 2.4.0
 */
public class LuceneIndexUpgraderTest {
	
	@TempDir
	Path tempDir;
	
	@Test
	public void upgradeLuceneIndexesIfNeeded_shouldReturnFalseWhenNoAppDataDir() {
		Properties props = new Properties();
		// No application data directory set
		
		boolean result = LuceneIndexUpgrader.upgradeLuceneIndexesIfNeeded(props);
		
		assertFalse(result);
	}
	
	@Test
	public void upgradeLuceneIndexesIfNeeded_shouldReturnFalseWhenLuceneDirDoesNotExist() throws IOException {
		Properties props = new Properties();
		props.setProperty(OpenmrsConstants.KEY_OPENMRS_APPLICATION_DATA_DIRECTORY, tempDir.toString());
		
		boolean result = LuceneIndexUpgrader.upgradeLuceneIndexesIfNeeded(props);
		
		assertFalse(result);
	}
	
	@Test
	public void upgradeLuceneIndexesIfNeeded_shouldReturnFalseWhenLuceneDirExistsButEmpty() throws IOException {
		Properties props = new Properties();
		props.setProperty(OpenmrsConstants.KEY_OPENMRS_APPLICATION_DATA_DIRECTORY, tempDir.toString());
		
		// Create lucene directory but leave it empty
		Path luceneDir = tempDir.resolve("lucene").resolve("indexes");
		Files.createDirectories(luceneDir);
		
		boolean result = LuceneIndexUpgrader.upgradeLuceneIndexesIfNeeded(props);
		
		assertFalse(result);
	}
	
	@Test
	public void upgradeLuceneIndexesIfNeeded_shouldHandleNonExistentAppDataDir() {
		Properties props = new Properties();
		props.setProperty(OpenmrsConstants.KEY_OPENMRS_APPLICATION_DATA_DIRECTORY, "/nonexistent/path");
		
		// Should not throw exception, just return false
		boolean result = LuceneIndexUpgrader.upgradeLuceneIndexesIfNeeded(props);
		
		assertFalse(result);
	}
}
