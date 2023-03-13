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
