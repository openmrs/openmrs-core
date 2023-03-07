/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.resource.impl;

import org.openmrs.OpenmrsData;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.annotation.RepHandler;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.response.ConversionException;

/**
 * Subclass of {@link DelegatingCrudResource} with helper methods specific to {@link OpenmrsData}
 * 
 * @param <T>
 */
public abstract class DataDelegatingCrudResource<T extends OpenmrsData> extends DelegatingCrudResource<T> {
	
	@RepHandler(RefRepresentation.class)
	public SimpleObject asRef(T delegate) throws ConversionException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("uuid");
		description.addProperty("display");
		if (delegate.isVoided())
			description.addProperty("voided");
		description.addSelfLink();
		return convertDelegateToRepresentation(delegate, description);
	}
	
	@RepHandler(DefaultRepresentation.class)
	public SimpleObject asDefaultRep(T delegate) throws Exception {
		SimpleObject ret = new SimpleObject();
		ret.put("uuid", delegate.getUuid());
		ret.put("display", delegate.toString());
		ret.put("voided", delegate.isVoided());
		ret.put("links", "[ All Data resources need to define their representations ]");
		return ret;
	}
	
	@Override
	public boolean isVoidable() {
		return true;
	}
}
