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

import java.util.ArrayList;

import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.report.DataSetDefinition;
import org.openmrs.report.ReportSchema;
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
	public void getSerializer_shouldSerializeAndDeserializeCorrectly() throws Exception {
		ReportSchema report = new ReportSchema();
		report.setName("TestReport");
		report.setDescription("A test report");
		report.setDataSetDefinitions(new ArrayList<DataSetDefinition>());
		String xml = Context.getSerializationService().serialize(report, XStreamSerializer.class);
		ReportSchema hydratedReport = Context.getSerializationService().deserialize(xml, ReportSchema.class, XStreamSerializer.class);
		assertEquals("TestReport", hydratedReport.getName());
		assertEquals("A test report", hydratedReport.getDescription());
	}
}
