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
 * Contains methods that get called at different stages as the application is executing, you should
 * extend {@link BaseModuleActivator} instead of directly implementing this interface for forward
 * compatibility of subclasses.
 * 
 * @see BaseModuleActivator
 * @since 1.7
 */
public interface ModuleActivator {
	
	/**
	 * Called just before spring's application context is refreshed, this method is called multiple
	 * times i.e. whenever a new module gets started, at application startup or a developer chooses
	 * to refresh the context.
	 */
	public void willRefreshContext();
	
	/**
	 * Called after spring's application context is refreshed , this method is called multiple times
	 * i.e. whenever a new module gets started and at application startup.
	 */
	public void contextRefreshed();
	
	/**
	 * Called after a module has been loaded but before the application context is refreshed, at
	 * this point the module's service methods aren't yet available so they can't be called. <br>
	 * <br>
	 * This method will be authenticated as the Daemon user and have all privileges.
	 */
	public void willStart();
	
	/**
	 * Called after a module is started, the application context has been refreshed and the module's
	 * service methods are ready to be called.
	 */
	public void started();
	
	/**
	 * Called just before a module is stopped
	 */
	public void willStop();
	
	/**
	 * Called after a module is stopped
	 */
	public void stopped();
	
}
