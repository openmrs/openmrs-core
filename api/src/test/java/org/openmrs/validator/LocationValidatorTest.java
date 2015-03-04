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
import org.openmrs.Location;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link LocationValidator} class.
 */
public class LocationValidatorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see {@link LocationValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if name is null or empty", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfNameIsNullOrEmpty() throws Exception {
		Location location = new Location();
		location.setDescription("desc");
		
		Errors errors = new BindException(location, "location");
		new LocationValidator().validate(location, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("name"));
		Assert.assertFalse(errors.hasFieldErrors("description"));
	}
	
	/**
	 * @see {@link LocationValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if retired and retireReason is null or empty", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfRetiredAndRetireReasonIsNullOrEmpty() throws Exception {
		Location location = new Location();
		location.setName("County General");
		location.setRetired(true);
		
		Errors errors = new BindException(location, "location");
		new LocationValidator().validate(location, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("retireReason"));
	}
	
	/**
	 * @see {@link LocationValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should set retired to false if retireReason is null or empty", method = "validate(Object,Errors)")
	public void validate_shouldSetRetiredToFalseIfRetireReasonIsNullOrEmpty() throws Exception {
		Location location = new Location();
		location.setName("County General");
		location.setRetired(true);
		
		Errors errors = new BindException(location, "location");
		new LocationValidator().validate(location, errors);
		
		Assert.assertFalse(location.isRetired());
	}
	
	/**
	 * @see {@link LocationValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass validation if all fields are correct", method = "validate(Object,Errors)")
	public void validate_shouldPassValidationIfAllFieldsAreCorrect() throws Exception {
		Location location = new Location();
		location.setName("County General");
		location.setDescription("desc");
		
		Errors errors = new BindException(location, "location");
		new LocationValidator().validate(location, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see {@link LocationValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass validation if retired location is given retired reason", method = "validate(Object,Errors)")
	public void validate_shouldPassValidationIfRetiredLocationIsGivenRetiredReason() throws Exception {
		Location location = new Location();
		location.setName("County General");
		location.setDescription("desc");
		location.setRetired(true);
		location.setRetireReason("Because I don't like County General");
		
		Errors errors = new BindException(location, "location");
		new LocationValidator().validate(location, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see {@link LocationValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if parent location creates a loop", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfParentLocationCreatesALoop() throws Exception {
		Location location1 = new Location();
		Location location2 = new Location();
		Location location3 = new Location();
		location1.setName("County General");
		location2.setName("Radiology");
		location3.setName("Radiology Lab");
		location3.setParentLocation(location2);
		location2.setParentLocation(location1);
		location1.setParentLocation(location3);
		
		Errors errors = new BindException(location1, "location");
		new LocationValidator().validate(location1, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("parentLocation"));
	}
	
	/**
	 * @see {@link org.openmrs.validator.LocationValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if location name is already exist", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfLocationNameAlreadyExist() throws Exception {
		Location location = new Location();
		location.setName("Unknown Location");
		location.setDescription("desc");
		
		Errors errors = new BindException(location, "location");
		new LocationValidator().validate(location, errors);
		
		Assert.assertTrue(errors.hasErrors());
	}
}
