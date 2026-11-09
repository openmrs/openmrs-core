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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.openmrs.security.OpenmrsAuthorizationManagerFactory;
import org.springframework.security.authorization.AuthenticatedAuthorizationManager;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.SingleResultAuthorizationManager;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * Lets core or a module require an {@link AuthorizationManager} decision to reach a URL pattern,
 * built from Spring Security's own vocabulary ({@code hasAuthority}, {@code hasRole},
 * {@code AuthorizationManagers.allOf(...)}, or any hand-written manager).
 * <p>
 * A URL rule narrows access; it does not replace a check on the service method.
 * {@link OpenmrsAuthorizationFilter} enforces these after {@code ModuleFilter}, so a module filter
 * that completes a request without continuing the chain skips them.
 * <p>
 * {@link #requestMatchers(String...)} is the entry point; a module bundles its rules into one
 * {@link AuthorizedUrlMatchers} bean. <pre>
 * AuthorizedUrlMatcher.requestMatchers("/ms/myModule/admin/**").hasAuthority("Manage My Module")
 * </pre> A pattern under a module prefix is expanded to every path that reaches the same resource -
 * the one above covers all four a module servlet answers on, and must name the module id and the
 * servlet name both - see {@link #equivalentPaths(String)}.
 * <p>
 * Unlike {@code HttpSecurity.authorizeHttpRequests(...)}, rules are self-contained rather than kept
 * in a shared registry, so a module cannot crash startup or have its rule silently dropped by an
 * overlap. An unmatched URL is permitted; where several rules match, all must agree, so the
 * combination is never looser than its strictest rule (see {@link OpenmrsAuthorizationManager}).
 *
 * @since 3.0.0
 */
public interface AuthorizedUrlMatcher {

	/** Both {@code web.xml} mappings of {@code ModuleServlet} - see {@link #equivalentPaths(String)} */
	List<String> MODULE_SERVLET_PREFIXES = List.of("/moduleServlet/", "/ms/");

	/** The single {@code web.xml} mapping of {@code ModuleResourcesServlet} */
	String MODULE_RESOURCES_PREFIX = "/moduleResources/";

	/** The path-based {@code web.xml} mapping of the {@code openmrs} {@code DispatcherServlet} */
	String WS_PREFIX_PATH = "/ws";

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
	 * @param patterns one or more {@code PathPattern} path patterns (e.g.
	 *            {@code "/ms/myModule/admin/**"}), relative to the servlet context; a request matches
	 *            if any one of them does, so every path that reaches the resource being guarded has to
	 *            be named - which {@link #equivalentPaths(String)} does for the module prefixes, whose
	 *            extra shapes are easy to miss. Parsed by {@code PathPatternParser}, so {@code **} may
	 *            appear only once and only at the start or end of a pattern, and URI template variables
	 *            ({@code "/a/{id}/b"}) are supported
	 * @return a {@link Rule} for the given patterns, ready to be turned into a
	 *         {@link AuthorizedUrlMatcher} by naming the authorization check that governs it
	 */
	static Rule requestMatchers(String... patterns) {
		List<String> expanded = Arrays.stream(patterns).flatMap(AuthorizedUrlMatcher::equivalentPaths).distinct().toList();

		RequestMatcher matcher = (expanded.size() == 1) ? PathPatternRequestMatcher.pathPattern(expanded.get(0))
		        : new OrRequestMatcher(expanded.stream().map(PathPatternRequestMatcher::pathPattern).toList());
		return matcher::matches;
	}

	/**
	 * Expands a pattern into every path that reaches the same resource, since a pattern matches the raw
	 * request URI and naming one shape leaves the others unguarded.
	 * <p>
	 * A {@code /moduleServlet/} or {@code /ms/} pattern must name both the module id and the servlet
	 * name - {@code "/ms/myModule/admin/**"} - and is expanded to the four paths that reach that
	 * servlet.
	 * <p>
	 * A {@code /moduleResources/} pattern with a dotted module id is expanded to every spelling of that
	 * id: {@code ModuleUtil.getModuleForPath} turns each slash into a dot before walking the id back,
	 * so {@code x.y.z} serves the same file at {@code x.y.z}, {@code x.y/z}, {@code x/y.z} and
	 * {@code x/y/z}, and {@code getPathForResource} strips the same length from each. Write the id
	 * dotted: a spelling that already contains slashes cannot be expanded, since nothing marks where
	 * the id ends and the resource path begins.
	 * <p>
	 * Anything else is a path the {@code openmrs} {@code DispatcherServlet} serves, and {@code web.xml}
	 * maps that servlet at {@code /ws/*} as well as by extension, so every such pattern is expanded
	 * with a {@code /ws}-prefixed twin: Spring MVC matches the path after {@code /ws}, which is how
	 * webservices.rest's {@code /rest/v1/...} mappings answer at {@code /ws/rest/v1/...}, while a rule
	 * matches the path after the context path - so {@code /ws} in front of a guarded MVC path would
	 * otherwise reach the same controller unguarded. A pattern starting with {@code /**} is left alone:
	 * it already covers {@code /ws}, and {@code **} cannot appear mid-pattern.
	 * <p>
	 * The reverse direction is not expanded. A pattern written under {@code /ws/} is left as it is,
	 * even though the extension mappings can reach the same handler without the prefix - a rule on
	 * {@code /ws/rest/v1/myModule/**} does not cover {@code /rest/v1/myModule/run.htm}. That needs a
	 * handler whose last path variable accepts the {@code .htm} suffix, so name both patterns when
	 * guarding one.
	 *
	 * @param pattern a path pattern
	 * @return {@code pattern} and every other pattern reaching the same resource
	 * @throws IllegalArgumentException if a module servlet pattern does not name both a module id and a
	 *             servlet name. Rules are built in {@code @Bean} methods, so this fails startup rather
	 *             than leaving a rule that looks right and guards one of four paths
	 */
	static Stream<String> equivalentPaths(String pattern) {
		for (String prefix : MODULE_SERVLET_PREFIXES) {
			if (pattern.startsWith(prefix)) {
				return moduleServletPaths(pattern, prefix);
			}
		}

		if (pattern.startsWith(MODULE_RESOURCES_PREFIX)) {
			return moduleResourcesPaths(pattern);
		}

		return dispatcherPaths(pattern);
	}

	private static Stream<String> moduleServletPaths(String pattern, String prefix) {
		String path = pattern.substring(prefix.length());
		int idEnd = path.indexOf('/');
		String moduleId = (idEnd < 0) ? path : path.substring(0, idEnd);

		String rest = (idEnd < 0) ? "" : path.substring(idEnd + 1);
		int nameEnd = rest.indexOf('/');
		String servletName = (nameEnd < 0) ? rest : rest.substring(0, nameEnd);
		String subPath = (nameEnd < 0) ? "" : rest.substring(nameEnd);

		if (!isLiteralSegment(moduleId) || !isLiteralSegment(servletName)) {
			throw new IllegalArgumentException("A module servlet pattern must name both the module id and the servlet "
			        + "name, as in \"/ms/myModule/admin/**\" - the servlet name is what ModuleServlet resolves on, and "
			        + "naming it second is what tells this apart from a module id. Got: " + pattern);
		}

		return MODULE_SERVLET_PREFIXES.stream()
		        .flatMap(p -> Stream.of(p + servletName + subPath, p + "*/" + servletName + subPath));
	}

	private static Stream<String> moduleResourcesPaths(String pattern) {
		String path = pattern.substring(MODULE_RESOURCES_PREFIX.length());
		int end = path.indexOf('/');
		String moduleId = (end < 0) ? path : path.substring(0, end);
		if (moduleId.indexOf('.') < 0) {
			return Stream.of(pattern);
		}

		String resourcePath = (end < 0) ? "" : path.substring(end);
		String[] parts = moduleId.split("\\.", -1);
		List<String> spellings = new ArrayList<>(List.of(parts[0]));
		for (int i = 1; i < parts.length; i++) {
			List<String> grown = new ArrayList<>(spellings.size() * 2);
			for (String spelling : spellings) {
				grown.add(spelling + '.' + parts[i]);
				grown.add(spelling + '/' + parts[i]);
			}
			spellings = grown;
		}

		return spellings.stream().map(spelling -> MODULE_RESOURCES_PREFIX + spelling + resourcePath);
	}

	private static Stream<String> dispatcherPaths(String pattern) {
		boolean alreadyCovered = !pattern.startsWith("/") || pattern.startsWith("/**") || pattern.equals(WS_PREFIX_PATH)
		        || pattern.startsWith(WS_PREFIX_PATH + "/");

		return alreadyCovered ? Stream.of(pattern) : Stream.of(pattern, WS_PREFIX_PATH + pattern);
	}

	/**
	 * @return whether {@code segment} is a literal name rather than empty, a wildcard or a template
	 *         variable - neither of the latter can identify a module or a servlet
	 */
	private static boolean isLiteralSegment(String segment) {
		return !segment.isEmpty() && segment.indexOf('*') < 0 && segment.indexOf('{') < 0;
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
		 * @param authority the privilege name required, resolved through
		 *            {@code Context.hasPrivilege(String)} (see {@link #factory()})
		 * @return a rule requiring the current user hold {@code authority}
		 */
		default AuthorizedUrlMatcher hasAuthority(String authority) {
			return access(factory().hasAuthority(authority));
		}

		/**
		 * @param authorities the privilege names, any one of which suffices
		 * @return a rule requiring the current user hold at least one of {@code authorities}
		 */
		default AuthorizedUrlMatcher hasAnyAuthority(String... authorities) {
			return access(factory().hasAnyAuthority(authorities));
		}

		/**
		 * @param authorities the privilege names, every one of which is required
		 * @return a rule requiring the current user hold every one of {@code authorities}
		 */
		default AuthorizedUrlMatcher hasAllAuthorities(String... authorities) {
			return access(factory().hasAllAuthorities(authorities));
		}

		/**
		 * @param role the role name required, with or without a {@code ROLE_} prefix, resolved through
		 *            {@code User#hasRole(String)} (see {@link #factory()})
		 * @return a rule requiring the current user hold {@code role}
		 */
		default AuthorizedUrlMatcher hasRole(String role) {
			return access(factory().hasRole(role));
		}

		/**
		 * @param roles the role names, with or without a {@code ROLE_} prefix, any one of which suffices
		 * @return a rule requiring the current user hold at least one of {@code roles}
		 */
		default AuthorizedUrlMatcher hasAnyRole(String... roles) {
			return access(factory().hasAnyRole(roles));
		}

		/**
		 * @param roles the role names, with or without a {@code ROLE_} prefix, every one of which is
		 *            required
		 * @return a rule requiring the current user hold every one of {@code roles}
		 */
		default AuthorizedUrlMatcher hasAllRoles(String... roles) {
			return access(factory().hasAllRoles(roles));
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
		 * @return the OpenMRS semantics behind the named checks above, shared with
		 *         {@code @PreAuthorize}/{@code @PostAuthorize} (see
		 *         {@code OpenmrsSecurityConfig#methodSecurityExpressionHandler}) so a privilege or role
		 *         name means the same thing whether it guards a URL or a service method. Built per call,
		 *         which happens while rules are being assembled at startup, not per request.
		 */
		private static OpenmrsAuthorizationManagerFactory<RequestAuthorizationContext> factory() {
			return new OpenmrsAuthorizationManagerFactory<>();
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
