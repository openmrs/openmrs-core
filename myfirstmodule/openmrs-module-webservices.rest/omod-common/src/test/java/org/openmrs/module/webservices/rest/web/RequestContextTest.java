/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.openmrs.api.APIException;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;

public class RequestContextTest extends BaseModuleWebContextSensitiveTest {
	
	/**
	 * @see RequestContext#setLimit(Integer)
	 * @verifies not accept a value less than one
	 */
	@Test(expected = APIException.class)
	public void setLimit_shouldNotAcceptAValueLessThanOne() throws Exception {
		new RequestContext().setLimit(0);
	}
	
	/**
	 * @see RequestContext#setLimit(Integer)
	 * @verifies not accept a null value
	 */
	@Test(expected = APIException.class)
	public void setLimit_shouldNotAcceptANullValue() throws Exception {
		new RequestContext().setLimit(null);
	}
	
	/**
	 * @see RequestContext#getParameter(String)
	 * @verifies return null if request is null
	 */
	@Test
	public void getParameter_shouldReturnNullIfRequestIsNull() throws Exception {
		
		RequestContext requestContext = new RequestContext();
		
		assertNull(requestContext.getParameter("UNKOWN"));
	}
	
	/**
	 * @see RequestContext#getParameter(String)
	 * @verifies return null if the wanted request parameter is not present in the request
	 */
	@Test
	public void getParameter_shouldReturnNullIfTheWantedRequestParameterIsNotPresentInTheRequest() throws Exception {
		
		RequestContext requestContext = new RequestContext();
		MockHttpServletRequest request = new MockHttpServletRequest();
		requestContext.setRequest(request);
		
		assertNull(requestContext.getParameter("UNKOWN"));
	}
	
	/**
	 * @see RequestContext#getParameter(String)
	 * @verifies return the request parameter of given name if present in the request
	 */
	@Test
	public void getParameter_shouldReturnTheRequestParameterOfGivenNameIfPresentInTheRequest() throws Exception {
		
		RequestContext requestContext = new RequestContext();
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addParameter("includeAll", "true");
		requestContext.setRequest(request);
		
		assertThat(requestContext.getParameter("includeAll"), is("true"));
	}
}
