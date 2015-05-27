/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.maintenance;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.web.test.BaseWebContextSensitiveTest;

import java.util.Map;

/**
 * Tests the {@link SearchIndexController} controller
 */
public class SearchIndexControllerTest  extends BaseWebContextSensitiveTest {

    private SearchIndexController controller;

    @Before
    public void before() {
        createController();
    }

    private void createController() {
        controller = new SearchIndexController();
    }

    /**
     * @verifies return the search index view
     * @see SearchIndexController#showPage()
     */
    @Test
    public void showPage_shouldReturnTheSearchIndexView() throws Exception {
        String viewName = controller.showPage();
        assertEquals("admin/maintenance/searchIndex", viewName);
    }

    /**
     * @verifies return true for success if the update does not fail
     * @see SearchIndexController#rebuildSearchIndex()
     */
    @Test
    public void rebuildSearchIndex_shouldReturnTrueForSuccessIfTheUpdateDoesNotFail() throws Exception {
        Map<String, Object> response = controller.rebuildSearchIndex();
        assertEquals(true, response.get("success"));
    }
}
