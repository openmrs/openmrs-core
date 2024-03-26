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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.openmrs.Location;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
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
		
		assertTrue(errors.hasFieldErrors("name"));
		assertThat(errors.getFieldErrors("name").get(0).getCode(), is("error.name"));
		assertFalse(errors.hasFieldErrors("description"));
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
		
		assertTrue(errors.hasFieldErrors("retireReason"));
		assertThat(errors.getFieldErrors("retireReason").get(0).getCode(), is("error.null"));
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
		
		assertFalse(location.getRetired());
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
		
		assertFalse(errors.hasErrors());
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
		
		assertFalse(errors.hasErrors());
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
		
		assertTrue(errors.hasFieldErrors("parentLocation"));
		assertThat(errors.getFieldErrors("parentLocation").get(0).getCode(), is("Location.parentLocation.error"));
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
		
		assertTrue(errors.hasErrors());
		assertThat(errors.getAllErrors().get(0).getCode(), is("location.duplicate.name"));
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
		
		assertFalse(errors.hasErrors());
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
		
		assertTrue(errors.hasFieldErrors("name"));
		assertThat(errors.getFieldErrors("name").get(0).getCode(), is("error.exceededMaxLengthOfField"));
		assertTrue(errors.hasFieldErrors("description"));
		assertThat(errors.getFieldErrors("description").get(0).getCode(), is("error.exceededMaxLengthOfField"));
		assertTrue(errors.hasFieldErrors("address1"));
		assertThat(errors.getFieldErrors("address1").get(0).getCode(), is("error.exceededMaxLengthOfField"));
		assertTrue(errors.hasFieldErrors("address2"));
		assertThat(errors.getFieldErrors("address2").get(0).getCode(), is("error.exceededMaxLengthOfField"));
		assertTrue(errors.hasFieldErrors("address3"));
		assertThat(errors.getFieldErrors("address3").get(0).getCode(), is("error.exceededMaxLengthOfField"));
		assertTrue(errors.hasFieldErrors("address4"));
		assertThat(errors.getFieldErrors("address4").get(0).getCode(), is("error.exceededMaxLengthOfField"));
		assertTrue(errors.hasFieldErrors("address5"));
		assertThat(errors.getFieldErrors("address5").get(0).getCode(), is("error.exceededMaxLengthOfField"));
		assertTrue(errors.hasFieldErrors("address6"));
		assertThat(errors.getFieldErrors("address6").get(0).getCode(), is("error.exceededMaxLengthOfField"));
		assertTrue(errors.hasFieldErrors("address7"));
		assertThat(errors.getFieldErrors("address7").get(0).getCode(), is("error.exceededMaxLengthOfField"));
		assertTrue(errors.hasFieldErrors("address8"));
		assertThat(errors.getFieldErrors("address8").get(0).getCode(), is("error.exceededMaxLengthOfField"));
		assertTrue(errors.hasFieldErrors("address9"));
		assertThat(errors.getFieldErrors("address9").get(0).getCode(), is("error.exceededMaxLengthOfField"));
		assertTrue(errors.hasFieldErrors("address10"));
		assertThat(errors.getFieldErrors("address10").get(0).getCode(), is("error.exceededMaxLengthOfField"));
		assertTrue(errors.hasFieldErrors("address11"));
		assertThat(errors.getFieldErrors("address11").get(0).getCode(), is("error.exceededMaxLengthOfField"));
		assertTrue(errors.hasFieldErrors("address12"));
		assertThat(errors.getFieldErrors("address12").get(0).getCode(), is("error.exceededMaxLengthOfField"));
		assertTrue(errors.hasFieldErrors("address13"));
		assertThat(errors.getFieldErrors("address13").get(0).getCode(), is("error.exceededMaxLengthOfField"));
		assertTrue(errors.hasFieldErrors("address14"));
		assertThat(errors.getFieldErrors("address14").get(0).getCode(), is("error.exceededMaxLengthOfField"));
		assertTrue(errors.hasFieldErrors("address15"));
		assertThat(errors.getFieldErrors("address15").get(0).getCode(), is("error.exceededMaxLengthOfField"));
		assertTrue(errors.hasFieldErrors("cityVillage"));
		assertThat(errors.getFieldErrors("cityVillage").get(0).getCode(), is("error.exceededMaxLengthOfField"));
		assertTrue(errors.hasFieldErrors("stateProvince"));
		assertThat(errors.getFieldErrors("stateProvince").get(0).getCode(), is("error.exceededMaxLengthOfField"));
		assertTrue(errors.hasFieldErrors("country"));
		assertThat(errors.getFieldErrors("country").get(0).getCode(), is("error.exceededMaxLengthOfField"));
		assertTrue(errors.hasFieldErrors("postalCode"));
		assertThat(errors.getFieldErrors("postalCode").get(0).getCode(), is("error.exceededMaxLengthOfField"));
		assertTrue(errors.hasFieldErrors("latitude"));
		assertThat(errors.getFieldErrors("latitude").get(0).getCode(), is("error.exceededMaxLengthOfField"));
		assertTrue(errors.hasFieldErrors("longitude"));
		assertThat(errors.getFieldErrors("longitude").get(0).getCode(), is("error.exceededMaxLengthOfField"));
		assertTrue(errors.hasFieldErrors("countyDistrict"));
		assertThat(errors.getFieldErrors("countyDistrict").get(0).getCode(), is("error.exceededMaxLengthOfField"));
		assertTrue(errors.hasFieldErrors("retireReason"));
		assertThat(errors.getFieldErrors("retireReason").get(0).getCode(), is("error.exceededMaxLengthOfField"));
	}
}
