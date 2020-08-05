/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.filter.update;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.liquibase.LiquibaseProvider;
import org.openmrs.util.DatabaseUpdater.OpenMRSChangeSet;
import org.openmrs.util.DatabaseUpdaterLiquibaseProvider;

/**
 * Tests {@link UpdateFilterModel}.
 */
public class UpdateFilterModelTest {
	
	private DatabaseUpdaterLiquibaseProvider liquibaseProvider;
	private DatabaseUpdaterWrapper databaseUpdaterWrapper;
	
	private UpdateFilterModel model;
	
	@BeforeEach
	public void setUp() {
		liquibaseProvider = new DatabaseUpdaterLiquibaseProvider();
		databaseUpdaterWrapper = mock( DatabaseUpdaterWrapper.class );
	}
	
	@Test
	public void createUpdateFilterModel_shouldrequireAnUpdateAndSetChangesToUnrunDatabaseChangesIfChangesAreNonEmpty()
	        throws Exception {
		List<OpenMRSChangeSet> changes = Arrays.asList(mock(OpenMRSChangeSet.class));
		
		when(databaseUpdaterWrapper.getUnrunDatabaseChanges(any(LiquibaseProvider.class))).thenReturn(changes);
		when(databaseUpdaterWrapper.isLocked()).thenReturn(false);
		
		model = new UpdateFilterModel(liquibaseProvider, databaseUpdaterWrapper);
		
		assertTrue(model.updateRequired, "should require an update");
		assertThat(model.changes, is(changes));
		
		verify( databaseUpdaterWrapper, times(1)).getUnrunDatabaseChanges( liquibaseProvider );
		verify( databaseUpdaterWrapper, never()).updatesRequired();
	}
	
	@Test
	public void createUpdateFilterModel_shouldRequiredAnUpdateIfChangesAreEmptyButDatabaseUpdaterDoesRequireAnUpdate()
	        throws Exception {
		List<OpenMRSChangeSet> changes = new ArrayList<>();
		
		when(databaseUpdaterWrapper.getUnrunDatabaseChanges(any(LiquibaseProvider.class))).thenReturn(changes);
		when(databaseUpdaterWrapper.isLocked()).thenReturn(false);
		when(databaseUpdaterWrapper.updatesRequired()).thenReturn(true);
		
		model = new UpdateFilterModel(liquibaseProvider, databaseUpdaterWrapper);
		
		assertTrue(model.updateRequired, "should require an update");
		assertThat(model.changes, is(empty()));

		verify( databaseUpdaterWrapper, times(1)).getUnrunDatabaseChanges( liquibaseProvider );
		verify( databaseUpdaterWrapper, times(1)).updatesRequired();
	}
	
	@Test
	public void createUpdateFilterModel_shouldNotRequireAnUpdateIfChangesAreEmptyAndDatabaseUpdaterDoesNotRequireAnUpdate()
	        throws Exception {
		List<OpenMRSChangeSet> changes = new ArrayList<>();
		
		when(databaseUpdaterWrapper.getUnrunDatabaseChanges(any(LiquibaseProvider.class))).thenReturn(changes);
		when(databaseUpdaterWrapper.isLocked()).thenReturn(false);
		when(databaseUpdaterWrapper.updatesRequired()).thenReturn(false);
		
		model = new UpdateFilterModel(liquibaseProvider, databaseUpdaterWrapper);
		
		assertFalse(model.updateRequired, "should not require an update");
		assertThat(model.changes, is(empty()));

		verify( databaseUpdaterWrapper, times(1)).getUnrunDatabaseChanges( liquibaseProvider );
		verify( databaseUpdaterWrapper, times(1)).updatesRequired();
	}
	
	@Test
	public void createUpdateFilterModel_shouldNotRequireAnUpdateIfChangesAreNullAndDatabaseUpdaterDoesNotRequireAnUpdate()
	        throws Exception {
		
		when(databaseUpdaterWrapper.getUnrunDatabaseChanges(any(LiquibaseProvider.class))).thenReturn(null);
		when(databaseUpdaterWrapper.isLocked()).thenReturn(false);
		when(databaseUpdaterWrapper.updatesRequired()).thenReturn(false);
		
		model = new UpdateFilterModel(liquibaseProvider, databaseUpdaterWrapper);
		
		assertFalse(model.updateRequired, "should not require an update");
		assertNull(model.changes, "should not have changes");

		verify( databaseUpdaterWrapper, times(1)).getUnrunDatabaseChanges( liquibaseProvider );
		verify( databaseUpdaterWrapper, times(1)).updatesRequired();
	}
	
	@Test
	public void createUpdateFilterModel_shouldNotRequireAnUpdateIfDatabaseUpdaterIsLockedAndCallingDatabaseUpdaterTwiceAlwaysReturnsNull()
	        throws Exception {
		
		when(databaseUpdaterWrapper.getUnrunDatabaseChanges(any(LiquibaseProvider.class))).thenReturn(null);
		when(databaseUpdaterWrapper.isLocked()).thenReturn(true);
		when(databaseUpdaterWrapper.updatesRequired()).thenReturn(false);
		
		model = new UpdateFilterModel(liquibaseProvider, databaseUpdaterWrapper);
		
		assertFalse(model.updateRequired, "should not require an update");
		assertNull(model.changes, "should not have changes");

		verify( databaseUpdaterWrapper, times(2)).getUnrunDatabaseChanges( liquibaseProvider );
		verify( databaseUpdaterWrapper, times(1)).updatesRequired();
	}
	
	@Test
	public void createUpdateFilterModel_shouldRequireAnUpdateIfDatabaseUpdaterIsLockedAndChangesAreNotNull()
	        throws Exception {
		List<OpenMRSChangeSet> changes = Arrays.asList(mock(OpenMRSChangeSet.class));
		
		when(databaseUpdaterWrapper.getUnrunDatabaseChanges(any(LiquibaseProvider.class))).thenReturn(changes);
		when(databaseUpdaterWrapper.isLocked()).thenReturn(true);
		
		model = new UpdateFilterModel(liquibaseProvider, databaseUpdaterWrapper);
		
		assertTrue(model.updateRequired, "should require an update");
		assertThat(model.changes, is(changes));

		verify( databaseUpdaterWrapper, times(1)).getUnrunDatabaseChanges( liquibaseProvider );
		verify( databaseUpdaterWrapper, never()).updatesRequired();
	}
}
