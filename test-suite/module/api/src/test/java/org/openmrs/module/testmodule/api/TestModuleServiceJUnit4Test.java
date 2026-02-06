package org.openmrs.module.testmodule.api; /**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Testing with JUnit 4
 */
public class TestModuleServiceJUnit4Test extends BaseModuleContextSensitiveTest {
	
	@Autowired
	private TestModuleService testModuleService;
	
	@Test
	public void testHello() {
		assertThat(testModuleService.hello(), is("hello"));
	}

}
