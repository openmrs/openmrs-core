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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import liquibase.changelog.ChangeSet;
import org.junit.jupiter.api.Test;

public class ChangeLogDetectiveTest {
	
	@Test
	public void shouldGetSnapshotVersionsInDescendingOrder() {
		Map<String, List<String>> snapshotCombinations = new HashMap<>();
		snapshotCombinations.put("1.9.x", null);
		snapshotCombinations.put("2.4.x", null);
		snapshotCombinations.put("2.1.x", null);
		snapshotCombinations.put("2.2.x", null);
		snapshotCombinations.put("2.3.x", null);
		
		ChangeLogDetective changeLogDetective = new ChangeLogDetective();
		List<String> actual = changeLogDetective.getSnapshotVersionsInDescendingOrder(snapshotCombinations);
		List<String> expected = Arrays.asList("2.4.x", "2.3.x", "2.2.x", "2.1.x", "1.9.x");
		
		assertEquals(expected, actual);
	}
	
	@Test
	public void shouldIgnoreDisableForeignKeyChecks() {
		ChangeSet changeSet = mock(ChangeSet.class);
		when(changeSet.getAuthor()).thenReturn("ben");
		when(changeSet.getId()).thenReturn("disable-foreign-key-checks");
		
		ChangeLogDetective changeLogDetective = new ChangeLogDetective();
		boolean actual = changeLogDetective
		        .isVintageChangeSet("org/openmrs/liquibase/snapshots/core-data/liquibase-core-data-1.9.x.xml", changeSet);
		assertTrue(actual);
	}
	
	@Test
	public void shouldIgnoreEnableForeignKeyChecks() {
		ChangeSet changeSet = mock(ChangeSet.class);
		when(changeSet.getAuthor()).thenReturn("ben");
		when(changeSet.getId()).thenReturn("enable-foreign-key-checks");
		
		ChangeLogDetective changeLogDetective = new ChangeLogDetective();
		boolean actual = changeLogDetective
		        .isVintageChangeSet("org/openmrs/liquibase/snapshots/core-data/liquibase-core-data-1.9.x.xml", changeSet);
		assertTrue(actual);
	}
	
	@Test
	public void shouldNotIgnoreDisableForeignKeyChecksForFilenameOtherThanLiquibaseCoreData_1_9_x() {
		ChangeSet changeSet = mock(ChangeSet.class);
		when(changeSet.getAuthor()).thenReturn("ben");
		when(changeSet.getId()).thenReturn("disable-foreign-key-checks");
		
		ChangeLogDetective changeLogDetective = new ChangeLogDetective();
		boolean actual = changeLogDetective.isVintageChangeSet("any_filename", changeSet);
		assertFalse(actual);
	}
	
	@Test
	public void shouldNotIgnoreEnableForeignKeyChecksForFilenameOtherThanLiquibaseCoreData_1_9_x() {
		ChangeSet changeSet = mock(ChangeSet.class);
		when(changeSet.getAuthor()).thenReturn("ben");
		when(changeSet.getId()).thenReturn("enable-foreign-key-checks");
		
		ChangeLogDetective changeLogDetective = new ChangeLogDetective();
		boolean actual = changeLogDetective.isVintageChangeSet("any_filename", changeSet);
		assertFalse(actual);
	}
	
	/*
	 * The vintage change sets to ignore were authored by a person called Ben, this test is about all the
	 * other change sets authored by Ben.
	 */
	@Test
	public void shouldNotIgnoreOtherChangeSetFromAuthorNamedBen() {
		ChangeSet changeSet = mock(ChangeSet.class);
		when(changeSet.getAuthor()).thenReturn("ben");
		when(changeSet.getId()).thenReturn("anything");
		
		ChangeLogDetective changeLogDetective = new ChangeLogDetective();
		boolean actual = changeLogDetective
		        .isVintageChangeSet("org/openmrs/liquibase/snapshots/core-data/liquibase-core-data-1.9.x.xml", changeSet);
		assertFalse(actual);
	}
	
	@Test
	public void shouldExcludeVintageChangeSets() {
		ChangeSet anyChangeSet = new ChangeSet("any id", "any author", false, false, null, null, null, null);
		ChangeSet changeSetToIgnore = new ChangeSet("disable-foreign-key-checks", "ben", false, false, null, null, null,
		        null);
		ChangeSet anotherChangeSetToIgnore = new ChangeSet("enable-foreign-key-checks", "ben", false, false, null, null,
		        null, null);
		List<ChangeSet> changeSets = Arrays.asList(anyChangeSet, changeSetToIgnore, anotherChangeSetToIgnore);
		
		ChangeLogDetective changeLogDetective = new ChangeLogDetective();
		List<ChangeSet> actual = changeLogDetective.excludeVintageChangeSets(
		    "org/openmrs/liquibase/snapshots/core-data/liquibase-core-data-1.9.x.xml", changeSets);
		List<ChangeSet> expected = Arrays.asList(anyChangeSet);
		
		assertEquals(expected, actual);
	}
	
	@Test
	public void shouldNotExcludeChangeSetsForFilenameOtherThanLiquibaseCoreData_1_9_x() {
		ChangeSet anyChangeSet = new ChangeSet("any id", "any author", false, false, null, null, null, null);
		ChangeSet changeSetToIgnore = new ChangeSet("disable-foreign-key-checks", "ben", false, false, null, null, null,
		        null);
		ChangeSet anotherChangeSetToIgnore = new ChangeSet("enable-foreign-key-checks", "ben", false, false, null, null,
		        null, null);
		List<ChangeSet> changeSets = Arrays.asList(anyChangeSet, changeSetToIgnore, anotherChangeSetToIgnore);
		
		ChangeLogDetective changeLogDetective = new ChangeLogDetective();
		List<ChangeSet> actual = changeLogDetective.excludeVintageChangeSets("any_filename", changeSets);
		List<ChangeSet> expected = changeSets;
		
		assertEquals(expected, actual);
	}
	
	@Test
	public void shouldLogSmallNumberOfUnrunChangeSets() {
		ChangeLogDetective changeLogDetective = new ChangeLogDetective();
		
		// log up to 9 change sets
		List<ChangeSet> changeSets = Arrays.asList(mock(ChangeSet.class), mock(ChangeSet.class), mock(ChangeSet.class),
		    mock(ChangeSet.class), mock(ChangeSet.class), mock(ChangeSet.class), mock(ChangeSet.class),
		    mock(ChangeSet.class), mock(ChangeSet.class));
		
		assertTrue(changeLogDetective.logUnRunChangeSetDetails("liquibase-core-data-1.9.x.xml", changeSets));
		assertTrue(changeLogDetective.logUnRunChangeSetDetails("liquibase-schema-only-1.9.x.xml", changeSets));
	}
	
	@Test
	public void shouldNotLogLargeNumberOfUnrunChangeSets() {
		ChangeLogDetective changeLogDetective = new ChangeLogDetective();
		
		// do not log 10 or more change sets
		List<ChangeSet> changeSets = Arrays.asList(mock(ChangeSet.class), mock(ChangeSet.class), mock(ChangeSet.class),
		    mock(ChangeSet.class), mock(ChangeSet.class), mock(ChangeSet.class), mock(ChangeSet.class),
		    mock(ChangeSet.class), mock(ChangeSet.class), mock(ChangeSet.class));
		
		assertFalse(changeLogDetective.logUnRunChangeSetDetails("liquibase-core-data-1.9.x.xml", changeSets));
		assertFalse(changeLogDetective.logUnRunChangeSetDetails("liquibase-schema-only-1.9.x.xml", changeSets));
	}
	
	@Test
	public void shouldNotLogUnrunChangeSetsFromOtherChangeLogFile() {
		ChangeLogDetective changeLogDetective = new ChangeLogDetective();
		
		List<ChangeSet> changeSets = Arrays.asList(mock(ChangeSet.class), mock(ChangeSet.class));
		
		assertFalse(changeLogDetective.logUnRunChangeSetDetails("liquibase-core-data-2.2.x.xml", changeSets));
		assertFalse(changeLogDetective.logUnRunChangeSetDetails("liquibase-schema-only-2.2.x.xml", changeSets));
		assertFalse(changeLogDetective.logUnRunChangeSetDetails("any_filename", changeSets));
	}
}
