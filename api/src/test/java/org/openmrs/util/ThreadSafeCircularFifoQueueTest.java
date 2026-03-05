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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link ThreadSafeCircularFifoQueue} class
 */
class ThreadSafeCircularFifoQueueTest {

	/* Queue contract tests */

	@Test
	void shouldOfferElementSuccessfully() {
		Queue<String> queue = new ThreadSafeCircularFifoQueue<>(100);
		assertTrue(queue.offer("element"));
		assertEquals(1, queue.size());
	}

	@Test
	void shouldOfferMultipleElementsSequentially() {
		Queue<String> queue = new ThreadSafeCircularFifoQueue<>(100);
		String[] elements = {"a", "b", "c", "d", "e"};
		int size = 0;
		for (String element : elements) {
			assertTrue(queue.offer(element));
			size++;
			assertEquals(size, queue.size());
			assertTrue(queue.contains(element));
		}
	}

	@Test
	void shouldReturnElementWithoutRemoving() {
		Queue<String> queue = new ThreadSafeCircularFifoQueue<>(100);
		queue.add("first");
		assertEquals("first", queue.element());
		assertEquals(1, queue.size());
	}

	@Test
	void shouldThrowOnElementWhenEmpty() {
		Queue<String> queue = new ThreadSafeCircularFifoQueue<>(100);
		assertThrows(NoSuchElementException.class, queue::element);
	}

	@Test
	void shouldDrainViaElementAndRemove() {
		Queue<String> queue = new ThreadSafeCircularFifoQueue<>(100);
		List<String> elements = Arrays.asList("a", "b", "c", "d", "e");
		queue.addAll(elements);
		List<String> confirmed = new ArrayList<>(elements);
		for (int i = 0; i < elements.size(); i++) {
			String head = queue.element();
			assertTrue(confirmed.contains(head));
			assertEquals(head, queue.remove());
			confirmed.remove(head);
			assertEquals(confirmed.size(), queue.size());
		}
		assertThrows(NoSuchElementException.class, queue::element);
		assertThrows(NoSuchElementException.class, queue::remove);
	}

	@Test
	void shouldPeekWithoutRemoving() {
		Queue<String> queue = new ThreadSafeCircularFifoQueue<>(100);
		assertNull(queue.peek());
		queue.add("first");
		assertEquals("first", queue.peek());
		assertEquals(1, queue.size());
	}

	@Test
	void shouldDrainViaPeekAndPoll() {
		Queue<String> queue = new ThreadSafeCircularFifoQueue<>(100);
		List<String> elements = Arrays.asList("a", "b", "c", "d", "e");
		queue.addAll(elements);
		List<String> confirmed = new ArrayList<>(elements);
		for (int i = 0; i < elements.size(); i++) {
			String head = queue.peek();
			assertNotNull(head);
			assertTrue(confirmed.contains(head));
			assertEquals(head, queue.poll());
			confirmed.remove(head);
			assertEquals(confirmed.size(), queue.size());
		}
		assertNull(queue.peek());
		assertNull(queue.poll());
	}

	@Test
	void shouldRemoveHead() {
		Queue<String> queue = new ThreadSafeCircularFifoQueue<>(100);
		queue.add("first");
		queue.add("second");
		assertEquals("first", queue.remove());
		assertEquals(1, queue.size());
	}

	@Test
	void shouldThrowOnRemoveWhenEmpty() {
		Queue<String> queue = new ThreadSafeCircularFifoQueue<>(100);
		assertThrows(NoSuchElementException.class, queue::remove);
	}

	@Test
	void shouldPollHead() {
		Queue<String> queue = new ThreadSafeCircularFifoQueue<>(100);
		assertNull(queue.poll());
		queue.add("first");
		queue.add("second");
		assertEquals("first", queue.poll());
		assertEquals(1, queue.size());
	}

	@Test
	void shouldAddElement() {
		Queue<String> queue = new ThreadSafeCircularFifoQueue<>(100);
		assertTrue(queue.add("element"));
		assertTrue(queue.contains("element"));
	}

	@Test
	void shouldAddMultipleElementsIndividually() {
		Queue<String> queue = new ThreadSafeCircularFifoQueue<>(100);
		String[] elements = {"a", "b", "c", "d", "e"};
		int size = 0;
		for (String element : elements) {
			assertTrue(queue.add(element));
			size++;
			assertEquals(size, queue.size());
			assertTrue(queue.contains(element));
		}
	}

	@Test
	void shouldAddAllElements() {
		Queue<String> queue = new ThreadSafeCircularFifoQueue<>(100);
		assertTrue(queue.addAll(Arrays.asList("a", "b", "c")));
		assertEquals(3, queue.size());
		assertTrue(queue.containsAll(Arrays.asList("a", "b", "c")));
	}

	@Test
	void shouldWorkWithHeterogeneousElements() {
		Queue<Object> queue = new ThreadSafeCircularFifoQueue<>(100);
		queue.add("string");
		queue.add(Integer.valueOf(42));
		queue.add(Double.valueOf(3.14));
		queue.add(Long.valueOf(100L));
		queue.add(Short.valueOf((short) 7));
		assertEquals(5, queue.size());
		assertTrue(queue.contains("string"));
		assertTrue(queue.contains(Integer.valueOf(42)));
		assertTrue(queue.contains(Double.valueOf(3.14)));
		assertEquals("string", queue.poll());
		assertEquals(Integer.valueOf(42), queue.poll());
	}

	@Test
	void shouldClearAllElements() {
		Queue<String> queue = new ThreadSafeCircularFifoQueue<>(100);
		queue.add("a");
		queue.add("b");
		queue.clear();
		assertTrue(queue.isEmpty());
		assertEquals(0, queue.size());
	}

	@Test
	void shouldReportContains() {
		Queue<String> queue = new ThreadSafeCircularFifoQueue<>(100);
		queue.add("a");
		assertTrue(queue.contains("a"));
		assertFalse(queue.contains("b"));
	}

	@Test
	void shouldReturnFalseForContainsNull() {
		Queue<String> queue = new ThreadSafeCircularFifoQueue<>(100);
		queue.add("a");
		assertFalse(queue.contains(null));
	}

	@Test
	void shouldReportContainsAll() {
		Queue<String> queue = new ThreadSafeCircularFifoQueue<>(100);
		queue.addAll(Arrays.asList("a", "b", "c"));
		assertTrue(queue.containsAll(Arrays.asList("a", "b")));
		assertFalse(queue.containsAll(Arrays.asList("a", "d")));
	}

	@Test
	void shouldReportIsEmpty() {
		Queue<String> queue = new ThreadSafeCircularFifoQueue<>(100);
		assertTrue(queue.isEmpty());
		queue.add("a");
		assertFalse(queue.isEmpty());
	}

	@Test
	void shouldIterateElements() {
		Queue<String> queue = new ThreadSafeCircularFifoQueue<>(100);
		queue.addAll(Arrays.asList("a", "b", "c"));
		Iterator<String> iter = queue.iterator();
		assertTrue(iter.hasNext());
		assertEquals("a", iter.next());
		assertEquals("b", iter.next());
		assertEquals("c", iter.next());
		assertFalse(iter.hasNext());
	}

	@Test
	void shouldRemoveElement() {
		Queue<String> queue = new ThreadSafeCircularFifoQueue<>(100);
		queue.addAll(Arrays.asList("a", "b", "c"));
		assertTrue(queue.remove("b"));
		assertEquals(2, queue.size());
		assertFalse(queue.contains("b"));
	}

	@Test
	void shouldReturnFalseWhenRemovingNonExistentElement() {
		Queue<String> queue = new ThreadSafeCircularFifoQueue<>(100);
		queue.addAll(Arrays.asList("a", "b", "c"));
		assertFalse(queue.remove("z"));
		assertEquals(3, queue.size());
	}

	@Test
	void shouldRemoveAllElements() {
		Queue<String> queue = new ThreadSafeCircularFifoQueue<>(100);
		queue.addAll(Arrays.asList("a", "b", "c", "d"));
		assertTrue(queue.removeAll(Arrays.asList("b", "c")));
		assertEquals(2, queue.size());
		assertTrue(queue.containsAll(Arrays.asList("a", "d")));
	}

	@Test
	void shouldReturnFalseWhenRemoveAllWithNonExistentElements() {
		Queue<String> queue = new ThreadSafeCircularFifoQueue<>(100);
		queue.addAll(Arrays.asList("a", "b", "c"));
		assertFalse(queue.removeAll(Arrays.asList("x", "y", "z")));
		assertEquals(3, queue.size());
	}

	@Test
	void shouldRetainAllElements() {
		Queue<String> queue = new ThreadSafeCircularFifoQueue<>(100);
		queue.addAll(Arrays.asList("a", "b", "c", "d"));
		assertTrue(queue.retainAll(Arrays.asList("b", "c")));
		assertEquals(2, queue.size());
		assertTrue(queue.containsAll(Arrays.asList("b", "c")));
	}

	@Test
	void shouldReportCorrectSize() {
		Queue<String> queue = new ThreadSafeCircularFifoQueue<>(100);
		assertEquals(0, queue.size());
		queue.add("a");
		assertEquals(1, queue.size());
		queue.add("b");
		assertEquals(2, queue.size());
		queue.poll();
		assertEquals(1, queue.size());
	}

	@Test
	void shouldConvertToArray() {
		Queue<String> queue = new ThreadSafeCircularFifoQueue<>(100);
		queue.addAll(Arrays.asList("a", "b", "c"));
		Object[] array = queue.toArray();
		assertEquals(3, array.length);
		assertEquals("a", array[0]);
		assertEquals("b", array[1]);
		assertEquals("c", array[2]);
	}

	@Test
	void shouldConvertToTypedArray() {
		Queue<String> queue = new ThreadSafeCircularFifoQueue<>(100);
		queue.addAll(Arrays.asList("a", "b", "c"));
		// Undersized array — should allocate new
		String[] undersized = queue.toArray(new String[0]);
		assertEquals(3, undersized.length);
		assertEquals("a", undersized[0]);
		assertEquals("b", undersized[1]);
		assertEquals("c", undersized[2]);
		// Exact-size array — should reuse
		String[] exact = queue.toArray(new String[3]);
		assertEquals(3, exact.length);
		assertEquals("a", exact[0]);
		assertEquals("b", exact[1]);
		assertEquals("c", exact[2]);
	}

	@Test
	void shouldReturnNonNullToString() {
		Queue<String> queue = new ThreadSafeCircularFifoQueue<>(100);
		assertNotNull(queue.toString());
		queue.add("a");
		assertNotNull(queue.toString());
	}

	@Test
	void shouldRejectNullElements() {
		Queue<String> queue = new ThreadSafeCircularFifoQueue<>(100);
		assertThrows(NullPointerException.class, () -> queue.add(null));
		assertThrows(NullPointerException.class, () -> queue.offer(null));
		assertTrue(queue.isEmpty());
	}

	@Test
	void shouldRemoveViaIterator() {
		Queue<String> queue = new ThreadSafeCircularFifoQueue<>(100);
		queue.addAll(Arrays.asList("a", "b", "c"));
		Iterator<String> iter = queue.iterator();
		assertEquals("a", iter.next());
		iter.remove();
		assertEquals(2, queue.size());
		assertFalse(queue.contains("a"));
		assertEquals("b", iter.next());
		iter.remove();
		assertEquals(1, queue.size());
		assertTrue(queue.contains("c"));
	}

	@Test
	void shouldRemoveIfMatchingPredicate() {
		Queue<Integer> queue = new ThreadSafeCircularFifoQueue<>(100);
		queue.addAll(Arrays.asList(1, 2, 3, 4, 5));
		assertTrue(queue.removeIf(n -> n % 2 == 0));
		assertEquals(3, queue.size());
		assertFalse(queue.contains(2));
		assertFalse(queue.contains(4));
		assertTrue(queue.containsAll(Arrays.asList(1, 3, 5)));
	}

	@Test
	void shouldSerializeAndDeserialize() throws Exception {
		ThreadSafeCircularFifoQueue<String> queue = new ThreadSafeCircularFifoQueue<>(5);
		queue.addAll(Arrays.asList("a", "b", "c"));

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
			oos.writeObject(queue);
		}

		@SuppressWarnings("unchecked")
		ThreadSafeCircularFifoQueue<String> deserialized =
			(ThreadSafeCircularFifoQueue<String>) new ObjectInputStream(
				new ByteArrayInputStream(baos.toByteArray())).readObject();

		assertEquals(queue.size(), deserialized.size());
		assertEquals("a", deserialized.poll());
		assertEquals("b", deserialized.poll());
		assertEquals("c", deserialized.poll());
		assertTrue(deserialized.isEmpty());
	}

	@Test
	void shouldHandleEmptyCollectionOperations() {
		Queue<String> queue = new ThreadSafeCircularFifoQueue<>(100);
		queue.add("a");
		assertFalse(queue.removeAll(Collections.emptyList()));
		assertFalse(queue.retainAll(Arrays.asList("a")));
		assertEquals(1, queue.size());
	}

	@Test
	void shouldIterateEmptyQueue() {
		Queue<String> queue = new ThreadSafeCircularFifoQueue<>(100);
		Iterator<String> iter = queue.iterator();
		assertFalse(iter.hasNext());
		assertThrows(NoSuchElementException.class, iter::next);
	}

	@Test
	void shouldThrowOnIteratorRemoveBeforeNext() {
		Queue<String> queue = new ThreadSafeCircularFifoQueue<>(100);
		queue.add("a");
		Iterator<String> iter = queue.iterator();
		assertThrows(IllegalStateException.class, iter::remove);
	}

	@Test
	void shouldThrowOnIteratorDoubleRemove() {
		Queue<String> queue = new ThreadSafeCircularFifoQueue<>(100);
		queue.addAll(Arrays.asList("a", "b", "c"));
		Iterator<String> iter = queue.iterator();
		iter.next();
		iter.remove();
		assertThrows(IllegalStateException.class, iter::remove);
	}

	@Test
	void shouldDrainAllViaIteratorRemove() {
		Queue<String> queue = new ThreadSafeCircularFifoQueue<>(100);
		queue.addAll(Arrays.asList("a", "b", "c", "d", "e"));
		Iterator<String> iter = queue.iterator();
		while (iter.hasNext()) {
			iter.next();
			iter.remove();
		}
		assertTrue(queue.isEmpty());
		assertEquals(0, queue.size());
	}

	@Test
	void shouldThrowOnNextBeyondEnd() {
		Queue<String> queue = new ThreadSafeCircularFifoQueue<>(100);
		queue.add("a");
		Iterator<String> iter = queue.iterator();
		iter.next();
		assertThrows(NoSuchElementException.class, iter::next);
	}

	/* Object contract tests */

	@Test
	void shouldEqualSelf() {
		Queue<String> queue = new ThreadSafeCircularFifoQueue<>(100);
		queue.add("a");
		assertEquals(queue, queue);
	}

	@Test
	void shouldNotEqualNull() {
		Queue<String> queue = new ThreadSafeCircularFifoQueue<>(100);
		assertNotEquals(null, queue);
	}

	@Test
	void shouldHaveRepeatableHashCode() {
		Queue<String> queue = new ThreadSafeCircularFifoQueue<>(100);
		queue.addAll(Arrays.asList("a", "b", "c"));
		assertEquals(queue.hashCode(), queue.hashCode());
	}

	@Test
	void shouldSerializeEmptyQueue() throws Exception {
		ThreadSafeCircularFifoQueue<String> queue = new ThreadSafeCircularFifoQueue<>(10);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
			oos.writeObject(queue);
		}

		@SuppressWarnings("unchecked")
		ThreadSafeCircularFifoQueue<String> deserialized =
			(ThreadSafeCircularFifoQueue<String>) new ObjectInputStream(
				new ByteArrayInputStream(baos.toByteArray())).readObject();

		assertEquals(0, deserialized.size());
		assertTrue(deserialized.isEmpty());
		assertNull(deserialized.poll());
	}

	/* Circular eviction tests */

	@Test
	void shouldEvictOldestElementWhenFull() {
		ThreadSafeCircularFifoQueue<String> queue = new ThreadSafeCircularFifoQueue<>(3);
		queue.add("1");
		queue.add("2");
		queue.add("3");

		assertEquals(3, queue.size());

		// Adding a 4th element should evict "1"
		queue.add("4");

		assertEquals(3, queue.size());
		assertFalse(queue.contains("1"));
		assertTrue(queue.contains("2"));
		assertTrue(queue.contains("3"));
		assertTrue(queue.contains("4"));

		// Verify FIFO order
		assertEquals("2", queue.poll());
		assertEquals("3", queue.poll());
		assertEquals("4", queue.poll());
		assertTrue(queue.isEmpty());
	}

	@Test
	void shouldMaintainCorrectSizeAfterMultipleEvictions() {
		ThreadSafeCircularFifoQueue<Integer> queue = new ThreadSafeCircularFifoQueue<>(3);

		// Add 10 elements to a queue of size 3
		for (int i = 1; i <= 10; i++) {
			queue.add(i);
			assertTrue(queue.size() <= 3, "Size should never exceed capacity");
		}

		assertEquals(3, queue.size());

		// Should contain only the last 3 elements
		assertTrue(queue.contains(8));
		assertTrue(queue.contains(9));
		assertTrue(queue.contains(10));

		// Verify iteration works correctly
		List<Integer> elements = new ArrayList<>();
		for (Integer elem : queue) {
			elements.add(elem);
		}
		assertEquals(3, elements.size());
		assertEquals(Integer.valueOf(8), elements.get(0));
		assertEquals(Integer.valueOf(9), elements.get(1));
		assertEquals(Integer.valueOf(10), elements.get(2));
	}

	@Test
	void shouldWorkCorrectlyWithIteratorAfterEviction() {
		ThreadSafeCircularFifoQueue<String> queue = new ThreadSafeCircularFifoQueue<>(3);
		queue.add("a");
		queue.add("b");
		queue.add("c");
		queue.add("d"); // evicts "a"

		Iterator<String> iter = queue.iterator();
		assertTrue(iter.hasNext());
		assertEquals("b", iter.next());
		assertTrue(iter.hasNext());
		assertEquals("c", iter.next());
		assertTrue(iter.hasNext());
		assertEquals("d", iter.next());
		assertFalse(iter.hasNext());
	}

	@Test
	void shouldReturnCorrectArrayAfterEviction() {
		ThreadSafeCircularFifoQueue<String> queue = new ThreadSafeCircularFifoQueue<>(3);
		queue.add("1");
		queue.add("2");
		queue.add("3");
		queue.add("4"); // evicts "1"
		queue.add("5"); // evicts "2"

		Object[] array = queue.toArray();
		assertEquals(3, array.length);
		assertEquals("3", array[0]);
		assertEquals("4", array[1]);
		assertEquals("5", array[2]);
	}

	/* Thread safety tests */

	@Test
	void shouldMaintainConsistentStateUnderConcurrentAdds() throws InterruptedException {
		final ThreadSafeCircularFifoQueue<Integer> queue = new ThreadSafeCircularFifoQueue<>(100);
		final int numThreads = 10;
		final int addsPerThread = 1000;
		final CountDownLatch startLatch = new CountDownLatch(1);
		final CountDownLatch doneLatch = new CountDownLatch(numThreads);

		ExecutorService executor = Executors.newFixedThreadPool(numThreads);

		for (int t = 0; t < numThreads; t++) {
			final int threadId = t;
			executor.submit(() -> {
				try {
					startLatch.await();
					for (int i = 0; i < addsPerThread; i++) {
						queue.add(threadId * addsPerThread + i);
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} finally {
					doneLatch.countDown();
				}
			});
		}

		startLatch.countDown();
		assertTrue(doneLatch.await(30, TimeUnit.SECONDS), "Threads should complete within timeout");
		executor.shutdown();

		// Queue should have exactly 100 elements (its capacity)
		assertEquals(100, queue.size());

		// Verify iteration doesn't throw and returns correct count
		int count = 0;
		for (Integer ignored : queue) {
			count++;
		}
		assertEquals(100, count);
	}

	@Test
	void shouldNotCorruptStateUnderConcurrentAddsAndPolls() throws InterruptedException {
		final ThreadSafeCircularFifoQueue<Integer> queue = new ThreadSafeCircularFifoQueue<>(50);
		final int numProducers = 5;
		final int numConsumers = 5;
		final int operationsPerThread = 1000;
		final CountDownLatch startLatch = new CountDownLatch(1);
		final CountDownLatch doneLatch = new CountDownLatch(numProducers + numConsumers);
		final AtomicInteger addCount = new AtomicInteger(0);
		final AtomicInteger pollCount = new AtomicInteger(0);

		ExecutorService executor = Executors.newFixedThreadPool(numProducers + numConsumers);

		// Producers
		for (int t = 0; t < numProducers; t++) {
			executor.submit(() -> {
				try {
					startLatch.await();
					for (int i = 0; i < operationsPerThread; i++) {
						queue.add(i);
						addCount.incrementAndGet();
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} finally {
					doneLatch.countDown();
				}
			});
		}

		// Consumers
		for (int t = 0; t < numConsumers; t++) {
			executor.submit(() -> {
				try {
					startLatch.await();
					for (int i = 0; i < operationsPerThread; i++) {
						if (queue.poll() != null) {
							pollCount.incrementAndGet();
						}
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} finally {
					doneLatch.countDown();
				}
			});
		}

		startLatch.countDown();
		assertTrue(doneLatch.await(30, TimeUnit.SECONDS), "Threads should complete within timeout");
		executor.shutdown();

		// Size should be non-negative and within capacity
		int size = queue.size();
		assertTrue(size >= 0, "Size should be non-negative");
		assertTrue(size <= 50, "Size should not exceed capacity");

		// Verify iteration works and count matches size
		int iterCount = 0;
		for (Integer ignored : queue) {
			iterCount++;
		}
		assertEquals(size, iterCount);
	}

	@Test
	void shouldNotThrowConcurrentModificationExceptionDuringConcurrentIteration() throws InterruptedException {
		final ThreadSafeCircularFifoQueue<Integer> queue = new ThreadSafeCircularFifoQueue<>(100);

		// Pre-fill the queue
		for (int i = 0; i < 100; i++) {
			queue.add(i);
		}

		final int numThreads = 10;
		final CountDownLatch startLatch = new CountDownLatch(1);
		final CountDownLatch doneLatch = new CountDownLatch(numThreads);
		final AtomicInteger errorCount = new AtomicInteger(0);

		ExecutorService executor = Executors.newFixedThreadPool(numThreads);

		for (int t = 0; t < numThreads; t++) {
			final boolean isWriter = (t % 2 == 0);
			executor.submit(() -> {
				try {
					startLatch.await();
					for (int i = 0; i < 100; i++) {
						if (isWriter) {
							queue.add(i);
						} else {
							// Iterate while others are writing
							for (Integer ignored : queue) {
								// Just iterate through
							}
						}
					}
				} catch (Exception e) {
					errorCount.incrementAndGet();
				} finally {
					doneLatch.countDown();
				}
			});
		}

		startLatch.countDown();
		assertTrue(doneLatch.await(30, TimeUnit.SECONDS), "Threads should complete within timeout");
		executor.shutdown();

		assertEquals(0, errorCount.get(), "No exceptions should occur during concurrent access");
	}
}
