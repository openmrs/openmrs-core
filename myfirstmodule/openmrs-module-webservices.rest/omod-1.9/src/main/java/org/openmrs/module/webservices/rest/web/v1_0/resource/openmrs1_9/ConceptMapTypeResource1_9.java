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

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.BooleanProperty;
import org.openmrs.ConceptMapType;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.MetadataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * {@link Resource} for {@link ConceptMapType}, supporting standard CRUD operations
 */
@Resource(name = RestConstants.VERSION_1 + "/conceptmaptype", supportedClass = ConceptMapType.class, supportedOpenmrsVersions = {
        "1.9.* - 9.*" })
public class ConceptMapTypeResource1_9 extends MetadataDelegatingCrudResource<ConceptMapType> {
	
	/**
	 * @see DelegatingCrudResource#getRepresentationDescription(Representation)
	 */
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("display");
			description.addProperty("name");
			description.addProperty("description");
			description.addProperty("isHidden");
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
			description.addProperty("isHidden");
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
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		
		description.addRequiredProperty("name");
		description.addProperty("description");
		description.addProperty("isHidden");
		
		return description;
	}
	
	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl model = (ModelImpl) super.getGETModel(rep);
		if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
			model
			        .property("isHidden", new BooleanProperty());
		}
		return model;
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		return ((ModelImpl) super.getCREATEModel(rep))
		        .property("isHidden", new BooleanProperty());
	}
	
	/**
	 * @see DelegatingCrudResource#newDelegate()
	 */
	@Override
	public ConceptMapType newDelegate() {
		return new ConceptMapType();
	}
	
	/**
	 * @see DelegatingCrudResource#save(java.lang.Object)
	 */
	@Override
	public ConceptMapType save(ConceptMapType conceptMapType) {
		return Context.getConceptService().saveConceptMapType(conceptMapType);
	}
	
	/**
	 * Fetches a conceptMapType by uuid, if no match is found, it tries to look up one with a
	 * matching name with the assumption that the passed parameter is a conceptMapType name
	 * 
	 * @see DelegatingCrudResource#getByUniqueId(java.lang.String)
	 */
	@Override
	public ConceptMapType getByUniqueId(String uuid) {
		ConceptMapType conceptMapType = Context.getConceptService().getConceptMapTypeByUuid(uuid);
		//We assume the caller was fetching by name
		if (conceptMapType == null)
			conceptMapType = Context.getConceptService().getConceptMapTypeByName(uuid);
		
		return conceptMapType;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#purge(java.lang.Object,
	 *      org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public void purge(ConceptMapType conceptMapType, RequestContext context) throws ResponseException {
		if (conceptMapType == null)
			return;
		Context.getConceptService().purgeConceptMapType(conceptMapType);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doGetAll(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected NeedsPaging<ConceptMapType> doGetAll(RequestContext context) {
		//Note that if includeAll is set to false, then types marked as hidden will be excluded
		return new NeedsPaging<ConceptMapType>(Context.getConceptService().getConceptMapTypes(context.getIncludeAll(),
		    context.getIncludeAll()), context);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doSearch(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected NeedsPaging<ConceptMapType> doSearch(RequestContext context) {
		List<ConceptMapType> types = Context.getConceptService().getConceptMapTypes(context.getIncludeAll(),
		    context.getIncludeAll());
		for (Iterator<ConceptMapType> iterator = types.iterator(); iterator.hasNext();) {
			ConceptMapType type = iterator.next();
			//find matches excluding retired ones if necessary
			if (!Pattern.compile(Pattern.quote(context.getParameter("q")), Pattern.CASE_INSENSITIVE).matcher(type.getName())
			        .find()) {
				iterator.remove();
			}
		}
		return new NeedsPaging<ConceptMapType>(types, context);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getResourceVersion()
	 */
	@Override
	public String getResourceVersion() {
		return "1.9";
	}
}
