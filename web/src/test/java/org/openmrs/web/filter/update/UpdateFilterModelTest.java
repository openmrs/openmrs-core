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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.util.DatabaseUpdater;
import org.openmrs.util.DatabaseUpdater.OpenMRSChangeSet;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Tests {@link UpdateFilterModel}.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(DatabaseUpdater.class)
public class UpdateFilterModelTest {
	
	private UpdateFilterModel model;
	
	@Before
	public void setUp() {
		PowerMockito.mockStatic(DatabaseUpdater.class);
	}
	
	@Test
	public void createUpdateFilterModel_shouldrequireAnUpdateAndSetChangesToUnrunDatabaseChangesIfChangesAreNonEmpty()
	        throws Exception {
		
		OpenMRSChangeSet change = mock(OpenMRSChangeSet.class);
		List<OpenMRSChangeSet> changes = new ArrayList<>();
		changes.add(change);
		when(DatabaseUpdater.getUnrunDatabaseChanges()).thenReturn(changes);
		when(DatabaseUpdater.isLocked()).thenReturn(false);
		
		model = new UpdateFilterModel();
		
		assertTrue("should require an update", model.updateRequired);
		assertThat(model.changes, is(changes));
		PowerMockito.verifyStatic();
		DatabaseUpdater.getUnrunDatabaseChanges();
		PowerMockito.verifyStatic(never());
		DatabaseUpdater.updatesRequired();
	}
	
	@Test
	public void createUpdateFilterModel_shouldRequiredAnUpdateIfChangesAreEmptyButDatabaseUpdaterDoesRequireAnUpdate()
	        throws Exception {
		
		when(DatabaseUpdater.getUnrunDatabaseChanges()).thenReturn(new ArrayList<>());
		when(DatabaseUpdater.isLocked()).thenReturn(false);
		when(DatabaseUpdater.updatesRequired()).thenReturn(true);
		
		model = new UpdateFilterModel();
		
		assertTrue("should require an update", model.updateRequired);
		assertThat(model.changes, is(empty()));
		PowerMockito.verifyStatic();
		DatabaseUpdater.getUnrunDatabaseChanges();
		PowerMockito.verifyStatic();
		DatabaseUpdater.updatesRequired();
	}
	
	@Test
	public void createUpdateFilterModel_shouldNotRequireAnUpdateIfChangesAreEmptyAndDatabaseUpdaterDoesNotRequireAnUpdate()
	        throws Exception {
		
		when(DatabaseUpdater.getUnrunDatabaseChanges()).thenReturn(new ArrayList<>());
		when(DatabaseUpdater.isLocked()).thenReturn(false);
		when(DatabaseUpdater.updatesRequired()).thenReturn(false);
		
		model = new UpdateFilterModel();
		
		assertFalse("should not require an update", model.updateRequired);
		assertThat(model.changes, is(empty()));
		PowerMockito.verifyStatic();
		DatabaseUpdater.getUnrunDatabaseChanges();
		PowerMockito.verifyStatic();
		DatabaseUpdater.updatesRequired();
	}
	
	@Test
	public void createUpdateFilterModel_shouldNotRequireAnUpdateIfChangesAreNullAndDatabaseUpdaterDoesNotRequireAnUpdate()
	        throws Exception {
		
		when(DatabaseUpdater.getUnrunDatabaseChanges()).thenReturn(null);
		when(DatabaseUpdater.isLocked()).thenReturn(false);
		when(DatabaseUpdater.updatesRequired()).thenReturn(false);
		
		model = new UpdateFilterModel();
		
		assertFalse("should not require an update", model.updateRequired);
		assertNull("should not have changes", model.changes);
		PowerMockito.verifyStatic();
		DatabaseUpdater.getUnrunDatabaseChanges();
		PowerMockito.verifyStatic();
		DatabaseUpdater.updatesRequired();
	}
	
	@Test
	public void createUpdateFilterModel_shouldNotRequireAnUpdateIfDatabaseUpdaterIsLockedAndCallingDatabaseUpdaterTwiceAlwaysReturnsNull()
	        throws Exception {
		
		when(DatabaseUpdater.getUnrunDatabaseChanges()).thenReturn(null);
		when(DatabaseUpdater.isLocked()).thenReturn(true);
		when(DatabaseUpdater.updatesRequired()).thenReturn(false);
		
		model = new UpdateFilterModel();
		
		assertFalse("should not require an update", model.updateRequired);
		assertNull("should not have changes", model.changes);
		PowerMockito.verifyStatic(times(2));
		DatabaseUpdater.getUnrunDatabaseChanges();
		PowerMockito.verifyStatic();
		DatabaseUpdater.updatesRequired();
	}
	
	@Test
	public void createUpdateFilterModel_shouldRequireAnUpdateIfDatabaseUpdaterIsLockedAndChangesAreNotNull()
	        throws Exception {
		
		OpenMRSChangeSet change = mock(OpenMRSChangeSet.class);
		List<OpenMRSChangeSet> changes = new ArrayList<>();
		changes.add(change);
		when(DatabaseUpdater.getUnrunDatabaseChanges()).thenReturn(null, changes);
		when(DatabaseUpdater.isLocked()).thenReturn(true);
		
		model = new UpdateFilterModel();
		
		assertTrue("should require an update", model.updateRequired);
		assertThat(model.changes, is(changes));
		PowerMockito.verifyStatic(times(2));
		DatabaseUpdater.getUnrunDatabaseChanges();
		PowerMockito.verifyStatic(never());
		DatabaseUpdater.updatesRequired();
	}
}
