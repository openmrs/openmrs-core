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
import org.openmrs.ConceptMapType;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Contains tests methods for the {@link ConceptMapTypeValidator}
 */
public class ConceptMapTypeValidatorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see {@link ConceptMapTypeValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if the concept map type name is a duplicate", method = "validate(Object,Errors)")
	public void validate_shouldFailIfTheConceptMapTypeNameIsADuplicate() throws Exception {
		ConceptMapType mapType = new ConceptMapType();
		mapType.setName("is a");
		Errors errors = new BindException(mapType, "mapType");
		new ConceptMapTypeValidator().validate(mapType, errors);
		Assert.assertEquals(true, errors.hasFieldErrors("name"));
	}
	
	/**
	 * @see {@link ConceptMapTypeValidator#validate(Object,Errors)}
	 */
	@Test(expected = IllegalArgumentException.class)
	@Verifies(value = "should fail if the concept map type object is null", method = "validate(Object,Errors)")
	public void validate_shouldFailIfTheConceptMapTypeObjectIsNull() throws Exception {
		Errors errors = new BindException(new ConceptMapType(), "mapType");
		new ConceptMapTypeValidator().validate(null, errors);
	}
	
	/**
	 * @see {@link ConceptMapTypeValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if the name is a white space character", method = "validate(Object,Errors)")
	public void validate_shouldFailIfTheNameIsAWhiteSpaceCharacter() throws Exception {
		ConceptMapType mapType = new ConceptMapType();
		mapType.setName(" ");
		Errors errors = new BindException(mapType, "mapType");
		new ConceptMapTypeValidator().validate(mapType, errors);
		Assert.assertEquals(true, errors.hasFieldErrors("name"));
	}
	
	/**
	 * @see {@link ConceptMapTypeValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if the name is an empty string", method = "validate(Object,Errors)")
	public void validate_shouldFailIfTheNameIsAnEmptyString() throws Exception {
		ConceptMapType mapType = new ConceptMapType();
		mapType.setName("");
		Errors errors = new BindException(mapType, "mapType");
		new ConceptMapTypeValidator().validate(mapType, errors);
		Assert.assertEquals(true, errors.hasFieldErrors("name"));
	}
	
	/**
	 * @see {@link ConceptMapTypeValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if the name is null", method = "validate(Object,Errors)")
	public void validate_shouldFailIfTheNameIsNull() throws Exception {
		ConceptMapType mapType = new ConceptMapType();
		Errors errors = new BindException(mapType, "mapType");
		new ConceptMapTypeValidator().validate(mapType, errors);
		Assert.assertEquals(true, errors.hasFieldErrors("name"));
	}
	
	/**
	 * @see {@link ConceptMapTypeValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "pass if the name is unique amongst all concept map type names", method = "validate(Object,Errors)")
	public void validate_shouldPassIfTheNameIsUniqueAmongstAllConceptMapTypeNames() throws Exception {
		ConceptMapType mapType = new ConceptMapType();
		mapType.setName("unique-name");
		Errors errors = new BindException(mapType, "mapType");
		new ConceptMapTypeValidator().validate(mapType, errors);
		Assert.assertEquals(false, errors.hasErrors());
	}
	
	/**
	 * @see {@link ConceptMapTypeValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass validation if field lengths are correct", method = "validate(Object,Errors)")
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() throws Exception {
		ConceptMapType mapType = new ConceptMapType();
		mapType.setName("unique-name");
		mapType.setDescription("Description");
		mapType.setRetireReason("RetireReason");
		Errors errors = new BindException(mapType, "mapType");
		new ConceptMapTypeValidator().validate(mapType, errors);
		Assert.assertEquals(false, errors.hasErrors());
	}
	
	/**
	 * @see {@link ConceptMapTypeValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if field lengths are not correct", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() throws Exception {
		ConceptMapType mapType = new ConceptMapType();
		mapType.setName("unique-name");
		mapType
		        .setDescription("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		mapType
		        .setRetireReason("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		Errors errors = new BindException(mapType, "mapType");
		new ConceptMapTypeValidator().validate(mapType, errors);
		Assert.assertEquals(true, errors.hasFieldErrors("description"));
		Assert.assertEquals(true, errors.hasFieldErrors("retireReason"));
	}
}
