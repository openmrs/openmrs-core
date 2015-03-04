/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.MalformedURLException;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

public class HttpUrlTest {
	
	public @Rule
	ExpectedException exception = ExpectedException.none();
	
	@Test
	public void constructor_shouldNotThrowExceptionIfItIsAnHttpUrl() throws Exception {
		HttpUrl url = new HttpUrl("http://something");
		assertThat(url, notNullValue());
	}
	
	@Test
	public void constructor_shouldThrowMalformedUrlExceptionIfTheUrlDoesNotHaveHttp() throws Exception {
		exception.expect(MalformedURLException.class);
		exception.expectMessage("Not a valid http url");
		new HttpUrl("not_http");
	}
	
	@Test
	public void constructor_shouldNotAllowNullUrls() throws Exception {
		exception.expect(MalformedURLException.class);
		exception.expectMessage("Url cannot be null");
		new HttpUrl(null);
	}
	
	@Test
	public void toString_shouldReturnUrl() throws Exception {
		assertThat(new HttpUrl("http://something").toString(), is("http://something"));
	}
}
