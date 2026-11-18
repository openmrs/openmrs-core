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
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link CollectionUtil}.
 */
public class CollectionUtilTest {

	private final Item a = new Item(1);

	private final Item b = new Item(2);

	private final Item c = new Item(3);

	private final Item d = new Item(4);

	/**
	 * @see CollectionUtil#setCollection(Set, java.util.Collection)
	 */
	@Test
	public void setCollection_shouldCreateNewSetWhenCurrentIsNull() {
		Set<Item> result = CollectionUtil.setCollection((Set<Item>) null, List.of(a, b));

		assertEquals(Set.of(a, b), result);
	}

	/**
	 * @see CollectionUtil#setCollection(Set, java.util.Collection)
	 */
	@Test
	public void setCollection_shouldReturnNullWhenCurrentAndReplacementSetsAreNull() {
		assertNull(CollectionUtil.setCollection((Set<Item>) null, null));
	}

	/**
	 * @see CollectionUtil#setCollection(Set, java.util.Collection)
	 */
	@Test
	public void setCollection_shouldKeepSortedReplacementOrderingWhenCurrentIsNull() {
		Set<Item> replacement = new TreeSet<>(Comparator.reverseOrder());
		Collections.addAll(replacement, a, b, c);

		Set<Item> result = CollectionUtil.setCollection((Set<Item>) null, replacement);
		result.add(d);

		assertEquals(List.of(d, c, b, a), new ArrayList<>(result));
	}

	/**
	 * @see CollectionUtil#setCollection(Set, java.util.Collection)
	 */
	@Test
	public void setCollection_shouldClearCurrentSetWhenReplacementIsNull() {
		Set<Item> current = trackingSet(a, b);

		CollectionUtil.setCollection(current, null);

		assertTrue(current.isEmpty());
	}

	/**
	 * @see CollectionUtil#setCollection(Set, java.util.Collection)
	 */
	@Test
	public void setCollection_shouldReturnCurrentSetWhenReplacementIsCurrent() {
		Set<Item> current = trackingSet(a, b);

		Set<Item> result = CollectionUtil.setCollection(current, current);

		assertSame(current, result);
	}

	/**
	 * @see CollectionUtil#setCollection(Set, java.util.Collection)
	 */
	@Test
	public void setCollection_shouldNotModifyCurrentSetWhenReplacementHoldsSameInstances() {
		TrackingSet<Item> current = trackingSet(a, b);

		CollectionUtil.setCollection(current, new LinkedHashSet<>(List.of(b, a)));

		assertEquals(0, current.mutations);
	}

	/**
	 * @see CollectionUtil#setCollection(Set, java.util.Collection)
	 */
	@Test
	public void setCollection_shouldAcceptViewOfCurrentSet() {
		TrackingSet<Item> current = trackingSet(a, b);

		CollectionUtil.setCollection(current, Collections.unmodifiableSet(current));

		assertEquals(0, current.mutations);
	}

	/**
	 * @see CollectionUtil#setCollection(Set, java.util.Collection)
	 */
	@Test
	public void setCollection_shouldReplaceEqualButDifferentInstanceInSet() {
		Set<Item> current = trackingSet(a);
		Item detached = new Item(a.id);

		CollectionUtil.setCollection(current, Set.of(detached));

		assertSame(detached, current.iterator().next());
	}

	/**
	 * @see CollectionUtil#setCollection(Set, java.util.Collection)
	 */
	@Test
	public void setCollection_shouldAcceptImmutableReplacementSet() {
		Set<Item> current = trackingSet(a);

		CollectionUtil.setCollection(current, Set.of(a, b));

		assertEquals(Set.of(a, b), current);
	}

	/**
	 * @see CollectionUtil#setCollection(Set, java.util.Collection)
	 */
	@Test
	public void setCollection_shouldKeepCurrentSetMutableWhenReplacementIsImmutable() {
		Set<Item> current = trackingSet(a);

		CollectionUtil.setCollection(current, Set.of(a, b));

		assertTrue(current.add(c));
	}

	/**
	 * @see CollectionUtil#setCollection(Set, java.util.Collection)
	 */
	@Test
	public void setCollection_shouldNotKeepReferenceToReplacementSet() {
		Set<Item> current = trackingSet(a);
		Set<Item> replacement = new LinkedHashSet<>(List.of(a, b));

		CollectionUtil.setCollection(current, replacement);
		replacement.add(c);

		assertFalse(current.contains(c));
	}

	/**
	 * @see CollectionUtil#setCollection(Set, java.util.Collection)
	 */
	@Test
	public void setCollection_shouldRemoveDroppedInstancesAndAddNewOnesToSet() {
		Set<Item> current = trackingSet(a, b, c);

		CollectionUtil.setCollection(current, Set.of(a, b, d));

		assertEquals(Set.of(a, b, d), current);
	}

	/**
	 * @see CollectionUtil#setCollection(Set, java.util.Collection)
	 */
	@Test
	public void setCollection_shouldKeepSortedSetInstance() {
		Set<Item> current = new TreeSet<>(Comparator.reverseOrder());
		current.add(a);

		Set<Item> result = CollectionUtil.setCollection(current, Set.of(b, c));

		assertSame(current, result);
	}

	/**
	 * @see CollectionUtil#setCollection(Set, java.util.Collection)
	 */
	@Test
	public void setCollection_shouldKeepSortedSetOrdering() {
		Set<Item> current = new TreeSet<>(Comparator.reverseOrder());
		current.add(a);

		CollectionUtil.setCollection(current, Set.of(a, b, c));

		assertEquals(List.of(c, b, a), new ArrayList<>(current));
	}

	/**
	 * @see CollectionUtil#setCollection(Set, java.util.Collection)
	 */
	@Test
	public void setCollection_shouldAddNewInstancesInReplacementOrder() {
		Set<Item> current = trackingSet(a);

		CollectionUtil.setCollection(current, new LinkedHashSet<>(List.of(a, d, c, b)));

		assertEquals(List.of(a, d, c, b), new ArrayList<>(current));
	}

	/**
	 * @see CollectionUtil#setCollection(List, java.util.Collection)
	 */
	@Test
	public void setCollection_shouldCreateNewListWhenCurrentIsNull() {
		List<Item> result = CollectionUtil.setCollection((List<Item>) null, List.of(a, b));

		assertEquals(List.of(a, b), result);
	}

	/**
	 * @see CollectionUtil#setCollection(List, java.util.Collection)
	 */
	@Test
	public void setCollection_shouldReturnNullWhenCurrentAndReplacementListsAreNull() {
		assertNull(CollectionUtil.setCollection((List<Item>) null, null));
	}

	/**
	 * @see CollectionUtil#setCollection(List, java.util.Collection)
	 */
	@Test
	public void setCollection_shouldClearCurrentListWhenReplacementIsNull() {
		List<Item> current = trackingList(a, b);

		CollectionUtil.setCollection(current, null);

		assertTrue(current.isEmpty());
	}

	/**
	 * @see CollectionUtil#setCollection(List, java.util.Collection)
	 */
	@Test
	public void setCollection_shouldReturnCurrentListWhenReplacementIsCurrent() {
		List<Item> current = trackingList(a, b);

		List<Item> result = CollectionUtil.setCollection(current, current);

		assertSame(current, result);
	}

	/**
	 * @see CollectionUtil#setCollection(List, java.util.Collection)
	 */
	@Test
	public void setCollection_shouldNotModifyCurrentListWhenReplacementHoldsSameInstancesInSameOrder() {
		TrackingList<Item> current = trackingList(a, b);

		CollectionUtil.setCollection(current, List.of(a, b));

		assertEquals(0, current.mutations);
	}

	/**
	 * @see CollectionUtil#setCollection(List, java.util.Collection)
	 */
	@Test
	public void setCollection_shouldAcceptViewOfCurrentList() {
		TrackingList<Item> current = trackingList(a, b);

		CollectionUtil.setCollection(current, Collections.unmodifiableList(current));

		assertEquals(0, current.mutations);
	}

	/**
	 * @see CollectionUtil#setCollection(List, java.util.Collection)
	 */
	@Test
	public void setCollection_shouldReplaceEqualButDifferentInstanceInList() {
		List<Item> current = trackingList(a);
		Item detached = new Item(a.id);

		CollectionUtil.setCollection(current, List.of(detached));

		assertSame(detached, current.get(0));
	}

	/**
	 * @see CollectionUtil#setCollection(List, java.util.Collection)
	 */
	@Test
	public void setCollection_shouldAcceptImmutableReplacementList() {
		List<Item> current = trackingList(a);

		CollectionUtil.setCollection(current, List.of(a, b));

		assertEquals(List.of(a, b), current);
	}

	/**
	 * @see CollectionUtil#setCollection(List, java.util.Collection)
	 */
	@Test
	public void setCollection_shouldKeepCurrentListMutableWhenReplacementIsImmutable() {
		List<Item> current = trackingList(a);

		CollectionUtil.setCollection(current, List.of(a, b));

		assertTrue(current.add(c));
	}

	/**
	 * @see CollectionUtil#setCollection(List, java.util.Collection)
	 */
	@Test
	public void setCollection_shouldNotKeepReferenceToReplacementList() {
		List<Item> current = trackingList(a);
		List<Item> replacement = new ArrayList<>(List.of(a, b));

		CollectionUtil.setCollection(current, replacement);
		replacement.add(c);

		assertFalse(current.contains(c));
	}

	/**
	 * @see CollectionUtil#setCollection(List, java.util.Collection)
	 */
	@Test
	public void setCollection_shouldReorderListToMatchReplacement() {
		List<Item> current = trackingList(a, b, c);

		CollectionUtil.setCollection(current, List.of(c, a, b));

		assertEquals(List.of(c, a, b), current);
	}

	/**
	 * @see CollectionUtil#setCollection(List, java.util.Collection)
	 */
	@Test
	public void setCollection_shouldRemoveTrailingInstancesMissingFromReplacement() {
		List<Item> current = trackingList(a, b, c);

		CollectionUtil.setCollection(current, List.of(a));

		assertEquals(List.of(a), current);
	}

	/**
	 * @see CollectionUtil#setCollection(List, java.util.Collection)
	 */
	@Test
	public void setCollection_shouldKeepDuplicatesInReplacementList() {
		List<Item> current = trackingList(a);

		CollectionUtil.setCollection(current, List.of(a, b, a));

		assertEquals(List.of(a, b, a), current);
	}

	/**
	 * @see CollectionUtil#setCollection(List, java.util.Collection)
	 */
	@Test
	public void setCollection_shouldOnlyTouchChangedListPositions() {
		TrackingList<Item> current = trackingList(a, b, c);

		CollectionUtil.setCollection(current, List.of(a, b, d));

		assertEquals(1, current.mutations);
	}

	/**
	 * @see CollectionUtil#setCollection(Collection, Collection)
	 */
	@Test
	public void setCollection_shouldNotReorderListGivenAsCollection() {
		TrackingList<Item> current = trackingList(a, b);

		CollectionUtil.setCollection((Collection<Item>) current, List.of(b, a));

		assertEquals(0, current.mutations);
	}

	/**
	 * @see CollectionUtil#setCollection(Collection, Collection)
	 */
	@Test
	public void setCollection_shouldNotAddDuplicatesToListGivenAsCollection() {
		TrackingList<Item> current = trackingList(a);

		CollectionUtil.setCollection((Collection<Item>) current, List.of(a, b, b));

		assertEquals(List.of(a, b), current);
	}

	/**
	 * @see CollectionUtil#setCollection(Collection, Collection)
	 */
	@Test
	public void setCollection_shouldUpdateSetGivenAsCollectionInPlace() {
		TrackingSet<Item> current = trackingSet(a, b);

		Collection<Item> result = CollectionUtil.setCollection((Collection<Item>) current, List.of(b, c));

		assertSame(current, result);
	}

	private TrackingSet<Item> trackingSet(Item... items) {
		TrackingSet<Item> set = new TrackingSet<>();
		Collections.addAll(set, items);
		set.mutations = 0;
		return set;
	}

	private TrackingList<Item> trackingList(Item... items) {
		TrackingList<Item> list = new TrackingList<>();
		Collections.addAll(list, items);
		list.mutations = 0;
		return list;
	}

	/**
	 * Equal by id, so that an "equal but different instance" can be told apart from the original by
	 * identity. Comparable by id for the sorted set tests.
	 */
	private static final class Item implements Comparable<Item> {

		private final int id;

		private Item(int id) {
			this.id = id;
		}

		@Override
		public boolean equals(Object o) {
			return o instanceof Item && ((Item) o).id == id;
		}

		@Override
		public int hashCode() {
			return id;
		}

		@Override
		public int compareTo(Item other) {
			return Integer.compare(id, other.id);
		}

		@Override
		public String toString() {
			return "Item" + id;
		}
	}

	/**
	 * Counts every change to its contents, the way Hibernate marks a PersistentSet dirty only when an
	 * add or remove actually changes it.
	 */
	private static final class TrackingSet<E> extends LinkedHashSet<E> {

		private int mutations;

		@Override
		public boolean add(E e) {
			boolean changed = super.add(e);
			if (changed) {
				mutations++;
			}
			return changed;
		}

		@Override
		public boolean remove(Object o) {
			boolean changed = super.remove(o);
			if (changed) {
				mutations++;
			}
			return changed;
		}

		@Override
		public void clear() {
			if (!isEmpty()) {
				mutations++;
			}
			super.clear();
		}

		@Override
		public Iterator<E> iterator() {
			Iterator<E> delegate = super.iterator();
			return new Iterator<E>() {

				@Override
				public boolean hasNext() {
					return delegate.hasNext();
				}

				@Override
				public E next() {
					return delegate.next();
				}

				@Override
				public void remove() {
					delegate.remove();
					mutations++;
				}
			};
		}
	}

	/**
	 * Counts every positional operation, the way Hibernate marks a PersistentList dirty on each set,
	 * add or remove regardless of whether the element changed.
	 */
	private static final class TrackingList<E> extends ArrayList<E> {

		private int mutations;

		@Override
		public boolean add(E e) {
			mutations++;
			return super.add(e);
		}

		@Override
		public void add(int index, E e) {
			mutations++;
			super.add(index, e);
		}

		@Override
		public E set(int index, E e) {
			mutations++;
			return super.set(index, e);
		}

		@Override
		public E remove(int index) {
			mutations++;
			return super.remove(index);
		}

		@Override
		public E removeLast() {
			mutations++;
			return super.removeLast();
		}

		@Override
		public void clear() {
			if (!isEmpty()) {
				mutations++;
			}
			super.clear();
		}
	}
}
