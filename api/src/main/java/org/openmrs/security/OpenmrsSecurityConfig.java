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
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Registers the core Spring Security beans that back OpenMRS's coexisting legacy
 * ({@link org.openmrs.api.context.Context}/{@code @Authorized}) and Spring Security
 * ({@code @PreAuthorize}/{@code @PostAuthorize}) authentication and authorization mechanisms - the
 * {@link AuthenticationManager}, method security, and the password encoder.
 *
 * @since 3.0.0
 */
@Configuration
@EnableMethodSecurity(offset = -200, prePostEnabled = true)
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
	 * Enables Spring Security's {@code @PreAuthorize}/{@code @PostAuthorize} method security alongside
	 * the existing {@code @Authorized}/{@code AuthorizationAdvice} advisor chain (see
	 * {@link org.openmrs.aop.AOPConfig}). Its pointcut only matches methods actually carrying a Spring
	 * Security method-security annotation, which is strictly narrower than
	 * {@code AuthorizationAdvice}'s "any {@code @Service} bean" pointcut, so the two coexist without
	 * interference on the same proxy - a method is guarded by one or the other, never both.
	 * <p>
	 * {@code @EnableMethodSecurity} has no direct {@code order} attribute; it exposes an {@code offset}
	 * added to each of its interceptors' default orders
	 * ({@link org.springframework.security.authorization.method.AuthorizationInterceptorsOrder}). Its
	 * {@code PRE_AUTHORIZE} interceptor defaults to order 200; {@code offset = -200} places it at order
	 * 0, immediately before {@code authorizationAdvisor} (order 1 in
	 * {@link org.openmrs.aop.AOPConfig}), keeping it, like the rest of that chain, ahead of caching
	 * (order 4) and transactions (order 5) - an unauthorized {@code @PreAuthorize} call is rejected
	 * before a transaction is opened, matching {@code @Authorized}'s existing guarantee.
	 * <p>
	 * Declared {@code static} per Spring Security's guidance for infrastructure beans consumed by
	 * {@code @EnableMethodSecurity}, so it is instantiated early enough to configure the method
	 * security interceptor without triggering premature proxying of other beans.
	 */
	@Bean
	static MethodSecurityExpressionHandler methodSecurityExpressionHandler(OpenmrsPermissionEvaluator permissionEvaluator) {
		DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
		handler.setPermissionEvaluator(permissionEvaluator);
		return handler;
	}

	/**
	 * Registers the {@link PasswordEncoder} bean that {@link org.openmrs.util.Security} delegates to
	 * for any hash carrying a recognizable {@code {id}} prefix (e.g. {@code {bcrypt}...}). Hashes
	 * without a prefix are OpenMRS's pre-3.0.0 legacy format and are verified by
	 * {@link org.openmrs.util.Security} directly, unaffected by this bean:
	 * {@link org.openmrs.util.Security#hashMatches(String, String)} only ever calls into this bean for
	 * a hash that already carries a prefix, so this bean itself is never asked to verify a legacy one.
	 * <p>
	 * Backed by {@link OpenmrsDelegatingPasswordEncoder} rather than Spring's own
	 * {@code DelegatingPasswordEncoder}: functionally equivalent here, since the two differ only in
	 * what an encoder produces when {@code idForEncode} is empty (bare, unprefixed output here;
	 * Spring's always prefixes, even with an empty id), and {@code idForEncode} is not empty below -
	 * kept as this class rather than Spring's own so a future need for that no-prefix behavior (e.g. an
	 * installation that must keep writing bare hashes) is a configuration change here, not a rewrite.
	 * The {@code fallbackEncoder} argument therefore plays no role in this particular configuration.
	 * <p>
	 * "bcrypt" is both the sole registered algorithm and the default for new encodings, matching Spring
	 * Security's own recommended default.
	 */
	@Bean(name = "openmrsPasswordEncoder")
	public PasswordEncoder openmrsPasswordEncoder() {
		return new OpenmrsDelegatingPasswordEncoder("bcrypt", Map.of("bcrypt", new BCryptPasswordEncoder()),
		        new LegacyOpenmrsPasswordEncoder());
	}
}
