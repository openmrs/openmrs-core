/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.filter;

import java.io.IOException;
import java.util.Objects;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.SessionPrincipal;
import org.openmrs.api.context.UserContext;
import org.openmrs.util.OpenmrsClassLoader;
import org.openmrs.web.WebConstants;
import org.openmrs.web.session.DistributedHttpSessionCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * This is the custom OpenMRS filter. It is defined as the filter of choice in the web.xml file. All
 * page/object calls run through the doFilter method so we can wrap every session with the user's
 * userContext (which holds the user's authenticated info). This is needed because the OpenMRS API
 * keeps authentication information on the current Thread. Web applications use a different thread
 * per request, so before each request this filter will make sure that the UserContext (the
 * authentication information) is on the Thread.
 * <p>
 * When distributed sessions are enabled ({@code session.distributed=true}) the non-replicable
 * {@link UserContext} is not stored on the session; a marshallable {@link SessionPrincipal}
 * snapshot is stored instead and the {@link UserContext} is rebuilt from it each request. Changes
 * are written back explicitly at end of request, because a replicated store only persists
 * {@code setAttribute} calls, not in-place mutation of a stored object.
 */
public class OpenmrsFilter extends OncePerRequestFilter {

	private static final Logger log = LoggerFactory.getLogger(OpenmrsFilter.class);

	private volatile Boolean distributedSessionsEnabled;

	/**
	 * @see jakarta.servlet.Filter#destroy()
	 */
	@Override
	public void destroy() {
		log.debug("Destroying filter");
	}

	/**
	 * This method is called for every request for a page/image/javascript file/etc The main point of
	 * this is to make sure the user's current userContext is on the session and on the current thread
	 *
	 * @see org.springframework.web.filter.OncePerRequestFilter#doFilterInternal(jakarta.servlet.http.HttpServletRequest,
	 *      jakarta.servlet.http.HttpServletResponse, jakarta.servlet.FilterChain)
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest httpRequest, HttpServletResponse httpResponse, FilterChain chain)
	        throws ServletException, IOException {

		// used by htmlInclude tag
		httpRequest.setAttribute(WebConstants.INIT_REQ_UNIQUE_ID, String.valueOf(System.currentTimeMillis()));

		log.debug("requestURI {}", httpRequest.getRequestURI());
		log.debug("requestURL {}", httpRequest.getRequestURL());
		log.debug("request path info {}", httpRequest.getPathInfo());

		applyCsrfCacheHeaders(httpRequest, httpResponse);

		if (distributedSessionsEnabled()) {
			doFilterWithDistributedSession(httpRequest, httpResponse, chain);
		} else {
			doFilterWithLocalSession(httpRequest, httpResponse, chain);
		}
	}

	/**
	 * The {@code session.distributed} flag is fixed at startup, so it is resolved once and cached.
	 * Resolving it per request would copy the runtime properties on every request when the feature is
	 * off (the default).
	 */
	private boolean distributedSessionsEnabled() {
		Boolean cached = distributedSessionsEnabled;
		if (cached == null) {
			cached = DistributedHttpSessionCondition.isEnabled();
			distributedSessionsEnabled = cached;
		}
		return cached;
	}

	/**
	 * Legacy behaviour (unchanged): the live {@link UserContext} is created once and kept on the HTTP
	 * session, mutated in place across requests. Used when distributed sessions are disabled.
	 */
	private void doFilterWithLocalSession(HttpServletRequest httpRequest, HttpServletResponse httpResponse,
	        FilterChain chain) throws ServletException, IOException {

		HttpSession httpSession = httpRequest.getSession();

		// User context is created if it doesn't already exist and added to the session
		// note: this usercontext storage logic is copied to webinf/view/uncaughtexception.jsp to
		// 		 prevent stack traces being shown to non-authenticated users
		UserContext userContext = (UserContext) httpSession.getAttribute(WebConstants.OPENMRS_USER_CONTEXT_HTTPSESSION_ATTR);

		// if there isn't a userContext on the session yet, create one and set it onto the session
		if (userContext == null) {
			userContext = new UserContext(Context.getAuthenticationScheme());
			httpSession.setAttribute(WebConstants.OPENMRS_USER_CONTEXT_HTTPSESSION_ATTR, userContext);

			log.debug("Just set user context {} as attribute on session", userContext);
		}

		setSessionAttributeIfChanged(httpSession, "username", usernameOf(userContext));
		setSessionAttributeIfChanged(httpSession, "locale", userContext.getLocale());

		// Add the user context to the current thread
		Context.setUserContext(userContext);
		Thread.currentThread().setContextClassLoader(OpenmrsClassLoader.getInstance());

		log.debug("before chain.Filter");

		// continue the filter chain (going on to spring, authorization, etc)
		try {
			chain.doFilter(httpRequest, httpResponse);
		} finally {
			Context.clearUserContext();
		}

		log.debug("after chain.doFilter");
	}

	/**
	 * Distributed behaviour: rebuild the {@link UserContext} from the replicated
	 * {@link SessionPrincipal} snapshot for this request, then write any change back explicitly so it
	 * replicates. A session is only created when there is authenticated state worth storing, so
	 * anonymous requests do not spawn replicated sessions here.
	 */
	private void doFilterWithDistributedSession(HttpServletRequest httpRequest, HttpServletResponse httpResponse,
	        FilterChain chain) throws ServletException, IOException {

		HttpSession existing = httpRequest.getSession(false);
		Object stored = existing == null ? null
		        : existing.getAttribute(WebConstants.OPENMRS_SESSION_PRINCIPAL_HTTPSESSION_ATTR);
		// instanceof (not a cast): an incompatible stored value degrades to anonymous rather than
		// throwing on every request (e.g. after a class/serialVersionUID change across an upgrade).
		SessionPrincipal before = stored instanceof SessionPrincipal principal ? principal : null;

		UserContext userContext = UserContext.fromSessionPrincipal(Context.getAuthenticationScheme(), before);
		Context.setUserContext(userContext);
		Thread.currentThread().setContextClassLoader(OpenmrsClassLoader.getInstance());

		// Bind the request so the login listener can reach it: REST authenticates in a filter, before the
		// servlet binds request attributes. Restore any previous binding afterwards.
		RequestAttributes previousAttributes = RequestContextHolder.getRequestAttributes();
		ServletRequestAttributes attributes = new ServletRequestAttributes(httpRequest, httpResponse);
		RequestContextHolder.setRequestAttributes(attributes);

		try {
			chain.doFilter(httpRequest, httpResponse);
		} finally {
			// End-of-request work runs in a finally, so guard it: a failure here must not mask an
			// exception thrown by the filter chain.
			try {
				writeSessionPrincipal(before, httpRequest);
			} catch (Exception e) {
				log.warn("Could not persist the distributed session at end of request", e);
			} finally {
				try {
					attributes.requestCompleted();
				} catch (Exception e) {
					log.warn("Could not complete request attributes", e);
				}
				RequestContextHolder.setRequestAttributes(previousAttributes);
				Context.clearUserContext();
			}
		}
	}

	/**
	 * Persists end-of-request session state in distributed mode: INVALIDATE drops the session, WRITE
	 * stores the current {@link SessionPrincipal} (plus username/locale) on an existing session, NONE
	 * does nothing. The principal and the username/locale attributes come from a single snapshot of the
	 * current context so they always describe the same state.
	 */
	private void writeSessionPrincipal(SessionPrincipal before, HttpServletRequest httpRequest) {
		UserContext current = Context.getUserContext();
		SessionPrincipal after = current.toSessionPrincipal();
		SessionWrite decision = decideSessionWrite(before, after);

		if (decision == SessionWrite.INVALIDATE) {
			HttpSession toInvalidate = httpRequest.getSession(false);
			if (toInvalidate != null) {
				toInvalidate.invalidate();
			}
			return;
		}

		// Reuse an existing session only; do not create one here, since that would fail once the response
		// is committed. It was already established at login, or pre-exists.
		HttpSession session = httpRequest.getSession(false);
		if (session == null) {
			if (decision == SessionWrite.WRITE && after.isAuthenticated()) {
				log.warn("Authenticated state was not persisted: no HTTP session exists at end of request "
				        + "(login may have happened after the response was committed)");
			}
			return;
		}
		if (decision == SessionWrite.WRITE) {
			session.setAttribute(WebConstants.OPENMRS_SESSION_PRINCIPAL_HTTPSESSION_ATTR, after);
		}
		// Keep the username/locale convenience attributes current even when the principal itself is
		// unchanged (e.g. a renamed account): setSessionAttributeIfChanged is a no-op when nothing changed.
		setSessionAttributeIfChanged(session, "username", usernameOf(current));
		setSessionAttributeIfChanged(session, "locale", current.getLocale());
	}

	/**
	 * Side-effect-free decision of what to persist at end of request, from the principal loaded at the
	 * start ({@code before}) vs. after any change during it ({@code after}): INVALIDATE (became
	 * unauthenticated), WRITE (authenticated state to persist or a changed principal), or NONE
	 * (unchanged, or anonymous with no prior session).
	 */
	static SessionWrite decideSessionWrite(SessionPrincipal before, SessionPrincipal after) {
		boolean wasAuthenticated = before != null && before.isAuthenticated();
		boolean isAuthenticated = after != null && after.isAuthenticated();

		if (wasAuthenticated && !isAuthenticated) {
			return SessionWrite.INVALIDATE;
		}
		if (after != null && (isAuthenticated || before != null) && !Objects.equals(before, after)) {
			return SessionWrite.WRITE;
		}
		return SessionWrite.NONE;
	}

	enum SessionWrite {
		NONE,
		WRITE,
		INVALIDATE
	}

	private static String usernameOf(UserContext userContext) {
		User user = userContext.getAuthenticatedUser();
		return user != null ? user.getUsername() : "-anonymous user-";
	}

	private void applyCsrfCacheHeaders(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		//TODO We do not cache the csrfguard javascript file because it contains the
		//csrf token that is dynamically embedded in forms. For this to work,
		//the OpenmrsFilter should be before the CSRFGuard filter in web.xml
		if (httpRequest.getRequestURI().endsWith("csrfguard")) {
			httpResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
			httpResponse.setHeader("Pragma", "no-cache"); // HTTP 1.0.
			httpResponse.setHeader("Expires", "0"); // Proxies.
		}
	}

	private void setSessionAttributeIfChanged(HttpSession session, String name, Object value) {
		if (!Objects.equals(session.getAttribute(name), value)) {
			session.setAttribute(name, value);
		}
	}

}
