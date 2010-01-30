package org.openmrs.module;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	ModuleInteroperabilityTest.class,
	ModuleUtilTest.class
})
public class AllModuleTests{}