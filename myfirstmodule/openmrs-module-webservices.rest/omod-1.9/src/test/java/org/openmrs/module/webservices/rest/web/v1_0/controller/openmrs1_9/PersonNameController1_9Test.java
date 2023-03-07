/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs1_9;

import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.PersonName;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.test.Util;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Tests functionality of {@link PersonNameController}.
 */
public class PersonNameController1_9Test extends MainResourceControllerTest {
	
	private PersonService service;
	
	private String personUuid = "5946f880-b197-400b-9caa-a3c661d23041";
	
	private String personNameUuid = "a65c347e-1384-493a-a55b-d325924acd94";
	
	@Override
	public String getURI() {
		return "person/" + personUuid + "/name";
	}
	
	@Override
	public String getUuid() {
		return personNameUuid;
	}
	
	@Override
	public long getAllCount() {
		return service.getPersonByUuid(personUuid).getNames().size();
	}
	
	@Before
	public void init() throws Exception {
		this.service = Context.getPersonService();
		executeDataSet("customTestDataset.xml");
	}
	
	@Test
	public void shouldGetAPersonName() throws Exception {
		
		MockHttpServletRequest httpReq = request(RequestMethod.GET, getURI() + "/" + getUuid());
		SimpleObject result = deserialize(handle(httpReq));
		
		Assert.assertNotNull(result);
		Assert.assertNotNull(PropertyUtils.getProperty(result, "givenName"));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "familyName"));
		
	}
	
	@Test
	public void shouldListNamesForPerson() throws Exception {
		
		SimpleObject response = deserialize(handle(newGetRequest(getURI())));
		List<Object> resultsList = Util.getResultsList(response);
		
		Assert.assertEquals(2, resultsList.size());
		List<Object> names = Arrays.asList(PropertyUtils.getProperty(resultsList.get(0), "givenName"));
		Assert.assertTrue(names.get(0).equals("Collet"));
		
	}
	
	@Test
	public void shouldAddNameToPerson() throws Exception {
		
		int before = service.getPersonByUuid(personUuid).getNames().size();
		String json = "{ \"givenName\":\"name1\", \"middleName\":\"name2\", \"familyName\":\"name3\" }";
		
		handle(newPostRequest(getURI(), json));
		
		int after = service.getPersonByUuid(personUuid).getNames().size();
		Assert.assertEquals(before + 1, after);
		
	}
	
	@Test
	public void shouldEditName() throws Exception {
		
		PersonName personName = service.getPersonNameByUuid(getUuid());
		Assert.assertEquals("Chebaskwony", personName.getFamilyName());
		String json = "{ \"familyName\":\"newName\" }";
		
		SimpleObject response = deserialize(handle(newPostRequest(getURI() + "/" + getUuid(), json)));
		Assert.assertNotNull(response);
		Assert.assertEquals("newName", PropertyUtils.getProperty(response, "familyName").toString());
		PersonName editedPersonName = service.getPersonNameByUuid(getUuid());
		Assert.assertEquals("newName", editedPersonName.getFamilyName());
		
	}
	
	@Test
	public void shouldVoidName() throws Exception {
		
		PersonName personName = service.getPersonNameByUuid(getUuid());
		Assert.assertTrue(!personName.isVoided());
		
		handle(newDeleteRequest(getURI() + "/" + getUuid(), new Parameter("!purge", "random reason")));
		
		PersonName voidedPersonName = service.getPersonNameByUuid(getUuid());
		Assert.assertTrue(voidedPersonName.isVoided());
		
	}
	
	@Test
	public void shouldPurgeName() throws Exception {
		
		int before = service.getPersonByUuid(personUuid).getNames().size();
		
		handle(newDeleteRequest(getURI() + "/" + getUuid(), new Parameter("purge", "")));
		
		int after = service.getPersonByUuid(personUuid).getNames().size();
		Assert.assertNull(service.getPersonNameByUuid(getUuid()));
		Assert.assertEquals(before - 1, after);
		
	}
	
}
