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

import java.util.Map;
import java.util.function.Supplier;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.core.Authentication;

/**
 * The expression handler behind OpenMRS method security, which is the stock one plus a privilege
 * cache spanning one {@code @PreFilter}/{@code @PostFilter} pass. {@code filter} is the one place
 * that evaluates an expression repeatedly - once per element - and it is called once per
 * invocation, so it is exactly the scope over which the privilege named by the expression cannot
 * change. See {@link FilterPassPrivilegeCache} for why only the privilege is cached.
 * <p>
 * It also resolves the {@link Authentication} an expression is evaluated against through
 * {@link LegacyContextAuthentication}, so an expression is decided from the legacy
 * {@code UserContext} when the {@code SecurityContextHolder} is empty - without which
 * {@code hasPermission(...)} and {@code hasAuthority(...)} would not agree on such a thread. See
 * that class for why the two can diverge.
 *
 * @since 3.0.0
 */
public class PrivilegeCachingMethodSecurityExpressionHandler extends DefaultMethodSecurityExpressionHandler {

	/**
	 * The one place every expression's {@link Authentication} comes from - {@code @PreAuthorize},
	 * {@code @PostAuthorize} and both filters all build their root here - which is why the fallback is
	 * installed at this level rather than per interceptor.
	 */
	@Override
	public EvaluationContext createEvaluationContext(Supplier<? extends Authentication> authentication,
	        MethodInvocation mi) {
		return super.createEvaluationContext(LegacyContextAuthentication.orCurrentUserContext(authentication), mi);
	}

	@Override
	public Object filter(Object filterTarget, Expression filterExpression, EvaluationContext ctx) {
		Map<String, Boolean> enclosing = FilterPassPrivilegeCache.begin();
		try {
			return super.filter(filterTarget, filterExpression, ctx);
		} finally {
			FilterPassPrivilegeCache.end(enclosing);
		}
	}
}
