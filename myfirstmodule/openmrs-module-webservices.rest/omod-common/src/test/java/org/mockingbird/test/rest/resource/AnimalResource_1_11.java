/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.mockingbird.test.rest.resource;

import org.mockingbird.test.Animal;
import org.openmrs.module.webservices.rest.web.RestConstants;

/**
 * Fake {@code Resource} used in tests at
 * {@link org.openmrs.module.webservices.rest.web.api.impl.RestServiceImplTest}. Located in a fake
 * package not under org.openmrs.xxx on purpose otherwise it will be picked up by other tests due to
 * {@link org.openmrs.module.webservices.rest.web.OpenmrsClassScanner} and its classpath pattern.
 */
@org.openmrs.module.webservices.rest.web.annotation.Resource(name = RestConstants.VERSION_1 + "/animal", order = 1, supportedClass = Animal.class, supportedOpenmrsVersions = { "1.11.*" })
public class AnimalResource_1_11 extends AnimalResource_1_9 {
	
	@Override
	public String getResourceVersion() {
		return "1.11";
	}
}
