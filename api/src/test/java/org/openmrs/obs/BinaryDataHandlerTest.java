/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.obs;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.Obs;
import org.openmrs.obs.handler.AbstractHandler;
import org.openmrs.obs.handler.BinaryDataHandler;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;


@RunWith(PowerMockRunner.class)
@PrepareForTest(AbstractHandler.class)
public class BinaryDataHandlerTest {

    @Test
    public void shouldReturnSupportedViews() {
        BinaryDataHandler handler = new BinaryDataHandler();
        String[] actualViews = handler.getSupportedViews();
        String[] expectedViews = { ComplexObsHandler.RAW_VIEW };

        assertArrayEquals(actualViews, expectedViews);
    }

    @Test
    public void shouldSupportRawView() {
        BinaryDataHandler handler = new BinaryDataHandler();

        assertTrue(handler.supportsView(ComplexObsHandler.RAW_VIEW));
    }

    @Test
    public void shouldNotSupportOtherViews() {
        BinaryDataHandler handler = new BinaryDataHandler();

        assertFalse(handler.supportsView(ComplexObsHandler.HTML_VIEW));
        assertFalse(handler.supportsView(ComplexObsHandler.PREVIEW_VIEW));
        assertFalse(handler.supportsView(ComplexObsHandler.TEXT_VIEW));
        assertFalse(handler.supportsView(ComplexObsHandler.TITLE_VIEW));
        assertFalse(handler.supportsView(ComplexObsHandler.URI_VIEW));
        assertFalse(handler.supportsView(""));
        assertFalse(handler.supportsView(null));
    }

    @Test
    public void shouldSupportMimeTypesForComplexData() throws Exception {
        BinaryDataHandler handler = new BinaryDataHandler();

        File file = new File(getClass().getClassLoader().getResource("TestingApplicationContext.xml").getFile());
        Obs obs = new Obs();
        obs.setValueComplex("TestingApplicationContext.xml");

        mockStatic(AbstractHandler.class);
        when(AbstractHandler.getComplexDataFile(obs)).thenReturn(file);

        Obs complexObs = handler.getObs(obs, "RAW_VIEW");
        assertThat(complexObs.getComplexData().getMimeType(), is("application/xml"));
    }
}
