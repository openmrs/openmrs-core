/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.layout.name;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NameSupportContextSensitiveTest extends BaseContextSensitiveTest {

	@Test
	public void getDefaultLayoutFormat_shouldReadFormatAlreadyInDatabaseOnInit() throws Exception {

		executeDataSet("org/openmrs/api/include/NameSupportTest-format.xml");

		NameSupport nameSupport = NameSupport.getInstance();

		Field initializedField = NameSupport.class.getDeclaredField("initialized");
		initializedField.setAccessible(true);
		Field layoutFormatField = NameSupport.class.getDeclaredField("layoutFormat");
		layoutFormatField.setAccessible(true);
		Field defaultFormatField = LayoutSupport.class.getDeclaredField("defaultLayoutFormat");
		defaultFormatField.setAccessible(true);

		boolean previousInitialized = initializedField.getBoolean(nameSupport);
		Object previousDefaultFormat = defaultFormatField.get(nameSupport);
		nameSupport.setDefaultLayoutFormat("short"); // mirrors legacyui's wiring

		try {
			// Simulate a fresh boot: clear the cache/init flag on the existing singleton
			initializedField.setBoolean(nameSupport, false);
			layoutFormatField.set(nameSupport, null);

			String format = NameSupport.getInstance().getDefaultLayoutFormat();

			assertEquals("long", format);
		} finally {
			initializedField.setBoolean(nameSupport, previousInitialized);
			layoutFormatField.set(nameSupport, null);
			defaultFormatField.set(nameSupport, previousDefaultFormat);
			Context.getAdministrationService().removeGlobalPropertyListener(nameSupport);
		}
	}
}
