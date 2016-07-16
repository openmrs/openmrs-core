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
import org.openmrs.obs.handler.TextHandler;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;


public class TextHandlerTest {

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
}
