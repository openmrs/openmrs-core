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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Memoizes the privilege half of {@code hasPermission(...)} for the duration of one
 * {@code @PreFilter}/{@code @PostFilter} pass, opened by
 * {@link PrivilegeCachingMethodSecurityExpressionHandler}.
 * <p>
 * A filter expression is evaluated once per element but names a fixed privilege, so a collection of
 * n elements resolves the same privilege n times - each resolution bracketed by a {@code Get Roles}
 * proxy privilege that mutates a synchronized list (see {@link PrivilegeResolution}). On the read
 * methods that return tens of thousands of rows that is the dominant cost of filtering, and every
 * answer after the first is already known.
 * <p>
 * Only the privilege is cached, never a {@link DomainObjectAuthorizationRule} verdict: rules are
 * asked about a particular object and genuinely differ per element. The cache is therefore no more
 * than "this user either holds this privilege for this pass or does not", which holds unless a rule
 * changes the ambient privilege set mid-pass - something no rule should do, since it would make the
 * pass's outcome depend on element order either way.
 */
final class FilterPassPrivilegeCache {

	private static final ThreadLocal<Map<String, Boolean>> verdicts = new ThreadLocal<>();

	private FilterPassPrivilegeCache() {
	}

	/**
	 * Opens a pass on the current thread.
	 *
	 * @return whatever pass was open before this call, to be handed back to {@link #end(Map)} so a
	 *         nested pass restores its enclosing one instead of discarding it
	 */
	static Map<String, Boolean> begin() {
		Map<String, Boolean> enclosing = verdicts.get();
		verdicts.set(new HashMap<>());
		return enclosing;
	}

	/**
	 * Closes the innermost pass, making <code>enclosing</code> the active one again.
	 *
	 * @param enclosing the value {@link #begin()} returned
	 */
	static void end(Map<String, Boolean> enclosing) {
		if (enclosing == null) {
			verdicts.remove();
		} else {
			verdicts.set(enclosing);
		}
	}

	/**
	 * Whether <code>privilege</code> is held, answered from the open pass when it has already been
	 * resolved there. Falls straight through to <code>resolver</code> when no pass is open, which is
	 * every {@code @PreAuthorize}/{@code @PostAuthorize} check as well as a {@code @PostFilter} over a
	 * {@code Stream}, whose elements are filtered lazily after the pass has closed.
	 *
	 * @param privilege the privilege name to resolve
	 * @param resolver resolves the privilege when the pass has not seen it yet
	 * @return true if the privilege is held
	 */
	static boolean holdsPrivilege(String privilege, Predicate<String> resolver) {
		Map<String, Boolean> pass = verdicts.get();
		if (pass == null) {
			return resolver.test(privilege);
		}

		// deliberately not computeIfAbsent: resolving re-enters privilege resolution, which may filter
		// a nested collection and so recurse into this map
		Boolean held = pass.get(privilege);
		if (held == null) {
			held = resolver.test(privilege);
			pass.put(privilege, held);
		}
		return held;
	}
}
