/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.test;

import org.openmrs.test.BaseContextSensitiveTest;
import org.springframework.test.context.ContextConfiguration;

/**
 * Web tests for controllers, etc should use this class instead of the general
 * {@link BaseWebContextSensitiveTest} one. The {@link ContextConfiguration} annotation adds in the
 * openmrs-servlet.xml context file to the config locations so that controller tests can pick up the
 * right type of controller, etc.
 */
// put reference to openmrs-servlet in the parent class to make this class really do nothing
//@ContextConfiguration(locations = { "classpath:openmrs-servlet.xml" }, inheritLocations = true)
public abstract class BaseWebContextSensitiveTest extends BaseContextSensitiveTest {

}
