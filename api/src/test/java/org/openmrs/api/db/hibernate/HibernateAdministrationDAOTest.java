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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.Location;
import org.openmrs.Role;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HibernateAdministrationDAOTest extends BaseContextSensitiveTest {

	private static final String EXISTING_PROPERTY = "concept.defaultConceptMapType";

	@Autowired
	private HibernateAdministrationDAO dao;

	private SessionFactory sessionFactory;

	@BeforeEach
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
			assertTrue(errorCode.equals(fielderror.getCode()));
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
		role.setRole(
		    "too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		role.setDescription(
		    "too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		Errors errors = new BindException(role, "type");
		dao.validate(role, errors);

		for (String feilds : RoleFeilds) {
			FieldError fielderror = errors.getFieldError(feilds);
			assertTrue(errorCode.equals(fielderror.getCode()));
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

		assertFalse(errors.hasErrors());

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
		assertFalse(errors.hasFieldErrors("role"));
	}

	/**
	 * @see HibernateAdministrationDAO#getGlobalPropertyObject(String)
	 */
	@Test
	public void getGlobalPropertyObject_shouldNotQueryTheCaseSensitivePropertyOnAnExactMatch() {
		// The point of looking the property up by its identifier first is that an exact match never
		// reaches the case-insensitive fallback. Hibernate counts that fallback as a query execution
		// but does not count a load by identifier, so the counter staying flat is what pins the
		// behaviour, whether the load was served from a cache or from the database.
		Context.flushSession();
		Context.clearSession();

		long queryExecutionsBefore = sessionFactory.getStatistics().getQueryExecutionCount();

		assertNotNull(dao.getGlobalPropertyObject(EXISTING_PROPERTY));

		assertEquals(queryExecutionsBefore, sessionFactory.getStatistics().getQueryExecutionCount(),
		    "an exact match should not fall back to the case insensitive query");
	}

	/**
	 * @see HibernateAdministrationDAO#getGlobalPropertyObject(String)
	 */
	@Test
	public void getGlobalPropertyObject_shouldStillMatchCaseInsensitively() {
		String expectedValue = dao.getGlobalPropertyObject(EXISTING_PROPERTY).getPropertyValue();

		GlobalProperty upperCased = dao.getGlobalPropertyObject(EXISTING_PROPERTY.toUpperCase());
		assertNotNull(upperCased, "an upper cased name should still resolve");
		assertEquals(expectedValue, upperCased.getPropertyValue());

		GlobalProperty lowerCased = dao.getGlobalPropertyObject(EXISTING_PROPERTY.toLowerCase());
		assertNotNull(lowerCased, "a lower cased name should still resolve");
		assertEquals(expectedValue, lowerCased.getPropertyValue());

		// Asserting on the value rather than on getProperty(): when the database itself compares
		// strings without regard to case, Hibernate hands back an entity whose identifier is the
		// name that was asked for rather than the one stored in the row. That is true of the H2
		// test database, which is created with IGNORECASE=TRUE, and of MySQL under a case
		// insensitive collation. The value comes from the row either way, so it is what proves the
		// right property was found.
	}

	/**
	 * @see HibernateAdministrationDAO#getGlobalPropertyObject(String)
	 */
	@Test
	public void getGlobalPropertyObject_shouldReturnNullForAPropertyThatDoesNotExist() {
		assertNull(dao.getGlobalPropertyObject("some.property.that.is.not.set"));
	}

	/**
	 * @see HibernateAdministrationDAO#getGlobalPropertyObject(String)
	 */
	@Test
	public void getGlobalPropertyObject_shouldFailForANullPropertyName() {
		// the property name is the identifier, so Hibernate rejects a null one. Pinned here so that
		// this does not silently become a null return in a later refactor.
		assertThrows(IllegalArgumentException.class, () -> dao.getGlobalPropertyObject(null));
	}

	/**
	 * @see HibernateAdministrationDAO#getGlobalPropertyObject(String)
	 */
	@Test
	public void getGlobalPropertyObject_shouldFindAPropertyThatHasNotBeenFlushedYet() {
		String propertyName = "unflushed.property";
		dao.saveGlobalProperty(new GlobalProperty(propertyName, "unflushed value"));

		GlobalProperty saved = dao.getGlobalPropertyObject(propertyName);

		assertNotNull(saved, "a property still sitting unflushed in the session should be found");
		assertEquals("unflushed value", saved.getPropertyValue());
	}
}
