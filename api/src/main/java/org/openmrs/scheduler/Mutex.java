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

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Guarantees execution of a method by one thread at a time even in a clustered environment.
 * It uses DB as a locking mechanism.
 * <p>
 * <b>Concurrent executions are not queued up. They are skipped if lock is taken.</b>
 * <p>
 * It is implemented with {@link SchedulerLock}. Please use {@link Mutex} instead of {@link SchedulerLock}
 * in case we ever need to change the implementation.
 * <p>
 * Remember to always use a unique name to avoid conflicts with other executions in the system. 
 * Use `yourmoduleid.` prefix for executions defined in modules.
 *
 * @since 2.9.x
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@SchedulerLock
public @interface Mutex {
	
	@AliasFor(annotation = SchedulerLock.class, attribute = "name")
	String name();

	@AliasFor(annotation = SchedulerLock.class, attribute = "lockAtMostFor")
	String lockAtMostFor() default "";

	@AliasFor(annotation = SchedulerLock.class, attribute = "lockAtLeastFor")
	String lockAtLeastFor() default "";
}
