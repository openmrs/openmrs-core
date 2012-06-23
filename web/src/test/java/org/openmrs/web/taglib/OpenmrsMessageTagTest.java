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
package org.openmrs.web.taglib;

import java.util.Locale;

import javax.servlet.jsp.tagext.TagSupport;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.test.Verifies;
import org.openmrs.web.test.BaseWebContextSensitiveTest;
import org.springframework.beans.BeansException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.mock.web.MockBodyContent;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockPageContext;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.StaticWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * Contains tests for {@link OpenmrsMessageTag} class
 */
public class OpenmrsMessageTagTest extends BaseWebContextSensitiveTest {
	
	private OpenmrsMessageTag openmrsMessageTag;
	
	private MockPageContext mockPageContext;
	
	@Before
	public void createMockPageContext() throws Exception {
		
		MockServletContext sc = new MockServletContext();
		SimpleWebApplicationContext wac = new SimpleWebApplicationContext();
		wac.setServletContext(sc);
		wac.setNamespace("test");
		wac.refresh();
		
		MockHttpServletRequest request = new MockHttpServletRequest(sc);
		request.addPreferredLocale(Context.getLocale());
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		request.setAttribute(DispatcherServlet.WEB_APPLICATION_CONTEXT_ATTRIBUTE, wac);
		
		openmrsMessageTag = new OpenmrsMessageTag();
		mockPageContext = new MockPageContext(sc, request, response);
		openmrsMessageTag.setPageContext(mockPageContext);
	}
	
	/**
	 * @see {@link OpenmrsMessageTag#doEndTag()}
	 */
	@Test
	@Verifies(value = "evaluate specified message resolvable", method = "doEndTag()")
	public void doEndTag_shouldEvaluateSpecifiedMessageResolvable() throws Exception {
		String expectedOutput = "this is a test";
		DefaultMessageSourceResolvable message = new DefaultMessageSourceResolvable(new String[] { "test" }, expectedOutput);
		openmrsMessageTag.setMessage(message);
		
		checkDoEndTagEvaluation(expectedOutput);
	}
	
	/**
	 * @see {@link OpenmrsMessageTag#doEndTag()}
	 */
	@Test
	@Verifies(value = "resolve message by code", method = "doEndTag()")
	public void doEndTag_shouldResolveMessageByCode() throws Exception {
		String expectedOutput = "this is a test";
		openmrsMessageTag.setCode("test.code");
		
		checkDoEndTagEvaluation(expectedOutput);
	}
	
	/**
	 * @see {@link OpenmrsMessageTag#doEndTag()}
	 */
	@Test
	@Verifies(value = "resolve message in locale that different from default", method = "doEndTag()")
	public void doEndTag_shouldResolveMessageInLocaleThatDifferentFromDefault() throws Exception {
		
		MockHttpServletRequest request = (MockHttpServletRequest) mockPageContext.getRequest();
		request.addPreferredLocale(Locale.FRENCH);
		
		String expectedOutput = "il s'agit d'un essai";
		openmrsMessageTag.setCode("test.code");
		
		checkDoEndTagEvaluation(expectedOutput);
	}
	
	/**
	 * @see {@link OpenmrsMessageTag#doEndTag()}
	 */
	@Test
	@Verifies(value = "return code if no message resolved", method = "doEndTag()")
	public void doEndTag_shouldReturnCodeIfNoMessageResolved() throws Exception {
		String expectedOutput = "test.wrong.code";
		openmrsMessageTag.setCode("test.wrong.code");
		
		checkDoEndTagEvaluation(expectedOutput);
	}
	
	/**
	 * @see {@link OpenmrsMessageTag#doEndTag()}
	 */
	@Test
	@Verifies(value = "use body content as fallback if no message resolved", method = "doEndTag()")
	public void doEndTag_shouldUseBodyContentAsFallbackIfNoMessageResolved() throws Exception {
		String expectedOutput = "test body content";
		openmrsMessageTag.setBodyContent(new MockBodyContent(expectedOutput, new MockHttpServletResponse()));
		openmrsMessageTag.setCode("test.wrong.code");
		
		checkDoEndTagEvaluation(expectedOutput);
	}
	
	/**
	 * @see {@link OpenmrsMessageTag#doEndTag()}
	 */
	@Test
	@Verifies(value = "use text attribute as fallback if no message resolved", method = "doEndTag()")
	public void doEndTag_shouldUseTextAttributeAsFallbackIfNoMessageResolved() throws Exception {
		String expectedOutput = "test content";
		openmrsMessageTag.setText(expectedOutput);
		openmrsMessageTag.setCode("test.wrong.code");
		
		checkDoEndTagEvaluation(expectedOutput);
	}
	
	/**
	 * @see {@link OpenmrsMessageTag#doEndTag()}
	 */
	@Test
	@Verifies(value = "use body content in prior to text attribute as fallback if no message resolved", method = "doEndTag()")
	public void doEndTag_shouldUseBodyContentInPriorToTextAttributeAsFallbackIfNoMessageResolved() throws Exception {
		String expectedOutput = "test body content";
		openmrsMessageTag.setBodyContent(new MockBodyContent(expectedOutput, new MockHttpServletResponse()));
		openmrsMessageTag.setText("test content");
		openmrsMessageTag.setCode("test.wrong.code");
		
		checkDoEndTagEvaluation(expectedOutput);
	}
	
	/**
	 * @see {@link OpenmrsMessageTag#doEndTag()}
	 */
	@Test
	@Verifies(value = "ignore fallbacks if tag locale differs from context locale", method = "doEndTag()")
	public void doEndTag_shouldIgnoreFallbacksIfTagLocaleDiffersFromContextLocale() throws Exception {
		String expectedOutput = "test.wrong.code";
		openmrsMessageTag.setBodyContent(new MockBodyContent(expectedOutput, new MockHttpServletResponse()));
		openmrsMessageTag.setText("test content");
		openmrsMessageTag.setCode(expectedOutput);
		openmrsMessageTag.setLocale("uk");
		
		checkDoEndTagEvaluation(expectedOutput);
	}
	
	/**
	 * Convenient method that checks results of evaluation of {@link OpenmrsMessageTag#doEndTag()}
	 * method
	 * 
	 * @param expectedOutput the expected output of tag evaluation
	 * @throws Exception if any evaluation error occurs
	 */
	private void checkDoEndTagEvaluation(String expectedOutput) throws Exception {
		int tagReturnValue = openmrsMessageTag.doEndTag();
		String output = ((MockHttpServletResponse) mockPageContext.getResponse()).getContentAsString();
		
		Assert.assertEquals("Tag should return 'EVAL_PAGE'", TagSupport.EVAL_PAGE, tagReturnValue);
		Assert.assertEquals(String.format("Output should be '%s'", expectedOutput), expectedOutput, output);
	}
	
	/**
	 * Mock web application context to be used in tests as messages source
	 */
	public class SimpleWebApplicationContext extends StaticWebApplicationContext {
		
		/**
		 * @see org.springframework.context.support.AbstractApplicationContext#refresh()
		 */
		@Override
		public void refresh() throws BeansException {
			addMessage("test.code", Context.getLocale(), "this is a test");
			addMessage("test.code", Locale.FRENCH, "il s'agit d'un essai");
			super.refresh();
		}
		
	}
}
