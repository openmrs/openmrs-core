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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Form;
import org.openmrs.FormResource;
import org.openmrs.api.DatatypeService;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.ClobDatatypeStorage;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.test.Util;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_9;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

public class FormResourceController1_9Test extends MainResourceControllerTest {
	
	private FormService formService;
	
	private DatatypeService datatypeService;
	
	@Before
	public void before() throws Exception {
		formService = Context.getFormService();
		datatypeService = Context.getDatatypeService();
		
		executeDataSet(RestTestConstants1_9.TEST_DATASET);
		executeDataSet(RestTestConstants1_9.FORM_RESOURCE_DATA_SET);
	}
	
	@Test
	public void shouldCreateANewFormResource() throws Exception {
		long before = getAllCount();
		
		String jsonPayload = "{" + "\"form\": \"" + RestTestConstants1_9.FORM_UUID + "\", "
		        + "\"dataType\": \"org.openmrs.customdatatype.datatype.LongFreeTextDatatype\","
		        + "\"name\": \"Test Resource 2\"," + "\"valueReference\": \""
		        + RestTestConstants1_9.CLOBDATATYPESTORAGE_RESOURCE_UUID + "\"" + "}";
		
		MockHttpServletResponse response = handle(newPostRequest(getURI(), jsonPayload));
		
		Assert.assertEquals(before + 1, getAllCount());
		
		Object resource = deserialize(response);
		Assert.assertEquals("Test Resource 2", PropertyUtils.getProperty(resource, "name"));
	}
	
	@Test
	public void shouldListFormResourcesForAForm() throws Exception {
		MockHttpServletResponse response = handle(newGetRequest(getURI()));
		
		List<Object> resources = Util.getResultsList(deserialize(response));
		
		List<FormResource> resourceObjects = (List<FormResource>) formService.getFormResourcesForForm(formService
		        .getFormByUuid(RestTestConstants1_9.FORM_UUID));
		
		List<String> names = new ArrayList<String>();
		for (Object resource : resources) {
			names.add((String) PropertyUtils.getProperty(resource, "name"));
		}
		
		Assert.assertEquals(resourceObjects.size(), resources.size());
		Assert.assertTrue(names.contains("Resource 1"));
		Assert.assertTrue(names.contains("Resource 2"));
		Assert.assertTrue(names.contains("Resource 3"));
	}
	
	@Test
	public void shouldReturnAResourceWithValueLink() throws Exception {
		MockHttpServletResponse response = handle(newGetRequest(getURI() + "/" + getUuid()));
		
		Object resource = deserialize(response);
		
		Assert.assertEquals(getUuid(), PropertyUtils.getProperty(resource, "uuid"));
		
		List<Object> links = (List<Object>) PropertyUtils.getProperty(resource, "links");
		
		Map<String, String> linksMap = new HashMap<String, String>();
		
		for (Object link : links) {
			linksMap.put((String) PropertyUtils.getProperty(link, "rel"), (String) PropertyUtils.getProperty(link, "uri"));
		}
		
		String uriPrefix = RestConstants.URI_PREFIX;
		
		String expectedLink = uriPrefix + "v1/" + getURI() + "/" + getUuid() + "/value";
		
		Assert.assertTrue(linksMap.containsKey("value"));
		Assert.assertEquals(expectedLink, linksMap.get("value"));
	}
	
	@Test
	public void shouldDeleteAFormResource() throws Exception {
		long before = getAllCount();
		MockHttpServletResponse response = handle(newDeleteRequest(getURI() + "/" + getUuid()));
		
		Assert.assertEquals(before - 1, getAllCount());
	}
	
	@Test
	public void shouldPostFormResourceValue() throws Exception {
		byte[] fileData = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream(
		    RestTestConstants1_9.TEST_RESOURCE_FILE));
		
		String valueReferenceBefore = formService.getFormResourceByUuid(getUuid()).getValueReference();
		
		MockMultipartFile toUpload = new MockMultipartFile("value", "formresource.txt", "text/plain", fileData);
		
		MockMultipartHttpServletRequest request = new MockMultipartHttpServletRequest();
		
		//Posting to uri of the form /ws/rest/v1/form/{uuid}/resource/{uuid}/value
		String uri = getBaseRestURI() + getURI() + "/" + getUuid() + "/value";
		request.setRequestURI(uri);
		request.setMethod(RequestMethod.POST.name());
		request.addHeader("Content-Type", "multipart/form-data");
		
		request.addFile(toUpload);
		
		MockHttpServletResponse response = handle(request);
		
		String valueReferenceAfter = formService.getFormResourceByUuid(getUuid()).getValueReference();
		
		Assert.assertNotEquals(valueReferenceBefore, valueReferenceAfter);
		Assert.assertNotNull(datatypeService.getClobDatatypeStorageByUuid(valueReferenceAfter));
		Assert.assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
	}
	
	@Test
	public void shouldRetrieveResourceValueAsFile() throws Exception {
		// Get the clobData for the resource
		FormResource resource = formService.getFormResourceByUuid(getUuid());
		ClobDatatypeStorage clobData = datatypeService.getClobDatatypeStorageByUuid(resource.getValueReference());
		
		MockHttpServletResponse response = handle(newGetRequest(getURI() + "/" + getUuid() + "/value"));
		
		String expected = "attachment;filename=\"" + resource.getName() + "\"";
		Assert.assertTrue(StringUtils.equals((String) response.getHeader("Content-Disposition"), expected));
		Assert.assertEquals(clobData.getValue(), response.getContentAsString());
	}
	
	@Test
	public void shouldGetAFormResourceByUuid() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + getUuid());
		SimpleObject result = deserialize(handle(req));
		
		FormResource resource = formService.getFormResourceByUuid(getUuid());
		assertEquals(resource.getUuid(), PropertyUtils.getProperty(result, "uuid"));
		assertEquals(resource.getName(), PropertyUtils.getProperty(result, "name"));		
	}
	
	@Test
	public void shouldEditAFormResource() throws Exception {		
		final String EDITED_NAME = "Edited Form Resource";
		FormResource formResource = formService.getFormResourceByUuid(getUuid());
		Assert.assertFalse(EDITED_NAME.equals(formResource.getForm().getName()));
		
		String json = "{ \"name\":\"" + EDITED_NAME + "\" }";
		MockHttpServletRequest req = request(RequestMethod.POST, getURI() + "/" + getUuid());
		req.setContent(json.getBytes());
		handle(req);

		FormResource editedForm = formService.getFormResourceByUuid(getUuid());
		Assert.assertEquals(EDITED_NAME, editedForm.getName());
	}
	
	@Override
	public String getURI() {
		return "form/" + RestTestConstants1_9.FORM_UUID + "/resource";
	}
	
	@Override
	public String getUuid() {
		return RestTestConstants1_9.FORM_RESOURCE_UUID;
	}
	
	@Override
	public long getAllCount() {
		List<Form> forms = formService.getAllForms();
		int count = 0;
		for (Form f : forms) {
			count += Context.getFormService().getFormResourcesForForm(f).size();
		}
		return count;
	}
}
