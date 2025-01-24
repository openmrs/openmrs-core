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

		// Enhanced logging to track tenant resolution
//		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
//
//		log.info("=== Resolving Current Tenant ===");
//		log.info("Thread ID: {}", Thread.currentThread().getId());
//		log.info("Thread Name: {}", Thread.currentThread().getName());
//
//		// Get the current tenant from TenantContext
//		String tenant = TenantContext.getCurrentTenant();
//
//		log.info("Tenant from TenantContext: {}", tenant);
//
//		// Log call stack to understand where this is being called from
//		log.info("Caller Method Trace:");
//		for (int i = 1; i < Math.min(stackTrace.length, 5); i++) {
//			log.info("  {}.{}", stackTrace[i].getClassName(), stackTrace[i].getMethodName());
//		}
//
//		// Fallback to "public" if no tenant is set
//		String resolvedTenant = tenant != null ? tenant : "public";
//
//		log.info("Resolved Tenant: {}", resolvedTenant);
//
//		return resolvedTenant;
		
        String tenant = TenantContext.getCurrentTenant();
		log.info("tenant:: {}", tenant);
		
        return tenant != null ? tenant : "public";
    }


    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
} 
