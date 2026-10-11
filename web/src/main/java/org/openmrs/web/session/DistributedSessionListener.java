/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.session;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.openmrs.User;
import org.openmrs.UserSessionListener;
import org.openmrs.api.cache.DistributedSessionPurger;
import org.openmrs.api.context.Context;
import org.openmrs.web.WebConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * On login (via {@link UserSessionListener}, the choke point every login path flows through)
 * maintains the replicated session: a successful login invalidates the pre-login session and
 * continues it on a fresh id (fixation protection) and stores the marshallable
 * {@link org.openmrs.api.context.SessionPrincipal}. Logout needs nothing here: ending the request,
 * the filter invalidates the replicated session when the principal turns anonymous, and callers
 * that also invalidate (e.g. legacyui's LogoutServlet) are already racing a finished invalidation.
 * <p>
 * The session is established at login time - not in a post-request step - because a controller
 * commits the response before the filter chain unwinds, and a session created after that commit can
 * never send its {@code Set-Cookie}.
 * <p>
 * Contributed by {@link DistributedHttpSessionConfig}, so it exists only when
 * {@code session.distributed=true}. Non-web logins (no bound request) are ignored.
 *
 * @since 3.0.0
 */
public class DistributedSessionListener implements UserSessionListener {

	private static final Logger log = LoggerFactory.getLogger(DistributedSessionListener.class);

	private final DistributedSessionPurger sessionPurger;

	/**
	 * @param sessionPurger drops the pre-rotation session's stored entry immediately after the id
	 *            rotates, so an attacker-chosen id cannot keep resolving while the request is still in
	 *            flight
	 * @since 3.0.0
	 */
	public DistributedSessionListener(DistributedSessionPurger sessionPurger) {
		this.sessionPurger = sessionPurger;
	}

	@Override
	public void loggedInOrOut(User user, Event event, Status status) {
		HttpServletRequest request = currentRequest();
		if (request == null) {
			// non-web login (e.g. Daemon/background); nothing to do
			return;
		}
		if (event == Event.LOGIN && status == Status.SUCCESS) {
			establishSession(request);
		}
		// No logout branch: Context.logout() already ends that request's session, and the filter
		// invalidates the replicated session at end of request when the principal turns anonymous.
		// legacyui's LogoutServlet additionally invalidates a handle it took before the logout, so
		// a second invalidate here would log IllegalStateException on every legacy logout.
	}

	private void establishSession(HttpServletRequest request) {
		// session-fixation protection: rotate the id WITHOUT invalidating the handle (callers like
		// the authentication module keep using it across Context.authenticate()), then drop the
		// pre-rotation session's stored entry through the purger so the old id stops resolving.
		HttpSession existing = request.getSession(false);
		if (existing != null) {
			String preLoginSessionKey = existing.getId();
			try {
				request.changeSessionId();
				if (sessionPurger != null) {
					sessionPurger.purge(preLoginSessionKey);
				}
			} catch (IllegalStateException e) {
				log.debug("Skipped session id rotation on login", e);
			}
		}
		// guarded: getSession throws once the response is committed; must not break login
		try {
			HttpSession session = request.getSession(true);
			session.setAttribute(WebConstants.OPENMRS_SESSION_PRINCIPAL_HTTPSESSION_ATTR,
			    Context.getUserContext().toSessionPrincipal());
			log.debug("Established distributed session {} on login", session.getId());
		} catch (Exception e) {
			log.warn("Could not establish the distributed session on login (response already committed?); "
			        + "this login will not be remembered across requests",
			    e);
		}
	}

	private HttpServletRequest currentRequest() {
		RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
		if (attributes instanceof ServletRequestAttributes servletRequestAttributes) {
			return servletRequestAttributes.getRequest();
		}
		return null;
	}
}
