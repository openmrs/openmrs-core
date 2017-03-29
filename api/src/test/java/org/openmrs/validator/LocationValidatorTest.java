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
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link LocationValidator} class.
 */
public class LocationValidatorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see LocationValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfNameIsNullOrEmpty() {
		Location location = new Location();
		location.setDescription("desc");
		
		Errors errors = new BindException(location, "location");
		new LocationValidator().validate(location, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("name"));
		Assert.assertFalse(errors.hasFieldErrors("description"));
	}
	
	/**
	 * @see LocationValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfRetiredAndRetireReasonIsNullOrEmpty() {
		Location location = new Location();
		location.setName("County General");
		location.setRetired(true);
		
		Errors errors = new BindException(location, "location");
		new LocationValidator().validate(location, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("retireReason"));
	}
	
	/**
	 * @see LocationValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldSetRetiredToFalseIfRetireReasonIsNullOrEmpty() {
		Location location = new Location();
		location.setName("County General");
		location.setRetired(true);
		
		Errors errors = new BindException(location, "location");
		new LocationValidator().validate(location, errors);
		
		Assert.assertFalse(location.getRetired());
	}
	
	/**
	 * @see LocationValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfAllFieldsAreCorrect() {
		Location location = new Location();
		location.setName("County General");
		location.setDescription("desc");
		
		Errors errors = new BindException(location, "location");
		new LocationValidator().validate(location, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see LocationValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfRetiredLocationIsGivenRetiredReason() {
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
	 * @see LocationValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfParentLocationCreatesALoop() {
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
	 * @see org.openmrs.validator.LocationValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfLocationNameAlreadyExist() {
		Location location = new Location();
		location.setName("Unknown Location");
		location.setDescription("desc");
		
		Errors errors = new BindException(location, "location");
		new LocationValidator().validate(location, errors);
		
		Assert.assertTrue(errors.hasErrors());
	}
	
	/**
	 * @see LocationValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() {
		Location location = new Location();
		location.setName("name");
		location.setDescription("description");
		location.setAddress1("address1");
		location.setAddress2("address2");
		location.setAddress3("address3");
		location.setAddress4("address4");
		location.setAddress5("address5");
		location.setAddress6("address6");
		location.setCityVillage("cityVillage");
		location.setStateProvince("stateProvince");
		location.setCountry("country");
		location.setPostalCode("postalCode");
		location.setLatitude("latitude");
		location.setLongitude("longitude");
		location.setCountyDistrict("countyDistrict");
		location.setRetireReason("retireReason");
		
		Errors errors = new BindException(location, "location");
		new LocationValidator().validate(location, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see LocationValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() {
		Location location = new Location();
		String longString = "too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text";
		location.setName(longString);
		location.setDescription(longString);
		location.setAddress1(longString);
		location.setAddress2(longString);
		location.setAddress3(longString);
		location.setAddress4(longString);
		location.setAddress5(longString);
		location.setAddress6(longString);
		location.setAddress7(longString);
		location.setAddress8(longString);
		location.setAddress9(longString);
		location.setAddress10(longString);
		location.setAddress11(longString);
		location.setAddress12(longString);
		location.setAddress13(longString);
		location.setAddress14(longString);
		location.setAddress15(longString);
		location.setCityVillage(longString);
		location.setStateProvince(longString);
		location.setCountry(longString);
		location.setPostalCode(longString);
		location.setLatitude(longString);
		location.setLongitude(longString);
		location.setCountyDistrict(longString);
		location.setRetireReason(longString);
		
		Errors errors = new BindException(location, "location");
		new LocationValidator().validate(location, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("name"));
		Assert.assertTrue(errors.hasFieldErrors("description"));
		Assert.assertTrue(errors.hasFieldErrors("address1"));
		Assert.assertTrue(errors.hasFieldErrors("address2"));
		Assert.assertTrue(errors.hasFieldErrors("address3"));
		Assert.assertTrue(errors.hasFieldErrors("address4"));
		Assert.assertTrue(errors.hasFieldErrors("address5"));
		Assert.assertTrue(errors.hasFieldErrors("address6"));
		Assert.assertTrue(errors.hasFieldErrors("address7"));
		Assert.assertTrue(errors.hasFieldErrors("address8"));
		Assert.assertTrue(errors.hasFieldErrors("address9"));
		Assert.assertTrue(errors.hasFieldErrors("address10"));
		Assert.assertTrue(errors.hasFieldErrors("address11"));
		Assert.assertTrue(errors.hasFieldErrors("address12"));
		Assert.assertTrue(errors.hasFieldErrors("address13"));
		Assert.assertTrue(errors.hasFieldErrors("address14"));
		Assert.assertTrue(errors.hasFieldErrors("address15"));
		Assert.assertTrue(errors.hasFieldErrors("cityVillage"));
		Assert.assertTrue(errors.hasFieldErrors("stateProvince"));
		Assert.assertTrue(errors.hasFieldErrors("country"));
		Assert.assertTrue(errors.hasFieldErrors("postalCode"));
		Assert.assertTrue(errors.hasFieldErrors("latitude"));
		Assert.assertTrue(errors.hasFieldErrors("longitude"));
		Assert.assertTrue(errors.hasFieldErrors("countyDistrict"));
		Assert.assertTrue(errors.hasFieldErrors("retireReason"));
	}
}
