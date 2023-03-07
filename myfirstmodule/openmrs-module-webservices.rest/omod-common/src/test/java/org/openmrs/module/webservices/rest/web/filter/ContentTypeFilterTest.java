/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.filter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Tests for the {@link ContentTypeFilter} class.
 */
public class ContentTypeFilterTest {
	
	private ContentTypeFilter testFilter;
	
	private MockFilterChain mockChain;
	
	private MockHttpServletRequest req;
	
	private MockHttpServletResponse resp;
	
	@Before
	public void init() {
		testFilter = new ContentTypeFilter();
		mockChain = new MockFilterChain();
		req = new MockHttpServletRequest();
		resp = new MockHttpServletResponse();
	}
	
	@Test
	public void doFilter_shouldNotAllowXmlContent() throws IOException, ServletException {
		
		List<String> xmlContentTypes = Arrays.asList("application/xml", "text/xml", "application/xml;utf-8");
		
		for (String contentType : xmlContentTypes) {
			init();
			req.setContentType(contentType);
			req.setMethod("POST");
			req.setRequestURI("/ws/rest/v1/obs");
			testFilter.doFilter(req, resp, mockChain);
			
			Assert.assertEquals(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, resp.getStatus());
		}
	}
	
	@Test
	public void doFilter_shouldAllowJSONContent() throws IOException, ServletException {
		req.setContentType("application/json");
		req.setMethod("POST");
		req.setRequestURI("/ws/rest/v1/obs");
		testFilter.doFilter(req, resp, mockChain);
		
		Assert.assertNotEquals(resp.getStatus(), HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
	}
	
	@Test
	public void doFilter_shouldAllowJSONContentTypeWithParameter() throws IOException, ServletException {
		req.setContentType("application/json;charset=UTF-8");
		req.setMethod("POST");
		req.setRequestURI("/ws/rest/v1/obs");
		testFilter.doFilter(req, resp, mockChain);
		
		Assert.assertNotEquals(resp.getStatus(), HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
	}
	
	@Test
	public void doFilter_shouldAllowMultipartFormDataContentTypeWithParameter() throws IOException, ServletException {
		req.setContentType("multipart/form-data; boundary=----WebKitFormBoundaryREl4lGYAfON7BGOo");
		req.setMethod("POST");
		req.setRequestURI("/ws/rest/v1/obs");
		testFilter.doFilter(req, resp, mockChain);
		
		Assert.assertNotEquals(resp.getStatus(), HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
	}
	
	@Test
	public void doFilter_shouldAllowNullContentType() throws IOException, ServletException {
		req.setMethod("POST");
		req.setRequestURI("/ws/rest/v1/obs");
		
		Assert.assertEquals(null, req.getContentType());
		
		testFilter.doFilter(req, resp, mockChain);
		
		Assert.assertNotEquals(resp.getStatus(), HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
	}
	
	@Test
	public void doFilter_shouldAllowGetRequest() throws IOException, ServletException {
		ContentTypeFilter testFilter = new ContentTypeFilter();
		req.setMethod("GET");
		req.setRequestURI("/ws/rest/v1/patient?sometestparam=bla");
		testFilter.doFilter(req, resp, mockChain);
		
		Assert.assertNotEquals(resp.getStatus(), HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
	}
}
