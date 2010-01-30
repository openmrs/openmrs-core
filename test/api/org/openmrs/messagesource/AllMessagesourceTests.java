package org.openmrs.messagesource;

import org.openmrs.messagesource.impl.AllMessagesourceImplTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	AllMessagesourceImplTests.class,
	PresentationMessageMapTest.class
})
public class AllMessagesourceTests{}