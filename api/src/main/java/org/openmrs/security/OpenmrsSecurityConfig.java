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

import java.util.List;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.method.MethodInvocationResult;
import org.springframework.security.config.ObjectPostProcessor;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Registers the core Spring Security beans that back OpenMRS's coexisting legacy
 * ({@link org.openmrs.api.context.Context}/{@code @Authorized}) and Spring Security
 * ({@code @PreAuthorize}/{@code @PostAuthorize}/{@code @PostFilter}) authentication and
 * authorization mechanisms - the {@link AuthenticationManager} and method security.
 * <p>
 * The Spring Security half is the preferred mechanism for new code as of 3.0.0;
 * {@link org.openmrs.annotation.Authorized} is deprecated and its usages are being converted
 * gradually (see {@code doc/AUTHORIZATION_MIGRATION.md}), which is why both are wired here.
 *
 * @since 3.0.0
 */
@Configuration
@EnableMethodSecurity(offset = -700, prePostEnabled = true)
public class OpenmrsSecurityConfig {

	/**
	 * Used by
	 * {@link org.openmrs.api.context.UserContext#authenticate(org.openmrs.api.context.Credentials)} as
	 * the real entry point for authentication, wrapping
	 * {@link AuthenticationSchemeAuthenticationProvider}.
	 */
	@Bean(name = "authenticationManager")
	public AuthenticationManager authenticationManager(AuthenticationSchemeAuthenticationProvider provider) {
		return new ProviderManager(List.<AuthenticationProvider> of(provider));
	}

	/**
	 * Enables {@code @PreAuthorize}/{@code @PostAuthorize} method security alongside the
	 * {@code @Authorized}/{@code AuthorizationAdvice} advisor (see {@link org.openmrs.aop.AOPConfig}).
	 * Its pointcut matches only methods carrying a Spring Security annotation, narrower than that
	 * advice's "any {@code @Service} bean", so the two coexist on one proxy.
	 * <p>
	 * {@code @EnableMethodSecurity} has no {@code order}, only an {@code offset} added to every one of
	 * its interceptors' default orders, and all of them - not just {@code PRE_AUTHORIZE} - have to
	 * clear {@code authorizationAdvisor} (order 1), caching (4) and transactions (5). A smaller offset
	 * leaves {@code POST_AUTHORIZE}/{@code POST_FILTER} nested inside caching, where a cache hit
	 * returns before either runs and serves the first caller's result to everyone.
	 * {@code offset = -700} puts the whole family ahead of all three, as in a stock Spring application.
	 * <p>
	 * Two consequences. {@code @PostFilter} sits outside the cache interceptor, which is what filters a
	 * cached collection at all - and obliges a {@code @PostFilter}ed {@code @Cacheable} method to key
	 * by the caller, since filtering is in place (see {@link UserKeyGenerator}). And authorization now
	 * runs outside the transaction boundary, so a write denied by {@code @PostAuthorize} is not rolled
	 * back; {@code @PreAuthorize} still runs before the transaction opens.
	 * <p>
	 * Declared {@code static} per Spring Security's guidance for infrastructure beans, so it is built
	 * early enough not to trigger premature proxying.
	 */
	@Bean
	static MethodSecurityExpressionHandler methodSecurityExpressionHandler(OpenmrsPermissionEvaluator permissionEvaluator) {
		DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
		handler.setPermissionEvaluator(permissionEvaluator);
		// Makes the built-in hasAuthority(...)/hasRole(...) resolve through Context.hasPrivilege(String)
		// and User#hasRole(String), so they agree with @Authorized. Set here rather than published as
		// an AuthorizationManagerFactory bean on purpose - PrePostMethodSecurityConfiguration applies
		// such a bean to its own internal expression handler, which this handler has already replaced
		// on the interceptors, so a bean would be silently ignored.
		handler.setAuthorizationManagerFactory(new OpenmrsAuthorizationManagerFactory<>());
		return handler;
	}

	/**
	 * Keys a cache entry by the authenticated user, ready to be named from
	 * {@code @Cacheable(keyGenerator = ...)} - which a {@code @Cacheable} method that is also
	 * {@code @PostFilter}ed needs to do one way or another, see {@link UserKeyGenerator} for why.
	 */
	@Bean(name = UserKeyGenerator.BEAN_NAME)
	public UserKeyGenerator userKeyGenerator() {
		return new UserKeyGenerator();
	}

	/**
	 * Wraps the {@code @PreAuthorize} {@link AuthorizationManager} in a
	 * {@link PrivilegeNamingAuthorizationManager}, so a denial names the missing privilege instead of
	 * reading "Access Denied" - webservices.rest's error message and legacyui's privilege-request alert
	 * both come from {@code getMessage()}.
	 * <p>
	 * {@code @EnableMethodSecurity} intends this {@code ObjectPostProcessor} as the customization
	 * point, which is also why the unchecked cast below is safe. {@code @Primary} is load-bearing, not
	 * decorative: the provider is resolved with {@code getIfUnique(...)} and
	 * {@code MethodObservationConfiguration} - auto-imported because Micrometer is on the classpath -
	 * already registers a bean of this type, so without it the provider would resolve to nothing and
	 * fall back to {@code ObjectPostProcessor::identity}, leaving this wrapper uninstalled with no
	 * error. The cost is Micrometer's own decoration, which is inert until an
	 * {@code ObservationRegistry} bean exists; anyone adding one should revisit this method.
	 */
	@Primary
	@Bean
	static ObjectPostProcessor<AuthorizationManager<MethodInvocation>> preAuthorizeAuthorizationManagerPostProcessor() {
		return privilegeNamingPostProcessor();
	}

	/**
	 * The {@code @PostAuthorize} counterpart of
	 * {@link #preAuthorizeAuthorizationManagerPostProcessor()}, so a {@code @PostAuthorize} denial
	 * names its missing privilege too. See that method for why this is {@code @Primary}.
	 */
	@Primary
	@Bean
	static ObjectPostProcessor<AuthorizationManager<MethodInvocationResult>> postAuthorizeAuthorizationManagerPostProcessor() {
		return privilegeNamingPostProcessor();
	}

	private static <T> ObjectPostProcessor<AuthorizationManager<T>> privilegeNamingPostProcessor() {
		return new ObjectPostProcessor<AuthorizationManager<T>>() {

			@Override
			@SuppressWarnings("unchecked")
			public <O extends AuthorizationManager<T>> O postProcess(O authorizationManager) {
				return (O) new PrivilegeNamingAuthorizationManager<>(authorizationManager);
			}
		};
	}
}
