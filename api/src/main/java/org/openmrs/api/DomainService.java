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
 * Interface for Openmrs services supporting
 * fetching domain objects by UUID.
 */
public interface DomainService extends OpenmrsService {

    /**
     * Returns a domain object by UUID, or null if not found.
     * 
     * @param uuid the UUID string of the domain object
     * @return an instance of the requested type
     */
    public <T> T fetchByUuid(Class<T> type, String uuid);

    /**
     * Returns a list of all domain types that are currently registered and
     * resolvable by this service.
     *
     * @return a list of registered domain classes
     */
    public List<Class<?>> getDomainTypes();

}