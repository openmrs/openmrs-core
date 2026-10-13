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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.util.OpenmrsConstants;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NameSupportTest {

	private NameSupport nameSupport;

	@BeforeEach
	public void setUp() {
		nameSupport = new NameSupport();
		nameSupport.setDefaultLayoutFormat("short");
	}

	@Test
	public void supportsPropertyName_shouldReturnTrueForTemplateAndFormatProperties() {
		assertTrue(nameSupport.supportsPropertyName(OpenmrsConstants.GLOBAL_PROPERTY_LAYOUT_NAME_TEMPLATE));
		assertTrue(nameSupport.supportsPropertyName(OpenmrsConstants.GLOBAL_PROPERTY_LAYOUT_NAME_FORMAT));
		assertFalse(nameSupport.supportsPropertyName("some.other.global.property"));
	}

	@Test
	public void globalPropertyChanged_shouldUpdateLayoutFormatWhenFormatPropertyChanges() {
		GlobalProperty gp = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LAYOUT_NAME_FORMAT, "long");
		nameSupport.globalPropertyChanged(gp);

		assertEquals("long", nameSupport.getDefaultLayoutFormat());
	}

	@Test
	public void globalPropertyDeleted_shouldRestoreDefaultLayoutFormatWhenFormatPropertyIsDeleted() {
		// First set a custom format via GP listener
		GlobalProperty gp = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LAYOUT_NAME_FORMAT, "long");
		nameSupport.globalPropertyChanged(gp);
		assertEquals("long", nameSupport.getDefaultLayoutFormat());

		// Now trigger property deletion
		nameSupport.globalPropertyDeleted(OpenmrsConstants.GLOBAL_PROPERTY_LAYOUT_NAME_FORMAT);
		assertEquals("short", nameSupport.getDefaultLayoutFormat());
	}
}
