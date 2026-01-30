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
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Web tests for controllers, etc. should use this class instead of the general
 * {@link org.openmrs.test.BaseContextSensitiveTest} one. 
 * <p>
 * The {@link ContextConfiguration} annotation adds the
 * openmrs-servlet.xml context file so that controller tests can pick up the
 * right type of controller, etc.
 *
 * 
 * @deprecated as of 2.4
 *   <p>openmrs-core migrated its tests from JUnit 4 to JUnit 5.
 *   JUnit 4 helpers are still supported so module developers can gradually migrate tests from JUnit 4 to JUnit 5. 
 *   To migrate your tests follow <a href="https://wiki.openmrs.org/display/docs/How+to+migrate+to+JUnit+5">How to migrate to JUnit 5</a>.
 *   The JUnit 5 version of the class is {@link org.openmrs.web.test.jupiter.BaseWebContextSensitiveTest}.<p>
 */
@WebAppConfiguration	
@ContextConfiguration(locations = { "classpath*:openmrs-servlet.xml", "classpath*:AltAuthSchemeTestingApplicationContext.xml" })
@Deprecated
public abstract class BaseWebContextSensitiveTest extends BaseContextSensitiveTest {
}
