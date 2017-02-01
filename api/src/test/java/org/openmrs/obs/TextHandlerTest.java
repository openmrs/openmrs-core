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
import org.mockito.Mock;
import org.openmrs.Obs;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.obs.handler.AbstractHandler;
import org.openmrs.obs.handler.TextHandler;
import org.openmrs.util.OpenmrsUtil;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({AbstractHandler.class, OpenmrsUtil.class, Context.class})
public class TextHandlerTest {

    @Mock
    private AdministrationService administrationService;

    @Test
    public void shouldReturnSupportedViews() {
        TextHandler handler = new TextHandler();
        String[] actualViews = handler.getSupportedViews();
        String[] expectedViews = { ComplexObsHandler.TEXT_VIEW, ComplexObsHandler.RAW_VIEW, ComplexObsHandler.URI_VIEW };

        assertArrayEquals(actualViews, expectedViews);
    }

    @Test
    public void shouldSupportRawView() {
        TextHandler handler = new TextHandler();

        assertTrue(handler.supportsView(ComplexObsHandler.RAW_VIEW));
        assertTrue(handler.supportsView(ComplexObsHandler.TEXT_VIEW));
        assertTrue(handler.supportsView(ComplexObsHandler.URI_VIEW));
    }

    @Test
    public void shouldNotSupportOtherViews() {
        TextHandler handler = new TextHandler();

        assertFalse(handler.supportsView(ComplexObsHandler.HTML_VIEW));
        assertFalse(handler.supportsView(ComplexObsHandler.PREVIEW_VIEW));
        assertFalse(handler.supportsView(ComplexObsHandler.TITLE_VIEW));
        assertFalse(handler.supportsView(""));
        assertFalse(handler.supportsView((String) null));
    }

    @Test
    public void shouldSaveMimeTypeInFilenameAndBeAbleToReadIt() throws Exception {
        final String filename = "TestingComplexObsSaving.xml";
        final String mimetype = "text/plain";

        TextHandler handler = new TextHandler();
        File dataFile = new File(getClass().getClassLoader().getResource("TestingApplicationContext.xml").toURI());

        ComplexData complexData = new ComplexData(filename, dataFile);
        complexData.setMimeType(mimetype);

        Obs obs = new Obs();
        obs.setValueComplex(filename);
        obs.setComplexData(complexData);

        // Mocked methods
        mockStatic(Context.class);
        when(Context.getAdministrationService()).thenReturn(administrationService);
        when(administrationService.getGlobalProperty(anyString())).thenReturn(dataFile.getParent());
        mockStatic(OpenmrsUtil.class);
        when(OpenmrsUtil.getDirectoryInApplicationDataDirectory(any())).thenReturn(dataFile.getParentFile());

        // Execute save
        handler.saveObs(obs);

        // Get observation
        Obs complexObs = handler.getObs(obs, "RAW_VIEW");
        assertThat(complexObs.getComplexData().getMimeType(), is(mimetype));
    }

    @Test
    public void shouldGuessMimeTypeIfNotSpecified() throws Exception {
        final String filename = "TestingComplexObsSaving.xml";
        final String mimetype = "application/xml";

        TextHandler handler = new TextHandler();
        File dataFile = new File(getClass().getClassLoader().getResource("TestingApplicationContext.xml").toURI());

        ComplexData complexData = new ComplexData(filename, dataFile);
        // MIME type not specified
        complexData.setMimeType(null);

        Obs obs = new Obs();
        obs.setValueComplex(filename);
        obs.setComplexData(complexData);

        // Mocked methods
        mockStatic(Context.class);
        when(Context.getAdministrationService()).thenReturn(administrationService);
        when(administrationService.getGlobalProperty(anyString())).thenReturn(dataFile.getParent());
        mockStatic(OpenmrsUtil.class);
        when(OpenmrsUtil.getDirectoryInApplicationDataDirectory(any())).thenReturn(dataFile.getParentFile());

        // Execute save
        handler.saveObs(obs);

        // Get observation
        Obs complexObs = handler.getObs(obs, "RAW_VIEW");
        assertThat(complexObs.getComplexData().getMimeType(), is(mimetype));
    }

}
