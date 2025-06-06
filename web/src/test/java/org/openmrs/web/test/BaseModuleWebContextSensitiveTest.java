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

import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Modules that test controllers, etc, or which contain references to Beans in openmrs-servlet.xml
 * should use this class instead of the general {@link BaseWebContextSensitiveTest} one. 
 * Developers just need to make sure their modules are on the classpath. 
 * The TestingApplicationContext.xml file tells spring/hibernate to look for and load all
 * modules found on the classpath. The ContextConfiguration annotation adds in the module
 * application context files to the config locations and the test application context (so that the
 * module services are loaded from the system classloader)
 * @deprecated as of 2.4
 * <p>openmrs-core migrated its tests from JUnit 4 to JUnit 5.
 * JUnit 4 helpers are still supported so module developers can gradually migrate tests from JUnit 4 to JUnit 5.
 * To migrate your tests follow <a href="https://wiki.openmrs.org/display/docs/How+to+migrate+to+JUnit+5">How to migrate to JUnit 5</a>.
 * The JUnit 5 version of the class is {@link org.openmrs.web.test.jupiter.BaseModuleWebContextSensitiveTest}.<p>
 */
@WebAppConfiguration
@ContextConfiguration(locations = { "classpath*:openmrs-servlet.xml", "classpath*:webModuleApplicationContext.xml" }, inheritLocations = true)
@Deprecated
public abstract class BaseModuleWebContextSensitiveTest extends BaseModuleContextSensitiveTest {
}
