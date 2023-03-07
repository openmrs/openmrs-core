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

import org.openmrs.Form;
import org.openmrs.FormResource;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.FormResource1_8;

import java.util.List;

@Resource(name = RestConstants.VERSION_1 + "/form", supportedClass = Form.class, order = 10, supportedOpenmrsVersions = {
        "1.9.* - 9.*" })
public class FormResource1_9 extends FormResource1_8 {
	
	// TODO: Find out why autowiring is failing, in the mean time use Context.getService
	//    @Autowired
	//    private FormService formService;
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		DelegatingResourceDescription description = super.getRepresentationDescription(rep);
		if (rep instanceof DefaultRepresentation) {
			description.addProperty("resources", Representation.REF);
			return description;
		} else if (rep instanceof FullRepresentation) {
			description.removeProperty("xslt");
			description.removeProperty("template");
			description.addProperty("resources");
			return description;
		}
		return null;
	}
	
	@PropertyGetter("resources")
	public List<FormResource> getFormResources(Form form) {
		FormService formService = Context.getFormService();
		return (List<FormResource>) formService.getFormResourcesForForm(form);
	}
	
	@PropertySetter("resources")
	public void setFormResources(Form form, List<FormResource> resources) {
		for (FormResource resource : resources) {
			resource.setForm(form);
		}
	}
	
	@Override
	public String getResourceVersion() {
		return RestConstants1_9.RESOURCE_VERSION;
	}
}
