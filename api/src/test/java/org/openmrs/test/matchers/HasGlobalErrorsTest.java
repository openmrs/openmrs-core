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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.hamcrest.Description;
import org.hamcrest.StringDescription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests {@link HasGlobalErrors}.
 */
public class HasGlobalErrorsTest {
	
	private HasGlobalErrors matcher;
	
	private Errors item;
	
	private Description description;
	
	@BeforeEach
	public void setUp() {
		
		item = new BindException("", "string");
		description = new StringDescription();
	}
	
	@Test
	public void shouldAppendGlobalErrorsDescriptionIfCodeIsNull() {
		
		matcher = HasGlobalErrors.hasGlobalErrors();
		
		matcher.describeTo(description);
		
		assertThat(description.toString(), is("to have global errors"));
	}
	
	@Test
	public void shouldAppendGlobalErrorsDescriptionWithCodeIfCodeIsNonNull() {
		
		matcher = HasGlobalErrors.hasGlobalErrors("error.null");
		
		matcher.describeTo(description);
		
		assertThat(description.toString(), is("to have global error of code 'error.null'"));
	}
	
	@Test
	public void shouldNotMatchIfCodeIsNullAndGivenErrorHasNoGlobalErrors() {
		
		matcher = HasGlobalErrors.hasGlobalErrors();
		
		assertFalse(matcher.matchesSafely(item), "should not match");
	}
	
	@Test
	public void shouldMatchIfCodeIsNullAndGivenErrorHasGlobalErrors() {
		
		matcher = HasGlobalErrors.hasGlobalErrors();
		item.reject("error.name");
		
		assertTrue(matcher.matchesSafely(item), "should match");
	}
	
	@Test
	public void shouldNotMatchIfCodeIsNonNullAndGivenErrorDoesNotHaveGlobalErrors() {
		
		matcher = HasGlobalErrors.hasGlobalErrors("error.name");
		
		assertFalse(matcher.matchesSafely(item), "should not match");
	}
	
	@Test
	public void shouldNotMatchIfCodeIsNonNullButNotContainedInGivenErrors() {
		
		matcher = HasGlobalErrors.hasGlobalErrors("error.name");
		item.reject("error.null");
		item.reject("invalid.uuid");
		
		assertFalse(matcher.matchesSafely(item), "should not match");
	}
	
	@Test
	public void shouldMatchIfCodeIsNonNullAndContainedInGivenErrors() {
		
		matcher = HasGlobalErrors.hasGlobalErrors("error.name");
		item.reject("error.null");
		item.reject("invalid.uuid");
		item.reject("error.name");
		
		assertTrue(matcher.matchesSafely(item), "should match");
	}
}
