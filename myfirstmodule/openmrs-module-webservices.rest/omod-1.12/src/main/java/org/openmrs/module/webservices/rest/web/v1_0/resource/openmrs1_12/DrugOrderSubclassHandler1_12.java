/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_12;

import org.openmrs.DrugOrder;
import org.openmrs.module.webservices.rest.web.annotation.SubClassHandler;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_10.DrugOrderSubclassHandler1_10;

/**
 * Exposes the {@link org.openmrs.DrugOrder} subclass as a type in
 * {@link org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_10.DrugOrderSubclassHandler1_10}
 */
@SubClassHandler(supportedClass = DrugOrder.class, supportedOpenmrsVersions = { "1.12.* - 9.*" })
public class DrugOrderSubclassHandler1_12 extends DrugOrderSubclassHandler1_10 {
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler#getRepresentationDescription(org.openmrs.module.webservices.rest.web.representation.Representation)
	 */
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		DelegatingResourceDescription d = super.getRepresentationDescription(rep);
		if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
			d.addProperty("drugNonCoded");
		}
		return d;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler#getCreatableProperties()
	 */
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription d = super.getCreatableProperties();
		d.addProperty("drugNonCoded");
		return d;
	}
}
