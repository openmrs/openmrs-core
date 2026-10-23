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
import org.hibernate.type.Type;
import org.openmrs.GlobalProperty;
import org.openmrs.api.cache.GlobalPropertyCache;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Evicts a global property from the {@link GlobalPropertyCache} whenever Hibernate flushes an
 * update or delete of it, and clears the cache whenever one is inserted, since an insert can change
 * what a lookup of another spelling the database treats as equal returns. This covers writes that
 * bypass {@link org.openmrs.api.AdministrationService}, such as code that changes a loaded
 * {@link GlobalProperty} and lets the flush save it.
 * <p>
 * The cache is looked up lazily because it depends on the session factory this interceptor is
 * registered with.
 *
 * @since 2.8.10
 */
@Component
public class GlobalPropertyCacheInterceptor implements Interceptor {

	private final ObjectProvider<GlobalPropertyCache> globalPropertyCache;

	@Autowired
	public GlobalPropertyCacheInterceptor(ObjectProvider<GlobalPropertyCache> globalPropertyCache) {
		this.globalPropertyCache = globalPropertyCache;
	}

	@Override
	public boolean onSave(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types) {
		clear(entity);
		return false;
	}

	@Override
	public boolean onFlushDirty(Object entity, Object id, Object[] currentState, Object[] previousState,
	        String[] propertyNames, Type[] types) {
		evict(entity);
		return false;
	}

	@Override
	public void onDelete(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types) {
		evict(entity);
	}

	private void clear(Object entity) {
		if (entity instanceof GlobalProperty) {
			GlobalPropertyCache cache = globalPropertyCache.getIfAvailable();
			if (cache != null) {
				cache.clear();
			}
		}
	}

	private void evict(Object entity) {
		if (entity instanceof GlobalProperty && ((GlobalProperty) entity).getProperty() != null) {
			GlobalPropertyCache cache = globalPropertyCache.getIfAvailable();
			if (cache != null) {
				cache.evict(((GlobalProperty) entity).getProperty());
			}
		}
	}
}
