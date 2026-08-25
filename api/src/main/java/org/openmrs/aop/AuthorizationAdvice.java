/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.aop;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.User;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.Daemon;
import org.openmrs.util.PrivilegeConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

/**
 * This class provides the authorization AOP advice performed before every service layer method
 * call.
 */
@Component("authorizationInterceptor")
public class AuthorizationAdvice implements MethodBeforeAdvice {

	/**
	 * Logger for this class and subclasses
	 */
	private static final Logger log = LoggerFactory.getLogger(AuthorizationAdvice.class);

	private static final String USER_IS_NOT_AUTHORIZED_TO_ACCESS = "User {} is not authorized to access {}";

	/**
	 * Memoized authorization metadata per {@link Method}. Annotation reflection is performed once per
	 * method and reused for every subsequent call.
	 */
	private static final Map<Method, AuthorizedMetadata> metadataCache = new ConcurrentHashMap<>();

	/**
	 * Holds the resolved {@link Authorized} annotation metadata for a single method.
	 */
	private static final class AuthorizedMetadata {

		final Collection<String> privileges;

		final boolean requireAll;

		final boolean hasAnnotation;

		AuthorizedMetadata(Collection<String> privileges, boolean requireAll, boolean hasAnnotation) {
			this.privileges = privileges;
			this.requireAll = requireAll;
			this.hasAnnotation = hasAnnotation;
		}
	}

	/**
	 * Resolves and caches the {@link Authorized} annotation metadata for the given method. Annotation
	 * reflection happens at most once per method.
	 */
	private AuthorizedMetadata resolveMetadata(Method method) {
		return metadataCache.computeIfAbsent(method, m -> {
			Authorized authorized = AnnotationUtils.findAnnotation(m, Authorized.class);
			if (authorized != null) {
				Set<String> privileges = new LinkedHashSet<>();
				Collections.addAll(privileges, authorized.value());
				return new AuthorizedMetadata(Collections.unmodifiableSet(privileges), authorized.requireAll(), true);
			}
			return new AuthorizedMetadata(Collections.emptySet(), false, false);
		});
	}

	/**
	 * Allows us to check whether a user is authorized to access a particular method.
	 * <p>
	 * <strong>Should</strong> notify listeners about checked privileges
	 *
	 * @param method
	 * @param args
	 * @param target
	 * @throws Throwable
	 */
	@Override
	public void before(Method method, Object[] args, Object target) throws Throwable {
		log.debug("Calling authorization advice before {}", method.getName());

		if (log.isDebugEnabled()) {
			User user = Context.getAuthenticatedUser();
			log.debug("User {}", user);
			if (user != null) {
				log.debug("has roles {}", user.getAllRoles());
			}
		}

		if (Daemon.isDaemonThread()) {
			return;
		}

		AuthorizedMetadata metadata = resolveMetadata(method);
		Collection<String> privileges = metadata.privileges;
		boolean requireAll = metadata.requireAll;

		// Only execute if the "secure" method has authorization attributes
		// Iterate through required privileges and return only if the user has
		// one of them
		if (!privileges.isEmpty()) {
			try {
				Context.addProxyPrivilege(PrivilegeConstants.GET_ROLES);
				for (String privilege : privileges) {
					// skip null privileges
					if (privilege == null || privilege.isEmpty()) {
						return;
					}
					boolean hasPrivilege = Context.hasPrivilege(privilege);
					log.debug("User has privilege {}? {}", privilege, hasPrivilege);

					if (hasPrivilege) {
						if (!requireAll) {
							// if not all required, the first one that they have
							// causes them to "pass"
							return;
						}
					} else {
						if (requireAll) {
							// if all are required, the first miss causes them
							// to "fail"
							throwUnauthorized(Context.getAuthenticatedUser(), method, privilege);
						}
					}
				}
			} finally {
				Context.removeProxyPrivilege(PrivilegeConstants.GET_ROLES);
			}

			if (!requireAll) {
				// If there's no match, then we know there are privileges and
				// that the user didn't have any of them. The user is not
				// authorized to access the method
				throwUnauthorized(Context.getAuthenticatedUser(), method, privileges);
			}

		} else if (metadata.hasAnnotation && !(Context.isAuthenticated() || Context.hasProxyPrivileges())) {
			throwUnauthorized(Context.getAuthenticatedUser(), method);
		}
	}

	/**
	 * Throws an APIAuthorization exception stating why the user failed
	 *
	 * @param user authenticated user
	 * @param method acting method
	 * @param attrs Collection of String privilege names that the user must have
	 */
	private void throwUnauthorized(User user, Method method, Collection<String> attrs) {
		log.debug(USER_IS_NOT_AUTHORIZED_TO_ACCESS, user, method.getName());
		throw new APIAuthenticationException(Context.getMessageSourceService().getMessage("error.privilegesRequired",
		    new Object[] { StringUtils.join(attrs, ",") }, Locale.getDefault()));
	}

	/**
	 * Throws an APIAuthorization exception stating why the user failed
	 *
	 * @param user authenticated user
	 * @param method acting method
	 * @param attr privilege names that the user must have
	 */
	private void throwUnauthorized(User user, Method method, String attr) {
		log.debug(USER_IS_NOT_AUTHORIZED_TO_ACCESS, user, method.getName());
		throw new APIAuthenticationException(Context.getMessageSourceService().getMessage("error.privilegesRequired",
		    new Object[] { attr }, Locale.getDefault()));
	}

	/**
	 * Throws an APIAuthorization exception stating why the user failed
	 *
	 * @param user authenticated user
	 * @param method acting method
	 */
	private void throwUnauthorized(User user, Method method) {
		log.debug(USER_IS_NOT_AUTHORIZED_TO_ACCESS, user, method.getName());
		throw new APIAuthenticationException(Context.getMessageSourceService().getMessage("error.aunthenticationRequired"));
	}
}
