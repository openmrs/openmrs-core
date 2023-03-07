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

import org.junit.Test;
import org.openmrs.Obs;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_8;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ObsController2_2Test extends MainResourceControllerTest {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    /**
     * @see MainResourceControllerTest#getURI()
     */
    @Override
    public String getURI() {
        return "obs";
    }

    @Override
    public long getAllCount() {
        return Context.getObsService().getObservationCount(null, true);
    }

    /**
     * @see MainResourceControllerTest#getUuid()
     */
    @Override
    public String getUuid() {
        return RestTestConstants1_8.OBS_UUID;
    }

    /**
     * @see MainResourceControllerTest#shouldGetAll()
     */
    @Override
    @Test(expected = ResourceDoesNotSupportOperationException.class)
    public void shouldGetAll() throws Exception {
        super.shouldGetAll();
    }

    @Test
    public void shouldUpdateObs() throws Exception {
        ObsService obsService = Context.getObsService();
        final Double UPDATED_VALUE = 10.0 ;

        Obs existingObs = obsService.getObsByUuid("2f616900-5e7c-4667-9a7f-dcb260abf1de");
        assertNotNull(existingObs);
        assertNull(obsService.getRevisionObs(existingObs));

        String json = "{ \"value\":\"" + UPDATED_VALUE + "\"}";

        handle(newPostRequest(getURI() + "/" + existingObs.getUuid(), json));
        Obs updatedObs = obsService.getRevisionObs(existingObs);

        assertNotNull(updatedObs);
        assertEquals(UPDATED_VALUE, updatedObs.getValueNumeric());
    }
}
