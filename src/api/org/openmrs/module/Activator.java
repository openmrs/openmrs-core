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
package org.openmrs.module;

/**
 * This interface must be implemented by modules and referenced
 * by the Module-Activator property in the module manifest
 * @version 1.0
 */
public interface Activator {

	/**
	 * Called on module start/load
	 */
	public void startup();

	/**
	 * Called on module end
	 */
	public void shutdown();

}
