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
import org.junit.Before;
import org.junit.Test;
import org.openmrs.RelationshipType;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link RelationshipTypeValidator} class.
 */
public class RelationshipTypeValidatorTest extends BaseContextSensitiveTest {
	
	protected static final String VALIDATOR_XML = "org/openmrs/api/include/ValidatorTest.xml";
	
	/**
	 * Run this before each unit test in this class. The "@Before" method in
	 * {@link BaseContextSensitiveTest} is run right before this method.
	 * 
	 * @throws Exception
	 */
	@Before
	public void runExtraSetup() throws Exception {
		executeDataSet(VALIDATOR_XML);
	}
	
	/**
	 * @see {@link RelationshipTypeValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if aIsToB is null or empty", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfaIsToBIsNullOrEmpty() throws Exception {
		RelationshipType type = new RelationshipType();
		Errors errors = new BindException(type, "RelationshipType");
		new RelationshipTypeValidator().validate(type, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("aIsToB"));
	}
	
	/**
	 * @see {@link RelationshipTypeValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if bIsToA is null or empty", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfbIsToAIsNullOrEmpty() throws Exception {
		RelationshipType type = new RelationshipType();
		
		Errors errors = new BindException(type, "RelationshipType");
		new RelationshipTypeValidator().validate(type, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("bIsToA"));
	}
	
	/**
	 * @see {@link RelationshipTypeValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if non-retired record with same combination exists", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfNonretiredRecordWithSameCombinationExists() throws Exception {
		RelationshipType type = new RelationshipType();
		type.setaIsToB("test");
		type.setbIsToA("unretired");
		
		Errors errors = new BindException(type, "RelationshipType");
		new RelationshipTypeValidator().validate(type, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("aIsToB"));
		Assert.assertTrue(errors.hasFieldErrors("bIsToA"));
	}
	
	/**
	 * @see {@link RelationshipTypeValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if non-retired record with reverse combination exists", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfNonretiredRecordWithReverseCombinationExists() throws Exception {
		RelationshipType type = new RelationshipType();
		type.setaIsToB("unretired");
		type.setbIsToA("test");
		
		Errors errors = new BindException(type, "RelationshipType");
		new RelationshipTypeValidator().validate(type, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("aIsToB"));
		Assert.assertTrue(errors.hasFieldErrors("bIsToA"));
	}
	
	/**
	 * @see {@link RelationshipTypeValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass validation if retired record with same combination exists", method = "validate(Object,Errors)")
	public void validate_shouldPassValidationIfRetiredRecordWithSameCombinationExists() throws Exception {
		RelationshipType type = new RelationshipType();
		type.setaIsToB("test");
		type.setbIsToA("retired");
		
		Errors errors = new BindException(type, "RelationshipType");
		new RelationshipTypeValidator().validate(type, errors);
		
		Assert.assertFalse(errors.hasFieldErrors("aIsToB"));
		Assert.assertFalse(errors.hasFieldErrors("bIsToA"));
	}
}