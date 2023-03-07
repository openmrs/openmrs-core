/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest;

import io.restassured.RestAssured;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.BeforeClass;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static io.restassured.RestAssured.basic;

public abstract class ITBase {
	
	private static final Object serverStartupLock = new Object();
	
	private static boolean serverStarted = false;
	
	public static final String ADMIN_USERNAME;
	
	public static final String ADMIN_PASSWORD;
	
	public static final URI TEST_URL;
	
	static {
		String testUrlProperty = System.getProperty("testUrl", "http://admin:Admin123@localhost:8080/openmrs");
		try {
			TEST_URL = new URI(testUrlProperty);
			RestAssured.baseURI = TEST_URL.getScheme() + "://" + TEST_URL.getHost();
			RestAssured.port = TEST_URL.getPort();
			RestAssured.basePath = TEST_URL.getPath() + "/ws/rest/v1";
			String[] userInfo = TEST_URL.getUserInfo().split(":");
			ADMIN_USERNAME = userInfo[0];
			ADMIN_PASSWORD = userInfo[1];
			RestAssured.authentication = basic(userInfo[0], userInfo[1]);
		}
		catch (URISyntaxException e) {
			throw new RuntimeException("Invalid uri: " + testUrlProperty, e);
		}
	}
	
	@BeforeClass
	public static void waitForServerToStart() {
		synchronized (serverStartupLock) {
			if (!serverStarted) {
				final long time = System.currentTimeMillis();
				final int timeout = 300000;
				final int retryAfter = 10000;
				
				final RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(retryAfter)
				        .setConnectTimeout(retryAfter).build();
				
				final String startupUri = TEST_URL.getScheme() + "://" + TEST_URL.getHost() + ":"
				        + TEST_URL.getPort() + TEST_URL.getPath();
				System.out.println("Waiting for server at " + startupUri + " for " + timeout / 1000 + " more seconds...");
				
				while (System.currentTimeMillis() - time < timeout) {
					try {
						final HttpClient client = HttpClientBuilder.create().disableAutomaticRetries().build();
						final HttpGet sessionGet = new HttpGet(startupUri);
						sessionGet.setConfig(requestConfig);
						final HttpClientContext context = HttpClientContext.create();
						final HttpResponse response = client.execute(sessionGet, context);
						
						int status = response.getStatusLine().getStatusCode();
						if (status >= 400) {
							throw new RuntimeException(status + " " + response.getStatusLine().getReasonPhrase());
						}
						
						URI finalUri = sessionGet.getURI();
						List<URI> redirectLocations = context.getRedirectLocations();
						if (redirectLocations != null) {
							finalUri = redirectLocations.get(redirectLocations.size() - 1);
						}
						
						String finalUriString = finalUri.toString();
						if (!finalUriString.contains("initialsetup")) {
							serverStarted = true;
							return;
						}
					}
					catch (IOException e) {
						System.out.println(e.toString());
					}
					
					try {
						System.out.println("Waiting for "
						        + (timeout - (System.currentTimeMillis() - time)) / 1000 + " more seconds...");
						Thread.sleep(retryAfter);
					}
					catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
				
				throw new RuntimeException("Server startup took longer than 5 minutes!");
			}
		}
	}
	
}
