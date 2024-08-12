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
import javax.servlet.ServletException;

import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.Daemon;
import org.openmrs.module.ModuleException;
import org.openmrs.util.DatabaseUpdateException;
import org.openmrs.util.InputRequiredException;

import java.util.concurrent.ExecutionException;

/**
 * This class provides {@link Daemon} functionality in a web context.
 * 
 * @since 1.9
 */
public class WebDaemon {
	
	/**
	 * Start openmrs in a new thread that is authenticated as the daemon user.
	 * 
	 * @param servletContext the servlet context.
	 */
	public static void startOpenmrs(final ServletContext servletContext) throws DatabaseUpdateException,
	        InputRequiredException {

		try {
			Daemon.runNewDaemonTask(() -> {
				try {
					Listener.startOpenmrs(servletContext);
				} catch (ServletException e) {
					throw new ModuleException("Unable to start OpenMRS. Error thrown was: " + e.getMessage(), e);
				}
			}).get();
		} catch (InterruptedException  ignored) {
		} catch (ExecutionException e) {
			if (e.getCause() instanceof RuntimeException) {
				throw (RuntimeException) e.getCause();
			} else if (e.getCause() instanceof InputRequiredException) {
				throw (InputRequiredException) e.getCause();
			} else if (e.getCause() instanceof DatabaseUpdateException) {
				throw (DatabaseUpdateException) e.getCause();
			} else {
				throw new APIException(e.getMessage(), e.getCause());
			}
		}
	}
}
