/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.web;

import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.Module;
import org.openmrs.module.web.WebModuleUtilTest;
import org.openmrs.test.Verifies;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * Contains tests for the functionality of the {@link OpenmrsRequestToViewNameTranslator}
 */
public class OpenmrsRequestToViewNameTranslatorTest {
	
	private MockHttpServletRequest request;
	
	@Before
	public void before() {
		request = new MockHttpServletRequest();
		request.setContextPath("/openmrs");
	}
	
	/**
	 * @see {@link OpenmrsRequestToViewNameTranslator#getViewName(HttpServletRequest)}
	 */
	@Test
	@Verifies(value = "should return the correct view name for a module url using a module id", method = "getViewName(HttpServletRequest)")
	public void getViewName_shouldReturnTheCorrectViewNameForAModuleUrlUsingAModuleId() throws Exception {
		Module module = WebModuleUtilTest.addTestModule("useless module", "useless.demo", "org.openmrs.module.");
		request.setRequestURI(request.getContextPath() + "/module/" + module.getModuleId() + "/manage.htm");
		Assert.assertEquals("module/" + module.getModulePackageAsPath() + "/manage",
		    new OpenmrsRequestToViewNameTranslator().getViewName(request));
	}
	
	/**
	 * @see {@link OpenmrsRequestToViewNameTranslator#getViewName(HttpServletRequest)}
	 */
	@Test
	@Verifies(value = "should return the correct view name for a module url using a package name", method = "getViewName(HttpServletRequest)")
	public void getViewName_shouldReturnTheCorrectViewNameForAModuleUrlUsingAPackageName() throws Exception {
		Module module = WebModuleUtilTest.addTestModule("useless module", "useless.demo", "org.openmrs.module.");
		request.setRequestURI(request.getContextPath() + "/module/" + module.getPackageName() + "/manage.htm");
		Assert.assertEquals("module/" + module.getModulePackageAsPath() + "/manage",
		    new OpenmrsRequestToViewNameTranslator().getViewName(request));
	}
	
	/**
	 * @see {@link OpenmrsRequestToViewNameTranslator#getViewName(HttpServletRequest)}
	 */
	@Test
	@Verifies(value = "should ignore a url in core", method = "getViewName(HttpServletRequest)")
	public void getViewName_shouldIgnoreAUrlInCore() throws Exception {
		Module module = WebModuleUtilTest.addTestModule("useless module", "useless.demo", "org.openmrs.module.");
		request.setRequestURI(request.getContextPath() + "/admin/" + module.getPackageName() + "/manage.htm");
		Assert.assertEquals("admin/" + module.getPackageName() + "/manage", new OpenmrsRequestToViewNameTranslator()
		        .getViewName(request));
	}
}
