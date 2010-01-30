package org.openmrs.util;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	DatabaseUpdaterTest.class,
	HandlerUtilTest.class,
	LocaleUtilityTest.class,
	OpenmrsUtilTest.class,
	SecurityTest.class
})
public class AllUtilTests{}