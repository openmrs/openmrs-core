/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.hl7;

import org.junit.jupiter.api.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.customdatatype.datatype.BooleanDatatype;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.OpenmrsConstants;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests that the global properties owned by hl7 support are declared by {@link HL7Constants} and
 * registered at startup.
 */
public class HL7GlobalPropertiesTest extends BaseContextSensitiveTest {

	/**
	 * @see HL7Constants#HL7_GLOBAL_PROPERTIES()
	 */
	@Test
	public void hl7GlobalProperties_shouldNotBeDeclaredAmongTheCoreGlobalProperties() {
		for (GlobalProperty coreProp : OpenmrsConstants.CORE_GLOBAL_PROPERTIES()) {
			for (GlobalProperty hl7Prop : HL7Constants.HL7_GLOBAL_PROPERTIES()) {
				assertFalse(coreProp.getProperty().equalsIgnoreCase(hl7Prop.getProperty()),
				    hl7Prop.getProperty() + " should be declared by hl7, not core");
			}
		}
	}

	/**
	 * @see Context#checkCoreDataset()
	 */
	@Test
	public void checkCoreDataset_shouldRegisterTheHl7GlobalPropertiesWithTheirDefaults() {
		AdministrationService as = Context.getAdministrationService();
		assertNull(as.getGlobalPropertyObject(HL7Constants.GLOBAL_PROPERTY_HL7_ARCHIVE_DIRECTORY));
		assertNull(as.getGlobalPropertyObject(HL7Constants.GLOBAL_PROPERTY_IGNORE_MISSING_NONLOCAL_PATIENTS));

		Context.checkCoreDataset();

		GlobalProperty archiveDir = as.getGlobalPropertyObject(HL7Constants.GLOBAL_PROPERTY_HL7_ARCHIVE_DIRECTORY);
		assertNotNull(archiveDir);
		assertEquals(HL7Constants.HL7_ARCHIVE_DIRECTORY_NAME, archiveDir.getPropertyValue());

		GlobalProperty ignoreMissing = as
		        .getGlobalPropertyObject(HL7Constants.GLOBAL_PROPERTY_IGNORE_MISSING_NONLOCAL_PATIENTS);
		assertNotNull(ignoreMissing);
		assertEquals("false", ignoreMissing.getPropertyValue());
		assertEquals(BooleanDatatype.class.getName(), ignoreMissing.getDatatypeClassname());
	}

	/**
	 * @see Context#checkCoreDataset()
	 */
	@Test
	public void checkCoreDataset_shouldNotOverwriteAnExistingHl7GlobalPropertyValue() {
		AdministrationService as = Context.getAdministrationService();
		as.saveGlobalProperty(new GlobalProperty(HL7Constants.GLOBAL_PROPERTY_HL7_ARCHIVE_DIRECTORY, "/custom/archives"));

		Context.checkCoreDataset();

		assertEquals("/custom/archives", as.getGlobalProperty(HL7Constants.GLOBAL_PROPERTY_HL7_ARCHIVE_DIRECTORY));
	}
}
