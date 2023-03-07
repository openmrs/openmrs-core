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

import java.util.ArrayList;
import java.util.List;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import org.openmrs.Field;
import org.openmrs.FieldAnswer;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.RepHandler;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.annotation.SubResource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubResource;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ConversionException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

/**
 * {@link Resource} for {@link FieldAnswer}, supporting standard CRUD operations
 */
@SubResource(parent = FieldResource1_8.class, path = "answer", supportedClass = FieldAnswer.class, supportedOpenmrsVersions = {
        "1.8.* - 9.*" })
public class FieldAnswerResource1_8 extends DelegatingSubResource<FieldAnswer, Field, FieldResource1_8> {
	
	@Override
	@RepHandler(RefRepresentation.class)
	public SimpleObject asRef(FieldAnswer delegate) throws ConversionException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("uuid");
		description.addProperty("display");
		description.addSelfLink();
		return convertDelegateToRepresentation(delegate, description);
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
			description.addProperty("concept", Representation.REF);
			description.addProperty("field", Representation.REF);
			description.addSelfLink();
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("display");
			description.addProperty("concept");
			description.addProperty("field");
			description.addProperty("auditInfo");
			description.addSelfLink();
			return description;
		}
		return null;
	}
	
	public Model getGETModel(Representation rep) {
		ModelImpl modelImpl = (ModelImpl) super.getGETModel(rep);
		if (rep instanceof DefaultRepresentation) {
			modelImpl
			        .property("uuid", new StringProperty())
			        .property("display", new StringProperty())
			        .property("concept", new RefProperty("#/definitions/ConceptGetRef"))
			        .property("field", new RefProperty("#/definitions/FieldGetRef"));
		} else if (rep instanceof FullRepresentation) {
			modelImpl
			        .property("uuid", new StringProperty())
			        .property("display", new StringProperty())
			        .property("concept", new RefProperty("#/definitions/ConceptGet"))
			        .property("field", new RefProperty("#/definitions/FieldGet"));
		}
		return modelImpl;
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		ModelImpl model = new ModelImpl()
		        .property("concept", new StringProperty().example("uuid"))
		        .property("field", new StringProperty().example("uuid"))
		        .required("field").required("concept");
		if (rep instanceof FullRepresentation) {
			model
			        .property("concept", new RefProperty("#/definitions/ConceptCreate"))
			        .property("field", new RefProperty("#/definitions/FieldCreate"));
		}
		return model;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.MetadataDelegatingCrudResource#getCreatableProperties()
	 */
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addRequiredProperty("field");
		description.addRequiredProperty("concept");
		
		return description;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getByUniqueId(java.lang.String)
	 */
	@Override
	public FieldAnswer getByUniqueId(String uniqueId) {
		return Context.getFormService().getFieldAnswerByUuid(uniqueId);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#newDelegate()
	 */
	@Override
	public FieldAnswer newDelegate() {
		return new FieldAnswer();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler#save(java.lang.Object)
	 */
	@Override
	public FieldAnswer save(FieldAnswer delegate) {
		throw new UnsupportedOperationException("A field answer must be added to a field, not created on its own.");
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#purge(java.lang.Object,
	 *      org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public void purge(FieldAnswer delegate, RequestContext context) throws ResponseException {
		throw new UnsupportedOperationException("A field answer must be removed from a field, not purged on its own.");
	}
	
	@Override
	protected void delete(FieldAnswer delegate, String reason, RequestContext context) throws ResponseException {
		throw new UnsupportedOperationException("A field answer must be removed from a field, not deleted on its own.");
	}
	
	/**
	 * @param encounter
	 * @return encounter type and date
	 */
	@PropertyGetter("display")
	public String getDisplayString(FieldAnswer delegate) {
		if (delegate == null)
			return null;
		
		return new StringBuilder().append(delegate.getField() == null ? "Null Field" : delegate.getField().getName())
		        .append(" - ")
		        .append(delegate.getConcept() == null ? "Null Concept" : delegate.getConcept().getName().toString())
		        .toString();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubResource#getParent(java.lang.Object)
	 */
	@Override
	public Field getParent(FieldAnswer instance) {
		return instance.getField();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubResource#setParent(java.lang.Object,
	 *      java.lang.Object)
	 */
	@Override
	public void setParent(FieldAnswer instance, Field parent) {
		instance.setField(parent);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubResource#doGetAll(java.lang.Object,
	 *      org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public NeedsPaging<FieldAnswer> doGetAll(Field parent, RequestContext context) throws ResponseException {
		List<FieldAnswer> fieldAnswers = new ArrayList<FieldAnswer>();
		if (parent.getAnswers() != null) {
			fieldAnswers.addAll(parent.getAnswers());
		}
		return new NeedsPaging<FieldAnswer>(fieldAnswers, context);
	}
	
}
