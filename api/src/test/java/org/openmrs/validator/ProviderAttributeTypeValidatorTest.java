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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.ProviderAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link ProviderAttributeTypeValidator} class.
 */
public class ProviderAttributeTypeValidatorTest extends BaseContextSensitiveTest {
	
	private static final String PROVIDER_ATTRIBUTE_DATA_XML = "org/openmrs/api/include/ProviderServiceTest-providerAttributes.xml";
	
	/**
	 * Run this before each unit test in this class. This adds a bit more data to the base data that
	 * is done in the "@Before" method in {@link BaseContextSensitiveTest} (which is run right
	 * before this method).
	 *
	 * @throws Exception
	 */
	@BeforeEach
	public void runBeforeEachTest() {
		executeDataSet(PROVIDER_ATTRIBUTE_DATA_XML);
	}
	
	/**
	 * @see ProviderAttributeTypeValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() {
		ProviderAttributeType type = new ProviderAttributeType();
		type.setName("name");
		type.setDatatypeClassname("org.openmrs.customdatatype.datatype.FreeTextDatatype");
		type.setDescription("description");
		type.setRetireReason("retireReason");
		
		Errors errors = new BindException(type, "type");
		new ProviderAttributeTypeValidator().validate(type, errors);
		
		assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see ProviderAttributeTypeValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfDatatypeClassnameIsEmpty() {
		ProviderAttributeType type = new ProviderAttributeType();
		type.setName("name");
		type.setDatatypeClassname("");
		type.setDescription("description");
		type.setRetireReason("retireReason");
		
		Errors errors = new BindException(type, "type");
		new ProviderAttributeTypeValidator().validate(type, errors);
		assertTrue(errors.hasFieldErrors("datatypeClassname"));
	}
	
	/**
	 * @see ProviderAttributeTypeValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationWhenAnActiveAttributeTypeWithSameNameExists() {
		assertNotNull(Context.getProviderService().getProviderAttributeTypeByName("Audit Date"));
		ProviderAttributeType type = new ProviderAttributeType();
		type.setName("Audit Date");
		type.setDatatypeClassname("org.openmrs.customdatatype.datatype.DateDatatype");
		Errors errors = new BindException(type, "providerAttributeType");
		new ProviderAttributeTypeValidator().validate(type, errors);
		assertTrue(errors.hasFieldErrors("name"));
	}
	
	/**
	 * @see ProviderAttributeTypeValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() {
		ProviderAttributeType type = new ProviderAttributeType();
		type
			.setName("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		type
			.setDatatypeClassname("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		type.setDescription(new String(new char[655555]));
		type
			.setPreferredHandlerClassname("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		type
			.setRetireReason("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		
		Errors errors = new BindException(type, "type");
		new ProviderAttributeTypeValidator().validate(type, errors);
		
		assertTrue(errors.hasFieldErrors("name"));
		assertTrue(errors.hasFieldErrors("datatypeClassname"));
		assertTrue(errors.hasFieldErrors("description"));
		assertTrue(errors.hasFieldErrors("preferredHandlerClassname"));
		assertTrue(errors.hasFieldErrors("retireReason"));
	}
}
