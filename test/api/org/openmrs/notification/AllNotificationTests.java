package org.openmrs.notification;

import org.openmrs.notification.db.AllNotificationDbTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	AlertServiceTest.class,
	AllNotificationDbTests.class,
	MessageServiceTest.class,
	MessageTest.class
})
public class AllNotificationTests{}