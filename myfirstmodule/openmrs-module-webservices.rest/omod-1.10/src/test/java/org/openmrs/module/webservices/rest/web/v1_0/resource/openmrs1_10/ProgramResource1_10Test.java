/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_10;

import org.apache.commons.beanutils.PropertyUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Program;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_8;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;

public class ProgramResource1_10Test extends MainResourceControllerTest {
	
	private ProgramWorkflowService service;
	
	@Before
	public void init() {
		service = Context.getProgramWorkflowService();
	}
	
	@Override
	public String getURI() {
		return "program";
	}
	
	@Override
	public String getUuid() {
		return RestTestConstants1_8.PROGRAM_UUID;
	}
	
	@Override
	public long getAllCount() {
		return service.getAllPrograms(false).size();
	}
	
	@Test
	public void shouldCreateAProgram() throws Exception {
		long originalCount = getAllCount();
		
		SimpleObject program = new SimpleObject();
		program.add("name", "Program name");
		program.add("description", "Program description");
		program.add("concept", RestTestConstants1_8.CONCEPT_UUID);
		program.add("outcomesConcept", RestTestConstants1_8.CONCEPT2_UUID);
		
		String json = new ObjectMapper().writeValueAsString(program);
		
		MockHttpServletRequest req = request(RequestMethod.POST, getURI());
		req.setContent(json.getBytes());
		
		SimpleObject newProgram = deserialize(handle(req));
		
		Assert.assertNotNull(PropertyUtils.getProperty(newProgram, "uuid"));
		Assert.assertEquals(RestTestConstants1_8.CONCEPT2_UUID,
		    ((Map) PropertyUtils.getProperty(newProgram, "outcomesConcept")).get("uuid"));
		Assert.assertEquals(originalCount + 1, getAllCount());
	}
	
	@Test
	public void shouldGetAProgramByUuid() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + getUuid());
		SimpleObject result = deserialize(handle(req));
		
		Program program = service.getProgramByUuid(getUuid());
		Assert.assertNotNull(result);
		Assert.assertEquals(program.getUuid(), PropertyUtils.getProperty(result, "uuid"));
		Assert.assertEquals(program.getName(), PropertyUtils.getProperty(result, "name"));
		Assert.assertEquals(program.getOutcomesConcept(), PropertyUtils.getProperty(result, "outcomesConcept"));
	}
	
}
