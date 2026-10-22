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

import org.springframework.security.core.Authentication;

/**
 * Extension point for object-level authorization, consulted by {@link OpenmrsPermissionEvaluator}
 * when a {@code @PreAuthorize}/{@code @PostAuthorize}/{@code @PostFilter} expression's
 * {@code hasPermission(...)} call names a target - e.g.
 * {@code @PreAuthorize("hasPermission(#patient, 'Get Patients')")} or
 * {@code @PostFilter("hasPermission(filterObject, 'Get Patients')")}.
 * <p>
 * Core has no built-in notion of per-instance access restriction (no care-setting or organizational
 * unit ACL model), so this interface exists purely for modules to plug one in. Implementations are
 * registered as Spring beans, and which of them apply to a given type is resolved once and cached
 * by {@link OpenmrsPermissionEvaluator} - {@code hasPermission(...)} may run once per element of a
 * large {@code @PostFilter}-ed collection, so this is not re-derived on every call. A target no
 * rule declares a supertype of is authorized by default, so adding this extension point never
 * restricts an existing {@code hasPermission(...)} call site that does not name a target, or names
 * a type outside every registered rule's reach.
 * <p>
 * Where more than one registered rule declares the same target type, every one of them must
 * authorize it - most-restrictive-wins, matching how {@code @Authorized}/{@code @PreAuthorize}
 * already combine with the underlying privilege check.
 * <p>
 * An expression may name a collection - {@code hasPermission(#whom, 'Get Observations')} on a
 * {@code getObservations(List<Person> whom, ...)}, so a rule can see which people a query is
 * restricted to. A rule is still only ever asked about one object: the collection is taken as a
 * target per element, and all of them have to authorize.
 * <p>
 * A rule is handed either the object or an id and type, depending on what the call site had: a
 * single-object read by id ({@code getPatient(Integer)}, {@code getPatientByUuid(String)}) names
 * its id, so the rule is consulted before the fetch and methods that never load the object at all
 * ({@code getEncountersByPatientId(Integer)}) can be covered too. The consequence is that a rule
 * needing more of the object than its id has to load it <em>without re-entering the method it is
 * authorizing</em>: calling {@code PatientService.getPatient(...)} from
 * {@link #isAuthorized(Authentication, Serializable, Class, Object)} re-triggers this same check
 * and recurses until the stack overflows, which surfaces as SpEL failing to evaluate the
 * expression. Read it through a DAO or the Hibernate session instead, or guard the re-entrant call
 * with a thread-local.
 * <p>
 * A rule is asked under one of two vocabularies, and both reach the same {@code isAuthorized}
 * methods. An annotation names the privilege its method requires, so a rule sees
 * {@code Get Patients} or {@code Edit Patients} and can tell a read from a write.
 * {@link Authorize}, which a presentation layer calls on its way to serializing what a service
 * already returned, names {@link Authorize#GET} instead: what it decides is not whether the call
 * was allowed but how much of the result may be written out, and that is one question however the
 * object was reached. A rule that branches on the permission should treat anything it does not
 * recognize as a read rather than assuming a privilege name.
 * <p>
 * A rule is only ever asked about the object. {@link OpenmrsPermissionEvaluator} has already
 * confirmed the current user holds the named privilege by the time any rule runs - a rule reached
 * at all means the privilege check passed - so an implementation must not re-check it, and
 * returning {@code false} is a statement about <em>this object</em> rather than about the user's
 * privileges. The {@code permission} each method receives is therefore context, not something to
 * evaluate: it lets one rule answer differently per operation on the same object (permitting
 * {@code 'Get Patients'} on a patient while denying {@code 'Edit Patients'}, say), since rules are
 * selected by target type alone and a rule for {@code "Patient"} is consulted for every
 * {@code hasPermission(patient, ...)} call whatever privilege it names. An implementation that
 * ignores the argument applies uniformly to all of them, which is the common case.
 *
 * @since 3.0.0
 */
public interface DomainObjectAuthorizationRule {

	/**
	 * @return the target type this rule applies to. It covers that type <em>and its subtypes</em>, so a
	 *         rule for {@code Person.class} is consulted for a {@code Patient} too; a rule needing to
	 *         cover unrelated types registers as more than one bean. An interface works as well, and
	 *         {@code OpenmrsObject.class} is the widest a rule can be - consulted for every domain
	 *         object, however far down the hierarchy it implements it.
	 */
	Class<?> getTargetType();

	/**
	 * @param authentication the current authentication
	 * @param targetDomainObject the target object named by a
	 *            {@code hasPermission(targetDomainObject, permission)} expression
	 * @param permission the privilege named by the expression, supplied so this rule can distinguish
	 *            which operation is being attempted; already confirmed to be held, so it is not to be
	 *            re-checked (see this interface's javadoc). {@link Authorize#GET} rather than a
	 *            privilege name when the question comes from {@link Authorize} - see this interface's
	 *            javadoc
	 * @return true if this rule authorizes access to {@code targetDomainObject}
	 */
	boolean isAuthorized(Authentication authentication, Object targetDomainObject, Object permission);

	/**
	 * @param authentication the current authentication
	 * @param targetId the target's identifier, named by a
	 *            {@code hasPermission(targetId, targetType, permission)} expression - an
	 *            {@code Integer} primary key (e.g. {@code hasPermission(#patientId, 'Patient', ...)})
	 *            or a {@code String} uuid (e.g. {@code hasPermission(#uuid, 'Patient', ...)}) alike,
	 *            whichever the calling method already has in hand. A rule reached with an id form it
	 *            cannot interpret must <em>deny</em>: a rule that handles only {@code Integer} and
	 *            authorizes everything else leaves {@code getPatientByUuid(String)} - how REST loads a
	 *            patient - unchecked, so every id form the type covers has to be handled or refused.
	 *            <p>
	 *            A {@code String} is a uuid for every type in core but one: where a method looks its
	 *            object up by a <em>natural key</em> it names that instead, as every
	 *            {@code AdministrationService} global-property method but
	 *            {@code getGlobalPropertyByUuid(String)} names the property name. Nothing tells the two
	 *            apart, so a rule for such a type must match both forms and deny if either is denied,
	 *            rather than read one as the other
	 * @param targetType the target's type, resolved from the name the expression gave - a
	 *            fully-qualified class name, or a simple one for a class in {@code org.openmrs}. Passed
	 *            because an id alone does not say what it identifies; it is this rule's
	 *            {@link #getTargetType()} or a subtype of it
	 * @param permission the privilege named by the expression, supplied so this rule can distinguish
	 *            which operation is being attempted; already confirmed to be held, so it is not to be
	 *            re-checked (see this interface's javadoc)
	 * @return true if this rule authorizes access to the object identified by {@code targetId}
	 */
	boolean isAuthorized(Authentication authentication, Serializable targetId, Class<?> targetType, Object permission);
}
