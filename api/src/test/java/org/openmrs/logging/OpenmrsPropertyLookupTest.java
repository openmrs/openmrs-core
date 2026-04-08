/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.OpenmrsUtil;

/**
 * Tests for {@link OpenmrsPropertyLookup}
 */
public class OpenmrsPropertyLookupTest extends BaseContextSensitiveTest {
	
	@Test
	public void lookup_shouldReturnApplicationDirectoryWhenAuthenticated() {
		OpenmrsPropertyLookup lookup = new OpenmrsPropertyLookup();
		
		String result = lookup.lookup(null, "applicationDirectory");
		
		assertNotNull(result);
		assertEquals(OpenmrsUtil.getApplicationDataDirectory(), result);
	}
	
	@Test
	public void lookup_shouldThrowExceptionForLogLayoutWhenNotAuthenticated() {
		Context.logout();
		OpenmrsPropertyLookup lookup = new OpenmrsPropertyLookup();
		
		// This will throw error, as we accessing logLocation on unaunthenticated context
		String result = lookup.lookup(null, "logLocation");
		
		assertNull(result);
	}
	

	
}
