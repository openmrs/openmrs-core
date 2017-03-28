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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.TreeMap;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;

public class HttpClientTest {
	
	private HttpClient client;
	
	private HttpURLConnection connection;
	
	@Before
	public void setUp() throws IOException {
		HttpUrl url = mock(HttpUrl.class);
		client = new HttpClient(url);
		connection = mock(HttpURLConnection.class);
		when(url.openConnection()).thenReturn(connection);
	}
	
	@Test
	public void post_shouldPostUrlParametersAndGetResponse() throws IOException {
		Map<String, String> parameters = new TreeMap<>();
		parameters.put("one", "one");
		parameters.put("two", "two");
		
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		when(connection.getOutputStream()).thenReturn(stream);
		when(connection.getInputStream()).thenReturn(new ByteArrayInputStream("response".getBytes()));
		
		String response = client.post(parameters);
		
		verify(connection).setDoOutput(true);
		verify(connection).setDoInput(true);
		verify(connection).setRequestMethod("POST");
		verify(connection).setRequestProperty("Content-Length", String.valueOf(16));
		verify(connection).setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		
		assertThat(stream.toString(), is("&one=one&two=two"));
		assertThat(response, CoreMatchers.containsString("response"));
	}
}
