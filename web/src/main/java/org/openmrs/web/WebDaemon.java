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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;

import org.openmrs.api.APIException;
import org.openmrs.api.context.Daemon;
import org.openmrs.module.ModuleException;
import org.openmrs.util.DatabaseUpdateException;
import org.openmrs.util.InputRequiredException;

/**
 * This class provides {@link Daemon} functionality in a web context.
 *
 * @since 1.9
 */
public final class WebDaemon {

	/**
	 * The capability that proves to {@link Daemon} that this class is allowed to act with daemon
	 * permissions.
	 */
	private static volatile Daemon.CallerKey daemonCallerKey;

	private WebDaemon() {
	};

	/**
	 * Start openmrs in a new thread that is authenticated as the daemon user.
	 *
	 * @param servletContext the servlet context.
	 */
	public static void startOpenmrs(final ServletContext servletContext)
	        throws DatabaseUpdateException, InputRequiredException {

		Daemon.CallerKey callerKey = daemonCallerKey();
		if (callerKey == null) {
			// Daemon distributes the key to WebDaemon reflectively during its initialization. If we get
			// here without one, that hand-off failed; surface the real cause rather than letting the
			// authorization check below fail with a misleading "unauthorized caller" message.
			throw new APIException("Unable to start OpenMRS: WebDaemon was not granted a Daemon caller key. "
			        + "Check the logs for an earlier error about providing the DaemonCallerKey to WebDaemon.");
		}

		// Startup runs on the servlet container's thread, which is not a daemon thread, so use the
		// CallerKey-authorized overload to launch the work on a daemon thread.
		Future<?> startup = Daemon.runNewDaemonTask(() -> {
			try {
				Listener.startOpenmrs(servletContext);
			} catch (ServletException e) {
				throw new ModuleException("Unable to start OpenMRS. Error thrown was: " + e.getMessage(), e);
			}
		}, callerKey);

		try {
			startup.get();
		} catch (InterruptedException ignored) {} catch (ExecutionException e) {
			Throwable cause = e.getCause();
			if (cause instanceof DatabaseUpdateException) {
				throw (DatabaseUpdateException) cause;
			} else if (cause instanceof InputRequiredException) {
				throw (InputRequiredException) cause;
			} else if (!(cause instanceof ModuleException)) {
				throw new ModuleException("Unable to start OpenMRS. Error thrown was: " + cause.getMessage(), cause);
			} else {
				throw (ModuleException) cause;
			}
		}
	}

	/**
	 * Receives the {@link Daemon} caller key. Called only by {@link Daemon} during its initialization.
	 *
	 * @param callerKey the caller key issued by {@link Daemon}
	 * @since 3.0.0, 2.9.0, 2.8.9
	 */
	public static void setDaemonCallerKey(Daemon.CallerKey callerKey) {
		if (callerKey != null && daemonCallerKey == null) {
			daemonCallerKey = callerKey;
		}
	}

	private static Daemon.CallerKey daemonCallerKey() {
		if (daemonCallerKey == null) {
			// Guarantee Daemon has initialized and therefore handed us the key, regardless of the order in
			// which the two classes were first loaded.
			Daemon.ensureInitialized();
		}
		return daemonCallerKey;
	}
}
