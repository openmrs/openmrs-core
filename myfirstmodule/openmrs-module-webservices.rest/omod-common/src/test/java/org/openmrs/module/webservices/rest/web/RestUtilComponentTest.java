/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Person;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.validation.ValidationException;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

public class RestUtilComponentTest extends BaseModuleWebContextSensitiveTest {
	
	@Test
	public void wrapValidationErrorResponse_shouldCreateSimpleObjectFromErrorsObject() {
		
		Errors ex = new BindException(new Person(), "");
		ex.rejectValue("birthdate", "field.error.message");
		ex.reject("global.error.message");
		
		SimpleObject result = RestUtil.wrapValidationErrorResponse(new ValidationException("some message", ex));
		SimpleObject errors = (SimpleObject) result.get("error");
		Assert.assertEquals("webservices.rest.error.invalid.submission", errors.get("code"));
		
		List<SimpleObject> globalErrors = (List<SimpleObject>) errors.get("globalErrors");
		Assert.assertEquals(1, globalErrors.size());
		Assert.assertEquals("global.error.message", globalErrors.get(0).get("code"));
		
		SimpleObject fieldErrors = (SimpleObject) errors.get("fieldErrors");
		List<SimpleObject> birthdateFieldErrors = (List<SimpleObject>) fieldErrors.get("birthdate");
		Assert.assertEquals("field.error.message", birthdateFieldErrors.get(0).get("code"));
		
	}
	
	@Test
	public void wrapValidationErrorResponse_shouldIncludeGlobalAndFieldErrorObjectsEvenIfEmpty() {
		
		Errors ex = new BindException(new Person(), "");
		
		SimpleObject result = RestUtil.wrapValidationErrorResponse(new ValidationException("some message", ex));
		SimpleObject errors = (SimpleObject) result.get("error");
		Assert.assertEquals("webservices.rest.error.invalid.submission", errors.get("code"));
		
		Assert.assertEquals(0, ((List<SimpleObject>) errors.get("globalErrors")).size());
		Assert.assertNotNull(errors.get("fieldErrors"));
		
	}
}
