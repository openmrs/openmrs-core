package org.openmrs.util;

import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;

/**
 * Tests methods on the {@link DatabaseUpdater} class. This class expects /metadata/model to be on
 * the classpath so that the liquibase-update-to-latest.xml can be found.
 */
public class DatabaseUpdaterTest extends BaseContextSensitiveTest {
	
	/**
	 * @verifies {@link DatabaseUpdater#updatesRequired()} test = should always have a valid update to latest
	 *           file
	 */
	@Test
	public void updatesRequired_shouldAlwaysHaveAValidUpdateToLatestFile() throws Exception {
		// expects /metadata/model to be on the classpath so that
		// the liquibase-update-to-latest.xml can be found.
		DatabaseUpdater.updatesRequired();
		
		// does not run DatabaseUpdater.update() because hsqldb doesn't like single quotes in strings
	}
}
