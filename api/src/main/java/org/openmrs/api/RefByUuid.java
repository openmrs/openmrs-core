/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api;

import java.util.List;

/**
 * Contract for services that can resolve domain objects by their UUID
 * in a generic way.
 *
 * <p>This interface is intended to standardize the mechanism of
 * retrieving references to domain objects without requiring clients
 * to know which specific service method to call. By implementing this,
 * each service can expose the subset of domain types it is able to
 * resolve, along with a generic lookup method.</p>
 *
 * <p>Usage examples:</p>
 * <pre>
 *     RefByUuid service = ...;
 *
 *     // Fetch a patient by UUID
 *     Patient p = service.getRefByUuid(Patient.class, "some-uuid");
 *
 *     // Get all types this service can resolve
 *     List<?> types = service.getRefTypes();
 * </pre>
 */
public interface RefByUuid {

    /**
     * Resolve a domain object managed by this service by its UUID.
     *
     * @param type the class type of the domain object to fetch (e.g. {@code Patient.class})
     * @param uuid the UUID of the object to fetch
     * @param <T>  the domain object type
     * @return the resolved domain object instance, or {@code null} if no matching object is found
     */
    <T> T getRefByUuid(Class<T> type, String uuid);

    /**
     * Get the list of domain types that this service can resolve.
     *
     * @return a list of {@link Class} objects representing supported domain types
     */
    List<Class<?>> getRefTypes();
}
