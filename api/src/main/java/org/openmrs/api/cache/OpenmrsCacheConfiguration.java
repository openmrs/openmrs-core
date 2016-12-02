/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.cache;

import java.util.HashMap;
import java.util.Set;

public class OpenmrsCacheConfiguration {
    private HashMap<String, String> cacheProperties = new HashMap<>();

    public void addProperty(String key, String value){
        cacheProperties.put(key, value);
    }

    public String getProperty(String key){
        return cacheProperties.get(key);
    }

    public Set<String> getAllKeys(){
        return cacheProperties.keySet();
    }
}
