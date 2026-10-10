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

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.hibernate.proxy.HibernateProxy;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

/**
 * Lets {@code @PreAuthorize}/{@code @PostAuthorize}/{@code @PostFilter} expressions check OpenMRS
 * privileges, e.g. {@code @PostAuthorize("hasPermission(returnObject, 'Get Providers')")}. Spring
 * Security's method security is the preferred way to enforce authorization as of 3.0.0, superseding
 * the deprecated {@link org.openmrs.annotation.Authorized} (see
 * {@code doc/AUTHORIZATION_MIGRATION.md}). It delegates to {@link Context#hasPrivilege(String)},
 * the same method {@code AuthorizationAdvice} uses, so both mechanisms decide a privilege
 * identically - superuser status, the implicit Anonymous/Authenticated roles, proxy privileges and
 * the {@code Daemon} bypass included, none of which a flat authority set can express (see
 * {@link OpenmrsAuthenticationToken#getAuthorities()}).
 * <p>
 * The built-in {@code hasAuthority('&lt;privilege&gt;')} resolves the same way, through
 * {@link OpenmrsAuthorizationManagerFactory}, so for a bare privilege check neither form is wrong.
 * Use {@code hasAuthority} for one, and {@code hasPermission} when the expression names what is
 * being accessed ({@code returnObject}, {@code filterObject}, {@code #someArg}), since only this
 * form is handed a target; {@code hasPermission(null, '&lt;privilege&gt;')} says the same thing as
 * {@code hasAuthority} the long way round.
 * <p>
 * What the target adds: when a call names one -
 * {@code hasPermission(targetDomainObject, permission)} or
 * {@code hasPermission(targetId, targetType, permission)} - every registered
 * {@link DomainObjectAuthorizationRule} whose {@link DomainObjectAuthorizationRule#getTargetType()}
 * the target is assignable to must authorize it too, on top of the privilege
 * (most-restrictive-wins). So a rule for {@code Person.class} covers a {@code Patient}; the reverse
 * cannot hold, since where only a supertype is known there is nothing to say the value is the
 * subtype. Which rules cover a type is derived on first sight and cached, not re-derived per call,
 * since this may run once per element of a {@code @PostFilter}ed collection.
 * <p>
 * In {@code hasPermission(targetId, targetType, permission)} the type is a name, resolved as a
 * fully-qualified class name when it contains a dot and as a class in {@code org.openmrs} otherwise
 * - so {@code 'Patient'} and {@code 'org.openmrs.hl7.HL7InQueue'} both work - and the rule is
 * handed the resolved {@link Class}, since an id alone does not say what it identifies.
 * <p>
 * Either overload takes a {@link Collection} as a target per element rather than as one target, and
 * all of the elements have to authorize. They differ in what each element is judged as, which
 * follows from what the expression said: with no type named, on the element's own runtime type;
 * with one named, on that type, since a collection of ids carries nothing to infer a type from - a
 * {@code List<Integer>} of patient ids is judged as {@code 'Patient'} because
 * {@code hasPermission(#patientIds, 'Patient', ...)} says so, where the same list handed to the
 * two-argument form would resolve rules for {@code Integer} and reach no rule for a patient at all.
 * This is why naming a collection of ids needs the type: not naming it is not a laxer check, it is
 * no check.
 * <p>
 * Judging per element is how a search method offers a rule the objects a query is restricted to -
 * {@code hasPermission(#whom, 'Get Observations')} on
 * {@code getObservations(List<Person> whom, ...)} - and in the two-argument form it does so without
 * the caller's collection having to be {@link Serializable}, which the id-and-type overload binds
 * its target to and a {@code List.subList(...)} view is not. Core registers no rules, so a target
 * no rule covers is authorized on the privilege alone - this is purely additive for modules needing
 * per-instance access control.
 * <p>
 * A plain predicate: a missing privilege, or a rule that denies, returns {@code false}, never
 * throws, because SpEL composes these - {@code hasPermission(...) or hasPermission(...)} has to
 * reach its second operand, and {@code @PreFilter}/{@code @PostFilter} evaluate once per element
 * and want a verdict. Throwing would stop the {@code or} short and make a filter pass everything or
 * fail outright. Naming the privilege in a denial therefore happens one level out, in
 * {@link PrivilegeNamingAuthorizationManager}, from what {@link MissingPrivilegeRecorder} collected
 * - only if the whole expression denied. A denial from a rule records nothing and so keeps the
 * generic message, correctly: every privilege the expression named was in fact held.
 * <p>
 * A no-value {@code @Authorized} needs nothing here: {@code @PreAuthorize("isAuthenticated()")} is
 * faithful, since {@link OpenmrsAuthenticationToken#isAuthenticated()} covers the same
 * Daemon-thread and proxy-privilege cases.
 *
 * @since 3.0.0
 */
@Component
public class OpenmrsPermissionEvaluator implements PermissionEvaluator {

	private static final Logger log = LoggerFactory.getLogger(OpenmrsPermissionEvaluator.class);

	/** Package a {@code targetType} naming no package is taken to be in. */
	private static final String OPENMRS_PACKAGE = "org.openmrs.";

	/**
	 * Sentinel for a {@code targetType} name that names no loadable class, so it is only logged once.
	 */
	private static final Class<?> UNRESOLVED = Void.class;

	private final List<DomainObjectAuthorizationRule> rules;

	/**
	 * Which rules apply to a type, derived on first sight and kept - {@code hasPermission(...)} may run
	 * once per element of a {@code @PostFilter}ed collection, so this must not be an assignability scan
	 * per call. Bounded by the number of types actually named.
	 */
	private final Map<Class<?>, List<DomainObjectAuthorizationRule>> rulesByClass = new ConcurrentHashMap<>();

	/** Resolved {@code targetType} names, including failures as {@link #UNRESOLVED}. */
	private final Map<String, Class<?>> classesByName = new ConcurrentHashMap<>();

	/**
	 * Id types already warned about, so a non-{@link Serializable} id is reported once, not per call.
	 */
	private final Set<Class<?>> unserializableIds = ConcurrentHashMap.newKeySet();

	@Autowired
	public OpenmrsPermissionEvaluator(List<DomainObjectAuthorizationRule> rules) {
		this.rules = List.copyOf(rules);
	}

	@Override
	public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
		if (!holdsPrivilege(permission)) {
			return false;
		}
		if (targetDomainObject == null) {
			return true;
		}

		if (targetDomainObject instanceof Collection<?> targets) {
			for (Object target : targets) {
				if (target != null && !authorizedByRules(authentication, target, permission)) {
					return false;
				}
			}
			return true;
		}

		return authorizedByRules(authentication, targetDomainObject, permission);
	}

	/**
	 * Every rule whose target type {@code target} is an instance of must authorize it -
	 * most-restrictive-wins. A type no rule covers is authorized, which is what keeps this extension
	 * point additive.
	 */
	private boolean authorizedByRules(Authentication authentication, Object target, Object permission) {
		for (DomainObjectAuthorizationRule rule : rulesFor(targetClassOf(target))) {
			if (!rule.isAuthorized(authentication, target, permission)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * The rules covering {@code type} - those declaring it or any of its supertypes. Interfaces are not
	 * walked: a rule for {@code OpenmrsObject} covering every domain object would be a far broader
	 * statement than a rule for a class, and is not what {@link DomainObjectAuthorizationRule}
	 * promises.
	 */
	private List<DomainObjectAuthorizationRule> rulesFor(Class<?> type) {
		if (type == null || rules.isEmpty()) {
			return List.of();
		}

		return rulesByClass.computeIfAbsent(type, resolved -> rules.stream()
		        .filter(rule -> rule.getTargetType() != null && rule.getTargetType().isAssignableFrom(resolved)).toList());
	}

	/**
	 * The class a {@code targetType} name refers to: taken as fully qualified when it contains a dot,
	 * otherwise as a class in {@code org.openmrs}. Loaded through {@link OpenmrsClassLoader} so a
	 * module can name its own types. A name that resolves to nothing is logged once and leaves the call
	 * authorized on the privilege alone - this is a predicate that must not throw (see the class
	 * javadoc), and a rule can only ever restrict, never grant.
	 */
	private Class<?> resolveTargetType(String targetType) {
		if (targetType == null) {
			return null;
		}

		Class<?> resolved = classesByName.computeIfAbsent(targetType, name -> {
			String qualified = name.indexOf('.') >= 0 ? name : OPENMRS_PACKAGE + name;
			try {
				return OpenmrsClassLoader.getInstance().loadClass(qualified);
			} catch (ClassNotFoundException | LinkageError e) {
				log.warn("No class '{}' for the targetType '{}' named by a hasPermission(...) expression, so no "
				        + "DomainObjectAuthorizationRule can be matched to it",
				    qualified, name, e);
				return UNRESOLVED;
			}
		});

		return resolved == UNRESOLVED ? null : resolved;
	}

	@Override
	public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType,
	        Object permission) {
		if (!holdsPrivilege(permission)) {
			return false;
		}
		if (targetId == null) {
			return true;
		}

		Class<?> resolved = resolveTargetType(targetType);
		if (targetId instanceof Collection<?> targetIds) {
			for (Object id : targetIds) {
				if (id != null && !authorizedByRules(authentication, id, resolved, permission)) {
					return false;
				}
			}
			return true;
		}

		return authorizedByRules(authentication, targetId, resolved, permission);
	}

	/**
	 * Every rule covering {@code type} must authorize the object {@code id} identifies - the
	 * id-and-type counterpart of {@link #authorizedByRules(Authentication, Object, Object)}, and like
	 * it most-restrictive-wins with an uncovered type authorized.
	 * <p>
	 * An {@code id} that is not {@link Serializable} cannot be handed to
	 * {@link DomainObjectAuthorizationRule#isAuthorized(Authentication, Serializable, Class, Object)}
	 * at all, so it is logged once per type and authorized on the privilege alone - the same way an
	 * unresolvable {@code targetType} is (see {@link #resolveTargetType(String)}), since this is a
	 * predicate that must not throw and a rule can only ever restrict, never grant.
	 */
	private boolean authorizedByRules(Authentication authentication, Object id, Class<?> type, Object permission) {
		List<DomainObjectAuthorizationRule> applicable = rulesFor(type);
		if (applicable.isEmpty()) {
			return true;
		}

		if (!(id instanceof Serializable serializableId)) {
			if (unserializableIds.add(id.getClass())) {
				log.warn(
				    "A hasPermission(targetId, '{}', ...) expression named an id of type {}, which is not "
				            + "Serializable and so cannot be passed to a DomainObjectAuthorizationRule; the call is "
				            + "authorized on the privilege alone",
				    type != null ? type.getName() : null, id.getClass().getName());
			}
			return true;
		}

		for (DomainObjectAuthorizationRule rule : applicable) {
			if (!rule.isAuthorized(authentication, serializableId, type, permission)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * The entity class behind {@code target}, unwrapping a Hibernate or Spring AOP proxy so a rule
	 * registered for {@code Obs} still matches a lazily loaded or
	 * {@code @AuthorizeReturnObject}-proxied one. Reads the persistent class rather than the
	 * implementation: resolving the type must not initialize a lazy proxy, because this runs once per
	 * element of a {@code @PostFilter}ed collection.
	 */
	private static Class<?> targetClassOf(Object target) {
		if (target instanceof HibernateProxy hibernateProxy) {
			return hibernateProxy.getHibernateLazyInitializer().getPersistentClass();
		}

		return ClassUtils.getUserClass(AopProxyUtils.ultimateTargetClass(target));
	}

	/**
	 * The privilege half of the check, shared by both overloads: true if the current user holds
	 * {@code permission}, otherwise false with the name handed to {@link MissingPrivilegeRecorder} so a
	 * denial can report it (see the class javadoc). Resolved through {@link FilterPassPrivilegeCache}
	 * so a {@code @PostFilter} resolves its privilege once per pass rather than once per element;
	 * recording stays outside that cache, and is a no-op during a filter pass anyway.
	 */
	private boolean holdsPrivilege(Object permission) {
		if (permission == null) {
			return false;
		}

		String privilege = permission.toString();
		if (FilterPassPrivilegeCache.holdsPrivilege(privilege, PrivilegeResolution::holdsPrivilege)) {
			return true;
		}

		MissingPrivilegeRecorder.record(privilege);
		return false;
	}
}
