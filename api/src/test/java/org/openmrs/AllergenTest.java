/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.util.OpenmrsConstants;

/**
 * Tests methods in {@link org.openmrs.Allergen}.
 */
public class AllergenTest extends BaseContextSensitiveTest {
	
	private static final String ALLERGY_OTHER_NONCODED_TEST_DATASET = "org/openmrs/api/include/otherNonCodedConcept.xml";
	
	Allergen allergen;
	
	@Before
	public void setup() {
		executeDataSet(ALLERGY_OTHER_NONCODED_TEST_DATASET);
		Allergen.setOtherNonCodedConceptUuid(Context.getAdministrationService().getGlobalProperty(
			    OpenmrsConstants.GP_ALLERGEN_OTHER_NON_CODED_UUID));
	}
	
	@Test
	public void shouldEitherBeCodedOrFreeText() {
		allergen = new Allergen(AllergenType.DRUG, new Concept(3), null);
		assertCoded();
		
		allergen.setNonCodedAllergen("Non coded allergen");
		assertNonCoded();
		
		allergen.setCodedAllergen(new Concept(3));
		assertCoded();
		
		allergen = new Allergen(AllergenType.DRUG, null, "Non coded allergen");
		assertNonCoded();
	}
	
	private void assertCoded() {
		Assert.assertNotEquals(allergen.getCodedAllergen().getUuid(), Allergen.getOtherNonCodedConceptUuid());
		Assert.assertNull(allergen.getNonCodedAllergen());
		Assert.assertTrue(allergen.isCoded());
	}
	
	private void assertNonCoded() {
		Assert.assertNull(allergen.getCodedAllergen());
		Assert.assertEquals(allergen.getNonCodedAllergen(), "Non coded allergen");
		Assert.assertFalse(allergen.isCoded());
	}
	
	@Test
	public void isSameAllergen_shouldReturnTrueForSameCodedAllergen() {
        Concept c = new Concept();
		Assert.assertTrue(new Allergen(null, c, null).isSameAllergen(new Allergen(null, c, null)));
	}
	
	@Test
	public void isSameAllergen_shouldReturnFalseForDifferentCodedAllergen() {
		Assert.assertFalse(new Allergen(null, new Concept(1), null).isSameAllergen(new Allergen(null, new Concept(2), null)));
	}
	
	@Test
	public void isSameAllergen_shouldReturnTrueForSameNonCodedAllergen() {
		Concept concept = Context.getConceptService().getConceptByUuid(Allergen.getOtherNonCodedConceptUuid());
		Assert.assertTrue(new Allergen(null, concept, "OTHER VALUE").isSameAllergen(new Allergen(null, concept, "OTHER VALUE")));
	}
	
	@Test
	public void isSameAllergen_shouldBeCaseInsensitiveForNonCodedAllergen() {
		Concept concept = Context.getConceptService().getConceptByUuid(Allergen.getOtherNonCodedConceptUuid());
		Assert.assertTrue(new Allergen(null, concept, "other value").isSameAllergen(new Allergen(null, concept, "OTHER VALUE")));
	}
	
	@Test
	public void isSameAllergen_shouldReturnFalseForDifferentNonCodedAllergen() {
		Concept concept = Context.getConceptService().getConceptByUuid(Allergen.getOtherNonCodedConceptUuid());
		Assert.assertFalse(new Allergen(null, concept, "OTHER VALUE1").isSameAllergen(new Allergen(null, concept, "OTHER VALUE2")));
	}
}
