/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs2_2;

import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Person;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.v1_0.RestTestConstants2_2;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;

/**
 * Tests CRUD operations for {@link Person}s via web service calls
 */
public class PersonController2_2Test extends MainResourceControllerTest {

    private PersonService service;

    @Override
    public String getURI() {
        return "person";
    }

    @Override
    public String getUuid() {
        return RestTestConstants2_2.PERSON_UUID;
    }

    @Override
    public long getAllCount() {
        return 0;
    }

    @Before
    public void before() {
        this.service = Context.getPersonService();
    }

    @Override
    @Test(expected = ResourceDoesNotSupportOperationException.class)
    public void shouldGetAll() throws Exception {
        super.shouldGetAll();
    }

    @Test
    public void shouldEditAPerson() throws Exception {
        Person person = service.getPersonByUuid(getUuid());
        assertFalse("F".equals(person.getGender()));
        assertFalse(person.isDead());
        assertNull(person.getCauseOfDeathNonCoded());
        String json = "{\"gender\":\"F\",\"dead\":true, \"causeOfDeathNonCoded\":\"Eating_Banana\"}";
        SimpleObject response = deserialize(handle(newPostRequest(getURI() + "/" + getUuid(), json)));
        assertNotNull(response);
        Object responsePersonContents = PropertyUtils.getProperty(response, "person");
        assertNotNull(responsePersonContents);
        assertTrue("F".equals(PropertyUtils.getProperty(responsePersonContents, "gender").toString()));
        assertEquals("F", person.getGender());
        assertTrue(person.isDead());
        assertNotNull(person.getCauseOfDeathNonCoded());
    }
}
