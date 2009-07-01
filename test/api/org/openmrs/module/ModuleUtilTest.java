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
package org.openmrs.module;

import java.net.URL;

import junit.framework.Assert;

import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;

/**
 * Tests methods on the {@link ModuleUtil} class
 */
public class ModuleUtilTest extends BaseContextSensitiveTest {
	
	/**
	 * This test requires a connection to the internet to pass
	 * 
	 * @see {@link ModuleUtil#getURLStream(URL)}
	 */
	@Test
	@Verifies(value = "should return a valid input stream for old module urls", method = "getURLStream(URL)")
	public void getURLStream_shouldReturnAValidInputStreamForOldModuleUrls() throws Exception {
		ModuleUtil.getURLStream(new URL("https://dev.openmrs.org/modules/download/formentry/update.rdf"));
	}
	
	/**
	 * This test requires a connection to the internet to pass
	 * 
	 * @see {@link ModuleUtil#getURL(URL)}
	 */
	@Test
	@Verifies(value = "should return an update rdf page for old https dev urls", method = "getURL(URL)")
	public void getURL_shouldReturnAnUpdateRdfPageForOldHttpsDevUrls() throws Exception {
		String url = "https://dev.openmrs.org/modules/download/formentry/update.rdf";
		String updateRdf = ModuleUtil.getURL(new URL(url));
		Assert.assertTrue("Unable to fetch module update url: " + url, updateRdf.contains("<updates"));
	}
	
	/**
	 * This test requires a connection to the internet to pass
	 * 
	 * @see {@link ModuleUtil#getURL(URL)}
	 */
	@Test
	@Verifies(value = "should return an update rdf page for old https module urls", method = "getURL(URL)")
	public void getURL_shouldReturnAnUpdateRdfPageForOldHttpsModuleUrls() throws Exception {
		String url = "https://modules.openmrs.org/modules/download/formentry/update.rdf";
		String updateRdf = ModuleUtil.getURL(new URL(url));
		Assert.assertTrue("Unable to fetch module update url: " + url, updateRdf.contains("<updates"));
	}
	
	/**
	 * This test requires a connection to the internet to pass
	 * 
	 * @see {@link ModuleUtil#getURL(URL)}
	 */
	@Test
	@Verifies(value = "should return an update rdf page for module urls", method = "getURL(URL)")
	public void getURL_shouldReturnAnUpdateRdfPageForModuleUrls() throws Exception {
		String url = "http://modules.openmrs.org/modules/download/formentry/update.rdf";
		String updateRdf = ModuleUtil.getURL(new URL(url));
		Assert.assertTrue("Unable to fetch module update url: " + url, updateRdf.contains("<updates"));
	}
	
	/**
	 * @see {@link ModuleUtil#checkMandatoryModulesStarted()}
	 */
	@Test(expected = MandatoryModuleException.class)
	@Verifies(value = "should throw ModuleException if a mandatory module is not started", method = "checkMandatoryModulesStarted()")
	public void checkMandatoryModulesStarted_shouldThrowModuleExceptionIfAMandatoryModuleIsNotStarted() throws Exception {
		executeDataSet("org/openmrs/module/include/mandatoryModulesGlobalProperties.xml");
		ModuleUtil.checkMandatoryModulesStarted();
	}
	
	/**
	 * @see {@link ModuleUtil#getMandatoryModules()}
	 */
	@Test
	@Verifies(value = "should return mandatory module ids", method = "getMandatoryModules()")
	public void getMandatoryModules_shouldReturnMandatoryModuleIds() throws Exception {
		executeDataSet("org/openmrs/module/include/mandatoryModulesGlobalProperties.xml");
		Assert.assertEquals(1, ModuleUtil.getMandatoryModules().size());
		Assert.assertEquals("firstmodule", ModuleUtil.getMandatoryModules().get(0));
	}
	
}
