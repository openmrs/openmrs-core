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
import io.swagger.models.properties.DoubleProperty;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import org.openmrs.Concept;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.MetadataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.util.OpenmrsUtil;

/**
 * Allows standard CRUD for the {@link PersonAttributeType} domain object
 */
@Resource(name = RestConstants.VERSION_1 + "/personattributetype", supportedClass = PersonAttributeType.class, supportedOpenmrsVersions = {
        "1.8.* - 9.*" })
public class PersonAttributeTypeResource1_8 extends MetadataDelegatingCrudResource<PersonAttributeType> {
	
	public PersonAttributeTypeResource1_8() {
		
	}
	
	private PersonService service() {
		return Context.getPersonService();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getRepresentationDescription(org.openmrs.module.webservices.rest.web.representation.Representation)
	 */
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("display");
			description.addProperty("name");
			description.addProperty("description");
			description.addProperty("format");
			description.addProperty("foreignKey");
			description.addProperty("sortWeight");
			description.addProperty("searchable");
			description.addProperty("editPrivilege");
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
			description.addProperty("format");
			description.addProperty("foreignKey");
			description.addProperty("sortWeight");
			description.addProperty("searchable");
			description.addProperty("editPrivilege");
			description.addProperty("retired");
			description.addProperty("concept");
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
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addRequiredProperty("name");
		description.addRequiredProperty("description");
		description.addProperty("format");
		description.addProperty("foreignKey");
		description.addProperty("sortWeight");
		description.addProperty("searchable");
		description.addProperty("editPrivilege");
		return description;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getUpdatableProperties()
	 */
	@Override
	public DelegatingResourceDescription getUpdatableProperties() {
		return getCreatableProperties();
	}
	
	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl model = (ModelImpl) super.getGETModel(rep);
		if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
			model
			        .property("format", new StringProperty())
			        .property("foreignKey", new IntegerProperty())
			        .property("sortWeight", new DoubleProperty())
			        .property("searchable", new BooleanProperty()._default(false));
		}
		if (rep instanceof DefaultRepresentation) {
			model
			        .property("editPrivilege", new RefProperty("#/definitions/PrivilegeGetRef"));
		} else if (rep instanceof FullRepresentation) {
			model
			        .property("editPrivilege", new RefProperty("#/definitions/PrivilegeGet"))
			        .property("concept", new StringProperty());
		}
		return model;
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		return ((ModelImpl) super.getCREATEModel(rep))
		        .property("format", new StringProperty().example("java.lang.String"))
		        .property("foreignKey", new IntegerProperty())
		        .property("sortWeight", new DoubleProperty())
		        .property("searchable", new BooleanProperty()._default(false))
		        .property("editPrivilege", new RefProperty("#/definitions/PrivilegeCreate"))
		        
		        .required("description");
	}
	
	@Override
	public Model getUPDATEModel(Representation rep) {
		return getCREATEModel(rep);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getByUniqueId(java.lang.String)
	 */
	@Override
	public PersonAttributeType getByUniqueId(String uniqueId) {
		return service().getPersonAttributeTypeByUuid(uniqueId);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doGetAll(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected NeedsPaging<PersonAttributeType> doGetAll(RequestContext context) throws ResponseException {
		return new NeedsPaging<PersonAttributeType>(service().getAllPersonAttributeTypes(context.getIncludeAll()), context);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#newDelegate()
	 */
	@Override
	public PersonAttributeType newDelegate() {
		return new PersonAttributeType();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler#save(java.lang.Object)
	 */
	@Override
	public PersonAttributeType save(PersonAttributeType delegate) {
		return service().savePersonAttributeType(delegate);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#purge(java.lang.Object,
	 *      org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public void purge(PersonAttributeType delegate, RequestContext context) throws ResponseException {
		service().purgePersonAttributeType(delegate);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doSearch(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected NeedsPaging<PersonAttributeType> doSearch(RequestContext context) {
		return new NeedsPaging<PersonAttributeType>(service().getPersonAttributeTypes(context.getParameter("q"), null, null,
		    null), context);
	}
	
	@PropertyGetter("concept")
	public Object getConcept(PersonAttributeType delegate) {
		if (OpenmrsUtil.nullSafeEquals(delegate.getFormat(), Concept.class.getCanonicalName())) {
			Concept concept = Context.getConceptService().getConcept(delegate.getForeignKey());
			return ConversionUtil.convertToRepresentation(concept, Representation.FULL);
		}
		return null;
	}
}
