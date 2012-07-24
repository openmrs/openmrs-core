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
	 * @should get the most recently called method
	 * @should throw an error if given a subzero call stack level
	 */
	public Class<?> getCallerClass(int callStackDepth) {
		if (callStackDepth < 0)
			throw new APIException("Call stack depth cannot be less than 0");

		// adjust the depth so that "0" is the not this "getCallerClass" method
		return getClassContext()[callStackDepth + 2];
	}
	
}
