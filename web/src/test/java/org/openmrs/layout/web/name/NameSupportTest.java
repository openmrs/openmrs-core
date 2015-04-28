/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.layout.web.name;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.Verifies;
import org.openmrs.web.test.BaseWebContextSensitiveTest;

public class NameSupportTest extends BaseWebContextSensitiveTest {
	
	/**
	 * Changes made in TRUNK-3849 to move NameSupport from package layout.web.name to layout.name
	 * broke layout.web.name.NameSupport.getInstance(), because there was no longer a bean of the
	 * old type being defined. This breaks several other modules that built against older versions
	 * of core. It is not practical to update all of those modules to have version-specific
	 * dependencies, so it's necessary to ensure that the old 'getInstance()' continues to work.
	 */
	@Test
	@Verifies(value = "getInstance() should return a layout.web.name.NameSupport instance", method = "getInstance()")
	public void getInstance_shouldFindNameSupportBean() throws Exception {
		NameSupport nameSupport = NameSupport.getInstance();
		Assert.assertNotNull(nameSupport);
		Assert.assertNotNull(nameSupport.getInstance().getDefaultLayoutFormat());
		//make sure that all 5 layout templates defined at the time of the package change continue to work
		Assert.assertTrue(NameSupport.getInstance().getLayoutTemplates().size() >= 5);
	}
	
}
