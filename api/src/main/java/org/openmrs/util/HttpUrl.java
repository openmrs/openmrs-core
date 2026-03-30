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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

/**
 * This is a wrapper around the Url class. It's main purpose is to abstract URL so that you can mock
 * http calls as the URL class cannot be mocked.
 */
public class HttpUrl {

	private final URL url;

	public HttpUrl(String url) throws MalformedURLException {
		if (url == null) {
			throw new MalformedURLException("Url cannot be null");
		} else if (!url.toLowerCase(Locale.ROOT).startsWith("http://")
		        && !url.toLowerCase(Locale.ROOT).startsWith("https://")) {
			throw new MalformedURLException("Not a valid http(s) url");
		}
		
		URL parsed = new URL(url);
		URL validated;
		try {
			// validateUrlForServerRequest resolves DNS once and returns a URL with the numeric
			// IP as host, preventing DNS-rebinding / TOCTOU attacks.
			validated = Security.validateUrlForServerRequest(parsed);
		}
		catch (SecurityException e) {
			MalformedURLException exception = new MalformedURLException(e.getMessage());
			exception.initCause(e);
			throw exception;
		}
		this.url = validated;
	}

	public HttpURLConnection openConnection() throws IOException {
		return (HttpURLConnection) url.openConnection();
	}

	@Override
	public String toString() {
		return url.toString();
	}
}
