package org.openmrs;

import org.openmrs.aop.AllAopTests;
import org.openmrs.api.AllApiTests;
import org.openmrs.arden.AllArdenTests;
import org.openmrs.cohort.AllCohortTests;
import org.openmrs.hl7.AllHl7Tests;
import org.openmrs.logic.AllLogicTests;
import org.openmrs.messagesource.AllMessagesourceTests;
import org.openmrs.module.AllModuleTests;
import org.openmrs.notification.AllNotificationTests;
import org.openmrs.patient.AllPatientTests;
import org.openmrs.propertyeditor.AllPropertyeditorTests;
import org.openmrs.report.AllReportTests;
import org.openmrs.reporting.AllReportingTests;
import org.openmrs.scheduler.AllSchedulerTests;
import org.openmrs.util.AllUtilTests;
import org.openmrs.validator.AllValidatorTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	AllAopTests.class,
	AllApiTests.class,
	AllArdenTests.class,
	AllCohortTests.class,
	ConceptAnswerTest.class,
	ConceptDescriptionTest.class,
	ConceptNameTest.class,
	ConceptTest.class,
	ConceptWordTest.class,
	EncounterTest.class,
	EncounterTypeTest.class,
	FormTest.class,
	AllHl7Tests.class,
	LocationTest.class,
	AllLogicTests.class,
	AllMessagesourceTests.class,
	AllModuleTests.class,
	AllNotificationTests.class,
	ObsTest.class,
	OpenmrsTestAnnotationsTest.class,
	OpenmrsTestsTest.class,
	OrderTest.class,
	AllPatientTests.class,
	PatientIdentifierTest.class,
	PatientTest.class,
	PersonAttributeTest.class,
	PersonNameTest.class,
	PersonTest.class,
	ProgramWorkflowTest.class,
	AllPropertyeditorTests.class,
	AllReportTests.class,
	AllReportingTests.class,
	RoleTest.class,
	AllSchedulerTests.class,
	AllUtilTests.class,
	AllValidatorTests.class
})
public class AllTests{}