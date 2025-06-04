/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.test.jupiter;

import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Web tests for controllers, etc. should use this class instead of the general
 * {@link BaseContextSensitiveTest} one. 
 * <p>
 * The {@link ContextConfiguration} annotation adds the
 * openmrs-servlet.xml context file so that controller tests can pick up the
 * right type of controller, etc.
 */
@WebAppConfiguration	
@ContextConfiguration(locations = { "classpath*:openmrs-servlet.xml", "classpath*:AltAuthSchemeTestingApplicationContext.xml" })
public abstract class BaseWebContextSensitiveTest extends BaseContextSensitiveTest {
}
