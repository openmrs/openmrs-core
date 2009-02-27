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
package org.openmrs.api.impl;

import org.openmrs.api.OpenmrsService;

/**
 * Default method implementations for the OpenmrsService It is recommended that all services extend
 * this class instead of simply implementing the OpenmrsService class. This will allow for some
 * buffer room so that inheriting classes don't have to immediately implement all methods in
 * OpenmrsService
 * 
 * @see OpenmrsService
 */
public abstract class BaseOpenmrsService implements OpenmrsService {
	
	/**
	 * @see org.openmrs.api.OpenmrsService#OnShutdown()
	 */
	public void onShutdown() {
	}
	
	/**
	 * @see org.openmrs.api.OpenmrsService#OnStartup()
	 */
	public void onStartup() {
	}
	
}
