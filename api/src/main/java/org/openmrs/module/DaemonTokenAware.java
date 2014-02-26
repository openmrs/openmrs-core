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
 * Allows to receive the daemon token to execute code as the daemon user.
 * <p>
 * Currently it can be implemented only by {@link ModuleActivator}. The daemon token is injected by
 * {@link ModuleFactory} before {@link ModuleActivator#contextRefreshed()} or {@link ModuleActivator#started()}.
 * 
 * @since 1.9.2
 */
public interface DaemonTokenAware {
	
	/**
	 * Allows to receive the daemon token.
	 * <p>
	 * It may be called by multiple threads. The last passed token is valid, whereas
	 * previously passed tokens may be invalidated.
	 * 
	 * @param token
	 */
	void setDaemonToken(DaemonToken token);
	
}
