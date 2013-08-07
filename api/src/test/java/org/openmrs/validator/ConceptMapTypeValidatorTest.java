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
	
}
