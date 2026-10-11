/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.context;

import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.openmrs.User;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Value semantics of {@link SessionPrincipal}. The principal is compared and logged on every
 * request that rebuilds a {@link UserContext}, and an equality slip would either rebuild the wrong
 * user or silently drop a principal, so the accessors, the null/anonymous handling and the
 * three-field comparison are pinned down here. Marshalling is proven separately by
 * {@link SessionPrincipalReplicationTest}.
 */
class SessionPrincipalTest {

	private static final Locale LOCALE = Locale.forLanguageTag("en-GB");

	private static User user(String uuid) {
		User user = new User();
		user.setUuid(uuid);
		return user;
	}

	@Test
	void shouldCopyTheUuidFromAUser() {
		SessionPrincipal principal = new SessionPrincipal(user("uuid-1"), LOCALE, 3);

		assertEquals("uuid-1", principal.getUserUuid());
		assertEquals(LOCALE, principal.getLocale());
		assertEquals(Integer.valueOf(3), principal.getLocationId());
		assertTrue(principal.isAuthenticated());
	}

	/**
	 * A null user is the anonymous case, so it must produce an unauthenticated principal rather than
	 * throwing.
	 */
	@Test
	void shouldTreatANullUserAsAnonymous() {
		SessionPrincipal principal = new SessionPrincipal((User) null, LOCALE, 3);

		assertNull(principal.getUserUuid());
		assertFalse(principal.isAuthenticated());
	}

	@Test
	void shouldReturnNullLocaleWhenNoneWasCaptured() {
		assertNull(new SessionPrincipal("uuid-1", null, null).getLocale());
	}

	@Test
	void shouldBeEqualToItself() {
		SessionPrincipal principal = new SessionPrincipal("uuid-1", LOCALE, 3);

		assertEquals(principal, principal);
	}

	/**
	 * Guards against a principal being considered equal to an unrelated object just because it happens
	 * to be non-null.
	 */
	@Test
	void shouldNotBeEqualToAnotherType() {
		SessionPrincipal principal = new SessionPrincipal("uuid-1", LOCALE, 3);

		assertThat(principal, not(equalTo("uuid-1")));
		assertThat(principal, not(equalTo(null)));
	}

	@Test
	void shouldBeEqualWhenAllThreeFieldsMatch() {
		SessionPrincipal one = new SessionPrincipal("uuid-1", LOCALE, 3);
		SessionPrincipal other = new SessionPrincipal("uuid-1", Locale.forLanguageTag("en-GB"), 3);

		assertEquals(one, other);
	}

	/**
	 * Every field is part of the identity: a different user, locale or location must not collapse to
	 * the same principal, or a node would serve a session as the wrong user or in the wrong locale.
	 */
	@Test
	void shouldDistinguishUserLocaleAndLocation() {
		SessionPrincipal principal = new SessionPrincipal("uuid-1", LOCALE, 3);

		assertNotEquals(principal, new SessionPrincipal("uuid-2", LOCALE, 3));
		assertNotEquals(principal, new SessionPrincipal("uuid-1", Locale.GERMAN, 3));
		assertNotEquals(principal, new SessionPrincipal("uuid-1", LOCALE, 4));
	}

	@Test
	void shouldProduceEqualHashCodesForEqualPrincipals() {
		SessionPrincipal one = new SessionPrincipal("uuid-1", LOCALE, 3);
		SessionPrincipal other = new SessionPrincipal(user("uuid-1"), Locale.forLanguageTag("en-GB"), 3);

		assertEquals(one, other);
		assertEquals(one.hashCode(), other.hashCode());
	}

	@Test
	void shouldComputeAStableHashCodeForTheAnonymousPrincipal() {
		SessionPrincipal anonymous = new SessionPrincipal((User) null, null, null);

		assertEquals(anonymous.hashCode(), new SessionPrincipal((User) null, null, null).hashCode());
	}

	/**
	 * A hash that ignored its inputs would make every principal collide in any hash-based lookup, so
	 * the hash has to respond to each field. Distinct inputs are not <em>required</em> to produce
	 * distinct hashes - collisions are legal - but a hash that is constant across all of them is a
	 * defect, so this pins down that the fields actually feed the hash.
	 */
	@Test
	void shouldHashDependOnEveryField() {
		SessionPrincipal principal = new SessionPrincipal("uuid-1", LOCALE, 3);

		assertNotEquals(principal.hashCode(), new SessionPrincipal("uuid-2", LOCALE, 3).hashCode());
		assertNotEquals(principal.hashCode(), new SessionPrincipal("uuid-1", Locale.GERMAN, 3).hashCode());
		assertNotEquals(principal.hashCode(), new SessionPrincipal("uuid-1", LOCALE, 4).hashCode());
	}

	/**
	 * The {@code toString} ends up in the log when a principal is written to or rebuilt from the
	 * session, so it must identify the principal without dumping anything else.
	 */
	@Test
	void shouldDescribeItselfInToString() {
		SessionPrincipal principal = new SessionPrincipal("uuid-1", LOCALE, 3);

		String text = principal.toString();

		assertTrue(text.contains("uuid-1"), text);
		assertTrue(text.contains("en-GB"), text);
		assertTrue(text.contains("3"), text);
	}
}
