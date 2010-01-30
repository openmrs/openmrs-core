package org.openmrs.aop;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	RequiredDataAdviceTest.class
})
public class AllAopTests{}