/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import org.openmrs.Form;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.MetadataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import java.util.Arrays;
import java.util.List;

/**
 * {@link Resource} for {@link Form}, supporting standard CRUD operations
 */
@Resource(name = RestConstants.VERSION_1 + "/form", supportedClass = Form.class, supportedOpenmrsVersions = { "1.8.* - 9.*" })
public class FormResource1_8 extends MetadataDelegatingCrudResource<Form> {
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#getRepresentationDescription(org.openmrs.module.webservices.rest.web.representation.Representation)
	 */
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("display");
			description.addProperty("name");
			description.addProperty("description");
			description.addProperty("encounterType", Representation.REF);
			description.addProperty("version");
			description.addProperty("build");
			description.addProperty("published");
			description.addProperty("formFields", Representation.REF);
			description.addProperty("retired");
			description.addSelfLink();
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("display");
			description.addProperty("name");
			description.addProperty("description");
			description.addProperty("encounterType");
			description.addProperty("version");
			description.addProperty("build");
			description.addProperty("published");
			description.addProperty("formFields");
			description.addProperty("retired");
			description.addProperty("auditInfo");
			description.addSelfLink();
			return description;
		}
		return null;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getCreatableProperties()
	 */
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription description = super.getCreatableProperties();
		description.addRequiredProperty("version");
		
		description.addProperty("encounterType");
		description.addProperty("build");
		description.addProperty("published");
		description.addProperty("formFields");
		description.addProperty("xslt");
		description.addProperty("template");
		
		return description;
	}
	
	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl modelImpl = (ModelImpl) super.getGETModel(rep);
		if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
			modelImpl
			        .property("uuid", new StringProperty())
			        .property("display", new StringProperty())
			        .property("name", new StringProperty())
			        .property("description", new StringProperty())
			        .property("version", new StringProperty())
			        .property("build", new IntegerProperty())
			        .property("published", new BooleanProperty()._default(false))
			        .property("retired", new BooleanProperty());
		}
		if (rep instanceof DefaultRepresentation) {
			modelImpl
			        .property("encounterType", new RefProperty("#/definitions/EncountertypeGetRef"))
			        .property("formFields", new ArrayProperty(new RefProperty("#/definitions/FormFormfieldGetRef")));
		} else if (rep instanceof FullRepresentation) {
			modelImpl
			        .property("encounterType", new RefProperty("#/definitions/EncountertypeGet"))
			        .property("formFields", new ArrayProperty(new RefProperty("#/definitions/FormFormfieldGet")));
		}
		return modelImpl;
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		ModelImpl model = ((ModelImpl) super.getCREATEModel(rep))
		        .property("version", new StringProperty())
		        .property("encounterType", new StringProperty())
		        .property("build", new IntegerProperty())
		        .property("published", new BooleanProperty()._default(false))
		        .property("formFields", new ArrayProperty(new StringProperty()))
		        .property("xslt", new StringProperty())
		        .property("template", new StringProperty())
		        
		        .required("version");
		if (rep instanceof FullRepresentation) {
			model
			        .property("encounterType", new RefProperty("#/definitions/EncountertypeCreate"))
			        .property("formFields", new ArrayProperty(new RefProperty("#/definitions/FormFormfieldCreate")));
		}
		return model;
	}
	
	@Override
	public Model getUPDATEModel(Representation rep) {
		return getCREATEModel(rep);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getByUniqueId(java.lang.String)
	 */
	@Override
	public Form getByUniqueId(String uniqueId) {
		return Context.getFormService().getFormByUuid(uniqueId);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#newDelegate()
	 */
	@Override
	public Form newDelegate() {
		return new Form();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler#save(java.lang.Object)
	 */
	@Override
	public Form save(Form delegate) {
		return Context.getFormService().saveForm(delegate);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#purge(java.lang.Object,
	 *      org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public void purge(Form delegate, RequestContext context) throws ResponseException {
		if (delegate == null)
			return;
		Context.getFormService().purgeForm(delegate);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doGetAll(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected NeedsPaging<Form> doGetAll(RequestContext context) throws ResponseException {
		return new NeedsPaging<Form>(Context.getFormService().getAllForms(context.getIncludeAll()), context);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doSearch(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected NeedsPaging<Form> doSearch(RequestContext context) {
		return new NeedsPaging<Form>(Context.getFormService().getForms(context.getParameter("q"), false), context);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#getPropertiesToExposeAsSubResources()
	 */
	@Override
	public List<String> getPropertiesToExposeAsSubResources() {
		return Arrays.asList("formFields");
	}
	
}
