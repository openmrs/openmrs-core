/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.impl;

import org.openmrs.api.OpenmrsService;

/**
 * Default method implementations for the OpenmrsService.
 * <p>
 * It is recommended that all services extend this class instead of simply implementing the
 * OpenmrsService class. This will allow for some buffer room so that inheriting classes don't have
 * to immediately implement all methods in OpenmrsService
 * 
 * @see OpenmrsService
 */
public abstract class BaseOpenmrsService implements OpenmrsService {
	
	/**
	 * @see org.openmrs.api.OpenmrsService#onShutdown()
	 */
	public void onShutdown() {
	}
	
	/**
	 * @see org.openmrs.api.OpenmrsService#onStartup()
	 */
	public void onStartup() {
	}
	
}
