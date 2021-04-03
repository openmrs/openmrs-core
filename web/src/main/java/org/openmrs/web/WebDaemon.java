/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web;

import javax.servlet.ServletContext;

import org.openmrs.api.context.Context;
import org.openmrs.api.context.Daemon;
import org.openmrs.module.ModuleException;
import org.openmrs.util.DatabaseUpdateException;
import org.openmrs.util.InputRequiredException;

/**
 * This class provides {@link Daemon} functionality in a web context.
 * 
 * @since 1.9
 */
public class WebDaemon extends Daemon {
	
	/**
	 * Start openmrs in a new thread that is authenticated as the daemon user.
	 * 
	 * @param servletContext the servlet context.
	 */
	public static void startOpenmrs(final ServletContext servletContext) throws DatabaseUpdateException,
	        InputRequiredException {
		
		// create a new thread and start openmrs in it.
		DaemonThread startOpenmrsThread = new DaemonThread() {
			
			@Override
			public void run() {
				isDaemonThread.set(true);
				try {
					Listener.startOpenmrs(servletContext);
				}
				catch (Exception e) {
					exceptionThrown = e;
				}
				finally {
					Context.closeSession();
				}
			}
		};
		
		startOpenmrsThread.start();
		
		// wait for the "startOpenmrs" thread to finish
		try {
			startOpenmrsThread.join();
		}
		catch (InterruptedException e) {
			// ignore
		}
		
		if (startOpenmrsThread.getExceptionThrown() != null) {
			throw new ModuleException("Unable to start OpenMRS. Error thrown was: "
			        + startOpenmrsThread.getExceptionThrown().getMessage(), startOpenmrsThread.getExceptionThrown());
		}
	}
}
