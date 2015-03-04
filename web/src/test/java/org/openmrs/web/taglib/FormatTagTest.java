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
