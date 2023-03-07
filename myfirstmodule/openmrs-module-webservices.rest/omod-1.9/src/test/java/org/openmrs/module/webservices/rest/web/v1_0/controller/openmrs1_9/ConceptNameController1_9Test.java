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

import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.ConceptName;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.test.Util;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;

public class ConceptNameController1_9Test extends MainResourceControllerTest {
	
	String conceptUuid = "0cbe2ed3-cd5f-4f46-9459-26127c9265ab";
	
	String nameUuid = "b8159118-c97b-4d5a-a63e-d4aa4be0c4d3";
	
	String conceptUuid2 = "a09ab2c5-878e-4905-b25d-5784167d0216";
	
	private ConceptService service;
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getURI()
	 */
	@Override
	public String getURI() {
		return "concept/" + conceptUuid + "/name";
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getUuid()
	 */
	@Override
	public String getUuid() {
		return nameUuid;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getAllCount()
	 */
	@Override
	public long getAllCount() {
		return service.getConceptByUuid(conceptUuid).getNames().size();
	}
	
	@Before
	public void before() throws Exception {
		this.service = Context.getConceptService();
	}
	
	@Test
	public void shouldAddNameToConcept() throws Exception {
		int before = service.getConceptByUuid(conceptUuid).getNames().size();
		String json = "{ \"name\":\"COUGH SYRUP II\", \"locale\":\"en\"}";
		
		handle(newPostRequest(getURI(), json));
		
		int after = service.getConceptByUuid(conceptUuid).getNames().size();
		Assert.assertEquals(before + 1, after);
	}
	
	@Test
	public void shouldListNamesForAConcept() throws Exception {
		SimpleObject response = deserialize(handle(newGetRequest("concept/" + conceptUuid2 + "/name")));
		
		List<Object> resultsList = Util.getResultsList(response);
		
		Assert.assertEquals(3, resultsList.size());
		List<Object> names = Arrays.asList(PropertyUtils.getProperty(resultsList.get(0), "name"),
		    PropertyUtils.getProperty(resultsList.get(1), "name"), PropertyUtils.getProperty(resultsList.get(2), "name"));
		
		Assert.assertTrue(names.contains("CD4 COUNT"));
		Assert.assertTrue(names.contains("CD4"));
		Assert.assertTrue(names.contains("CD3+CD4+ABS CNT"));
	}
	
	@Test
	public void shouldEditAConceptName() throws Exception {
		ConceptName conceptName = service.getConceptNameByUuid(nameUuid);
		Assert.assertNotNull(conceptName);
		Assert.assertEquals("COUGH SYRUP", conceptName.getName());
		
		String json = "{ \"name\":\"NEW TEST NAME\"}";
		
		handle(newPostRequest(getURI() + "/" + getUuid(), json));
		
		ConceptName updateConceptName = service.getConceptNameByUuid(nameUuid);
		//should have voided the old edited name
		Assert.assertTrue(updateConceptName.isVoided());
		
		SimpleObject results2 = deserialize(handle(newGetRequest(getURI())));
		List<Object> results2List = Util.getResultsList(results2);
		Assert.assertEquals(1, results2List.size());
		//should have created a new one with the new name
		Assert.assertTrue(PropertyUtils.getProperty(results2List.get(0), "name").equals("NEW TEST NAME"));
	}
	
	@Test
	public void shouldDeleteAConceptName() throws Exception {
		int before = service.getConceptByUuid(conceptUuid2).getNames().size();
		
		handle(newDeleteRequest("concept/" + conceptUuid2 + "/name/8230adbf-30a9-4e18-b6d7-fc57e0c23cab", new Parameter(
		        "reason", "testing")));
		
		int after = service.getConceptByUuid(conceptUuid2).getNames().size();
		Assert.assertEquals(before - 1, after);
	}
	
	@Test
	public void shouldPurgeAConceptName() throws Exception {
		String conceptId = "5497";
		//using sql to be able to include voided names too
		Long before = (Long) Context.getAdministrationService()
		        .executeSQL("select count(*) from concept_name where concept_id = " + conceptId, true).get(0).get(0);
		
		handle(newDeleteRequest("concept/" + conceptUuid2 + "/name/8230adbf-30a9-4e18-b6d7-fc57e0c23cab", new Parameter(
		        "purge", "")));
		
		Long after = (Long) Context.getAdministrationService()
		        .executeSQL("select count(*) from concept_name where concept_id = " + conceptId, true).get(0).get(0);
		Assert.assertEquals(before.longValue() - 1, after.longValue());
	}
}
