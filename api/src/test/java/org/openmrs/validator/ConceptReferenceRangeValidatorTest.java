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

import org.junit.jupiter.api.Test;
import org.openmrs.ConceptReferenceRange;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConceptReferenceRangeValidatorTest extends BaseContextSensitiveTest {

	/**
	 * @see ConceptReferenceRangeValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailIfTheConceptReferenceRangeObjectIsNull() {
		Errors errors = new BindException(new ConceptReferenceRange(), "referenceRange");
		ConceptReferenceRangeValidator validator = new ConceptReferenceRangeValidator();
		assertThrows(IllegalArgumentException.class, () -> validator.validate(null, errors));
	}

	/**
	 * @see ConceptReferenceRangeValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailIfTheCriteriaDoesNotParse() {
		ConceptReferenceRange referenceRange = new ConceptReferenceRange();
		referenceRange.setCriteria("$patient.getAge( >");
		Errors errors = new BindException(referenceRange, "referenceRange");
		new ConceptReferenceRangeValidator().validate(referenceRange, errors);
		assertTrue(errors.hasFieldErrors("criteria"));
	}

	/**
	 * @see ConceptReferenceRangeValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailIfTheCriteriaCallsADisallowedMethod() {
		ConceptReferenceRange referenceRange = new ConceptReferenceRange();
		referenceRange.setCriteria("$patient.setDead(true) || true");
		Errors errors = new BindException(referenceRange, "referenceRange");
		new ConceptReferenceRangeValidator().validate(referenceRange, errors);
		assertTrue(errors.hasFieldErrors("criteria"));
	}

	/**
	 * @see ConceptReferenceRangeValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailIfTheCriteriaAssignsAValue() {
		ConceptReferenceRange referenceRange = new ConceptReferenceRange();
		referenceRange.setCriteria("($patient.dead = true) == true");
		Errors errors = new BindException(referenceRange, "referenceRange");
		new ConceptReferenceRangeValidator().validate(referenceRange, errors);
		assertTrue(errors.hasFieldErrors("criteria"));
	}

	/**
	 * @see ConceptReferenceRangeValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassIfTheCriteriaParses() {
		ConceptReferenceRange referenceRange = new ConceptReferenceRange();
		referenceRange.setCriteria("$patient.getAge() >= 1 && $patient.getAge() <= 70");
		Errors errors = new BindException(referenceRange, "referenceRange");
		new ConceptReferenceRangeValidator().validate(referenceRange, errors);
		assertFalse(errors.hasErrors());
	}

	/**
	 * @see ConceptReferenceRangeValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassIfTheCriteriaIsHtmlEscapedAndParsesOnceUnescaped() {
		ConceptReferenceRange referenceRange = new ConceptReferenceRange();
		referenceRange.setCriteria("$patient.getAge() &gt;= 1 &amp;&amp; $patient.getAge() &lt;= 70");
		Errors errors = new BindException(referenceRange, "referenceRange");
		new ConceptReferenceRangeValidator().validate(referenceRange, errors);
		assertFalse(errors.hasErrors());
	}

	/**
	 * @see ConceptReferenceRangeValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassIfTheCriteriaIsNull() {
		ConceptReferenceRange referenceRange = new ConceptReferenceRange();
		Errors errors = new BindException(referenceRange, "referenceRange");
		new ConceptReferenceRangeValidator().validate(referenceRange, errors);
		assertFalse(errors.hasErrors());
	}
}
