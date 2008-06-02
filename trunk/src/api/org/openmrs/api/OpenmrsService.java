/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.api;

import org.openmrs.api.context.ServiceContext;
import org.openmrs.api.impl.BaseOpenmrsService;

/**
 * Base OpenMRS Service Interface
 * 
 * All services registered to the {@link ServiceContext} are 
 * required to implement this interface.  It is recommended
 * that all services extend the {@link BaseOpenmrsService} abstract
 * class to buffer themselves from changes to this interface.
 *
 * @see BaseOpenmrsService
 */
public interface OpenmrsService {

	/**
	 * Called when the OpenMRS service layer is initializing.
	 * 
	 * This occurs when a new module is loaded or during
	 * the initial server/api start
	 */
	public void onStartup();
	
	/**
	 * Called when the OpenMRS service layer is shutting down
	 */
	public void onShutdown();
	
}
