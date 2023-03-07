/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.User;
import org.openmrs.api.ConceptService;
import org.openmrs.api.UserService;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ConversionUtil1_12Test extends BaseModuleWebContextSensitiveTest {
	
	@Autowired
	ConceptService conceptService;
	
	@Autowired
	UserService userService;
	
	@Test
	public void convertToRepresentation_shouldConvertValuesOfMaps() throws Exception {
		Map<Object, Object> toConvert = new LinkedHashMap<Object, Object>();
		Date date = new Date();
		toConvert.put("date", date);
		
		Map<String, Object> toConvert2ndLevel = new HashMap<String, Object>();
		Concept concept = conceptService.getConcept(3);
		toConvert2ndLevel.put("concept", concept);
		toConvert.put("map", toConvert2ndLevel);
		toConvert.put("string", "a string");
		
		User user = userService.getUser(1);
		toConvert.put(1, user);
		
		SimpleObject converted = (SimpleObject) ConversionUtil.convertToRepresentation(toConvert, Representation.REF);
		
		assertThat(converted,
		    (Matcher) hasEntry(is("date"), is(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(date))));
		assertThat(converted, (Matcher) hasEntry(is("string"), is("a string")));
		assertThat(converted, (Matcher) hasEntry(is("1"), is(hasEntry(is("uuid"), is(user.getUuid())))));
		assertThat(converted,
		    (Matcher) hasEntry(is("map"), is(hasEntry(is("concept"), is(hasEntry(is("uuid"), is(concept.getUuid())))))));
	}
}
