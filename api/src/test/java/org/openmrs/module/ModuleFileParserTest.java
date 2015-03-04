/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

/**
 * Tests ModuleFileParser
 */
public class ModuleFileParserTest {
	
	/**
	 * @verifies parse openmrsVersion and modules
	 * @see ModuleFileParser#getConditionalResources(org.w3c.dom.Element)
	 */
	@Test
	public void getConditionalResources_shouldParseOpenmrsVersionAndModules() throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><module configVersion=\"1.2\">"
		        + "<conditionalResources><conditionalResource>"
		        + "<path>/lib/htmlformentry-api-1.10*</path><openmrsVersion>1.10</openmrsVersion>"
		        + "</conditionalResource><conditionalResource>"
		        + "<path>/lib/metadatasharing-api-1.9*</path><openmrsVersion>1.9</openmrsVersion>"
		        + "<modules><module><moduleId>metadatamapping</moduleId><version>1.0</version></module>"
		        + "<module><moduleId>reporting</moduleId><version>2.0</version></module>"
		        + "</modules></conditionalResource></conditionalResources></module>";
		Element documentElement = getRootElement(xml);
		
		ModuleFileParser moduleFileParser = new ModuleFileParser();
		List<ModuleConditionalResource> conditionalResources = moduleFileParser.getConditionalResources(documentElement);
		
		ModuleConditionalResource htmlformentry = new ModuleConditionalResource();
		htmlformentry.setPath("/lib/htmlformentry-api-1.10*");
		htmlformentry.setOpenmrsVersion("1.10");
		
		ModuleConditionalResource metadatasharing = new ModuleConditionalResource();
		metadatasharing.setPath("/lib/metadatasharing-api-1.9*");
		metadatasharing.setOpenmrsVersion("1.9");
		ModuleConditionalResource.ModuleAndVersion metadatamapping = new ModuleConditionalResource.ModuleAndVersion();
		metadatamapping.setModuleId("metadatamapping");
		metadatamapping.setVersion("1.0");
		ModuleConditionalResource.ModuleAndVersion reporting = new ModuleConditionalResource.ModuleAndVersion();
		reporting.setModuleId("reporting");
		reporting.setVersion("2.0");
		
		metadatasharing.setModules(Arrays.asList(metadatamapping, reporting));
		
		assertThat(conditionalResources, contains(htmlformentry, metadatasharing));
	}
	
	private Element getRootElement(String xml) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document document = db.parse(new ByteArrayInputStream(xml.getBytes()));
		return document.getDocumentElement();
	}
	
	/**
	 * @verifies throw exception if multiple conditionalResources tags found
	 * @see ModuleFileParser#getConditionalResources(org.w3c.dom.Element)
	 */
	@Test(expected = IllegalArgumentException.class)
	public void getConditionalResources_shouldThrowExceptionIfMultipleConditionalResourcesTagsFound() throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><module configVersion=\"1.2\">"
		        + "<conditionalResources></conditionalResources><conditionalResources></conditionalResources></module>";
		Element documentElement = getRootElement(xml);
		
		new ModuleFileParser().getConditionalResources(documentElement);
	}
	
	/**
	 * @verifies throw exception if conditionalResources contains invalid tag
	 * @see ModuleFileParser#getConditionalResources(org.w3c.dom.Element)
	 */
	@Test(expected = IllegalArgumentException.class)
	public void getConditionalResources_shouldThrowExceptionIfConditionalResourcesContainsInvalidTag() throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><module configVersion=\"1.2\">"
		        + "<conditionalResources><invalidTag></invalidTag></conditionalResources></module>";
		Element documentElement = getRootElement(xml);
		
		new ModuleFileParser().getConditionalResources(documentElement);
	}
	
	/**
	 * @verifies throw exception if path is blank
	 * @see ModuleFileParser#getConditionalResources(org.w3c.dom.Element)
	 */
	@Test(expected = IllegalArgumentException.class)
	public void getConditionalResources_shouldThrowExceptionIfPathIsBlank() throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><module configVersion=\"1.2\">"
		        + "<conditionalResources><conditionalResource>" + "<path></path><openmrsVersion>1.10</openmrsVersion>"
		        + "</conditionalResource>></conditionalResources></module>";
		Element documentElement = getRootElement(xml);
		
		new ModuleFileParser().getConditionalResources(documentElement);
	}
	
	/**
	 * @verifies parse conditionalResource with whitespace
	 * @see ModuleFileParser#getConditionalResources(org.w3c.dom.Element)
	 */
	@Test
	public void getConditionalResources_shouldParseConditionalResourceWithWhitespace() throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><module configVersion=\"1.2\">"
		        + "<conditionalResources>     	<conditionalResource>     	"
		        + "<path>/lib/htmlformentry-api-1.10*</path><openmrsVersion>1.10</openmrsVersion>"
		        + "</conditionalResource></conditionalResources></module>";
		Element documentElement = getRootElement(xml);
		
		ModuleFileParser moduleFileParser = new ModuleFileParser();
		List<ModuleConditionalResource> conditionalResources = moduleFileParser.getConditionalResources(documentElement);
		
		ModuleConditionalResource htmlformentry = new ModuleConditionalResource();
		htmlformentry.setPath("/lib/htmlformentry-api-1.10*");
		htmlformentry.setOpenmrsVersion("1.10");
		
		assertThat(conditionalResources, contains(htmlformentry));
	}
}
