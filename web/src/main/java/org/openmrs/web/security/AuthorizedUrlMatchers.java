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
import java.util.List;

import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

/**
 * Bundles every {@link AuthorizedUrlMatcher} a module needs into one bean, so a module registers
 * one bean rather than one per pattern. {@link WebSecurityConfig} collects
 * {@code List<AuthorizedUrlMatchers>} - one element per contributing bean, as {@code AOPConfig}
 * collects module {@code Advisor} beans - and flattens them.
 * <p>
 * {@link #builder()} is the only way to build one: <pre>
 * &#64;Bean
 * public AuthorizedUrlMatchers myModuleUrlRules() {
 *     return AuthorizedUrlMatchers.builder()
 *             // module id and servlet name both, or startup fails
 *             .requestMatchers("/ms/myModule/admin/**").hasAuthority("Manage My Module")
 *             .requestMatchers("/ms/myModule/reports/**").hasAnyRole("Doctor", "Nurse")
 *             .requestMatchers("/moduleResources/myModule/**").hasAuthority("Manage My Module")
 *             .requestMatchers("/module/myModule/config.form").hasAuthority("Manage My Module")
 *             .build();
 * }
 * </pre> Each is expanded to every path reaching the same resource, so one pattern is enough where
 * a module servlet answers on four, a dotted module id's resources on one per spelling of the id,
 * and the Spring MVC page on both {@code /module/myModule/config.form} and
 * {@code /ws/module/myModule/config.form} - {@code web.xml} maps the {@code openmrs}
 * {@code DispatcherServlet} at {@code /ws/*} too. See
 * {@link AuthorizedUrlMatcher#equivalentPaths(String)}.
 * <p>
 * Chaining is safe here, unlike {@code HttpSecurity.authorizeHttpRequests(...)} (see
 * {@link AuthorizedUrlMatcher}): each module builds and consumes its own {@link Builder} inside its
 * own {@code @Bean} method, with no shared registry to fight over.
 *
 * @since 3.0.0
 */
public final class AuthorizedUrlMatchers {

	private final List<AuthorizedUrlMatcher> rules;

	private AuthorizedUrlMatchers(List<AuthorizedUrlMatcher> rules) {
		this.rules = rules;
	}

	/**
	 * @return every {@link AuthorizedUrlMatcher} this bean contributes
	 */
	public List<AuthorizedUrlMatcher> getAuthorizedUrlMatchers() {
		return this.rules;
	}

	/**
	 * @return a new, private {@link Builder} - see this class's javadoc for why building and consuming
	 *         it entirely within one {@code @Bean} method is safe
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Accumulates {@link AuthorizedUrlMatcher} rules one {@code requestMatchers(...)} call at a time.
	 * Not reusable across multiple {@code @Bean} methods and not meant to be - see the enclosing
	 * class's javadoc.
	 */
	public static final class Builder {

		private final List<AuthorizedUrlMatcher> rules = new ArrayList<>();

		private Builder() {
		}

		/**
		 * @param patterns one or more {@code PathPattern} path patterns, as accepted by
		 *            {@link AuthorizedUrlMatcher#requestMatchers(String...)} - see there for the syntax
		 *            {@code PathPatternParser} does and does not allow
		 * @return a {@link RuleBuilder} for the given patterns, ready to be turned into a rule (added to
		 *         this builder) by naming the authorization check that governs it
		 */
		public RuleBuilder requestMatchers(String... patterns) {
			return new RuleBuilder(AuthorizedUrlMatcher.requestMatchers(patterns));
		}

		/**
		 * @return an {@link AuthorizedUrlMatchers} bundling every rule added so far
		 */
		public AuthorizedUrlMatchers build() {
			return new AuthorizedUrlMatchers(List.copyOf(this.rules));
		}

		/**
		 * Exposes the same named checks as {@link AuthorizedUrlMatcher.Rule} - delegating to them directly,
		 * so the two never disagree on what a given check means - except each one adds the finished rule to
		 * the enclosing {@link Builder} and returns it for further chaining, instead of returning the rule
		 * itself.
		 */
		public final class RuleBuilder {

			private final AuthorizedUrlMatcher.Rule rule;

			private RuleBuilder(AuthorizedUrlMatcher.Rule rule) {
				this.rule = rule;
			}

			public Builder permitAll() {
				return add(this.rule.permitAll());
			}

			public Builder denyAll() {
				return add(this.rule.denyAll());
			}

			public Builder hasAuthority(String authority) {
				return add(this.rule.hasAuthority(authority));
			}

			public Builder hasAnyAuthority(String... authorities) {
				return add(this.rule.hasAnyAuthority(authorities));
			}

			public Builder hasAllAuthorities(String... authorities) {
				return add(this.rule.hasAllAuthorities(authorities));
			}

			public Builder hasRole(String role) {
				return add(this.rule.hasRole(role));
			}

			public Builder hasAnyRole(String... roles) {
				return add(this.rule.hasAnyRole(roles));
			}

			public Builder hasAllRoles(String... roles) {
				return add(this.rule.hasAllRoles(roles));
			}

			public Builder authenticated() {
				return add(this.rule.authenticated());
			}

			/**
			 * @param manager any {@link AuthorizationManager} - an escape hatch for anything the named checks
			 *            above don't cover, mirroring {@link AuthorizedUrlMatcher.Rule#access}
			 */
			public Builder access(AuthorizationManager<RequestAuthorizationContext> manager) {
				return add(this.rule.access(manager));
			}

			private Builder add(AuthorizedUrlMatcher rule) {
				Builder.this.rules.add(rule);
				return Builder.this;
			}
		}
	}
}
