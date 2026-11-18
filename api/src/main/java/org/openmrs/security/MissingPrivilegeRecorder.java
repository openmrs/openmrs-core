/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.security;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Collects what one method-security expression found missing while it is evaluated - privilege
 * names, and authentication when {@code isAuthenticated()} denied - so
 * {@link PrivilegeNamingAuthorizationManager} can name it if the expression denies. The state lives
 * here because two classes record into it: {@link OpenmrsPermissionEvaluator} behind
 * {@code hasPermission(...)} and {@link OpenmrsAuthorizationManagerFactory} behind
 * {@code hasAuthority(...)} and {@code isAuthenticated()}.
 * <p>
 * Outside an open scope nothing is recorded - the {@code @PreFilter}/{@code @PostFilter} case,
 * where the expression runs per element and no denial message is produced to drain them.
 */
final class MissingPrivilegeRecorder {

	private static final ThreadLocal<Recording> recording = new ThreadLocal<>();

	private MissingPrivilegeRecorder() {
	}

	/**
	 * Notes that <code>privilege</code> was checked and not held. A no-op when no scope is open.
	 *
	 * @param privilege the privilege name that was checked
	 */
	static void record(String privilege) {
		Recording current = recording.get();
		if (current != null) {
			current.missingPrivileges.add(privilege);
		}
	}

	/**
	 * Notes that {@code isAuthenticated()} was checked and denied. A no-op when no scope is open.
	 */
	static void recordMissingAuthentication() {
		Recording current = recording.get();
		if (current != null) {
			current.authenticationMissing = true;
		}
	}

	/**
	 * Opens a scope on the current thread.
	 *
	 * @return whatever scope was open before this call, to be handed back to {@link #end(Recording)} so
	 *         that a nested scope restores its enclosing one instead of discarding it
	 */
	static Recording begin() {
		Recording enclosing = recording.get();
		recording.set(new Recording());
		return enclosing;
	}

	/**
	 * Closes the innermost scope, making <code>enclosing</code> the active one again.
	 *
	 * @param enclosing the value {@link #begin()} returned
	 * @return what was recorded within the scope just closed; never <code>null</code>
	 */
	static Recording end(Recording enclosing) {
		Recording recorded = recording.get();
		if (enclosing == null) {
			recording.remove();
		} else {
			recording.set(enclosing);
		}

		return recorded != null ? recorded : new Recording();
	}

	/**
	 * What one scope recorded.
	 */
	static final class Recording {

		private final Set<String> missingPrivileges = new LinkedHashSet<>();

		private boolean authenticationMissing;

		/**
		 * @return the privilege names found missing, in the order they were checked
		 */
		Set<String> getMissingPrivileges() {
			return Collections.unmodifiableSet(missingPrivileges);
		}

		/**
		 * @return true if {@code isAuthenticated()} denied
		 */
		boolean isAuthenticationMissing() {
			return authenticationMissing;
		}
	}
}
