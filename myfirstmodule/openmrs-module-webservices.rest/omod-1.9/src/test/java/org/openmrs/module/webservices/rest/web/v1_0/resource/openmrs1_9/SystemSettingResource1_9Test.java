/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_9;

import org.junit.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_9;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SystemSettingResource1_9Test extends BaseDelegatingResourceTest<SystemSettingResource1_9, GlobalProperty> {
	
	@Override
	public GlobalProperty newObject() {
		return Context.getAdministrationService().getGlobalPropertyByUuid(getUuidProperty());
	}
	
	@Override
	public void validateDefaultRepresentation() throws Exception {
		super.validateDefaultRepresentation();
		assertPropPresent("property");
		assertPropPresent("value");
		assertPropPresent("description");
		assertPropEquals("display", getDisplayProperty());
	}
	
	@Override
	public void validateFullRepresentation() throws Exception {
		super.validateFullRepresentation();
		assertPropPresent("property");
		assertPropPresent("value");
		assertPropPresent("description");
		assertPropEquals("display", getDisplayProperty());
		assertPropPresent("datatypeClassname");
		assertPropPresent("datatypeConfig");
		assertPropPresent("preferredHandlerClassname");
		assertPropPresent("handlerConfig");
	}
	
	@Override
	public String getDisplayProperty() {
		return "Locale - Allowed List = en";
	}
	
	@Override
	public String getUuidProperty() {
		return RestTestConstants1_9.GLOBAL_PROPERTY_UUID;
	}
	
	@Test
	public void shouldAddPropertyFieldToCreatableProperties() {
		SystemSettingResource1_9 resource = new SystemSettingResource1_9();
		DelegatingResourceDescription creatableProperties = resource.getCreatableProperties();
		assertTrue(creatableProperties.getProperties().containsKey("property"));
	}
	
	@Test
	public void shouldAddCommonPropertiesToCreatableProperties() {
		SystemSettingResource1_9 resource = new SystemSettingResource1_9();
		DelegatingResourceDescription creatableProperties = resource.getCreatableProperties();
		assertTrue(creatableProperties.getProperties().containsKey("description"));
		assertTrue(creatableProperties.getProperties().containsKey("datatypeClassname"));
		assertTrue(creatableProperties.getProperties().containsKey("datatypeConfig"));
		assertTrue(creatableProperties.getProperties().containsKey("preferredHandlerClassname"));
		assertTrue(creatableProperties.getProperties().containsKey("handlerConfig"));
		assertTrue(creatableProperties.getProperties().containsKey("value"));
	}
	
	@Test
	public void shouldAddCommonPropertiesToUpdatableProperties() {
		SystemSettingResource1_9 resource = new SystemSettingResource1_9();
		DelegatingResourceDescription updatableProperties = resource.getUpdatableProperties();
		assertTrue(updatableProperties.getProperties().containsKey("description"));
		assertTrue(updatableProperties.getProperties().containsKey("datatypeClassname"));
		assertTrue(updatableProperties.getProperties().containsKey("datatypeConfig"));
		assertTrue(updatableProperties.getProperties().containsKey("preferredHandlerClassname"));
		assertTrue(updatableProperties.getProperties().containsKey("handlerConfig"));
		assertTrue(updatableProperties.getProperties().containsKey("value"));
	}
	
	@Test
	public void shouldRemovePropertyFromUpdatableProperties() {
		SystemSettingResource1_9 resource = new SystemSettingResource1_9();
		DelegatingResourceDescription updatableProperties = resource.getUpdatableProperties();
		assertFalse(updatableProperties.getProperties().containsKey("property"));
	}
}
