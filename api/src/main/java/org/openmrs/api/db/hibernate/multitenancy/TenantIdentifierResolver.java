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

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

//@Slf4j
@Component
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver {

	private static final Logger log = LoggerFactory.getLogger(TenantIdentifierResolver.class);

	@Override
    public String resolveCurrentTenantIdentifier() {
		log.info("Inside resolveCurrentTenantIdentifier");
        String tenant = TenantContext.getCurrentTenant();
		log.info("tenant:: {}", tenant);
		
        return tenant != null ? tenant : "public";
    }


    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
} 
