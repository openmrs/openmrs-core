package org.openmrs.patient.impl;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	LuhnIdentifierValidatorTest.class,
	VerhoeffIdentifierValidatorTest.class
})
public class AllPatientImplTests{}