/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.propertyeditor;

import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.Provider;
import org.openmrs.api.ProviderService;
import org.springframework.beans.factory.annotation.Autowired;

public class ProviderEditorTest extends BasePropertyEditorTest<Provider, ProviderEditor> {
	
	private static final Integer EXISTING_ID = 1;
	
	@Autowired
	private ProviderService providerService;
	
	@Override
	protected ProviderEditor getNewEditor() {
		return new ProviderEditor();
	}
	
	@Override
	protected Provider getExistingObject() {
		return providerService.getProvider(EXISTING_ID);
	}
	
	@Override
	@Test
	@Ignore("see TRUNK-5153 that ProviderDAO throws different exception")
	public void shouldSetTheEditorValueToNullIfGivenIdDoesNotExist() {
	}
}
