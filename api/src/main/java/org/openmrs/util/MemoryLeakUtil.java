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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sun.net.www.http.KeepAliveCache;

/**
 * Utility functions to clean up causes of memory leakages.
 */
public class MemoryLeakUtil {
	
	private final static Log log = LogFactory.getLog(MemoryLeakUtil.class);
	
	//http://bugs.mysql.com/bug.php?id=36565
	public static void shutdownMysqlCancellationTimer() {
		try {
			ClassLoader myClassLoader = MemoryLeakUtil.class.getClassLoader();
			Class<?> clazz = Class.forName("com.mysql.jdbc.ConnectionImpl", false, myClassLoader);
			
			if (!(clazz.getClassLoader() == myClassLoader)) {
				log.info("MySQL ConnectionImpl was loaded with another ClassLoader: (" + clazz.getClassLoader()
				        + "): cancelling anyway");
			} else {
				log.info("MySQL ConnectionImpl was loaded with the WebappClassLoader: cancelling the Timer");
			}
			
			Field f = clazz.getDeclaredField("cancelTimer");
			f.setAccessible(true);
			Timer timer = (Timer) f.get(null);
			timer.cancel();
			log.info("completed timer cancellation");
		}
		catch (ClassNotFoundException cnfe) {
			// Ignore
			log.error("Cannot cancel", cnfe);
		}
		catch (NoSuchFieldException nsfe) {
			// Ignore
			log.error("Cannot cancel", nsfe);
		}
		catch (SecurityException se) {
			log.info("Failed to shut-down MySQL Statement Cancellation Timer due to a SecurityException", se);
		}
		catch (IllegalAccessException iae) {
			log.info("Failed to shut-down MySQL Statement Cancellation Timer due to an IllegalAccessException", iae);
		}
	}
	
	public static void shutdownKeepAliveTimer() {
		try {
			final Field kac = HttpClient.class.getDeclaredField("kac");
			
			kac.setAccessible(true);
			final Field keepAliveTimer = KeepAliveCache.class.getDeclaredField("keepAliveTimer");
			
			keepAliveTimer.setAccessible(true);
			
			final Thread thread = (Thread) keepAliveTimer.get(kac.get(null));
			
			if (thread.getContextClassLoader() == OpenmrsClassLoader.getInstance()) {
				//Set to system class loader such that we can be garbage collected.
				thread.setContextClassLoader(ClassLoader.getSystemClassLoader());
			}
		}
		catch (final Exception e) {
			log.error(e.getMessage(), e);
		}
	}
}
