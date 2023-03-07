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

import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.empty;

public class SessionIT extends ITBase {
	
	@Test
	public void shouldBeUnauthenticatedByDefault() throws Exception {
		given().auth().none().when().get("session").then().body("authenticated", is(false));
	}
	
	@Test
	public void shouldAuthenticateAsAdmin() throws Exception {
		given().auth().preemptive().basic(ADMIN_USERNAME, ADMIN_PASSWORD).get("session").then()
		        .body("authenticated", is(true)).body("user.systemId", is("admin")).body("sessionId", not(empty()));
	}
}
