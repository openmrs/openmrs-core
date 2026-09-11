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
 * Bundles every {@link AuthorizedUrlMatcher} a module needs to protect its own URL patterns into
 * one bean, so a module registers a single bean instead of one {@link AuthorizedUrlMatcher} bean
 * per pattern. {@link WebSecurityConfig} collects {@code List<AuthorizedUrlMatchers>} - an
 * ordinary, single-level list, one element per contributing bean - the same way {@code AOPConfig}
 * collects module-contributed {@code Advisor} beans, then flattens each bean's own list together.
 * <p>
 * {@link #builder()} is the only way to build one, chaining {@code requestMatchers(...)} with the
 * named check that governs it, same as {@link AuthorizedUrlMatcher.Rule}'s own vocabulary: <pre>
 * &#64;Bean
 * public AuthorizedUrlMatchers myModuleUrlRules() {
 *     return AuthorizedUrlMatchers.builder()
 *             .requestMatchers("/moduleServlet/myModule/admin/**").hasAuthority("Manage My Module")
 *             .requestMatchers("/moduleServlet/myModule/reports/**").hasAnyRole("Doctor", "Nurse")
 *             .build();
 * }
 * </pre> This is deliberately a concrete class with a private constructor, not a
 * {@code @FunctionalInterface} - so a plain lambda cannot bypass the builder and drift from its
 * vocabulary. Safe to chain this way, unlike {@code HttpSecurity.authorizeHttpRequests(...)}'s own
 * {@code Customizer}-based configuration (see {@link AuthorizedUrlMatcher}'s javadoc for why that
 * one is unsafe for modules to share): each module gets its own private {@link Builder} instance,
 * built and consumed entirely within its own {@code @Bean} method, never shared with or mutated by
 * another module - there is no {@code anyRequest()}-once-ever registry and no first-match-wins
 * ordering to fight over.
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
		 * @param patterns one or more Ant-style path patterns, as accepted by
		 *            {@link AuthorizedUrlMatcher#requestMatchers(String...)}
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

			public Builder fullyAuthenticated() {
				return add(this.rule.fullyAuthenticated());
			}

			public Builder rememberMe() {
				return add(this.rule.rememberMe());
			}

			public Builder anonymous() {
				return add(this.rule.anonymous());
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
