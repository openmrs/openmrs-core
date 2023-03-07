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
import java.util.Locale;

import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptDescription;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.test.Util;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.ConceptDescriptionResource1_8;

public class ConceptDescriptionController1_9Test extends MainResourceControllerTest {
	
	String conceptUuid = "b055abd8-a420-4a11-8b98-02ee170a7b54";
	
	String descriptionUuid = "be3321b3-c1c7-4339-aaca-1b60db12e1df";
	
	private ConceptService service;
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getURI()
	 */
	@Override
	public String getURI() {
		return "concept/" + conceptUuid + "/description";
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getUuid()
	 */
	@Override
	public String getUuid() {
		return descriptionUuid;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getAllCount()
	 */
	@Override
	public long getAllCount() {
		return service.getConceptByUuid(conceptUuid).getDescriptions().size();
	}
	
	@Before
	public void before() throws Exception {
		this.service = Context.getConceptService();
	}
	
	/**
	 * @See {@link ConceptDescriptionResource1_8#create(String, SimpleObject, org.openmrs.module.webservices.rest.web.RequestContext)}
	 * @throws Exception
	 */
	@Test
	public void shouldAddADescriptionToConcept() throws Exception {
		int before = service.getConceptByUuid(conceptUuid).getDescriptions().size();
		String json = "{ \"description\":\"New Description\", \"locale\":\"fr\"}";
		
		handle(newPostRequest(getURI(), json));
		
		int after = service.getConceptByUuid(conceptUuid).getDescriptions().size();
		Assert.assertEquals(before + 1, after);
	}
	
	/**
	 * @See {@link ConceptDescriptionResource1_8#doGetAll(Concept, org.openmrs.module.webservices.rest.web.RequestContext)}
	 * @throws Exception
	 */
	@Test
	public void shouldListDescriptionsForAConcept() throws Exception {
		//Add one more description for testing purposes
		Concept testConcept = Context.getConceptService().getConceptByUuid(conceptUuid);
		ConceptDescription testDescription = new ConceptDescription("another description", new Locale("fr"));
		testConcept.addDescription(testDescription);
		Context.getConceptService().saveConcept(testConcept);
		Assert.assertNotNull(testDescription.getConceptDescriptionId());
		Assert.assertEquals(2, testConcept.getDescriptions().size());
		
		SimpleObject response = deserialize(handle(newGetRequest(getURI())));
		
		List<Object> resultsList = Util.getResultsList(response);
		Assert.assertEquals(2, resultsList.size());
		List<Object> descriptions = Arrays.asList(PropertyUtils.getProperty(resultsList.get(0), "description"),
		    PropertyUtils.getProperty(resultsList.get(1), "description"));
		
		Assert.assertTrue(descriptions.contains("Affirmative"));
		Assert.assertTrue(descriptions.contains("another description"));
	}
	
	/**
	 * @See {@link ConceptDescriptionResource1_8#update(String, String, SimpleObject, org.openmrs.module.webservices.rest.web.RequestContext)}
	 * @throws Exception
	 */
	@Test
	public void shouldEditAConceptDescription() throws Exception {
		ConceptDescription conceptDescription = service.getConceptDescriptionByUuid(descriptionUuid);
		Assert.assertEquals("Affirmative", conceptDescription.getDescription());
		
		String json = "{ \"description\":\"NEW TEST DESCRIPTION\"}";
		handle(newPostRequest(getURI() + "/" + getUuid(), json));
		
		//should have created a new one with the new description
		Assert.assertTrue(PropertyUtils.getProperty(conceptDescription, "description").equals("NEW TEST DESCRIPTION"));
	}
	
	/**
	 * This tests that delete always delegates to
	 * {@link ConceptDescriptionResource1_8#purge(ConceptDescription, org.openmrs.module.webservices.rest.web.RequestContext)}
	 * since descriptions are not retirable/voidable
	 * 
	 * @see {@link ConceptDescriptionResource1_8#delete(ConceptDescription, String, org.openmrs.module.webservices.rest.web.RequestContext)}
	 * @throws Exception
	 */
	@Test
	public void shouldDeleteAConceptDescription() throws Exception {
		int before = service.getConceptByUuid(conceptUuid).getDescriptions().size();
		
		handle(newDeleteRequest(getURI() + "/" + getUuid(), new Parameter("reason", "testing")));
		
		int after = service.getConceptByUuid(conceptUuid).getDescriptions().size();
		Assert.assertEquals(before - 1, after);
	}
	
	/**
	 * @See {@link ConceptDescriptionResource1_8#purge(ConceptDescription, org.openmrs.module.webservices.rest.web.RequestContext)}
	 * @throws Exception
	 */
	@Test
	public void shouldPurgeAConceptDescription() throws Exception {
		int before = service.getConceptByUuid(conceptUuid).getDescriptions().size();
		
		handle(newDeleteRequest(getURI() + "/" + getUuid(), new Parameter("purge", "")));
		
		int after = service.getConceptByUuid(conceptUuid).getDescriptions().size();
		Assert.assertEquals(before - 1, after);
	}
	
}
