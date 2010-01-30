package org.openmrs.hl7;

import org.openmrs.hl7.handler.AllHl7HandlerTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	AllHl7HandlerTests.class,
	HL7ServiceTest.class,
	HL7UtilTest.class
})
public class AllHl7Tests{}