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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * Tests for the {@link RestUtil} class.
 */
public class RestUtilTest {
	
	/**
	 * @see RestUtil#ipMatches(String,List)
	 * @verifies return true if list is empty
	 */
	@Test
	public void ipMatches_shouldReturnTrueIfListIsEmpty() throws Exception {
		Assert.assertTrue(RestUtil.ipMatches("10.0.0.0", new ArrayList<String>()));
	}
	
	/**
	 * @see RestUtil#ipMatches(String,List)
	 * @verifies return false if there is no match
	 */
	@Test
	public void ipMatches_shouldReturnFalseIfThereIsNoMatch() throws Exception {
		List<String> candidateIps = new ArrayList<String>();
		candidateIps.add("10.0.0.0");
		candidateIps.add("10.0.0.1");
		
		Assert.assertFalse(RestUtil.ipMatches("10.0.0.2", candidateIps));
	}
	
	/**
	 * @see RestUtil#ipMatches(String,List)
	 * @verifies return true for exact match
	 */
	@Test
	public void ipMatches_shouldReturnTrueForExactMatch() throws Exception {
		List<String> candidateIps = new ArrayList<String>();
		candidateIps.add("10.0.0.0");
		candidateIps.add("10.0.0.1");
		
		Assert.assertTrue(RestUtil.ipMatches("10.0.0.1", candidateIps));
	}
	
	/**
	 * @see RestUtil#ipMatches(String,List)
	 * @verifies return true for match with submask
	 */
	@Test
	public void ipMatches_shouldReturnTrueForMatchWithSubmask() throws Exception {
		List<String> candidateIps = new ArrayList<String>();
		candidateIps.add("10.0.0.0/30");
		
		Assert.assertTrue(RestUtil.ipMatches("10.0.0.1", candidateIps));
	}
	
	/**
	 * @see RestUtil#ipMatches(String,List)
	 * @verifies return false if there is no match with submask
	 */
	@Test
	public void ipMatches_shouldReturnFalseIfThereIsNoMatchWithSubmask() throws Exception {
		List<String> candidateIps = new ArrayList<String>();
		candidateIps.add("10.0.0.0/30");
		
		Assert.assertFalse(RestUtil.ipMatches("10.0.0.4", candidateIps));
	}
	
	/**
	 * @see RestUtil#ipMatches(String,List)
	 * @verifies return true for exact ipv6 match
	 */
	@Test
	public void ipMatches_shouldReturnTrueForExactIpv6Match() throws Exception {
		List<String> candidateIps = new ArrayList<String>();
		candidateIps.add("fe80:0:0:0:202:b3ff:fe1e:8329");
		
		Assert.assertTrue(RestUtil.ipMatches("fe80::202:b3ff:fe1e:8329", candidateIps));
	}
	
	/**
	 * @see RestUtil#ipMatches(String,List)
	 * @verifies throw IllegalArgumentException for invalid mask
	 */
	@Test(expected = IllegalArgumentException.class)
	public void ipMatches_shouldThrowIllegalArgumentExceptionForInvalidMask() throws Exception {
		List<String> candidateIps = new ArrayList<String>();
		candidateIps.add("10.0.0.0/33");
		
		RestUtil.ipMatches("10.0.0.4", candidateIps);
	}
	
	/**
	 * @see RestUtil#getBooleanParam(HttpServletRequest,String)
	 * @verifies return true only if request param is 'true'
	 */
	@Test
	public void getBooleanParam_shouldReturnTrueOnlyIfRequestParamIsTrue() throws Exception {
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		String includeAllParam = RestConstants.REQUEST_PROPERTY_FOR_INCLUDE_ALL;
		
		Assert.assertNull("getBooleanParam should return true if includeAllParam is not set",
		    RestUtil.getBooleanParam(request, includeAllParam));
		
		request.setParameter(includeAllParam, "true");
		Assert.assertTrue("getBooleanParam should return true if includeAllParam is equal 'true'",
		    RestUtil.getBooleanParam(request, includeAllParam));
		
		request.setParameter(includeAllParam, "t");
		Assert.assertFalse("getBooleanParam should return false if includeAllParam is not equal to 'true'",
		    RestUtil.getBooleanParam(request, includeAllParam));
		
		request.setParameter(includeAllParam, (String) null);
		Assert.assertNull("getBooleanParam should return null if includeAllParam is null",
		    RestUtil.getBooleanParam(request, includeAllParam));
	}
	
	/**
	 * @see RestUtil#wrapErrorResponse(Exception,String)
	 * @verifies sets message to the exception message if the reason given is null
	 */
	@Test
	public void wrapErrorResponse_shouldSetExceptionMessageIfReasonIsNull() throws Exception {
		SimpleObject returnObject = RestUtil.wrapErrorResponse(new Exception("exceptionmessage"), null);
		LinkedHashMap errorResponseMap = (LinkedHashMap) returnObject.get("error");
		Assert.assertEquals("[exceptionmessage]", errorResponseMap.get("message"));
	}
	
	/**
	 * @see RestUtil#wrapErrorResponse(Exception,String)
	 * @verifies sets message to the exception message if the reason given is empty
	 */
	@Test
	public void wrapErrorResponse_shouldSetExceptionMessageIfReasonIsEmpty() throws Exception {
		SimpleObject returnObject = RestUtil.wrapErrorResponse(new Exception("exceptionmessage"), "");
		LinkedHashMap errorResponseMap = (LinkedHashMap) returnObject.get("error");
		Assert.assertEquals("[exceptionmessage]", errorResponseMap.get("message"));
	}
	
	/**
	 * @see RestUtil#wrapErrorResponse(Exception,String)
	 * @verifies sets the reason passed into wrapErrorResponse as the message if it is nonempty
	 */
	@Test
	public void wrapErrorResponse_shouldSetReasonAsMessageIfNotEmpty() throws Exception {
		SimpleObject returnObject = RestUtil.wrapErrorResponse(new Exception("exceptionmessage"), "reason");
		LinkedHashMap errorResponseMap = (LinkedHashMap) returnObject.get("error");
		Assert.assertEquals("reason [exceptionmessage]", errorResponseMap.get("message"));
	}
	
	/**
	 * @see RestUtil#wrapErrorResponse(Exception,String)
	 * @verifies set stack trace code if available
	 */
	@Test
	public void wrapErrorResponse_shouldSetStackTraceCodeAndDetailIfAvailable() throws Exception {
		Exception mockException = Mockito.mock(Exception.class);
		Mockito.when(mockException.getMessage()).thenReturn("exceptionmessage");
		StackTraceElement ste = new StackTraceElement("org.mypackage.myclassname", "methodName", "fileName", 149);
		Mockito.when(mockException.getStackTrace()).thenReturn(new StackTraceElement[] { ste });
		
		SimpleObject returnObject = RestUtil.wrapErrorResponse(mockException, "wraperrorresponsemessage");
		
		LinkedHashMap errorResponseMap = (LinkedHashMap) returnObject.get("error");
		Assert.assertEquals("org.mypackage.myclassname:149", errorResponseMap.get("code"));
	}
	
	/**
	 * @see RestUtil#wrapErrorResponse(Exception,String)
	 * @verifies set stack trace code and detail empty if not available
	 */
	@Test
	public void wrapErrorResponse_shouldSetStackTraceCodeAndDetailEmptyIfNotAvailable() throws Exception {
		Exception mockException = Mockito.mock(Exception.class);
		Mockito.when(mockException.getMessage()).thenReturn("exceptionmessage");
		Mockito.when(mockException.getStackTrace()).thenReturn(new StackTraceElement[] {});
		
		SimpleObject returnObject = RestUtil.wrapErrorResponse(mockException, "wraperrorresponsemessage");
		
		LinkedHashMap errorResponseMap = (LinkedHashMap) returnObject.get("error");
		Assert.assertEquals("", errorResponseMap.get("code"));
		Assert.assertEquals("", errorResponseMap.get("detail"));
	}
	
}
