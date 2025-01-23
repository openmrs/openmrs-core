/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db.hibernate.multitenancy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TenantContext {

	private static final Logger log = LoggerFactory.getLogger(TenantContext.class);


	private static ThreadLocal<String> currentTenant = new ThreadLocal<>();

    public static void setCurrentTenant(String tenant) {
		log.info("Inside setCurrentTenant:: tenantId::: {}", tenant);
		currentTenant.set(tenant);
    }

    public static String getCurrentTenant() {
        return currentTenant.get();
    }

    public static void clear() {
		log.info("Inside clear method");
        currentTenant.remove();
    }
}
