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

import org.openmrs.api.context.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * Clears the cached listener registrations whenever the application context is refreshed, ensuring
 * listeners contributed by the refreshed context are not served from a stale cache.
 *
 * @since 2.8.10
 */
@Component
public class RegisteredListenerCache implements ApplicationListener<ContextRefreshedEvent> {

	private static final Logger log = LoggerFactory.getLogger(RegisteredListenerCache.class);

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		log.debug("Clearing registered listener cache after context refresh");
		UserContext.clearCachedListeners();
	}
}
