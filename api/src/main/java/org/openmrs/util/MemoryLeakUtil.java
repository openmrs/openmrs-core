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

import java.lang.reflect.Field;
import java.util.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility functions to clean up causes of memory leakages.
 */
public class MemoryLeakUtil {
	
	private MemoryLeakUtil() {
	}
	
	private static final Logger LOG = LoggerFactory.getLogger(MemoryLeakUtil.class);
	
	//http://bugs.mysql.com/bug.php?id=36565
	public static void shutdownMysqlCancellationTimer() {
		try {
			ClassLoader myClassLoader = MemoryLeakUtil.class.getClassLoader();
			Class<?> clazz = Class.forName("com.mysql.jdbc.ConnectionImpl", false, myClassLoader);
			
			if (!(clazz.getClassLoader() == myClassLoader)) {
				LOG.info("MySQL ConnectionImpl was loaded with another ClassLoader: (" + clazz.getClassLoader()
				        + "): cancelling anyway");
			} else {
				LOG.info("MySQL ConnectionImpl was loaded with the WebappClassLoader: cancelling the Timer");
			}
			
			Field f = clazz.getDeclaredField("cancelTimer");
			f.setAccessible(true);
			Timer timer = (Timer) f.get(null);
			timer.cancel();
			LOG.info("completed timer cancellation");
		}
		catch (ClassNotFoundException | NoSuchFieldException cnfe) {
			// Ignore
			LOG.error("Cannot cancel", cnfe);
		}
		catch (SecurityException se) {
			LOG.info("Failed to shut-down MySQL Statement Cancellation Timer due to a SecurityException", se);
		}
		catch (IllegalAccessException iae) {
			LOG.info("Failed to shut-down MySQL Statement Cancellation Timer due to an IllegalAccessException", iae);
		}
	}
}
