/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.openmrs.EncounterType;
import org.openmrs.api.context.Context;
import org.openmrs.serialization.OpenmrsSerializer;
import org.openmrs.serialization.xstream.XStreamSerializer;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;

/**
 * Test class that tests the SerializationService methods
 */
public class SerializationServiceTest extends BaseContextSensitiveTest {
	
	@Test
	@Verifies(value = "should return a serializer", method = "getDefaultCohort()")
	public void getDefaultSerializer_shouldReturnASerializer() throws Exception {
		assertNotNull(Context.getSerializationService().getDefaultSerializer());
	}
	
	@Test
	@Verifies(value = "should return a serializer of the given class", method = "getSerializer(Class)")
	public void getSerializer_shouldReturnASerializerOfTheGivenClass() throws Exception {
		OpenmrsSerializer s = Context.getSerializationService().getSerializer(XStreamSerializer.class);
		assertNotNull(s);
		assertEquals(s.getClass(), XStreamSerializer.class);
	}
	
	@Test
	public void deserialize_shouldSerializeAndDeserializeCorrectly() throws Exception {
		SerializationService svc = Context.getSerializationService();
		EncounterType e = new EncounterType();
		e.setName("TestType");
		e.setDescription("A test type");
		String xml = svc.serialize(e, XStreamSerializer.class);
		EncounterType hydratedLocation = svc.deserialize(xml, EncounterType.class, XStreamSerializer.class);
		assertEquals("TestType", hydratedLocation.getName());
		assertEquals("A test type", hydratedLocation.getDescription());
	}
	
	@Test
	public void deserialize_shouldSerializeAndDeserializeHibernateObjectsCorrectly() throws Exception {
		EncounterType e = Context.getEncounterService().getAllEncounterTypes().iterator().next();
		e.setDescription("Changed Emergency Visit");
		e = Context.getEncounterService().saveEncounterType(e);
		String s = Context.getSerializationService().serialize(e, XStreamSerializer.class);
		EncounterType e1 = Context.getSerializationService().deserialize(s, EncounterType.class, XStreamSerializer.class);
		assertEquals("Changed Emergency Visit", e1.getDescription());
	}
}
