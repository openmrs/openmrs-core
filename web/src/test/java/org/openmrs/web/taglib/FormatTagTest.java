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

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.ConceptNameTag;
import org.openmrs.api.ConceptNameType;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;

public class FormatTagTest extends BaseContextSensitiveTest {
	
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
}
