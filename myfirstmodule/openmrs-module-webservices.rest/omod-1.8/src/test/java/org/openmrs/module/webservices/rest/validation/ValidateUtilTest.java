/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.validation;

import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.module.webservices.validation.ValidateUtil;
import org.openmrs.test.Verifies;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;

public class ValidateUtilTest extends BaseModuleWebContextSensitiveTest {
	
	// we are not supporting this validation against 1.8, so when running against 1.8 no exception should be thrown
	
	/**
	 * @see {@link org.openmrs.validator.ValidateUtil#validate(Object)}
	 */
	@Test
	@Verifies(value = "should not throw exception", method = "validate(Object)")
	public void validate_shouldNotThrowValidationExceptionButShouldNotFail() throws Exception {
		
		// we are not supporting this validation against 1.8, so when running against 1.8 when passing in an
		// invalid object, no exception should be thrown
		
		Location loc = new Location();
		loc.setName("name");
		ValidateUtil.validate(loc);
	}
	
}
