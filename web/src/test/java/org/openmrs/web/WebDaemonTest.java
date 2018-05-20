package org.openmrs.web;

import org.junit.Test;
/**
 * Created by freddy on 20.05.18.
 */
public class WebDaemonTest {
	
	@Test(expected = Exception.class)
	public void startOpenmrs_shouldThrowExceptionGivenNull() throws Exception {
		WebDaemon.startOpenmrs(null);
	}

}
