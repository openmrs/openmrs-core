/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.validator;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Drug;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link ConceptDrugValidator} class.
 */
public class ConceptDrugValidatorTest {
	
	/**
	 * @see ConceptDrugValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailIfAConceptIsNotSpecified() {
		Drug drug = new Drug();
		Errors errors = new BindException(drug, "drug");
		new ConceptDrugValidator().validate(drug, errors);
		Assert.assertTrue(errors.hasErrors());
	}
	
	/**
	 * @see ConceptDrugValidator#supports(Class)
	 */
	@Test
	public void supports_shouldRejectClassesNotExtendingDrug() {
		Assert.assertFalse(new ConceptDrugValidator().supports(String.class));
	}
	
	/**
	 * @see ConceptDrugValidator#supports(Class)
	 */
	@Test
	public void supports_shouldSupportDrug() {
		Assert.assertTrue(new ConceptDrugValidator().supports(Drug.class));
	}
	
}
