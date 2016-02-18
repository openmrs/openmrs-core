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

import org.openmrs.api.context.Context;
import org.openmrs.api.db.AdministrationDAO;
import org.hibernate.SessionFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.Role;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

public class HibernateAdministrationDAOTest extends BaseContextSensitiveTest {

	@Autowired
	private HibernateAdministrationDAO dao;

	private SessionFactory sessionFactory;

	@Before
	public void getSessionFactory() throws Exception {
		sessionFactory = (SessionFactory) applicationContext.getBean("sessionFactory");
	}



	/**
	 * @see HibernateAdministrationDAO#validate(Object,Errors)
	 * @verifies Fail validation for location class if field lengths are not correct
	 */
	@Test
	public void validate_shouldFailValidationForLocationClassIfFieldLengthsAreNotCorrect() throws Exception {
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
		dao.validate(location, errors);
		
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


	/**
	 * @see HibernateAdministrationDAO#validate(Object,Errors)
	 * @verifies Fail validation if field lengths are not correct
	 */
	@Test
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() throws Exception {
		Role role = new Role();
		role.setRole(
				"too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		role.setDescription(
				"too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		Errors errors = new BindException(role, "type");
		dao.validate(role, errors);
		Assert.assertTrue(errors.hasFieldErrors("role"));

	}


	/**
	 * @see HibernateAdministrationDAO#validate(Object,Errors)
	 * @verifies Pass validation for location class if field lengths are correct
	 */
	@Test
	public void validate_shouldPassValidationForLocationClassIfFieldLengthsAreCorrect() throws Exception {
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
	 * @verifies Pass validation if field lengths are correct
	 */
	@Test
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() throws Exception {
		Role role = new Role();
		role.setRole("Bowling race car driver");
		role.setDescription("description");
		Errors errors = new BindException(role, "type");
		dao.validate(role, errors);
		Assert.assertFalse(errors.hasFieldErrors("role"));
	}
}
