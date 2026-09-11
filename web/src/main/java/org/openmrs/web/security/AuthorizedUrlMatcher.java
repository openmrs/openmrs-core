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

import java.util.Arrays;

import org.springframework.security.authorization.AuthenticatedAuthorizationManager;
import org.springframework.security.authorization.AuthorityAuthorizationManager;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationManagers;
import org.springframework.security.authorization.SingleResultAuthorizationManager;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * Lets core or a module require an {@link AuthorizationManager} decision to reach a URL pattern -
 * the web-request equivalent of guarding a service method with
 * {@link org.openmrs.annotation.Authorized}, but built entirely from Spring Security's own
 * vocabulary ({@code hasAuthority}, {@code hasRole}, {@code AuthorizationManagers.allOf(...)}, or
 * any hand-written {@link AuthorizationManager}) rather than a narrower, OpenMRS-specific shape.
 * This type itself is a plain value, not something registered as a bean directly - core or a module
 * bundles every rule it needs into one {@link AuthorizedUrlMatchers} bean instead (see that
 * interface's javadoc for the registration mechanics and why it is one bean per module, not one per
 * rule). {@link WebSecurityConfig} gathers every such bean once, when the
 * {@code SecurityFilterChain} bean is built, not re-queried per request. That is safe because
 * loading or unloading a module fully refreshes the Spring context anyway, so a stale rule from an
 * unloaded module, or a missing rule from one loaded afterward, cannot persist past that refresh.
 * <p>
 * {@link #requestMatchers(String...)} is the intended way to build one: it returns a {@link Rule} -
 * itself a plain {@link RequestMatcher}, reusable anywhere one is expected, not a separate
 * unrelated builder type - whose {@code hasAuthority(...)}/{@code hasRole(...)}/etc. default
 * methods turn it into a finished {@link AuthorizedUrlMatcher}: <pre>
 * AuthorizedUrlMatcher.requestMatchers("/moduleServlet/myModule/admin/**").hasAuthority("Manage My Module")
 * </pre>
 * <p>
 * Deliberately not exposed the way {@code HttpSecurity.authorizeHttpRequests(...)} itself is
 * customized (a {@code Customizer} contributing to one shared, mutable registry): Spring's own
 * registry allows {@code anyRequest()} to be configured at most once for its entire lifetime
 * (throwing {@code IllegalStateException} at startup on a second call) and resolves overlapping
 * matchers by first-registered-wins, not by requiring every match to agree. A module contributing
 * to a shared registry could therefore crash every installation that also has core's own fallback
 * rule, or silently have its rule never consulted because another module's overlapping pattern was
 * registered first. Each {@link AuthorizedUrlMatcher} is self-contained instead: a URL with no
 * matching rule is permitted (adding a rule can only narrow access, never be the sole thing
 * granting it), and when more than one rule matches the same path, every matching rule's
 * {@link AuthorizationManager} must agree - the combination is never looser than its strictest
 * rule, mirroring how {@code @Authorized} and {@code @PreAuthorize} combine on a single method (see
 * {@code CombinedAuthorizedAndPreAuthorizeTest} in the api module) - see
 * {@link OpenmrsAuthorizationManager} for where that combination happens.
 *
 * @since 3.0.0
 */
public interface AuthorizedUrlMatcher {

	/**
	 * @return the {@link RequestMatcher} this rule governs
	 */
	RequestMatcher getRequestMatcher();

	/**
	 * @return the {@link AuthorizationManager} checked for a request matching
	 *         {@link #getRequestMatcher()}
	 */
	AuthorizationManager<RequestAuthorizationContext> getAuthorizationManager();

	/**
	 * @param patterns one or more Ant-style path patterns (e.g. {@code "/moduleServlet/myModule/**"}),
	 *            relative to the servlet context; a request matches if any one of them does
	 * @return a {@link Rule} for the given patterns, ready to be turned into a
	 *         {@link AuthorizedUrlMatcher} by naming the authorization check that governs it
	 */
	static Rule requestMatchers(String... patterns) {
		RequestMatcher matcher = (patterns.length == 1) ? PathPatternRequestMatcher.pathPattern(patterns[0])
		        : new OrRequestMatcher(Arrays.stream(patterns).map(PathPatternRequestMatcher::pathPattern).toList());
		return matcher::matches;
	}

	/**
	 * A {@link RequestMatcher} that can also be turned directly into a {@link AuthorizedUrlMatcher} by
	 * naming the authorization check that governs it. This is not a separate, unrelated builder type:
	 * {@link RequestMatcher} itself declares exactly one abstract method
	 * ({@link RequestMatcher#matches(jakarta.servlet.http.HttpServletRequest)}), so a sub-interface
	 * that only adds default methods remains a valid functional interface - the value returned by
	 * {@link #requestMatchers(String...)} genuinely is a {@link RequestMatcher}, usable anywhere one is
	 * expected, with these methods simply riding along.
	 */
	@FunctionalInterface
	interface Rule extends RequestMatcher {

		/**
		 * @return a rule granting anyone access, regardless of authentication
		 */
		default AuthorizedUrlMatcher permitAll() {
			return access(SingleResultAuthorizationManager.permitAll());
		}

		/**
		 * @return a rule denying everyone access
		 */
		default AuthorizedUrlMatcher denyAll() {
			return access(SingleResultAuthorizationManager.denyAll());
		}

		/**
		 * @param authority the privilege name required, checked via
		 *            {@link org.springframework.security.core.Authentication#getAuthorities()}
		 * @return a rule requiring the current user hold {@code authority}
		 */
		default AuthorizedUrlMatcher hasAuthority(String authority) {
			return access(AuthorityAuthorizationManager.hasAuthority(authority));
		}

		/**
		 * @param authorities the privilege names, any one of which suffices
		 * @return a rule requiring the current user hold at least one of {@code authorities}
		 */
		default AuthorizedUrlMatcher hasAnyAuthority(String... authorities) {
			return access(AuthorityAuthorizationManager.hasAnyAuthority(authorities));
		}

		/**
		 * @param authorities the privilege names, every one of which is required
		 * @return a rule requiring the current user hold every one of {@code authorities}
		 */
		@SuppressWarnings("unchecked")
		default AuthorizedUrlMatcher hasAllAuthorities(String... authorities) {
			return access(
			    AuthorizationManagers.allOf(Arrays.stream(authorities)
			            .map(authority -> (AuthorizationManager<RequestAuthorizationContext>) AuthorityAuthorizationManager
			                    .<RequestAuthorizationContext> hasAuthority(authority))
			            .toArray(AuthorizationManager[]::new)));
		}

		/**
		 * @param role the role name required (without the {@code ROLE_} prefix), checked via
		 *            {@link org.springframework.security.core.Authentication#getAuthorities()}
		 * @return a rule requiring the current user hold {@code role}
		 */
		default AuthorizedUrlMatcher hasRole(String role) {
			return access(AuthorityAuthorizationManager.hasRole(role));
		}

		/**
		 * @param roles the role names (without the {@code ROLE_} prefix), any one of which suffices
		 * @return a rule requiring the current user hold at least one of {@code roles}
		 */
		default AuthorizedUrlMatcher hasAnyRole(String... roles) {
			return access(AuthorityAuthorizationManager.hasAnyRole(roles));
		}

		/**
		 * @param roles the role names (without the {@code ROLE_} prefix), every one of which is required
		 * @return a rule requiring the current user hold every one of {@code roles}
		 */
		@SuppressWarnings("unchecked")
		default AuthorizedUrlMatcher hasAllRoles(String... roles) {
			return access(AuthorizationManagers.allOf(Arrays.stream(roles)
			        .map(role -> (AuthorizationManager<RequestAuthorizationContext>) AuthorityAuthorizationManager
			                .<RequestAuthorizationContext> hasRole(role))
			        .toArray(AuthorizationManager[]::new)));
		}

		/**
		 * @return a rule requiring only that the caller be authenticated - the URL equivalent of a no-value
		 *         {@code @Authorized}, though see {@code OpenmrsPermissionEvaluator}'s javadoc (api module)
		 *         for why this and a no-value {@code @Authorized} still agree even though this uses
		 *         Spring's built-in {@code isAuthenticated()} semantics
		 */
		default AuthorizedUrlMatcher authenticated() {
			return access(AuthenticatedAuthorizationManager.authenticated());
		}

		/**
		 * @return a rule requiring the caller be authenticated other than by "remember me" - see
		 *         {@link org.springframework.security.authentication.RememberMeAuthenticationToken}
		 */
		default AuthorizedUrlMatcher fullyAuthenticated() {
			return access(AuthenticatedAuthorizationManager.fullyAuthenticated());
		}

		/**
		 * @return a rule requiring the caller be authenticated via "remember me"
		 */
		default AuthorizedUrlMatcher rememberMe() {
			return access(AuthenticatedAuthorizationManager.rememberMe());
		}

		/**
		 * @return a rule requiring the caller be anonymous, i.e. not authenticated at all
		 */
		default AuthorizedUrlMatcher anonymous() {
			return access(AuthenticatedAuthorizationManager.anonymous());
		}

		/**
		 * @param manager any {@link AuthorizationManager} - an escape hatch for anything the named
		 *            convenience methods above don't cover, such as
		 *            {@code (auth, ctx) -> new AuthorizationDecision(Context.hasPrivilege("X"))} for exact
		 *            {@code Context.hasPrivilege(String)} parity
		 * @return a rule requiring {@code manager} to grant access
		 */
		default AuthorizedUrlMatcher access(AuthorizationManager<RequestAuthorizationContext> manager) {
			RequestMatcher self = this;
			return new AuthorizedUrlMatcher() {

				@Override
				public RequestMatcher getRequestMatcher() {
					return self;
				}

				@Override
				public AuthorizationManager<RequestAuthorizationContext> getAuthorizationManager() {
					return manager;
				}
			};
		}
	}
}
