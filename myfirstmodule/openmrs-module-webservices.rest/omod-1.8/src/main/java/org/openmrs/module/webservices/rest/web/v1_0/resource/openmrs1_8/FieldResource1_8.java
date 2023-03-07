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
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import org.openmrs.Field;
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
 * {@link Resource} for {@link Field}, supporting standard CRUD operations
 */
@Resource(name = RestConstants.VERSION_1 + "/field", supportedClass = Field.class, supportedOpenmrsVersions = { "1.8.* - 9.*" })
public class FieldResource1_8 extends MetadataDelegatingCrudResource<Field> {
	
	public Model getGETModel(Representation rep) {
		ModelImpl modelImpl = (ModelImpl) super.getGETModel(rep);
		if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
			modelImpl
			        .property("tableName", new StringProperty())
			        .property("attributeName", new StringProperty())
			        .property("defaultValue", new StringProperty())
			        .property("selectMultiple", new BooleanProperty()._default(false));
		}
		if (rep instanceof DefaultRepresentation) {
			modelImpl
			        .property("fieldType", new RefProperty("#/definitions/FieldtypeGetRef"))
			        .property("concept", new RefProperty("#/definitions/ConceptGetRef"));
		} else if (rep instanceof FullRepresentation) {
			modelImpl
			        .property("fieldType", new RefProperty("#/definitions/FieldtypeGet"))
			        .property("concept", new RefProperty("#/definitions/ConceptGet"));
		}
		return modelImpl;
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		return ((ModelImpl) super.getCREATEModel(rep))
		        .property("fieldType", new RefProperty("#/definitions/FieldtypeCreate"))
		        .property("selectMultiple", new BooleanProperty()._default(false))
		        .property("concept", new RefProperty("#/definitions/ConceptCreate"))
		        .property("tableName", new StringProperty())
		        .property("attributeName", new StringProperty())
		        .property("defaultValue", new StringProperty())
		        
		        .required("fieldType").required("selectMultiple");
	}
	
	@Override
	public Model getUPDATEModel(Representation representation) {
		return new ModelImpl(); //FIXME missing props
	}
	
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
			description.addProperty("fieldType", Representation.REF);
			description.addProperty("concept", Representation.REF);
			description.addProperty("tableName");
			description.addProperty("attributeName");
			description.addProperty("defaultValue");
			description.addProperty("selectMultiple");
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
			description.addProperty("fieldType");
			description.addProperty("concept");
			description.addProperty("tableName");
			description.addProperty("attributeName");
			description.addProperty("defaultValue");
			description.addProperty("selectMultiple");
			description.addProperty("retired");
			description.addProperty("auditInfo");
			description.addSelfLink();
			return description;
		}
		return null;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.MetadataDelegatingCrudResource#getCreatableProperties()
	 */
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription description = super.getCreatableProperties();
		description.addRequiredProperty("fieldType");
		description.addRequiredProperty("selectMultiple");
		
		description.addProperty("concept");
		description.addProperty("tableName");
		description.addProperty("attributeName");
		description.addProperty("defaultValue");
		
		return description;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getByUniqueId(java.lang.String)
	 */
	@Override
	public Field getByUniqueId(String uniqueId) {
		return Context.getFormService().getFieldByUuid(uniqueId);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#newDelegate()
	 */
	@Override
	public Field newDelegate() {
		return new Field();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler#save(java.lang.Object)
	 */
	@Override
	public Field save(Field delegate) {
		return Context.getFormService().saveField(delegate);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#purge(java.lang.Object,
	 *      org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public void purge(Field delegate, RequestContext context) throws ResponseException {
		if (delegate == null)
			return;
		Context.getFormService().purgeField(delegate);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doGetAll(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected NeedsPaging<Field> doGetAll(RequestContext context) throws ResponseException {
		return new NeedsPaging<Field>(Context.getFormService().getAllFields(context.getIncludeAll()), context);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#getPropertiesToExposeAsSubResources()
	 */
	@Override
	public List<String> getPropertiesToExposeAsSubResources() {
		return Arrays.asList("answers");
	}
}
