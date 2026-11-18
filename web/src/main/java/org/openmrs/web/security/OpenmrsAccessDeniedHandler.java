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
 * Publishes the {@link AccessDeniedException} behind a denial as the
 * {@link WebAttributes#ACCESS_DENIED_403} request attribute, then responds exactly as
 * {@link AccessDeniedHandlerImpl} would.
 * <p>
 * Without it the reason is lost: denials from
 * {@link org.openmrs.security.PrivilegeNamingAuthorizationManager} name the missing privilege, but
 * {@code AccessDeniedHandlerImpl} answers {@code sendError(403, "Forbidden")} without ever reading
 * the exception, so the message reached only the logs. The attribute is set before the response
 * commits, so it survives the container's {@code ERROR} dispatch and is available to whatever
 * renders the 403, with status code and body unchanged.
 * <p>
 * Installed on both {@code ExceptionTranslationFilter}s: {@link OpenmrsAuthorizationFilter}'s, and
 * {@link WebSecurityConfig}'s for denials that never reach the inner one.
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
