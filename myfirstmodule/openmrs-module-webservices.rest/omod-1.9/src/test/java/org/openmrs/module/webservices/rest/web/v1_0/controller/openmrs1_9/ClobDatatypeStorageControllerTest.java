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

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.api.DatatypeService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.ClobDatatypeStorage;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_9;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

import java.sql.Connection;
import java.sql.ResultSet;

import javax.servlet.http.HttpServletResponse;

public class ClobDatatypeStorageControllerTest extends MainResourceControllerTest {
	
	private DatatypeService datatypeService;
	
	@Before
	public void before() throws Exception {
		datatypeService = Context.getDatatypeService();
		executeDataSet(RestTestConstants1_9.FORM_RESOURCE_DATA_SET);
	}
	
	@Test
	public void shouldAcceptAndStoreClobDataViaPost() throws Exception {
		long before = getAllCount();
		
		byte[] fileData = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream(
		    RestTestConstants1_9.TEST_RESOURCE_FILE));
		
		MockMultipartFile toUpload = new MockMultipartFile("file", "formresource.txt", "text/plain", fileData);
		
		MockMultipartHttpServletRequest request = new MockMultipartHttpServletRequest();
		request.setRequestURI(getBaseRestURI() + getURI());
		request.setMethod(RequestMethod.POST.name());
		request.addHeader("Content-Type", "multipart/form-data");
		
		request.addFile(toUpload);
		
		MockHttpServletResponse response = handle(request);
		
		Assert.assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
		Assert.assertEquals(before + 1, getAllCount());
	}
	
	@Test
	public void shouldReturnClobDataAsFileByUuid() throws Exception {
		ClobDatatypeStorage clob = datatypeService
		        .getClobDatatypeStorageByUuid(RestTestConstants1_9.CLOBDATATYPESTORAGE_RESOURCE_UUID);
		
		Assert.assertNotNull(clob);
		int size = clob.getValue().getBytes().length;
		MockHttpServletResponse response = handle(newGetRequest(getURI() + "/"
		        + RestTestConstants1_9.CLOBDATATYPESTORAGE_RESOURCE_UUID));
		
		Assert.assertEquals(size, response.getContentAsByteArray().length);
	}
	
	@Test
	public void shouldDeleteAnExistingClobData() throws Exception {
		ClobDatatypeStorage clob = datatypeService
		        .getClobDatatypeStorageByUuid(RestTestConstants1_9.CLOBDATATYPESTORAGE_RESOURCE_UUID);
		
		Assert.assertNotNull(clob);
		
		MockHttpServletResponse response = handle(newDeleteRequest(getURI() + "/"
		        + RestTestConstants1_9.CLOBDATATYPESTORAGE_RESOURCE_UUID));
		
		clob = datatypeService.getClobDatatypeStorageByUuid(RestTestConstants1_9.CLOBDATATYPESTORAGE_RESOURCE_UUID);
		
		Assert.assertNull(clob);
		Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
	}
	
	@Test
	public void shouldReturnHTTP404ForNonExistenceClobdata() throws Exception {
		MockHttpServletResponse response = handle(newGetRequest(getURI() + "/non-existence-uuid"));
		Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
	}
	
	@Override
	public String getURI() {
		return "clobdata";
	}
	
	@Override
	public String getUuid() {
		return RestTestConstants1_9.CLOBDATATYPESTORAGE_RESOURCE_UUID;
	}
	
	@Override
	public long getAllCount() {
		try {
			Connection connection = getConnection();
			
			ResultSet resultSet = connection.prepareStatement("select count('id') from clob_datatype_storage")
			        .executeQuery();
			if (resultSet.next()) {
				return resultSet.getLong(1);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	@Override
	@Ignore
	public void shouldGetRefByUuid() throws Exception {
		
	}
	
	@Override
	@Ignore
	public void shouldGetDefaultByUuid() throws Exception {
		
	}
	
	@Override
	@Ignore
	public void shouldGetFullByUuid() throws Exception {
		
	}
	
	@Override
	@Ignore
	public void shouldGetAll() throws Exception {
		
	}
}
