package org.openmrs.api;

import org.openmrs.api.context.AllApiContextTests;
import org.openmrs.api.db.AllApiDbTests;
import org.openmrs.api.handler.AllApiHandlerTests;
import org.openmrs.api.impl.AllApiImplTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	AdministrationServiceTest.class,
	CohortServiceTest.class,
	ConceptServiceTest.class,
	AllApiContextTests.class,
	AllApiDbTests.class,
	EncounterServiceTest.class,
	FormServiceTest.class,
	AllApiHandlerTests.class,
	AllApiImplTests.class,
	LocationServiceTest.class,
	ObsServiceTest.class,
	OrderServiceTest.class,
	PatientServiceTest.class,
	PatientSetServiceTest.class,
	PersonServiceTest.class,
	ProgramWorkflowServiceTest.class,
	UserServiceTest.class
})
public class AllApiTests{}