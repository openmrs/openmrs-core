package org.openmrs.validator;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	ConceptClassValidatorTest.class,
	ConceptDatatypeValidatorTest.class,
	DrugOrderValidatorTest.class,
	EncounterTypeValidatorTest.class,
	FieldTypeValidatorTest.class,
	FormValidatorTest.class,
	LocationValidatorTest.class,
	ObsValidatorTest.class,
	OrderTypeValidatorTest.class,
	OrderValidatorTest.class,
	PatientIdentifierTypeValidatorTest.class,
	PatientIdentifierValidatorTest.class,
	PersonAttributeTypeValidatorTest.class,
	PrivilegeValidatorTest.class,
	ProgramValidatorTest.class,
	RequireNameValidatorTest.class,
	RoleValidatorTest.class,
	SchedulerFormValidatorTest.class,
	StateConversionValidatorTest.class,
	UserValidatorTest.class,
	ValidateUtilTest.class
})
public class AllValidatorTests{}