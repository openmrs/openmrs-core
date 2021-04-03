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
import java.util.Queue;

import org.apache.commons.collections4.queue.AbstractQueueTest;

/**
 * Unit tests for the {@link ThreadSafeCircularFifoQueue} class
 * 
 * This class uses the tests from Apache Commons Collections for Queues to ensure the correctness of the implementation
 * @param <E>
 */
public class ThreadSafeCircularFifoQueueTest<E> extends AbstractQueueTest<E> {
	// TODO We should add some tests related to concurrent access

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
}
