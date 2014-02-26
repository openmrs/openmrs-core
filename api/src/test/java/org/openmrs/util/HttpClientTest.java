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

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class HttpClientTest {
	
	private HttpClient client;
	
	private HttpURLConnection connection;
	
	@Before
	public void setUp() throws Exception {
		HttpUrl url = mock(HttpUrl.class);
		client = new HttpClient(url);
		connection = mock(HttpURLConnection.class);
		when(url.openConnection()).thenReturn(connection);
	}
	
	@Test
	public void post_shouldPostUrlParametersAndGetResponse() throws Exception {
		HashMap<String, String> parameters = new HashMap<String, String>();
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
		
		assertThat(stream.toString(), is("&two=two&one=one"));
		assertThat(response, containsString("response"));
	}
}
