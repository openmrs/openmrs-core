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

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.hibernate.proxy.HibernateProxy;
import org.openmrs.OpenmrsObject;
import org.openmrs.api.context.Context;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.target.EmptyTargetSource;
import org.springframework.util.ClassUtils;

/**
 * Applies registered {@link DomainObjectAuthorizationRule}s to what a presentation layer is about
 * to serialize, replacing what a rule refuses with a {@link MaskedObject} rather than dropping it.
 * <p>
 * Service methods decide <em>whether</em> a call is allowed, and {@code @PostFilter} already drops
 * elements a rule refuses. This is for the layer after that: webservices.rest and the FHIR module
 * hold an object graph and have to decide how much of it to write out, and a reader needs to be
 * able to tell data that is hidden from data that does not exist. Masking says so; a shorter list
 * does not.
 * <p>
 * <strong>A caller that bypassed the services is the only check there is.</strong> The paragraph
 * above assumes a service method already applied the rules through {@code @PostFilter} or a
 * {@code hasPermission} guard, which is true of anything read through {@code ObsService} and its
 * siblings. A module that queries with its own DAO has had no rule applied to what it holds: fhir2
 * reads observations that way, and its DAO checks only the privilege, so an obs a rule refuses is
 * served exactly as an allowed one is. Where that is how the data arrived, this is not a second
 * opinion on top of a guard, it is the guard, and it has to be placed and tested as one. Nothing
 * here makes such a reader covered by having called it.
 * <p>
 * <strong>What comes back must never be written.</strong> A masked object is a serialization-time
 * artifact: it carries no data, and persisting one would blank the record it stands for. Handing
 * one to a {@code save*} method is rejected rather than attempted (see {@code RequiredDataAdvice}).
 * <p>
 * Every rule is asked about {@link #GET} rather than about a privilege the caller names. What is
 * being decided here is not whether the call was allowed - the service method settled that - but
 * how much of what it returned may be written out, and that is one question however the object was
 * reached.
 * <p>
 * A refused object becomes a mask, which holds nothing. One no rule refuses comes back exactly as
 * it was, so only the objects handed to these methods are judged and nothing reachable through them
 * is: a rule covering {@code PersonAttribute} does nothing for {@code maskIfDenied(patient)}, and
 * those attributes go on to serialize in full. Reaching one means naming it,
 * {@code maskIfDenied(patient.getAttributes())} as well as the patient.
 * <p>
 * Masking the fields of an allowed object was tried here and removed. A proxy that intercepted its
 * getters has to hold the real object to delegate to, and in webservices.rest that both missed
 * paths and changed what it rendered for data nobody had denied: a getter taking an argument
 * ({@code Encounter.getObsAtTopLevel(boolean)}) is never intercepted, a proxy over a lazily loaded
 * association masks nothing because the association's own class redeclares the getters, and
 * {@code BaseDelegatingResource.getSubclassHandler} matches a delegate by exact class, so every
 * {@code DrugOrder} reached through such a proxy lost its subclass representation as soon as any
 * rule was registered. A presentation layer sees every nested object already unwrapped, and that is
 * where the check belongs.
 *
 * @see MaskedObject
 * @see DomainObjectAuthorizationRule
 * @since 3.0.0
 */
public final class Authorize {

	/**
	 * First group of the uuid a mask reports, for a reader that has only the serialized value to go on
	 * and no {@link MaskedObject} to test. The remaining groups are random: see
	 * {@link #standInFor(OpenmrsObject)}.
	 * <p>
	 * Hex, because the whole value has to parse as a uuid and a readable word cannot: {@code m},
	 * {@code s} and {@code k} are not hex digits, so {@code masked-} and uuid syntax are mutually
	 * exclusive. This spells what the alphabet allows and is improbable as the opening group of a real
	 * one.
	 */
	public static final String MASKED_UUID_PREFIX = "effaced0-";

	/**
	 * The permission every rule is asked about here, standing for "this is being read at the
	 * presentation layer" rather than for any one service method's privilege.
	 * <p>
	 * Not a privilege a role holds, and never checked against one: {@code rulesAuthorize} puts it
	 * straight to the rules without consulting {@code Context.hasPrivilege}, because whether the call
	 * was allowed is a question the service method already answered. A rule that wants to distinguish
	 * reading from writing can branch on it; one that does not can ignore it.
	 */
	public static final String GET = "Get";

	/**
	 * What a mask prints. Deliberately says nothing about what it stands for, since anything that
	 * renders an object by its {@code toString()} - a JSP among them - would otherwise show the masked
	 * type and the substitute uuid to whoever is reading the page.
	 */
	private static final String MASKED_TO_STRING = "[hidden]";

	private Authorize() {
	}

	/**
	 * The object, or a {@link MaskedObject} standing in for it when a registered rule refuses it. An
	 * object that is already a mask comes back untouched rather than being judged again. A mask holds
	 * nothing, so asking a rule about one asks it about a null id, and a rule phrased as an allow-list
	 * reads that as "not allowed" at best and throws at worst.
	 *
	 * @param object the object about to be serialized, may be null
	 * @param <T> the object's type
	 * @return {@code object} itself when no rule refuses it, otherwise a {@link MaskedObject} stand-in
	 *         of the same type; null stays null
	 */
	public static <T extends OpenmrsObject> T maskIfDenied(T object) {
		if (object == null || isMasked(object)) {
			return object;
		}

		OpenmrsPermissionEvaluator evaluator = evaluator();
		if (!evaluator.hasRules()) {
			return object;
		}

		return evaluator.rulesAuthorize(initialized(object), GET) ? object : standInFor(object);
	}

	/**
	 * The collection with every object a registered rule refuses replaced by a {@link MaskedObject}.
	 * <p>
	 * Replaced rather than removed, so the size a caller sees is the size the query found: a reader can
	 * tell six observations with two hidden from four observations, and a page of twenty stays a page
	 * of twenty. The argument is never modified - unlike {@code @PostFilter}, which filters in place
	 * and is why a {@code @PostFilter}ed {@code @Cacheable} method has to key its cache by the caller.
	 * <p>
	 * What comes back is unmodifiable, and when no rule is registered it is a view of the argument
	 * rather than a copy. That is deliberate: copying calls {@code toArray()} on the argument, which
	 * initializes a lazy Hibernate association, and several of them arrive uninitialized
	 * ({@code patient.getIdentifiers()}, {@code visit.getEncounters()},
	 * {@code concept.getConceptSets()}). A read helper should not turn a collection nobody asked for
	 * into a select, nor raise {@code LazyInitializationException} on a closed session where returning
	 * the collection untouched would not have. Being unmodifiable is what makes sharing it safe: a
	 * caller that tries to sort or trim the result fails at its own call site instead of silently
	 * scheduling deletes on the service's collection at flush.
	 * <p>
	 * Judging elements does have to iterate, so the laziness above only survives while no rule is
	 * registered. The elements themselves stay mutable either way; only the collection is protected.
	 *
	 * @param collection the objects about to be serialized, may be null
	 * @param <T> the element type
	 * @return an unmodifiable collection of the same size, in the same order, possibly a view of
	 *         {@code collection}; null stays null
	 */
	public static <T extends OpenmrsObject> Collection<T> maskIfDenied(Collection<T> collection) {
		if (collection == null) {
			return null;
		}

		OpenmrsPermissionEvaluator evaluator = evaluator();
		if (!evaluator.hasRules()) {
			// wrapped, not copied: copying would initialize a lazy association. See the javadoc
			return Collections.unmodifiableCollection(collection);
		}

		List<T> masked = new ArrayList<>(collection.size());
		for (T element : collection) {
			masked.add(element == null || isMasked(element) ? element
			        : evaluator.rulesAuthorize(initialized(element), GET) ? element : standInFor(element));
		}
		return Collections.unmodifiableList(masked);
	}

	/**
	 * @param object the object to test, may be null
	 * @return true if {@code object} stands in for one a rule refused
	 */
	public static boolean isMasked(Object object) {
		return object instanceof MaskedObject;
	}

	/**
	 * The object itself, or what a lazily loaded association stands for, so the rules are asked about
	 * the real thing. A proxy is typed by the association's declared class, so a {@code Patient} rule
	 * is never matched to an {@code obs.getPerson()} proxy whose persistent class is {@code Person},
	 * and a refused patient reached that way would go out unmasked.
	 * <p>
	 * This initializes the proxy, which is why it sits here rather than in the evaluator: the rules are
	 * also consulted once per element of a {@code @PostFilter}, by way of {@code hasPermission(...)},
	 * and that path must not load anything, while a caller here is about to serialize the object
	 * regardless. Whether a proxy arrives at all depends on what the session had already loaded, so
	 * leaving it wrapped would make masking depend on that too.
	 */
	private static Object initialized(Object object) {
		return object instanceof HibernateProxy hibernateProxy
		        ? hibernateProxy.getHibernateLazyInitializer().getImplementation()
		        : object;
	}

	/** The entity class behind a Hibernate or AOP proxy, so methods are read off the real type. */
	private static Class<?> targetTypeOf(Object object) {
		if (object instanceof HibernateProxy hibernateProxy) {
			return hibernateProxy.getHibernateLazyInitializer().getPersistentClass();
		}

		return ClassUtils.getUserClass(AopProxyUtils.ultimateTargetClass(object));
	}

	/**
	 * A stand-in of {@code object}'s own type, built with no target so that a method this mask does not
	 * recognize cannot reach the real object: the proxy has nothing to delegate to, and every call is
	 * answered by {@link MaskInterceptor}.
	 * <p>
	 * It discloses nothing of what it masks, its identity included. The uuid it reports is
	 * {@link #MASKED_UUID_PREFIX} followed by the remaining groups of a fresh random one, and its id is
	 * null, because the real identifiers are themselves disclosure: returning them from a search result
	 * would tell a caller that a record exists and hand over a key to try elsewhere. A substitute is
	 * reported rather than nothing only so the shape downstream stays valid: fhir2 sets a resource's
	 * {@code id} from the uuid and a REST representation is keyed by it, and neither tolerates the
	 * absence well. It buys nothing beyond that, and should not be mistaken for a usable reference -
	 * what each layer renders from it points at no record, so fhir2 emits a
	 * {@code "subject":{"reference":"Patient/effaced0-..."}} and REST a self link that resolves to
	 * nothing. Showing a reader that something is hidden is each layer's own job, which it has to do
	 * from {@link #isMasked(Object)} rather than from this value.
	 * <p>
	 * Generated once per mask, so it is stable while one object is being written out, and deliberately
	 * not derived from the real uuid, so it is stable for nothing else: two masks of the same record do
	 * not match. Correlating hidden entries across requests is not a capability this is meant to give.
	 * <p>
	 * Well-formed rather than a decorated string, so anything parsing it as a uuid - a client, a column
	 * of that type, {@link UUID#fromString(String)} - is not handed something it rejects. The random
	 * groups come from a version 4 uuid, so the version and variant bits are those of a real one.
	 */
	@SuppressWarnings("unchecked")
	private static <T extends OpenmrsObject> T standInFor(T object) {
		// the entity class, not object.getClass(): a lazily loaded association arrives as a Hibernate
		// proxy, and masking that class would report a generated name from getMaskedType() and subclass
		// the proxy rather than the entity
		Class<?> maskedType = targetTypeOf(object);
		ProxyFactory factory = new ProxyFactory();
		factory.setTargetSource(EmptyTargetSource.forClass(maskedType));
		factory.setProxyTargetClass(true);
		factory.addInterface(MaskedObject.class);
		// substring(9) drops the random uuid's own first group and separator, so the result is still 36
		// characters in 8-4-4-4-12
		factory.addAdvice(new MaskInterceptor(maskedType, MASKED_UUID_PREFIX + UUID.randomUUID().toString().substring(9)));
		return (T) factory.getProxy(maskedType.getClassLoader());
	}

	private static OpenmrsPermissionEvaluator evaluator() {
		return Context.getRegisteredComponents(OpenmrsPermissionEvaluator.class).get(0);
	}

	/**
	 * Answers every call on a masked object without a target to fall back on, so a getter nobody
	 * thought about returns emptiness rather than data. Collection, map and array returns come back
	 * empty rather than null, because a serializer handed a null where it expected a collection fails
	 * on the response it was halfway through writing.
	 * <p>
	 * A setter throws. Masking is for a read on its way out, and writing to one is a programmer error
	 * worth hearing about at the call site rather than when the blanked record reaches the database.
	 */
	private static final class MaskInterceptor implements MethodInterceptor {

		private final Class<?> maskedType;

		private final String uuid;

		private MaskInterceptor(Class<?> maskedType, String uuid) {
			this.maskedType = maskedType;
			this.uuid = uuid;
		}

		@Override
		public Object invoke(MethodInvocation invocation) {
			Method method = invocation.getMethod();
			String name = method.getName();

			switch (name) {
				case "getMaskedType":
					return maskedType;
				case "getUuid":
					return uuid;
				case "toString":
					// nothing identifying: a JSP that prints an object prints this, and legacyui was
					// putting the masked type and the substitute uuid straight onto the page
					return MASKED_TO_STRING;
				case "hashCode":
					return System.identityHashCode(invocation.getThis());
				case "equals":
					return invocation.getThis() == invocation.getArguments()[0];
				default:
					break;
			}

			if (name.startsWith("set") && method.getParameterCount() == 1) {
				throw new UnsupportedOperationException(
				        "Cannot write to a masked " + maskedType.getName() + ": it stands in for an object the "
				                + "current user may not read, and persisting it would blank that record");
			}

			return emptyValueFor(method.getReturnType());
		}

		private static Object emptyValueFor(Class<?> returnType) {
			if (Set.class.isAssignableFrom(returnType)) {
				return Collections.emptySet();
			}
			if (Map.class.isAssignableFrom(returnType)) {
				return Collections.emptyMap();
			}
			if (Collection.class.isAssignableFrom(returnType)) {
				return Collections.emptyList();
			}
			if (returnType.isArray()) {
				return Array.newInstance(returnType.getComponentType(), 0);
			}
			if (Boolean.class.equals(returnType)) {
				// boxed, but treated as a flag everywhere in the domain model: `if (x.getVoided())` is the
				// common idiom, so a null here would NPE the very serialization this is for
				return Boolean.FALSE;
			}
			if (returnType.isPrimitive() && !void.class.equals(returnType)) {
				// the zero value of any primitive, boxed: a one-element array of it is zero-filled, so this
				// covers false and '\0' as well as the numeric zeroes without a branch each
				return Array.get(Array.newInstance(returnType, 1), 0);
			}
			return null;
		}
	}
}
