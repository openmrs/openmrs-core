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

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.Errors;

/**
 * Matcher to assert {@code FieldError} in {@code Errors} used in Spring validation.
 * <p>
 * Usage examples:
 * <ul>
 * <li>Has one or more {@code FieldError}: assertThat(errors, hasFieldErrors());</li>
 * <li>Has one or more {@code FieldError} in the specified field: assertThat(errors,
 * hasFieldErrors("givenName"));</li>
 * <li>Has {@code FieldError} in the specified field of specified code: assertThat(errors,
 * hasFieldErrors("givenName", "GivenName.invalid"));</li>
 * </ul>
 * </p>
 * 
 * @see org.springframework.validation.Errors;
 * @see org.springframework.validation.FieldError;
 * @since 2.2.0
 */
public final class HasFieldErrors extends TypeSafeMatcher<Errors> {
	
	private final String field;
	
	private final String code;
	
	private HasFieldErrors(String field, String code) {
		this.field = field;
		this.code = code;
	}
	
	@Override
	public void describeTo(Description description) {
		if (field == null) {
			description.appendText("to have field errors");
		} else if (code == null) {
			description.appendText("to have field errors for '" + field + "'");
		} else {
			description.appendText("to have field errors for '" + field + "' and code '" + code + "'");
		}
	}
	
	@Override
	protected boolean matchesSafely(Errors item) {
		if (field == null) {
			return item.hasFieldErrors();
		} else if (code == null) {
			return item.hasFieldErrors(field);
		} else {
			return item.getFieldErrors(field).stream().map(DefaultMessageSourceResolvable::getCode).anyMatch(code::equals);
		}
	}
	
	public static HasFieldErrors hasFieldErrors() {
		return new HasFieldErrors(null, null);
	}
	
	public static HasFieldErrors hasFieldErrors(String field) {
		return new HasFieldErrors(field, null);
	}
	
	public static HasFieldErrors hasFieldErrors(String field, String code) {
		return new HasFieldErrors(field, code);
	}
}
