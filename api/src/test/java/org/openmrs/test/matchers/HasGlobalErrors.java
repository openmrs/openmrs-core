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
import org.springframework.validation.Errors;

/**
 * Matcher to assert global errors in {@code Errors} used in Spring validation.
 * <p>
 * Usage example: <pre> {@code
 * Errors item = new BindException(myObject, "myObject");
 * item.reject("error.name");
 * }
 * </pre>
 * <ul>
 * <li>Has one or more global error: {@code assertThat(errors, hasGlobalErrors());}</li>
 * <li>Has global error of specific code:
 * {@code assertThat(errors, hasGlobalErrors("error.name"));}</li>
 * </ul>
 * </p>
 * 
 * @see org.springframework.validation.Errors;
 * @since 2.2.0
 */
public final class HasGlobalErrors extends TypeSafeMatcher<Errors> {
	
	private final String code;
	
	private HasGlobalErrors() {
		this(null);
	}
	
	private HasGlobalErrors(String code) {
		this.code = code;
	}
	
	@Override
	public void describeTo(Description description) {
		if (code == null) {
			description.appendText("to have global errors");
		} else {
			description.appendText("to have global error of code '" + code + "'");
		}
	}
	
	@Override
	protected boolean matchesSafely(Errors item) {
		if (code == null) {
			return item.hasGlobalErrors();
		} else {
			return item.getGlobalErrors().stream().filter(c -> code.equals(c.getCode())).findFirst().isPresent();
		}
	}
	
	/**
	 * Creates a matcher to assert global errors.
	 */
	public static HasGlobalErrors hasGlobalErrors() {
		return new HasGlobalErrors();
	}
	
	/**
	 * Creates a matcher to assert global errors of a specific code.
	 */
	public static HasGlobalErrors hasGlobalErrors(String code) {
		return new HasGlobalErrors(code);
	}
}
