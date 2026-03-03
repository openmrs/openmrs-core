/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
