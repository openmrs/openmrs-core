/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.taglib;

import java.util.Locale;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import org.junit.Assert;
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
import org.springframework.web.util.TagUtils;

/**
 * Contains tests for {@link OpenmrsMessageTag} class
 */
public class OpenmrsMessageTagTest extends BaseWebContextSensitiveTest {

	private static final String TEST_CODE = "test.code";
	private static final String TEST_CODE_TWO_ARGS = "test.code.two_args";
	private static final String TEST_CODE_ONE_ARG = "test.code.one_arg";
		
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
	 * @see OpenmrsMessageTag#doEndTag()
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
	 * @see OpenmrsMessageTag#doEndTag()
	 */
	@Test
	@Verifies(value = "resolve a message of the wrong type fails", method = "doEndTag()")
	public void doEndTag_shouldFailEvaluateRandomObjectMessage() throws Exception {
		final Object message = new Object()
		{
			@Override
			public String toString()
			{
				return "this is *only* a test.";
			}
		};
		
		openmrsMessageTag.setMessage(message);
		
		checkDoEndTagEvaluationException();
	}
	
	@Test
	@Verifies(value = "resolve a Tag with var not set to null", method = "doEndTag()")
	public void doEndTag_shouldEvaluateVarIfIsNotNull() throws Exception {
		final String varName = "Mary";
		final String expectedOutput = "had a little lamb"; 
		openmrsMessageTag.setVar(varName);
		DefaultMessageSourceResolvable message = new DefaultMessageSourceResolvable(new String[] { "test" }, expectedOutput);
		openmrsMessageTag.setMessage(message);
		openmrsMessageTag.setScope(TagUtils.SCOPE_PAGE);
		
		checkDoEndTagEvaluationOfVar(varName, PageContext.PAGE_SCOPE, expectedOutput);
	}

	/**
	 * @see OpenmrsMessageTag#doEndTag()
	 */
	@Test
	@Verifies(value = "javaScript escaping works when activated", method = "doEndTag()")
	public void doEndTag_javaScriptEscapingWorks() throws Exception {
		final String unescapedText = "'Eensy Weensy' spider";
		openmrsMessageTag.setJavaScriptEscape("true");
		final DefaultMessageSourceResolvable message = new DefaultMessageSourceResolvable(new String[] { "test" }, unescapedText);
		openmrsMessageTag.setMessage(message);
		
		checkDoEndTagEvaluation("\\'Eensy Weensy\\' spider");
	}
	
	@Test
	@Verifies(value = "a single argument works", method = "doEndTag()")
	public void doEndTag_singleArgument() throws Exception {
		openmrsMessageTag.setArguments("singleArgument");
		openmrsMessageTag.setCode(TEST_CODE_ONE_ARG);
		openmrsMessageTag.setBodyContent(new MockBodyContent("doesn't matter", new MockHttpServletResponse()));
		
		checkDoEndTagEvaluation("this is a test with arg1: singleArgument");
	}
	
	@Test
	@Verifies(value = "an array of arguments with more than one element works", method = "doEndTag()")
	public void doEndTag_doubleArgument() throws Exception {
		openmrsMessageTag.setArguments("firstArgument,secondArgument");
		openmrsMessageTag.setCode(TEST_CODE_TWO_ARGS);
		openmrsMessageTag.setBodyContent(new MockBodyContent("doesn't matter", new MockHttpServletResponse()));
		
		checkDoEndTagEvaluation("this is a test with arg1: firstArgument, and arg2: secondArgument");
	}
	
	/**
	 * @see OpenmrsMessageTag#doEndTag()
	 */
	@Test
	@Verifies(value = "javaScript escaping is not performed when not activated", method = "doEndTag()")
	public void doEndTag_messageIsNotEscapedWhenEscapingIsOff() throws Exception {
		final String unescapedText = "'Eensy Weensy' spider";
		openmrsMessageTag.setJavaScriptEscape("false");
		final DefaultMessageSourceResolvable message = new DefaultMessageSourceResolvable(new String[] { "test" }, unescapedText);
		openmrsMessageTag.setMessage(message);
		
		checkDoEndTagEvaluation(unescapedText);
	}
	
	/**
	 * @see OpenmrsMessageTag#doEndTag()
	 */
	@Test
	@Verifies(value = "resolve message by code", method = "doEndTag()")
	public void doEndTag_shouldResolveMessageByCode() throws Exception {
		String expectedOutput = "this is a test";
		openmrsMessageTag.setCode(TEST_CODE);
		
		checkDoEndTagEvaluation(expectedOutput);
	}
	
	/**
	 * @see OpenmrsMessageTag#doEndTag()
	 */
	@Test
	@Verifies(value = "resolve message in locale that different from default", method = "doEndTag()")
	public void doEndTag_shouldResolveMessageInLocaleThatDifferentFromDefault() throws Exception {
		
		MockHttpServletRequest request = (MockHttpServletRequest) mockPageContext.getRequest();
		request.addPreferredLocale(Locale.FRENCH);
		
		String expectedOutput = "il s'agit d'un essai";
		openmrsMessageTag.setCode(TEST_CODE);
		
		checkDoEndTagEvaluation(expectedOutput);
	}
	
	/**
	 * @see OpenmrsMessageTag#doEndTag()
	 */
	@Test
	@Verifies(value = "return code if no message resolved", method = "doEndTag()")
	public void doEndTag_shouldReturnCodeIfNoMessageResolved() throws Exception {
		String expectedOutput = "test.wrong.code";
		openmrsMessageTag.setCode("test.wrong.code");
		
		checkDoEndTagEvaluation(expectedOutput);
	}
	
	/**
	 * @see OpenmrsMessageTag#doEndTag()
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
	 * @see OpenmrsMessageTag#doEndTag()
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
	 * @see OpenmrsMessageTag#doEndTag()
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
	 * @see OpenmrsMessageTag#doEndTag()
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
	 * Convenient method that checks that {@link OpenmrsMessageTag#doEndTag()}
	 * throws an exception.
	 */
	private void checkDoEndTagEvaluationException() {
		try {
			openmrsMessageTag.doEndTag();
			
			Assert.assertTrue("doEndTag should have thrown an exception", false);
		}
		catch (Exception e)
		{
			//Do nothing.  Test successful
		}
	}
	
	private void checkDoEndTagEvaluationOfVar(String varName, int scope, String expectedOutput) throws Exception {
		final int tagReturnValue = openmrsMessageTag.doEndTag();
		final String output = (String) mockPageContext.getAttribute(varName, scope);

		Assert.assertEquals("Tag should return 'EVAL_PAGE'", TagSupport.EVAL_PAGE, tagReturnValue);
		Assert.assertEquals(String.format("Variable '%s' should be '%s'", varName, expectedOutput), output, expectedOutput);
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
	public static class SimpleWebApplicationContext extends StaticWebApplicationContext {
		/**
		 * @see org.springframework.context.support.AbstractApplicationContext#refresh()
		 */
		@Override
		public void refresh() throws BeansException {
			addMessage(TEST_CODE, Context.getLocale(), "this is a test");
			addMessage(TEST_CODE, Locale.FRENCH, "il s'agit d'un essai");
			addMessage(TEST_CODE_ONE_ARG, Context.getLocale(), "this is a test with arg1: {0}");
			addMessage(TEST_CODE_TWO_ARGS, Context.getLocale(), "this is a test with arg1: {0}, and arg2: {1}");
			super.refresh();
		}
		
	}
}
