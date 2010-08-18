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
package org.openmrs;

import java.util.Locale;

import junit.framework.Assert;

import org.databene.benerator.Generator;
import org.databene.benerator.factory.GeneratorFactory;
import org.junit.Test;
import org.openmrs.api.ConceptNameType;
import org.openmrs.api.context.Context;
import org.openmrs.test.Verifies;

/**
 * Behavior-driven tests of the ConceptName class.
 */
public class ConceptNameTest {
	
	final static String NAME_PATTERN = "[a-z]*";
	
	final static Generator<String> nameGenerator;
	
	static {
		nameGenerator = GeneratorFactory.getUniqueRegexStringGenerator(NAME_PATTERN, 2, 12, Locale.ENGLISH);
	}
	
	/**
	 * Convenient factory method to create a populated Concept name.
	 * 
	 * @param conceptNameId id for the conceptName
	 * @param locale for the conceptName
	 * @param conceptNameType the conceptNameType of the concept
	 * @param isLocalePreferred if this name should be marked as preferred in its locale
	 */
	public static ConceptName createMockConceptName(int conceptNameId, Locale locale, ConceptNameType conceptNameType,
	                                                Boolean isLocalePreferred) {
		ConceptName mockConceptName = new ConceptName();
		
		mockConceptName.setConceptNameId(conceptNameId);
		if (locale == null)
			mockConceptName.setLocale(Context.getLocale());
		else
			mockConceptName.setLocale(locale);
		mockConceptName.setConceptNameType(conceptNameType);
		mockConceptName.setLocalePreferred(isLocalePreferred);
		mockConceptName.setName(nameGenerator.generate());
		
		return mockConceptName;
	}
	
	/**
	 * @see {@link ConceptName#equals(Object)}
	 */
	@Test
	@Verifies(value = "should compare on conceptNameId if non null", method = "equals(Object)")
	public void equals_shouldCompareOnConceptNameIdIfNonNull() throws Exception {
		ConceptName firstName = new ConceptName(1);
		ConceptName secondName = new ConceptName(1);
		Assert.assertTrue(firstName.equals(secondName));
	}
	
	/**
	 * @see {@link ConceptName#equals(Object)}
	 */
	@Test
	@Verifies(value = "should not return true with different objects and null ids", method = "equals(Object)")
	public void equals_shouldNotReturnTrueWithDifferentObjectsAndNullIds() throws Exception {
		ConceptName firstName = new ConceptName();
		ConceptName secondName = new ConceptName();
		Assert.assertFalse(firstName.equals(secondName));
	}
	
	/**
	 * @see {@link ConceptName#equals(Object)}
	 */
	@Test
	@Verifies(value = "should default to object equality", method = "equals(Object)")
	public void equals_shouldDefaultToObjectEquality() throws Exception {
		ConceptName firstName = new ConceptName();
		Assert.assertTrue(firstName.equals(firstName));
	}
}
