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
import static org.hamcrest.Matchers.is;

import java.util.List;

import net.sf.ehcache.config.CacheConfiguration;
import org.junit.jupiter.api.Test;

public class CachePropertiesUtilTest {

    @Test
    public void shouldReturnCacheConfigsFromPropertiesFile(){
        List<CacheConfiguration> cacheConfigurations = CachePropertiesUtil.getCacheConfigurations();
        assertThat(cacheConfigurations.size(), is(2));
        assertThat(cacheConfigurations.get(0).getTimeToIdleSeconds(), is(300L));
    }
}
