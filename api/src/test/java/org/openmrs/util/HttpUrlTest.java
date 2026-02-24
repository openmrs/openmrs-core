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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.MalformedURLException;

import org.junit.jupiter.api.Test;

public class HttpUrlTest {
	
	
	@Test
	public void constructor_shouldNotThrowExceptionIfItIsAnHttpUrl() throws MalformedURLException {
		HttpUrl url = new HttpUrl("http://93.184.216.34");
		assertThat(url, notNullValue());
	}

	@Test
	public void constructor_shouldNotThrowExceptionIfItIsAnHttpsUrl() throws MalformedURLException {
		HttpUrl url = new HttpUrl("https://93.184.216.34");
		assertThat(url, notNullValue());
	}
	
	@Test
	public void constructor_shouldThrowMalformedUrlExceptionIfTheUrlDoesNotHaveHttp() throws MalformedURLException {
		MalformedURLException exception = assertThrows(MalformedURLException.class, () -> new HttpUrl("not_http"));
		assertThat(exception.getMessage(), is("Not a valid http(s) url"));
	}
	
	@Test
	public void constructor_shouldNotAllowNullUrls() throws MalformedURLException {
		MalformedURLException exception = assertThrows(MalformedURLException.class, () -> new HttpUrl(null));
		assertThat(exception.getMessage(), is("Url cannot be null"));
	}
	
	@Test
	public void toString_shouldReturnUrl() throws MalformedURLException {
		assertThat(new HttpUrl("http://93.184.216.34").toString(), is("http://93.184.216.34"));
	}

	@Test
	public void toString_shouldReturnUrlHttps() throws MalformedURLException {
		assertThat(new HttpUrl("https://93.184.216.34").toString(), is("https://93.184.216.34"));
	}
}
