package org.openmrs.util;

import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;

/**
 * Tests methods on the {@link DatabaseUpdater} class. This class expects /metadata/model to be on
 * the classpath so that the liquibase-update-to-latest.xml can be found.
 */
public class DatabaseUpdaterTest extends BaseContextSensitiveTest {
	
	/**
	 * @see {@link DatabaseUpdater#updatesRequired()}
	 */
	@Test
	@Verifies(value = "should always have a valid update to latest file", method = "updatesRequired()")
	public void updatesRequired_shouldAlwaysHaveAValidUpdateToLatestFile() throws Exception {
		// expects /metadata/model to be on the classpath so that
		// the liquibase-update-to-latest.xml can be found.
		DatabaseUpdater.updatesRequired();
		
		// does not run DatabaseUpdater.update() because hsqldb doesn't like single quotes in strings
	}
}
