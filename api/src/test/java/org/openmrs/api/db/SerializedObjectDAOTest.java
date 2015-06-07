/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.Program;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.hibernate.HibernateSerializedObjectDAO;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.StartModule;
import org.openmrs.test.Verifies;

/**
 * This class tests the {@link SerializedObjectDAO} linked to from the Context. Currently that file
 * is the {@link HibernateSerializedObjectDAO}.
 */
@Ignore("TRUNK-4704 Serialization.xstream module must be fixed to work with Hibernate 4")
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
			dao.registerSupportedType(Program.class);
		}
		
	}
	
	@Test
	@Verifies(value = "should return the saved object", method = "getObject(Class, Integer)")
	public void getObject_shouldReturnTheSavedObject() throws Exception {
		Program data = dao.getObject(Program.class, 1);
		assertEquals(data.getId().intValue(), 1);
		assertEquals(data.getName(), "TestProgram");
	}
	
	@Test
	@Verifies(value = "should return the saved object", method = "getObjectByUuid(Class, String)")
	public void getObjectByUuid_shouldReturnTheSavedObject() throws Exception {
		Program data = dao.getObjectByUuid(Program.class, "83b452ca-a4c8-4bf2-9e0b-8bbddf2f9901");
		assertEquals(data.getId().intValue(), 2);
		assertEquals(data.getName(), "TestProgram2");
	}
	
	@Test
	@Verifies(value = "should save the passed object if supported", method = "saveObject(OpenmrsObject)")
	public void saveObject_shouldSaveThePassedObjectIfSupported() throws Exception {
		Program data = new Program();
		data.setName("NewProgram");
		data.setDescription("This is to test saving a Program");
		data.setCreator(new User(1));
		data.setDateCreated(new Date());
		data = dao.saveObject(data);
		Assert.assertNotNull(data.getId());
		Program newData = dao.getObject(Program.class, data.getId());
		assertEquals("NewProgram", newData.getName());
	}
	
	@Test
	@Verifies(value = "should set auditable fields before serializing", method = "saveObject(OpenmrsObject)")
	public void saveObject_shouldSetAuditableFieldsBeforeSerializing() throws Exception {
		Program data = new Program();
		data.setName("NewProgram");
		data.setDescription("This is to test saving a Program");
		data = dao.saveObject(data);
		Assert.assertNotNull(data.getId());
		Program newData = dao.getObject(Program.class, data.getId());
		assertEquals("NewProgram", newData.getName());
		assertNotNull(newData.getCreator());
		assertNotNull(newData.getDateCreated());
	}
	
	@Test(expected = DAOException.class)
	@Verifies(value = "should throw an exception if object not supported", method = "saveObject(OpenmrsObject)")
	public void saveObject_shouldThrowAnExceptionIfObjectNotSupported() throws Exception {
		dao.unregisterSupportedType(Program.class);
		Program data = new Program();
		data.setName("NewProgram");
		data.setDescription("This is to test saving a Program");
		dao.saveObject(data);
	}
	
	@Test
	@Verifies(value = "should return all saved objects of the passed type", method = "getAllObjects(Class)")
	public void getAllObjects_shouldReturnAllSavedObjectsOfThePassedType() throws Exception {
		List<Program> l = dao.getAllObjects(Program.class);
		assertEquals(2, l.size());
	}
	
	@Test
	@Verifies(value = "should return only non-retired objects of the passed type if not includeRetired", method = "getAllObjects(Class, boolean)")
	public void getAllObjects_shouldReturnOnlyNonRetiredObjectsOfThePassedTypeIfNotIncludeRetired() throws Exception {
		List<Program> l = dao.getAllObjects(Program.class, false);
		assertEquals(2, l.size());
		l = dao.getAllObjects(Program.class, true);
		assertEquals(3, l.size());
	}
	
	@Test
	@Verifies(value = "should return all saved objects with the given type and exact name", method = "getAllObjectsByName(Class, String, boolean)")
	public void getAllObjects_shouldReturnAllSavedObjectsWithTheGivenTypeAndExactName() throws Exception {
		List<Program> l = dao.getAllObjectsByName(Program.class, "TestProgram", true);
		assertEquals(1, l.size());
		assertEquals(l.get(0).getName(), "TestProgram");
	}
	
	@Test
	@Verifies(value = "should return all saved objects with the given type and partial name", method = "getAllObjectsByName(Class, String, boolean)")
	public void getAllObjects_shouldReturnAllSavedObjectsWithTheGivenTypeAndPartialName() throws Exception {
		List<Program> l = dao.getAllObjectsByName(Program.class, "TestProgram", false);
		assertEquals(3, l.size());
	}
	
	@Test
	@Verifies(value = "should delete the object with the passed id", method = "purgeObject(Integer)")
	public void purgeObject_shouldDeleteTheObjectWithThePassedId() throws Exception {
		List<Program> l = dao.getAllObjects(Program.class);
		assertEquals(2, l.size());
		dao.purgeObject(2);
		Context.flushSession();
		l = dao.getAllObjects(Program.class);
		assertEquals(1, l.size());
	}
	
}
