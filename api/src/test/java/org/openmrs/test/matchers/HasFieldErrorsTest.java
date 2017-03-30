/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.test.matchers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.hamcrest.Description;
import org.hamcrest.StringDescription;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.notification.Alert;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests {@link HasFieldErrors}.
 */
public class HasFieldErrorsTest {
	
	private HasFieldErrors matcher;
	
	private Errors item;
	
	private Description description;
	
	@Before
	public void setUp() {
		
		Alert target = new Alert();
		item = new BindException(target, "alert");
		description = new StringDescription();
	}
	
	@Test
	public void shouldAppendFieldErrorsDescriptionIfFieldAndCodeAreNull() {
		
		matcher = HasFieldErrors.hasFieldErrors();
		
		matcher.describeTo(description);
		
		assertThat(description.toString(), is("to have field errors"));
	}
	
	@Test
	public void shouldAppendNoErrorForSpecificFieldIfFieldIsNonNull() {
		
		matcher = HasFieldErrors.hasFieldErrors("uuid");
		
		matcher.describeTo(description);
		
		assertThat(description.toString(), is("to have field errors for 'uuid'"));
	}
	
	@Test
	public void shouldAppendNoErrorForSpecificFieldAndCodeIfFieldAndCodeAreNonNull() {
		
		matcher = HasFieldErrors.hasFieldErrors("uuid", "error.null");
		
		matcher.describeTo(description);
		
		assertThat(description.toString(), is("to have field errors for 'uuid' and code 'error.null'"));
	}
	
	@Test
	public void shouldNotMatchIfFieldAndCodeAreNullAndGivenErrorHasNoFieldErrors() {
		
		matcher = HasFieldErrors.hasFieldErrors();
		
		assertFalse(matcher.matchesSafely(item));
	}
	
	@Test
	public void shouldMatchIfFieldAndCodeAreNullAndGivenErrorHasFieldErrors() {
		
		matcher = HasFieldErrors.hasFieldErrors();
		item.rejectValue("text", "error.null");
		
		assertTrue(matcher.matchesSafely(item));
	}
	
	@Test
	public void shouldNotMatchIfFieldIsNonNullAndGivenErrorDoesNotHaveFieldErrors() {
		
		matcher = HasFieldErrors.hasFieldErrors("text");
		
		assertFalse(matcher.matchesSafely(item));
	}
	
	@Test
	public void shouldNotMatchIfFieldIsNonNullButNotContainedInGivenErrors() {
		
		matcher = HasFieldErrors.hasFieldErrors("text");
		item.rejectValue("uuid", "duplicate.uuid");
		
		assertFalse(matcher.matchesSafely(item));
	}
	
	@Test
	public void shouldMatchIfFieldIsNonNullAndContainedInGivenErrors() {
		
		matcher = HasFieldErrors.hasFieldErrors("uuid");
		item.rejectValue("uuid", "duplicate.uuid");
		item.rejectValue("text", "error.null");
		
		assertTrue(matcher.matchesSafely(item));
	}
	
	@Test
	public void shouldNotMatchIfFieldAndCodeAreNonNullAndGivenErrorDoesNotHaveFieldErrors() {
		
		matcher = HasFieldErrors.hasFieldErrors("uuid", "invalid.uuid");
		
		assertFalse(matcher.matchesSafely(item));
	}
	
	@Test
	public void shouldNotMatchIfFieldAndCodeAreNonNullAndGivenErrorsDoesNotContainSpecificErrorCode() {
		
		matcher = HasFieldErrors.hasFieldErrors("uuid", "error.null");
		item.rejectValue("uuid", "duplicate.uuid");
		item.rejectValue("uuid", "invalid.uuid");
		
		assertFalse(matcher.matchesSafely(item));
	}
	
	@Test
	public void shouldNotMatchIfFieldAndCodeAreNonNullAndGivenErrorsDoesNotContainSpecificField() {
		
		matcher = HasFieldErrors.hasFieldErrors("id", "error.null");
		item.rejectValue("text", "error.null");
		item.rejectValue("uuid", "invalid.uuid");
		
		assertFalse(matcher.matchesSafely(item));
	}
	
	@Test
	public void shouldMatchIfFieldAndCodeAreNonNullAndContainedInGivenErrorsWithASingleFieldError() {
		
		matcher = HasFieldErrors.hasFieldErrors("uuid", "duplicate.uuid");
		item.rejectValue("uuid", "duplicate.uuid");
		
		assertTrue(matcher.matchesSafely(item));
	}
	
	@Test
	public void shouldMatchIfFieldAndCodeAreNonNullAndContainedInGivenErrorsWithMultipleFieldErrors() {
		
		matcher = HasFieldErrors.hasFieldErrors("uuid", "invalid.uuid");
		item.rejectValue("uuid", "duplicate.uuid");
		item.rejectValue("uuid", "invalid.uuid");
		
		assertTrue(matcher.matchesSafely(item));
	}
}
