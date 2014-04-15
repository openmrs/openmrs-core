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
