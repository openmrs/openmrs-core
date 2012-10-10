/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * This is a wrapper around the Url class. It's main purpose is to abstract URL
 * so that you can mock http calls as the URL class cannot be mocked.
 */
public class HttpUrl {
	
	private final URL url;
	
	public HttpUrl(String url) throws MalformedURLException {
		if (url == null) {
			throw new MalformedURLException("Url cannot be null");
		} else if (!url.startsWith("http://")) {
			throw new MalformedURLException("Not a valid http url");
		}
		
		this.url = new URL(url);
	}
	
	public HttpURLConnection openConnection() throws IOException {
		return (HttpURLConnection) url.openConnection();
	}
	
	@Override
	public String toString() {
		return url.toString();
	}
}
