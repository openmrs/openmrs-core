/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.servlet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openmrs.Concept;
import org.openmrs.api.ConceptService;
import org.openmrs.api.db.ConceptDAO;
import org.openmrs.web.test.BaseWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Tests the {@link DownloadDictionaryServlet}. Since the test dataset for concepts is rather large,
 * this test uses the ConceptDAO to load specific examples to test the formatting. ConceptDAO is
 * used to load the concepts rather than the ConceptService because the ConceptService needs to be
 * mocked to return only the concepts under test.
 */
public class DownloadDictionaryServletTest extends BaseWebContextSensitiveTest {
	
	private static final String EXPECTED_HEADER = "Concept Id,Name,Description,Synonyms,Answers,Set Members,Class,Datatype,Changed By,Creator\n";
	
	@Mock
	private ConceptService conceptService;
	
	@Resource(name = "conceptDAO")
	private ConceptDAO conceptDAO;
	
	@Test
	public void shouldPrintHeaderAndFormattedConceptLines() throws Exception {
		String actualContent = runServletWithConcepts(conceptDAO.getConcept(3), conceptDAO.getConcept(5), conceptDAO
		        .getConcept(6));
		String expectedContent = EXPECTED_HEADER
		        + "3,\"COUGH SYRUP\",\"This is used for coughs\",\"COUGH SYRUP\",\"\",\"\",\"Drug\",\"N/A\",\"\",\"Super User\"\n"
		        + "5,\"SINGLE\",\"\",\"SINGLE\",\"\",\"\",\"Misc\",\"N/A\",\"\",\"Super User\"\n"
		        + "6,\"MARRIED\",\"\",\"MARRIED\",\"\",\"\",\"Misc\",\"N/A\",\"\",\"Super User\"\n";
		Assert.assertEquals(expectedContent, actualContent);
	}
	
	@Test
	public void shouldFormatMultipleAnswersWithLineBreaks() throws Exception {
		String actualContent = runServletWithConcepts(conceptDAO.getConcept(4));
		String expectedStart = EXPECTED_HEADER
		        + "4,\"CIVIL STATUS\",\"What is the person's marital state\",\"CIVIL STATUS\",";
		String expectedEnd = ",\"\",\"ConvSet\",\"Coded\",\"Super User\",\"Super User\"\n";
		String[] expectedLineBreakSections = { "SINGLE", "MARRIED" };
		
		assertContent(expectedStart, expectedEnd, expectedLineBreakSections, actualContent);
	}
	
	@Test
	public void shouldFormatMultipleSynonymsWithLineBreaks() throws Exception {
		String actualContent = runServletWithConcepts(conceptDAO.getConcept(792));
		String expectedStart = EXPECTED_HEADER
		        + "792,\"STAVUDINE LAMIVUDINE AND NEVIRAPINE\",\"Combination antiretroviral drug.\",";
		String expectedEnd = ",\"\",\"\",\"Drug\",\"N/A\",\"Super User\",\"Super User\"\n";
		String[] expectedLineBreakSections = { "STAVUDINE LAMIVUDINE AND NEVIRAPINE", "D4T+3TC+NVP", "TRIOMUNE-30",
		        "D4T+3TC+NVP" };
		
		assertContent(expectedStart, expectedEnd, expectedLineBreakSections, actualContent);
	}
	
	@Test
	public void shouldFormatMultipleSetMembersWithLineBreaks() throws Exception {
		String actualContent = runServletWithConcepts(conceptDAO.getConcept(23));
		String expectedStart = EXPECTED_HEADER
		        + "23,\"FOOD CONSTRUCT\",\"Holder for all things edible\",\"FOOD CONSTRUCT\",\"\",";
		String expectedEnd = ",\"ConvSet\",\"N/A\",\"\",\"Super User\"\n";
		String[] expectedLineBreakSections = { "FOOD ASSISTANCE", "DATE OF FOOD ASSISTANCE", "FAVORITE FOOD, NON-CODED" };
		
		assertContent(expectedStart, expectedEnd, expectedLineBreakSections, actualContent);
	}
	
	@Test
	public void shouldPrintColumnsWithEmptyQuotesForNullFields() throws Exception {
		String actualContent = runServletWithConcepts(new Concept(1));
		String expectedContent = EXPECTED_HEADER + "1,\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"\n";
		Assert.assertEquals(expectedContent, actualContent);
	}
	
	private String runServletWithConcepts(Concept... concepts) throws Exception {
		DownloadDictionaryServlet downloadServlet = new DownloadDictionaryServlet();
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/downloadDictionary.csv");
		request.setContextPath("/somecontextpath");
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		List<Concept> conceptList = Arrays.asList(concepts);
		Mockito.when(conceptService.conceptIterator()).thenReturn(conceptList.iterator());
		
		downloadServlet.service(request, response);
		return response.getContentAsString();
	}
	
	private void assertContent(String expectedStart, String expectedEnd, String[] expectedLineSections, String actualContent) {
		Assert.assertTrue(actualContent.startsWith(expectedStart));
		Assert.assertTrue(actualContent.endsWith(expectedEnd));
		
		// The content with line breaks can come in any order so test for the content flexibly
		String lineBreakContent = actualContent.substring(expectedStart.length(), actualContent.length()
		        - expectedEnd.length());
		
		// Should start and end with "
		Assert.assertTrue(lineBreakContent.startsWith("\""));
		Assert.assertTrue(lineBreakContent.endsWith("\""));
		
		lineBreakContent = lineBreakContent.replace("\"", "");
		List<String> actualLineBreakSections = Arrays.asList(lineBreakContent.split("\n"));
		
		Assert.assertEquals(expectedLineSections.length, actualLineBreakSections.size());
		for (String expectedSection : expectedLineSections) {
			Assert.assertTrue(actualLineBreakSections.contains(expectedSection));
		}
	}
}
