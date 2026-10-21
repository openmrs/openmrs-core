/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.filter.initialization;

import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.junit.jupiter.api.Test;
import org.openmrs.api.APIAuthenticationException;

import com.sun.net.httpserver.HttpServer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests {@link TestInstallUtil}.
 */
public class TestInstallUtilTest {

	@Test
	public void getResourceInputStream_shouldDenyCredentialsTheRemoteServerRejectsWithAnAPIAuthenticationException()
	        throws Exception {
		// what this threw before 3.0.0, which is now also the AccessDeniedException InitializationFilter catches
		HttpServer remoteServer = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
		remoteServer.createContext("/", exchange -> {
			exchange.getRequestBody().readAllBytes();
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_UNAUTHORIZED, -1);
			exchange.close();
		});
		remoteServer.start();
		try {
			String url = "http://" + remoteServer.getAddress().getHostString() + ":" + remoteServer.getAddress().getPort()
			        + "/verifycredentials.htm";

			APIAuthenticationException denial = assertThrows(APIAuthenticationException.class,
			    () -> TestInstallUtil.getResourceInputStream(url, "admin", "wrong"));
			assertEquals("Invalid username or password", denial.getMessage());
		} finally {
			remoteServer.stop(0);
		}
	}
}
