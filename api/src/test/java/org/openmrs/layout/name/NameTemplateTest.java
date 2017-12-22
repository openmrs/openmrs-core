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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.PersonName;
import org.openmrs.test.BaseContextSensitiveTest;

public class NameTemplateTest extends BaseContextSensitiveTest {
	
	private NameSupport nameSupport;
	
	@Before
	public void setup() {
		nameSupport = NameSupport.getInstance();
		nameSupport.setSpecialTokens(Arrays.asList("prefix", "givenName", "middleName", "familyNamePrefix",
		    "familyNameSuffix", "familyName2", "familyName", "degree"));
	}
	
	@Test
	public void shouldProperlyFormatName() {
		
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
		personName.setGivenName("Mark");
		personName.setFamilyName("Goodrich");
		
		Assert.assertEquals("Mark Goodrich", nameTemplate.format(personName));
		
	}
	
	@Test
	public void shouldProperlyFormatNameWithNonTokens() {
		
		NameTemplate nameTemplate = new NameTemplate();
		
		List<String> lineByLineFormat = new ArrayList<>();
		lineByLineFormat.add("familyName,");
		lineByLineFormat.add("givenName");
		lineByLineFormat.add("\"middleName\"");
		nameTemplate.setLineByLineFormat(lineByLineFormat);
		
		Map<String, String> nameMappings = new HashMap<>();
		nameMappings.put("familyName", "familyName");
		nameMappings.put("givenName", "givenName");
		nameMappings.put("middleName", "middleName");
		nameTemplate.setNameMappings(nameMappings);
		
		Map<String, String> sizeMappings = new HashMap<>();
		sizeMappings.put("familyName", "30");
		sizeMappings.put("givenName", "30");
		sizeMappings.put("middleName", "30");
		nameTemplate.setSizeMappings(sizeMappings);
		
		nameSupport.setLayoutTemplates(Collections.singletonList(nameTemplate));
		
		PersonName personName = new PersonName();
		personName.setGivenName("Mark");
		personName.setFamilyName("Goodrich");
		personName.setMiddleName("Blue State");
		
		Assert.assertEquals("Goodrich, Mark \"Blue State\"", nameTemplate.format(personName));
		
	}
	
}
