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
import io.swagger.models.properties.StringProperty;
import org.openmrs.ConceptSource;
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * {@link Resource} for {@link ConceptSource}, supporting standard CRUD operations
 */
@Resource(name = RestConstants.VERSION_1 + "/conceptsource", supportedClass = ConceptSource.class, supportedOpenmrsVersions = {
        "1.8.* - 1.12.*" })
public class ConceptSourceResource1_8 extends MetadataDelegatingCrudResource<ConceptSource> {
	
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
			description.addProperty("hl7Code");
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
			description.addProperty("hl7Code");
			description.addProperty("retired");
			description.addProperty("auditInfo");
			description.addSelfLink();
			return description;
		}
		return null;
	}
	
	public Model getGETModel(Representation rep) {
		return ((ModelImpl) super.getGETModel(rep))
		        .property("uuid", new StringProperty())
		        .property("display", new StringProperty())
		        .property("name", new StringProperty())
		        .property("description", new StringProperty())
		        .property("hl7Code", new StringProperty())
		        .property("retired", new BooleanProperty());
	}
	
	@Override
	public Model getCREATEModel(Representation representation) {
		return new ModelImpl()
		        .property("name", new StringProperty())
		        .property("description", new StringProperty())
		        .property("hl7Code", new StringProperty())
		        .required("name").required("description");
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getCreatableProperties()
	 */
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription description = super.getCreatableProperties();
		description.addRequiredProperty("name");
		description.addRequiredProperty("description");
		description.addProperty("hl7Code");
		
		return description;
	}
	
	/**
	 * @see DelegatingCrudResource#newDelegate()
	 */
	@Override
	public ConceptSource newDelegate() {
		return new ConceptSource();
	}
	
	/**
	 * Fetches a conceptSource by uuid, if no match is found, it tries to look up one with a
	 * matching name with the assumption that the passed parameter is a conceptSource name
	 * 
	 * @see DelegatingCrudResource#getByUniqueId(java.lang.String)
	 */
	@Override
	public ConceptSource getByUniqueId(String uuid) {
		ConceptSource conceptSource = Context.getConceptService().getConceptSourceByUuid(uuid);
		//We assume the caller was fetching by name
		if (conceptSource == null)
			conceptSource = Context.getConceptService().getConceptSourceByName(uuid);
		
		return conceptSource;
	}
	
	/**
	 * @see DelegatingCrudResource#save(java.lang.Object)
	 */
	@Override
	public ConceptSource save(ConceptSource conceptSource) {
		return Context.getConceptService().saveConceptSource(conceptSource);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#purge(java.lang.Object,
	 *      org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public void purge(ConceptSource conceptSource, RequestContext context) throws ResponseException {
		if (conceptSource == null)
			return;
		Context.getConceptService().purgeConceptSource(conceptSource);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doGetAll(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected NeedsPaging<ConceptSource> doGetAll(RequestContext context) {
		return doGetAll(context, Context.getConceptService().getAllConceptSources());
	}
	
	protected NeedsPaging<ConceptSource> doGetAll(RequestContext context, List<ConceptSource> sources) {
		if (context.getIncludeAll()) {
			return new NeedsPaging<ConceptSource>(sources, context);
		}
		List<ConceptSource> unretiredSources = new ArrayList<ConceptSource>();
		for (ConceptSource conceptSource : sources) {
			if (!conceptSource.isRetired())
				unretiredSources.add(conceptSource);
		}
		return new NeedsPaging<ConceptSource>(unretiredSources, context);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doSearch(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected NeedsPaging<ConceptSource> doSearch(RequestContext context) {
		return doSearch(context, Context.getConceptService().getAllConceptSources());
	}
	
	protected NeedsPaging<ConceptSource> doSearch(RequestContext context, List<ConceptSource> sources) {
		for (Iterator<ConceptSource> iterator = sources.iterator(); iterator.hasNext();) {
			ConceptSource conceptSource = iterator.next();
			//find matches excluding retired ones if necessary
			if (!Pattern.compile(Pattern.quote(context.getParameter("q")), Pattern.CASE_INSENSITIVE)
			        .matcher(conceptSource.getName()).find()
			        || (!context.getIncludeAll() && conceptSource.isRetired())) {
				iterator.remove();
			}
		}
		return new NeedsPaging<ConceptSource>(sources, context);
	}
}
