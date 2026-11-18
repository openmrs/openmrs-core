/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db.hibernate;

import org.hibernate.collection.spi.PersistentCollection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Location;
import org.openmrs.Privilege;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.cache.RolePrivilegeCache;
import org.springframework.beans.factory.ObjectProvider;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RolePrivilegeCacheInterceptorTest {

	private RolePrivilegeCache cache;

	private RolePrivilegeCacheInterceptor interceptor;

	@BeforeEach
	@SuppressWarnings("unchecked")
	public void setUp() {
		cache = mock(RolePrivilegeCache.class);
		ObjectProvider<RolePrivilegeCache> provider = mock(ObjectProvider.class);
		when(provider.getIfAvailable()).thenReturn(cache);
		interceptor = new RolePrivilegeCacheInterceptor(provider);
	}

	@Test
	public void shouldClearTheCacheWhenARoleOrPrivilegeIsSavedUpdatedOrDeleted() {
		interceptor.onSave(new Role("Clerk"), "Clerk", null, null, null);
		interceptor.onFlushDirty(new Role("Clerk"), "Clerk", null, null, null, null);
		interceptor.onDelete(new Privilege("View Patients"), "View Patients", null, null, null);

		verify(cache, times(3)).clear();
	}

	@Test
	public void shouldClearTheCacheWhenARolesCollectionChanges() {
		PersistentCollection<?> privileges = collectionOwnedBy(new Role("Clerk"));

		interceptor.onCollectionUpdate(privileges, "Clerk");
		interceptor.onCollectionRecreate(privileges, "Clerk");
		interceptor.onCollectionRemove(privileges, "Clerk");

		verify(cache, times(3)).clear();
	}

	@Test
	public void shouldNotClearTheCacheForOtherEntitiesOrCollections() {
		interceptor.onSave(new Location(), 1, null, null, null);
		interceptor.onFlushDirty(new Location(), 1, null, null, null, null);
		interceptor.onDelete(new Location(), 1, null, null, null);
		// a user's roles belong to the user; changing them does not change what a role grants
		interceptor.onCollectionUpdate(collectionOwnedBy(new User()), 1);

		verify(cache, never()).clear();
	}

	private static PersistentCollection<?> collectionOwnedBy(Object owner) {
		PersistentCollection<?> collection = mock(PersistentCollection.class);
		when(collection.getOwner()).thenReturn(owner);
		return collection;
	}
}
