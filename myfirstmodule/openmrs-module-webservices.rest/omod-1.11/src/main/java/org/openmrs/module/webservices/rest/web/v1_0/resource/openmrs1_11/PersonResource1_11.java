/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_11;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.BooleanProperty;
import org.openmrs.Person;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_10.PersonResource1_10;

/**
 * {@link Resource} for Person, supporting standard CRUD operations
 */
@Resource(name = RestConstants.VERSION_1 + "/person", order = 1, supportedClass = Person.class, supportedOpenmrsVersions = {
        "1.11.* - 2.1.*" })
public class PersonResource1_11 extends PersonResource1_10 {
	
	/**
	 * @see DelegatingCrudResource#getRepresentationDescription(Representation)
	 */
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		DelegatingResourceDescription description = super.getRepresentationDescription(rep);
		if (description != null) {
			description.addProperty("deathdateEstimated");
		}
		return description;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getCreatableProperties()
	 */
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription description = super.getCreatableProperties();
		description.addProperty("deathdateEstimated");
		return description;
	}
	
	/**
	 * @throws org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getUpdatableProperties()
	 */
	@Override
	public DelegatingResourceDescription getUpdatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = super.getUpdatableProperties();
		description.addProperty("deathdateEstimated");
		return description;
	}
	
	@Override
	public Model getGETModel(Representation rep) {
		return addNewProperties(super.getGETModel(rep), rep);
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		return addNewProperties(super.getCREATEModel(rep), rep);
	}
	
	@Override
	public Model getUPDATEModel(Representation rep) {
		return addNewProperties(super.getUPDATEModel(rep), rep);
	}
	
	private Model addNewProperties(Model model, Representation rep) {
		if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
			((ModelImpl) model)
			        .property("deathdateEstimated", new BooleanProperty()._default(false));
		}
		return model;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getResourceVersion()
	 */
	@Override
	public String getResourceVersion() {
		return RestConstants1_11.RESOURCE_VERSION;
	}
}
