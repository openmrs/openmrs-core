/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Helpers for the collection setters of domain objects.
 * <p>
 * A domain object's collection setter must not replace the collection Hibernate is tracking on a
 * loaded entity, otherwise every row is deleted and re-inserted on flush and an all-delete-orphan
 * collection fails with "collection was no longer referenced by the owning entity instance".
 * Instead the setter updates the current collection in place so that it contains exactly the given
 * instances.
 * <p>
 * Instances are compared by identity, so a detached copy of an element is replaced by the instance
 * passed in. The given collection is copied, never kept, so it may be immutable or a view of the
 * current collection, and changes made to it after the call do not affect the domain object. When
 * the current collection is null, a new one is created from the given collection. Setting a lazy
 * collection initializes it.
 * <p>
 * The collection must be mapped with field access ({@code access="field"} in hbm.xml, or
 * {@code @Access(AccessType.FIELD)} or annotations placed on the field): with property access,
 * Hibernate hands the collection it loads to the setter, and the entity keeps a copy that Hibernate
 * does not track.
 *
 * @since 2.9.0, 3.0.0
 */
public class CollectionUtil {

	/**
	 * Updates {@code current} in place so that it holds exactly the instances in {@code replacement}.
	 * Elements that are no longer wanted are removed and missing ones are added in the order of
	 * {@code replacement}; a sorted or ordered set keeps its type and ordering.
	 *
	 * @param current the collection the domain object holds, may be null
	 * @param replacement the instances the collection should hold, may be null to empty it
	 * @return the collection to assign to the field: {@code current} when it is not null, otherwise a
	 *         new {@link TreeSet} with the comparator of a {@link SortedSet} {@code replacement} or a
	 *         new {@link LinkedHashSet} holding {@code replacement}, or null when both are null
	 */
	public static <E> Set<E> setCollection(Set<E> current, Collection<? extends E> replacement) {
		if (current == null) {
			if (replacement == null) {
				return null;
			}
			return replacement instanceof SortedSet ? new TreeSet<>((SortedSet<E>) replacement)
			        : new LinkedHashSet<>(replacement);
		}

		update(current, replacement);
		return current;
	}

	/**
	 * Updates {@code current} in place so that it holds exactly the instances in {@code replacement},
	 * in the same order. Only positions whose instance differs are touched, and duplicates in
	 * {@code replacement} are kept.
	 *
	 * @param current the list the domain object holds, may be null
	 * @param replacement the instances the list should hold, may be null to empty it
	 * @return the list to assign to the field: {@code current} when it is not null, otherwise a new
	 *         {@link ArrayList} holding {@code replacement}, or null when both are null
	 */
	public static <E> List<E> setCollection(List<E> current, Collection<? extends E> replacement) {
		if (current == null) {
			return replacement == null ? null : new ArrayList<>(replacement);
		}

		if (replacement == null) {
			current.clear();
			return current;
		}

		List<E> wanted = new ArrayList<>(replacement);

		for (int i = 0; i < wanted.size(); i++) {
			E e = wanted.get(i);
			if (i >= current.size()) {
				current.add(e);
			} else if (current.get(i) != e) {
				current.set(i, e);
			}
		}
		while (current.size() > wanted.size()) {
			current.removeLast();
		}
		return current;
	}

	/**
	 * Variant for fields declared as {@link Collection}, which Hibernate may back by a set or by a bag.
	 * Behaves like {@link #setCollection(Set, Collection)}: elements are removed and added without
	 * positional operations, so a bag is never reordered and never receives the same instance twice.
	 *
	 * @param current the collection the domain object holds, may be null
	 * @param replacement the instances the collection should hold, may be null to empty it
	 * @return the collection to assign to the field
	 * @see #setCollection(Set, Collection)
	 */
	public static <E> Collection<E> setCollection(Collection<E> current, Collection<? extends E> replacement) {
		if (current == null) {
			return setCollection((Set<E>) null, replacement);
		}
		update(current, replacement);
		return current;
	}

	private static <E> void update(Collection<E> current, Collection<? extends E> replacement) {
		if (replacement == null) {
			current.clear();
			return;
		}

		List<E> copy = new ArrayList<>(replacement);
		Set<E> wanted = identitySet(copy);
		current.removeIf(e -> !wanted.contains(e));

		Set<E> present = identitySet(current);
		for (E e : copy) {
			if (present.add(e)) {
				current.add(e);
			}
		}
	}

	private static <E> Set<E> identitySet(Collection<? extends E> elements) {
		Set<E> set = Collections.newSetFromMap(new IdentityHashMap<>());
		set.addAll(elements);
		return set;
	}
}
