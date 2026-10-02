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
 * Collects the privilege names checked and found missing while one method-security expression is
 * being evaluated, so that {@link PrivilegeNamingAuthorizationManager} can name them if the
 * expression as a whole denies.
 * <p>
 * Two things record into it - {@link OpenmrsPermissionEvaluator}, behind
 * {@code hasPermission(null, '&lt;privilege&gt;')}, and {@link OpenmrsAuthorizationManagerFactory},
 * behind {@code hasAuthority(...)} - which is why the state lives here rather than in either of
 * them.
 * <p>
 * Outside an open scope nothing is recorded at all. That is the
 * {@code @PreFilter}/{@code @PostFilter} case: those interceptors evaluate the expression once per
 * element and never produce a denial message, so nothing would ever drain the names.
 */
final class MissingPrivilegeRecorder {

	private static final ThreadLocal<Set<String>> missingPrivileges = new ThreadLocal<>();

	private MissingPrivilegeRecorder() {
	}

	/**
	 * Notes that <code>privilege</code> was checked and not held. A no-op when no scope is open.
	 *
	 * @param privilege the privilege name that was checked
	 */
	static void record(String privilege) {
		Set<String> recording = missingPrivileges.get();
		if (recording != null) {
			recording.add(privilege);
		}
	}

	/**
	 * Opens a scope on the current thread.
	 *
	 * @return whatever scope was open before this call, to be handed back to {@link #end(Set)} so that
	 *         a nested scope restores its enclosing one instead of discarding it
	 */
	static Set<String> begin() {
		Set<String> enclosing = missingPrivileges.get();
		missingPrivileges.set(new LinkedHashSet<>());
		return enclosing;
	}

	/**
	 * Closes the innermost scope, making <code>enclosing</code> the active one again.
	 *
	 * @param enclosing the value {@link #begin()} returned
	 * @return the privilege names found missing within the scope just closed, in the order they were
	 *         checked; never <code>null</code>
	 */
	static Set<String> end(Set<String> enclosing) {
		Set<String> recorded = missingPrivileges.get();
		if (enclosing == null) {
			missingPrivileges.remove();
		} else {
			missingPrivileges.set(enclosing);
		}

		return recorded != null ? recorded : Collections.emptySet();
	}
}
