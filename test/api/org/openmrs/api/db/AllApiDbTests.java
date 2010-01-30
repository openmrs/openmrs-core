package org.openmrs.api.db;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	ContextDAOTest.class,
	EncounterDAOTest.class,
	SerializedObjectDAOTest.class
})
public class AllApiDbTests{}