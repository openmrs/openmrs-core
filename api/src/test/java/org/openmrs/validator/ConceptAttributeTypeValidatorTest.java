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
import org.junit.Before;
import org.junit.Test;
import org.openmrs.ConceptAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link ConceptAttributeTypeValidator} class.
 */
public class ConceptAttributeTypeValidatorTest extends BaseContextSensitiveTest {

	protected static final String CONCEPT_ATTRIBUTE_TYPE_XML = "org/openmrs/api/include/ConceptServiceTest-conceptAttributeType.xml";

	/**
	 * Run this before each unit test in this class. This adds a bit more data to the base data that
	 * is done in the "@Before" method in {@link BaseContextSensitiveTest} (which is run right
	 * before this method).
	 *
	 * @throws Exception
	 */
	@Before
	public void runBeforeEachTest() throws Exception {
		executeDataSet(CONCEPT_ATTRIBUTE_TYPE_XML);
	}
	
	/**
	 * @see ConceptAttributeTypeValidator#validate(Object,Errors)
	 */
	@Test
	@Verifies(value = "should fail validation if name is null or empty or whitespace", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfNameIsNullOrEmptyOrWhitespace() throws Exception {
		ConceptAttributeType type = new ConceptAttributeType();
		type.setName(null);
		type.setDescription("description");
		
		Errors errors = new BindException(type, "type");
		new ConceptAttributeTypeValidator().validate(type, errors);
		Assert.assertTrue(errors.hasFieldErrors("name"));
		
		type.setName("");
		errors = new BindException(type, "type");
		new ConceptAttributeTypeValidator().validate(type, errors);
		Assert.assertTrue(errors.hasFieldErrors("name"));
		
		type.setName(" ");
		errors = new BindException(type, "type");
		new ConceptAttributeTypeValidator().validate(type, errors);
		Assert.assertTrue(errors.hasFieldErrors("name"));
	}
	
	/**
	 * @see ConceptAttributeTypeValidator#validate(Object,Errors)
	 */
	@Test
	@Verifies(value = "should pass validation if all required fields have proper values", method = "validate(Object,Errors)")
	public void validate_shouldPassValidationIfAllRequiredFieldsHaveProperValues() throws Exception {
		ConceptAttributeType type = new ConceptAttributeType();
		type.setName("name");
		type.setDatatypeClassname("org.openmrs.customdatatype.datatype.FreeTextDatatype");
		
		Errors errors = new BindException(type, "type");
		new ConceptAttributeTypeValidator().validate(type, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see ConceptAttributeTypeValidator#validate(Object, Errors)
	 */
	@Test
	@Verifies(value = "should fail if concept attribute type name is duplicate", method = "validate(Object,Errors)")
	public void validate_shouldFailIfConceptAttributeTypeNameIsDuplicate() throws Exception {
		
		Assert.assertNotNull(Context.getConceptService().getConceptAttributeTypeByName("Audit Date"));
		
		ConceptAttributeType type = new ConceptAttributeType();
		type.setName("Audit Date");
		type.setDatatypeClassname("org.openmrs.customdatatype.datatype.FreeTextDatatype");
		Errors errors = new BindException(type, "conceptAttributeType");
		new ConceptAttributeTypeValidator().validate(type, errors);
		Assert.assertTrue(errors.hasFieldErrors("name"));
		
	}
	
	/**
	 * @see ConceptAttributeTypeValidator#validate(Object, Errors)
	 */
	@Test
	@Verifies(value = "should pass editing concept attribute type name", method = "validate(Object,Errors)")
	public void validate_shouldPassEditingConceptAttributeTypeName() throws Exception {
		
		ConceptAttributeType et = Context.getConceptService().getConceptAttributeTypeByName("Audit Date");
		Assert.assertNotNull(et);
		Errors errors = new BindException(et, "conceptAttributeType");
		new ConceptAttributeTypeValidator().validate(et, errors);
		Assert.assertFalse(errors.hasErrors());
		
	}
	
	/**
	 * @see ConceptAttributeTypeValidator#validate(Object,Errors)
	 */
	@Test
	@Verifies(value = "should pass validation if field lengths are correct", method = "validate(Object,Errors)")
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() throws Exception {
		ConceptAttributeType type = new ConceptAttributeType();
		type.setName("name");
		type.setDatatypeClassname("org.openmrs.customdatatype.datatype.FreeTextDatatype");
		type.setDescription("description");
		type.setRetireReason("retireReason");
		
		Errors errors = new BindException(type, "type");
		new ConceptAttributeTypeValidator().validate(type, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see ConceptAttributeTypeValidator#validate(Object,Errors)
	 */
	@Test
	@Verifies(value = "should fail validation if field lengths are not correct", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() throws Exception {
		ConceptAttributeType type = new ConceptAttributeType();
		type
		        .setName("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		type
		        .setDatatypeClassname("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		type
		        .setDescription("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		type
		        .setPreferredHandlerClassname("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		type
		        .setRetireReason("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		
		Errors errors = new BindException(type, "type");
		new ConceptAttributeTypeValidator().validate(type, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("name"));
		Assert.assertTrue(errors.hasFieldErrors("datatypeClassname"));
		Assert.assertTrue(errors.hasFieldErrors("description"));
		Assert.assertTrue(errors.hasFieldErrors("preferredHandlerClassname"));
		Assert.assertTrue(errors.hasFieldErrors("retireReason"));
	}
}
