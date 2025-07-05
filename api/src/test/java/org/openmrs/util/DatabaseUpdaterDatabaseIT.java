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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.liquibase.ChangeLogDetective;
import org.openmrs.liquibase.ChangeLogVersionFinder;
import org.powermock.reflect.Whitebox;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DatabaseUpdaterDatabaseIT extends DatabaseIT {
	
	private static final String VERSION_2_1_X = "2.1.x";
	
	/*
	 * This is the number of change sets defined by the Liquibase snapshot files 2.1.x and all Liquibase update
	 * files with versions greater than 2.1.x.
	 * 
	 * This constant needs to be updated when adding new Liquibase update files to openmrs-core.
	 */
	
	private static final int CHANGE_SET_COUNT_FOR_GREATER_THAN_2_1_X = 899;

	private static final int CHANGE_SET_COUNT_FOR_2_1_X = 870;

	@BeforeEach
	public void setup() throws ClassNotFoundException {
		DatabaseUpdater.setLiquibaseProvider(this);
	}
	
	@AfterEach
	public void tearDown() {
		DatabaseUpdater.unsetLiquibaseProvider();
	}
	
	@Test
	public void should() throws Exception {
		
		Whitebox.setInternalState(ChangeLogDetective.getInstance(), "initialSnapshotVersion", (Object)null);
		Whitebox.setInternalState(ChangeLogDetective.getInstance(), "unrunLiquibaseUpdates", (Object)null);
		
		ChangeLogVersionFinder changeLogVersionFinder = new ChangeLogVersionFinder();
		Map<String, List<String>> snapshotCombinations = changeLogVersionFinder.getSnapshotCombinations();
		updateDatabase(snapshotCombinations.get(VERSION_2_1_X));
		
		List<DatabaseUpdater.OpenMRSChangeSet> actual = DatabaseUpdater.getDatabaseChanges();
		
		assertEquals(CHANGE_SET_COUNT_FOR_GREATER_THAN_2_1_X, actual.size());
		
	}
}
