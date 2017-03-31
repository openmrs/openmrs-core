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
import org.openmrs.ImplementationId;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link ImplementationIdValidator} class.
 */

public class ImplementationIdValidatorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see ImplementationIdValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldThrowAPIExceptionIfImplementationIdIsNUll() {
		try {
			new ImplementationIdValidator().validate(null, null);
			Assert.fail();
		}
		catch (APIException e) {
			Assert.assertEquals(e.getMessage(), Context.getMessageSourceService().getMessage("ImplementationId.null"));
		}
	}
	
	/**
	 * @see ImplementationIdValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfImplementationIdIsNull() {
		ImplementationId implementationId = new ImplementationId();
		implementationId.setPassphrase("PASSPHRASE");
		implementationId.setDescription("Description");
		
		Errors errors = new BindException(implementationId, "implementationId");
		new ImplementationIdValidator().validate(implementationId, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("implementationId"));
		Assert.assertFalse(errors.hasFieldErrors("passphrase"));
		Assert.assertFalse(errors.hasFieldErrors("description"));
	}
	
	/**
	 * @see ImplementationIdValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfDescriptionIsNull() {
		ImplementationId implementationId = new ImplementationId();
		implementationId.setImplementationId("IMPL_ID");
		implementationId.setPassphrase("PASSPHRASE");
		
		Errors errors = new BindException(implementationId, "implementationId");
		new ImplementationIdValidator().validate(implementationId, errors);
		
		Assert.assertFalse(errors.hasFieldErrors("description"));
		Assert.assertFalse(errors.hasFieldErrors("implementationId"));
		Assert.assertFalse(errors.hasFieldErrors("passphrase"));
	}
	
	/**
	 * @see ImplementationIdValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfPassPhraseIsNull() {
		ImplementationId implementationId = new ImplementationId();
		implementationId.setImplementationId("IMPL_ID");
		implementationId.setDescription("Description");
		
		Errors errors = new BindException(implementationId, "implementationId");
		new ImplementationIdValidator().validate(implementationId, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("passphrase"));
		Assert.assertFalse(errors.hasFieldErrors("implementationId"));
		Assert.assertFalse(errors.hasFieldErrors("description"));
	}
	
	/**
	 * @see ImplementationIdValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailIfGivenEmptyImplementationIdObject() {
		// save a blank impl id. exception thrown
		ImplementationId implementationId = new ImplementationId();
		Errors errors = new BindException(implementationId, "implementationId");
		new ImplementationIdValidator().validate(implementationId, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("passphrase"));
		Assert.assertTrue(errors.hasFieldErrors("implementationId"));
		Assert.assertFalse(errors.hasFieldErrors("description"));
	}
	
	/**
	 * @see ImplementationIdValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailIfGivenACaretInTheImplementationIdCode() {
		ImplementationId invalidId = new ImplementationId();
		invalidId.setImplementationId("caret^caret");
		invalidId.setName("an invalid impl id for a unit test");
		invalidId.setPassphrase("some valid passphrase");
		invalidId.setDescription("Some valid description");
		Errors errors = new BindException(invalidId, "implementationId");
		new ImplementationIdValidator().validate(invalidId, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("implementationId"));
		Assert.assertEquals("ImplementationId.implementationId.invalidCharacter", errors.getFieldError("implementationId")
		        .getCode());
	}
	
	/**
	 * @see ImplementationIdValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailIfGivenAPipeInTheImplementationIdCode() {
		// save an impl id with an invalid hl7 code
		ImplementationId invalidId2 = new ImplementationId();
		invalidId2.setImplementationId("pipe|pipe");
		invalidId2.setName("an invalid impl id for a unit test");
		invalidId2.setPassphrase("some valid passphrase");
		invalidId2.setDescription("Some valid description");
		Errors errors = new BindException(invalidId2, "implementationId");
		new ImplementationIdValidator().validate(invalidId2, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("implementationId"));
		Assert.assertEquals("ImplementationId.implementationId.invalidCharacter", errors.getFieldError("implementationId")
		        .getCode());
	}
	
	/**
	 * @see ImplementationIdValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfNameIsNull() {
		ImplementationId implementationId = new ImplementationId();
		implementationId.setImplementationId("IMPL_ID");
		implementationId.setPassphrase("PASSPHRASE");
		implementationId.setDescription("Description");
		
		Errors errors = new BindException(implementationId, "implementationId");
		new ImplementationIdValidator().validate(implementationId, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("name"));
		Assert.assertFalse(errors.hasFieldErrors("implementationId"));
		Assert.assertFalse(errors.hasFieldErrors("passphrase"));
		Assert.assertFalse(errors.hasFieldErrors("description"));
	}
	
}
