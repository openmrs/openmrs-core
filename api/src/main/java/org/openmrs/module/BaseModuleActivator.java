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
 * Must be extended by modules and referenced by the Module-Activator property in the module
 * manifest, contains methods that let modules get notifications as the application is executing to
 * allow modules to react in custom ways.
 * 
 * @since 1.7
 */
public abstract class BaseModuleActivator implements ModuleActivator {
	
	/**
	 * @see org.openmrs.module.ModuleActivator#contextRefreshed()
	 */
	@Override
	public void contextRefreshed() {
	}
	
	/**
	 * @see org.openmrs.module.ModuleActivator#started()
	 */
	@Override
	public void started() {
	}
	
	/**
	 * @see org.openmrs.module.ModuleActivator#stopped()
	 */
	@Override
	public void stopped() {
		
	}
	
	/**
	 * @see org.openmrs.module.ModuleActivator#willRefreshContext()
	 */
	@Override
	public void willRefreshContext() {
	}
	
	/**
	 * @see org.openmrs.module.ModuleActivator#willStart()
	 */
	@Override
	public void willStart() {
	}
	
	/**
	 * @see org.openmrs.module.ModuleActivator#willStop()
	 */
	@Override
	public void willStop() {
	}
	
}
