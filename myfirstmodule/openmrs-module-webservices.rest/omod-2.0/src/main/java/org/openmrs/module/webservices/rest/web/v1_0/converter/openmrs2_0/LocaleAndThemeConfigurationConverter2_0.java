/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.converter.openmrs2_0;

import org.openmrs.annotation.Handler;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingConverter;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ConversionException;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.v1_0.wrapper.LocaleAndThemeConfiguration;

@Handler(supports = LocaleAndThemeConfiguration.class, order = 0)
public class LocaleAndThemeConfigurationConverter2_0 extends BaseDelegatingConverter<LocaleAndThemeConfiguration> {

	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("defaultLocale");
		description.addProperty("defaultTheme");
		return description;
	}

	@Override
	public LocaleAndThemeConfiguration newInstance(String type) {
		return new LocaleAndThemeConfiguration();
	}

	@Override
	public LocaleAndThemeConfiguration getByUniqueId(String string) {
		throw new ResourceDoesNotSupportOperationException();
	}

	@Override
	public SimpleObject asRepresentation(LocaleAndThemeConfiguration delegate, Representation rep) throws ConversionException {
		SimpleObject configuration = new SimpleObject();
		configuration.add("defaultLocale", delegate.getDefaultLocale());
		configuration.add("defaultTheme", delegate.getDefaultTheme());
		return configuration;
	}
}
