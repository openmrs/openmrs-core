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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.app.event.MethodExceptionEventHandler;

/**
 * Class to safely catch velocity exceptions
 */
public class VelocityExceptionHandler implements MethodExceptionEventHandler {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	/**
	 * When a user-supplied method throws an exception, the MethodExceptionEventHandler is invoked
	 * with the Class, method name and thrown Exception. The handler can either return a valid
	 * Object to be used as the return value of the method call, or throw the passed-in or new
	 * Exception, which will be wrapped and propagated to the user as a MethodInvocationException
	 * 
	 * @see org.apache.velocity.app.event.MethodExceptionEventHandler#methodException(java.lang.Class,
	 *      java.lang.String, java.lang.Exception)
	 */
	@SuppressWarnings("unchecked")
	public Object methodException(Class claz, String method, Exception e) throws Exception {
		
		log.debug("Claz: " + claz.getName() + " method: " + method, e);
		
		// if formatting a date (and probably getting an "IllegalArgumentException")
		if ("format".equals(method))
			return null;
		
		// keep the default behavior
		throw e;
	}
	
}
