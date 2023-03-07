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

import org.openmrs.ImplementationId;
import org.openmrs.annotation.Handler;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingConverter;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;

@Handler(supports = ImplementationId.class, order = 0)
public class ImplementationIdConverter2_0 extends BaseDelegatingConverter<ImplementationId> {

	@Override
	public ImplementationId newInstance(String type) {
		return new ImplementationId();
	}

	@Override
	public ImplementationId getByUniqueId(String string) {
		throw new ResourceDoesNotSupportOperationException();
	}

	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("name");
		description.addProperty("description");
		description.addProperty("implementationId");
		description.addProperty("passphrase");
		return description;
	}

	@Override
	public SimpleObject asRepresentation(ImplementationId delegate, Representation rep) {
		SimpleObject allergenReactionObject = new SimpleObject();
		allergenReactionObject.add("name", delegate.getName());
		allergenReactionObject.add("description", delegate.getDescription());
		allergenReactionObject.add("implementationId", delegate.getImplementationId());
		allergenReactionObject.add("passphrase", delegate.getPassphrase());
		return allergenReactionObject;
	}
}
