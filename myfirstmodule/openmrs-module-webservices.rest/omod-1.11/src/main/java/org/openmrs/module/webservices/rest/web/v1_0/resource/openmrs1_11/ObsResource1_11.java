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
import io.swagger.models.properties.StringProperty;
import org.openmrs.Obs;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_9.ObsResource1_9;

@Resource(name = RestConstants.VERSION_1 + "/obs", order = 1, supportedClass = Obs.class, supportedOpenmrsVersions = {
        "1.11.* - 2.0.*" })
public class ObsResource1_11 extends ObsResource1_9 {
	
	/**
	 * @see DelegatingCrudResource#getRepresentationDescription(Representation)
	 */
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		DelegatingResourceDescription description = super.getRepresentationDescription(rep);
		if (description != null) {
			description.addProperty("formFieldPath");
			description.addProperty("formFieldNamespace");
		}
		return description;
	}
	
	@Override
	public Model getGETModel(Representation rep) {
		return ((ModelImpl) super.getGETModel(rep))
		        .property("formFieldPath", new StringProperty())
		        .property("formFieldNamespace", new StringProperty());
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		return ((ModelImpl) super.getCREATEModel(rep))
		        .property("formFieldPath", new StringProperty())
		        .property("formFieldNamespace", new StringProperty());
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getCreatableProperties()
	 */
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription description = super.getCreatableProperties();
		description.addProperty("formFieldPath");
		description.addProperty("formFieldNamespace");
		return description;
	}
	
	/**
	 * Annotated setter for formFieldPath
	 * 
	 * @param obs
	 * @param formFieldPath
	 */
	@PropertySetter("formFieldPath")
	public static void setFormFieldPath(Obs obs, Object formFieldPath) {
		obs.setFormField(obs.getFormFieldNamespace(), (String)formFieldPath);
	}
	
	/**
	 * Annotated setter for formFieldNamespace
	 * 
	 * @param obs
	 * @param namespace
	 */
	@PropertySetter("formFieldNamespace")
	public static void setFormFieldNamespace(Obs obs, Object namespace) {
		obs.setFormField((String)namespace, obs.getFormFieldPath());
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getResourceVersion()
	 */
	@Override
	public String getResourceVersion() {
		return RestConstants1_11.RESOURCE_VERSION;
	}
}
