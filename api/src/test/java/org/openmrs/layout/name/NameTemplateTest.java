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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.User;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UsernamePasswordCredentials;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NameTemplateTest extends BaseContextSensitiveTest {

	private final String NAME_TEMPLATE_GP_DATASET_PATH = "src/test/resources/org/openmrs/include/nameSupportTestDataSet.xml";

	private NameSupport nameSupport;

	@BeforeEach
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

		assertEquals("Mark Goodrich", nameTemplate.format(personName));

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

		assertEquals("Goodrich, Mark \"Blue State\"", nameTemplate.format(personName));

	}

	@Test
	public void shouldUseNameTemplateConfiguredViaGlobalProperties() {
		// setup
		executeDataSet(NAME_TEMPLATE_GP_DATASET_PATH);
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LAYOUT_NAME_FORMAT, "customXmlTemplate"));

		PersonName personName = new PersonName();
		personName.setGivenName("Moses");
		personName.setMiddleName("Tusha");
		personName.setFamilyName("Mujuzi");

		//Simulate GP Changed Listener event
		Context.getAdministrationService().saveGlobalProperty(Context.getAdministrationService()
		        .getGlobalPropertyObject(OpenmrsConstants.GLOBAL_PROPERTY_LAYOUT_NAME_TEMPLATE));

		// replay
		NameTemplate nameTemplate = NameSupport.getInstance().getDefaultLayoutTemplate();

		// verify
		assertEquals("Moses Mujuzi", nameTemplate.format(personName));
	}

	@Test
	public void shouldUseNameTemplateConfiguredViaGlobalPropertiesForAUserWithoutGetGlobalProperties() {
		executeDataSet(NAME_TEMPLATE_GP_DATASET_PATH);
		AdministrationService administrationService = Context.getAdministrationService();
		administrationService.saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LAYOUT_NAME_FORMAT, "customXmlTemplate"));
		// saved through the service, so this transaction reads it back rather than the global property cache
		administrationService.saveGlobalProperty(
		    administrationService.getGlobalPropertyObject(OpenmrsConstants.GLOBAL_PROPERTY_LAYOUT_NAME_TEMPLATE));

		PersonName personName = new PersonName();
		personName.setGivenName("Moses");
		personName.setMiddleName("Tusha");
		personName.setFamilyName("Mujuzi");

		authenticateAsUserWithoutPrivileges();
		// NameSupport reads the template when it initializes, which then happens as this user too
		administrationService.removeGlobalPropertyListener(nameSupport);
		ReflectionTestUtils.setField(nameSupport, "initialized", false);

		assertEquals("Moses Mujuzi", personName.getFullName());
	}

	private void authenticateAsUserWithoutPrivileges() {
		Person person = new Person();
		person.setGender("F");
		person.addName(new PersonName("Name", null, "Reader"));
		User user = new User(person);
		user.setUsername("name-reader");
		Context.getUserService().createUser(user, "NameReader123");

		Context.logout();
		Context.authenticate(new UsernamePasswordCredentials("name-reader", "NameReader123"));
	}

	@Test
	public void shouldResolveAllStandardTokensDirectly() {

		NameTemplate nameTemplate = new NameTemplate();

		List<String> lineByLineFormat = new ArrayList<>();
		lineByLineFormat.add("prefix");
		lineByLineFormat.add("familyNamePrefix");
		lineByLineFormat.add("familyName2");
		lineByLineFormat.add("givenName");
		lineByLineFormat.add("middleName");
		lineByLineFormat.add("familyName");
		lineByLineFormat.add("familyNameSuffix");
		lineByLineFormat.add("degree");
		nameTemplate.setLineByLineFormat(lineByLineFormat);

		Map<String, String> nameMappings = new HashMap<>();
		nameMappings.put("prefix", "prefix");
		nameMappings.put("familyNamePrefix", "familyNamePrefix");
		nameMappings.put("familyName2", "familyName2");
		nameMappings.put("givenName", "givenName");
		nameMappings.put("middleName", "middleName");
		nameMappings.put("familyName", "familyName");
		nameMappings.put("familyNameSuffix", "familyNameSuffix");
		nameMappings.put("degree", "degree");
		nameTemplate.setNameMappings(nameMappings);

		Map<String, String> sizeMappings = new HashMap<>();
		for (String token : nameMappings.keySet()) {
			sizeMappings.put(token, "30");
		}
		nameTemplate.setSizeMappings(sizeMappings);

		nameSupport.setLayoutTemplates(Collections.singletonList(nameTemplate));

		PersonName personName = new PersonName();
		personName.setPrefix("Mr.");
		personName.setFamilyNamePrefix("Ganege");
		personName.setFamilyName2("Ralalage");
		personName.setGivenName("Ranidu");
		personName.setMiddleName("Nethma");
		personName.setFamilyName("Rathnayaka");
		personName.setFamilyNameSuffix("(Contributor)");
		personName.setDegree("BSc");

		String formattedName = nameTemplate.format(personName);

		assertEquals("Mr. Ganege Ralalage Ranidu Nethma Rathnayaka (Contributor) BSc", formattedName);
	}

	@Test
	public void shouldResolveCustomTokenViaFallback() {

		List<String> customTokens = new ArrayList<>(nameSupport.getSpecialTokens());
		customTokens.add("uuid");
		nameSupport.setSpecialTokens(customTokens);

		NameTemplate nameTemplate = new NameTemplate();

		nameTemplate.setLineByLineFormat(Collections.singletonList("uuid"));

		Map<String, String> nameMappings = new HashMap<>();
		nameMappings.put("uuid", "uuid");
		nameTemplate.setNameMappings(nameMappings);

		Map<String, String> sizeMappings = new HashMap<>();
		sizeMappings.put("uuid", "30");
		nameTemplate.setSizeMappings(sizeMappings);

		nameSupport.setLayoutTemplates(Collections.singletonList(nameTemplate));

		PersonName personName = new PersonName();
		personName.setUuid("Ranidu Rathnayaka");

		assertEquals("Ranidu Rathnayaka", nameTemplate.format(personName));
	}

}
