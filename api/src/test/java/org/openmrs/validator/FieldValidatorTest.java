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
	
	/**
	 * @see {@link FieldValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass validation if field lengths are correct", method = "validate(Object,Errors)")
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() throws Exception {
		Field ff = new Field();
		FieldType ft = new FieldType();
		Boolean retired = new Boolean(false);
		ft.setId(0xdeadcafe);
		ff.setFieldType(ft);
		ff.setName("name");
		ff.setRetired(retired);
		Boolean multiple = new Boolean(false);
		ff.setSelectMultiple(multiple);
		ff.setTableName("tableName");
		ff.setAttributeName("attributeName");
		ff.setRetireReason("retireReason");
		
		Errors errors = new BindException(ff, "field");
		new FieldValidator().validate(ff, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see {@link FieldValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if field lengths are not correct", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() throws Exception {
		Field ff = new Field();
		FieldType ft = new FieldType();
		Boolean retired = new Boolean(false);
		ft.setId(0xdeadcafe);
		ff.setFieldType(ft);
		ff
		        .setName("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		ff.setRetired(retired);
		Boolean multiple = new Boolean(false);
		ff.setSelectMultiple(multiple);
		ff
		        .setTableName("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		ff
		        .setAttributeName("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		ff
		        .setRetireReason("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		
		Errors errors = new BindException(ff, "field");
		new FieldValidator().validate(ff, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("name"));
		Assert.assertTrue(errors.hasFieldErrors("tableName"));
		Assert.assertTrue(errors.hasFieldErrors("attributeName"));
		Assert.assertTrue(errors.hasFieldErrors("retireReason"));
	}
}
