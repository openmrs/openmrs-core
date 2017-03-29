/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db.hibernate;

import org.hibernate.SessionFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.Role;
import org.openmrs.test.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;

public class HibernateAdministrationDAOTest extends BaseContextSensitiveTest {
	
	@Autowired
	private HibernateAdministrationDAO dao;
	
	private SessionFactory sessionFactory;
	
	@Before
	public void getSessionFactory() {
		sessionFactory = (SessionFactory) applicationContext.getBean("sessionFactory");
	}
	
	/**
	 * @see HibernateAdministrationDAO#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationForLocationClassIfFieldLengthsAreNotCorrect() {
		Location location = new Location();
		String longString = "too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text";
		
		String[] LocationFields = new String[] { "name", "description", "address1", "address2", "address3", "address4",
		        "address5", "address6", "address7", "address8", "address9", "address10", "address11", "address12",
		        "address13", "address14", "address15", "cityVillage", "stateProvince", "country", "postalCode", "latitude",
		        "longitude", "countyDistrict", "retireReason" };
		
		String errorCode = "error.exceededMaxLengthOfField";
		
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
		dao.validate(location, errors);
		
		for (String feilds : LocationFields) {
			FieldError fielderror = errors.getFieldError(feilds);
			Assert.assertTrue(errorCode.equals(fielderror.getCode()));
		}
		
	}
	
	/**
	 * @see HibernateAdministrationDAO#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() {
		String errorCode = "error.exceededMaxLengthOfField";
		String[] RoleFeilds = new String[] { "role", "description" };
		Role role = new Role();
		role.setRole("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		role.setDescription("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		Errors errors = new BindException(role, "type");
		dao.validate(role, errors);
		
		for (String feilds : RoleFeilds) {
			FieldError fielderror = errors.getFieldError(feilds);
			Assert.assertTrue(errorCode.equals(fielderror.getCode()));
		}
		
	}
	
	/**
	 * @see HibernateAdministrationDAO#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassValidationForLocationClassIfFieldLengthsAreCorrect() {
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
		dao.validate(location, errors);
		
		Assert.assertFalse(errors.hasErrors());
		
	}
	
	/**
	 * @see HibernateAdministrationDAO#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() {
		Role role = new Role();
		role.setRole("Bowling race car driver");
		role.setDescription("description");
		Errors errors = new BindException(role, "type");
		dao.validate(role, errors);
		Assert.assertFalse(errors.hasFieldErrors("role"));
	}
}
