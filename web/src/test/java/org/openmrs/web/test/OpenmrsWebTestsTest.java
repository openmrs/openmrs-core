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

import org.openmrs.OpenmrsTestsTest;

/**
 * This class will run all the tests found in {@link OpenmrsTestsTest} on the files in the web layer <br/>
 * <br/>
 * This is needed because when running tests through maven, maven will separate each maven module
 * into batches of tests, so because the {@link OpenmrsTestsTest} is in the api module, it will not
 * verify the tests in the web layer
 */
public class OpenmrsWebTestsTest extends OpenmrsTestsTest {

}
