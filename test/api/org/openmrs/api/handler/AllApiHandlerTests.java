package org.openmrs.api.handler;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	AuditableSaveHandlerTest.class,
	BaseRetireHandlerTest.class,
	BaseUnretireHandlerTest.class,
	BaseUnvoidHandlerTest.class,
	BaseVoidHandlerTest.class,
	ConceptNameSaveHandlerTest.class,
	PersonUnvoidHandlerTest.class,
	PersonVoidHandlerTest.class,
	RequiredReasonVoidSaveHandlerTest.class,
	RequireVoidReasonVoidHandlerTest.class
})
public class AllApiHandlerTests{}