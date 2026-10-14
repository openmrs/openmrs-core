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

import java.lang.reflect.Method;

import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleKey;

/**
 * Cache key generator that includes the authenticated user alongside the method arguments, so each
 * user gets their own entry: <pre>
 * &#64;Cacheable(value = "myCache", keyGenerator = UserKeyGenerator.BEAN_NAME)
 * &#64;PostFilter("hasAuthority('Get Widgets')")
 * public List&lt;Widget&gt; getWidgets(String query) { ... }
 * </pre>
 * <p>
 * A {@code @PostFilter}ed {@code @Cacheable} method has to key by the caller one way or another,
 * because {@code @PostFilter} filters in place - {@code filterCollection} clears the returned
 * collection and re-adds what survived, so with a shared key the first caller's filtering
 * permanently strips the entry and every later caller is served that remainder. A {@code key}
 * expression naming the caller works too, and may refer to arguments by name, since the build
 * compiles with {@code -parameters}.
 * <p>
 * The identity is the user's id, falling back to the uuid, then to a constant when unauthenticated.
 * It covers who is asking, not what they have been lent: privileges from
 * {@link Context#addProxyPrivilege(String)} are not part of the key, so a {@code @PostFilter} whose
 * outcome depends on one is not safe to cache at all.
 *
 * @since 3.0.0
 */
public class UserKeyGenerator implements KeyGenerator {

	/**
	 * Bean name this generator is registered under, for {@code @Cacheable(keyGenerator = ...)}.
	 */
	public static final String BEAN_NAME = "userKeyGenerator";

	private static final String ANONYMOUS = "anonymous";

	@Override
	public Object generate(Object target, Method method, Object... params) {
		Object[] keyParts = new Object[params.length + 1];
		keyParts[0] = currentUserIdentity();
		System.arraycopy(params, 0, keyParts, 1, params.length);
		return new SimpleKey(keyParts);
	}

	/**
	 * @return a small, stable identifier for whoever is asking - the user id where there is one, the
	 *         uuid otherwise, and a constant when nobody is authenticated
	 */
	private static Object currentUserIdentity() {
		User user = Context.isSessionOpen() ? Context.getAuthenticatedUser() : null;
		if (user == null) {
			return ANONYMOUS;
		}

		return user.getUserId() != null ? user.getUserId() : user.getUuid();
	}
}
