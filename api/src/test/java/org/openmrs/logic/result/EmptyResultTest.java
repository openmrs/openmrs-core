/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.logic.result;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the {@link EmptyResult} class
 */
public class EmptyResultTest {
	
	@Test
	public void isEmpty_shouldReturnTrue() {
		Assert.assertTrue(new EmptyResult().isEmpty());
	}
	
	@Test
	public void isNull_shouldReturnTrue() {
		Assert.assertTrue(new EmptyResult().isNull());
	}
}
