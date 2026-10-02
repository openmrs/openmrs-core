/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.security;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;

/**
 * Publishes the {@link AccessDeniedException} that caused a denial as the
 * {@link WebAttributes#ACCESS_DENIED_403} request attribute, then responds exactly as Spring
 * Security's own {@link AccessDeniedHandlerImpl} would.
 * <p>
 * The point is to stop losing the reason. Denials raised by
 * {@link org.openmrs.security.PrivilegeNamingAuthorizationManager} carry the
 * {@code error.privilegesRequired} message naming the privilege that was missing, but
 * {@code AccessDeniedHandlerImpl} answers with {@code sendError(403, "Forbidden")} when no error
 * page is configured on it and never looks at the exception, so that message reached the logs and
 * nothing else. Publishing it here makes it available to whatever renders the 403 - an
 * {@code <error-page>} JSP, a module's own error handling - without altering the status code or the
 * response body, so nothing that already relies on a plain 403 changes.
 * <p>
 * The attribute is set before the response is committed, so it is still on the request when the
 * container performs its {@code ERROR} dispatch and the error page runs. Spring's own handler sets
 * the same attribute, but only on the branch where an {@code errorPage} has been configured on it,
 * which is not how it is used here.
 * <p>
 * Installed on both {@code ExceptionTranslationFilter}s OpenMRS puts in the chain:
 * {@link OpenmrsAuthorizationFilter}'s, which handles {@link AuthorizedUrlMatcher} denials and
 * anything thrown downstream of it, and {@link WebSecurityConfig}'s, which handles whatever reaches
 * the outer chain instead - a filter between the two failing before
 * {@code openmrsAuthorizationFilter} is entered, for instance.
 *
 * @since 3.0.0
 */
public class OpenmrsAccessDeniedHandler implements AccessDeniedHandler {

	private final AccessDeniedHandler delegate = new AccessDeniedHandlerImpl();

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
	        throws IOException, ServletException {
		request.setAttribute(WebAttributes.ACCESS_DENIED_403, accessDeniedException);
		delegate.handle(request, response, accessDeniedException);
	}
}
