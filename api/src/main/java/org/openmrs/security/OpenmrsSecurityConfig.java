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
 * ({@code @PreAuthorize}/{@code @PostAuthorize}) authentication and authorization mechanisms - the
 * {@link AuthenticationManager} and method security.
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
	 * Enables Spring Security's {@code @PreAuthorize}/{@code @PostAuthorize} method security alongside
	 * the existing {@code @Authorized}/{@code AuthorizationAdvice} advisor chain (see
	 * {@link org.openmrs.aop.AOPConfig}). Its pointcut only matches methods actually carrying a Spring
	 * Security method-security annotation, which is strictly narrower than
	 * {@code AuthorizationAdvice}'s "any {@code @Service} bean" pointcut, so the two coexist without
	 * interference on the same proxy - a method is guarded by one or the other, never both.
	 * <p>
	 * {@code @EnableMethodSecurity} has no direct {@code order} attribute; it exposes an {@code offset}
	 * added to each of its interceptors' default orders
	 * ({@link org.springframework.security.authorization.method.AuthorizationInterceptorsOrder}), and
	 * that offset has to move <em>every</em> one of them - not just {@code PRE_AUTHORIZE} - ahead of
	 * {@code authorizationAdvisor} (order 1 in {@link org.openmrs.aop.AOPConfig}), caching (order 4),
	 * and transactions (order 5). A smaller offset that only clears {@code PRE_AUTHORIZE} still leaves
	 * {@code POST_AUTHORIZE}/{@code POST_FILTER} numerically behind caching, i.e. nested inside it: a
	 * cached hit then returns before either ever runs, handing the first caller's result to every later
	 * caller with the same arguments regardless of their own privileges. {@code offset = -700} puts the
	 * whole family (-600 to -100) ahead of all three, the same relative position they hold in a stock
	 * Spring application that never touches this offset - an unauthorized {@code @PreAuthorize} call is
	 * rejected before a transaction is opened, matching {@code @Authorized}'s existing guarantee, and a
	 * denied {@code @PostAuthorize}/{@code @PostFilter} is evaluated against the real, uncached result.
	 * <p>
	 * One trade-off comes with running authorization outside the transaction boundary rather than
	 * inside it: a write that a {@code @PostAuthorize} check then denies is no longer rolled back,
	 * since the transaction advisor's own commit (order 5) runs, on the way back out, before this
	 * offset's interceptors get a chance to deny anything. {@code @PreAuthorize} is unaffected - it
	 * still runs before the transaction is opened at all.
	 * <p>
	 * Declared {@code static} per Spring Security's guidance for infrastructure beans consumed by
	 * {@code @EnableMethodSecurity}, so it is instantiated early enough to configure the method
	 * security interceptor without triggering premature proxying of other beans.
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
	 * Wraps the {@code @PreAuthorize} {@link AuthorizationManager} in a
	 * {@link PrivilegeNamingAuthorizationManager}, so a denial caused by a missing OpenMRS privilege
	 * names that privilege instead of reading "Access Denied" (webservices.rest's error message, and
	 * the privilege-request alert legacyui builds from it, both come straight from
	 * {@code getMessage()}).
	 * <p>
	 * {@code @EnableMethodSecurity} exposes this as its customization point for the pre/post
	 * authorization managers: {@code PrePostMethodSecurityConfiguration} takes an
	 * {@code ObjectProvider<ObjectPostProcessor<AuthorizationManager<MethodInvocation>>>} and applies
	 * it to the {@code PreAuthorizeAuthorizationManager} it builds. Returning a different instance from
	 * {@code postProcess(...)} is how that point is meant to be used; Spring Security's own
	 * {@code MethodObservationConfiguration} decorates the same manager the same way, which is also why
	 * the unchecked cast below is safe (the erased return type is {@code AuthorizationManager}, which
	 * is all the caller ever assigns it to).
	 * <p>
	 * {@code @Primary} is required rather than decorative. That provider is resolved with
	 * {@code getIfUnique(...)}, and {@code MethodObservationConfiguration} - imported automatically
	 * because {@code io.micrometer.observation.ObservationRegistry} is on the classpath - already
	 * registers a bean of this exact type, so without a primary candidate the provider would resolve to
	 * nothing and silently fall back to {@code ObjectPostProcessor::identity}, leaving this wrapper
	 * uninstalled with no error anywhere. The cost is that Micrometer's own decoration of the
	 * authorization managers no longer applies; it is inert here in any case, since it only wraps
	 * anything when an {@code ObservationRegistry} bean exists, and OpenMRS registers none. Anyone
	 * wiring one up for authorization observations needs to revisit this method.
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
