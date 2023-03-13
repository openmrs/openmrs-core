/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util;

import org.openmrs.api.APIException;

/**
 * Helper class created only to call some protected methods on the SecurityManager class.
 *
 * @see SecurityManager
 */
public class OpenmrsSecurityManager extends SecurityManager {
	
	/**
	 * Returns the class on the current execution stack at the given depth. 0 is the most recently
	 * called class.
	 *
	 * @param callStackDepth
	 * @return the most recently called class.
	 * @throws APIException if given a callStackDepth less than zero
	 * @see SecurityManager#getClassContext()
	 * <strong>Should</strong> get the most recently called method
	 * <strong>Should</strong> throw an error if given a subzero call stack level
	 */
	public Class<?> getCallerClass(int callStackDepth) {
		if (callStackDepth < 0) {
			throw new APIException("call.stack.depth.error", (Object[]) null);
		}
		
		//SecurityManager may appear more than once in classContext
		int skipClasses = 1;
		Class<?>[] classContext = getClassContext();
		for (Class<?> clazz : classContext) {
			if (SecurityManager.class.isAssignableFrom(clazz)) {
				skipClasses++;
			} else {
				break;
			}
		}
		
		//Adjust the depth so that "0" is the not this "getCallerClass" method
		return getClassContext()[callStackDepth + skipClasses];
	}
	
}
