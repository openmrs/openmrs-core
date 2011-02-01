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

import org.openmrs.test.BaseContextSensitiveTest;
import org.springframework.test.context.ContextConfiguration;

/**
 * Web tests for controllers, etc should use this class instead of the general
 * {@link BaseWebContextSensitiveTest} one. The {@link ContextConfiguration} annotation adds in the
 * openmrs-servlet.xml context file to the config locations so that controller tests can pick up the
 * right type of controller, etc.
 */
// put refernce to openmrs-servlet in the parent class to make this class really do nothing
//@ContextConfiguration(locations = { "classpath:openmrs-servlet.xml" }, inheritLocations = true)
public abstract class BaseWebContextSensitiveTest extends BaseContextSensitiveTest {

}
