/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs1_11;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.test.Util;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;

public class DrugIngredientController1_11Test extends MainResourceControllerTest {
	
	String drugUuid = "3cfcf118-931c-46f7-8ff6-7b876f0d4202";
	
	String drugUuid2 = "05ec820a-d297-44e3-be6e-698531d9dd3f";
	
	String ingredientUuid = "6519d653-393d-4118-9c83-a3715b82d4dc";
	
	private ConceptService service;
	
	@Override
	public String getURI() {
		return "drug/" + drugUuid + "/ingredient";
	}
	
	@Override
	public String getUuid() {
		return ingredientUuid;
	}
	
	@Override
	public long getAllCount() {
		return service.getDrugByUuid(drugUuid).getIngredients().size();
	}
	
	@Before
	public void before() throws Exception {
		this.service = Context.getConceptService();
	}
	
	@Test
	public void shouldAddIngredientToDrug() throws Exception {
		int before = service.getDrugByUuid(drugUuid).getIngredients().size();
		String json = "{ \"ingredient\":\"0abca361-f6bf-49cc-97de-b2f37f099dde\", \"strength\":4.0, \"units\":\"0955b484-b364-43dd-909b-1fa3655eaad2\"}";
		
		handle(newPostRequest(getURI(), json));
		
		int after = service.getDrugByUuid(drugUuid).getIngredients().size();
		Assert.assertEquals(before + 1, after);
	}
	
	@Test
	public void shouldListIngredientsForADrug() throws Exception {
		SimpleObject response = deserialize(handle(newGetRequest("drug/" + drugUuid2 + "/ingredient")));
		
		List<Object> resultsList = Util.getResultsList(response);
		
		Assert.assertEquals(1, resultsList.size());
		List<Object> ingredients = Arrays.asList(PropertyUtils.getProperty(resultsList.get(0), "ingredient"));
		
		Assert.assertEquals("ASPIRIN", ((Map) ingredients.get(0)).get("display"));
		Assert.assertEquals("15f83cd6-64e9-4e06-a5f9-364d3b14a43d", ((Map) ingredients.get(0)).get("uuid"));
	}
}
