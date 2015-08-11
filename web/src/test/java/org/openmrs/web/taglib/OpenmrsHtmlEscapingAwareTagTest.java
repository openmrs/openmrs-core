/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.taglib;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.Verifies;
import org.openmrs.web.test.BaseWebContextSensitiveTest;

public class OpenmrsHtmlEscapingAwareTagTest extends BaseWebContextSensitiveTest {
	private static class OpenmrsHtmlEscapingAwareTagUnderTest extends OpenmrsHtmlEscapingAwareTag {

		private static final long serialVersionUID = 1L;

		@Override
		protected int doEndTagInternal() throws Exception {
			// Not called in this test, so return value doesn't matter
			return 0;
		}
		
		@Override
		public boolean isHtmlEscape()
		{
			return super.isHtmlEscape();
		}
		
	};
	
	@Test
	@Verifies(value = "set the escape to true", method = "setHtmlEscape()")
	public void setHtmlEscape_true() throws Exception
	{		
		setHtmlEscape("true", true);
	}
	
	@Test
	@Verifies(value = "set the escape to false", method = "setHtmlEscape()")
	public void setHtmlEscape_false() throws Exception
	{
		setHtmlEscape("false", false);
	}

	private void setHtmlEscape(String setValue, boolean expectedValue)
			throws Exception {
		final OpenmrsHtmlEscapingAwareTag objectUnderTest = new OpenmrsHtmlEscapingAwareTagUnderTest();
		
		objectUnderTest.setHtmlEscape(setValue);
		
		Assert.assertEquals(expectedValue, objectUnderTest.isHtmlEscape());
	}
}
