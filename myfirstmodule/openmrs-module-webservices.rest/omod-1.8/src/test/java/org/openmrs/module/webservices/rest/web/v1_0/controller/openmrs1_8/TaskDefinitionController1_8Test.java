/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs1_8;

import org.apache.commons.beanutils.PropertyUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.test.Util;
import org.openmrs.module.webservices.rest.web.MockTaskServiceWrapper;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.TaskDefinitionResource1_8;
import org.openmrs.scheduler.TaskDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;
import java.util.List;

public class TaskDefinitionController1_8Test extends MainResourceControllerTest {
	
	@Autowired
	RestService restService;
	
	private TaskDefinition testTask = new TaskDefinition(1, "TestTask", "TestTask Description",
	        "org.openmrs.scheduler.tasks.TestTask");
	
	private MockTaskServiceWrapper mockTaskServiceWrapper = new MockTaskServiceWrapper();
	
	@Before
	public void setUp() throws Exception {
		testTask.setRepeatInterval(10L);
		testTask.setStartOnStartup(true);
		testTask.setProperty("propertyKey", "propertyValue");
		
		mockTaskServiceWrapper.registeredTasks.add(testTask);
		
		TaskDefinitionResource1_8 taskResource = (TaskDefinitionResource1_8) restService
		        .getResourceBySupportedClass(TaskDefinition.class);
		taskResource.setTaskServiceWrapper(mockTaskServiceWrapper);
	}
	
	/**
	 * Get all registered tasks - Used to test for registered tasks.
	 * 
	 * @throws Exception
	 */
	@Test
	@Override
	public void shouldGetAll() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		SimpleObject resultTasks = deserialize(handle(req));
		List<Object> results = Util.getResultsList(resultTasks);
		Assert.assertNotNull(resultTasks);
		Assert.assertEquals(results.size(), getAllCount());
	}
	
	/**
	 * Get all scheduled tasks - Used to test for scheduled tasks.
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldGetAllScheduled() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("q", "scheduled");
		SimpleObject resultTasks = deserialize(handle(req));
		List<Object> results = Util.getResultsList(resultTasks);
		Assert.assertNotNull(resultTasks);
		Assert.assertEquals(results.size(), getAllScheduledCount());
	}
	
	@Test
	public void shouldGetTaskByUuid() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + getUuid());
		SimpleObject result = deserialize(handle(req));
		Assert.assertNotNull(result.get("name"));
		Assert.assertNotNull(result.get("description"));
		Assert.assertNotNull(result.get("taskClass"));
	}
	
	@Test
	@Override
	public void shouldGetFullByUuid() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + getUuid());
		req.addParameter("v", "full");
		
		SimpleObject result = deserialize(handle(req));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "name"));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "description"));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "taskClass"));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "repeatInterval"));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "startOnStartup"));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "started"));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "properties"));
	}
	
	@Test
	@Override
	public void shouldGetDefaultByUuid() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + getUuid());
		SimpleObject result = deserialize(handle(req));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "name"));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "description"));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "taskClass"));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "startTime"));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "started"));
	}
	
	@Test
	@Override
	public void shouldGetRefByUuid() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + getUuid());
		req.addParameter("v", "ref");
		SimpleObject result = deserialize(handle(req));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "name"));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "description"));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "taskClass"));
	}
	
	@Test
	public void shouldSaveTaskDefinition() throws Exception {
		SimpleObject mockTask = new SimpleObject();
		mockTask.add("name", "MockTask");
		mockTask.add("description", "MockTask Description");
		mockTask.add("taskClass", "org.openmrs.scheduler.tasks.TestTask");
		mockTask.add("startTime", "2017-08-28T23:59:59.000+0530");
		mockTask.add("repeatInterval", "10");
		mockTask.add("startOnStartup", false);
		mockTask.add("properties", null);
		String json = new ObjectMapper().writeValueAsString(mockTask);
		
		Assert.assertNull(mockTaskServiceWrapper.getTaskByName("MockTask"));
		MockHttpServletRequest req = request(RequestMethod.POST, getURI());
		req.setContent(json.getBytes());
		deserialize(handle(req));
		Assert.assertEquals("MockTask", mockTaskServiceWrapper.getTaskByName("MockTask").getName());
	}
	
	@Override
	public String getURI() {
		return "taskdefinition";
	}
	
	@Override
	public String getUuid() {
		return getTestTaskName();
	}
	
	private String getTestTaskName() {
		return "TestTask";
	}
	
	@Override
	public long getAllCount() {
		return getAllRegisteredCount();
	}
	
	public long getAllRegisteredCount() {
		return mockTaskServiceWrapper.registeredTasks.size();
	}
	
	public long getAllScheduledCount() {
		return mockTaskServiceWrapper.scheduledTasks.size();
	}
	
}
