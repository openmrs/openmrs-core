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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.openmrs.PersonName;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

public class NameSupportTest extends BaseContextSensitiveTest {

	protected static final String NAME_SUPPORT_DATASET_PATH = "src/test/resources/org/openmrs/include/nameSupportTestDataSet.xml";

	@Test
	public void shouldOverrideTheExistingDefaultNameTemplate() {
		
		executeDataSet(NAME_SUPPORT_DATASET_PATH);
		NameSupport nameSupport = NameSupport.getInstance();
		NameTemplate nameTemplate = new NameTemplate();

		List<String> lineByLineFormat = new ArrayList<>();
		lineByLineFormat.add("givenName");
		lineByLineFormat.add("familyName");
		nameTemplate.setLineByLineFormat(lineByLineFormat);

		Map<String, String> nameMappings = new HashMap<>();
		nameMappings.put("givenName", "givenName");
		nameMappings.put("familyName", "familyName");
		nameTemplate.setNameMappings(nameMappings);

		Map<String, String> sizeMappings = new HashMap<>();
		sizeMappings.put("givenName", "30");
		sizeMappings.put("familyName", "30");
		nameTemplate.setSizeMappings(sizeMappings);

		nameSupport.setLayoutTemplates(Collections.singletonList(nameTemplate));

		PersonName personName = new PersonName();
		personName.setGivenName("Moses");
		personName.setFamilyName("Mujuzi");

		assertEquals("Moses Mujuzi", nameTemplate.format(personName));
	}
}
