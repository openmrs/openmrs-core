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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openmrs.Program;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.hibernate.HibernateSerializedObjectDAO;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.test.StartModule;

/**
 * This class tests the {@link SerializedObjectDAO} linked to from the Context. Currently that file
 * is the {@link HibernateSerializedObjectDAO}.
 */
@Disabled("TRUNK-4704 Serialization.xstream module must be fixed to work with Hibernate 4")
@StartModule( { "org/openmrs/api/db/include/serialization.xstream-0.2.8-SNAPSHOT.omod" })
public class SerializedObjectDAOTest extends BaseContextSensitiveTest {
	
	private SerializedObjectDAO dao = null;
	
	/**
	 * Run this before each unit test in this class. The "@Before" method in
	 * {@link BaseContextSensitiveTest} is run right before this method.
	 * 
	 * @throws Exception
	 */
	@BeforeEach
	public void runBeforeEachTest() {
		
		assertNotNull(Context.getSerializationService().getDefaultSerializer());
		
		executeDataSet("org/openmrs/api/db/include/SerializedObjectDAOTest-initialData.xml");
		if (dao == null) {
			dao = (SerializedObjectDAO) applicationContext.getBean("serializedObjectDAO");
			dao.registerSupportedType(Program.class);
		}
		
	}
	
	@Test
	public void getObject_shouldReturnTheSavedObject() {
		Program data = dao.getObject(Program.class, 1);
		assertEquals(data.getId().intValue(), 1);
		assertEquals(data.getName(), "TestProgram");
	}
	
	@Test
	public void getObjectByUuid_shouldReturnTheSavedObject() {
		Program data = dao.getObjectByUuid(Program.class, "83b452ca-a4c8-4bf2-9e0b-8bbddf2f9901");
		assertEquals(data.getId().intValue(), 2);
		assertEquals(data.getName(), "TestProgram2");
	}
	
	@Test
	public void saveObject_shouldSaveThePassedObjectIfSupported() {
		Program data = new Program();
		data.setName("NewProgram");
		data.setDescription("This is to test saving a Program");
		data.setCreator(new User(1));
		data.setDateCreated(new Date());
		data = dao.saveObject(data);
		assertNotNull(data.getId());
		Program newData = dao.getObject(Program.class, data.getId());
		assertEquals("NewProgram", newData.getName());
	}
	
	@Test
	public void saveObject_shouldSetAuditableFieldsBeforeSerializing() {
		Program data = new Program();
		data.setName("NewProgram");
		data.setDescription("This is to test saving a Program");
		data = dao.saveObject(data);
		assertNotNull(data.getId());
		Program newData = dao.getObject(Program.class, data.getId());
		assertEquals("NewProgram", newData.getName());
		assertNotNull(newData.getCreator());
		assertNotNull(newData.getDateCreated());
	}
	
	@Test
	public void saveObject_shouldThrowAnExceptionIfObjectNotSupported() {
		dao.unregisterSupportedType(Program.class);
		Program data = new Program();
		data.setName("NewProgram");
		data.setDescription("This is to test saving a Program");
		assertThrows(DAOException.class, () -> dao.saveObject(data));
	}
	
	@Test
	public void getAllObjects_shouldReturnAllSavedObjectsOfThePassedType() {
		List<Program> l = dao.getAllObjects(Program.class);
		assertEquals(2, l.size());
	}
	
	@Test
	public void getAllObjects_shouldReturnOnlyNonRetiredObjectsOfThePassedTypeIfNotIncludeRetired() {
		List<Program> l = dao.getAllObjects(Program.class, false);
		assertEquals(2, l.size());
		l = dao.getAllObjects(Program.class, true);
		assertEquals(3, l.size());
	}
	
	@Test
	public void getAllObjects_shouldReturnAllSavedObjectsWithTheGivenTypeAndExactName() {
		List<Program> l = dao.getAllObjectsByName(Program.class, "TestProgram", true);
		assertEquals(1, l.size());
		assertEquals(l.get(0).getName(), "TestProgram");
	}
	
	@Test
	public void getAllObjects_shouldReturnAllSavedObjectsWithTheGivenTypeAndPartialName() {
		List<Program> l = dao.getAllObjectsByName(Program.class, "TestProgram", false);
		assertEquals(3, l.size());
	}
	
	@Test
	public void purgeObject_shouldDeleteTheObjectWithThePassedId() {
		List<Program> l = dao.getAllObjects(Program.class);
		assertEquals(2, l.size());
		dao.purgeObject(2);
		Context.flushSession();
		l = dao.getAllObjects(Program.class);
		assertEquals(1, l.size());
	}
	
}
