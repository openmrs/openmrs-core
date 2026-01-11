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

import org.slf4j.LoggerFactory
import java.util.Timer

/**
 * Utility functions to clean up causes of memory leakages.
 */
object MemoryLeakUtil {
    
    private val log = LoggerFactory.getLogger(MemoryLeakUtil::class.java)
    
    // http://bugs.mysql.com/bug.php?id=36565
    @JvmStatic
    fun shutdownMysqlCancellationTimer() {
        try {
            val myClassLoader = MemoryLeakUtil::class.java.classLoader
            val clazz = Class.forName("com.mysql.jdbc.ConnectionImpl", false, myClassLoader)
            
            if (clazz.classLoader != myClassLoader) {
                log.info("MySQL ConnectionImpl was loaded with another ClassLoader: (${clazz.classLoader}): cancelling anyway")
            } else {
                log.info("MySQL ConnectionImpl was loaded with the WebappClassLoader: cancelling the Timer")
            }
            
            val f = clazz.getDeclaredField("cancelTimer")
            f.isAccessible = true
            val timer = f.get(null) as Timer
            timer.cancel()
            log.info("completed timer cancellation")
        } catch (cnfe: ClassNotFoundException) {
            // Ignore
            log.error("Cannot cancel", cnfe)
        } catch (cnfe: NoSuchFieldException) {
            // Ignore
            log.error("Cannot cancel", cnfe)
        } catch (se: SecurityException) {
            log.info("Failed to shut-down MySQL Statement Cancellation Timer due to a SecurityException", se)
        } catch (iae: IllegalAccessException) {
            log.info("Failed to shut-down MySQL Statement Cancellation Timer due to an IllegalAccessException", iae)
        }
    }
}
