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
	 * this point the module's service methods aren't yet available so they can't be called. <br/>
	 * <br/>
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
