/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util

import org.apache.velocity.app.event.MethodExceptionEventHandler
import org.slf4j.LoggerFactory

/**
 * Class to safely catch velocity exceptions
 */
class VelocityExceptionHandler : MethodExceptionEventHandler {
    
    /**
     * When a user-supplied method throws an exception, the MethodExceptionEventHandler is invoked
     * with the Class, method name and thrown Exception. The handler can either return a valid
     * Object to be used as the return value of the method call, or throw the passed-in or new
     * Exception, which will be wrapped and propagated to the user as a MethodInvocationException
     *
     * @see org.apache.velocity.app.event.MethodExceptionEventHandler.methodException
     */
    override fun methodException(claz: Class<*>, method: String, e: Exception): Any? {
        log.debug("Claz: ${claz.name} method: $method", e)
        
        // if formatting a date (and probably getting an "IllegalArgumentException")
        if ("format" == method) {
            return null
        }
        
        // keep the default behavior
        throw e
    }
    
    companion object {
        private val log = LoggerFactory.getLogger(VelocityExceptionHandler::class.java)
    }
}
