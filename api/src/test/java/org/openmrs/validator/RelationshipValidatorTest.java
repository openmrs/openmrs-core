/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.validator;

import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Relationship;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

/**
 * Tests methods on the {@link RelationshipValidator} class.
 */
public class RelationshipValidatorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see {@link RelationshipValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "Should fail if start date is after end date", method = "validate(Relationship)")
	public void validate_shouldFailIfEndDateIsBeforeStartDate() throws Exception {
		Relationship relationship = new Relationship(1);
		relationship.setStartDate(Context.getDateFormat().parse("18/02/2012"));
		relationship.setEndDate(Context.getDateFormat().parse("18/02/2001"));
		Map<String, String> map = new HashMap<String, String>();
		MapBindingResult errors = new MapBindingResult(map, Relationship.class.getName());
		new RelationshipValidator().validate(relationship, errors);
		Assert.assertEquals(true, errors.hasErrors());
	}
	
	/**
	 * @see {@link RelationshipValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "Should pass if end date is after the start date", method = "validate(Relationship)")
	public void validate_shouldPassIfEndDateIsAfterStartDate() throws Exception {
		Relationship relationship = new Relationship(1);
		relationship.setStartDate(Context.getDateFormat().parse("18/02/2012"));
		relationship.setEndDate(Context.getDateFormat().parse("18/03/2012"));
		Map<String, String> map = new HashMap<String, String>();
		MapBindingResult errors = new MapBindingResult(map, Relationship.class.getName());
		new RelationshipValidator().validate(relationship, errors);
		Assert.assertFalse(errors.hasErrors());
	}
}
