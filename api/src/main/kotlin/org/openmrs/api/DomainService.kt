/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api

/**
 * Interface for Openmrs services supporting
 * fetching domain objects by UUID.
 *
 * @since 3.0.0
 */
interface DomainService : OpenmrsService {

    /**
     * Returns a domain object by UUID, or null if not found.
     *
     * @param type the class type to retrieve
     * @param uuid the UUID string of the domain object
     * @return an instance of the requested type
     */
    fun <T> fetchByUuid(type: Class<T>, uuid: String): T?

    /**
     * Returns a list of all domain types that are currently registered and
     * resolvable by this service.
     *
     * @return a list of registered domain classes
     */
    fun getDomainTypes(): List<Class<*>>
}
