/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.web.test;

import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.test.context.ContextConfiguration;

/**
 * Modules that test controllers, etc, or which contain references to Beans in openmrs-servlet.xml
 * should use this class instead of the general {@link BaseWebContextSensitiveTest} one. 
 * Developers just need to make sure their modules are on the classpath. 
 * The TestingApplicationContext.xml file tells spring/hibernate to look for and load all
 * modules found on the classpath. The ContextConfiguration annotation adds in the module
 * application context files to the config locations and the test application context (so that the
 * module services are loaded from the system classloader)
 */
@ContextConfiguration(locations = { "classpath:openmrs-servlet.xml", "classpath*:webModuleApplicationContext.xml" }, 
					  inheritLocations = true)
public abstract class BaseModuleWebContextSensitiveTest extends BaseModuleContextSensitiveTest {

}
