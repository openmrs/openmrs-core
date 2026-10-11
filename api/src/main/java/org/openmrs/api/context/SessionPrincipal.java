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

import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;

import org.openmrs.User;

/**
 * A minimal, marshallable snapshot of a {@link UserContext}'s authenticated state, stored on the
 * HTTP session when distributed sessions are enabled so the session can be replicated and any node
 * can rebuild an equivalent {@link UserContext}.
 * <p>
 * It holds only the user's UUID, locale and location id - never the live {@link org.openmrs.User}/
 * {@link org.openmrs.Role} graph or the non-serializable {@link AuthenticationScheme}; on rebuild
 * the user is re-fetched by UUID and the scheme re-injected (see
 * {@link UserContext#fromSessionPrincipal(AuthenticationScheme, SessionPrincipal)}). Request-scoped
 * proxy privileges are intentionally excluded, so a temporary escalation cannot outlive its
 * request.
 *
 * @since 3.0.0
 */
public class SessionPrincipal implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String userUuid;

	private final String localeLanguageTag;

	private final Integer locationId;

	public SessionPrincipal(String userUuid, Locale locale, Integer locationId) {
		this.userUuid = userUuid;
		this.localeLanguageTag = locale == null ? null : locale.toLanguageTag();
		this.locationId = locationId;
	}

	/** From a {@link User} (may be {@code null} for an anonymous principal). */
	public SessionPrincipal(User user, Locale locale, Integer locationId) {
		this(user == null ? null : user.getUuid(), locale, locationId);
	}

	/** @return the authenticated user's uuid, or {@code null} if anonymous */
	public String getUserUuid() {
		return userUuid;
	}

	public Locale getLocale() {
		return localeLanguageTag == null ? null : Locale.forLanguageTag(localeLanguageTag);
	}

	public Integer getLocationId() {
		return locationId;
	}

	/** @return true if this principal carries a user uuid (i.e. is authenticated) */
	public boolean isAuthenticated() {
		return userUuid != null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof SessionPrincipal)) {
			return false;
		}
		SessionPrincipal that = (SessionPrincipal) o;
		return Objects.equals(userUuid, that.userUuid) && Objects.equals(localeLanguageTag, that.localeLanguageTag)
		        && Objects.equals(locationId, that.locationId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(userUuid, localeLanguageTag, locationId);
	}

	@Override
	public String toString() {
		return "SessionPrincipal{userUuid=" + userUuid + ", locale=" + localeLanguageTag + ", locationId=" + locationId
		        + "}";
	}
}
