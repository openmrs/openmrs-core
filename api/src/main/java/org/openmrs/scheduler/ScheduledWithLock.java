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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.core.annotation.AliasFor;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * A composed annotation that combines {@link Scheduled} and {@link SchedulerLock}.
 * <p>
 * Please use {@link ScheduledWithLock} instead of {@link Scheduled} and {@link SchedulerLock}
 * in case we ever need to change the implementation.
 * <p>
 * Remember to always use a unique name as you must not have 2 tasks with the same name in the system. 
 * Use `yourmoduleid.` prefix for tasks defined in modules.
 * <p>
 * <b>Do not use {@link Scheduled} alone since it doesn't prevent the task from being
 * run concurrently by multiple replicas in a clustered environment.</b>
 * <p>
 * Please note that the runs and results of running methods annotated with {@link ScheduledWithLock} are not persisted 
 * in any way. It means that the number of runs, failures or successes can only be determined by looking at server logs. 
 * Use it for internal operations defined in code that do not need to be checked by users and when the runs and results 
 * are monitored by system administrators.
 * <p>
 * Please use {@link SchedulerService} for persistent background tasks instead.
 * <p>
 * Please see {@link SchedulerConfig} for how it is configured and ScheduledWithLockTest.TestTask for usage.
 *
 * @since 2.9.x
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Scheduled
@SchedulerLock
public @interface ScheduledWithLock {

	@AliasFor(annotation = Scheduled.class, attribute = "cron")
	String cron() default "";

	@AliasFor(annotation = Scheduled.class, attribute = "fixedDelay")
	long fixedDelay() default -1;

	@AliasFor(annotation = Scheduled.class, attribute = "fixedRate")
	long fixedRate() default -1;

	@AliasFor(annotation = SchedulerLock.class, attribute = "name")
	String name();

	@AliasFor(annotation = SchedulerLock.class, attribute = "lockAtMostFor")
	String lockAtMostFor() default "";

	@AliasFor(annotation = SchedulerLock.class, attribute = "lockAtLeastFor")
	String lockAtLeastFor() default "";
}
