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

import org.openmrs.api.context.ServiceContext;
import org.openmrs.api.impl.BaseOpenmrsService;

/**
 * Base OpenMRS Service Interface All services registered to the {@link ServiceContext} are required
 * to implement this interface. It is recommended that all services extend the
 * {@link BaseOpenmrsService} abstract class to buffer themselves from changes to this interface.
 * 
 * @see BaseOpenmrsService
 */
public interface OpenmrsService {
	
	/**
	 * Called when the OpenMRS service layer is initializing. This occurs when a new module is
	 * loaded or during the initial server/api start
	 */
	public void onStartup();
	
	/**
	 * Called when the OpenMRS service layer is shutting down
	 */
	public void onShutdown();
	
}
