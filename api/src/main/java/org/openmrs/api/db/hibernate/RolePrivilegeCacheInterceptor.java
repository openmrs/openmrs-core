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

import org.hibernate.Interceptor;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.type.Type;
import org.openmrs.Privilege;
import org.openmrs.Role;
import org.openmrs.api.cache.RolePrivilegeCache;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Clears the {@link RolePrivilegeCache} whenever Hibernate flushes an insert, update or delete of a
 * {@link Role} or {@link Privilege}, or a change to a role's privileges or inherited roles. This
 * covers writes that bypass {@link org.openmrs.api.UserService}, such as code that changes a loaded
 * role and lets the flush save it. Changes to a role's collections are flushed as collection
 * updates rather than as updates of the role, so both are watched.
 * <p>
 * The cache is looked up lazily because it depends on the session factory this interceptor is
 * registered with.
 *
 * @since 2.8.10
 */
@Component
public class RolePrivilegeCacheInterceptor implements Interceptor {

	private final ObjectProvider<RolePrivilegeCache> rolePrivilegeCache;

	@Autowired
	public RolePrivilegeCacheInterceptor(ObjectProvider<RolePrivilegeCache> rolePrivilegeCache) {
		this.rolePrivilegeCache = rolePrivilegeCache;
	}

	@Override
	public boolean onSave(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types) {
		clearIfRoleOrPrivilege(entity);
		return false;
	}

	@Override
	public boolean onFlushDirty(Object entity, Object id, Object[] currentState, Object[] previousState,
	        String[] propertyNames, Type[] types) {
		clearIfRoleOrPrivilege(entity);
		return false;
	}

	@Override
	public void onDelete(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types) {
		clearIfRoleOrPrivilege(entity);
	}

	@Override
	public void onCollectionRecreate(Object collection, Object key) {
		clearIfRoleCollection(collection);
	}

	@Override
	public void onCollectionRemove(Object collection, Object key) {
		clearIfRoleCollection(collection);
	}

	@Override
	public void onCollectionUpdate(Object collection, Object key) {
		clearIfRoleCollection(collection);
	}

	private void clearIfRoleOrPrivilege(Object entity) {
		if (entity instanceof Role || entity instanceof Privilege) {
			clear();
		}
	}

	private void clearIfRoleCollection(Object collection) {
		if (collection instanceof PersistentCollection
		        && ((PersistentCollection<?>) collection).getOwner() instanceof Role) {
			clear();
		}
	}

	private void clear() {
		RolePrivilegeCache cache = rolePrivilegeCache.getIfAvailable();
		if (cache != null) {
			cache.clear();
		}
	}
}
