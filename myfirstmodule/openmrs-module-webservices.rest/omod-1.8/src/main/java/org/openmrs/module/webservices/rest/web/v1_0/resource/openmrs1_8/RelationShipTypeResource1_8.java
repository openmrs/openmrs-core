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

import org.apache.commons.lang.StringUtils;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.StringProperty;
import org.openmrs.RelationshipType;
import org.openmrs.api.context.Context;
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

import java.util.ArrayList;
import java.util.List;

@Resource(name = RestConstants.VERSION_1 + "/relationshiptype", supportedClass = RelationshipType.class, supportedOpenmrsVersions = {
        "1.8.* - 9.*" })
public class RelationShipTypeResource1_8 extends MetadataDelegatingCrudResource<RelationshipType> {
	
	public RelationShipTypeResource1_8() {
		super();
		this.propertiesIgnoredWhenUpdating.add("displayAIsToB");
		this.propertiesIgnoredWhenUpdating.add("displayBIsToA");
	}
	
	/**
	 * Fetches a relationshipType by uuid, if no match is found, it tries to look up one with a
	 * matching name with the assumption that the passed parameter is a relationshipType name
	 * 
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#getByUniqueId(java.lang.String)
	 */
	@Override
	public RelationshipType getByUniqueId(String uuid) {
		RelationshipType relationshipType = Context.getPersonService().getRelationshipTypeByUuid(uuid);
		if (relationshipType == null) {
			List<RelationshipType> relationshipTypes = Context.getPersonService().getAllRelationshipTypes();
			for (RelationshipType possibleRelationshipType : relationshipTypes) {
				if (possibleRelationshipType.getaIsToB().equalsIgnoreCase(uuid)
				        || possibleRelationshipType.getbIsToA().equalsIgnoreCase(uuid)) {
					relationshipType = possibleRelationshipType;
					break;
				}
			}
		}
		return relationshipType;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#newDelegate()
	 */
	@Override
	public RelationshipType newDelegate() {
		return new RelationshipType();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#save(java.lang.Object)
	 */
	@Override
	public RelationshipType save(RelationshipType relationshipType) {
		return Context.getPersonService().saveRelationshipType(relationshipType);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#purge(java.lang.Object,
	 *      org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public void purge(RelationshipType relationshipType, RequestContext context) throws ResponseException {
		if (relationshipType == null) {
			return;
		}
		Context.getPersonService().purgeRelationshipType(relationshipType);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#getRepresentationDescription(Representation)
	 */
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("uuid");
		description.addProperty("display");
		description.addProperty("description");
		description.addProperty("aIsToB");
		description.addProperty("bIsToA");
		description.addProperty("displayAIsToB");
		description.addProperty("displayBIsToA");
		description.addProperty("retired");
		description.addSelfLink();
		if (rep instanceof DefaultRepresentation) {
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			return description;
		} else if (rep instanceof FullRepresentation) {
			description.addProperty("weight");
			description.addProperty("auditInfo");
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
		
		description.addRequiredProperty("aIsToB");
		description.addRequiredProperty("bIsToA");
		description.addProperty("weight");
		description.addProperty("description");
		return description;
	}
	
	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl model = (ModelImpl) super.getGETModel(rep);
		if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
			model
			        .property("aIsToB", new StringProperty())
			        .property("bIsToA", new StringProperty());
		}
		if (rep instanceof FullRepresentation) {
			model
			        .property("weight", new IntegerProperty());
		}
		return model;
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		return ((ModelImpl) super.getCREATEModel(rep))
		        .property("aIsToB", new StringProperty())
		        .property("bIsToA", new StringProperty())
		        .property("weight", new IntegerProperty())
		        
		        .required("aIsToB").required("bIsToA");
	}
	
	@Override
	public Model getUPDATEModel(Representation rep) {
		return new ModelImpl(); //FIXME missing props
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doGetAll(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected NeedsPaging<RelationshipType> doGetAll(RequestContext context) {
		//Apparently, in 1.9.0 this method returns all and has no argument for excluding retired ones
		List<RelationshipType> relationshipTypes = Context.getPersonService().getAllRelationshipTypes();
		List<RelationshipType> unRetiredRelationshipTypes = new ArrayList<RelationshipType>();
		for (RelationshipType relationshipType : relationshipTypes) {
			if (!relationshipType.isRetired())
				unRetiredRelationshipTypes.add(relationshipType);
		}
		return new NeedsPaging<RelationshipType>(unRetiredRelationshipTypes, context);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doSearch(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected NeedsPaging<RelationshipType> doSearch(RequestContext context) {
		String queryString = context.getParameter("q");
		List<RelationshipType> allRelationshipTypes = Context.getPersonService().getAllRelationshipTypes();
		List<RelationshipType> unRetiredRelationshipTypes = new ArrayList<RelationshipType>();
		for (RelationshipType relationshipType : allRelationshipTypes) {
			if (!relationshipType.isRetired()
			        && (relationshipType.getaIsToB().contains(queryString) || relationshipType.getbIsToA().contains(
			            queryString)))
				unRetiredRelationshipTypes.add(relationshipType);
		}
		return new NeedsPaging<RelationshipType>(unRetiredRelationshipTypes, context);
	}
	
	@Override
	@PropertyGetter("display")
	public String getDisplayString(RelationshipType delegate) {
		// TODO i18n based on message properties
		return delegate.toString();
	}
	
	@PropertyGetter("displayAIsToB")
	public String getDisplayAIsToBe(RelationshipType delegate) {
		String localization = getLocalization(delegate.getUuid(), "aIsToB");
		if (localization != null) {
			return localization;
		} else {
			return StringUtils.isEmpty(delegate.getaIsToB()) ? "" : delegate.getaIsToB();
		}
	}
	
	@PropertyGetter("displayBIsToA")
	public String getDisplayBIsToAe(RelationshipType delegate) {
		String localization = getLocalization(delegate.getUuid(), "bIsToA");
		if (localization != null) {
			return localization;
		} else {
			return StringUtils.isEmpty(delegate.getbIsToA()) ? "" : delegate.getbIsToA();
		}
	}
	
	private String getLocalization(String uuid, String type) {
		String code = "ui.i18n.RelationshipType" + "." + type + "." + uuid;
		String localization = Context.getMessageSourceService().getMessage(code);
		if (localization == null || localization.equals(code)) {
			return null;
		} else {
			return localization;
		}
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getResourceVersion()
	 */
	@Override
	public String getResourceVersion() {
		return "1.8";
	}
	
}
