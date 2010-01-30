package org.openmrs.scheduler;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	SchedulerServiceTest.class,
	SchedulerUtilTest.class
})
public class AllSchedulerTests{}