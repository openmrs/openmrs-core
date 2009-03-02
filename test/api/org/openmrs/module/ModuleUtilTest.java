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

import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.test.Verifies;

/**
 * Tests methods on the {@link ModuleUtil} class
 */
public class ModuleUtilTest {
	
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
		String updateRdf = ModuleUtil.getURL(new URL("https://dev.openmrs.org/modules/download/formentry/update.rdf"));
		Assert.assertTrue(updateRdf.contains("<updates"));
	}
	
	/**
	 * This test requires a connection to the internet to pass
	 * 
	 * @see {@link ModuleUtil#getURL(URL)}
	 */
	@Test
	@Verifies(value = "should return an update rdf page for old https module urls", method = "getURL(URL)")
	public void getURL_shouldReturnAnUpdateRdfPageForOldHttpsModuleUrls() throws Exception {
		String updateRdf = ModuleUtil.getURL(new URL("https://modules.openmrs.org/modules/download/formentry/update.rdf"));
		Assert.assertTrue(updateRdf.contains("<updates"));
	}
	
	/**
	 * This test requires a connection to the internet to pass
	 * 
	 * @see {@link ModuleUtil#getURL(URL)}
	 */
	@Test
	@Verifies(value = "should return an update rdf page for module urls", method = "getURL(URL)")
	public void getURL_shouldReturnAnUpdateRdfPageForModuleUrls() throws Exception {
		String updateRdf = ModuleUtil.getURL(new URL("http://modules.openmrs.org/modules/download/formentry/update.rdf"));
		Assert.assertTrue(updateRdf.contains("<updates"));
	}
	
}
