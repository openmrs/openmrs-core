/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.context;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.api.context.Authenticated;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.Credentials;
import org.openmrs.api.context.TestUsernameCredentials;
import org.openmrs.web.test.BaseWebContextSensitiveTest;

/**
 * A Context loaded with a test Spring config that overrides the default auth scheme.
 * 
 * @see Context
 */
public class AltAuthSchemeContextTest extends BaseWebContextSensitiveTest {

  /**
   * @see Context#authenticate(Credentials)
   */
  @Test
  public void authenticate_shouldAuthenticateUserWithAlternateScheme() {
    // replay
    Authenticated authenticated = Context.authenticate(new TestUsernameCredentials("admin"));

    // verif
    Assert.assertEquals("test-scheme", authenticated.getAuthenticationScheme());
    Assert.assertEquals(Context.getAuthenticatedUser().getUuid(), authenticated.getUser().getUuid());
    Assert.assertEquals("admin", Context.getAuthenticatedUser().getUsername());
  }
}