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
import org.openmrs.Field;
import org.openmrs.FieldType;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link FieldValidator} class.
 */
public class FieldValidatorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see {@link FieldValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if name is null", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfNameIsNull() throws Exception {
		Field ff = new Field();
		ff.setName(null);
		FieldType ft = new FieldType();
		Boolean retired = new Boolean(false);
		ft.setId(0xdeadcafe);
		ff.setFieldType(ft);
		ff.setRetired(retired);
		Boolean multiple = new Boolean(false);
		ff.setSelectMultiple(multiple);
		
		Errors errors = new BindException(ff, "name");
		new FieldValidator().validate(ff, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("name"));
	}
	
	/**
	 * @see {@link FieldValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if name is empty", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfNameIsEmpty() throws Exception {
		Field ff = new Field();
		ff.setName("");
		FieldType ft = new FieldType();
		Boolean retired = new Boolean(false);
		ft.setId(0xdeadcafe);
		ff.setFieldType(ft);
		ff.setRetired(retired);
		Boolean multiple = new Boolean(false);
		ff.setSelectMultiple(multiple);
		
		Errors errors = new BindException(ff, "name");
		new FieldValidator().validate(ff, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("name"));
	}
	
	/**
	 * @see {@link FieldValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if name is all whitespace", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfNameIsAllWhitespace() throws Exception {
		Field ff = new Field();
		ff.setName("    ");
		FieldType ft = new FieldType();
		Boolean retired = new Boolean(false);
		ft.setId(0xdeadcafe);
		ff.setFieldType(ft);
		ff.setRetired(retired);
		Boolean multiple = new Boolean(false);
		ff.setSelectMultiple(multiple);
		
		Errors errors = new BindException(ff, "name");
		new FieldValidator().validate(ff, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("name"));
	}
	
	/**
	 * @see {@link FieldValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if selectMultiple is null", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfSelectMultipleIsNull() throws Exception {
		Field ff = new Field();
		ff.setName("good");
		FieldType ft = new FieldType();
		ft.setFieldTypeId(0xdeadcafe);
		Boolean retired = new Boolean(false);
		ff.setFieldType(ft);
		ff.setName("valid");
		ff.setRetired(retired);
		Boolean multiple = null;
		ff.setSelectMultiple(multiple);
		
		Errors errors = new BindException(ff, "selectMultiple");
		new FieldValidator().validate(ff, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("selectMultiple"));
	}
	
	/**
	 * @see {@link FieldValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if retired is null", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfRetiredIsNull() throws Exception {
		Field ff = new Field();
		ff.setName("good");
		FieldType ft = new FieldType();
		ft.setFieldTypeId(0xdeadcafe);
		ff.setFieldType(ft);
		ff.setName("valid");
		Boolean retired = null;
		ff.setRetired(retired);
		Boolean multiple = new Boolean(true);
		ff.setSelectMultiple(multiple);
		
		Errors errors = new BindException(ff, "retired");
		new FieldValidator().validate(ff, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("retired"));
	}
	
	/**
	 * @see {@link FieldValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass if name is ok and fieldType, selectMultiple, and retired are non-null", method = "validate(Object,Errors)")
	public void validate_shouldPassValidationIfAllFieldsAreCorrect() throws Exception {
		Field ff = new Field();
		FieldType ft = new FieldType();
		Boolean retired = new Boolean(false);
		ft.setId(0xdeadcafe);
		ff.setFieldType(ft);
		ff.setName("valid");
		ff.setRetired(retired);
		Boolean multiple = new Boolean(false);
		ff.setSelectMultiple(multiple);
		
		Errors errors = new BindException(ff, "name");
		new FieldValidator().validate(ff, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
}
