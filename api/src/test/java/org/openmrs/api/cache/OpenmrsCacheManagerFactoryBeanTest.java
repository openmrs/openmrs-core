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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;

import java.util.Collection;

import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;

public class OpenmrsCacheManagerFactoryBeanTest extends BaseContextSensitiveTest{
    
    @Autowired
    CacheManager cacheManager;
    
    @Test
    public void shouldContainsThreeCacheConfigurations(){
        Collection<String> cacheNames = cacheManager.getCacheNames();
        assertThat(cacheNames.size(), is(3));
        cacheNames.forEach(cn ->
                assertThat(cn, anyOf(is("conceptDatatype"), is("subscription"), is("userSearchLocales"))));
    }
}
