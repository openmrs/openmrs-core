/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.cache;

import java.io.Serializable;

import org.openmrs.GlobalProperty;

/**
 * An immutable snapshot of the parts of a {@link GlobalProperty} that
 * {@link org.openmrs.api.AdministrationService#getGlobalProperty(String)} needs, suitable for
 * holding in the API cache.
 * <p>
 * A {@link GlobalProperty} entity is deliberately <b>not</b> cached directly. It is a live
 * Hibernate entity whose privilege associations are lazily fetched, so reading them from a detached
 * instance outside of a session would fail. Callers also mutate the entity returned by
 * {@link org.openmrs.api.AdministrationService#getGlobalPropertyObject(String)} before saving it,
 * which a shared cache must never hand out.
 * <p>
 * The view privilege is flattened to its name so that no entity reference survives into the cache,
 * and so that the privilege check can be re-run on every read rather than being skipped on a cache
 * hit.
 *
 * @see GlobalPropertyCache
 * @since 3.0.0
 */
public final class CachedGlobalProperty implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Marks a property that does not exist. Caching this, rather than a plain {@code null}, is what
	 * makes repeated lookups of unset properties free: Hibernate can only cache rows that exist, so
	 * without a negative entry every such lookup is a guaranteed database round trip.
	 */
	public static final CachedGlobalProperty ABSENT = new CachedGlobalProperty(false, null, null);

	private final boolean present;

	private final String value;

	private final String viewPrivilege;

	private CachedGlobalProperty(boolean present, String value, String viewPrivilege) {
		this.present = present;
		this.value = value;
		this.viewPrivilege = viewPrivilege;
	}

	/**
	 * Takes a snapshot of the given global property.
	 *
	 * @param globalProperty the property to snapshot, not null
	 * @return a cacheable snapshot of <code>globalProperty</code>
	 */
	public static CachedGlobalProperty of(GlobalProperty globalProperty) {
		String privilege = globalProperty.getViewPrivilege() == null ? null
		        : globalProperty.getViewPrivilege().getPrivilege();

		return new CachedGlobalProperty(true, globalProperty.getPropertyValue(), privilege);
	}

	/**
	 * @return true if a global property with this name existed when the snapshot was taken
	 */
	public boolean isPresent() {
		return present;
	}

	/**
	 * @return the property value, or null if the property does not exist
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @return the name of the privilege required to view this property, or null if it is unrestricted
	 */
	public String getViewPrivilege() {
		return viewPrivilege;
	}

	@Override
	public String toString() {
		return present ? "CachedGlobalProperty[value=" + value + "]" : "CachedGlobalProperty[ABSENT]";
	}
}
