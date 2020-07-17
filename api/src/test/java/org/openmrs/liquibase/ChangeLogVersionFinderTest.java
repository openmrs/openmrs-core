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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ChangeLogVersionFinderTest {
	
	private static final char SLASH = '/';
	
	private static final String SNAPSHOTS_CORE_DATA_1_9_X_FILENAME = "org/openmrs/liquibase/snapshots/core-data/liquibase-core-data-1.9.x.xml".replace(SLASH, File.separatorChar);
	
	private static final String SNAPSHOTS_SCHEMA_ONLY_1_9_X_FILENAME = "org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-1.9.x.xml".replace(SLASH, File.separatorChar);
	
	private static final String SNAPSHOTS_CORE_DATA_2_1_X_FILENAME = "org/openmrs/liquibase/snapshots/core-data/liquibase-core-data-2.1.x.xml".replace(SLASH, File.separatorChar);
	
	private static final String SNAPSHOTS_SCHEMA_ONLY_2_1_X_FILENAME = "org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.1.x.xml".replace(SLASH, File.separatorChar);
	
	private static final String UPDATES_LIQUIBASE_UPDATE_TO_LATEST_1_9_X_FILENAME = "org/openmrs/liquibase/updates/liquibase-update-to-latest-1.9.x.xml".replace(SLASH, File.separatorChar);
	
	private static final String UPDATES_LIQUIBASE_UPDATE_TO_LATEST_2_0_X_FILENAME = "org/openmrs/liquibase/updates/liquibase-update-to-latest-2.0.x.xml".replace(SLASH, File.separatorChar);
	
	private static final String UPDATES_LIQUIBASE_UPDATE_TO_LATEST_2_1_X_FILENAME = "org/openmrs/liquibase/updates/liquibase-update-to-latest-2.1.x.xml".replace(SLASH, File.separatorChar);
	
	private static final String UPDATES_LIQUIBASE_UPDATE_TO_LATEST_2_2_X_FILENAME = "org/openmrs/liquibase/updates/liquibase-update-to-latest-2.2.x.xml".replace(SLASH, File.separatorChar);
	
	private static final String NON_EXISTING_VERSION_42_7_X = "42.7.x";
	
	private static final String OPENMRS_LONG_VERSION = "1.2.3 SNAPSHOT Build 12ab34";
	
	private static final String OPENMRS_MAJOR_MINOR_X = "1.2.x";
	
	private static final String OPENMRS_NOT_A_VERSION = "some random string";
	
	private static final String OPENMRS_SHORT_VERSION = "1.2.3-12ab34";
	
	private static final String VERSION_1_9_X = "1.9.x";
	
	private static final String VERSION_2_0_X = "2.0.x";
	
	private static final String VERSION_2_1_X = "2.1.x";
	
	private static final String VERSION_2_2_X = "2.2.x";
	
	private ChangeLogVersionFinder changeLogVersionFinder;
	
	private ChangeLogVersions changeLogVersions;
	
	@BeforeEach
	public void setup() {
		changeLogVersions = mock(ChangeLogVersions.class);
		
		when(changeLogVersions.getSnapshotVersions()).thenReturn(Arrays.asList(VERSION_1_9_X, VERSION_2_1_X));
		
		when(changeLogVersions.getUpdateVersions())
		        .thenReturn(Arrays.asList(VERSION_1_9_X, VERSION_2_0_X, VERSION_2_1_X, VERSION_2_2_X));
		
		changeLogVersionFinder = new ChangeLogVersionFinder(changeLogVersions);
	}
	
	@Test
	public void shouldGetLiquibaseChangeSetCombinations() {
		Map<String, List<String>> actual = changeLogVersionFinder.getChangeLogCombinations();
		
		List<String> liquibaseChangeSetsForSnapshot_1_9 = Arrays.asList(SNAPSHOTS_SCHEMA_ONLY_1_9_X_FILENAME,
		    SNAPSHOTS_CORE_DATA_1_9_X_FILENAME, UPDATES_LIQUIBASE_UPDATE_TO_LATEST_2_0_X_FILENAME,
		    UPDATES_LIQUIBASE_UPDATE_TO_LATEST_2_1_X_FILENAME, UPDATES_LIQUIBASE_UPDATE_TO_LATEST_2_2_X_FILENAME);
		
		List<String> liquibaseChangeSetsForSnapshot_2_1 = Arrays.asList(SNAPSHOTS_SCHEMA_ONLY_2_1_X_FILENAME,
		    SNAPSHOTS_CORE_DATA_2_1_X_FILENAME, UPDATES_LIQUIBASE_UPDATE_TO_LATEST_2_2_X_FILENAME);
		
		Map<String, List<String>> expected = new HashMap<>();
		expected.put(VERSION_1_9_X, liquibaseChangeSetsForSnapshot_1_9);
		expected.put(VERSION_2_1_X, liquibaseChangeSetsForSnapshot_2_1);
		
		assertEquals(expected, actual);
	}
	
	@Test
	public void shouldGetLiquibaseSnapshotCombinations() {
		Map<String, List<String>> actual = changeLogVersionFinder.getSnapshotCombinations();
		
		List<String> liquibaseChangeSetsForSnapshot_1_9 = Arrays.asList(SNAPSHOTS_SCHEMA_ONLY_1_9_X_FILENAME,
		    SNAPSHOTS_CORE_DATA_1_9_X_FILENAME);
		
		List<String> liquibaseChangeSetsForSnapshot_2_1 = Arrays.asList(SNAPSHOTS_SCHEMA_ONLY_2_1_X_FILENAME,
		    SNAPSHOTS_CORE_DATA_2_1_X_FILENAME);
		
		Map<String, List<String>> expected = new HashMap<>();
		expected.put(VERSION_1_9_X, liquibaseChangeSetsForSnapshot_1_9);
		expected.put(VERSION_2_1_X, liquibaseChangeSetsForSnapshot_2_1);
		
		assertEquals(expected, actual);
	}
	
	@Test
	public void shouldGetLiquibaseSnapshotFilenames() {
		List<String> actual = changeLogVersionFinder.getSnapshotFilenames("1.2.3-one-two-three");
		List<String> expected = Arrays.asList("org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-1.2.x.xml".replace(SLASH, File.separatorChar),
		    "org/openmrs/liquibase/snapshots/core-data/liquibase-core-data-1.2.x.xml".replace(SLASH, File.separatorChar));
		
		assertEquals(expected, actual);
	}
	
	@Test
	public void shouldGetLatestLiquibaseSnapshotVersion() {
		Optional<String> actual = changeLogVersionFinder.getLatestSnapshotVersion();
		Optional<String> expected = Optional.of(VERSION_2_1_X);
		assertEquals(expected, actual);
	}
	
	@Test
	public void shouldGetLatestLiquibaseSchemaSnapshotFilename() {
		Optional<String> actual = changeLogVersionFinder.getLatestSchemaSnapshotFilename();
		Optional<String> expected = Optional.of(SNAPSHOTS_SCHEMA_ONLY_2_1_X_FILENAME);
		assertEquals(expected, actual);
	}
	
	@Test
	public void shouldGetLatestLiquibaseCoreDataSnapshotFilename() {
		Optional<String> actual = changeLogVersionFinder.getLatestCoreDataSnapshotFilename();
		Optional<String> expected = Optional.of(SNAPSHOTS_CORE_DATA_2_1_X_FILENAME);
		assertEquals(expected, actual);
	}
	
	@Test
	public void shouldGetUpdateVersionsGreaterThanOtherVersion() {
		List<String> actual = changeLogVersionFinder.getUpdateVersionsGreaterThan(VERSION_1_9_X);
		List<String> expected = Arrays.asList(VERSION_2_0_X, VERSION_2_1_X, VERSION_2_2_X);
		assertEquals(expected, actual);
	}
	
	@Test
	public void shouldGetNoUpdateVersionsGreaterThanFutureVersion() {
		List<String> actual = changeLogVersionFinder.getUpdateVersionsGreaterThan(NON_EXISTING_VERSION_42_7_X);
		assertThat(actual, is(empty()));
	}
	
	@Test
	public void shouldHandleEmtpyString() {
		assertThrows(IllegalArgumentException.class, () -> changeLogVersionFinder.getUpdateVersionsGreaterThan(""));
	}
	
	@Test
	public void shouldGetLiquibaseUpdateFileNames() {
		List<String> versions = Arrays.asList(VERSION_1_9_X, VERSION_2_0_X, VERSION_2_1_X);
		List<String> actual = changeLogVersionFinder.getUpdateFileNames(versions);
		List<String> expected = Arrays.asList(UPDATES_LIQUIBASE_UPDATE_TO_LATEST_1_9_X_FILENAME,
		    UPDATES_LIQUIBASE_UPDATE_TO_LATEST_2_0_X_FILENAME, UPDATES_LIQUIBASE_UPDATE_TO_LATEST_2_1_X_FILENAME);
		assertEquals(expected, actual);
	}
	
	@Test
	public void shouldGetLiquibaseSnapshotVersions() {
		List<String> actual = changeLogVersionFinder.getSnapshotVersions();
		List<String> expected = Arrays.asList(VERSION_1_9_X, VERSION_2_1_X);
		assertEquals(expected, actual);
	}
	
	@Test
	public void shouldGetLiquibaseUpdateVersions() {
		List<String> actual = changeLogVersionFinder.getUpdateVersions();
		List<String> expected = Arrays.asList(VERSION_1_9_X, VERSION_2_0_X, VERSION_2_1_X, VERSION_2_2_X);
		assertEquals(expected, actual);
	}
	
	@Test
	public void shouldGetVersionAsDotXFromShortVersionName() {
		String actual = changeLogVersionFinder.getVersionAsDotX(OPENMRS_SHORT_VERSION);
		String expected = OPENMRS_MAJOR_MINOR_X;
		assertEquals(expected, actual);
	}
	
	@Test
	public void shouldGetVersionAsDotXFromLongVersionName() {
		String actual = changeLogVersionFinder.getVersionAsDotX(OPENMRS_LONG_VERSION);
		String expected = OPENMRS_MAJOR_MINOR_X;
		assertEquals(expected, actual);
	}
	
	@Test
	public void shouldHandleNonPatternMatchingVersionName() {
		assertThrows(IllegalArgumentException.class, () -> changeLogVersionFinder.getVersionAsDotX(OPENMRS_NOT_A_VERSION));
	}
}
