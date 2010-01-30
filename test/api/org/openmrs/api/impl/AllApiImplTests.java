package org.openmrs.api.impl;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	ConceptServiceImplTest.class,
	SerializationServiceImplTest.class
})
public class AllApiImplTests{}