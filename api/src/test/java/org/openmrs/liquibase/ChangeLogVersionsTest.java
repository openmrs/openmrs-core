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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public class ChangeLogVersionsTest {
	
	private static final org.slf4j.Logger log = LoggerFactory.getLogger(ChangeLogVersionFinderTest.class);
	
	private static final String CORE_DATA_BASE_NAME = ChangeLogVersionFinder.CORE_DATA_BASE_NAME;
	
	private static final String CORE_DATA_PATTERN = "classpath*:" + ChangeLogVersionFinder.CORE_DATA_FOLDER_NAME
	        + File.separator + "*";
	
	private static final String SCHEMA_ONLY_BASE_NAME = ChangeLogVersionFinder.SCHEMA_ONLY_BASE_NAME;
	
	private static final String SCHEMA_ONLY_PATTERN = "classpath*:" + ChangeLogVersionFinder.SCHEMA_ONLY_FOLDER_NAME
	        + File.separator + "*";
	
	private static final String UPDATE_TO_LATEST_BASE_NAME = ChangeLogVersionFinder.UPDATE_TO_LATEST_BASE_NAME;
	
	private static final String UPDATE_TO_LATEST_PATTERN = "classpath*:" + ChangeLogVersionFinder.UPDATES_FOLDER_NAME
	        + File.separator + "*";
	
	private ChangeLogVersions changeLogVersions;
	
	@BeforeEach
	public void setup() {
		changeLogVersions = new ChangeLogVersions();
	}
	
	/**
	 * This test compares the static list of Liquibase snapshot versions defined by
	 * org.openmrs.liquibase.ChangeLogVersions#getSnapshotVersions() with the list of actual change log
	 * files in the two folders
	 * <li>openmrs-core/api/src/main/resources/liquibase/snapshots/core-data
	 * <li>openmrs-core/api/src/main/resources/liquibase/snapshots/schema-only If this test fails,
	 * org.openmrs.liquibase.ChangeLogVersions#SNAPSHOT_VERSIONS needs to be updated.
	 *
	 * @throws IOException
	 */
	@Test
	public void shouldGetSnapshotVersions() throws IOException {
		compareActualAndExpectedChangeLogs(changeLogVersions.getSnapshotVersions(), CORE_DATA_BASE_NAME, CORE_DATA_PATTERN);
		compareActualAndExpectedChangeLogs(changeLogVersions.getSnapshotVersions(), SCHEMA_ONLY_BASE_NAME,
		    SCHEMA_ONLY_PATTERN);
	}
	
	/**
	 * This test compares the static list of Liquibase update versions defined by
	 * org.openmrs.liquibase.ChangeLogVersions#getUpdateVersions() with the list of actual change log
	 * files in the folder openmrs-core/api/src/main/resources/liquibase/updates. If this test fails,
	 * org.openmrs.liquibase.ChangeLogVersions#UPDATE_VERSIONS needs to be updated.
	 *
	 * @throws IOException
	 */
	@Test
	public void shouldGetUpdateVersions() throws IOException {
		compareActualAndExpectedChangeLogs(changeLogVersions.getUpdateVersions(), UPDATE_TO_LATEST_BASE_NAME,
		    UPDATE_TO_LATEST_PATTERN);
	}
	
	/**
	 * Tests a helper method implemented in this test class.
	 */
	@Test
	public void shouldGetChangeLogNameFromVersions() {
		List<String> actual = this.getChangelogNamesFromVersions(Arrays.asList("alpha", "bravo", "charlie"), "basename-");
		List<String> expected = Arrays.asList("basename-alpha.xml", "basename-bravo.xml", "basename-charlie.xml");
		assertEquals(expected, actual);
	}
	
	private void compareActualAndExpectedChangeLogs(List<String> versions, String basename, String pattern)
	        throws IOException {
		List<String> expectedChangeLogFiles = getChangelogNamesFromVersions(versions, basename);
		List<String> actualChangeLogFiles = lookupLiquibaseChangeLogs(pattern);
		assertEquals(expectedChangeLogFiles, actualChangeLogFiles);
	}
	
	private List<String> getChangelogNamesFromVersions(List<String> versions, String baseName) {
		List<String> changeLogNames = new ArrayList<>();
		for (String version : versions) {
			changeLogNames.add(String.format("%s%s.xml", baseName, version));
		}
		return changeLogNames;
	}
	
	private List<String> lookupLiquibaseChangeLogs(String resourcePattern) throws IOException {
		PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
		Resource[] resources = resourcePatternResolver.getResources(resourcePattern);
		
		log.debug("Liquibase resources found for pattern '{}' are: {}", resourcePattern, Arrays.toString(resources));
		
		return Arrays.stream(resources).map(resource -> resource.getFilename()).sorted().collect(Collectors.toList());
	}
}
