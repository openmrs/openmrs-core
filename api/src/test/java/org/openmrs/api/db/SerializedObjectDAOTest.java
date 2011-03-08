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
package org.openmrs.api.db;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.hibernate.HibernateSerializedObjectDAO;
import org.openmrs.report.ReportSchema;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.StartModule;
import org.openmrs.test.Verifies;

/**
 * This class tests the {@link SerializedObjectDAO} linked to from the Context. Currently that file
 * is the {@link HibernateSerializedObjectDAO}.
 */
@StartModule( { "org/openmrs/api/db/include/serialization.xstream-0.1.1.omod" })
public class SerializedObjectDAOTest extends BaseContextSensitiveTest {
	
	private SerializedObjectDAO dao = null;
	
	/**
	 * Run this before each unit test in this class. The "@Before" method in
	 * {@link BaseContextSensitiveTest} is run right before this method.
	 * 
	 * @throws Exception
	 */
	@Before
	public void runBeforeEachTest() throws Exception {
		
		Assert.assertNotNull(Context.getSerializationService().getDefaultSerializer());
		
		executeDataSet("org/openmrs/api/db/include/SerializedObjectDAOTest-initialData.xml");
		if (dao == null) {
			dao = (SerializedObjectDAO) applicationContext.getBean("serializedObjectDAO");
			dao.registerSupportedType(ReportSchema.class);
		}
		
	}
	
	@Test
	@Verifies(value = "should return the saved object", method = "getObject(Class, Integer)")
	public void getObject_shouldReturnTheSavedObject() throws Exception {
		ReportSchema data = dao.getObject(ReportSchema.class, 1);
		assertEquals(data.getId().intValue(), 1);
		assertEquals(data.getName(), "TestReport");
	}
	
	@Test
	@Verifies(value = "should return the saved object", method = "getObjectByUuid(Class, String)")
	public void getObjectByUuid_shouldReturnTheSavedObject() throws Exception {
		ReportSchema data = dao.getObjectByUuid(ReportSchema.class, "83b452ca-a4c8-4bf2-9e0b-8bbddf2f9901");
		assertEquals(data.getId().intValue(), 2);
		assertEquals(data.getName(), "TestReport2");
	}
	
	@Test
	@Verifies(value = "should save the passed object if supported", method = "saveObject(OpenmrsObject)")
	public void saveObject_shouldSaveThePassedObjectIfSupported() throws Exception {
		ReportSchema data = new ReportSchema();
		data.setName("NewReport");
		data.setDescription("This is to test saving a report");
		data.setCreator(new User(1));
		data.setDateCreated(new Date());
		data = dao.saveObject(data);
		Assert.assertNotNull(data.getId());
		ReportSchema newData = dao.getObject(ReportSchema.class, data.getId());
		assertEquals("NewReport", newData.getName());
	}
	
	@Test(expected = DAOException.class)
	@Verifies(value = "should throw an exception if object not supported", method = "saveObject(OpenmrsObject)")
	public void saveObject_shouldThrowAnExceptionIfObjectNotSupported() throws Exception {
		dao.unregisterSupportedType(ReportSchema.class);
		ReportSchema data = new ReportSchema();
		data.setName("NewReport");
		data.setDescription("This is to test saving a report");
		data = dao.saveObject(data);
	}
	
	@Test
	@Verifies(value = "should return all saved objects of the passed type", method = "getAllObjects(Class)")
	public void getAllObjects_shouldReturnAllSavedObjectsOfThePassedType() throws Exception {
		List<ReportSchema> l = dao.getAllObjects(ReportSchema.class);
		assertEquals(2, l.size());
	}
	
	@Test
	@Verifies(value = "should return only non-retired objects of the passed type if not includeRetired", method = "getAllObjects(Class, boolean)")
	public void getAllObjects_shouldReturnOnlyNonRetiredObjectsOfThePassedTypeIfNotIncludeRetired() throws Exception {
		List<ReportSchema> l = dao.getAllObjects(ReportSchema.class, false);
		assertEquals(2, l.size());
		l = dao.getAllObjects(ReportSchema.class, true);
		assertEquals(3, l.size());
	}
	
	@Test
	@Verifies(value = "should return all saved objects with the given type and exact name", method = "getAllObjectsByName(Class, String, boolean)")
	public void getAllObjects_shouldReturnAllSavedObjectsWithTheGivenTypeAndExactName() throws Exception {
		List<ReportSchema> l = dao.getAllObjectsByName(ReportSchema.class, "TestReport", true);
		assertEquals(1, l.size());
		assertEquals(l.get(0).getName(), "TestReport");
	}
	
	@Test
	@Verifies(value = "should return all saved objects with the given type and partial name", method = "getAllObjectsByName(Class, String, boolean)")
	public void getAllObjects_shouldReturnAllSavedObjectsWithTheGivenTypeAndPartialName() throws Exception {
		List<ReportSchema> l = dao.getAllObjectsByName(ReportSchema.class, "TestReport", false);
		assertEquals(3, l.size());
	}
	
	@Test
	@Verifies(value = "should delete the object with the passed id", method = "purgeObject(Integer)")
	public void purgeObject_shouldDeleteTheObjectWithThePassedId() throws Exception {
		List<ReportSchema> l = dao.getAllObjects(ReportSchema.class);
		assertEquals(2, l.size());
		dao.purgeObject(2);
		l = dao.getAllObjects(ReportSchema.class);
		assertEquals(1, l.size());
	}
	
}
