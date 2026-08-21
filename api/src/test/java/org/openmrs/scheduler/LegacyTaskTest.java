/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.scheduler;

import org.junit.jupiter.api.Test;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.validator.SchedulerFormValidatorTest.ConstructorSideEffectNonTask;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link LegacyTask}.
 */
public class LegacyTaskTest extends BaseContextSensitiveTest {

	/**
	 * Unlike {@link org.openmrs.validator.SchedulerFormValidator}, the JobRunr scheduler service does
	 * not validate a {@link TaskDefinition} before persisting or running it, so a definition whose task
	 * class does not implement {@link Task} can reach {@link LegacyTask#execute} unchecked. This pins
	 * LegacyTask's own runtime guard: such a class must be rejected before it is instantiated, so that
	 * a constructor or static initializer with side effects never runs.
	 *
	 * @see LegacyTask#execute(TaskDefinition, TaskContext)
	 */
	@Test
	public void execute_shouldRejectANonTaskClassBeforeInstantiatingIt() {
		ConstructorSideEffectNonTask.instantiated = false;

		TaskDefinition def = new TaskDefinition();
		def.setName("Chores");
		def.setRepeatInterval(3600000L);
		def.setTaskClass(ConstructorSideEffectNonTask.class.getName());

		// Catch the broad type so we can inspect the outcome ourselves: without the type check the
		// class is instantiated first and the (Task) cast fails with a ClassCastException, whereas the
		// guard rejects it up front with a TaskException and never constructs it.
		Exception exception = assertThrows(Exception.class, () -> new LegacyTask().execute(def, null));

		assertFalse(ConstructorSideEffectNonTask.instantiated,
		    "a class that does not implement Task must be rejected before it is instantiated");
		assertTrue(exception instanceof TaskException,
		    "rejection must surface as a TaskException, not a ClassCastException after instantiation");
		assertTrue(exception.getMessage().contains("must implement Task"));
	}
}
