package org.openmrs.api.context;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	ContextTest.class,
	ContextWithModuleTest.class
})
public class AllApiContextTests{}