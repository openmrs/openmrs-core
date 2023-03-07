/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Attributable;
import org.openmrs.Location;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.LocationService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;

public class PersonAttributeResource1_8Test extends BaseModuleWebContextSensitiveTest {
	
	public static final String PERSON_ATTRIBUTE_JSON = "{" + "    \"value\": \"Bangalore\"," + "    \"attributeType\": {"
	        + "        \"uuid\": \"54fc8400-1683-4d71-a1ac-98d40836ff7c\"" + "    }" + "}";
	
	private SimpleObject personAttributeSimpleObject = new SimpleObject();
	
	private PersonAttributeResource1_8 resource;
	
	@Autowired
	private PersonService personService;
	
	@Autowired
	private LocationService locationService;
	
	@Before
	public void beforeEachTests() throws Exception {
		personAttributeSimpleObject.putAll(new ObjectMapper().readValue(PERSON_ATTRIBUTE_JSON, HashMap.class));
		resource = (PersonAttributeResource1_8) Context.getService(RestService.class).getResourceBySupportedClass(
		    PersonAttribute.class);
	}
	
	@Test
	public void shouldCreatePersonAttribute() throws Exception {
		SimpleObject created = (SimpleObject) resource.create("da7f524f-27ce-4bb2-86d6-6d1d05312bd5",
		    personAttributeSimpleObject, new RequestContext());
		Assert.assertEquals("Bangalore", created.get("value"));
	}
	
	@Test
	public void setValue_shouldSetProperAttributableIdIfFound() {
		PersonAttributeType type = new PersonAttributeType();
		type.setFormat("org.openmrs.Location");
		type.setName("Second Home");
		type.setDescription("Testing Attributable domain objects");
		type.setSortWeight(5.5);
		type.setSearchable(false);
		type = personService.savePersonAttributeType(type);
		
		//Get the first location in from the list
		Location location = locationService.getAllLocations().get(0);
		
		PersonAttribute attribute = new PersonAttribute(type, null);
		attribute.setAttributeType(type);
		
		Assert.assertNull(attribute.getValue());
		
		resource.setValue(attribute, location.getUuid());
		
		Assert.assertEquals(location.getUuid(), attribute.getValue());
	}
	
	@Test
	public void setValue_shouldSetPassedValueIfCouldNotBeConvertedToAttributable() throws ClassNotFoundException {
		PersonAttributeType type = new PersonAttributeType();
		type.setFormat("org.openmrs.Location");
		type.setName("Second Home");
		type.setDescription("Testing Attributable domain objects");
		type.setSortWeight(5.5);
		type.setSearchable(false);
		type = personService.savePersonAttributeType(type);
		
		String nonExistenceLocationUuid = "this-uuid-does-not-exist-of-course";
		PersonAttribute attribute = new PersonAttribute(type, null);
		
		Assert.assertNull(attribute.getValue());
		Assert.assertTrue(Attributable.class.isAssignableFrom(Context.loadClass(type.getFormat())));
		
		resource.setValue(attribute, nonExistenceLocationUuid);
		
		Assert.assertEquals(nonExistenceLocationUuid, attribute.getValue());
	}
	
	@Test
	public void setValue_shouldSetThePassedValueForNonAttributableClasses() throws ClassNotFoundException {
		PersonAttributeType type = personService.getPersonAttributeTypeByName("Race");
		PersonAttribute attribute = new PersonAttribute(type, null);
		
		Assert.assertNull(attribute.getValue());
		Assert.assertEquals("java.lang.String", type.getFormat());
		Assert.assertFalse(Attributable.class.isAssignableFrom(Context.loadClass(type.getFormat())));
		
		resource.setValue(attribute, "arab");
		
		Assert.assertEquals("arab", attribute.getValue());
	}
}
