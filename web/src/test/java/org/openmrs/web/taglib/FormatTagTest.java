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
import javax.servlet.jsp.tagext.Tag;

import org.junit.Assert;
import org.junit.Test;
import org.hamcrest.Matchers;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.ConceptNameTag;
import org.openmrs.api.ConceptNameType;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.springframework.mock.web.MockPageContext;

public class FormatTagTest extends BaseContextSensitiveTest {
	
	private static final String ATTRIBUTE_OBJECT_VALUE = "objectValue";
	
	/**
	 * @see FormatTag#printConcept(StringBuilder,Concept)
	 * @verifies print the name with the correct name, and type
	 */
	@Test
	public void printConcept_shouldPrintTheNameWithTheCorrectLocaleNameAndType() throws Exception {
		ConceptService service = Context.getConceptService();
		Locale locale = Context.getLocale();
		ConceptNameTag tag = service.getConceptNameTag(5);
		ConceptNameTag anotherTag = service.getConceptNameTag(6);
		Context.flushSession();
		
		Concept c = new Concept();
		c.addName(buildName("English fully specified", locale, true, ConceptNameType.FULLY_SPECIFIED, null));
		c.addName(buildName("English synonym", locale, false, null, null));
		c.addName(buildName("English tag", locale, false, null, tag));
		c.addName(buildName("English another tag", locale, false, null, anotherTag));
		c.setDatatype(service.getConceptDatatype(1));
		c.setConceptClass(service.getConceptClass(1));
		
		Context.getConceptService().saveConcept(c);
		
		assertPrintConcept("English fully specified", c, null, null);
		assertPrintConcept("English fully specified", c, ConceptNameType.FULLY_SPECIFIED.toString(), null);
		assertPrintConcept("English tag", c, null, tag.getTag());
	}
	
	/**
	 * @param expected
	 * @param concept
	 * @param withType
	 * @param withTag
	 */
	private void assertPrintConcept(String expected, Concept concept, String withType, String withTag) {
		FormatTag format = new FormatTag();
		format.setWithConceptNameType(withType);
		format.setWithConceptNameTag(withTag);
		StringBuilder sb = new StringBuilder();
		format.printConcept(sb, concept);
		Assert.assertEquals(expected, sb.toString());
	}
	
	/**
	 * @see FormatTag#printConcept(StringBuilder,Concept)
	 * @verifies escape html tags
	 */
	@Test
	public void printConcept_shouldEscapeHtmlTags() throws Exception {
		ConceptService service = Context.getConceptService();
		Locale locale = Context.getLocale();
		ConceptNameTag tag = service.getConceptNameTag(5);
		ConceptNameTag anotherTag = service.getConceptNameTag(6);
		Context.flushSession();
		
		Concept c = new Concept();
		c.addName(buildName("English fully\"><script>alert('xss possible!')</script> specified", locale, true,
		    ConceptNameType.FULLY_SPECIFIED, null));
		c.setDatatype(service.getConceptDatatype(1));
		c.setConceptClass(service.getConceptClass(1));
		
		Context.getConceptService().saveConcept(c);
		FormatTag format = new FormatTag();
		format.setWithConceptNameType(ConceptNameType.FULLY_SPECIFIED.toString());
		format.setWithConceptNameTag(null);
		StringBuilder sb = new StringBuilder();
		format.printConcept(sb, c);
		
		Assert.assertThat(sb.toString(), Matchers.not(Matchers.containsString("<script>")));
	}
	
	/**
	 * @param name
	 * @param locale
	 * @param localePreferred
	 * @param nameType
	 * @param tag
	 * @return
	 */
	private ConceptName buildName(String name, Locale locale, boolean localePreferred, ConceptNameType nameType,
	        ConceptNameTag tag) {
		ConceptName ret = new ConceptName();
		ret.setName(name);
		ret.setLocale(locale);
		ret.setLocalePreferred(localePreferred);
		ret.setConceptNameType(nameType);
		if (tag != null)
			ret.addTag(tag);
		return ret;
	}
	
	/**
	 * @see {@link FormatTag#doStartTag()}
	 */
	@Test
	@Verifies(value = "print any domain object", method = "doStartTag()")
	public void doStartTag_shouldPrintAnyDomainObject() throws Exception {
		FormatTag tag = new FormatTag();
		PageContext pageContext = new MockPageContext();
		tag.setPageContext(pageContext);
		tag.setVar(ATTRIBUTE_OBJECT_VALUE);
		
		// check if concept is properly printed
		checkStartTagEvaluation(pageContext, tag, Context.getConceptService().getConcept(3), "COUGH SYRUP");
		
		// check if encounter is properly printed
		checkStartTagEvaluation(pageContext, tag, Context.getEncounterService().getEncounter(3),
		    "Emergency @Unknown Location | 01/08/2008 | Super User");
		
		// check if observation value is properly printed
		checkStartTagEvaluation(pageContext, tag, Context.getObsService().getObs(7), "50.0");
		
		// check if user is properly printed
		checkStartTagEvaluation(pageContext, tag, Context.getUserService().getUser(502),
		    "<span class=\"user\"><span class=\"username\">butch</span><span class=\"personName\"> (Hippocrates of Cos)</span></span>");
		
		// check if encounter type is properly printed
		checkStartTagEvaluation(pageContext, tag, Context.getEncounterService().getEncounterType(1), "Scheduled");
		
		// check if location is properly printed
		checkStartTagEvaluation(pageContext, tag, Context.getLocationService().getLocation(1), "Unknown Location");
		
		// check if program is properly printed
		checkStartTagEvaluation(pageContext, tag, Context.getProgramWorkflowService().getProgram(1), "HIV PROGRAM");
		
		// check if visit is properly printed
		checkStartTagEvaluation(pageContext, tag, Context.getVisitService().getVisit(1),
		    "Initial HIV Clinic Visit @Unknown Location | 01/01/2005");
		
		// check if visit type is properly printed
		checkStartTagEvaluation(pageContext, tag, Context.getVisitService().getVisitType(1), "Initial HIV Clinic Visit");
		
		// check if form is properly printed
		checkStartTagEvaluation(pageContext, tag, Context.getFormService().getForm(1), "Basic Form (v0.1)");
	}
	
	/**
	 * This method checks correctness of start tag evaluation of given tag
	 * 
	 * @param pageContext the page context to be used when checking start tag evaluation
	 * @param tag the format tag whose doStartTag() method will be evaluated
	 * @param object the object to format with given tag
	 * @param expected the expected result of object formatting 
	 */
	private void checkStartTagEvaluation(PageContext pageContext, FormatTag tag, Object object, String expected) {
		tag.setObject(object);
		Assert.assertEquals(Tag.SKIP_BODY, tag.doStartTag());
		Assert.assertNotNull(pageContext.getAttribute(ATTRIBUTE_OBJECT_VALUE));
		Assert.assertEquals(expected, pageContext.getAttribute(ATTRIBUTE_OBJECT_VALUE));
	}
}
