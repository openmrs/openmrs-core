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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;


/**
 * A controller for the search index
 */
@Controller
public class SearchIndexController {

    protected final Log log = LogFactory.getLog(getClass());

    /**
     * @should return the search index view
     * @return the searchIndex view
     */
    @RequestMapping(method = RequestMethod.GET, value = "admin/maintenance/searchIndex")
    public String showPage() {
        return "admin/maintenance/searchIndex";
    }


    /**
     * @should return true for success if the update does not fail
     * @should return false for success if a RuntimeException is thrown
     * @return a marker indicating success
     */
    @RequestMapping(method = RequestMethod.POST, value = "admin/maintenance/rebuildSearchIndex")
    public @ResponseBody Map<String, Object> rebuildSearchIndex() {
        boolean success = true;
        Map<String, Object> results = new HashMap<String, Object>();
        log.debug("rebuilding search index");
        try {
            Context.updateSearchIndex();
        } catch (RuntimeException e) {
            success = false;
        }

        results.put("success", success);
        return results;
    }
}
