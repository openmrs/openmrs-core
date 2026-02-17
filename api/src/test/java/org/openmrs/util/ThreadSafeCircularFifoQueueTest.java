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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections4.queue.AbstractQueueTest;

/**
 * Unit tests for the {@link ThreadSafeCircularFifoQueue} class
 * 
 * This class uses the tests from Apache Commons Collections for Queues to ensure the correctness of the implementation
 * @param <E>
 */
public class ThreadSafeCircularFifoQueueTest<E> extends AbstractQueueTest<E> {

	public ThreadSafeCircularFifoQueueTest(String testName) {
		super(testName);
	}

	/* Configuration */
	
	@Override
	public boolean isFailFastSupported() {
		return false;
	}

	@Override
	public boolean isNullSupported() {
		return false;
	}

	// NB This is marked as false not because we don't support serialization, but because we don't provide the artefacts
	// needed for these tests
	@Override
	public boolean isTestSerialization() {
		return false;
	}

	@Override
	public Queue<E> makeObject() {
		return new ThreadSafeCircularFifoQueue<>(100);
	}
	
	/* Test overrides */
	/* included here to allow running individual test cases */

	@Override
	public void testQueueOffer() {
		super.testQueueOffer();
	}

	@Override
	public void testQueueElement() {
		super.testQueueElement();
	}

	@Override
	public void testQueuePeek() {
		super.testQueuePeek();
	}

	@Override
	public void testQueueRemove() {
		super.testQueueRemove();
	}

	@Override
	public void testQueuePoll() {
		super.testQueuePoll();
	}

	@Override
	public void testEmptyQueueSerialization() throws IOException, ClassNotFoundException {
		super.testEmptyQueueSerialization();
	}

	@Override
	public void testFullQueueSerialization() throws IOException, ClassNotFoundException {
		super.testFullQueueSerialization();
	}

	@Override
	public void testEmptyQueueCompatibility() throws IOException, ClassNotFoundException {
		super.testEmptyQueueCompatibility();
	}

	@Override
	public void testFullQueueCompatibility() throws IOException, ClassNotFoundException {
		super.testFullQueueCompatibility();
	}

	@Override
	public void testCollectionAdd() {
		super.testCollectionAdd();
	}

	@Override
	public void testCollectionAddAll() {
		super.testCollectionAddAll();
	}

	@Override
	public void testUnsupportedAdd() {
		super.testUnsupportedAdd();
	}

	@Override
	public void testCollectionClear() {
		super.testCollectionClear();
	}

	@Override
	public void testCollectionContains() {
		super.testCollectionContains();
	}

	@Override
	public void testCollectionContainsAll() {
		super.testCollectionContainsAll();
	}

	@Override
	public void testCollectionIsEmpty() {
		super.testCollectionIsEmpty();
	}

	@Override
	public void testCollectionIterator() {
		super.testCollectionIterator();
	}

	@Override
	public void testCollectionIteratorRemove() {
		super.testCollectionIteratorRemove();
	}

	@Override
	public void testCollectionRemove() {
		super.testCollectionRemove();
	}

	@Override
	public void testCollectionRemoveAll() {
		super.testCollectionRemoveAll();
	}

	@Override
	public void testCollectionRemoveIf() {
		super.testCollectionRemoveIf();
	}

	@Override
	public void testCollectionRetainAll() {
		super.testCollectionRetainAll();
	}

	@Override
	public void testCollectionSize() {
		super.testCollectionSize();
	}

	@Override
	public void testCollectionToArray() {
		super.testCollectionToArray();
	}

	@Override
	public void testCollectionToArray2() {
		super.testCollectionToArray2();
	}

	@Override
	public void testCollectionToString() {
		super.testCollectionToString();
	}

	@Override
	public void testObjectEqualsSelf() {
		super.testObjectEqualsSelf();
	}

	@Override
	public void testEqualsNull() {
		super.testEqualsNull();
	}

	@Override
	public void testObjectHashCodeEqualsSelfHashCode() {
		super.testObjectHashCodeEqualsSelfHashCode();
	}

	@Override
	public void testObjectHashCodeEqualsContract() {
		super.testObjectHashCodeEqualsContract();
	}

	@Override
	public void testSimpleSerialization() throws Exception {
		super.testSimpleSerialization();
	}

	@Override
	public void testCanonicalEmptyCollectionExists() {
		super.testCanonicalEmptyCollectionExists();
	}

	@Override
	public void testCanonicalFullCollectionExists() {
		super.testCanonicalFullCollectionExists();
	}

	/* Circular eviction tests */

	public void testCircularEviction_shouldEvictOldestElementWhenFull() {
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

	public void testCircularEviction_shouldMaintainCorrectSizeAfterMultipleEvictions() {
		ThreadSafeCircularFifoQueue<Integer> queue = new ThreadSafeCircularFifoQueue<>(3);

		// Add 10 elements to a queue of size 3
		for (int i = 1; i <= 10; i++) {
			queue.add(i);
			assertTrue("Size should never exceed capacity", queue.size() <= 3);
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

	public void testCircularEviction_shouldWorkCorrectlyWithIterator() {
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

	public void testCircularEviction_toArrayShouldReturnCorrectElements() {
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

	public void testConcurrentAdds_shouldMaintainConsistentState() throws InterruptedException {
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
		assertTrue("Threads should complete within timeout", doneLatch.await(30, TimeUnit.SECONDS));
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

	public void testConcurrentAddsAndPolls_shouldNotCorruptState() throws InterruptedException {
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
		assertTrue("Threads should complete within timeout", doneLatch.await(30, TimeUnit.SECONDS));
		executor.shutdown();

		// Size should be non-negative and within capacity
		int size = queue.size();
		assertTrue("Size should be non-negative", size >= 0);
		assertTrue("Size should not exceed capacity", size <= 50);

		// Verify iteration works and count matches size
		int iterCount = 0;
		for (Integer ignored : queue) {
			iterCount++;
		}
		assertEquals(size, iterCount);
	}

	public void testConcurrentIteration_shouldNotThrowConcurrentModificationException() throws InterruptedException {
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
		assertTrue("Threads should complete within timeout", doneLatch.await(30, TimeUnit.SECONDS));
		executor.shutdown();

		assertEquals("No exceptions should occur during concurrent access", 0, errorCount.get());
	}
}
