/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Tests UpdateFileParser
 */
public class UpdateFileParserTest {

	
	/**
	 * @see UpdateFileParser#parse()
	 */
	@Test
	public void parse_shouldsetPropertiesFromXmlFile() {

		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<update configVersion=\"1.0\">"
			+ "<moduleId>formEntry</moduleId>"
			+ "<currentVersion>1.0</currentVersion>" 
			+ "<downloadURL>https://modules.openmrs.org/modules/formentry/formentry-1.0.omod</downloadURL>"
			+ "</update>";
		
		UpdateFileParser parser = new UpdateFileParser(xml);
		parser.parse();

		assertEquals("formEntry", parser.getModuleId());
		assertEquals("1.0", parser.getCurrentVersion());
		assertEquals("https://modules.openmrs.org/modules/formentry/formentry-1.0.omod", parser.getDownloadURL());
	}

	/**
	 * @see UpdateFileParser#parse()
	 */
	@Test
	public void parse_shouldSetPropertiesUsingNewestUpdate() {

                String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                        + "<updates configVersion=\"1.1\" moduleId=\"formEntry\">"
			+ "<update>"
			+ "<currentVersion>1.2</currentVersion>"
			+ "<requireOpenMRSVersion/>" 
			+ "<downloadURL>https://modules.openmrs.org/modulus/api/releases/169/download/formentry-1.2.omod</downloadURL>"
			+ "</update>"
			+ "<update>" 
			+ "<currentVersion>1.1</currentVersion>"
			+ "<requireOpenMRSVersion/>"
			+ "<downloadURL>https://modules.openmrs.org/modulus/api/releases/168/download/formentry-1.1.omod</downloadURL>"
			+ "</update>"
			+ "<update>"
                        + "<moduleId>formEntry</moduleId>"
                        + "<currentVersion>1.0</currentVersion>"
			+ "<requireOpenMRSVersion/>"
                        + "<downloadURL>https://modules.openmrs.org/modulus/api/releases/167/download/formentry-1.0.omod</downloadURL>"
                        + "</update>"
                        + "</updates>";

                UpdateFileParser parser = new UpdateFileParser(xml);
                parser.parse();

                assertEquals("formEntry", parser.getModuleId());
                assertEquals("1.2", parser.getCurrentVersion());
		assertEquals("https://modules.openmrs.org/modulus/api/releases/169/download/formentry-1.2.omod", parser.getDownloadURL());
	}

	/**
	 * @see UpdateFileParser#parse()
	 */
	@Test
	public void parse_shouldNotSetPropertiesUsingUpdatesAheadOfCurrentOpenmrsVersion() {
		
                String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                        + "<updates configVersion=\"1.1\" moduleId=\"formEntry\">"
                        + "<update>"
                        + "<currentVersion>1.2</currentVersion>"
                        + "<requireOpenMRSVersion>999.9.9</requireOpenMRSVersion>"
                        + "<downloadURL>https://modules.openmrs.org/modulus/api/releases/169/download/formentry-1.2.omod</downloadURL>"
                        + "</update>"
                        + "<update>"
                        + "<currentVersion>1.1</currentVersion>"
                        + "<requireOpenMRSVersion/>"
                        + "<downloadURL>https://modules.openmrs.org/modulus/api/releases/168/download/formentry-1.1.omod</downloadURL>"
                        + "</update>"
                        + "<update>"
                        + "<moduleId>formEntry</moduleId>"
                        + "<currentVersion>1.0</currentVersion>"
                        + "<requireOpenMRSVersion/>"
                        + "<downloadURL>https://modules.openmrs.org/modulus/api/releases/167/download/formentry-1.0.omod</downloadURL>"
                        + "</update>"
                        + "</updates>";

                UpdateFileParser parser = new UpdateFileParser(xml);
                parser.parse();

                assertEquals("formEntry", parser.getModuleId());
                assertEquals("1.1", parser.getCurrentVersion());
                assertEquals("https://modules.openmrs.org/modulus/api/releases/168/download/formentry-1.1.omod", parser.getDownloadURL());
	}
}
