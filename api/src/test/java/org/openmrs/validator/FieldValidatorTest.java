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
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link FieldValidator} class.
 */
public class FieldValidatorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see FieldValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfNameIsNull() {
		Field ff = new Field();
		ff.setName(null);
		FieldType ft = new FieldType();
		Boolean retired = Boolean.FALSE;
		ft.setId(0xdeadcafe);
		ff.setFieldType(ft);
		ff.setRetired(retired);
		Boolean multiple = Boolean.FALSE;
		ff.setSelectMultiple(multiple);
		
		Errors errors = new BindException(ff, "name");
		new FieldValidator().validate(ff, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("name"));
	}
	
	/**
	 * @see FieldValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfNameIsEmpty() {
		Field ff = new Field();
		ff.setName("");
		FieldType ft = new FieldType();
		Boolean retired = Boolean.FALSE;
		ft.setId(0xdeadcafe);
		ff.setFieldType(ft);
		ff.setRetired(retired);
		Boolean multiple = Boolean.FALSE;
		ff.setSelectMultiple(multiple);
		
		Errors errors = new BindException(ff, "name");
		new FieldValidator().validate(ff, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("name"));
	}
	
	/**
	 * @see FieldValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfNameIsAllWhitespace() {
		Field ff = new Field();
		ff.setName("    ");
		FieldType ft = new FieldType();
		Boolean retired = Boolean.FALSE;
		ft.setId(0xdeadcafe);
		ff.setFieldType(ft);
		ff.setRetired(retired);
		Boolean multiple = Boolean.FALSE;
		ff.setSelectMultiple(multiple);
		
		Errors errors = new BindException(ff, "name");
		new FieldValidator().validate(ff, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("name"));
	}
	
	/**
	 * @see FieldValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfSelectMultipleIsNull() {
		Field ff = new Field();
		ff.setName("good");
		FieldType ft = new FieldType();
		ft.setFieldTypeId(0xdeadcafe);
		Boolean retired = Boolean.FALSE;
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
	 * @see FieldValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfRetiredIsNull() {
		Field ff = new Field();
		ff.setName("good");
		FieldType ft = new FieldType();
		ft.setFieldTypeId(0xdeadcafe);
		ff.setFieldType(ft);
		ff.setName("valid");
		Boolean retired = null;
		ff.setRetired(retired);
		Boolean multiple = Boolean.TRUE;
		ff.setSelectMultiple(multiple);
		
		Errors errors = new BindException(ff, "retired");
		new FieldValidator().validate(ff, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("retired"));
	}
	
	/**
	 * @see FieldValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfAllFieldsAreCorrect() {
		Field ff = new Field();
		FieldType ft = new FieldType();
		Boolean retired = Boolean.FALSE;
		ft.setId(0xdeadcafe);
		ff.setFieldType(ft);
		ff.setName("valid");
		ff.setRetired(retired);
		Boolean multiple = Boolean.FALSE;
		ff.setSelectMultiple(multiple);
		
		Errors errors = new BindException(ff, "name");
		new FieldValidator().validate(ff, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see FieldValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() {
		Field ff = new Field();
		FieldType ft = new FieldType();
		Boolean retired = Boolean.FALSE;
		ft.setId(0xdeadcafe);
		ff.setFieldType(ft);
		ff.setName("name");
		ff.setRetired(retired);
		Boolean multiple = Boolean.FALSE;
		ff.setSelectMultiple(multiple);
		ff.setTableName("tableName");
		ff.setAttributeName("attributeName");
		ff.setRetireReason("retireReason");
		
		Errors errors = new BindException(ff, "field");
		new FieldValidator().validate(ff, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see FieldValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() {
		Field ff = new Field();
		FieldType ft = new FieldType();
		Boolean retired = Boolean.FALSE;
		ft.setId(0xdeadcafe);
		ff.setFieldType(ft);
		ff
		        .setName("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		ff.setRetired(retired);
		Boolean multiple = Boolean.FALSE;
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
