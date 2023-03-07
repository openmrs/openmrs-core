/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs2_0;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.api.context.Context;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
public class SearchIndexController2_0Test {
	
	private SearchIndexController2_0 controller = new SearchIndexController2_0();
	
	@Test
	public void updateSearchIndex_shouldUpdateTheEntireSearchIndex() throws Exception {
		PowerMockito.mockStatic(Context.class);
		PowerMockito.doNothing().when(Context.class, "updateSearchIndex");
		controller.updateSearchIndex(null);
		
		PowerMockito.verifyStatic();
		Context.updateSearchIndex();
	}
	
	@Test
	public void updateSearchIndex_shouldUpdateTheEntireSearchIndexAsynchronously() throws Exception {
		PowerMockito.mockStatic(Context.class);
		PowerMockito.doNothing().when(Context.class, "updateSearchIndex");
		controller.updateSearchIndex("{\"async\": true}");
		
		PowerMockito.verifyStatic();
		Context.updateSearchIndexAsync();
	}
	
}
