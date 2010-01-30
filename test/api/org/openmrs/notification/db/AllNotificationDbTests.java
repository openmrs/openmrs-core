package org.openmrs.notification.db;

import org.openmrs.notification.db.hibernate.AllNotificationDbHibernateTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	AllNotificationDbHibernateTests.class
})
public class AllNotificationDbTests{}