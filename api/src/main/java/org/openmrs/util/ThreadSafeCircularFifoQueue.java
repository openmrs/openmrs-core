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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.util.AbstractQueue;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;

/**
 * A thread-safe first-in, first-out queue with a fixed size that replaces the oldest element when full.
 * 
 * This class does not support null elements.
 *
 * @param <E> the type of elements in this collection
 * @since 2.4
 */
/*
 * There are already several existing implementations that are similar to this, so why create a new class?
 * Commons Collection's CircularFifoQueue and Guava's EvictingQueue are not thread-safe. The built-in ArrayBlockingQueue
 * is thread-safe, but implements a blocking queue. This involves safety guarantees that we don't need to make as this
 * circular queue is never "full"
 */
public class ThreadSafeCircularFifoQueue<E> extends AbstractQueue<E> implements Queue<E>, Serializable {

	private static final long serialVersionUID = -89162358098721L;

	// queue capacity
	private final int maxElements;

	// Underlying storage
	private final E[] elements;

	private transient ReentrantLock lock = new ReentrantLock();

	// index of the "start" of the queue, i.e., where data is read from
	private int read = 0;

	// index of the "end" of the queue, i.e., where data is written to
	private int write = 0;

	// number of elements in the queue
	private int size;

	// tracks the state of any iterators
	private transient Iterators iterators = null;

	@SuppressWarnings("unused")
	public ThreadSafeCircularFifoQueue() {
		this(32);
	}

	@SuppressWarnings("unchecked")
	public ThreadSafeCircularFifoQueue(int maxElements) {
		if (maxElements <= 0) {
			throw new IllegalArgumentException("The size must be greater than 0");
		}

		// NB add one more element so that the queue size matches the expected size
		elements = (E[]) new Object[maxElements];
		this.maxElements = elements.length;
	}

	@SuppressWarnings("unused")
	public ThreadSafeCircularFifoQueue(Collection<E> collection) {
		this(collection.size());
		this.addAll(collection);
	}

	@Override
	public boolean add(E e) {
		Objects.requireNonNull(e);

		final ReentrantLock lock = this.lock;
		lock.lock();
		try {
			internalAdd(e);
		}
		finally {
			lock.unlock();
		}

		return true;
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		Objects.requireNonNull(c);

		final ReentrantLock lock = this.lock;
		lock.lock();
		try {
			for (E e : c) {
				Objects.requireNonNull(e);
				internalAdd(e);
			}
		}
		finally {
			lock.unlock();
		}

		return true;
	}

	@Override
	public void clear() {
		final E[] elements = this.elements;
		final ReentrantLock lock = this.lock;
		lock.lock();
		try {
			if (size > 0) {
				int idx = read;
				do {
					elements[idx] = null;

					idx = increment(idx);
				} while (idx != write);

				read = write = size = 0;
			}
		}
		finally {
			lock.unlock();
		}
	}

	@Override
	public boolean contains(Object o) {
		if (null == o) {
			return false;
		}

		final ReentrantLock lock = this.lock;
		lock.lock();
		try {
			return internalContains(o);
		}
		finally {
			lock.unlock();
		}
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		if (c == null || c.isEmpty()) {
			return true;
		}

		final ReentrantLock lock = this.lock;
		lock.lock();
		try {
			for (Object o : c) {
				if (!internalContains(o)) {
					return false;
				}
			}
			
			return true;
		}
		finally {
			lock.unlock();
		}
	}

	@Override
	public E element() {
		final ReentrantLock lock = this.lock;
		lock.lock();
		try {
			if (size == 0) {
				throw new NoSuchElementException("queue is empty");
			}

			return elements[read];
		}
		finally {
			lock.unlock();
		}

	}

	@Override
	public java.util.Iterator<E> iterator() {
		return new Iterator();
	}

	@Override
	public boolean isEmpty() {
		final ReentrantLock lock = this.lock;
		lock.lock();
		try {
			return size == 0;
		}
		finally {
			lock.unlock();
		}
	}

	@Override
	public boolean offer(E e) {
		return add(e);
	}

	@Override
	public E peek() {
		final ReentrantLock lock = this.lock;
		lock.lock();
		try {
			return size == 0 ? null : elements[read];
		}
		finally {
			lock.unlock();
		}
	}

	@Override
	public E poll() {
		final ReentrantLock lock = this.lock;
		lock.lock();
		try {
			return size == 0 ? null : internalRemove();
		}
		finally {
			lock.unlock();
		}
	}

	@Override
	public E remove() {
		final ReentrantLock lock = this.lock;
		lock.lock();
		try {
			if (size == 0) {
				throw new NoSuchElementException("queue is empty");
			}

			return internalRemove();
		}
		finally {
			lock.unlock();
		}
	}

	@Override
	public boolean remove(Object o) {
		if (null == o) {
			return false;
		}

		final ReentrantLock lock = this.lock;
		lock.lock();
		try {
			if (size == 0) {
				return false;
			}

			int idx = this.read;
			do {
				if (o.equals(elements[idx])) {
					internalRemoveAtIndex(idx);
					return true;
				}

				idx = increment(idx);
			} while (idx != write);

			return false;
		}
		finally {
			lock.unlock();
		}
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		Objects.requireNonNull(c);

		final ReentrantLock lock = this.lock;
		lock.lock();
		try {
			return removeIf(c::contains);
		}
		finally {
			lock.unlock();
		}
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		Objects.requireNonNull(c);

		final ReentrantLock lock = this.lock;
		lock.lock();
		try {
			return removeIf(o -> !c.contains(o));
		}
		finally {
			lock.unlock();
		}
	}

	@Override
	public int size() {
		final ReentrantLock lock = this.lock;
		lock.lock();
		try {
			return size;
		}
		finally {
			lock.unlock();
		}
	}

	@Override
	public Object[] toArray() {
		return toArray(new Object[0]);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T[] toArray(T[] a) {
		Objects.requireNonNull(a);

		final ReentrantLock lock = this.lock;
		lock.lock();
		try {
			final int size = this.size;
			final T[] result = a.length < size ? (T[]) Array.newInstance(a.getClass().getComponentType(), size) : a;

			final int n = elements.length - read;
			if (size <= n) {
				System.arraycopy(elements, read, result, 0, size);
			} else {
				System.arraycopy(elements, read, result, 0, n);
				System.arraycopy(elements, 0, result, n, size - n);
			}

			if (result.length > size) {
				for (int i = Math.max(0, size - 1); i < result.length; i++) {
					result[i] = null;
				}
			}

			return result;
		}
		finally {
			lock.unlock();
		}
	}

	public String toString() {
		final ReentrantLock lock = this.lock;
		lock.lock();
		try {
			if (size == 0) {
				return "[]";
			}

			StringBuilder sb = new StringBuilder();
			sb.append('[');

			int idx = read;
			while (true) {
				E e = elements[idx];
				sb.append(e == this ? "(this Collection)" : e);

				idx = (idx + 1) % maxElements;

				if (idx == write) {
					return sb.append(']').toString();
				} else {
					sb.append(',').append(' ');
				}
			}
		}
		finally {
			lock.unlock();
		}
	}

	/* Internal implementations: MUST BE USED INSIDE LOCKS  */
	
	private int increment(int i) {
		return (i + 1) % maxElements;
	}

	private int decrement(int i) {
		return ((i == 0) ? maxElements : i) - 1;
	}

	private void internalAdd(E e) {
		if (size == maxElements) {
			internalRemove();
		} else {
			size++;
		}

		elements[write] = e;

		write = increment(write);
	}
	
	private boolean internalContains(Object o) {
		if (size > 0) {
			int idx = read;
			do {
				if (o.equals(elements[idx])) {
					return true;
				}

				idx = (idx + 1) % maxElements;
			} while (idx != write);
		}

		return false;
	}

	private E internalRemove() {
		final E element = elements[read];

		if (null != element) {
			internalRemoveAtIndex(read);
		}

		return element;
	}

	private void internalRemoveAtIndex(int idx) {
		if (idx == read) {
			elements[read] = null;

			read = increment(read);

			size--;
			if (iterators != null) {
				iterators.elementRemoved();
			}
		} else {
			int i = idx;
			while (true) {
				int next = increment(i);

				if (next != write) {
					elements[i] = elements[next];
					i = next;
				} else {
					elements[i] = null;
					write = i;
					break;
				}
			}

			size--;
			if (iterators != null) {
				iterators.removedAt(idx);
			}
		}
	}

	private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		lock = new ReentrantLock();
	}

	/**
	 * A linked list maintaining references between the queue and any iterators
	 * 
	 * This class exists to ensure that iterator objects are properly updated when items are removed from the queue and
	 * are invalidated if the underlying queue becomes incompatible with the iterator's view of it.
	 * 
	 * This is based on the implementation of ArrayBlockingQueue.Itrs and involves the same garbage collection scheme
	 * described there.
	 */
	private class Iterators {

		private static final int SHORT_SWEEP_PROBES = 4;

		private static final int LONG_SWEEP_PROBES = 16;

		private Node head;

		private Node sweeper = null;

		int cycles = 0;

		Iterators(Iterator iterator) {
			register(iterator);
		}

		void register(Iterator iterator) {
			head = new Node(iterator, head);
		}

		void elementRemoved() {
			if (size == 0) {
				queueEmptied();
			} else if (read == 0) {
				readWrapped();
			}
		}

		void queueEmptied() {
			for (Node p = head; p != null; p = p.next) {
				Iterator it = p.get();
				if (it != null) {
					p.clear();
					it.shutdown();
				}
			}

			head = null;
			iterators = null;
		}

		void removedAt(int removedIndex) {
			prune(it -> it.removedAt(removedIndex));
		}

		void readWrapped() {
			cycles++;
			prune(Iterator::readWrapped);
		}

		void sweep(boolean tryHarder) {
			int probes = tryHarder ? LONG_SWEEP_PROBES : SHORT_SWEEP_PROBES;
			Node o, p;
			final Node sweeper = this.sweeper;
			boolean completeCycle;   // to limit search to one full sweep

			if (sweeper == null) {
				o = null;
				p = head;
				completeCycle = true;
			} else {
				o = sweeper;
				p = o.next;
				completeCycle = false;
			}

			for (; probes > 0; probes--) {
				if (p == null) {
					if (completeCycle) {
						break;
					}

					o = null;
					p = head;
					completeCycle = true;
				}

				final Iterator it = p.get();
				final Node next = p.next;
				if (it == null || it.isDetached()) {
					// found a discarded/exhausted iterator
					probes = LONG_SWEEP_PROBES; // "try harder"
					// unlink p
					p.clear();
					p.next = null;
					if (o == null) {
						head = next;
						if (next == null) {
							// We've run out of iterators to track; retire
							iterators = null;
							return;
						}
					} else {
						o.next = next;
					}
				} else {
					o = p;
				}
				p = next;
			}

			this.sweeper = (p == null) ? null : o;
		}

		private void prune(Predicate<Iterator> shouldRemove) {
			for (Node o = null, p = head; p != null; ) {
				final Iterator it = p.get();
				final Node next = p.next;

				if (it == null || shouldRemove.test(it)) {
					p.clear();
					p.next = null;

					if (o == null) {
						head = next;
					} else {
						o.next = next;
					}
				} else {
					o = p;
				}

				p = next;
			}

			if (head == null) {
				ThreadSafeCircularFifoQueue.this.iterators = null;
			}
		}

		/**
		 * The actual list node implementation
		 */
		private class Node extends WeakReference<Iterator> {

			Node next;

			Node(Iterator iterator, Node next) {
				super(iterator);
				this.next = next;
			}
		}
	}

	/**
	 * An attempt to be a straight-forward iterator implementation for a ThreadSafeCircularFifoQueue.
	 * 
	 * It should iterate over each member in the queue only once, assuming that the queue is not modified while this
	 * iterator remains in use. If the underlying queue is modified, the iterator will attempt to recover and keep going.
	 * 
	 * If it becomes too far out of synch with the underlying data, an iterator may fail before iterating over all elements
	 * in a queue. However if {@link #hasNext()} returns true, {@link #next()} will always return a result.
	 */
	private class Iterator implements java.util.Iterator<E> {

		/* Special index values */

		// indicates an index that is invalid or empty
		private static final int NONE = -1;

		// used as a value for prevRead when the iterator is no longer valid
		private static final int DETACHED = -2;

		private int nextIndex;

		private E nextItem;

		private int prevIndex = NONE;

		private E prevItem = null;

		private int prevRead;

		private int prevCycles;

		Iterator() {
			final ReentrantLock lock = ThreadSafeCircularFifoQueue.this.lock;
			lock.lock();
			try {
				if (size == 0) {
					nextIndex = NONE;
					prevRead = DETACHED;
				} else {
					nextItem = elements[read];
					nextIndex = read;
					prevRead = read;

					if (iterators == null) {
						iterators = new Iterators(this);
					} else {
						iterators.register(this);
						iterators.sweep(false);
					}

					prevCycles = iterators.cycles;
				}
			}
			finally {
				lock.unlock();
			}
		}

		@Override
		public boolean hasNext() {
			// no locks for the simplest case
			if (nextItem != null) {
				return true;
			}

			final ReentrantLock lock = ThreadSafeCircularFifoQueue.this.lock;
			lock.lock();
			try {
				if (!isDetached()) {
					updateIndices();
					if (prevIndex >= 0) {
						prevItem = elements[prevIndex];
						detach();
					}
				}
			}
			finally {
				lock.unlock();
			}
			
			return false;
		}

		@Override
		public E next() {
			final E it = nextItem;
			if (it == null) {
				throw new NoSuchElementException();
			}

			final ReentrantLock lock = ThreadSafeCircularFifoQueue.this.lock;
			lock.lock();
			try {
				if (!isDetached()) {
					updateIndices();
				}
				
				prevIndex = nextIndex;
				prevItem = it;

				if (nextIndex < 0 || nextIndex == write) {
					nextIndex = NONE;
					nextItem = null;
				} else {
					nextIndex = increment(nextIndex);
					nextItem = elements[nextIndex];
				}

				return it;
			}
			finally {
				lock.unlock();
			}
		}

		@Override
		public void remove() {
			final ReentrantLock lock = ThreadSafeCircularFifoQueue.this.lock;
			lock.lock();
			try {
				if (!isDetached()) {
					updateIndices();
				}
				
				if (prevIndex == NONE) {
					throw new IllegalStateException();
				} else if (prevIndex >= 0 && elements[prevIndex] == prevItem) {
					internalRemoveAtIndex(prevIndex);

					if (prevIndex != read) {
						nextIndex = Math.max(prevIndex, read);
					}
				}

				prevIndex = NONE;
				prevItem = null;
				
				if (nextIndex < 0) {
					detach();
				}
			}
			finally {
				lock.unlock();
			}
		}

		boolean readWrapped() {
			if (isDetached()) {
				return true;
			}

			if (iterators.cycles - prevCycles > 1) {
				shutdown();
				return true;
			}

			return false;
		}

		boolean removedAt(int removedIndex) {
			if (isDetached()) {
				return true;
			}

			final int cycles = iterators.cycles;
			final int read = ThreadSafeCircularFifoQueue.this.read;
			
			int cycleDiff = cycles - prevCycles;

			if (removedIndex < read) {
				cycleDiff++;
			}

			final int removedDistance = (cycleDiff * maxElements) + (removedIndex - prevRead);

			if (prevIndex >= 0) {
				int x = distance(prevIndex);
				if (x == removedDistance) {
					prevIndex = NONE;
				} else if (x > removedDistance) {
					prevIndex = decrement(prevIndex);
				}
			}

			if (nextIndex >= 0) {
				int x = distance(nextIndex);
				if (x == removedDistance) {
					nextIndex = NONE;
				} else if (x > removedDistance) {
					nextIndex = decrement(nextIndex);
				}
			} else if (prevIndex < 0) {
				// don't call detach() as returning true will trigger a full sweep anyways
				prevRead = DETACHED;
				return true;
			}

			return false;
		}

		void shutdown() {
			// nextItem is not set to null as it has been cached and can be returned
			nextIndex = nextIndex >= 0 ? NONE : nextIndex;
			prevIndex = prevIndex >= 0 ? NONE : nextIndex;
			prevItem = null;
			prevRead = DETACHED;
		}

		/**
		 * Detach the iterator and trigger a sweep of the iterator queue
		 */
		private void detach() {
			if (prevRead >= 0) {
				prevRead = DETACHED;
				iterators.sweep(true);
			}
		}

		private boolean isDetached() {
			return prevRead < 0;
		}

		private int distance(int index) {
			int distance = index - prevRead;
			if (distance < 0) {
				distance += maxElements;
			}
			return distance;
		}
		
		private boolean indexInvalidated(int index, long dequeues) {
			if (index < 0) {
				return false;
			}
			
			int distance = distance(index);
			
			return dequeues > distance;
		}
		
		private void updateIndices() {
			final int cycles = ThreadSafeCircularFifoQueue.this.iterators.cycles;
			if (cycles != prevCycles || read != prevRead) {
				long dequeues = (cycles - prevCycles) * maxElements + (read - prevRead);
				
				if (indexInvalidated(prevIndex, dequeues)) {
					prevIndex = NONE;
				}

				if (indexInvalidated(nextIndex, dequeues)) {
					nextIndex = NONE;
				}
				
				if (prevIndex < 0 && nextIndex < 0) {
					detach();
				} else {
					prevCycles = cycles;
					prevRead = read;
				}
			}
		}
	}
}
